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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

import org.datacleaner.data.InputColumn;
import org.datacleaner.result.AnalyzerResult;
import org.apache.metamodel.util.HasName;

/**
 * Descriptor class for {@link AnalyzerResult} metrics.
 * 
 * @since AnalyzerBeans 0.16
 */
public interface MetricDescriptor extends Serializable, HasName, Comparable<MetricDescriptor> {

    /**
     * Gets the metric value for a particular {@link AnalyzerResult}.
     * 
     * @param result
     * @param metricParameters
     * @return the metric value of the particular {@link AnalyzerResult}.
     */
    public Number getValue(AnalyzerResult result, MetricParameters metricParameters);

    /**
     * Gets a collection of suggested metric parameters. How these suggestions
     * are created depends upon the AnalyzerResult's data and capabilities, so
     * therefore the quality or extensiveness of the suggestions is not
     * guaranteed and should be considered a sample more than a direction.
     * 
     * @return an interable of suggested metric parameters.
     */
    public Collection<String> getMetricParameterSuggestions(AnalyzerResult result);

    /**
     * Gets the optional description of the metric
     * 
     * @return a humanly readable description of the metric
     */
    public String getDescription();

    /**
     * Gets any additional annotations for the metric.
     * 
     * @return additional annotations for the metric.
     */
    public Set<Annotation> getAnnotations();

    /**
     * Gets a particular annotation for the metric, if available.
     * 
     * @param annotationClass
     * @return a particular annotation for the metric, if available.
     */
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass);

    /**
     * Determines whether this metric is parameterized with an
     * {@link InputColumn}.
     * 
     * @return true if this metric is parameterized with an {@link InputColumn},
     *         or false if not.
     */
    public boolean isParameterizedByInputColumn();

    /**
     * Determines whether this metric is parameterized with an {@link String}.
     * 
     * @return true if this metric is parameterized with an {@link String}, or
     *         false if not.
     */
    public boolean isParameterizedByString();
}
