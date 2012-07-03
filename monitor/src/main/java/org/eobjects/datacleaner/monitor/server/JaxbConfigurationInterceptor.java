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
package org.eobjects.datacleaner.monitor.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

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
import org.eobjects.analyzer.util.JaxbValidationEventHandler;
import org.eobjects.datacleaner.monitor.configuration.ConfigurationFactory;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.data.Row;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.ColumnType;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.Ref;
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

    public void intercept(final String tenantId, final InputStream in, final OutputStream out) throws Exception {
        final TenantContext context = _contextFactory.getContext(tenantId);

        final Unmarshaller unmarshaller = _jaxbContext.createUnmarshaller();
        final Configuration configuration = (Configuration) unmarshaller.unmarshal(in);

        // replace datastore catalog
        if (_replaceDatastores) {
            final DatastoreCatalogType originalDatastoreCatalog = configuration.getDatastoreCatalog();
            final DatastoreCatalogType newDatastoreCatalog = interceptDatastoreCatalog(context,
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
     * 
     * @param originalDatastoreCatalog
     * @return
     */
    private DatastoreCatalogType interceptDatastoreCatalog(final TenantContext context,
            final DatastoreCatalogType originalDatastoreCatalog) {
        final AnalyzerBeansConfiguration configuration = context.getConfiguration();
        final DatastoreCatalogType newDatastoreCatalog = new DatastoreCatalogType();

        final DatastoreCatalog datastoreCatalog = configuration.getDatastoreCatalog();

        for (final String name : datastoreCatalog.getDatastoreNames()) {
            final Datastore datastore = datastoreCatalog.getDatastore(name);
            if (datastore != null) {
                final AbstractDatastoreType pojoDatastoreType = createPojoDatastore(datastore);
                newDatastoreCatalog.getJdbcDatastoreOrAccessDatastoreOrCsvDatastore().add(pojoDatastoreType);
            }
        }

        return newDatastoreCatalog;
    }

    private AbstractDatastoreType createPojoDatastore(Datastore datastore) {
        final PojoDatastoreType datastoreType = new PojoDatastoreType();
        datastoreType.setName(datastore.getName());
        datastoreType.setDescription(datastore.getDescription());

        final DatastoreConnection con = datastore.openConnection();
        try {
            final DataContext dataContext = con.getDataContext();
            final Schema schema = dataContext.getDefaultSchema();
            datastoreType.setSchemaName(schema.getName());

            final Table[] tables = schema.getTables();
            for (Table table : tables) {
                final PojoTableType tableType = createPojoTable(dataContext, table);
                datastoreType.getTable().add(tableType);
            }
        } finally {
            con.close();
        }

        return datastoreType;
    }

    private PojoTableType createPojoTable(DataContext dataContext, Table table) {
        final PojoTableType tableType = new PojoTableType();
        tableType.setName(table.getName());

        // read columns
        final Columns columnsType = new Columns();
        final Column[] columns = table.getColumns();
        for (Column column : columns) {
            columnsType.getColumn().add(createPojoColumn(column.getName(), column.getType()));
        }
        tableType.setColumns(columnsType);

        // read values
        final Query q = dataContext.query().from(table).select(columns).toQuery();
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
