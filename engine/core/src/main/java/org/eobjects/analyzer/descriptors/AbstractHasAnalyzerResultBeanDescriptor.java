/**
 * AnalyzerBeans
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

import java.util.Set;

import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.AnalyzerResultReducer;
import org.eobjects.analyzer.result.HasAnalyzerResult;
import org.eobjects.analyzer.util.ReflectionUtils;

/**
 * Abstract implementation of the {@link HasAnalyzerResultBeanDescriptor}
 * interface. Convenient for implementing it's subclasses.
 * 
 * 
 * 
 * @param <B>
 */
abstract class AbstractHasAnalyzerResultBeanDescriptor<B extends HasAnalyzerResult<?>> extends
        AbstractBeanDescriptor<B> implements HasAnalyzerResultBeanDescriptor<B> {

    private static final long serialVersionUID = 1L;

    private final ResultDescriptor _resultDescriptor;

    public AbstractHasAnalyzerResultBeanDescriptor(Class<B> beanClass, boolean requireInputColumns) {
        super(beanClass, requireInputColumns);
        

        Class<?> typeParameter = ReflectionUtils.getTypeParameter(getComponentClass(), HasAnalyzerResult.class, 0);

        @SuppressWarnings("unchecked")
        Class<? extends AnalyzerResult> resultClass = (Class<? extends AnalyzerResult>) typeParameter;
        _resultDescriptor = Descriptors.ofResult(resultClass);
    }
    
    @Override
    public MetricDescriptor getResultMetric(String name) {
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
