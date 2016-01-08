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

import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.util.AggregateBuilder;

final class CreateListAggregateBuilder extends AbstractRowNumberAwareAggregateBuilder<List<?>> implements
        AggregateBuilder<List<?>> {

    private final List<Object> _result;

    public CreateListAggregateBuilder(SortationType sortationType, boolean skipNulls) {
        super(sortationType, skipNulls);
        _result = new ArrayList<>();
    }

    @Override
    protected void addSorted(Object o) {
        _result.add(o);
    }

    @Override
    public List<?> getAggregateSorted() {
        return _result;
    }

}
