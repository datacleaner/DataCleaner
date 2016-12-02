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

import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Table;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.result.AnalysisResult;

/**
 * Provides useful metrics about a row processing flow and it's execution.
 */
public interface RowProcessingMetrics {

    /**
     * Gets the metrics for the complete analysis job.
     *
     * @return the metrics for the complete analysis job.
     */
    AnalysisJobMetrics getAnalysisJobMetrics();

    /**
     * Gets references to the {@link AnalyzerJob}s that are a part of this row
     * processing flow.
     *
     * @return an array of {@link AnalyzerJob}s.
     */
    AnalyzerJob[] getAnalyzerJobs();

    /**
     * Gets references to the {@link ComponentJob}s that are expected to produce
     * an {@link AnalysisResult}
     *
     * @return
     */
    ComponentJob[] getResultProducers();

    /**
     * Gets the query that will be executed to retrieve the table records.
     *
     * @return the query that will be executed to retrieve the table records.
     */
    Query getQuery();

    /**
     * Gets the table that is being processed (eg. acts as a source table) in
     * this row processing flow.
     *
     * @return the Table that acts as a source to the row processor.
     */
    Table getTable();

    /**
     * Gets the {@link RowProcessingStream} that is being processed
     *
     * @return
     */
    RowProcessingStream getStream();

    /**
     * Gets the expected amount of rows to process. Use this method only if
     * necessary, since it may query the database to find the record count.
     *
     * @return an expected count of records to process, or -1 if the expected
     *         count could not be determined.
     */
    int getExpectedRows();

}
