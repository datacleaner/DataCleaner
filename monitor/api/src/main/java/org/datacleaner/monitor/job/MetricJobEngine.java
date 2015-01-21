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
package org.datacleaner.monitor.job;

import java.util.Collection;
import java.util.List;

import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Metric;
import org.datacleaner.api.ParameterizableMetric;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.monitor.configuration.ResultContext;
import org.datacleaner.monitor.shared.model.MetricIdentifier;

/**
 * Defines a {@link JobEngine} whose jobs and results expose metrics that can be
 * monitored in the timelines of DataCleaner monitor.
 * 
 * @param <T>
 *            the job context type
 */
public interface MetricJobEngine<T extends MetricJobContext> extends JobEngine<T> {

    /**
     * Gets/calculates metric values for a particular list of metrics
     * 
     * @param job
     * @param result
     * @param metricIdentifiers
     * @return
     */
    public MetricValues getMetricValues(MetricJobContext job, ResultContext result, List<MetricIdentifier> metricIdentifiers);

    /**
     * Gets suggestions for a string-parameterized metric. This method will only
     * be invoked if results of the jobs expose {@link ParameterizableMetric}
     * metric methods.
     * 
     * @see {@link Metric}
     * @see {@link ParameterizableMetric}
     * 
     * @param job
     * @param result
     * @param metricIdentifier
     * @return
     */
    public Collection<String> getMetricParameterSuggestions(MetricJobContext job, ResultContext result,
            MetricIdentifier metricIdentifier);

    /**
     * Gets the available column values for a column-parameterized metric. This
     * method will only be invoked if results of the jobs expose metric methods
     * which take {@link InputColumn} as a parameter.
     * 
     * @see {@link Metric}
     * 
     * @param job
     * @return
     */
    public Collection<InputColumn<?>> getMetricParameterColumns(MetricJobContext job, ComponentJob component);
}
