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
 */package org.datacleaner.metamodel.datahub;

import java.util.List;

import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.delete.AbstractRowDeletionBuilder;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.schema.Table;

public class DataHubDeleteBuilder extends AbstractRowDeletionBuilder{

    
    private final DataHubUpdateCallback _callback;
    
    public DataHubDeleteBuilder(DataHubUpdateCallback callback, Table table) {
        super(table);
        _callback = callback;
    }

    @Override
    public void execute() throws MetaModelException {
        String grId = createDeleteData();
        _callback.executeDelete(grId);
        
    }

    private String createDeleteData() {
        List<FilterItem> whereItems = getWhereItems();
        if (whereItems.size() != 1 || !"gr_id".equals(whereItems.get(0).getSelectItem().getColumn().getName())) {
            throw new IllegalArgumentException("Updates are only allowed on individual records, identified by gr_id (golden record id)");
        }
        return (String) whereItems.get(0).getOperand();
    }

}
