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
package org.eobjects.datacleaner.monitor.configuration;

import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.repository.RepositoryFile;

/**
 * Represents the context of an analysis result stored in the repository.
 */
public interface ResultContext {

    /**
     * Reads and materializes the analysis result.
     * 
     * @return
     * @throws IllegalStateException
     *             if an exception occurs while reading or materializing the
     *             analysis result
     */
    public AnalysisResult getAnalysisResult() throws IllegalStateException;

    /**
     * Gets the file in the repository that holds this analysis result.
     * 
     * @return
     */
    public RepositoryFile getResultFile();

    /**
     * Gets the job that produced this result (if available).
     * 
     * @return the job that produced this result, or null if that job is not
     *         available.
     */
    public JobContext getJob();

}
