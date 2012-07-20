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
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.SwingUtilities;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.DelegateFileObject;
import org.apache.commons.vfs2.provider.url.UrlFileName;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eobjects.analyzer.cli.CliArguments;
import org.eobjects.analyzer.cli.CliRunner;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.analyzer.util.VFSUtils;
import org.eobjects.datacleaner.Main;
import org.eobjects.datacleaner.actions.DownloadFilesActionListener;
import org.eobjects.datacleaner.actions.OpenAnalysisJobActionListener;
import org.eobjects.datacleaner.extensionswap.ExtensionSwapClient;
import org.eobjects.datacleaner.extensionswap.ExtensionSwapInstallationHttpContainer;
import org.eobjects.datacleaner.guice.DCModule;
import org.eobjects.datacleaner.guice.InjectorBuilder;
import org.eobjects.datacleaner.macos.MacOSManager;
import org.eobjects.datacleaner.regexswap.RegexSwapUserPreferencesHandler;
import org.eobjects.datacleaner.user.DataCleanerHome;
import org.eobjects.datacleaner.user.MonitorConnection;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.DCUncaughtExceptionHandler;
import org.eobjects.datacleaner.util.LookAndFeelManager;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;
import org.eobjects.datacleaner.windows.MonitorConnectionDialog;
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
        if (options == null) {
            throw new IllegalArgumentException("Bootstrap options cannot be null");
        }
        _options = options;
    }

    public void run() {
        try {
            runInternal();
        } catch (Exception e) {
            logger.error("An unexpected error has occurred during bootstrap. Exiting with status code -2.", e);
            exitCommandLine(null, -2);
        }
    }

    private void runInternal() throws FileSystemException {
        logger.info("Welcome to DataCleaner {}", Main.VERSION);

        // determine whether to run in command line interface mode
        final boolean cliMode = _options.isCommandLineMode();
        final CliArguments arguments = _options.getCommandLineArguments();

        logger.info("CLI mode={}, use -usage to view usage options", cliMode);

        if (cliMode) {

            if (!GraphicsEnvironment.isHeadless()) {
                // hide splash screen
                final SplashScreen splashScreen = SplashScreen.getSplashScreen();
                if (splashScreen != null) {
                    splashScreen.close();
                }
            }

            if (arguments.isUsageMode()) {
                final PrintWriter out = new PrintWriter(System.out);
                try {
                    CliArguments.printUsage(out);
                } finally {
                    FileHelper.safeClose(out);
                }

                exitCommandLine(null, 1);
                return;
            }
        }

        if (!cliMode) {
            // set up error handling that displays an error dialog
            final DCUncaughtExceptionHandler exceptionHandler = new DCUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);

            // init the look and feel
            LookAndFeelManager.getInstance().init();
        }

        final String configurationFilePath = arguments.getConfigurationFile();
        final FileObject configurationFile = resolveFile(configurationFilePath, "conf.xml", null);

        Injector injector = Guice.createInjector(new DCModule(DataCleanerHome.get(), configurationFile));

        // configuration loading can be multithreaded, so begin early
        final AnalyzerBeansConfiguration configuration = injector.getInstance(AnalyzerBeansConfiguration.class);

        // log usage
        final UsageLogger usageLogger = injector.getInstance(UsageLogger.class);
        usageLogger.logApplicationStartup();

        if (cliMode) {

            // run in CLI mode

            int exitCode = 0;
            final CliRunner runner = new CliRunner(arguments);
            try {
                runner.run(configuration);
            } catch (Throwable e) {
                logger.error("Error occurred while running DataCleaner command line mode", e);
                exitCode = 1;
            } finally {
                runner.close();
                exitCommandLine(configuration, exitCode);
            }
            return;
        } else {
            // run in GUI mode
            final AnalysisJobBuilderWindow analysisJobBuilderWindow;

            // initialize Mac OS specific settings
            final MacOSManager macOsManager = injector.getInstance(MacOSManager.class);
            macOsManager.init();

            final UserPreferences userPreferences = injector.getInstance(UserPreferences.class);

            // check for job file
            final String jobFilePath = _options.getCommandLineArguments().getJobFile();
            if (jobFilePath != null) {
                final FileObject jobFile = resolveFile(jobFilePath, null, userPreferences);
                injector = OpenAnalysisJobActionListener.open(jobFile, configuration, injector);
            }

            analysisJobBuilderWindow = injector.getInstance(AnalysisJobBuilderWindow.class);

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
                final AnalysisJobBuilder analysisJobBuilder = analysisJobBuilderWindow.getAnalysisJobBuilder();
                final DatastoreConnection con = singleDatastore.openConnection();
                final InjectorBuilder injectorBuilder = injector.getInstance(InjectorBuilder.class);
                try {
                    _options.initializeSingleDatastoreJob(analysisJobBuilder, con.getDataContext(), injectorBuilder);
                } finally {
                    con.close();
                }
            }

            final Image welcomeImage = _options.getWelcomeImage();
            if (welcomeImage != null) {
                // Ticket #834: make sure to show welcome dialog in swing's
                // dispatch thread.
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        final WelcomeDialog welcomeDialog = new WelcomeDialog(analysisJobBuilderWindow, welcomeImage);
                        welcomeDialog.setVisible(true);
                    }
                });
            }

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

    /**
     * Looks up a file, either based on a user requested filename (typically a
     * CLI parameter, may be a URL) or by a relative filename defined in the
     * system-
     * 
     * @param userRequestedFilename
     *            the user requested filename, may be null
     * @param localFilename
     *            the relative filename defined by the system
     * @param userPreferences
     * @return
     * @throws FileSystemException
     */
    private FileObject resolveFile(final String userRequestedFilename, final String localFilename,
            UserPreferences userPreferences) throws FileSystemException {
        final FileObject dataCleanerHome = DataCleanerHome.get();
        if (userRequestedFilename == null) {
            return dataCleanerHome.resolveFile(localFilename);
        } else {
            String lowerCaseFilename = userRequestedFilename.toLowerCase();
            if (lowerCaseFilename.startsWith("http://") || lowerCaseFilename.startsWith("https://")) {
                if (!GraphicsEnvironment.isHeadless()) {
                    // download to a RAM file.
                    final FileObject targetDirectory = VFSUtils.getFileSystemManager().resolveFile(
                            "ram:///datacleaner/temp");
                    if (!targetDirectory.exists()) {
                        targetDirectory.createFolder();
                    }

                    final URI uri;
                    try {
                        uri = new URI(userRequestedFilename);
                    } catch (URISyntaxException e) {
                        throw new IllegalArgumentException("Illegal URI: " + userRequestedFilename, e);
                    }

                    final HttpClient httpClient = new DefaultHttpClient();
                    final WindowContext windowContext = new SimpleWindowContext();

                    // check if URI points to DC monitor. If so, make sure
                    // credentials are entered.
                    if (userPreferences != null && userPreferences.getMonitorConnection() != null) {
                        MonitorConnection monitorConnection = userPreferences.getMonitorConnection();
                        if (monitorConnection.matchesURI(uri)) {
                            if (monitorConnection.isAuthenticationEnabled()) {
                                if (monitorConnection.getEncodedPassword() == null) {
                                    final MonitorConnectionDialog dialog = new MonitorConnectionDialog(windowContext,
                                            userPreferences, httpClient);
                                    // show modal dialog, this will block until
                                    // closed.
                                    dialog.setModal(true);
                                    dialog.open();
                                    monitorConnection = userPreferences.getMonitorConnection();
                                }
                            }
                            monitorConnection.prepareClient(httpClient);
                        }
                    }

                    final String[] urls = new String[] { userRequestedFilename };
                    final String[] targetFilenames = DownloadFilesActionListener.createTargetFilenames(urls);

                    final DownloadFilesActionListener downloadAction = new DownloadFilesActionListener(urls,
                            targetDirectory, targetFilenames, null, windowContext, httpClient);
                    downloadAction.actionPerformed(null);

                    final FileObject[] files = downloadAction.getFiles();

                    assert files.length == 1;

                    final FileObject ramFile = files[0];

                    final String scheme = uri.getScheme();
                    final int defaultPort;
                    if ("http".equals(scheme)) {
                        defaultPort = 80;
                    } else {
                        defaultPort = 8443;
                    }

                    final UrlFileName fileName = new UrlFileName(scheme, uri.getHost(), uri.getPort(), defaultPort,
                            null, null, uri.getPath(), FileType.FILE, uri.getQuery());

                    AbstractFileSystem fileSystem = (AbstractFileSystem) dataCleanerHome.getFileSystem();
                    return new DelegateFileObject(fileName, fileSystem, ramFile);
                }
            }

            return VFSUtils.getFileSystemManager().resolveFile(userRequestedFilename);
        }
    }

    private void exitCommandLine(AnalyzerBeansConfiguration configuration, int statusCode) {
        if (configuration != null) {
            logger.debug("Shutting down task runner");
            try {
                configuration.getTaskRunner().shutdown();
            } catch (Exception e) {
                logger.warn("Shutting down TaskRunner threw unexpected exception", e);
            }
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
        ExtensionSwapInstallationHttpContainer container = new ExtensionSwapInstallationHttpContainer(
                extensionSwapClient, userPreferences, usageLogger);

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
