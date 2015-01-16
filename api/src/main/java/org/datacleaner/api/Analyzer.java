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

import javax.inject.Named;

/**
 * Ananalyzer is a {@link Component} that recieves rows of data and produces an
 * {@link AnalyzerResult} from it.
 * 
 * See {@link Component} for general information about all components. Like all
 * components, {@link Analyzer} require a {@link Named} annotation in order to
 * be discovered.
 * 
 * The {@link #run(InputRow, int)} method will be invoked on the
 * {@link Analyzer} for each row in a data stream. The framework may choose to
 * optimize the number of operations in case multiple exactly identical rows
 * occur. In such case the {@link #run(InputRow, int)} may only be invoked once
 * but with a greater-than-1 second argument.
 * 
 * Use of the {@link Named} annotation is required for the {@link Analyzer} to
 * by automatically discovered.
 * 
 * See {@link Component} for more details.
 * 
 * @param <R>
 *            the {@link AnalyzerResult} type of this analyzer.
 * 
 * @since 4.0
 */
public interface Analyzer<R extends AnalyzerResult> extends Component, HasAnalyzerResult<R> {

    /**
     * Executes the analyzer for a single row.
     * 
     * @param row
     *            the row to analyze
     * @param distinctCount
     *            the distinct count of the row.
     */
    public void run(InputRow row, int distinctCount);

    /**
     * Gets the result of the analysis that the analyzer has conducted.
     * 
     * @return an analyzer result object.
     */
    @Override
    public R getResult();
}
