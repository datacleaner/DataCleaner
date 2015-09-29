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

import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.AbstractRowUpdationBuilder;
import org.datacleaner.metamodel.datahub.update.UpdateData;
import org.datacleaner.metamodel.datahub.update.UpdateField;

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
        List<UpdateField> fields = new ArrayList<UpdateField>();
        for (int i = 0; i < columns.length; i++) {
            final Object value = values[i];
            if (value != null || explicitNulls[i]) {
                String columnName = columns[i].getName();
                final UpdateField field = new UpdateField(columnName, value.toString());
                fields.add(field);
            }
        }

        List<FilterItem> whereItems = getWhereItems();
        if (!(whereItems.size() == 1 && whereItems.get(0).getSelectItem().getColumn().getName().equals("gr_id"))) {
            throw new IllegalArgumentException("Updates are only allowed on individual records, identified by gr_id (golden record id)");
        }
        String grId = (String) whereItems.get(0).getOperand();
        return new UpdateData(grId, fields.toArray(new UpdateField[fields.size()]));
    }

}
