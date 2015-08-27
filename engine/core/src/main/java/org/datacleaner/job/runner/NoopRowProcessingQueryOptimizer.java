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
import java.util.List;
import java.util.Set;

import org.apache.metamodel.query.Query;
import org.datacleaner.job.FilterOutcome;

/**
 * A {@link RowProcessingQueryOptimizer} that does not apply any optimizations
 */
public class NoopRowProcessingQueryOptimizer implements RowProcessingQueryOptimizer {

    private final Query _query;
    private List<RowProcessingConsumer> _consumers;
    
    public NoopRowProcessingQueryOptimizer(Query query, List<RowProcessingConsumer> consumers) {
        _query = query;
        _consumers = consumers;
    }

    @Override
    public Query getOptimizedQuery() {
        return _query;
    }

    @Override
    public List<RowProcessingConsumer> getOptimizedConsumers() {
        return _consumers;
    }

    @Override
    public Set<? extends RowProcessingConsumer> getEliminatedConsumers() {
        return Collections.emptySet();
    }

    @Override
    public Collection<? extends FilterOutcome> getOptimizedAvailableOutcomes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isOptimizable() {
        return false;
    }

}
