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

import org.datacleaner.api.Concurrent;
import org.datacleaner.api.Filter;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.Optimizeable;
import org.datacleaner.api.QueryOptimizedFilter;
import org.datacleaner.job.FilterJob;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.FilterOutcomes;
import org.datacleaner.job.ImmutableFilterOutcome;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.SourceColumnFinder;

final class FilterConsumer extends AbstractRowProcessingConsumer implements RowProcessingConsumer {

    private final Filter<?> _filter;
    private final FilterJob _filterJob;
    private final InputColumn<?>[] _inputColumns;
    private final boolean _concurrent;

    public FilterConsumer(Filter<?> filter, FilterJob filterJob, InputColumn<?>[] inputColumns,
            SourceColumnFinder sourceColumnFinder) {
        super(null, null, filterJob, filterJob, sourceColumnFinder);
        _filter = filter;
        _filterJob = filterJob;
        _inputColumns = inputColumns;
        _concurrent = determineConcurrent();
    }
    
    public FilterConsumer(Filter<?> filter, FilterJob filterJob, InputColumn<?>[] inputColumns,
            RowProcessingPublishers publishers) {
        super(publishers, filterJob, filterJob);
        _filter = filter;
        _filterJob = filterJob;
        _inputColumns = inputColumns;
        _concurrent = determineConcurrent();
    }

    private boolean determineConcurrent() {
        Concurrent concurrent = _filterJob.getDescriptor().getAnnotation(Concurrent.class);
        if (concurrent == null) {
            // filter are by default concurrent
            return true;
        }
        return concurrent.value();
    }

    @Override
    public boolean isConcurrent() {
        return _concurrent;
    }

    @Override
    public InputColumn<?>[] getRequiredInput() {
        return _inputColumns;
    }

    @Override
    public Filter<?> getComponent() {
        return _filter;
    }

    @Override
    public void consumeInternal(InputRow row, int distinctCount, FilterOutcomes outcomes, RowProcessingChain chain) {
        Enum<?> category = _filter.categorize(row);
        FilterOutcome outcome = new ImmutableFilterOutcome(_filterJob, category);
        outcomes.add(outcome);
        chain.processNext(row, distinctCount, outcomes);
    }

    @Override
    public FilterJob getComponentJob() {
        return _filterJob;
    }

    @Override
    public String toString() {
        return "FilterConsumer[" + _filter + "]";
    }

    public boolean isQueryOptimizable(FilterOutcome filterOutcome) {
        if (_filter instanceof QueryOptimizedFilter) {
            @SuppressWarnings("rawtypes")
            QueryOptimizedFilter queryOptimizedFilter = (QueryOptimizedFilter) _filter;
            @SuppressWarnings("unchecked")
            boolean optimizable = queryOptimizedFilter.isOptimizable(filterOutcome.getCategory());
            return optimizable;
        }
        return false;
    }

    public boolean isRemoveableUponOptimization() {
        final Optimizeable optimizeable = ReflectionUtils.getAnnotation(_filterJob.getDescriptor().getComponentClass(),
                Optimizeable.class);
        if (optimizeable == null) {
            return true;
        }
        return optimizeable.removeableUponOptimization();
    }
}
