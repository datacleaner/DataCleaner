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
package org.eobjects.analyzer.result;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eobjects.analyzer.beans.api.ParameterizableMetric;
import org.eobjects.analyzer.data.InputColumn;

/**
 * Annotation used to mark a getter-method as a retrieval mechanism for
 * {@link AnalyzerResult} metrics.
 * 
 * A metric is an exposed number value which can be used to get summary
 * statistics of a particular result. This mechanism is used to allow
 * applications to compare results over time, by comparing their metrics.
 * 
 * This annotation should be used for methods that conform to these
 * restrictions:
 * <ul>
 * <li>The return type must be a {@link Number} or a subtype of {@link Number},
 * including primitive/unboxed types. Alternatively it is possible to return an
 * {@link ParameterizableMetric} instance which allow the consumer to get more
 * metadata about the metrics parameter values.</li>
 * <li>The method can optionally have an {@link InputColumn} parameter, if the
 * metric contains different values for different analyzed columns.</li>
 * <li>The method can optionally have a String parameter, if the metric
 * furthermore takes a user-defined query parameter.</li>
 * <li>Except for the above mentioned exceptions, the method must not have any
 * parameters.</li>
 * </ul>
 * 
 * @since AnalyzerBeans 0.16
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
@Inherited
public @interface Metric {

    /**
     * Defines the name of the metric. A metric name must be unique for any
     * particular {@link AnalyzerResult} class.
     * 
     * @return the name of the metric.
     */
    public String value();

    /**
     * Defines the display order of this metric, relative to other metrics.
     * 
     * @return the order (if any) of this metric when sorting metrics of a
     *         result type. A low order will place the metric before higher
     *         order metric.
     */
    public int order() default Integer.MAX_VALUE;

    /**
     * Defines if the string parameter of this metric (if any) supports IN and
     * NOT INT expressions.
     * 
     * @return
     */
    public boolean supportsInClause() default false;
}
