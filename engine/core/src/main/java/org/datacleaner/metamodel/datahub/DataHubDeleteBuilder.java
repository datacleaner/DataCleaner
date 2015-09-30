package org.datacleaner.metamodel.datahub;

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
