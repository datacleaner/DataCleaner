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

import org.apache.metamodel.schema.Table;

/**
 * Creates a cache of update queries to be sent to DataCleaner Monitor in a
 * single batch.
 *
 */
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
