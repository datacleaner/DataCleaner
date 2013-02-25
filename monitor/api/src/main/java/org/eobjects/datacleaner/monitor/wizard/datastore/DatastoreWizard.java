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
package org.eobjects.datacleaner.monitor.wizard.datastore;

/**
 * A pluggable component (plug-in / extension) which provides a wizard in the
 * webapp for creating a datastore in the DC monitor configuration.
 */
public interface DatastoreWizard {

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
    public DatastoreWizardSession start(DatastoreWizardContext context);
}
