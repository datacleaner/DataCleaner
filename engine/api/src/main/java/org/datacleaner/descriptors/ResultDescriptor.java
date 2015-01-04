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
import java.util.Set;

import org.datacleaner.result.AnalyzerResult;
import org.datacleaner.result.AnalyzerResultReducer;

/**
 * Descriptor of a {@link AnalyzerResult} object.
 */
public interface ResultDescriptor extends Serializable {

    /**
     * Gets the result class of this component.
     * 
     * @return the result class of this component.
     */
    public Class<? extends AnalyzerResult> getResultClass();

    /**
     * Gets a result metric by name
     * 
     * @param name
     *            the name of the result metric
     * @return the result metric with the given name, or null if no such metric
     *         exist.
     */
    public MetricDescriptor getResultMetric(String name);

    /**
     * Gets the result metrics of this component's result
     * 
     * @return the result metrics of this component's result
     */
    public Set<MetricDescriptor> getResultMetrics();

    /**
     * Gets the {@link AnalyzerResultReducer} class of this component, if
     * available.
     * 
     * @return a reducer class, or null if none is available
     */
    public Class<? extends AnalyzerResultReducer<?>> getResultReducerClass();

}
