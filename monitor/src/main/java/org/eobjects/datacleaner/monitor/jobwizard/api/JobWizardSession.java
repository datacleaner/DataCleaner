/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.monitor.jobwizard.api;

import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;

public interface JobWizardSession {
    
    /**
     * Gets the next page of the wizard, or null if this wizard has finished
     * 
     * @param params
     * @return
     */
    public JobWizardPageController firstPageController();

    /**
     * Creates the final analysis job as prescribed by the wizard. This method
     * will be invoked when no more pages are available and the wizard has
     * ended.
     * 
     * @return
     */
    public AnalysisJobBuilder createJob();

    /**
     * Gets the expected amount of pages in this wizard.
     * 
     * @return the count of pages, or null if not known.
     */
    public Integer getPageCount();

}
