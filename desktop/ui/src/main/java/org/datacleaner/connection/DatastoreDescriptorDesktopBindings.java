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

import java.util.HashMap;
import java.util.Map;

import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.util.IconUtils;
import org.datacleaner.windows.AbstractDialog;
import org.datacleaner.windows.AccessDatastoreDialog;
import org.datacleaner.windows.CassandraDatastoreDialog;
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

public class DatastoreDescriptorDesktopBindings {

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

    private static Map<DatastoreDescriptor, String> _iconPaths = new HashMap<DatastoreDescriptor, String>();

    private static Map<DatastoreDescriptor, Class<? extends AbstractDialog>> _dialogClasses = new HashMap<>();

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
    }

    public static String getIconPath(DatastoreDescriptor datastoreDescriptor) {
        return _iconPaths.get(datastoreDescriptor);
    }

    public static Class<? extends AbstractDialog> getDialogClass(DatastoreDescriptor datastoreDescriptor) {
        return _dialogClasses.get(datastoreDescriptor);
    }

}
