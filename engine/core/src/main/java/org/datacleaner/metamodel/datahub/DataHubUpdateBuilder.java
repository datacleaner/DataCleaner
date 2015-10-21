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
package org.datacleaner.metamodel.datahub;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.AbstractRowUpdationBuilder;
import org.datacleaner.metamodel.datahub.update.UpdateData;

public class DataHubUpdateBuilder extends AbstractRowUpdationBuilder {

    private final DataHubUpdateCallback _callback;

    public DataHubUpdateBuilder(DataHubUpdateCallback dataHubUpdateCallback, Table table) {
        super(table);
        _callback = dataHubUpdateCallback;
    }

    @Override
    public void execute() throws MetaModelException {
        UpdateData updateData = createUpdateData();
        _callback.executeUpdate(updateData);
    }

    private UpdateData createUpdateData() {
        final Object[] values = getValues();
        Column[] columns = getColumns();
        boolean[] explicitNulls = getExplicitNulls();
        Map<String, Object> fields = new HashMap<String, Object>();
        for (int i = 0; i < columns.length; i++) {
            final Object value = values[i];
            if (value != null || explicitNulls[i]) {
                String columnName = columns[i].getName();
                if (columnName.startsWith("_")) {
                    throw new IllegalArgumentException("Updates are not allowed on fields containing meta data, identified by the prefix \" _\".");
                }
                fields.put(columnName, value);
            }
        }

        List<FilterItem> whereItems = getWhereItems();
        if (whereItems.size() != 1 || !"gr_id".equals(whereItems.get(0).getSelectItem().getColumn().getName())) {
            throw new IllegalArgumentException("Updates should have the gr_id as the sole condition value.");
        }
        String grId = (String) whereItems.get(0).getOperand();
        return new UpdateData(grId, fields);
    }
}
