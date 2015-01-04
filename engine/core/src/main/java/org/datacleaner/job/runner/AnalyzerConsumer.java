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

import org.datacleaner.beans.api.Analyzer;
import org.datacleaner.beans.api.Concurrent;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.util.SourceColumnFinder;

final class AnalyzerConsumer extends AbstractRowProcessingConsumer implements RowProcessingConsumer {

    private final AnalyzerJob _analyzerJob;
    private final Analyzer<?> _analyzer;
    private final InputColumn<?>[] _inputColumns;
    private final boolean _concurrent;

    public AnalyzerConsumer(Analyzer<?> analyzer, AnalyzerJob analyzerJob, InputColumn<?>[] inputColumns,
            SourceColumnFinder sourceColumnFinder) {
        super(null, null, analyzerJob, analyzerJob, sourceColumnFinder);
        _analyzer = analyzer;
        _analyzerJob = analyzerJob;
        _inputColumns = inputColumns;
        _concurrent = determineConcurrent();
    }

    public AnalyzerConsumer(Analyzer<?> analyzer, AnalyzerJob analyzerJob, InputColumn<?>[] inputColumns,
            RowProcessingPublishers publishers) {
        super(publishers, analyzerJob, analyzerJob);
        _analyzer = analyzer;
        _analyzerJob = analyzerJob;
        _inputColumns = inputColumns;
        _concurrent = determineConcurrent();
    }

    private boolean determineConcurrent() {
        Concurrent concurrent = _analyzerJob.getDescriptor().getAnnotation(Concurrent.class);
        if (concurrent == null) {
            // analyzers are by default not concurrent
            return false;
        }
        return concurrent.value();
    }

    @Override
    public Analyzer<?> getComponent() {
        return _analyzer;
    }

    @Override
    public boolean isConcurrent() {
        return _concurrent;
    }

    @Override
    public InputColumn<?>[] getRequiredInput() {
        return _inputColumns;
    }

    @Override
    public void consumeInternal(InputRow row, int distinctCount, FilterOutcomes outcomes, RowProcessingChain chain) {
        _analyzer.run(row, distinctCount);
        chain.processNext(row, distinctCount, outcomes);
    }

    @Override
    public AnalyzerJob getComponentJob() {
        return _analyzerJob;
    }

    @Override
    public String toString() {
        return "AnalyzerConsumer[" + _analyzer + "]";
    }
}
