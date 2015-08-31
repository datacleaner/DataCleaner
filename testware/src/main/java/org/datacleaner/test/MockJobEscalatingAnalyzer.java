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

import javax.inject.Named;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.ColumnProperty;
import org.datacleaner.api.Configured;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.result.ListResult;

@Named("Mock job-escalating analyzer")
public class MockJobEscalatingAnalyzer implements Analyzer<ListResult<Integer>> {

    @Configured
    @ColumnProperty(escalateToMultipleJobs = true)
    InputColumn<?> column;

    @Override
    public void run(InputRow row, int distinctCount) {
    }

    @Override
    public ListResult<Integer> getResult() {
        List<Integer> list = new ArrayList<>();
        list.add(42);
        return new ListResult<>(list);
    }
}
