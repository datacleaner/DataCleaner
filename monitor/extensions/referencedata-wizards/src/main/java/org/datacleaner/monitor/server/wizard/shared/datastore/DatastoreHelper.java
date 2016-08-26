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
            final String option = String.format("<option value=\"%s\">%s</option>", name, name);
            builder.append(option);
        }

        return builder.toString();
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
