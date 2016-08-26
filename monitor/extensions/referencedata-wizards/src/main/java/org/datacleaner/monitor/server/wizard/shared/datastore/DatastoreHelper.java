package org.datacleaner.monitor.server.wizard.shared.datastore;

import java.util.List;

import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.datacleaner.connection.Datastore;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.datacleaner.monitor.shared.model.HasName;

public class DatastoreHelper {
    public static String getDatastoreOptions(final TenantContext tenantContext) {
        final List<DatastoreIdentifier> datastoreIds = tenantContext.getDatastores();
        
        return getOptionsHtml(datastoreIds.toArray(new Datastore[datastoreIds.size()]));
    }

    public static String getSchemaOptions(final TenantContext tenantContext, final String datastoreName) {
        return getOptionsHtml(getDatastore(tenantContext, datastoreName).openConnection().getSchemaNavigator()
                .getSchemas());
    }

    public static String getTableOptions(TenantContext tenantContext, String datastoreName, String schemaName) {
        return getOptionsHtml(getSchema(tenantContext, datastoreName, schemaName).getTables());
    }

    public static String getColumnOptions(TenantContext tenantContext, String datastoreName, String schemaName, 
            String tableName) {
        return getOptionsHtml(getTable(tenantContext, datastoreName, schemaName, tableName).getColumns());
    }

    private static String getOptionsHtml(final Object[] objects) {
        final StringBuilder builder = new StringBuilder();

        for (Object item : objects) {
            final String name = (item instanceof HasName) ? ((HasName)item).getName() : item.toString();
            builder.append(getOptionHtml(name));
        }

        return builder.toString();
    }

    private static String getOptionHtml(final String name) {
        return String.format("<option value=\"%s\">%s</option>", name, name);
    }

    private static Datastore getDatastore(final TenantContext tenantContext, final String datastoreName) {
        return tenantContext.getDatastore(new DatastoreIdentifier(datastoreName));
    }

    private static Schema getSchema(final TenantContext tenantContext, final String datastoreName, 
            final String schemaName) {
        return getDatastore(tenantContext, datastoreName).openConnection().getSchemaNavigator()
                .getSchemaByName(schemaName);
    }

    private static Table getTable(final TenantContext tenantContext, final String datastoreName, 
            final String schemaName, final String tableName) {
        return getSchema(tenantContext, datastoreName, schemaName).getTableByName(tableName);
    }
}
