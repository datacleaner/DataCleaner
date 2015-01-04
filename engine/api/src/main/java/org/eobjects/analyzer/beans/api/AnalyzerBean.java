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
package org.eobjects.analyzer.beans.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes that are annotated with the @AnalyzerBean annotation are components
 * for data analysis. All @AnalyzerBean classes must implement {@link Analyzer}.
 * 
 * The life-cycle of an AnalyzerBean is as follows:
 * <ul>
 * <li>Instantiation. All AnalyzerBeans need to provide a no-args constructor.</li>
 * <li>All methods or fields with the @Configured annotation are
 * invoked/assigned to configure the AnalyzerBean before execution.</li>
 * <li>All methods or fields with the @Provided annotation are invoked/assigned</li>
 * <li>Any no-args methods with the @Initialize annotation are executed.</li>
 * <li>The {@link Analyzer#run(org.eobjects.analyzer.data.InputRow, int)} method is
 * called for each row in the analyzed DataSet.</li>
 * <li>All methods with the @Result annotation are invoked to retrieve the
 * result.</li>
 * <li>Any no-args methods with the @Close annotation are invoked if the
 * analyzer needs to release any resources.</li>
 * <li>If the analyzer implements the java.io.Closeable interface, the close()
 * method is also invoked.</li>
 * <li>The AnalyzerBean object is dereferenced and garbage collected</li>
 * </ul>
 * 
 * AnalyzerBeans are by default only invoked by a single thread at a time. This
 * behaviour can be overridden by using the @Concurrent annotation.
 * 
 * @see org.eobjects.analyzer.lifecycle.LifeCycleState
 * @see Concurrent
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface AnalyzerBean {

    /**
     * The display name of the AnalyzerBean. The display name should be humanly
     * readable and is presented to the user in User Interfaces.
     * 
     * @return the name of the AnalyzerBean
     */
    String value();
}
