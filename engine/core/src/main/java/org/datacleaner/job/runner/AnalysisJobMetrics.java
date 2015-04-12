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

/**
 * Provides useful metrics and information about an analysis job.
 */
public interface AnalysisJobMetrics {

	/**
	 * Gets the analysis job being executed.
	 * 
	 * @return the analysis job being executed.
	 */
	public AnalysisJob getAnalysisJob();

	public ComponentMetrics getComponentMetrics(ComponentJob componentJob);

	public Table getRowProcessingTable(ComponentJob componentJob);

	public Table[] getRowProcessingTables();

	public RowProcessingMetrics getRowProcessingMetrics(Table table);
}
