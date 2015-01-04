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
package org.eobjects.analyzer.job.builder;

import org.eobjects.analyzer.job.AbstractFilterOutcome;
import org.eobjects.analyzer.job.BeanConfiguration;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.HasFilterOutcomes;
import org.eobjects.analyzer.job.ImmutableBeanConfiguration;
import org.eobjects.analyzer.job.ImmutableFilterJob;

public final class LazyFilterOutcome extends AbstractFilterOutcome {

    private static final long serialVersionUID = 1L;

    private final FilterJobBuilder<?, ?> _filterJobBuilder;
    private final Enum<?> _category;

    protected LazyFilterOutcome(FilterJobBuilder<?, ?> filterJobBuilder, Enum<?> category) {
        _filterJobBuilder = filterJobBuilder;
        _category = category;
    }

    @Override
    public HasFilterOutcomes getSource() {
        return _filterJobBuilder;
    }

    @Override
    public FilterJob getFilterJob() {
        if (_filterJobBuilder.isConfigured()) {
            return _filterJobBuilder.toFilterJob();
        } else {
            // Create an incomplete job. This representation is typically used
            // for comparison, not execution.
            final BeanConfiguration beanConfiguration = new ImmutableBeanConfiguration(
                    _filterJobBuilder.getConfiguredProperties());
            return new ImmutableFilterJob(_filterJobBuilder.getName(), _filterJobBuilder.getDescriptor(),
                    beanConfiguration, _filterJobBuilder.getComponentRequirement(),
                    _filterJobBuilder.getMetadataProperties());
        }
    }

    @Override
    public Enum<?> getCategory() {
        return _category;
    }

    public FilterJobBuilder<?, ?> getFilterJobBuilder() {
        return _filterJobBuilder;
    }
}
