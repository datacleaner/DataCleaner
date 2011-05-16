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
package org.eobjects.datacleaner;

import java.awt.SplashScreen;
import java.io.PrintWriter;

import javax.swing.UnsupportedLookAndFeelException;

import org.eobjects.analyzer.cli.CliArguments;
import org.eobjects.analyzer.cli.CliRunner;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.datacleaner.regexswap.RegexSwapUserPreferencesHandler;
import org.eobjects.datacleaner.user.DCConfiguration;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.DCUncaughtExceptionHandler;
import org.eobjects.datacleaner.util.LookAndFeelManager;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static final String VERSION = "2.1.1";

	public static void main(String[] args) throws UnsupportedLookAndFeelException {
		logger.info("Welcome to DataCleaner {}", VERSION);

		// determine whether to run in command line interface mode
		final CliArguments arguments = CliArguments.parse(args);
		final boolean cliMode = arguments.isSet();

		logger.info("CLI mode={}, use -usage to view usage options", cliMode);

		if (cliMode) {
			// hide splash screen
			SplashScreen.getSplashScreen().close();

			if (arguments.isUsageMode()) {
				final PrintWriter out = new PrintWriter(System.out);
				CliArguments.printUsage(out);

				System.exit(1);
			}
		} else {
			// set up error handling that displays an error dialog
			final DCUncaughtExceptionHandler uncaughtExceptionHandler = new DCUncaughtExceptionHandler();
			Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);

			// init the look and feel
			LookAndFeelManager.getInstance().init();
		}

		// loads static configuration
		final AnalyzerBeansConfiguration configuration = DCConfiguration.get();

		// log usage
		UsageLogger.getInstance().logApplicationStartup();

		if (cliMode) {

			final PrintWriter out = new PrintWriter(System.out);
			// run in CLI mode
			final CliRunner runner = new CliRunner(arguments, out);
			runner.run(configuration);
			out.flush();

			System.exit(0);
		} else {
			// run in GUI mode

			// loads dynamic user preferences
			final UserPreferences userPreferences = UserPreferences.getInstance();

			new AnalysisJobBuilderWindow(configuration).setVisible(true);

			// load regex swap regexes if logged in
			final RegexSwapUserPreferencesHandler regexSwapHandler = new RegexSwapUserPreferencesHandler(
					(MutableReferenceDataCatalog) configuration.getReferenceDataCatalog());
			userPreferences.addLoginChangeListener(regexSwapHandler);
		}
	}
}
