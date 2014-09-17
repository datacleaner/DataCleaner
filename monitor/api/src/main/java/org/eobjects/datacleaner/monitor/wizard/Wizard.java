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
 * An abstract wizard definition. Can be extended by sub-interfaces to fit
 * specific wizard requirements.
 * 
 * A wizard are generally speaking pluggable components (plug-ins) that are
 * added to the user interface for customization purposes.
 */
public interface Wizard<C extends WizardContext, S extends WizardSession> {

	/**
	 * Determines if a wizard is applicable to the initial settings, provided in
	 * the {@link WizardContext}. This method allows a wizard to be applicable
	 * only to e.g. certain circumstances.
	 * 
	 * @param context
	 * @return
	 */
	public boolean isApplicableTo(C context);

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
	 * Starts a wizard session.
	 * 
	 * @param context
	 * @return
	 */
	public S start(C context);
}
