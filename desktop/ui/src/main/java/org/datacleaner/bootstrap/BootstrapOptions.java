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
package org.datacleaner.bootstrap;

import java.awt.Image;

import org.apache.metamodel.DataContext;
import org.datacleaner.cli.CliArguments;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.job.builder.AnalysisJobBuilder;

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
    boolean isCommandLineMode();

    /**
     * Gets the command line parameters (only invoked if
     * {@link #isCommandLineMode()} is true).
     *
     * @return an object representing the command line parameters.
     */
    CliArguments getCommandLineArguments();

    /**
     * Gets whether or not to run in "single datastore mode", which means that
     * DataCleaner will not display the datastore selection panel, which is
     * otherwise the initial view of the application.
     *
     * @return true if "single datastore mode" should be used, false if not.
     */
    boolean isSingleDatastoreMode();

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
    Datastore getSingleDatastore(DatastoreCatalog datastoreCatalog);

    /**
     * Method that is invoked after selecting the single datastore mode (see
     * {@link #isSingleDatastoreMode()}). This method enables the bootstrap to
     * be initialized with eg. certain columns selected and/or certain analyzers
     * added to the job.
     *
     * @param analysisJobBuilder
     *            the {@link AnalysisJobBuilder} object which is represented in
     *            the main window of DataCleaner.
     * @param dataContext
     *            the {@link DataContext} that is active in the window. This
     *            will provide convenient access to traversing the schema for
     *            finding relevant columns etc.
     * @param injectorBuilder
     *            an {@link InjectorBuilder} which provides access to injectable
     *            and shared resources.
     */
    void initializeSingleDatastoreJob(AnalysisJobBuilder analysisJobBuilder, DataContext dataContext,
            InjectorBuilder injectorBuilder);

    /**
     * Optionally fetches a welcome image for displaying when the application
     * window shows up.
     *
     * @return an image to show when the application start, or null if this
     *         feature should be disabled.
     */
    Image getWelcomeImage();

    /**
     * Gets an {@link ExitActionListener} implementation suitable for receiving
     * notifications that the user requests an exit from DataCleaner.
     *
     * @return an {@link ExitActionListener} implementation.
     */
    ExitActionListener getExitActionListener();
}
