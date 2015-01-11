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
package org.datacleaner.beans.api;

import javax.inject.Named;

import org.datacleaner.data.InputRow;
import org.datacleaner.result.AnalyzerResult;
import org.datacleaner.result.HasAnalyzerResult;

/**
 * Ananalyzer is a component that recieves rows of data and produces some sort
 * of result from it.
 * 
 * The run(InputRow, int) method will be invoked on the analyzer for each row in
 * a configured datastore. To retrieve the values from the row InputColumn
 * instances must be used as qualifiers. These InputColumns needs to be injected
 * (either a single instance or an array) using the @Configured annotation. If
 * no @Configured InputColumns are found in the class, the analyzer will not be
 * able to execute.
 * 
 * Use of the {@link Named} annotation is required for the {@link Analyzer} to
 * by automatically discovered.
 * 
 * @see Named
 * 
 * @param <R>
 *            the result type of this analyzer.
 */
public interface Analyzer<R extends AnalyzerResult> extends HasAnalyzerResult<R> {

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
