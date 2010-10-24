package org.eobjects.datacleaner;

import java.io.File;

import javax.swing.UnsupportedLookAndFeelException;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.JaxbConfigurationFactory;
import org.eobjects.datacleaner.util.DCUncaughtExceptionHandler;
import org.eobjects.datacleaner.util.LookAndFeelManager;
import org.eobjects.datacleaner.windows.MainWindow;

public final class Main {

	public static void main(String[] args) throws UnsupportedLookAndFeelException {
		JaxbConfigurationFactory configurationFactory = new JaxbConfigurationFactory();
		AnalyzerBeansConfiguration configuration = configurationFactory.create(new File("conf.xml"));
		
		Thread.setDefaultUncaughtExceptionHandler(new DCUncaughtExceptionHandler());
		LookAndFeelManager.getInstance().init();
		
		new MainWindow(configuration).setVisible(true);
	}
}
