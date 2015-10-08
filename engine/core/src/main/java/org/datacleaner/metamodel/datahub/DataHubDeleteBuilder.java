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
        String columnName = whereItems.get(0).getSelectItem().getColumn().getName();
        String sourceIdName = (whereItems.size() > 1) ? whereItems.get(1).getSelectItem().getColumn().getName() :null;
        String id = (String) whereItems.get(0).getOperand();
        if (whereItems.size() == 1 && columnName.equals("gr_id")) {
            _callback.executeDeleteGoldenRecord(id);
        } else if (whereItems.size() == 2 && columnName.equalsIgnoreCase("source_id") && "source_name".equals(sourceIdName)) {
            _callback.executeDeleteSourceRecord(getSourceName(), id, getRecordType());
        } else {
            throw new IllegalArgumentException("Deletes are only allowed on individual records, identified by their id)");            
        }
    }
    
    

    private String getSourceName() {
        return (String)getWhereItems().get(1).getOperand();
    }

    private String getRecordType() {
        String tableName = getTable().getName();
        return RecordType.valueOf(tableName.toUpperCase()).shortname;
    }

}
