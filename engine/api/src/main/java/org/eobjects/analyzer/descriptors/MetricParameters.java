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
package org.eobjects.analyzer.descriptors;

import org.eobjects.analyzer.data.InputColumn;

/**
 * Container class for metric parameters.
 */
public class MetricParameters {

    private final String _queryString;
    private final InputColumn<?> _queryInputColumn;

    public MetricParameters() {
        this(null, null);
    }

    public MetricParameters(InputColumn<?> queryInputColumn) {
        this(null, queryInputColumn);
    }

    public MetricParameters(String queryString) {
        this(queryString, null);

    }

    public MetricParameters(String queryString, InputColumn<?> queryInputColumn) {
        _queryString = queryString;
        _queryInputColumn = queryInputColumn;
    }

    public InputColumn<?> getQueryInputColumn() {
        return _queryInputColumn;
    }

    public String getQueryString() {
        return _queryString;
    }
}
