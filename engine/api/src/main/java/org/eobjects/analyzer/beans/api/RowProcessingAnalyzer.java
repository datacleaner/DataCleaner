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

import org.eobjects.analyzer.result.AnalyzerResult;

/**
 * A row processing analyzer is a component that recieves rows of data and
 * produces some sort of result from it.
 * 
 * The run(InputRow, int) method will be invoked on the analyzer for each row in
 * a configured datastore. To retrieve the values from the row InputColumn
 * instances must be used as qualifiers. These InputColumns needs to be injected
 * (either a single instance or an array) using the @Configured annotation. If
 * no @Configured InputColumns are found in the class, the analyzer will not be
 * able to execute.
 * 
 * Use of the @AnalyzerBean annotation is required for analyzers in order to be
 * automatically discovered.
 * 
 * @see AnalyzerBean
 * @see Configured
 * @see ExploringAnalyzer
 * 
 * @param <R>
 *            the result type returned by this analyzer
 * 
 * @deprecated use the {@link Analyzer} interface instead.
 */
@Deprecated
public interface RowProcessingAnalyzer<R extends AnalyzerResult> extends Analyzer<R> {

}
