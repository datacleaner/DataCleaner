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
package org.datacleaner.components.group;

import com.google.common.base.Strings;

final class ConcatAggregateBuilder extends AbstractRowNumberAwareAggregateBuilder<String> {

    private final String _concatenationSeparator;
    private final StringBuilder _result;

    public ConcatAggregateBuilder(SortationType sortationType, boolean skipNulls, String concatenationSeparator) {
        super(sortationType, skipNulls);
        _concatenationSeparator = concatenationSeparator;
        _result = new StringBuilder();
    }

    @Override
    protected void addSorted(Object o) {
        if (!Strings.isNullOrEmpty(_concatenationSeparator) && _result.length() != 0) {
            _result.append(_concatenationSeparator);
        }
        _result.append(o == null ? "" : o.toString());
    }
    
    @Override
    public String getAggregateSorted() {
        return _result.toString();
    }
}
