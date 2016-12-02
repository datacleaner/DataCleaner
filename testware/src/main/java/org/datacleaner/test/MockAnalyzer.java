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
package org.datacleaner.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Named;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Concurrent;
import org.datacleaner.api.Configured;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.result.ListResult;

@Named("Mock analyzer")
@Concurrent(true)
public class MockAnalyzer implements Analyzer<ListResult<InputRow>> {

    @Configured
    InputColumn<?>[] cols;

    private BlockingQueue<InputRow> rows = new LinkedBlockingQueue<>();

    @Override
    public void run(final InputRow row, final int distinctCount) {
        rows.add(row);
    }

    @Override
    public ListResult<InputRow> getResult() {
        final List<InputRow> rowsList = new ArrayList<>(rows.size());
        rows.drainTo(rowsList);
        return new ListResult<>(rowsList);
    }

    public InputColumn<?>[] getCols() {
        return cols;
    }

    public void setCols(final InputColumn<?>[] cols) {
        this.cols = cols;
    }
}
