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
package org.datacleaner.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Named;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Configured;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;

/**
 * A dummy analyzer used by the Preview Data button (
 * {@link PreviewTransformedDataActionListener}) to collect values from the
 * previewed records.
 *
 * Note that this analyzer is not annotated with {@link Named} - this enables it
 * to not be discovered and exposed to the users, but still to be used programmatically.
 */
public class PreviewTransformedDataAnalyzer implements Analyzer<PreviewTransformedDataAnalyzer>, AnalyzerResult {

    private static final long serialVersionUID = 1L;

    @Configured
    InputColumn<?>[] columns;

    private BlockingQueue<Object[]> rows = new LinkedBlockingQueue<>();

    @Override
    public void run(final InputRow row, final int distinctCount) {
        final List<Object> result = row.getValues(columns);
        rows.add(result.toArray(new Object[result.size()]));
    }

    public List<Object[]> getList() {
        return new ArrayList<>(rows);
    }

    public InputColumn<?>[] getColumns() {
        return columns;
    }

    @Override
    public PreviewTransformedDataAnalyzer getResult() {
        return this;
    }
}
