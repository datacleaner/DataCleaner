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
package org.datacleaner.cli;

import java.util.HashMap;
import java.util.Map;

import org.apache.metamodel.schema.Table;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.runner.AnalysisListenerAdaptor;
import org.datacleaner.job.runner.RowProcessingMetrics;
import org.datacleaner.util.ProgressCounter;

final class CliProgressAnalysisListener extends AnalysisListenerAdaptor {

    private final Map<Table, ProgressCounter> rowCounts = new HashMap<Table, ProgressCounter>();

    @Override
    public void rowProcessingBegin(AnalysisJob job, RowProcessingMetrics metrics) {
        final Table table = metrics.getTable();
        rowCounts.put(table, new ProgressCounter());
    }

    @Override
    public void rowProcessingProgress(AnalysisJob job, RowProcessingMetrics metrics, int currentRow) {
        final Table table = metrics.getTable();
        final ProgressCounter progressCounter = rowCounts.get(table);
        if (progressCounter != null) {
            final boolean significant = progressCounter.setIfSignificantToUser(currentRow);
            if (significant) {
                System.out.println(currentRow + " rows processed from table: " + table.getName());
            }
        }
    }
}
