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
package org.eobjects.datacleaner.monitor.wizard;

/**
 * Represents the session of going through a wizard
 */
public interface WizardSession {

    /**
     * Gets the first page of the wizard
     * 
     * @param params
     * @return
     */
    public WizardPageController firstPageController();

    /**
     * Gets the context of the wizard.s
     * 
     * @return
     */
    public WizardContext getWizardContext();

    /**
     * Gets the expected amount of pages in this wizard.
     * 
     * @return the count of pages, or null if not known.
     */
    public Integer getPageCount();

    /**
     * Method invoked by the wizard framework when the wizard has finished.
     * Wizard implementations should use this method to persist the work created
     * by the user.
     * 
     * @return the name of the resulting job/datastore (or whatever other entity
     *         was built using the wizard), or null if no applicable name can be
     *         applied.
     */
    public String finished();
}
