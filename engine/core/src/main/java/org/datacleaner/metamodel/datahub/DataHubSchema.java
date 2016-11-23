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

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.schema.AbstractSchema;
import org.apache.metamodel.schema.MutableTable;
import org.apache.metamodel.schema.Table;

/**
 *  implementation of Datahub schema, final must be implemented in metamodel
 */

public class DataHubSchema extends AbstractSchema {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String _name;
    private String _datastoreName;
    private List<Table> _tables;

    public DataHubSchema() {
        _name = EMPTY;
        _tables = new ArrayList<>();

    }

    @Override
    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }

    public String getDatastoreName() {
        return _datastoreName;
    }

    public void setDatastoreName(final String name) {
        _datastoreName = name;
    }

    @Override
    public Table[] getTables() {
        return _tables.toArray(new Table[_tables.size()]);
    }

    @Override
    public String getQuote() {
        return null;
    }

    public void addTable(final MutableTable table) {
        _tables.add(table);
    }

    public void addTables(final Table[] tables) {
        for (int i = 0; i < tables.length; ++i) {
            _tables.add(tables[i]);
        }
    }

}
