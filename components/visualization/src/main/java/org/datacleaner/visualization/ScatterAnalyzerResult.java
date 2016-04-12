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

package org.datacleaner.visualization;

import java.util.List;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;

public class ScatterAnalyzerResult implements AnalyzerResult {

    private static final long serialVersionUID = 1L;
    private final List<ScatterGroup> _groups;
    private final InputColumn<?> _variable1;
    private final InputColumn<?> _variable2;
    private final InputColumn<?> _groupColumn;

    public ScatterAnalyzerResult(List<ScatterGroup> groups, InputColumn<?> variable1, InputColumn<?> variable2,
            InputColumn<?> groupColumn) {
        _groups = groups;
        _variable1 = variable1;
        _variable2 = variable2;
        _groupColumn = groupColumn;
    }

    public InputColumn<?> getVariable1() {
        return _variable1;
    }

    public InputColumn<?> getVariable2() {
        return _variable2;
    }

    public List<ScatterGroup> getGroups() {
        return _groups;
    }

    public InputColumn<?> getGroupColumn() {
        return _groupColumn;
    }

    public boolean hasGroups() {
        return _groupColumn != null;
    }
}
