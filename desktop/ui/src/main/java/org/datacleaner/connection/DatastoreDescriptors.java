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
package org.datacleaner.connection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.database.DatabaseDriverDescriptor;
import org.datacleaner.util.IconUtils;
import org.datacleaner.windows.AbstractDatastoreDialog;
import org.datacleaner.windows.AccessDatastoreDialog;
import org.datacleaner.windows.CassandraDatastoreDialog;
import org.datacleaner.windows.CompositeDatastoreDialog;
import org.datacleaner.windows.CouchDbDatastoreDialog;
import org.datacleaner.windows.CsvDatastoreDialog;
import org.datacleaner.windows.DbaseDatastoreDialog;
import org.datacleaner.windows.ElasticSearchDatastoreDialog;
import org.datacleaner.windows.ExcelDatastoreDialog;
import org.datacleaner.windows.FixedWidthDatastoreDialog;
import org.datacleaner.windows.HBaseDatastoreDialog;
import org.datacleaner.windows.JdbcDatastoreDialog;
import org.datacleaner.windows.JsonDatastoreDialog;
import org.datacleaner.windows.MongoDbDatastoreDialog;
import org.datacleaner.windows.SalesforceDatastoreDialog;
import org.datacleaner.windows.SasDatastoreDialog;
import org.datacleaner.windows.SugarCrmDatastoreDialog;
import org.datacleaner.windows.XmlDatastoreDialog;

public class DatastoreDescriptors {

    private static final DatastoreDescriptor CSV_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl("CSV file",
            "Comma-separated values (CSV) file (or file with other separators)", CsvDatastore.class);

    private static final DatastoreDescriptor EXCEL_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl(
            "Excel spreadsheet", "Microsoft Excel spreadsheet. Either .xls (97-2003) or .xlsx (2007+) format.",
            ExcelDatastore.class);

    private static final DatastoreDescriptor ACCESS_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl(
            "Access database", "Microsoft Access database file (.mdb).", AccessDatastore.class);

    private static final DatastoreDescriptor SAS_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl("SAS library",
            "A directory of SAS library files (.sas7bdat).", SasDatastore.class);

    private static final DatastoreDescriptor DBASE_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl("DBase database",
            "DBase database file (.dbf)", DbaseDatastore.class);

    private static final DatastoreDescriptor FIXEDWIDTH_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl(
            "Fixed width file",
            "Text file with fixed width values. Each value spans a fixed amount of text characters.",
            FixedWidthDatastore.class);

    private static final DatastoreDescriptor XML_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl("XML file",
            "Extensible Markup Language file (.xml)", XmlDatastore.class);

    private static final DatastoreDescriptor JSON_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl("JSON file",
            "JavaScript Object NOtation file (.json).", JsonDatastore.class);

    private static final DatastoreDescriptor SALESFORCE_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl(
            "Salesforce.com", "Connect to a Salesforce.com account", SalesforceDatastore.class);

    private static final DatastoreDescriptor SUGARCRM_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl("SugarCRM",
            "Connect to a SugarCRM system", SugarCrmDatastore.class);

    private static final DatastoreDescriptor MONGODB_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl(
            "MongoDB database", "Connect to a MongoDB database", MongoDbDatastore.class);

    private static final DatastoreDescriptor COUCHDB_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl(
            "CouchDB database", "Connect to an Apache CouchDB database", CouchDbDatastore.class);

    private static final DatastoreDescriptor ELASTICSEARCH_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl(
            "ElasticSearch index", "Connect to an ElasticSearch index", ElasticSearchDatastore.class);

    private static final DatastoreDescriptor CASSANDRA_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl(
            "Cassandra database", "Connect to an Apache Cassandra database", CassandraDatastore.class);

    private static final DatastoreDescriptor HBASE_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl("HBase database",
            "Connect to an Apache HBase database", HBaseDatastore.class);

    private static final DatastoreDescriptor HIVE_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl(
            DatabaseDriverCatalog.DATABASE_NAME_HIVE, "Connect to an Apache Hive database", JdbcDatastore.class);

    private static final DatastoreDescriptor MYSQL_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl(DatabaseDriverCatalog.DATABASE_NAME_MYSQL,
            "Connect to a MySQL database", JdbcDatastore.class);

    private static final DatastoreDescriptor POSTGRESQL_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl(DatabaseDriverCatalog.DATABASE_NAME_POSTGRESQL,
            "Connect to a PostgreSQL database", JdbcDatastore.class);

    private static final DatastoreDescriptor ORACLE_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl(DatabaseDriverCatalog.DATABASE_NAME_ORACLE,
            "Connect to a Oracle database", JdbcDatastore.class);

    private static final DatastoreDescriptor SQLSERVER_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl(DatabaseDriverCatalog.DATABASE_NAME_MICROSOFT_SQL_SERVER_JTDS,
            "Connect to a Microsoft SQL Server database", JdbcDatastore.class);
    
    private static final DatastoreDescriptor COMPOSITE_DATASTORE_DESCRIPTOR = new DatastoreDescriptorImpl("Composite datastore",
            "Create a composite datastore", CompositeDatastore.class);
    
    private static List<DatastoreDescriptor> _allDatastoreDescriptors = new ArrayList<>();

    private static Map<DatastoreDescriptor, String> _iconPaths = new HashMap<DatastoreDescriptor, String>();

    private static Map<DatastoreDescriptor, Class<? extends AbstractDatastoreDialog<? extends Datastore>>> _dialogClasses = new HashMap<>();

    static {
        _allDatastoreDescriptors.add(CSV_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(ACCESS_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(EXCEL_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(SAS_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(DBASE_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(FIXEDWIDTH_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(XML_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(JSON_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(SALESFORCE_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(SUGARCRM_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(MONGODB_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(COUCHDB_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(ELASTICSEARCH_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(CASSANDRA_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(HBASE_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(HIVE_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(MYSQL_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(POSTGRESQL_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(ORACLE_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(SQLSERVER_DATASTORE_DESCRIPTOR);
        _allDatastoreDescriptors.add(COMPOSITE_DATASTORE_DESCRIPTOR);
    }
    
    static {
        _iconPaths.put(CSV_DATASTORE_DESCRIPTOR, IconUtils.CSV_IMAGEPATH);
        _iconPaths.put(ACCESS_DATASTORE_DESCRIPTOR, IconUtils.ACCESS_IMAGEPATH);
        _iconPaths.put(EXCEL_DATASTORE_DESCRIPTOR, IconUtils.EXCEL_IMAGEPATH);
        _iconPaths.put(SAS_DATASTORE_DESCRIPTOR, IconUtils.SAS_IMAGEPATH);
        _iconPaths.put(DBASE_DATASTORE_DESCRIPTOR, IconUtils.DBASE_IMAGEPATH);
        _iconPaths.put(FIXEDWIDTH_DATASTORE_DESCRIPTOR, IconUtils.FIXEDWIDTH_IMAGEPATH);
        _iconPaths.put(XML_DATASTORE_DESCRIPTOR, IconUtils.XML_IMAGEPATH);
        _iconPaths.put(JSON_DATASTORE_DESCRIPTOR, IconUtils.JSON_IMAGEPATH);
        _iconPaths.put(SALESFORCE_DATASTORE_DESCRIPTOR, IconUtils.SALESFORCE_IMAGEPATH);
        _iconPaths.put(SUGARCRM_DATASTORE_DESCRIPTOR, IconUtils.SUGAR_CRM_IMAGEPATH);
        _iconPaths.put(MONGODB_DATASTORE_DESCRIPTOR, IconUtils.MONGODB_IMAGEPATH);
        _iconPaths.put(COUCHDB_DATASTORE_DESCRIPTOR, IconUtils.COUCHDB_IMAGEPATH);
        _iconPaths.put(ELASTICSEARCH_DATASTORE_DESCRIPTOR, IconUtils.ELASTICSEARCH_IMAGEPATH);
        _iconPaths.put(CASSANDRA_DATASTORE_DESCRIPTOR, IconUtils.CASSANDRA_IMAGEPATH);
        _iconPaths.put(HBASE_DATASTORE_DESCRIPTOR, IconUtils.HBASE_IMAGEPATH);
        _iconPaths.put(HIVE_DATASTORE_DESCRIPTOR, "images/datastore-types/databases/hive.png");
        _iconPaths.put(MYSQL_DATASTORE_DESCRIPTOR, "images/datastore-types/databases/mysql.png");
        _iconPaths.put(POSTGRESQL_DATASTORE_DESCRIPTOR, "images/datastore-types/databases/postgresql.png");
        _iconPaths.put(ORACLE_DATASTORE_DESCRIPTOR, "images/datastore-types/databases/oracle.png");
        _iconPaths.put(SQLSERVER_DATASTORE_DESCRIPTOR, "images/datastore-types/databases/microsoft.png");
        _iconPaths.put(COMPOSITE_DATASTORE_DESCRIPTOR, IconUtils.COMPOSITE_IMAGEPATH);
    }

    static {
        _dialogClasses.put(CSV_DATASTORE_DESCRIPTOR, CsvDatastoreDialog.class);
        _dialogClasses.put(ACCESS_DATASTORE_DESCRIPTOR, AccessDatastoreDialog.class);
        _dialogClasses.put(EXCEL_DATASTORE_DESCRIPTOR, ExcelDatastoreDialog.class);
        _dialogClasses.put(SAS_DATASTORE_DESCRIPTOR, SasDatastoreDialog.class);
        _dialogClasses.put(DBASE_DATASTORE_DESCRIPTOR, DbaseDatastoreDialog.class);
        _dialogClasses.put(FIXEDWIDTH_DATASTORE_DESCRIPTOR, FixedWidthDatastoreDialog.class);
        _dialogClasses.put(XML_DATASTORE_DESCRIPTOR, XmlDatastoreDialog.class);
        _dialogClasses.put(JSON_DATASTORE_DESCRIPTOR, JsonDatastoreDialog.class);
        _dialogClasses.put(SALESFORCE_DATASTORE_DESCRIPTOR, SalesforceDatastoreDialog.class);
        _dialogClasses.put(SUGARCRM_DATASTORE_DESCRIPTOR, SugarCrmDatastoreDialog.class);
        _dialogClasses.put(MONGODB_DATASTORE_DESCRIPTOR, MongoDbDatastoreDialog.class);
        _dialogClasses.put(COUCHDB_DATASTORE_DESCRIPTOR, CouchDbDatastoreDialog.class);
        _dialogClasses.put(ELASTICSEARCH_DATASTORE_DESCRIPTOR, ElasticSearchDatastoreDialog.class);
        _dialogClasses.put(CASSANDRA_DATASTORE_DESCRIPTOR, CassandraDatastoreDialog.class);
        _dialogClasses.put(HBASE_DATASTORE_DESCRIPTOR, HBaseDatastoreDialog.class);
        _dialogClasses.put(HIVE_DATASTORE_DESCRIPTOR, JdbcDatastoreDialog.class);
        _dialogClasses.put(MYSQL_DATASTORE_DESCRIPTOR, JdbcDatastoreDialog.class);
        _dialogClasses.put(POSTGRESQL_DATASTORE_DESCRIPTOR, JdbcDatastoreDialog.class);
        _dialogClasses.put(ORACLE_DATASTORE_DESCRIPTOR, JdbcDatastoreDialog.class);
        _dialogClasses.put(SQLSERVER_DATASTORE_DESCRIPTOR, JdbcDatastoreDialog.class);
        _dialogClasses.put(COMPOSITE_DATASTORE_DESCRIPTOR, CompositeDatastoreDialog.class);
    }
    
    private DatabaseDriverCatalog _databaseDriverCatalog;
    
    public DatastoreDescriptors(DatabaseDriverCatalog databaseDriverCatalog) {
        _databaseDriverCatalog = databaseDriverCatalog;
    }
    
    public static List<DatastoreDescriptor> getAllDatabaseDescriptors() {
        return _allDatastoreDescriptors;
    }
    
    /**
     * Returns the descriptors of datastore types available in DataCleaner.
     */
    public List<DatastoreDescriptor> getAvailableDatastoreDescriptors() {
        List<DatastoreDescriptor> availableDatabaseDescriptors = new ArrayList<>();
        
        List<DatastoreDescriptor> manualDatastoreDescriptors = getManualDatastoreDescriptors();
        availableDatabaseDescriptors.addAll(manualDatastoreDescriptors);
        
        List<DatastoreDescriptor> driverBasedDatastoreDescriptors = getDriverBasedDatastoreDescriptors();
        availableDatabaseDescriptors.addAll(driverBasedDatastoreDescriptors);
        
        Set<String> alreadyAddedDatabaseNames = new HashSet<>();
        for (DatastoreDescriptor datastoreDescriptor : driverBasedDatastoreDescriptors) {
            alreadyAddedDatabaseNames.add(datastoreDescriptor.getName());
        }
        
        List<DatastoreDescriptor> otherDatastoreDescriptors = getOtherDatastoreDescriptors(alreadyAddedDatabaseNames);
        availableDatabaseDescriptors.addAll(otherDatastoreDescriptors);
        
        return availableDatabaseDescriptors;
    }
    
    private List<DatastoreDescriptor> getManualDatastoreDescriptors() {
        List<DatastoreDescriptor> datastoreDescriptors = new ArrayList<>();

        DatastoreDescriptor csvDatastoreDescriptor = new DatastoreDescriptorImpl("CSV file",
                "Comma-separated values (CSV) file (or file with other separators)", CsvDatastore.class);
        datastoreDescriptors.add(csvDatastoreDescriptor);

        DatastoreDescriptor excelDatastoreDescriptor = new DatastoreDescriptorImpl("Excel spreadsheet",
                "Microsoft Excel spreadsheet. Either .xls (97-2003) or .xlsx (2007+) format.", ExcelDatastore.class);
        datastoreDescriptors.add(excelDatastoreDescriptor);

        DatastoreDescriptor accessDatastoreDescriptor = new DatastoreDescriptorImpl("Access database",
                "Microsoft Access database file (.mdb).", AccessDatastore.class);
        datastoreDescriptors.add(accessDatastoreDescriptor);

        DatastoreDescriptor sasDatastoreDescriptor = new DatastoreDescriptorImpl("SAS library",
                "A directory of SAS library files (.sas7bdat).", SasDatastore.class);
        datastoreDescriptors.add(sasDatastoreDescriptor);

        DatastoreDescriptor dbaseDatastoreDescriptor = new DatastoreDescriptorImpl("DBase database",
                "DBase database file (.dbf)", DbaseDatastore.class);
        datastoreDescriptors.add(dbaseDatastoreDescriptor);

        DatastoreDescriptor fixedWidthDatastoreDescriptor = new DatastoreDescriptorImpl("Fixed width file",
                "Text file with fixed width values. Each value spans a fixed amount of text characters.",
                FixedWidthDatastore.class);
        datastoreDescriptors.add(fixedWidthDatastoreDescriptor);

        DatastoreDescriptor xmlDatastoreDescriptor = new DatastoreDescriptorImpl("XML file",
                "Extensible Markup Language file (.xml)", XmlDatastore.class);
        datastoreDescriptors.add(xmlDatastoreDescriptor);

        DatastoreDescriptor jsonDatastoreDescriptor = new DatastoreDescriptorImpl("JSON file",
                "JavaScript Object NOtation file (.json).", JsonDatastore.class);
        datastoreDescriptors.add(jsonDatastoreDescriptor);
        
        DatastoreDescriptor salesforceDatastoreDescriptor = new DatastoreDescriptorImpl("Salesforce.com",
                "Connect to a Salesforce.com account", SalesforceDatastore.class, Arrays.asList("Cloud service"));
        datastoreDescriptors.add(salesforceDatastoreDescriptor);
        
        DatastoreDescriptor sugarCrmDatastoreDescriptor = new DatastoreDescriptorImpl("SugarCRM",
                "Connect to a SugarCRM system", SugarCrmDatastore.class, Arrays.asList("Cloud service"));
        datastoreDescriptors.add(sugarCrmDatastoreDescriptor);

        DatastoreDescriptor mongoDbDatastoreDescriptor = new DatastoreDescriptorImpl("MongoDB database",
                "Connect to a MongoDB database", MongoDbDatastore.class, Arrays.asList("Database"));
        datastoreDescriptors.add(mongoDbDatastoreDescriptor);
        
        DatastoreDescriptor couchDbDatastoreDescriptor = new DatastoreDescriptorImpl("CouchDB database",
                "Connect to an Apache CouchDB database", CouchDbDatastore.class, Arrays.asList("Database"));
        datastoreDescriptors.add(couchDbDatastoreDescriptor);
        
        DatastoreDescriptor elasticSearchDatastoreDescriptor = new DatastoreDescriptorImpl("ElasticSearch index",
                "Connect to an ElasticSearch index", ElasticSearchDatastore.class, Arrays.asList("Database"));
        datastoreDescriptors.add(elasticSearchDatastoreDescriptor);
        
        DatastoreDescriptor cassandraDatastoreDescriptor = new DatastoreDescriptorImpl("Cassandra database",
                "Connect to an Apache Cassandra database", CassandraDatastore.class, Arrays.asList("Database"));
        datastoreDescriptors.add(cassandraDatastoreDescriptor);
        
        DatastoreDescriptor hbaseDatastoreDescriptor = new DatastoreDescriptorImpl("HBase database",
                "Connect to an Apache HBase database", HBaseDatastore.class, Arrays.asList("Database"));
        datastoreDescriptors.add(hbaseDatastoreDescriptor);
        
        return datastoreDescriptors;
    }
    
    private List<DatastoreDescriptor> getDriverBasedDatastoreDescriptors() {
        List<DatastoreDescriptor> datastoreDescriptors = new ArrayList<>();
        
        if (_databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_HIVE)) {
            DatastoreDescriptor hiveDatastoreDescriptor = new DatastoreDescriptorImpl(DatabaseDriverCatalog.DATABASE_NAME_HIVE,
                    "Connect to an Apache Hive database", JdbcDatastore.class, Arrays.asList("Database"));
            datastoreDescriptors.add(hiveDatastoreDescriptor);
        }
        
        if (_databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_MYSQL)) {
            DatastoreDescriptor mysqlDatastoreDescriptor = new DatastoreDescriptorImpl(DatabaseDriverCatalog.DATABASE_NAME_MYSQL,
                    "Connect to a MySQL database", JdbcDatastore.class, Arrays.asList("Database"));
            datastoreDescriptors.add(mysqlDatastoreDescriptor);
        }
        
        if (_databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_POSTGRESQL)) {
            DatastoreDescriptor postgresqlDatastoreDescriptor = new DatastoreDescriptorImpl(DatabaseDriverCatalog.DATABASE_NAME_POSTGRESQL,
                    "Connect to a PostgreSQL database", JdbcDatastore.class, Arrays.asList("Database"));
            datastoreDescriptors.add(postgresqlDatastoreDescriptor);
        }

        if (_databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_ORACLE)) {
            DatastoreDescriptor oracleDatastoreDescriptor = new DatastoreDescriptorImpl(DatabaseDriverCatalog.DATABASE_NAME_ORACLE,
                    "Connect to a Oracle database", JdbcDatastore.class, Arrays.asList("Database"));
            datastoreDescriptors.add(oracleDatastoreDescriptor);
        }

        if (_databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_MICROSOFT_SQL_SERVER_JTDS)) {
            DatastoreDescriptor sqlServerDatastoreDescriptor = new DatastoreDescriptorImpl(DatabaseDriverCatalog.DATABASE_NAME_MICROSOFT_SQL_SERVER_JTDS,
                    "Connect to a Microsoft SQL Server database", JdbcDatastore.class, Arrays.asList("Database"));
            datastoreDescriptors.add(sqlServerDatastoreDescriptor);
        }

        return datastoreDescriptors;
    }
    
    private List<DatastoreDescriptor> getOtherDatastoreDescriptors(Set<String> alreadyAddedDatabaseNames) {
        List<DatastoreDescriptor> datastoreDescriptors = new ArrayList<>();
        
        final List<DatabaseDriverDescriptor> databaseDrivers = _databaseDriverCatalog.getInstalledWorkingDatabaseDrivers();
        for (DatabaseDriverDescriptor databaseDriver : databaseDrivers) {
            final String databaseName = databaseDriver.getDisplayName();
            if (!alreadyAddedDatabaseNames.contains(databaseName)) {
                DatastoreDescriptor jdbcDatastoreDescriptor = new DatastoreDescriptorImpl(databaseName,
                        "Connect to " + databaseName, JdbcDatastore.class);
                datastoreDescriptors.add(jdbcDatastoreDescriptor);
            }
        }

        // custom/other jdbc connection
        final String databaseName = "Other database";
        if (!alreadyAddedDatabaseNames.contains(databaseName)) {
            DatastoreDescriptor otherDatastoreDescriptor = new DatastoreDescriptorImpl(databaseName,
                    "Connect to other database", JdbcDatastore.class);
            datastoreDescriptors.add(otherDatastoreDescriptor);
        }

        // composite datastore
        final String compositeDatastoreName = "Composite datastore";
        if (!alreadyAddedDatabaseNames.contains(compositeDatastoreName)) {
            DatastoreDescriptor compositeDatastoreDescriptor = new DatastoreDescriptorImpl(compositeDatastoreName,
                    "Create composite datastore", CompositeDatastore.class);
            datastoreDescriptors.add(compositeDatastoreDescriptor);
        }

        
        return datastoreDescriptors;
    }

    public static String getIconPath(DatastoreDescriptor datastoreDescriptor) {
        String iconPath = _iconPaths.get(datastoreDescriptor);
        if (iconPath == null) {
            if (datastoreDescriptor.getDatastoreClass().equals(JdbcDatastore.class)) {
                return IconUtils.GENERIC_DATASTORE_IMAGEPATH;
            }
        }
        return iconPath;
    }

    public static Class<? extends AbstractDatastoreDialog<? extends Datastore>> getDialogClass(DatastoreDescriptor datastoreDescriptor) {
        Class<? extends AbstractDatastoreDialog<? extends Datastore>> dialogClass = _dialogClasses.get(datastoreDescriptor);
        if (dialogClass == null) {
            if (datastoreDescriptor.getDatastoreClass().equals(JdbcDatastore.class)) {
                return JdbcDatastoreDialog.class;
            }
        }
        return dialogClass;
    }

}
