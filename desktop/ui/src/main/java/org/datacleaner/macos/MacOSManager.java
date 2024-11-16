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

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.inject.Provider;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.datacleaner.actions.OpenAnalysisJobActionListener;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.VFSUtils;
import org.datacleaner.windows.AboutDialog;
import org.datacleaner.windows.OptionsDialog;
import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;
import org.simplericity.macify.eawt.DefaultApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 *
 * @author Kasper Sørensen
 */
public class MacOSManager {

    public class DCApplicationListener implements ApplicationListener {

        @Override
        public void handleAbout(final ApplicationEvent event) {
            final AboutDialog dialog = new AboutDialog(_windowContext);
            dialog.setVisible(true);
            event.setHandled(true);
        }

        @Override
        public void handleOpenFile(final ApplicationEvent event) {
            final String filename = event.getFilename();
            final OpenAnalysisJobActionListener actionListener = _openAnalysisJobActionListenerProvider.get();
            try {
                final FileObject file = VFSUtils.getFileSystemManager().resolveFile(filename);
                actionListener.openFile(file);
            } catch (final FileSystemException e) {
                throw new IllegalArgumentException("Could not resolve filename: " + filename, e);
            }
        }

        @Override
        public void handlePreferences(final ApplicationEvent event) {
            final OptionsDialog dialog = _optionsDialogProvider.get();
            dialog.setVisible(true);
        }

        @Override
        public void handleQuit(final ApplicationEvent event) {
            _windowContext.exit();
        }

        @Override
        public void handleOpenApplication(final ApplicationEvent event) {
        }

        @Override
        public void handlePrintFile(final ApplicationEvent event) {
        }

        @Override
        public void handleReOpenApplication(final ApplicationEvent event) {
        }
    }

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
    
    private BufferedImage bufferedImage(Image image) throws IllegalArgumentException {
    	if (image instanceof BufferedImage) {
    		return (BufferedImage) image;
    	}
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Image dimensions are invalid");
        }
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.getGraphics().drawImage(image, 0, 0, null);
        return bufferedImage;
    }

    public void init() {
        final DefaultApplication app = new DefaultApplication();

        if (!app.isMac()) {
            logger.debug("Omitting Mac OS initialization, since operating system is not Mac OS");
            return;
        }

        System.setProperty("apple.laf.useScreenMenuBar", "true");

        final Image image = ImageManager.get().getImage("images/menu/dc-logo-30.png");
        app.setApplicationIconImage(bufferedImage(image));
        app.addAboutMenuItem();
        app.setEnabledAboutMenu(true);
        app.addPreferencesMenuItem();
        app.setEnabledPreferencesMenu(true);
        app.addApplicationListener(new DCApplicationListener());
    }
}