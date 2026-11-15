/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
package org.datacleaner.macos;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitResponse;
import java.io.File;
import java.util.Locale;

import javax.inject.Provider;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.datacleaner.actions.OpenAnalysisJobActionListener;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.VFSUtils;
import org.datacleaner.windows.AboutDialog;
import org.datacleaner.windows.OptionsDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 *
 * @author Kasper Sørensen
 */
public class MacOSManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final WindowContext _windowContext;
    private final Provider<OpenAnalysisJobActionListener> _openAnalysisJobActionListenerProvider;
    private final Provider<OptionsDialog> _optionsDialogProvider;

    @Inject
    protected MacOSManager(final WindowContext windowContext,
            final Provider<OpenAnalysisJobActionListener> openAnalysisJobActionListenerProvider,
            final Provider<OptionsDialog> optionsDialogProvider) {
        _windowContext = windowContext;
        _openAnalysisJobActionListenerProvider = openAnalysisJobActionListenerProvider;
        _optionsDialogProvider = optionsDialogProvider;
    }

    private boolean isMacOS() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
    }

    private void handleAbout(final AboutEvent event) {
        final AboutDialog dialog = new AboutDialog(_windowContext);
        dialog.setVisible(true);
    }

    private void handleOpenFiles(final OpenFilesEvent event) {
        final OpenAnalysisJobActionListener actionListener = _openAnalysisJobActionListenerProvider.get();
        for (final File file : event.getFiles()) {
            final String filename = file.getAbsolutePath();
            try {
                final FileObject fileObject = VFSUtils.getFileSystemManager().resolveFile(filename);
                actionListener.openFile(fileObject);
            } catch (final FileSystemException e) {
                throw new IllegalArgumentException("Could not resolve filename: " + filename, e);
            }
        }
    }

    private void handlePreferences(final PreferencesEvent event) {
        final OptionsDialog dialog = _optionsDialogProvider.get();
        dialog.setVisible(true);
    }

    private void handleQuit(final QuitEvent event, final QuitResponse response) {
        _windowContext.exit();
        response.performQuit();
    }

    private void setApplicationIcon() {
        try {
            if (!Taskbar.isTaskbarSupported()) {
                logger.debug("Taskbar is not supported, omitting Mac OS application icon");
                return;
            }
            final Taskbar taskbar = Taskbar.getTaskbar();
            if (!taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                logger.debug("Taskbar icon images are not supported, omitting Mac OS application icon");
                return;
            }
            final Image image = ImageManager.get().getImage("images/menu/dc-logo-30.png");
            taskbar.setIconImage(image);
        } catch (final RuntimeException e) {
            logger.debug("Could not set Mac OS application icon", e);
        }
    }

    private void initDesktopIntegration() {
        if (!Desktop.isDesktopSupported()) {
            logger.debug("Desktop API is not supported, omitting Mac OS menu integration");
            return;
        }

        final Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            desktop.setAboutHandler(this::handleAbout);
        }
        if (desktop.isSupported(Desktop.Action.APP_PREFERENCES)) {
            desktop.setPreferencesHandler(this::handlePreferences);
        }
        if (desktop.isSupported(Desktop.Action.APP_OPEN_FILE)) {
            desktop.setOpenFileHandler(this::handleOpenFiles);
        }
        if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
            desktop.setQuitHandler(this::handleQuit);
        }
    }

    public void init() {
        if (!isMacOS()) {
            logger.debug("Omitting Mac OS initialization, since operating system is not Mac OS");
            return;
        }

        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "DataCleaner");

        setApplicationIcon();

        try {
            initDesktopIntegration();
        } catch (final RuntimeException e) {
            logger.warn("Could not initialize Mac OS desktop integration", e);
        }
    }
}
