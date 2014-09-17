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
package org.eobjects.datacleaner.monitor.wizard.job;

import org.eobjects.datacleaner.monitor.wizard.Wizard;

/**
 * A pluggable component (plug-in / extension) which provides a wizard in the
 * webapp for creating a job in the DC monitor repository.
 */
public interface JobWizard extends Wizard<JobWizardContext, JobWizardSession> {

    /**
     * Determines if this wizard produces jobs that consumes one or more
     * datastores in the DataCleaner repository. Most jobs do, but some, such as
     * command line scripts or third party applications do not, so they are
     * presented differently to the user.
     */
    public boolean isDatastoreConsumer();
}
