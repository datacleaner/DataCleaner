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
package org.eobjects.datacleaner.windows;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;

/**
 * This interface represents the main window in the DataCleaner GUI. An
 * {@link AnalysisJobBuilderWindow} has it's name because it's primary purpose
 * is to present a job that is being built. Behind the covers this job state is
 * respresented in the {@link AnalysisJobBuilder} class.
 * 
 * Besides job building, an {@link AnalysisJobBuilderWindow} also handles
 * datastore selection and menus in general.
 * 
 * @author Kasper Sørensen
 */
public interface AnalysisJobBuilderWindow extends DCWindow {

	/**
	 * Gets whether or not the datastore has been set in this window (ie. if the
	 * tree is showing a datastore).
	 * 
	 * @return true if a datastore is set.
	 */
	public boolean isDatastoreSet();

	/**
	 * Initializes the window to use a particular datastore in the schema tree.
	 * 
	 * @param datastore
	 */
	public void setDatastore(final Datastore datastore);

	/**
	 * Initializes the window to use a particular datastore in the schema tree.
	 * 
	 * @param datastore
	 * @param expandTree
	 *            true if the datastore tree should be initially expanded.
	 */
	public void setDatastore(final Datastore datastore, boolean expandTree);

	/**
	 * Sets the job filename of the window (will be visible in the title and
	 * more).
	 * 
	 * @param jobFilename
	 */
	public void setJobFilename(String jobFilename);

	/**
	 * Gets the current job filename
	 * 
	 * @return
	 */
	public String getJobFilename();

	/**
	 * Sets whether or not datastore selection should be enabled (default is
	 * true). If disabled, only a single datastore will be usable within this
	 * window.
	 * 
	 * @param datastoreSelectionEnabled
	 */
	public void setDatastoreSelectionEnabled(boolean datastoreSelectionEnabled);

	/**
	 * Gets whether datastore selection is enabled.
	 * 
	 * @see #setDatastoreSelectionEnabled(boolean)
	 * @return a boolean indicating whether or not datastore selection is
	 *         enabled.
	 */
	public boolean isDatastoreSelectionEnabled();

	/**
	 * Applies property values for all job components visible in the window.
	 */
	public void applyPropertyValues();

	/**
	 * Gets the status text of the status label. Useful if something goes wrong
	 * - this status label will typically have a humanly readable explanation.
	 * 
	 * @return
	 */
	public String getStatusLabelText();

}
