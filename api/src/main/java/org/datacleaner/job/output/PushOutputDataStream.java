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
package org.datacleaner.job.output;

import org.apache.metamodel.schema.Table;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.connection.PerformanceCharacteristics;
import org.datacleaner.connection.PerformanceCharacteristicsImpl;

public class PushOutputDataStream implements OutputDataStream {

    private static final long serialVersionUID = 1L;

    private final String _name;
    private final Table _table;

    public PushOutputDataStream(String name, Table table) {
        _name = name;
        _table = table;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public Table getTable() {
        return _table;
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(false, false);
    }

    @Override
    public String toString() {
        return "PushOutputDataStream[name=" + _name + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_name == null) ? 0 : _name.hashCode());
        result = prime * result + ((_table == null) ? 0 : _table.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PushOutputDataStream other = (PushOutputDataStream) obj;
        if (_name == null) {
            if (other._name != null)
                return false;
        } else if (!_name.equals(other._name))
            return false;
        if (_table == null) {
            if (other._table != null)
                return false;
        } else if (!_table.equals(other._table))
            return false;
        return true;
    }

}
