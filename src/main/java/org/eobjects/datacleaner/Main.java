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

import java.io.File;

import javax.swing.UnsupportedLookAndFeelException;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.configuration.JaxbConfigurationReader;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.util.DCUncaughtExceptionHandler;
import org.eobjects.datacleaner.util.LookAndFeelManager;
import org.eobjects.datacleaner.windows.MainWindow;
import org.eobjects.datacleaner.windows.WelcomeDialog;

public final class Main {

	public static void main(String[] args) throws UnsupportedLookAndFeelException {

		JaxbConfigurationReader configurationReader = new JaxbConfigurationReader();
		AnalyzerBeansConfiguration c = configurationReader.create(new File("conf.xml"));

		// make the configuration mutable
		c = new AnalyzerBeansConfigurationImpl(new MutableDatastoreCatalog(c.getDatastoreCatalog()),
				c.getReferenceDataCatalog(), c.getDescriptorProvider(), c.getTaskRunner(), c.getStorageProvider());

		Thread.setDefaultUncaughtExceptionHandler(new DCUncaughtExceptionHandler());
		LookAndFeelManager.getInstance().init();
		
		new WelcomeDialog(c).setVisible(true);

		new MainWindow(c).setVisible(true);
	}
}
