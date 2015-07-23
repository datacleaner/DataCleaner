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

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.SplashScreen;
import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.swing.JOptionPane;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.DelegateFileObject;
import org.apache.commons.vfs2.provider.url.UrlFileName;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.Version;
import org.datacleaner.actions.DownloadFilesActionListener;
import org.datacleaner.actions.OpenAnalysisJobActionListener;
import org.datacleaner.cli.CliArguments;
import org.datacleaner.cli.CliRunner;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.extensionswap.ExtensionSwapClient;
import org.datacleaner.extensionswap.ExtensionSwapInstallationHttpContainer;
import org.datacleaner.guice.DCModuleImpl;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.macos.MacOSManager;
import org.datacleaner.user.DataCleanerHome;
import org.datacleaner.user.MonitorConnection;
import org.datacleaner.user.UsageLogger;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.user.UserPreferencesImpl;
import org.datacleaner.util.DCUncaughtExceptionHandler;
import org.datacleaner.util.LookAndFeelManager;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.VFSUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.util.http.MonitorHttpClient;
import org.datacleaner.util.http.SimpleWebServiceHttpClient;
import org.datacleaner.windows.AnalysisJobBuilderWindow;
import org.datacleaner.windows.MonitorConnectionDialog;
import org.datacleaner.windows.WelcomeDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Bootstraps an instance of DataCleaner into a running state. The initial state
 * of the application will be dependent on specified options (or defaults).
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
        logger.info("Welcome to DataCleaner {}", Version.getVersion());

        // determine whether to run in command line interface mode
        final boolean cliMode = _options.isCommandLineMode();
        final CliArguments arguments = _options.getCommandLineArguments();

        logger.info("CLI mode={}, use -usage to view usage options", cliMode);

        if (cliMode) {

            try {
                if (!GraphicsEnvironment.isHeadless()) {
                    // hide splash screen
                    final SplashScreen splashScreen = SplashScreen.getSplashScreen();
                    if (splashScreen != null) {
                        splashScreen.close();
                    }
                }
            } catch (Exception e) {
                // ignore this condition - may happen rarely on e.g. X windows
                // systems when the user is not authorized to access the
                // graphics environment.
                logger.trace("Swallowing exception caused by trying to hide splash screen", e);
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
            
            if (arguments.isVersionMode()) {
                final PrintWriter out = new PrintWriter(System.out);
                try {
                    CliArguments.printVersion(out);
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
            LookAndFeelManager.get().init();
        }

        // initially use a temporary non-persistent user preferences object.
        // This is just to have basic settings available for eg. resolving
        // files.
        final UserPreferences initialUserPreferences = new UserPreferencesImpl(null);

        final String configurationFilePath = arguments.getConfigurationFile();
        final FileObject configurationFile = resolveFile(configurationFilePath, "conf.xml", initialUserPreferences);

        Injector injector = Guice.createInjector(new DCModuleImpl(DataCleanerHome.get(), configurationFile));

        // configuration loading can be multithreaded, so begin early
        final DataCleanerConfiguration configuration = injector.getInstance(DataCleanerConfiguration.class);

        // log usage
        final UsageLogger usageLogger = injector.getInstance(UsageLogger.class);
        usageLogger.logApplicationStartup();

        if (cliMode) {
            // run in CLI mode

            int exitCode = 0;
            try (final CliRunner runner = new CliRunner(arguments)) {
                runner.run(configuration);
            } catch (Throwable e) {
                logger.error("Error occurred while running DataCleaner command line mode", e);
                exitCode = 1;
            } finally {
                exitCommandLine(configuration, exitCode);
            }
            return;
        } else {
            // run in GUI mode
            final AnalysisJobBuilderWindow analysisJobBuilderWindow;

            // initialize Mac OS specific settings
            final MacOSManager macOsManager = injector.getInstance(MacOSManager.class);
            macOsManager.init();

            // check for job file
            final String jobFilePath = _options.getCommandLineArguments().getJobFile();
            if (jobFilePath != null) {
                final FileObject jobFile = resolveFile(jobFilePath, null, initialUserPreferences);
                injector = OpenAnalysisJobActionListener.open(jobFile, configuration, injector);
            }

            final UserPreferences userPreferences = injector.getInstance(UserPreferences.class);

            analysisJobBuilderWindow = injector.getInstance(AnalysisJobBuilderWindow.class);

            final Datastore singleDatastore;
            if (_options.isSingleDatastoreMode()) {
                DatastoreCatalog datastoreCatalog = configuration.getDatastoreCatalog();
                singleDatastore = _options.getSingleDatastore(datastoreCatalog);
                if (singleDatastore == null) {
                    logger.info("Single datastore mode was enabled, but datastore was null!");
                } else {
                    logger.info("Initializing single datastore mode with {}", singleDatastore);
                    analysisJobBuilderWindow.setDatastoreSelectionEnabled(false);
                    analysisJobBuilderWindow.setDatastore(singleDatastore, true);
                }
            } else {
                singleDatastore = null;
            }

            // show the window
            analysisJobBuilderWindow.open();

            if (singleDatastore != null) {
                // this part has to be done after displaying the window (a lot
                // of initialization goes on there)
                final AnalysisJobBuilder analysisJobBuilder = analysisJobBuilderWindow.getAnalysisJobBuilder();
                try (final DatastoreConnection con = singleDatastore.openConnection()) {
                    final InjectorBuilder injectorBuilder = injector.getInstance(InjectorBuilder.class);
                    _options.initializeSingleDatastoreJob(analysisJobBuilder, con.getDataContext(), injectorBuilder);
                }
            }

            final Image welcomeImage = _options.getWelcomeImage();
            if (welcomeImage != null) {
                // Ticket #834: make sure to show welcome dialog in swing's
                // dispatch thread.
                WidgetUtils.invokeSwingAction(new Runnable() {
                    @Override
                    public void run() {
                        final WelcomeDialog welcomeDialog = new WelcomeDialog(analysisJobBuilderWindow, welcomeImage);
                        welcomeDialog.setVisible(true);
                    }
                });
            }

            final WindowContext windowContext = injector.getInstance(WindowContext.class);

            // set up HTTP service for ExtensionSwap installation
            loadExtensionSwapService(userPreferences, windowContext, configuration, usageLogger);

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
        final File dataCleanerHome = DataCleanerHome.getAsFile();
        if (userRequestedFilename == null) {
            File file = new File(dataCleanerHome, localFilename);
            return VFSUtils.toFileObject(file);
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

                    final WindowContext windowContext = new SimpleWindowContext();

                    MonitorConnection monitorConnection = null;

                    // check if URI points to DC monitor. If so, make sure
                    // credentials are entered.
                    if (userPreferences != null && userPreferences.getMonitorConnection() != null) {
                        monitorConnection = userPreferences.getMonitorConnection();
                        if (monitorConnection.matchesURI(uri)) {
                            if (monitorConnection.isAuthenticationEnabled()) {
                                if (monitorConnection.getEncodedPassword() == null) {
                                    final MonitorConnectionDialog dialog = new MonitorConnectionDialog(windowContext,
                                            userPreferences);
                                    dialog.openBlocking();
                                }
                                monitorConnection = userPreferences.getMonitorConnection();
                            }
                        }
                    }

                    try (MonitorHttpClient httpClient = getHttpClient(monitorConnection)) {

                        final String[] urls = new String[] { userRequestedFilename };
                        final String[] targetFilenames = DownloadFilesActionListener.createTargetFilenames(urls);

                        final FileObject[] files = downloadFiles(urls, targetDirectory, targetFilenames, windowContext,
                                httpClient, monitorConnection);

                        assert files.length == 1;

                        final FileObject ramFile = files[0];

                        if (logger.isInfoEnabled()) {
                            final InputStream in = ramFile.getContent().getInputStream();
                            try {
                                final String str = FileHelper.readInputStreamAsString(ramFile.getContent()
                                        .getInputStream(), "UTF8");
                                logger.info("Downloaded file contents: {}\n{}", userRequestedFilename, str);
                            } finally {
                                FileHelper.safeClose(in);
                            }
                        }

                        final String scheme = uri.getScheme();
                        final int defaultPort;
                        if ("http".equals(scheme)) {
                            defaultPort = 80;
                        } else {
                            defaultPort = 443;
                        }

                        final UrlFileName fileName = new UrlFileName(scheme, uri.getHost(), uri.getPort(), defaultPort,
                                null, null, uri.getPath(), FileType.FILE, uri.getQuery());
                        
                        AbstractFileSystem fileSystem = (AbstractFileSystem) VFSUtils.getBaseFileSystem();
                        return new DelegateFileObject(fileName, fileSystem, ramFile);
                    } finally {
                        userPreferences.setMonitorConnection(monitorConnection);
                    }

                }
            }

            return VFSUtils.getFileSystemManager().resolveFile(userRequestedFilename);
        }
    }

    private MonitorHttpClient getHttpClient(MonitorConnection monitorConnection) {
        if (monitorConnection == null) {
            return new SimpleWebServiceHttpClient();
        } else {
            return monitorConnection.getHttpClient();
        }
    }

    private FileObject[] downloadFiles(String[] urls, FileObject targetDirectory, String[] targetFilenames,
            WindowContext windowContext, MonitorHttpClient httpClient, MonitorConnection monitorConnection) {
        final DownloadFilesActionListener downloadAction = new DownloadFilesActionListener(urls, targetDirectory,
                targetFilenames, null, windowContext, httpClient);
        try {
            downloadAction.actionPerformed(null);
            FileObject[] files = downloadAction.getFiles();
            if (logger.isInfoEnabled()) {
                logger.info("Succesfully downloaded urls: {}", Arrays.toString(urls));
            }
            return files;
        } catch (SSLPeerUnverifiedException e) {
            downloadAction.cancelDownload(true);
            if (monitorConnection == null || monitorConnection.isAcceptUnverifiedSslPeers()) {
                throw new IllegalStateException("Failed to verify SSL peer", e);
            }
            if (logger.isInfoEnabled()) {
                logger.info("SSL peer not verified. Asking user for confirmation to accept urls: {}",
                        Arrays.toString(urls));
            }
            int confirmation = JOptionPane.showConfirmDialog(null, "Unverified SSL peer.\n\n"
                    + "The certificate presented by the server could not be verified.\n\n"
                    + "Do you want to continue, accepting the unverified certificate?", "Unverified SSL peer",
                    JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            if (confirmation != JOptionPane.YES_OPTION) {
                throw new IllegalStateException(e);
            }
            monitorConnection.setAcceptUnverifiedSslPeers(true);
            httpClient = monitorConnection.getHttpClient();

            return downloadFiles(urls, targetDirectory, targetFilenames, windowContext, httpClient, monitorConnection);
        }
    }

    private void exitCommandLine(DataCleanerConfiguration configuration, int statusCode) {
        if (configuration != null) {
            logger.debug("Shutting down task runner");
            try {
                configuration.getEnvironment().getTaskRunner().shutdown();
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
            DataCleanerConfiguration configuration, UsageLogger usageLogger) {
        String websiteHostname = userPreferences.getAdditionalProperties().get("extensionswap.hostname");
        if (StringUtils.isNullOrEmpty(websiteHostname)) {
            websiteHostname = System.getProperty("extensionswap.hostname");
        }

        final ExtensionSwapClient extensionSwapClient;
        if (StringUtils.isNullOrEmpty(websiteHostname)) {
            logger.info("Using default ExtensionSwap website hostname");
            extensionSwapClient = new ExtensionSwapClient(windowContext, userPreferences, configuration);
        } else {
            logger.info("Using custom ExtensionSwap website hostname: {}", websiteHostname);
            extensionSwapClient = new ExtensionSwapClient(websiteHostname, windowContext, userPreferences,
                    configuration);
        }
        ExtensionSwapInstallationHttpContainer container = new ExtensionSwapInstallationHttpContainer(
                extensionSwapClient, usageLogger);

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
