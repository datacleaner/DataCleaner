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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.eobjects.datacleaner.bootstrap.Bootstrap;
import org.eobjects.datacleaner.bootstrap.BootstrapOptions;
import org.eobjects.datacleaner.bootstrap.DefaultBootstrapOptions;
import org.eobjects.datacleaner.user.DataCleanerHome;

/**
 * The main executable class of DataCleaner. This class primarily sets up
 * logging, system properties and delegates to the {@link Bootstrap} class for
 * actual application startup.
 * 
 * @author Kasper Sørensen
 */
public final class Main {

	public static final String VERSION = "3.0-SNAPSHOT";

	public static void main(String[] args) {
		initializeSystemProperties(args);

		initializeLogging();

		BootstrapOptions bootstrapOptions = new DefaultBootstrapOptions(args);
		Bootstrap bootstrap = new Bootstrap(bootstrapOptions);
		bootstrap.run();
	}

	/**
	 * Initializes system properties based on the arguments passed
	 * 
	 * @param args
	 * @return
	 */
	protected static Map<String, String> initializeSystemProperties(String[] args) {
		Map<String, String> result = new HashMap<String, String>();
		Pattern pattern = Pattern.compile("-D(.+)=(.+)");
		for (String arg : args) {
			Matcher matcher = pattern.matcher(arg);
			if (matcher.matches()) {
				String key = matcher.group(1);
				String value = matcher.group(2);
				result.put(key, value);
				System.setProperty(key, value);
			}
		}
		return result;
	}

	/**
	 * Initializes logging, specifically by looking for log4j.xml or
	 * log4j.properties file in DataCleaner's home directory.
	 * 
	 * @return true if a logging configuration file was found, or false
	 *         otherwise
	 */
	protected static boolean initializeLogging() {
		final File dataCleanerHome = DataCleanerHome.get();
		final File xmlConfigurationFile = new File(dataCleanerHome, "log4j.xml");
		if (xmlConfigurationFile.exists() && xmlConfigurationFile.isFile()) {
			DOMConfigurator.configure(xmlConfigurationFile.getAbsolutePath());
			return true;
		}

		final File propertiesConfigurationFile = new File(dataCleanerHome, "log4j.properties");
		if (propertiesConfigurationFile.exists() && propertiesConfigurationFile.isFile()) {
			PropertyConfigurator.configure(propertiesConfigurationFile.getAbsolutePath());
			return true;
		}

		return false;
	}
}
