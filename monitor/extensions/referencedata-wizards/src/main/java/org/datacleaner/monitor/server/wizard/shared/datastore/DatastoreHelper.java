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

import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.datacleaner.connection.Datastore;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.shared.model.DatastoreIdentifier;

public class DatastoreHelper {
    public static String getDatastoreOptions(final TenantContext tenantContext) {
        final List<String> names = new ArrayList<>();

        for (final DatastoreIdentifier datastoreId : tenantContext.getDatastores()) {
            names.add(datastoreId.getName());
        }

        return getOptionsHtml(names);
    }

    public static String getSchemaOptions(final TenantContext tenantContext, final String datastoreName) {
        final List<String> names = new ArrayList<>();

        for (final Schema schema : getDatastore(tenantContext, datastoreName).openConnection().getSchemaNavigator()
                .getSchemas()) {
            names.add(schema.getName());
        }

        return getOptionsHtml(names);
    }

    public static String getTableOptions(final TenantContext tenantContext, final String datastoreName,
            final String schemaName) {
        final List<String> names = new ArrayList<>();

        for (final Table table : getSchema(tenantContext, datastoreName, schemaName).getTables()) {
            names.add(table.getName());
        }

        return getOptionsHtml(names);
    }

    public static String getColumnOptions(final TenantContext tenantContext, final String datastoreName,
            final String schemaName, final String tableName) {
        final List<String> names = new ArrayList<>();

        for (final Column column : getTable(tenantContext, datastoreName, schemaName, tableName).getColumns()) {
            names.add(column.getName());
        }

        return getOptionsHtml(names);
    }

    private static String getOptionsHtml(final List<String> allNames) {
        if (allNames == null || allNames.isEmpty()) {
            return "";
        }

        final StringBuilder builder = new StringBuilder();

        for (final String name : allNames) {
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
