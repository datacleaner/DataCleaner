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
package org.eobjects.datacleaner.monitor.wizard.job;

/**
 * A pluggable component (plug-in / extension) which provides a wizard in the
 * webapp for creating a job in the DC monitor repository.
 */
public interface JobWizard {

    /**
     * Determines if this wizard produces jobs that consumes one or more
     * datastores in the DataCleaner repository. Most jobs do, but some, such as
     * command line scripts or third party applications do not, so they are
     * presented differently to the user.
     */
    public boolean isDatastoreConsumer();

    /**
     * Determines if a wizard is applicable to the initial settings, provided in
     * the {@link JobWizardContext}. This method allows a wizard to be
     * applicable only to e.g. certain types of datastores, tenants or other
     * circumstances.
     * 
     * @param context
     * @return
     */
    public boolean isApplicableTo(JobWizardContext context);

    /**
     * Gets the display name of this wizard - this name will guide the user as
     * to what kind of job he is creating.
     * 
     * @return
     */
    public String getDisplayName();

    /**
     * Gets an expected count of pages in this wizard. Since the amount of pages
     * can vary depending on different routes in a wizard, this number should
     * just represent the most "plain" scenario's number of pages.
     * 
     * @return
     */
    public int getExpectedPageCount();

    /**
     * Starts the wizard
     * 
     * @param context
     * @return
     */
    public JobWizardSession start(JobWizardContext context);
}
