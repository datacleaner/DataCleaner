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

import java.awt.SplashScreen;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eobjects.analyzer.cli.CliArguments;
import org.eobjects.analyzer.cli.CliRunner;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.Main;
import org.eobjects.datacleaner.extensionswap.ExtensionSwapClient;
import org.eobjects.datacleaner.extensionswap.ExtensionSwapInstallationHttpContainer;
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
			// hide splash screen
			SplashScreen.getSplashScreen().close();

			final CliArguments arguments = _options.getCommandLineArguments();

			if (arguments.isUsageMode()) {
				final PrintWriter out = new PrintWriter(System.out);
				CliArguments.printUsage(out);

				_options.getExitActionListener().exit(1);
				return;
			}
		}

		// loads static configuration in a separate thread
		final Future<AnalyzerBeansConfiguration> configurationFuture = loadConfiguration();

		if (!cliMode) {
			// set up error handling that displays an error dialog
			final DCUncaughtExceptionHandler exceptionHandler = new DCUncaughtExceptionHandler();
			Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);

			// init the look and feel
			LookAndFeelManager.getInstance().init();
		}

		// log usage
		UsageLogger.getInstance().logApplicationStartup();

		try {
			if (cliMode) {

				final PrintWriter out = new PrintWriter(System.out);
				// run in CLI mode

				CliArguments arguments = _options.getCommandLineArguments();

				final CliRunner runner = new CliRunner(arguments, out);
				runner.run(configurationFuture.get());
				out.flush();

				_options.getExitActionListener().exit(0);
			} else {
				// run in GUI mode

				// loads dynamic user preferences
				final UserPreferences userPreferences = UserPreferences.getInstance();

				final WindowManager windowManager = new DCWindowContext(_options.getExitActionListener());

				final AnalyzerBeansConfiguration configuration = configurationFuture.get();

				new AnalysisJobBuilderWindow(configuration, windowManager).setVisible(true);

				// set up HTTP service for ExtensionSwap installation
				loadExtensionSwapService(userPreferences, windowManager);

				// load regex swap regexes if logged in
				final RegexSwapUserPreferencesHandler regexSwapHandler = new RegexSwapUserPreferencesHandler(
						(MutableReferenceDataCatalog) configuration.getReferenceDataCatalog());
				userPreferences.addLoginChangeListener(regexSwapHandler);
			}
		} catch (InterruptedException e) {
			logger.error("Configuration loading was interrupted!", e);
			_options.getExitActionListener().exit(2);
		} catch (ExecutionException e) {
			logger.error("Configuration loading threw unexpected exception!", e.getCause());
			_options.getExitActionListener().exit(3);
		}
	}

	private void loadExtensionSwapService(UserPreferences userPreferences, WindowManager windowManager) {
		String websiteHostname = userPreferences.getAdditionalProperties().get("extensionswap.hostname");
		if (StringUtils.isNullOrEmpty(websiteHostname)) {
			websiteHostname = System.getProperty("extensionswap.hostname");
		}

		final ExtensionSwapClient extensionSwapClient;
		if (StringUtils.isNullOrEmpty(websiteHostname)) {
			logger.info("Using default ExtensionSwap website hostname");
			extensionSwapClient = new ExtensionSwapClient(windowManager);
		} else {
			logger.info("Using custom ExtensionSwap website hostname: {}", websiteHostname);
			extensionSwapClient = new ExtensionSwapClient(websiteHostname, windowManager);
		}
		ExtensionSwapInstallationHttpContainer.initialize(extensionSwapClient);
	}

	private Future<AnalyzerBeansConfiguration> loadConfiguration() {
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		logger.info("Loading configuration reader thread");
		Future<AnalyzerBeansConfiguration> future = executor.submit(new Callable<AnalyzerBeansConfiguration>() {
			@Override
			public AnalyzerBeansConfiguration call() throws Exception {
				try {
					AnalyzerBeansConfiguration configuration = DCConfiguration.get();
					return configuration;
				} finally {
					logger.info("Shutting down configuration reader thread");
					executor.shutdown();
				}
			}
		});
		return future;
	}
}
