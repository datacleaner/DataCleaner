/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.jobwizard.quickanalysis;

import org.eobjects.datacleaner.monitor.wizard.job.DataCleanerJobWizard;
import org.eobjects.datacleaner.monitor.wizard.job.DataCleanerJobWizardSession;
import org.eobjects.datacleaner.monitor.wizard.job.JobWizardContext;
import org.springframework.stereotype.Component;

/**
 * A simple wizard which auto-generates a job with a default set of analyzers.
 */
@Component
public class QuickAnalysisWizard extends DataCleanerJobWizard {

    @Override
    public String getDisplayName() {
        return "Quick analysis";
    }

    @Override
    public int getExpectedPageCount() {
        return 5;
    }

    @Override
    protected DataCleanerJobWizardSession startInternal(JobWizardContext context) {
        return new QuickAnalysisWizardSession(context);
    }
}
