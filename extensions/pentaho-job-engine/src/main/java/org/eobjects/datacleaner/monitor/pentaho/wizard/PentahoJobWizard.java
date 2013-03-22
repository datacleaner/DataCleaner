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
package org.eobjects.datacleaner.monitor.pentaho.wizard;

import org.eobjects.datacleaner.monitor.wizard.job.JobWizard;
import org.eobjects.datacleaner.monitor.wizard.job.JobWizardContext;
import org.eobjects.datacleaner.monitor.wizard.job.JobWizardSession;
import org.springframework.stereotype.Component;

/**
 * {@link JobWizard} for Pentaho jobs running on a Carte server
 */
@Component
public class PentahoJobWizard implements JobWizard {

    @Override
    public boolean isApplicableTo(JobWizardContext context) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Pentaho Data Integration transformation";
    }

    @Override
    public int getExpectedPageCount() {
        return 2;
    }

    @Override
    public JobWizardSession start(JobWizardContext context) {
        return new PentahoJobWizardSession(context);
    }

    @Override
    public boolean isDatastoreConsumer() {
        return false;
    }

}
