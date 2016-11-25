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

import java.util.Objects;

import org.apache.metamodel.schema.Table;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.OutputDataStreamJob;

/**
 * Represents a stream of data that is being processed by DataCleaner.
 */
public class RowProcessingStream {

    private final AnalysisJob _analysisJob;
    private final Table _table;
    private final boolean _sourceTable;

    private RowProcessingStream(final AnalysisJob analysisJob, final Table table, final boolean sourceTable) {
        _analysisJob = analysisJob;
        _table = table;
        _sourceTable = sourceTable;
    }

    public static RowProcessingStream ofOutputDataStream(final OutputDataStreamJob outputDataStreamJob) {
        return new RowProcessingStream(outputDataStreamJob.getJob(),
                outputDataStreamJob.getOutputDataStream().getTable(), false);
    }

    public static RowProcessingStream ofSourceTable(final AnalysisJob analysisJob, final Table table) {
        return new RowProcessingStream(analysisJob, table, true);
    }

    /**
     * Determines if this RowProcessingStream is acting on a source
     * {@link Table} or not (in which case it would normally represent an
     * {@link OutputDataStream})
     *
     * @return
     */
    public boolean isSourceTable() {
        return _sourceTable;
    }

    /**
     * Gets the {@link AnalysisJob} that this stream will be executing.
     *
     * @return
     */
    public AnalysisJob getAnalysisJob() {
        return _analysisJob;
    }

    /**
     * Gets the logical or physical {@link Table} that this stream's records
     * matches.
     *
     * @return
     */
    public Table getTable() {
        return _table;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_analysisJob, _table);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof RowProcessingStream)) {
            return false;
        }
        final RowProcessingStream other = (RowProcessingStream) obj;
        return Objects.equals(_analysisJob, other._analysisJob) && Objects.equals(_table, other._table);
    }

    @Override
    public String toString() {
        return getTable().getName();
    }
}
