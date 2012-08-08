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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
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
import org.eobjects.analyzer.configuration.jaxb.AbstractDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.ClasspathScannerType;
import org.eobjects.analyzer.configuration.jaxb.ClasspathScannerType.Package;
import org.eobjects.analyzer.configuration.jaxb.Configuration;
import org.eobjects.analyzer.configuration.jaxb.ConfigurationMetadataType;
import org.eobjects.analyzer.configuration.jaxb.DatastoreCatalogType;
import org.eobjects.analyzer.configuration.jaxb.MultithreadedTaskrunnerType;
import org.eobjects.analyzer.configuration.jaxb.ObjectFactory;
import org.eobjects.analyzer.configuration.jaxb.PojoDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.PojoTableType;
import org.eobjects.analyzer.configuration.jaxb.PojoTableType.Columns;
import org.eobjects.analyzer.configuration.jaxb.PojoTableType.Rows;
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
import org.eobjects.datacleaner.monitor.configuration.ConfigurationFactory;
import org.eobjects.datacleaner.monitor.configuration.JobContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.server.ConfigurationInterceptor;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.MetaModelHelper;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.data.Row;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.ColumnType;
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
    private final Ref<Date> _dateRef;
    private final TenantContextFactory _contextFactory;
    private final boolean _replaceDatastores;

    @Autowired
    public JaxbConfigurationInterceptor(TenantContextFactory contextFactory, ConfigurationFactory configurationFactory)
            throws JAXBException {
        this(contextFactory, configurationFactory, true);
    }

    public JaxbConfigurationInterceptor(TenantContextFactory contextFactory, ConfigurationFactory configurationFactory,
            boolean replaceDatastores) throws JAXBException {
        this(contextFactory, configurationFactory, replaceDatastores, new Ref<Date>() {
            @Override
            public Date get() {
                return new Date();
            }
        });
    }

    public JaxbConfigurationInterceptor(TenantContextFactory contextFactory, ConfigurationFactory configurationFactory,
            boolean replaceDatastores, Ref<Date> dateRef) throws JAXBException {
        _contextFactory = contextFactory;
        _configurationFactory = configurationFactory;
        _replaceDatastores = replaceDatastores;
        _jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(),
                ObjectFactory.class.getClassLoader());
        _dateRef = dateRef;
    }

    public void intercept(final String tenantId, final JobContext job, final InputStream in, final OutputStream out)
            throws Exception {
        final TenantContext context = _contextFactory.getContext(tenantId);

        final Unmarshaller unmarshaller = _jaxbContext.createUnmarshaller();
        final Configuration configuration = (Configuration) unmarshaller.unmarshal(in);

        // replace datastore catalog
        if (_replaceDatastores) {
            final DatastoreCatalogType originalDatastoreCatalog = configuration.getDatastoreCatalog();
            final DatastoreCatalogType newDatastoreCatalog = interceptDatastoreCatalog(context, job,
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
        configurationMetadata.setConfigurationName("DataCleaner dq monitor configuration for tenant " + tenantId);
        configuration.setConfigurationMetadata(configurationMetadata);
        configurationMetadata.setUpdatedDate(createDate(_dateRef.get()));
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
     * 
     * @param originalDatastoreCatalog
     * @return
     */
    private DatastoreCatalogType interceptDatastoreCatalog(final TenantContext context, final JobContext job,
            final DatastoreCatalogType originalDatastoreCatalog) {
        final AnalyzerBeansConfiguration configuration = context.getConfiguration();
        final DatastoreCatalogType newDatastoreCatalog = new DatastoreCatalogType();

        final DatastoreCatalog datastoreCatalog = configuration.getDatastoreCatalog();

        // Create a map of all used datastores and the columns which are
        // being accessed within them.
        Map<String, Set<Column>> datastoreUsage = new HashMap<String, Set<Column>>();

        if (job == null) {
            // represent all datastores (no job-specific information is known)
            for (final String name : datastoreCatalog.getDatastoreNames()) {
                datastoreUsage.put(name, new HashSet<Column>());
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
                final AbstractDatastoreType pojoDatastoreType = createPojoDatastore(datastore, columns);
                newDatastoreCatalog.getJdbcDatastoreOrAccessDatastoreOrCsvDatastore().add(pojoDatastoreType);
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

    private AbstractDatastoreType createPojoDatastore(Datastore datastore, Set<Column> columns) {
        final PojoDatastoreType datastoreType = new PojoDatastoreType();
        datastoreType.setName(datastore.getName());
        datastoreType.setDescription(datastore.getDescription());

        final DatastoreConnection con = datastore.openConnection();
        try {
            final DataContext dataContext = con.getDataContext();

            final Schema schema;
            final Table[] tables;
            if (columns == null || columns.isEmpty()) {
                schema = dataContext.getDefaultSchema();
                tables = schema.getTables();
            } else {
                tables = MetaModelHelper.getTables(columns);
                // TODO: There's a possibility that tables span multiple
                // schemas, but we cannot currently support that in a
                // PojoDatastore, so we just pick the first and cross our
                // fingers.
                schema = tables[0].getSchema();
            }

            datastoreType.setSchemaName(schema.getName());

            for (final Table table : tables) {
                final Column[] usedColumns;
                if (columns == null || columns.isEmpty()) {
                    usedColumns = table.getColumns();
                } else {
                    usedColumns = getTableColumns(table, columns);
                }

                final PojoTableType tableType = createPojoTable(dataContext, table, usedColumns);
                datastoreType.getTable().add(tableType);
            }
        } finally {
            con.close();
        }

        return datastoreType;
    }

    /**
     * TODO: This method resembles
     * {@link MetaModelHelper#getTableColumns(Table, Iterable)}, but due to a
     * small equality bug, this will not work. Should work when MetaModel 3.0.2
     * (or later) is out.
     * 
     * @param table
     * @param columns
     * @return
     */
    private Column[] getTableColumns(final Table table, final Set<Column> columns) {
        final List<Column> result = new ArrayList<Column>();
        for (final Column column : columns) {
            if (table.equals(column.getTable())) {
                result.add(column);
            }
        }
        return result.toArray(new Column[result.size()]);
    }

    private PojoTableType createPojoTable(final DataContext dataContext, final Table table, final Column[] usedColumns) {
        final PojoTableType tableType = new PojoTableType();
        tableType.setName(table.getName());

        // read columns
        final Columns columnsType = new Columns();
        for (Column column : usedColumns) {
            columnsType.getColumn().add(createPojoColumn(column.getName(), column.getType()));
        }
        tableType.setColumns(columnsType);

        // read values
        final Query q = dataContext.query().from(table).select(usedColumns).toQuery();
        q.setMaxRows(MAX_POJO_ROWS);

        final Rows rowsType = new Rows();
        final DataSet ds = dataContext.executeQuery(q);
        try {
            while (ds.next()) {
                Row row = ds.getRow();
                rowsType.getRow().add(createPojoRow(row));
            }
        } finally {
            ds.close();
        }

        tableType.setRows(rowsType);

        return tableType;
    }

    private org.eobjects.analyzer.configuration.jaxb.PojoTableType.Rows.Row createPojoRow(Row row) {
        org.eobjects.analyzer.configuration.jaxb.PojoTableType.Rows.Row rowType = new org.eobjects.analyzer.configuration.jaxb.PojoTableType.Rows.Row();
        Object[] values = row.getValues();
        for (Object value : values) {
            if (value == null) {
                rowType.getV().add(null);
            } else {
                rowType.getV().add(value.toString());
            }
        }
        return rowType;
    }

    private org.eobjects.analyzer.configuration.jaxb.PojoTableType.Columns.Column createPojoColumn(String name,
            ColumnType type) {
        org.eobjects.analyzer.configuration.jaxb.PojoTableType.Columns.Column columnType = new org.eobjects.analyzer.configuration.jaxb.PojoTableType.Columns.Column();
        columnType.setName(name);
        columnType.setType(type.toString());
        return columnType;
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

    public XMLGregorianCalendar createDate(Date date) {
        if (date == null) {
            return null;
        }
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        return getDatatypeFactory().newXMLGregorianCalendar(cal);
    }
}
