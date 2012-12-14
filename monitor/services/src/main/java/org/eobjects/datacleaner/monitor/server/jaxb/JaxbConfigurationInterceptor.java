/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.datacleaner.monitor.server.jaxb;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eobjects.analyzer.beans.api.ColumnProperty;
import org.eobjects.analyzer.beans.api.SchemaProperty;
import org.eobjects.analyzer.beans.api.TableProperty;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.JaxbPojoDatastoreAdaptor;
import org.eobjects.analyzer.configuration.jaxb.AbstractDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.ClasspathScannerType;
import org.eobjects.analyzer.configuration.jaxb.ClasspathScannerType.Package;
import org.eobjects.analyzer.configuration.jaxb.Configuration;
import org.eobjects.analyzer.configuration.jaxb.ConfigurationMetadataType;
import org.eobjects.analyzer.configuration.jaxb.DatastoreCatalogType;
import org.eobjects.analyzer.configuration.jaxb.MultithreadedTaskrunnerType;
import org.eobjects.analyzer.configuration.jaxb.ObjectFactory;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.HasBeanConfiguration;
import org.eobjects.analyzer.util.JaxbValidationEventHandler;
import org.eobjects.analyzer.util.SchemaNavigator;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.configuration.ConfigurationFactory;
import org.eobjects.datacleaner.monitor.configuration.JobContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.server.ConfigurationInterceptor;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.Func;
import org.eobjects.metamodel.util.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Interceptor class which transforms a tenant's configuration as it is being
 * used at runtime on the server, into it's form as seen by the client.
 * 
 * There are many differences in these two variants of the configuration files,
 * but also a few similarities:
 * 
 * <ul>
 * <li>The datastores are the same.</li>
 * <li>The reference data items are the same.</li>
 * <li>The task runners are different (on the server side a shared task runner
 * for all tenants are used).</li>
 * <li>The descriptor providers are different (on the server side a shared
 * descriptor provider is used for all tenants).</li>
 * </ul>
 */
@Component("configurationInterceptor")
public class JaxbConfigurationInterceptor implements ConfigurationInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JaxbConfigurationInterceptor.class);
    private static final Integer MAX_POJO_ROWS = 20;

    private final JAXBContext _jaxbContext;
    private final ConfigurationFactory _configurationFactory;
    private final Ref<Calendar> _calRef;
    private final TenantContextFactory _contextFactory;
    private final boolean _replaceDatastores;

    @Autowired
    public JaxbConfigurationInterceptor(TenantContextFactory contextFactory, ConfigurationFactory configurationFactory)
            throws JAXBException {
        this(contextFactory, configurationFactory, true);
    }

    public JaxbConfigurationInterceptor(TenantContextFactory contextFactory, ConfigurationFactory configurationFactory,
            boolean replaceDatastores) throws JAXBException {
        this(contextFactory, configurationFactory, replaceDatastores, new Ref<Calendar>() {
            @Override
            public Calendar get() {
                return Calendar.getInstance();
            }
        });
    }

    public JaxbConfigurationInterceptor(TenantContextFactory contextFactory, ConfigurationFactory configurationFactory,
            boolean replaceDatastores, Ref<Calendar> calRef) throws JAXBException {
        _contextFactory = contextFactory;
        _configurationFactory = configurationFactory;
        _replaceDatastores = replaceDatastores;
        _jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(),
                ObjectFactory.class.getClassLoader());
        _calRef = calRef;
    }

    @Override
    public void intercept(final String tenantId, final JobContext job, final String datastoreName,
            final InputStream in, final OutputStream out) throws Exception {
        final TenantContext context = _contextFactory.getContext(tenantId);

        final Unmarshaller unmarshaller = _jaxbContext.createUnmarshaller();
        final Configuration configuration = (Configuration) unmarshaller.unmarshal(in);

        // replace datastore catalog
        if (_replaceDatastores) {
            final DatastoreCatalogType originalDatastoreCatalog = configuration.getDatastoreCatalog();
            final DatastoreCatalogType newDatastoreCatalog = interceptDatastoreCatalog(context, job, datastoreName,
                    originalDatastoreCatalog);
            configuration.setDatastoreCatalog(newDatastoreCatalog);
        }

        // set appropriate descriptor provider
        configuration.setCustomDescriptorProvider(null);
        final List<String> scannedPackages = _configurationFactory.getScannedPackages();
        final ClasspathScannerType descriptorProvider = new ClasspathScannerType();
        for (String packageName : scannedPackages) {
            descriptorProvider.getPackage().add(newPackage(packageName, true));
        }
        configuration.setClasspathScanner(descriptorProvider);

        // set appropriate task runner
        configuration.setCustomTaskrunner(null);
        configuration.setSinglethreadedTaskrunner(null);

        final MultithreadedTaskrunnerType taskRunner = new MultithreadedTaskrunnerType();
        taskRunner.setMaxThreads(_configurationFactory.getNumThreads().shortValue());
        configuration.setMultithreadedTaskrunner(taskRunner);

        // set a configuration metadata element with the tenants name
        final ConfigurationMetadataType configurationMetadata = new ConfigurationMetadataType();
        configurationMetadata.setConfigurationName("DataCleaner monitor configuration for tenant " + tenantId);
        configuration.setConfigurationMetadata(configurationMetadata);
        configurationMetadata.setUpdatedDate(createDate(_calRef.get()));
        configurationMetadata.setAuthor("Automatically generated");

        final Marshaller marshaller = createMarshaller();
        marshaller.marshal(configuration, out);
    }

    /**
     * Replaces all "live" datastores with POJO based datastores. This will
     * allow working with sample data, but not modifying live data on the
     * server.
     * 
     * @param context
     * @param job
     * @param datastoreName
     * 
     * @param originalDatastoreCatalog
     * @return
     */
    private DatastoreCatalogType interceptDatastoreCatalog(final TenantContext context, final JobContext job,
            final String datastoreName, final DatastoreCatalogType originalDatastoreCatalog) {
        final AnalyzerBeansConfiguration configuration = context.getConfiguration();
        final DatastoreCatalogType newDatastoreCatalog = new DatastoreCatalogType();

        final DatastoreCatalog datastoreCatalog = configuration.getDatastoreCatalog();

        // Create a map of all used datastores and the columns which are
        // being accessed within them.
        Map<String, Set<Column>> datastoreUsage = new HashMap<String, Set<Column>>();

        if (job == null) {
            if (StringUtils.isNullOrEmpty(datastoreName)) {
                // represent all datastores (no job-specific information is
                // known)
                for (final String name : datastoreCatalog.getDatastoreNames()) {
                    datastoreUsage.put(name, new HashSet<Column>());
                }
            } else {
                // represent only a single datastore
                datastoreUsage.put(datastoreName, new HashSet<Column>());
            }
        } else {
            AnalysisJob analysisJob = job.getAnalysisJob();
            Collection<InputColumn<?>> sourceColumns = analysisJob.getSourceColumns();
            List<Column> columns = CollectionUtils.map(sourceColumns, new Func<InputColumn<?>, Column>() {
                @Override
                public Column eval(InputColumn<?> col) {
                    return col.getPhysicalColumn();
                }
            });

            // represent the datastore of the job
            datastoreUsage.put(analysisJob.getDatastore().getName(), new LinkedHashSet<Column>(columns));

            buildDatastoreUsageMap(datastoreUsage, analysisJob.getExplorerJobs());
            buildDatastoreUsageMap(datastoreUsage, analysisJob.getFilterJobs());
            buildDatastoreUsageMap(datastoreUsage, analysisJob.getTransformerJobs());
            buildDatastoreUsageMap(datastoreUsage, analysisJob.getAnalyzerJobs());
        }

        final Set<Entry<String, Set<Column>>> datastoreUsageEntries = datastoreUsage.entrySet();
        for (Entry<String, Set<Column>> entry : datastoreUsageEntries) {
            final String name = entry.getKey();
            final Set<Column> columns = entry.getValue();
            final Datastore datastore = datastoreCatalog.getDatastore(name);
            if (datastore != null) {
                try {
                    JaxbPojoDatastoreAdaptor adaptor = new JaxbPojoDatastoreAdaptor();
                    final AbstractDatastoreType pojoDatastoreType = adaptor.createPojoDatastore(datastore, columns,
                            MAX_POJO_ROWS);
                    newDatastoreCatalog.getJdbcDatastoreOrAccessDatastoreOrCsvDatastore().add(pojoDatastoreType);
                } catch (Exception e) {
                    // allow omitting errornous datastores here.
                    logger.error("Failed to serialize datastore '" + name + "' to POJO format: " + e.getMessage(), e);
                }
            }
        }

        return newDatastoreCatalog;
    }

    /**
     * Builds the map of datastore usage by scanning all {@link ComponentJob}s
     * given for configured {@link Datastore} properties.
     * 
     * @param datastoreUsage
     * @param componentJobs
     */
    private void buildDatastoreUsageMap(final Map<String, Set<Column>> datastoreUsage,
            final Collection<? extends HasBeanConfiguration> componentJobs) {
        for (HasBeanConfiguration componentJob : componentJobs) {
            final Set<ConfiguredPropertyDescriptor> datastoreProperties = componentJob.getDescriptor()
                    .getConfiguredPropertiesByType(Datastore.class, true);
            if (datastoreProperties.size() > 1) {
                // we do not know any cases where this will happen, but
                // it might potentially happen in a third party
                // extension.
                logger.warn(
                        "Multiple datastore properties found in {}. Datastore usage will be built, but usage scenario cannot be precisely inferred, since it may span multiple datastores",
                        componentJob);
            }
            for (ConfiguredPropertyDescriptor datastoreProperty : datastoreProperties) {
                final Object datastoreValue = componentJob.getConfiguration().getProperty(datastoreProperty);
                if (datastoreValue == null) {
                    logger.debug("No value for property {}, ignoring", datastoreProperty);
                } else if (datastoreValue.getClass().isArray()) {
                    final int length = Array.getLength(datastoreValue);
                    if (length > 1) {
                        // we do not know any cases where this will happen, but
                        // it might potentially happen in a third party
                        // extension.
                        logger.warn(
                                "Multiple datastores referenced by {}. Datastore usage will be built, but usage scenario cannot be precisely inferred, since it may span multiple datastores",
                                datastoreProperty);
                    }
                    for (int i = 0; i < length; i++) {
                        final Datastore datastore = (Datastore) Array.get(datastoreValue, i);
                        buildDatastoreUsageMap(datastoreUsage, componentJob, datastore);
                    }
                } else {
                    final Datastore datastore = (Datastore) datastoreValue;
                    buildDatastoreUsageMap(datastoreUsage, componentJob, datastore);
                }
            }
        }
    }

    /**
     * Builds the map of datastore usage for a single component job which
     * references a specific {@link Datastore}.
     * 
     * @param datastoreUsage
     * @param componentJob
     * @param datastore
     */
    private void buildDatastoreUsageMap(final Map<String, Set<Column>> datastoreUsage,
            final HasBeanConfiguration componentJob, final Datastore datastore) {
        final Set<ConfiguredPropertyDescriptor> schemaProperties = componentJob.getDescriptor()
                .getConfiguredPropertiesByAnnotation(SchemaProperty.class);
        if (schemaProperties.isEmpty()) {
            return;
        }

        final DatastoreConnection con = datastore.openConnection();
        try {
            final SchemaNavigator schemaNavigator = con.getSchemaNavigator();
            for (ConfiguredPropertyDescriptor schemaProperty : schemaProperties) {
                Collection<Schema> schemas = getSchemas(schemaNavigator, componentJob, schemaProperty);
                if (schemas.isEmpty()) {
                    logger.warn(
                            "No schemas specified for Datastore {} in {}. Registering an unspecified datastore usage scenario.",
                            datastore, componentJob);
                    datastoreUsage.put(datastore.getName(), new HashSet<Column>());
                } else if (schemas.size() > 1) {
                    // we do not know any cases where this will happen, but
                    // it might potentially happen in a third party
                    // extension.
                    logger.warn(
                            "Multiple schemas referenced by {}. Datastore usage will be built, but usage scenario cannot be precisely inferred, since it may span multiple schemas",
                            schemaProperty);
                }
                for (Schema schema : schemas) {
                    Collection<Table> tables = getTables(componentJob, schema);
                    if (tables.isEmpty()) {
                        logger.warn(
                                "No tables specified for Schema {} in {}. Registering an unspecified datastore usage scenario.",
                                schema, componentJob);
                        datastoreUsage.put(datastore.getName(), new HashSet<Column>());
                        return;
                    } else if (tables.size() > 1) {
                        // we do not know any cases where this will happen, but
                        // it might potentially happen in a third party
                        // extension.
                        logger.warn(
                                "Multiple tables referenced by {}. Datastore usage will be built, but usage scenario cannot be precisely inferred, since it may span multiple schemas",
                                componentJob);
                    }

                    for (final Table table : tables) {
                        Collection<Column> columns = getColumns(componentJob, table);
                        if (columns == null || columns.isEmpty()) {
                            logger.info(
                                    "Could not determine used columns for {}. Adding all columns to usage scenario.",
                                    table);
                            columns = Arrays.asList(table.getColumns());
                        }

                        // update the usage map
                        Set<Column> usage = datastoreUsage.get(datastore.getName());
                        if (usage == null) {
                            usage = new LinkedHashSet<Column>();
                            datastoreUsage.put(datastore.getName(), usage);
                        }
                        usage.addAll(columns);
                    }
                }
            }
        } finally {
            con.close();
        }
    }

    private Collection<Column> getColumns(HasBeanConfiguration componentJob, Table table) {
        final Collection<Column> columns = new LinkedHashSet<Column>();

        final Set<ConfiguredPropertyDescriptor> columnProperties = componentJob.getDescriptor()
                .getConfiguredPropertiesByAnnotation(ColumnProperty.class);
        for (ConfiguredPropertyDescriptor columnProperty : columnProperties) {
            final Object columnValue = componentJob.getConfiguration().getProperty(columnProperty);
            if (columnValue == null) {
                logger.debug("Column value for {} is null", columnProperty);
            } else {
                if (columnValue instanceof String) {
                    final String columnName = (String) columnValue;
                    final Column column = table.getColumnByName(columnName);
                    if (column != null) {
                        columns.add(column);
                    }
                } else if (columnValue instanceof String[]) {
                    final String[] columnNames = (String[]) columnValue;
                    for (String columnName : columnNames) {
                        final Column column = table.getColumnByName(columnName);
                        if (column != null) {
                            columns.add(column);
                        }
                    }
                } else if (columnValue instanceof Column) {
                    columns.add((Column) columnValue);
                } else if (columnValue instanceof Column[]) {
                    Column[] columnArray = (Column[]) columnValue;
                    for (Column column : columnArray) {
                        columns.add(column);
                    }
                } else {
                    throw new UnsupportedOperationException("Unsupported column property type: " + columnValue);
                }
            }
        }

        return columns;
    }

    private Collection<Table> getTables(HasBeanConfiguration componentJob, Schema schema) {
        final Collection<Table> tables = new LinkedHashSet<Table>();

        final Set<ConfiguredPropertyDescriptor> tableProperties = componentJob.getDescriptor()
                .getConfiguredPropertiesByAnnotation(TableProperty.class);
        for (ConfiguredPropertyDescriptor tableProperty : tableProperties) {
            final Object tableValue = componentJob.getConfiguration().getProperty(tableProperty);
            if (tableValue == null) {
                logger.debug("Table value for {} is null", tableProperty);
            } else {
                if (tableValue instanceof String) {
                    final String tableName = (String) tableValue;
                    final Table table = schema.getTableByName(tableName);
                    if (table != null) {
                        tables.add(table);
                    }
                } else if (tableValue instanceof String[]) {
                    final String[] tableNames = (String[]) tableValue;
                    for (String tableName : tableNames) {
                        final Table table = schema.getTableByName(tableName);
                        if (table != null) {
                            tables.add(table);
                        }
                    }
                } else if (tableValue instanceof Table) {
                    tables.add((Table) tableValue);
                } else if (tableValue instanceof Table[]) {
                    Table[] tableArray = (Table[]) tableValue;
                    for (Table table : tableArray) {
                        tables.add(table);
                    }
                } else {
                    throw new UnsupportedOperationException("Unsupported table property type: " + tableValue);
                }
            }
        }

        return tables;
    }

    private Collection<Schema> getSchemas(SchemaNavigator schemaNavigator, HasBeanConfiguration componentJob,
            ConfiguredPropertyDescriptor schemaProperty) {
        final Collection<Schema> schemas = new LinkedHashSet<Schema>();

        final Object schemaValue = componentJob.getConfiguration().getProperty(schemaProperty);
        if (schemaValue == null) {
            logger.debug("Schema value for {} is null. Using default schema.", schemaProperty);
            schemas.add(schemaNavigator.getDefaultSchema());
        } else if (schemaValue instanceof String) {
            Schema schema = schemaNavigator.convertToSchema((String) schemaValue);
            if (schema != null) {
                schemas.add(schema);
            }
        } else if (schemaValue instanceof String[]) {
            String[] schemaNames = (String[]) schemaValue;
            for (String schemaName : schemaNames) {
                Schema schema = schemaNavigator.convertToSchema(schemaName);
                if (schema != null) {
                    schemas.add(schema);
                }
            }
        } else if (schemaValue instanceof Schema) {
            schemas.add((Schema) schemaValue);
        } else if (schemaValue instanceof Schema[]) {
            Schema[] schemaArray = (Schema[]) schemaValue;
            for (Schema schema : schemaArray) {
                schemas.add(schema);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported schema property type: " + schemaValue);
        }

        return schemas;
    }

    private Package newPackage(String packageName, boolean recursive) {
        Package p = new Package();
        p.setValue(packageName);
        p.setRecursive(recursive);
        return p;
    }

    private Marshaller createMarshaller() {
        try {
            Marshaller marshaller = _jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setEventHandler(new JaxbValidationEventHandler());
            return marshaller;
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public DatatypeFactory getDatatypeFactory() {
        try {
            return DatatypeFactory.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public XMLGregorianCalendar createDate(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(calendar.getTime());
        cal.setTimeZone(calendar.getTimeZone());
        return getDatatypeFactory().newXMLGregorianCalendar(cal);
    }
}
