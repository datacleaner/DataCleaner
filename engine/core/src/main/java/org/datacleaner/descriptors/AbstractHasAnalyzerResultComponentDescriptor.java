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
package org.datacleaner.descriptors;

import java.util.Set;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.AnalyzerResultReducer;
import org.datacleaner.api.HasAnalyzerResult;
import org.datacleaner.util.ReflectionUtils;

/**
 * Abstract implementation of the {@link HasAnalyzerResultComponentDescriptor}
 * interface. Convenient for implementing it's subclasses.
 *
 *
 *
 * @param <B>
 */
abstract class AbstractHasAnalyzerResultComponentDescriptor<B extends HasAnalyzerResult<?>>
        extends AbstractComponentDescriptor<B> implements HasAnalyzerResultComponentDescriptor<B> {

    private static final long serialVersionUID = 1L;

    private final ResultDescriptor _resultDescriptor;

    AbstractHasAnalyzerResultComponentDescriptor(final Class<B> beanClass, final boolean requireInputColumns) {
        super(beanClass, requireInputColumns);

        final Class<?> typeParameter =
                ReflectionUtils.getTypeParameter(getComponentClass(), HasAnalyzerResult.class, 0);

        @SuppressWarnings("unchecked") final Class<? extends AnalyzerResult> resultClass =
                (Class<? extends AnalyzerResult>) typeParameter;
        _resultDescriptor = Descriptors.ofResult(resultClass);
    }

    @Override
    public MetricDescriptor getResultMetric(final String name) {
        return _resultDescriptor.getResultMetric(name);
    }

    @Override
    public Set<MetricDescriptor> getResultMetrics() {
        return _resultDescriptor.getResultMetrics();
    }

    @Override
    public Class<? extends AnalyzerResultReducer<?>> getResultReducerClass() {
        return _resultDescriptor.getResultReducerClass();
    }

    @Override
    public Class<? extends AnalyzerResult> getResultClass() {
        return _resultDescriptor.getResultClass();
    }
}
