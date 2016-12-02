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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Metric;
import org.datacleaner.api.ParameterizableMetric;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.StringUtils;

/**
 * Default {@link MetricDescriptor} implementation.
 */
final class MetricDescriptorImpl extends AbstractMetricDescriptor implements MetricDescriptor {

    private static final long serialVersionUID = 1L;

    private final transient Method _method;

    private final String _name;
    private final Class<? extends AnalyzerResult> _resultClass;
    private final String _methodName;

    MetricDescriptorImpl(final Class<? extends AnalyzerResult> resultClass, final Method method) {
        _resultClass = resultClass;
        _method = method;
        _method.setAccessible(true);

        final String metricName = ReflectionUtils.getAnnotation(_method, Metric.class).value();
        if (StringUtils.isNullOrEmpty(metricName)) {
            throw new IllegalStateException("Metric method has no name: " + _method);
        }
        _name = metricName.trim();
        _methodName = _method.getName();
    }

    public Method getMethod() {
        if (_method == null) {
            final Method method = ReflectionUtils.getMethod(_resultClass, _methodName, true);
            if (method == null) {
                throw new IllegalStateException("No such method: " + _methodName + " in " + _resultClass);
            }
            return method;
        }
        return _method;
    }

    @Override
    public String getName() {
        return _name;
    }

    public Class<? extends AnalyzerResult> getResultClass() {
        return _resultClass;
    }

    @Override
    public Collection<String> getMetricParameterSuggestions(final AnalyzerResult result) {
        final Method method = getMethod();
        final Class<?> returnType = method.getReturnType();
        if (ReflectionUtils.is(returnType, ParameterizableMetric.class)) {
            final Object[] methodParameters = createMethodParameters(method, null);
            try {
                final Object returnValue = method.invoke(result, methodParameters);
                final ParameterizableMetric parameterizableMetric = (ParameterizableMetric) returnValue;
                return parameterizableMetric.getParameterSuggestions();
            } catch (final Exception e) {
                throw new IllegalStateException("Could not invoke metric getter " + _methodName, e);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Number getValue(final AnalyzerResult result, final MetricParameters metricParameters) {
        if (result == null) {
            throw new IllegalArgumentException("AnalyzerResult cannot be null");
        }
        final Method method = getMethod();
        final Object[] methodParameters = createMethodParameters(method, metricParameters);
        try {
            final Object returnValue = method.invoke(result, methodParameters);
            final Number number;
            if (returnValue instanceof ParameterizableMetric) {
                final ParameterizableMetric parameterizableMetric = (ParameterizableMetric) returnValue;
                number = parameterizableMetric.getValue(metricParameters.getQueryString());
            } else {
                number = (Number) returnValue;
            }
            return number;
        } catch (final Exception e) {
            throw new IllegalStateException("Could not invoke metric getter " + _methodName, e);
        }
    }

    private Object[] createMethodParameters(final Method method, final MetricParameters metricParameters) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes == null || parameterTypes.length == 0) {
            return null;
        }

        final Object[] result = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            if (String.class == parameterTypes[i]) {
                result[i] = metricParameters.getQueryString();
            } else if (InputColumn.class == parameterTypes[i]) {
                result[i] = metricParameters.getQueryInputColumn();
            } else {
                throw new IllegalStateException("Unsupported metric parameter type: " + parameterTypes[i]);
            }
        }

        return result;
    }

    @Override
    public boolean isParameterizedByInputColumn() {
        final Method method = getMethod();
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes == null || parameterTypes.length == 0) {
            return false;
        }

        for (final Class<?> parameterType : parameterTypes) {
            if (InputColumn.class == parameterType) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isParameterizedByString() {
        final Method method = getMethod();
        final Class<?> returnType = method.getReturnType();
        if (ReflectionUtils.is(returnType, ParameterizableMetric.class)) {
            return true;
        }

        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes == null || parameterTypes.length == 0) {
            return false;
        }

        for (final Class<?> parameterType : parameterTypes) {
            if (String.class == parameterType) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Set<Annotation> getAnnotations() {
        final Annotation[] annotations = getMethod().getAnnotations();
        return new HashSet<>(Arrays.asList(annotations));
    }

    @Override
    public <A extends Annotation> A getAnnotation(final Class<A> annotationClass) {
        return ReflectionUtils.getAnnotation(getMethod(), annotationClass);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_resultClass == null) ? 0 : _resultClass.hashCode());
        result = prime * result + ((_methodName == null) ? 0 : _methodName.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MetricDescriptorImpl other = (MetricDescriptorImpl) obj;
        if (_resultClass == null) {
            if (other._resultClass != null) {
                return false;
            }
        } else if (!_resultClass.equals(other._resultClass)) {
            return false;
        }
        if (_methodName == null) {
            if (other._methodName != null) {
                return false;
            }
        } else if (!_methodName.equals(other._methodName)) {
            return false;
        }
        return true;
    }
}
