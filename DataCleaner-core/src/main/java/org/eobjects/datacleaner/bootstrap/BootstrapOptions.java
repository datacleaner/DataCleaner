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
package org.eobjects.datacleaner.bootstrap;

import org.eobjects.analyzer.cli.CliArguments;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;

/**
 * Defines the bootstrapping options used to start up and initialize
 * DataCleaner.
 * 
 * @author Kasper Sørensen
 */
public interface BootstrapOptions {

	/**
	 * Gets whether or not to run in command line / batch mode.
	 * 
	 * @return true if command line mode should be used
	 */
	public boolean isCommandLineMode();

	/**
	 * Gets the command line parameters (only invoked if
	 * {@link #isCommandLineMode()} is true).
	 * 
	 * @return an object representing the command line parameters.
	 */
	public CliArguments getCommandLineArguments();

	/**
	 * Gets whether or not to run in "single datastore mode", which means that
	 * DataCleaner will not display the datastore selection panel, which is
	 * otherwise the initial view of the application.
	 * 
	 * @return true if "single datastore mode" should be used, false if not.
	 */
	public boolean isSingleDatastoreMode();

	/**
	 * Gets the single datastore to use, if {@link #isSingleDatastoreMode()} was
	 * true.
	 * 
	 * @param datastoreCatalog
	 *            the datastore catalog that was available at initialization
	 *            time. Note that the returned datastore does NOT nescesarily
	 *            have to originate from the datastore catalog.
	 * @return the datastore to show initially.
	 */
	public Datastore getSingleDatastore(DatastoreCatalog datastoreCatalog);

	/**
	 * Gets an {@link ExitActionListener} implementation suitable for receiving
	 * notifications that the user requests an exit from DataCleaner.
	 * 
	 * @return an {@link ExitActionListener} implementation.
	 */
	public ExitActionListener getExitActionListener();
}
