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
package org.eobjects.analyzer.descriptors;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.eobjects.analyzer.result.AnalyzerResult;

/**
 * A mock-implementation of the {@link MetricDescriptor} interface. Use this only for testing purposes
 * or in cases where you want to circumvent the actual framework!
 */
public class MockMetricDescriptor extends AbstractMetricDescriptor implements MetricDescriptor {

    private static final long serialVersionUID = 1L;

    private final Number _value;
    private String _name;

    public MockMetricDescriptor(Number value) {
        _value = value;
    }

    @Override
    public Number getValue(AnalyzerResult result, MetricParameters metricParameters) {
        return _value;
    }

    @Override
    public Collection<String> getMetricParameterSuggestions(AnalyzerResult result) {
        return Collections.emptyList();
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return Collections.emptySet();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return null;
    }

    @Override
    public boolean isParameterizedByInputColumn() {
        return false;
    }

    @Override
    public boolean isParameterizedByString() {
        return false;
    }

    @Override
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

}
