package org.datacleaner.metamodel.datahub;

import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.schema.Table;

public class PendingUpdates {
    private final List<String> _queries;
    private final Table _table;

    public PendingUpdates(Table table, String query) {
        _queries = new ArrayList<String>();
        _queries.add(query);
        _table = table;
    }

    public List<String> getQueries() {
        return _queries;
    }

    public Table getTable() {
        return _table;
    }

    public void addQuery(String query) {
        _queries.add(query);        
    }

    public int size() {
        return _queries.size();
    }

    public boolean isEmpty() {
        return _queries.size() == 0;
    }
}
