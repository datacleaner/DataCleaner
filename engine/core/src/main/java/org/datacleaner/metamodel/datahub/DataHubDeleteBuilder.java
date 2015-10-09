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
    private static final String GR_ID_COLUMN_NAME = "gr_id";
    private static final String SOURCE_ID_COLUMN_NAME = "source_id";
    private static final String SOURCE_NAME_COLUMN_NAME = "source_name";

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
        if (whereItems == null || whereItems.size() == 0) {
            throw new IllegalArgumentException("Delete requires a condition.");
        }
        String firstColumnName = getConditionColumnName(whereItems.get(0));
        if (firstColumnName.equals(GR_ID_COLUMN_NAME)) {
            deleteGoldenRecord(whereItems);
        } else if (firstColumnName.equalsIgnoreCase(SOURCE_ID_COLUMN_NAME)) {
            deleteSourceRecord(whereItems);
        } else {
            throw new IllegalArgumentException("Delete condition is not valid.");
        }
    }

    private void deleteSourceRecord(List<FilterItem> whereItems) {
        if (whereItems.size() != 2) {
            throw new IllegalArgumentException(
                    "Delete must be executed on a SourceRecordsGoldenRecordFormat table using " + SOURCE_ID_COLUMN_NAME
                    + " and " + SOURCE_NAME_COLUMN_NAME + " as condition values.");
       }
        FilterItem firstWhereItem = whereItems.get(0);
        FilterItem secondWhereItem = whereItems.get(1);

        String secondColumnName = getConditionColumnName(secondWhereItem);
        if (SOURCE_NAME_COLUMN_NAME.equals(secondColumnName)) {
            String id = getConditionColumnValue(firstWhereItem);
            String sourceName = getConditionColumnValue(secondWhereItem);
            _callback.executeDeleteSourceRecord(sourceName, id, getRecordType());
        } else {
            throw new IllegalArgumentException(
                    "Delete must be executed on a SourceRecordsGoldenRecordFormat table using " + SOURCE_ID_COLUMN_NAME
                            + " and " + SOURCE_NAME_COLUMN_NAME + " as condition values.");
        }
    }

    private void deleteGoldenRecord(List<FilterItem> whereItems) {
        if (whereItems.size() != 1) {
            throw new IllegalArgumentException("Delete requires the " + GR_ID_COLUMN_NAME
                    + " as the sole condition value.");
        }
        FilterItem whereItem = whereItems.get(0);
        String grId = getConditionColumnValue(whereItem);
        _callback.executeDeleteGoldenRecord(grId);
    }

    private String getConditionColumnValue(FilterItem filterItem) {
        return (String) filterItem.getOperand();
    }

    private String getConditionColumnName(FilterItem filterItem) {
        return filterItem.getSelectItem().getColumn().getName();
    }

    private String getRecordType() {
        String tableName = getTable().getName();
        try {
            return RecordType.valueOf(tableName.toUpperCase()).shortname;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Illegal table name: \"" + tableName
                    + "\". Table name should be either \"person\" or \"organization\".");
        }
    }

}
