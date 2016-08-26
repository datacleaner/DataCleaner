package org.datacleaner.monitor.server.wizard.shared.datastore;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.datacleaner.connection.Datastore;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.shared.model.DatastoreIdentifier;

public class DatastoreHelper {
    public static String getDatastoreOptions(TenantContext tenantContext) {
        final StringBuilder builder = new StringBuilder();

        for (DatastoreIdentifier id : tenantContext.getDatastores()) {
            builder.append(String.format("<option value=\"%s\">%s</option>", id.getName(), id.getName()));
        }

        return builder.toString();
    }

    public static String getSchemaOptions(TenantContext tenantContext, String datastoreName) {
        final StringBuilder builder = new StringBuilder();
        final DatastoreIdentifier datastoreId = new DatastoreIdentifier(datastoreName);
        final Datastore datastore = tenantContext.getDatastore(datastoreId);

        for (Schema schema : datastore.openConnection().getSchemaNavigator().getSchemas()) {
            builder.append(String.format("<option value=\"%s\">%s</option>", schema.getName(), schema.getName()));
        }

        return builder.toString();
    }

    public static String getTableOptions(TenantContext tenantContext, String datastoreName, String schemaName) {
        final StringBuilder builder = new StringBuilder();
        final DatastoreIdentifier datastoreId = new DatastoreIdentifier(datastoreName);
        final Datastore datastore = tenantContext.getDatastore(datastoreId);
        final Schema schema = datastore.openConnection().getSchemaNavigator().getSchemaByName(schemaName);

        for (Table table : schema.getTables()) {
            builder.append(String.format("<option value=\"%s\">%s</option>", table.getName(), table.getName()));
        }

        return builder.toString();
    }

    public static String getColumnOptions(TenantContext tenantContext, String datastoreName, String schemaName, 
            String tableName) {
        final StringBuilder builder = new StringBuilder();
        final DatastoreIdentifier datastoreId = new DatastoreIdentifier(datastoreName);
        final Datastore datastore = tenantContext.getDatastore(datastoreId);
        final Schema schema = datastore.openConnection().getSchemaNavigator().getSchemaByName(schemaName);
        final Table table = schema.getTableByName(tableName);

        for (Column column : table.getColumns()) {
            builder.append(String.format("<option value=\"%s\">%s</option>", column.getName(), column.getName()));
        }

        return builder.toString();
    }
}
