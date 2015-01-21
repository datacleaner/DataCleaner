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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.metamodel.query.Query;
import org.datacleaner.api.Filter;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.QueryOptimizedFilter;
import org.datacleaner.components.maxrows.MaxRowsFilter;
import org.datacleaner.connection.Datastore;
import org.datacleaner.descriptors.FilterDescriptor;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.HasComponentRequirement;
import org.datacleaner.job.HasFilterOutcomes;
import org.datacleaner.job.InputColumnSinkJob;
import org.datacleaner.job.InputColumnSourceJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Optimizer that will apply possible optimizations coming from
 * {@link QueryOptimizedFilter} instances in the job.
 * 
 * 
 */
public class RowProcessingQueryOptimizer {

    private static final Logger logger = LoggerFactory.getLogger(RowProcessingQueryOptimizer.class);

    private static final Class<?>[] ALWAYS_OPTIMIZABLE = new Class[] { MaxRowsFilter.class };
    private final Datastore _datastore;
    private final Query _baseQuery;
    private final List<RowProcessingConsumer> _consumers;
    private final Map<FilterConsumer, FilterOutcome> _optimizedFilters;

    public RowProcessingQueryOptimizer(Datastore datastore, List<RowProcessingConsumer> consumers, Query baseQuery) {
        _datastore = datastore;
        _consumers = consumers;
        _baseQuery = baseQuery;
        _optimizedFilters = new HashMap<FilterConsumer, FilterOutcome>();

        init();
    }

    private void init() {
        int consumerIndex = 0;
        for (final RowProcessingConsumer consumer : _consumers) {
            if (consumer instanceof FilterConsumer) {
                final FilterConsumer filterConsumer = (FilterConsumer) consumer;

                if (!isOptimizable(filterConsumer)) {
                    logger.debug("Breaking optimization. Not optimizable: {}", filterConsumer);

                    // if it can be established that the filter is not
                    // optimizable at all (either because it is not an
                    // QueryOptimizableFilter or because input is not physical
                    // columns), then abort.
                    break;
                }

                final Collection<FilterOutcome> outcomes = filterConsumer.getComponentJob().getFilterOutcomes();
                FilterOutcome optimizableOutcome = null;

                for (final FilterOutcome outcome : outcomes) {
                    final boolean optimizable = isOptimizable(filterConsumer, outcome, consumerIndex);
                    if (optimizable) {
                        if (optimizableOutcome != null) {
                            // cannot have multiple optimizable outcomes for a
                            // single filter
                            break;
                        }
                        optimizableOutcome = outcome;
                    }
                }

                if (optimizableOutcome == null) {
                    break;
                }

                _optimizedFilters.put(filterConsumer, optimizableOutcome);
            }
            consumerIndex++;
        }
    }

    private boolean isOptimizable(FilterConsumer filterConsumer) {
        final FilterDescriptor<?, ?> descriptor = filterConsumer.getComponentJob().getDescriptor();
        if (!descriptor.isQueryOptimizable()) {
            logger.debug("FilterBeanDescriptor not optimizable: {}", descriptor);
            return false;
        }
        final InputColumn<?>[] input = filterConsumer.getRequiredInput();
        for (InputColumn<?> inputColumn : input) {
            if (inputColumn.isVirtualColumn()) {
                logger.debug("InputColumn is virtual: {}, so filter is not optimizable: {}", inputColumn,
                        filterConsumer);
                return false;
            }
        }
        return true;
    }

    private boolean isOptimizable(final FilterConsumer filterConsumer, final FilterOutcome filterOutcome,
            final int consumerIndex) {
        if (!filterConsumer.isQueryOptimizable(filterOutcome)) {
            // the filter is not optimizable
            return false;
        }

        if (!_datastore.getPerformanceCharacteristics().isQueryOptimizationPreferred()) {
            // the datastore doesn't prefer query optimization
            Class<?> filterClass = filterConsumer.getComponentJob().getDescriptor().getComponentClass();
            if (!ArrayUtils.contains(ALWAYS_OPTIMIZABLE, filterClass)) {
                logger.debug(
                        "Datastore performance characteristics indicate that query optimization will not improve performance for {}, stopping",
                        filterConsumer);

                // the filter is not in the "always optimizable" set.
                return false;
            }
        }

        final Set<InputColumn<?>> satisfiedColumns = new HashSet<InputColumn<?>>();
        final Set<FilterOutcome> satisfiedRequirements = new HashSet<FilterOutcome>();
        satisfiedRequirements.add(filterOutcome);

        for (int i = consumerIndex + 1; i < _consumers.size(); i++) {
            boolean independentComponent = true;

            final RowProcessingConsumer nextConsumer = _consumers.get(i);
            final ComponentJob componentJob = nextConsumer.getComponentJob();
            if (componentJob instanceof HasComponentRequirement) {
                final ComponentRequirement componentRequirement = componentJob.getComponentRequirement();
                if (componentRequirement != null) {
                    final Collection<FilterOutcome> requirements = componentRequirement.getProcessingDependencies();
                    for (final FilterOutcome requirement : requirements) {
                        if (!satisfiedRequirements.contains(requirement)) {
                            logger.debug("Requirement {} is not met using query optimization of {}", requirement,
                                    filterConsumer);
                            return false;
                        } else {
                            independentComponent = false;
                        }
                    }
                }
            }

            if (componentJob instanceof InputColumnSinkJob) {
                InputColumn<?>[] requiredColumns = ((InputColumnSinkJob) componentJob).getInput();
                for (InputColumn<?> column : requiredColumns) {
                    if (column.isVirtualColumn()) {
                        if (!satisfiedColumns.contains(column)) {
                            logger.debug(
                                    "InputColumn {} is available at query time, and therefore not satisfied for query optimization of {}",
                                    column, filterConsumer);
                            return false;
                        } else {
                            independentComponent = false;
                        }
                    }
                }
            }

            if (independentComponent) {
                // totally independent components prohibit optimization
                logger.debug(
                        "Component {} is completely independent. Position in chain is not determinable, so optimization cannot be done.",
                        filterConsumer);
                return false;
            }

            // this component is accepted now, add it's outcomes to the
            // satisfied requirements
            if (componentJob instanceof HasFilterOutcomes) {
                final Collection<FilterOutcome> outcomes = ((HasFilterOutcomes) componentJob).getFilterOutcomes();
                for (final FilterOutcome outcome : outcomes) {
                    satisfiedRequirements.add(outcome);
                }
            }

            if (componentJob instanceof InputColumnSourceJob) {
                InputColumn<?>[] output = ((InputColumnSourceJob) componentJob).getOutput();
                for (InputColumn<?> column : output) {
                    satisfiedColumns.add(column);
                }
            }
        }

        return true;
    }

    /**
     * Gets the optimized query.
     * 
     * @return
     */
    public Query getOptimizedQuery() {
        // if (isOptimizable()) {
        // return _baseQuery;
        // }

        // create a copy/clone of the original query
        Query q = _baseQuery.clone();

        Set<Entry<FilterConsumer, FilterOutcome>> entries = _optimizedFilters.entrySet();
        for (Entry<FilterConsumer, FilterOutcome> entry : entries) {

            FilterConsumer consumer = entry.getKey();
            FilterOutcome outcome = entry.getValue();
            Filter<?> filter = consumer.getComponent();

            @SuppressWarnings("rawtypes")
            QueryOptimizedFilter queryOptimizedFilter = (QueryOptimizedFilter) filter;

            @SuppressWarnings("unchecked")
            Query newQuery = queryOptimizedFilter.optimizeQuery(q, outcome.getCategory());
            q = newQuery;
        }
        return q;
    }

    /**
     * Gets the optimized list of {@link RowProcessingConsumer}. This list will
     * consist of the original consumers, except the eliminated ones (see
     * {@link #getEliminatedConsumers()}).
     * 
     * @return
     */
    public List<RowProcessingConsumer> getOptimizedConsumers() {
        List<RowProcessingConsumer> result = new ArrayList<RowProcessingConsumer>(_consumers);
        for (FilterConsumer filterConsumer : _optimizedFilters.keySet()) {
            if (filterConsumer.isRemoveableUponOptimization()) {
                result.remove(filterConsumer);
            }
        }
        return result;
    }

    /**
     * Gets the {@link RowProcessingConsumer}s that where eliminated while
     * optimizing the query.
     * 
     * @return
     */
    public Set<? extends RowProcessingConsumer> getEliminatedConsumers() {
        final Set<FilterConsumer> consumers = _optimizedFilters.keySet();
        return consumers;
    }

    /**
     * Gets the {@link Outcome}s that has been optimized by the query.
     * 
     * @return
     */
    public Collection<? extends FilterOutcome> getOptimizedAvailableOutcomes() {
        return _optimizedFilters.values();
    }

    /**
     * Determines if the query has been optimized or not.
     * 
     * @return
     */
    public boolean isOptimizable() {
        return !_optimizedFilters.isEmpty();
    }

}
