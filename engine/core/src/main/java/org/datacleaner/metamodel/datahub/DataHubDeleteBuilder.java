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

import java.util.List;

import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.delete.AbstractRowDeletionBuilder;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.schema.Table;

public class DataHubDeleteBuilder extends AbstractRowDeletionBuilder {

    enum RecordType {
        PERSON("P"), ORGANIZATION("O");
        private String shortname;

        RecordType(String shortname) {
            this.shortname = shortname;
        }
    }

    private final DataHubUpdateCallback _callback;

    public DataHubDeleteBuilder(DataHubUpdateCallback callback, Table table) {
        super(table);
        _callback = callback;
    }

    @Override
    public void execute() throws MetaModelException {
        List<FilterItem> whereItems = getWhereItems();
        if (whereItems.size() != 1) {
            throw new IllegalArgumentException("Deletes are only allowed on individual records, identified by id only)");
        }
        String columnName = whereItems.get(0).getSelectItem().getColumn().getName();
        String id = (String) whereItems.get(0).getOperand();
        if (columnName.equals("gr_id")) {
            _callback.executeDeleteGoldenRecord(id);
        } else if (columnName.equalsIgnoreCase("source_id")) {
            _callback.executeDeleteSourceRecord(getSourceName(), id, getRecordType());
        } else {
            throw new IllegalArgumentException("Deletes are only allowed on individual records, identified by id)");            
        }
    }

    private String getSourceName() {
        //TODO currently the schema of the table always refers to the "GoldenRecords" schema. Should be fixed(?)
        // We now get the correct schema name from the where item.
        //return getTable().getSchema().getName();
        String prefixedName = getWhereItems().get(0).getSelectItem().getColumn().getTable().getSchema().getName();
        String[] parts = prefixedName.split("-");
        return parts[parts.length - 1];
    }

    private String getRecordType() {
        String tableName = getTable().getName();
        return RecordType.valueOf(tableName.toUpperCase()).shortname;
    }

}
