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
package org.datacleaner.api;

import java.util.Collection;

/**
 * Represents a parameterizable metric definition. With this instance you allow
 * metrics to provide (potentially dynamic) metadata about the metric to hint
 * how it should be properly parameterized.
 * 
 * Use the {@link #getValue(String)} with an actual parameter to retrieve the
 * metric value.
 * 
 * @see Metric
 * 
 * @since AnalyzerBeans 0.16
 */
public interface ParameterizableMetric {

    public Collection<String> getParameterSuggestions();

    public Number getValue(String parameter);
}
