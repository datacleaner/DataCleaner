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
package org.eobjects.datacleaner.macos;

import java.io.File;

import javax.inject.Provider;

import org.eobjects.datacleaner.actions.OpenAnalysisJobActionListener;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.windows.AboutDialog;
import org.eobjects.datacleaner.windows.OptionsDialog;
import org.simplericity.macify.eawt.Application;
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

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final WindowContext _windowContext;
	private final Provider<OpenAnalysisJobActionListener> _openAnalysisJobActionListenerProvider;
	private final Provider<OptionsDialog> _optionsDialogProvider;

	@Inject
	protected MacOSManager(WindowContext windowContext,
			Provider<OpenAnalysisJobActionListener> openAnalysisJobActionListenerProvider,
			Provider<OptionsDialog> optionsDialogProvider) {
		_windowContext = windowContext;
		_openAnalysisJobActionListenerProvider = openAnalysisJobActionListenerProvider;
		_optionsDialogProvider = optionsDialogProvider;
	}

	public void init() {
		Application app = new DefaultApplication();

		if (!app.isMac()) {
			logger.debug("Omitting Mac OS initialization, since operating system is not Mac OS");
			return;
		}

		System.setProperty("apple.laf.useScreenMenuBar", "true");

		app.addAboutMenuItem();
		app.setEnabledAboutMenu(true);
		app.addPreferencesMenuItem();
		app.setEnabledPreferencesMenu(true);
		app.addApplicationListener(new DCApplicationListener());
	}

	public class DCApplicationListener implements ApplicationListener {

		@Override
		public void handleAbout(ApplicationEvent event) {
			AboutDialog dialog = new AboutDialog(_windowContext);
			dialog.setVisible(true);
			event.setHandled(true);
		}

		@Override
		public void handleOpenFile(ApplicationEvent event) {
			final String filename = event.getFilename();
			final OpenAnalysisJobActionListener actionListener = _openAnalysisJobActionListenerProvider.get();
			actionListener.openFile(new File(filename));
		}

		@Override
		public void handlePreferences(ApplicationEvent event) {
			OptionsDialog dialog = _optionsDialogProvider.get();
			dialog.setVisible(true);
		}

		@Override
		public void handleQuit(ApplicationEvent event) {
			if (_windowContext.showExitDialog()) {
				_windowContext.exit();
			}
			event.setHandled(true);
		}

		@Override
		public void handleOpenApplication(ApplicationEvent event) {
		}

		@Override
		public void handlePrintFile(ApplicationEvent event) {
		}

		@Override
		public void handleReOpenApplication(ApplicationEvent event) {
		}
	}
}
