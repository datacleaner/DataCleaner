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

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.SplashScreen;
import java.io.Closeable;
import java.io.File;
import java.io.PrintWriter;

import org.apache.http.client.HttpClient;
import org.eobjects.analyzer.cli.CliArguments;
import org.eobjects.analyzer.cli.CliRunner;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.Main;
import org.eobjects.datacleaner.extensionswap.ExtensionSwapClient;
import org.eobjects.datacleaner.extensionswap.ExtensionSwapInstallationHttpContainer;
import org.eobjects.datacleaner.guice.DCModule;
import org.eobjects.datacleaner.regexswap.RegexSwapUserPreferencesHandler;
import org.eobjects.datacleaner.user.DataCleanerHome;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.DCUncaughtExceptionHandler;
import org.eobjects.datacleaner.util.LookAndFeelManager;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;
import org.eobjects.datacleaner.windows.WelcomeDialog;
import org.eobjects.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Bootstraps an instance of DataCleaner into a running state. The initial state
 * of the application will be dependent on specified options (or defaults).
 * 
 * @author Kasper Sørensen
 */
public final class Bootstrap {

	private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

	private final BootstrapOptions _options;

	public Bootstrap(BootstrapOptions options) {
		_options = options;
	}

	public void run() {
		logger.info("Welcome to DataCleaner {}", Main.VERSION);

		// determine whether to run in command line interface mode
		final boolean cliMode = _options.isCommandLineMode();

		logger.info("CLI mode={}, use -usage to view usage options", cliMode);

		if (cliMode) {

			if (!GraphicsEnvironment.isHeadless()) {
				// hide splash screen
				SplashScreen splashScreen = SplashScreen.getSplashScreen();
				if (splashScreen != null) {
					splashScreen.close();
				}
			}

			final CliArguments arguments = _options.getCommandLineArguments();

			if (arguments.isUsageMode()) {
				final PrintWriter out = new PrintWriter(System.out);
				CliArguments.printUsage(out);

				exitCommandLine(null, 1);
				return;
			}
		}

		final File dataCleanerHome = DataCleanerHome.get();

		final Injector injector = Guice.createInjector(new DCModule(dataCleanerHome));

		// configuration loading can be multithreaded, so begin early
		final AnalyzerBeansConfiguration configuration = injector.getInstance(AnalyzerBeansConfiguration.class);

		if (!cliMode) {
			// set up error handling that displays an error dialog
			final DCUncaughtExceptionHandler exceptionHandler = new DCUncaughtExceptionHandler();
			Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);

			// init the look and feel
			LookAndFeelManager.getInstance().init();
		}

		// log usage
		final UsageLogger usageLogger = injector.getInstance(UsageLogger.class);
		usageLogger.logApplicationStartup();

		if (cliMode) {

			final PrintWriter out = new PrintWriter(System.out);
			// run in CLI mode

			int exitCode = 0;
			try {
				final CliArguments arguments = _options.getCommandLineArguments();
				final CliRunner runner = new CliRunner(arguments, out);
				runner.run(configuration);
			} catch (Throwable e) {
				logger.error("Error occurred while running DataCleaner command line mode", e);
				exitCode = 1;
			} finally {
				out.flush();
				exitCommandLine(configuration, exitCode);
			}
			return;
		} else {
			// run in GUI mode
			final AnalysisJobBuilderWindow analysisJobBuilderWindow = injector.getInstance(AnalysisJobBuilderWindow.class);

			final Datastore singleDatastore;
			if (_options.isSingleDatastoreMode()) {
				DatastoreCatalog datastoreCatalog = configuration.getDatastoreCatalog();
				singleDatastore = _options.getSingleDatastore(datastoreCatalog);
				if (singleDatastore == null) {
					logger.info("Single datastore mode was enabled, but datastore was null!");
				} else {
					logger.info("Initializing single datastore mode with {}", singleDatastore);
				}
				analysisJobBuilderWindow.setDatastoreSelectionEnabled(false);
				analysisJobBuilderWindow.setDatastore(singleDatastore, true);
			} else {
				singleDatastore = null;
			}

			// show the window
			analysisJobBuilderWindow.open();

			if (singleDatastore != null) {
				// this part has to be done after displaying the window (a lot
				// of initialization goes on there)
				AnalysisJobBuilder analysisJobBuilder = injector.getInstance(AnalysisJobBuilder.class);
				_options.initializeSingleDatastoreJob(analysisJobBuilder, singleDatastore.openConnection().getDataContext());

				Image welcomeImage = _options.getWelcomeImage();
				if (welcomeImage != null) {
					WelcomeDialog welcomeDialog = new WelcomeDialog(analysisJobBuilderWindow, welcomeImage);
					welcomeDialog.setVisible(true);
				}
			}

			final UserPreferences userPreferences = injector.getInstance(UserPreferences.class);
			final WindowContext windowContext = injector.getInstance(WindowContext.class);

			final HttpClient httpClient = injector.getInstance(HttpClient.class);

			// set up HTTP service for ExtensionSwap installation
			loadExtensionSwapService(userPreferences, windowContext, configuration, httpClient, usageLogger);

			// load regex swap regexes if logged in
			final RegexSwapUserPreferencesHandler regexSwapHandler = new RegexSwapUserPreferencesHandler(
					(MutableReferenceDataCatalog) configuration.getReferenceDataCatalog(), httpClient, usageLogger);
			userPreferences.addLoginChangeListener(regexSwapHandler);

			final ExitActionListener exitActionListener = _options.getExitActionListener();
			if (exitActionListener != null) {
				windowContext.addExitActionListener(exitActionListener);
			}
		}
	}

	private void exitCommandLine(AnalyzerBeansConfiguration configuration, int statusCode) {
		if (configuration != null) {
			logger.debug("Shutting down task runner");
			configuration.getTaskRunner().shutdown();
		}
		ExitActionListener exitActionListener = _options.getExitActionListener();
		if (exitActionListener != null) {
			exitActionListener.exit(statusCode);
		}
	}

	private void loadExtensionSwapService(UserPreferences userPreferences, WindowContext windowContext,
			AnalyzerBeansConfiguration configuration, HttpClient httpClient, UsageLogger usageLogger) {
		String websiteHostname = userPreferences.getAdditionalProperties().get("extensionswap.hostname");
		if (StringUtils.isNullOrEmpty(websiteHostname)) {
			websiteHostname = System.getProperty("extensionswap.hostname");
		}

		final ExtensionSwapClient extensionSwapClient;
		if (StringUtils.isNullOrEmpty(websiteHostname)) {
			logger.info("Using default ExtensionSwap website hostname");
			extensionSwapClient = new ExtensionSwapClient(httpClient, windowContext, userPreferences, configuration);
		} else {
			logger.info("Using custom ExtensionSwap website hostname: {}", websiteHostname);
			extensionSwapClient = new ExtensionSwapClient(httpClient, websiteHostname, windowContext, userPreferences,
					configuration);
		}
		ExtensionSwapInstallationHttpContainer container = new ExtensionSwapInstallationHttpContainer(extensionSwapClient,
				userPreferences, usageLogger);

		final Closeable closeableConnection = container.initialize();
		if (closeableConnection != null) {
			windowContext.addExitActionListener(new ExitActionListener() {
				@Override
				public void exit(int statusCode) {
					FileHelper.safeClose(closeableConnection);
				}
			});
		}
	}
}
