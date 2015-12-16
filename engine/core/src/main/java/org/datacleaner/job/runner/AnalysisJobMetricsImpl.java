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
package org.datacleaner.job.runner;

import org.apache.metamodel.schema.Table;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.util.SourceColumnFinder;

final class AnalysisJobMetricsImpl implements AnalysisJobMetrics {

    private final AnalysisJob _job;
    private final RowProcessingPublishers _publishers;

    public AnalysisJobMetricsImpl(AnalysisJob job, RowProcessingPublishers publishers) {
        _job = job;
        _publishers = publishers;
    }

    @Override
    public AnalysisJob getAnalysisJob() {
        return _job;
    }

    @Override
    public ComponentMetrics getComponentMetrics(ComponentJob componentJob) {
        final Table table = getRowProcessingTable(componentJob);
        final RowProcessingMetrics rowProcessingMetrics = getRowProcessingMetrics(table);
        return new ComponentMetricsImpl(rowProcessingMetrics, componentJob);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Table[] getRowProcessingTables() {
        return _publishers.getTables();
    }

    @Override
    public RowProcessingMetrics getRowProcessingMetrics(Table table) {
        final RowProcessingStream stream = _publishers.getStream(table);
        final RowProcessingPublisher publisher = _publishers.getRowProcessingPublisher(stream);
        if (publisher == null) {
            return null;
        }
        return publisher.getRowProcessingMetrics();
    }

    @Override
    public Table getRowProcessingTable(ComponentJob componentJob) {
        SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
        sourceColumnFinder.addSources(_job);
        Table[] tables = _publishers.getTables(sourceColumnFinder, componentJob);
        if (tables == null || tables.length == 0) {
            return null;
        }
        // this should always work for component, since they only pertain to a
        // single table
        return tables[0];
    }
}
