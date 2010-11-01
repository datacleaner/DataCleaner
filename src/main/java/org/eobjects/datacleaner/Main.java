package org.eobjects.datacleaner;

import java.io.File;

import javax.swing.UnsupportedLookAndFeelException;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.configuration.JaxbConfigurationFactory;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.util.DCUncaughtExceptionHandler;
import org.eobjects.datacleaner.util.LookAndFeelManager;
import org.eobjects.datacleaner.windows.MainWindow;

public final class Main {

	public static void main(String[] args) throws UnsupportedLookAndFeelException {

		JaxbConfigurationFactory configurationFactory = new JaxbConfigurationFactory();
		AnalyzerBeansConfiguration c = configurationFactory.create(new File("conf.xml"));

		// make the configuration mutable
		c = new AnalyzerBeansConfigurationImpl(new MutableDatastoreCatalog(c.getDatastoreCatalog()),
				c.getReferenceDataCatalog(), c.getDescriptorProvider(), c.getTaskRunner(), c.getStorageProvider());

		Thread.setDefaultUncaughtExceptionHandler(new DCUncaughtExceptionHandler());
		LookAndFeelManager.getInstance().init();

		new MainWindow(c).setVisible(true);
	}
}
