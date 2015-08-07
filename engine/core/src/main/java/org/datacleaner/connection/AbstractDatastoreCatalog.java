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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.database.DatabaseDriverDescriptor;
import org.datacleaner.user.UserPreferences;

@SuppressWarnings("serial")
public abstract class AbstractDatastoreCatalog implements DatastoreCatalog {

    private DatabaseDriverCatalog _databaseDriverCatalog;
    
    public AbstractDatastoreCatalog() {
        this(null);
    }
    
    public AbstractDatastoreCatalog(UserPreferences userPreferences) {
        _databaseDriverCatalog = new DatabaseDriverCatalog(userPreferences);
    }
    
    @Override
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
        
        DatabaseDriverCatalog databaseDriverCatalog = new DatabaseDriverCatalog(null);
        
        if (databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_HIVE)) {
            DatastoreDescriptor hiveDatastoreDescriptor = new DatastoreDescriptorImpl(DatabaseDriverCatalog.DATABASE_NAME_HIVE,
                    "Connect to an Apache Hive database", JdbcDatastore.class, Arrays.asList("Database"));
            datastoreDescriptors.add(hiveDatastoreDescriptor);
        }
        
        if (databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_MYSQL)) {
            DatastoreDescriptor mysqlDatastoreDescriptor = new DatastoreDescriptorImpl(DatabaseDriverCatalog.DATABASE_NAME_MYSQL,
                    "Connect to a MySQL database", JdbcDatastore.class, Arrays.asList("Database"));
            datastoreDescriptors.add(mysqlDatastoreDescriptor);
        }
        
        if (databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_POSTGRESQL)) {
            DatastoreDescriptor postgresqlDatastoreDescriptor = new DatastoreDescriptorImpl(DatabaseDriverCatalog.DATABASE_NAME_POSTGRESQL,
                    "Connect to a PostgreSQL database", JdbcDatastore.class, Arrays.asList("Database"));
            datastoreDescriptors.add(postgresqlDatastoreDescriptor);
        }

        if (databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_ORACLE)) {
            DatastoreDescriptor oracleDatastoreDescriptor = new DatastoreDescriptorImpl(DatabaseDriverCatalog.DATABASE_NAME_ORACLE,
                    "Connect to a Oracle database", JdbcDatastore.class, Arrays.asList("Database"));
            datastoreDescriptors.add(oracleDatastoreDescriptor);
        }

        if (databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_MICROSOFT_SQL_SERVER_JTDS)) {
            DatastoreDescriptor sqlServerDatastoreDescriptor = new DatastoreDescriptorImpl(DatabaseDriverCatalog.DATABASE_NAME_MICROSOFT_SQL_SERVER_JTDS,
                    "Connect to a Microsoft SQL Server database", JdbcDatastore.class, Arrays.asList("Database"));
            datastoreDescriptors.add(sqlServerDatastoreDescriptor);
        }

        return datastoreDescriptors;
    }

    private List<DatastoreDescriptor> getOtherDatastoreDescriptors(Set<String> alreadyAddedDatabaseNames) {
        List<DatastoreDescriptor> datastoreDescriptors = new ArrayList<>();
        
        final List<DatabaseDriverDescriptor> databaseDrivers = _databaseDriverCatalog
                .getInstalledWorkingDatabaseDrivers();
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
                    "Create composite datastore", JdbcDatastore.class);
            datastoreDescriptors.add(compositeDatastoreDescriptor);
        }

        
        return datastoreDescriptors;
    }
}