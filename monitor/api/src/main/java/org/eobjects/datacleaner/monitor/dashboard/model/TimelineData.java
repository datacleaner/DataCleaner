/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.monitor.dashboard.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the data shown in a timeline. Consists of a list of
 * {@link TimelineDataRow}s.
 */
public class TimelineData implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private List<TimelineDataRow> _rows;

    public List<TimelineDataRow> getRows() {
        if (_rows == null) {
            _rows = new ArrayList<TimelineDataRow>();
        }
        return _rows;
    }
    
    public void setRows(List<TimelineDataRow> rows) {
        _rows = rows;
    }

    @Override
    public String toString() {
        return "TimelineData[" + getRows().size() + " rows]";
    }
}
