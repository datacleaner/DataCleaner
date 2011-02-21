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

import javax.swing.UnsupportedLookAndFeelException;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.datacleaner.regexswap.RegexSwapUserPreferencesHandler;
import org.eobjects.datacleaner.user.DCConfiguration;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.DCUncaughtExceptionHandler;
import org.eobjects.datacleaner.util.LookAndFeelManager;
import org.eobjects.datacleaner.windows.MainWindow;
import org.eobjects.datacleaner.windows.WelcomeWindow;

public final class Main {

	public static final String VERSION = "2.0.1";

	public static void main(String[] args) throws UnsupportedLookAndFeelException {
		// set up default error handling
		Thread.setDefaultUncaughtExceptionHandler(new DCUncaughtExceptionHandler());

		// init the look and feel
		LookAndFeelManager.getInstance().init();

		// loads static configuration
		final AnalyzerBeansConfiguration configuration = DCConfiguration.get();

		// loads dynamic user preferences
		final UserPreferences userPreferences = UserPreferences.getInstance();

		// show windows
		if (userPreferences.isWelcomeDialogShownOnStartup()) {
			new WelcomeWindow(configuration).setVisible(true);
		}
		new MainWindow(configuration).setVisible(true);

		// log usage
		UsageLogger.getInstance().logApplicationStartup();

		// load regex swap regexes if logged in
		final RegexSwapUserPreferencesHandler regexSwapHandler = new RegexSwapUserPreferencesHandler(
				(MutableReferenceDataCatalog) configuration.getReferenceDataCatalog());
		userPreferences.addLoginChangeListener(regexSwapHandler);
	}
}
