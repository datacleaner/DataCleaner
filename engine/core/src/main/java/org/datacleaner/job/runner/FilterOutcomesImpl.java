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
package org.datacleaner.job.runner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.FilterOutcomes;

/**
 * Default implementation of {@link FilterOutcomes}
 */
public final class FilterOutcomesImpl implements FilterOutcomes {

    private final Set<FilterOutcome> _outcomes;

    @SuppressWarnings("unchecked")
    public FilterOutcomesImpl() {
        this(Collections.EMPTY_LIST);
    }

    public FilterOutcomesImpl(Collection<? extends FilterOutcome> availableOutcomes) {
        if (availableOutcomes == null) {
            _outcomes = new HashSet<FilterOutcome>();
        } else {
            // always take a copy of the collection argument
            _outcomes = new HashSet<FilterOutcome>(availableOutcomes);
        }
    }

    @Override
    public void add(FilterOutcome filterOutcome) {
        _outcomes.add(filterOutcome);
    }

    @Override
    public boolean contains(FilterOutcome outcome) {
        return _outcomes.contains(outcome);
    }

    @Override
    public FilterOutcome[] getOutcomes() {
        return _outcomes.toArray(new FilterOutcome[_outcomes.size()]);
    }

    @Override
    public String toString() {
        return "FilterOutcomes[" + _outcomes + "]";
    }

    @Override
    public FilterOutcomes clone() {
        return new FilterOutcomesImpl(_outcomes);
    }
}
