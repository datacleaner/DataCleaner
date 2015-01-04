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
package org.datacleaner.job;

import java.util.List;

import org.datacleaner.connection.Datastore;
import org.datacleaner.data.InputColumn;
import org.apache.metamodel.schema.Column;

/**
 * Defines a job to be executed by AnalyzerBeans.
 * 
 * A {@link AnalysisJob} contains a set of components.
 * 
 * A {@link AnalysisJob} references a source {@link Datastore} and some
 * {@link Column}s (represented via {@link InputColumn}s) of this datastore.
 * 
 * Building jobs is usually done using the
 * {@link org.datacleaner.job.builder.AnalysisJobBuilder} class.
 * 
 * Executing jobs is usually done using the
 * {@link org.datacleaner.job.runner.AnalysisRunner} interface.
 */
public interface AnalysisJob {

    /**
     * Gets the {@link AnalysisJobMetadata} which add additional descriptions
     * and properties of the job.
     * 
     * @return
     */
    public AnalysisJobMetadata getMetadata();

    /**
     * Gets the {@link Datastore} that this job uses as it's source.
     * 
     * @return
     */
    public Datastore getDatastore();

    /**
     * Gets the source columns of the {@link Datastore} (see
     * {@link #getDatastore()}) referenced by this job.
     * 
     * @return
     */
    public List<InputColumn<?>> getSourceColumns();

    /**
     * Gets all {@link TransformerJob}s contained in this job.
     * 
     * @return
     */
    public List<TransformerJob> getTransformerJobs();

    /**
     * Gets all {@link FilterJob}s contained in this job.
     * 
     * @return
     */
    public List<FilterJob> getFilterJobs();

    /**
     * Gets all {@link AnalyzerJob}s contained in this job.
     * 
     * @return
     */
    public List<AnalyzerJob> getAnalyzerJobs();
}
