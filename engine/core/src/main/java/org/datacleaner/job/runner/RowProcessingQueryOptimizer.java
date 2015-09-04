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
import java.util.List;
import java.util.Set;

import org.apache.metamodel.query.Query;
import org.datacleaner.api.QueryOptimizedFilter;
import org.datacleaner.job.FilterOutcome;

/**
 * Optimizer that will apply possible optimizations coming from
 * {@link QueryOptimizedFilter} instances in the job.
 * 
 * 
 */
public interface RowProcessingQueryOptimizer {

    /**
     * Gets the optimized query.
     * 
     * @return
     */
    public Query getOptimizedQuery();

    /**
     * Gets the optimized list of {@link RowProcessingConsumer}. This list will
     * consist of the original consumers, except the eliminated ones (see
     * {@link #getEliminatedConsumers()}).
     * 
     * @return
     */
    public List<RowProcessingConsumer> getOptimizedConsumers();

    /**
     * Gets the {@link RowProcessingConsumer}s that where eliminated while
     * optimizing the query.
     * 
     * @return
     */
    public Set<? extends RowProcessingConsumer> getEliminatedConsumers();

    /**
     * Gets the {@link FilterOutcome}s that has been optimized by the query.
     * 
     * @return
     */
    public Collection<? extends FilterOutcome> getOptimizedAvailableOutcomes();

    /**
     * Determines if the query has been optimized or not.
     * 
     * @return
     */
    public boolean isOptimizable();

}
