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
import org.datacleaner.job.OutputDataStreamJob;

/**
 * Represents a stream of data that is being processed by DataCleaner.
 */
public class RowProcessingStream {

    private final AnalysisJob _analysisJob;
    private final Table _table;

    public static RowProcessingStream ofOutputDataStream(OutputDataStreamJob outputDataStreamJob) {
        return new RowProcessingStream(outputDataStreamJob.getJob(), outputDataStreamJob.getOutputDataStream()
                .getTable());
    }

    public static RowProcessingStream ofSourceTable(AnalysisJob analysisJob, Table table) {
        return new RowProcessingStream(analysisJob, table);
    }

    private RowProcessingStream(AnalysisJob analysisJob, Table table) {
        _analysisJob = analysisJob;
        _table = table;
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_analysisJob == null) ? 0 : _analysisJob.hashCode());
        result = prime * result + ((_table == null) ? 0 : _table.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RowProcessingStream other = (RowProcessingStream) obj;
        if (_analysisJob == null) {
            if (other._analysisJob != null)
                return false;
        } else if (!_analysisJob.equals(other._analysisJob))
            return false;
        if (_table == null) {
            if (other._table != null)
                return false;
        } else if (!_table.equals(other._table))
            return false;
        return true;
    }
}
