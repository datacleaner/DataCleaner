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
package org.eobjects.datacleaner.monitor.jobwizard.movedata;

import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizard;
import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizardContext;
import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizardSession;
import org.springframework.stereotype.Component;

@Component
public class MoveDataWizard implements JobWizard {

    @Override
    public String getDisplayName() {
        return "Move data";
    }

    @Override
    public JobWizardSession start(JobWizardContext context) {
        return new MoveDataWizardSession(context);
    }

    @Override
    public int getExpectedPageCount() {
        return 4;
    }
}
