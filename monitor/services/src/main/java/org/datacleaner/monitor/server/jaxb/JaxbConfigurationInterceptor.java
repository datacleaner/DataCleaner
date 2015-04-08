/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.monitor.server.jaxb;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
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

import org.apache.metamodel.DataContext;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.MutableSchema;
import org.apache.metamodel.schema.MutableTable;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.Ref;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.JaxbPojoDatastoreAdaptor;
import org.datacleaner.configuration.jaxb.AbstractDatastoreType;
import org.datacleaner.configuration.jaxb.ClasspathScannerType;
import org.datacleaner.configuration.jaxb.ClasspathScannerType.Package;
import org.datacleaner.configuration.jaxb.Configuration;
import org.datacleaner.configuration.jaxb.ConfigurationMetadataType;
import org.datacleaner.configuration.jaxb.DatastoreCatalogType;
import org.datacleaner.configuration.jaxb.MultithreadedTaskrunnerType;
import org.datacleaner.configuration.jaxb.ObjectFactory;
import org.datacleaner.configuration.jaxb.PojoTableType;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.monitor.configuration.ConfigurationFactory;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.server.ConfigurationInterceptor;
import org.datacleaner.monitor.server.job.DataCleanerJobContext;
import org.datacleaner.util.JaxbValidationEventHandler;
import org.datacleaner.util.StringUtils;
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

    private static final String REMARK_INCLUDE_IN_QUERY = "INCLUDE_IN_QUERY";

    private static final Logger logger = LoggerFactory.getLogger(JaxbConfigurationInterceptor.class);

    private static final int MAX_POJO_ROWS = 20;

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
    public void intercept(final String tenantId, final DataCleanerJobContext job, final String datastoreName,
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
    private DatastoreCatalogType interceptDatastoreCatalog(final TenantContext context,
            final DataCleanerJobContext job, String datastoreName, final DatastoreCatalogType originalDatastoreCatalog) {
        final DataCleanerConfiguration configuration = context.getConfiguration();

        final DatastoreCatalog datastoreCatalog = configuration.getDatastoreCatalog();

        // Create a map of all used datastores and the columns which are
        // being accessed within them.
        final Map<String, MutableSchema> datastoreUsage = new LinkedHashMap<String, MutableSchema>();

        if (job != null && StringUtils.isNullOrEmpty(datastoreName)) {
            datastoreName = job.getSourceDatastoreName();
        }

        if (StringUtils.isNullOrEmpty(datastoreName)) {
            // represent all datastores (no job-specific information is
            // known)
            for (final String name : datastoreCatalog.getDatastoreNames()) {
                datastoreUsage.put(name, new MutableSchema());
            }
        } else {
            // represent the single datastore fully

            final Datastore datastore = datastoreCatalog.getDatastore(datastoreName);
            if (datastore == null) {
                throw new IllegalArgumentException("Datastore '" + datastoreName + "' does not exist");
            }
            try (final DatastoreConnection con = datastore.openConnection()) {
                final MutableSchema usageSchema = new MutableSchema();
                final Schema schema;
                if (job == null) {
                    schema = con.getDataContext().getDefaultSchema();
                } else {
                    String columnPath = job.getSourceColumnPaths().get(0);
                    Column column = con.getDataContext().getColumnByQualifiedLabel(columnPath);
                    schema = column.getTable().getSchema();
                }
                usageSchema.setName(schema.getName());
                String[] tableNames = schema.getTableNames();
                for (String tableName : tableNames) {
                    usageSchema.addTable(new MutableTable(tableName).setSchema(usageSchema).setRemarks(
                            REMARK_INCLUDE_IN_QUERY));
                }
                datastoreUsage.put(datastoreName, usageSchema);
            }

            // add schema information about the remaining datastores
            for (final String name : datastoreCatalog.getDatastoreNames()) {
                if (!datastoreName.equals(name)) {
                    datastoreUsage.put(name, new MutableSchema());
                }
            }
        }

        return interceptDatastoreCatalog(datastoreCatalog, datastoreUsage);
    }

    public DatastoreCatalogType interceptDatastoreCatalog(final DatastoreCatalog datastoreCatalog,
            final Map<String, MutableSchema> datastoreUsage) {
        final DatastoreCatalogType newDatastoreCatalog = new DatastoreCatalogType();
        final Set<Entry<String, MutableSchema>> datastoreUsageEntries = datastoreUsage.entrySet();
        for (final Entry<String, MutableSchema> entry : datastoreUsageEntries) {
            final String name = entry.getKey();
            Schema schema = entry.getValue();

            final Datastore datastore = datastoreCatalog.getDatastore(name);
            if (datastore != null) {
                // a comparator that takes the column number into account.
                final Comparator<Column> columnComparator = new Comparator<Column>() {
                    @Override
                    public int compare(Column o1, Column o2) {
                        if (o1.getTable() == o2.getTable()) {
                            int diff = o1.getColumnNumber() - o2.getColumnNumber();
                            if (diff != 0) {
                                return diff;
                            }
                        }
                        return o1.compareTo(o2);
                    }
                };

                try (final DatastoreConnection connection = datastore.openConnection()) {
                    final DataContext dataContext = connection.getDataContext();
                    final JaxbPojoDatastoreAdaptor adaptor = new JaxbPojoDatastoreAdaptor();
                    final Collection<PojoTableType> pojoTables = new ArrayList<PojoTableType>();

                    Table[] usageTables = schema.getTables();
                    if (usageTables == null || usageTables.length == 0) {
                        // an unspecified schema entry will be interpreted as an
                        // open-ended inclusion of schema information only
                        schema = dataContext.getDefaultSchema();
                        usageTables = dataContext.getDefaultSchema().getTables();
                    }

                    final String schemaName = schema.getName();
                    for (final Table usageTable : usageTables) {
                        Column[] usageColumns = usageTable.getColumns();
                        if (usageColumns == null || usageColumns.length == 0) {
                            // an unspecified table entry will be interpreted by
                            // including all columns of that table
                            final String tableName = usageTable.getName();
                            final Schema schemaByName = dataContext.getSchemaByName(schemaName);
                            if (schemaByName == null) {
                                logger.error("Could not find schema by name: {}, skipping table: {}", schemaName,
                                        usageTable);
                                usageColumns = new Column[0];
                            } else {
                                final Table table = schemaByName.getTableByName(tableName);
                                usageColumns = table.getColumns();
                            }
                        }

                        if (usageColumns != null && usageColumns.length > 0) {
                            Arrays.sort(usageColumns, columnComparator);

                            final int maxRows = REMARK_INCLUDE_IN_QUERY.equals(usageTable.getRemarks()) ? MAX_POJO_ROWS
                                    : 0;

                            final Table sourceTable = usageColumns[0].getTable();
                            try {
                                final PojoTableType pojoTable = adaptor.createPojoTable(dataContext, sourceTable,
                                        usageColumns, maxRows);
                                pojoTables.add(pojoTable);
                            } catch (Exception e) {
                                // allow omitting errornous tables here.
                                logger.error("Failed to serialize table '" + sourceTable + "' of datastore '" + name
                                        + "' to POJO format: " + e.getMessage(), e);
                            }
                        }
                    }

                    final AbstractDatastoreType pojoDatastoreType = adaptor.createPojoDatastore(datastore.getName(),
                            schemaName, pojoTables);
                    pojoDatastoreType.setDescription(datastore.getDescription());

                    newDatastoreCatalog.getJdbcDatastoreOrAccessDatastoreOrCsvDatastore().add(pojoDatastoreType);
                } catch (Exception e) {
                    // allow omitting errornous datastores here.
                    logger.error("Failed to serialize datastore '" + name + "' to POJO format: " + e.getMessage(), e);
                }
            }
        }

        return newDatastoreCatalog;
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
