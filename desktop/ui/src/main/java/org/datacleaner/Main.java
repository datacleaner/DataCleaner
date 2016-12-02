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
package org.datacleaner;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.datacleaner.bootstrap.Bootstrap;
import org.datacleaner.bootstrap.BootstrapOptions;
import org.datacleaner.bootstrap.DefaultBootstrapOptions;
import org.datacleaner.extensions.ClassLoaderUtils;
import org.datacleaner.user.DataCleanerHome;

/**
 * The main executable class of DataCleaner. This class primarily sets up
 * logging, system properties and delegates to the {@link Bootstrap} class for
 * actual application startup.
 */
public final class Main {

    /**
     * Initializes system properties based on the arguments passed
     *
     * @param args
     * @return
     */
    protected static Map<String, String> initializeSystemProperties(final String[] args) {
        final Map<String, String> result = new HashMap<>();
        final Pattern pattern = Pattern.compile("-D(.+)=(.+)");
        for (final String arg : args) {
            final Matcher matcher = pattern.matcher(arg);
            if (matcher.matches()) {
                final String key = matcher.group(1);
                final String value = matcher.group(2);
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
        try {

            // initial logging config, used before anything else
            {
                final URL url = Main.class.getResource("log4j-initial.xml");
                assert url != null;
                DOMConfigurator.configure(url);
            }

            if (ClassLoaderUtils.IS_WEB_START) {
                final URL url = Main.class.getResource("log4j-jnlp.xml");
                assert url != null;
                println("Using JNLP log configuration: " + url);
                DOMConfigurator.configure(url);
                return true;
            }

            final File dataCleanerHome = DataCleanerHome.getAsFile();
            if (initializeLoggingFromDirectory(dataCleanerHome)) {
                return true;
            }

            if (initializeLoggingFromDirectory(new File("."))) {
                return true;
            }

            // fall back to default log4j.xml file in classpath
            final URL url = Main.class.getResource("log4j-default.xml");
            assert url != null;
            println("Using default log configuration: " + url);
            DOMConfigurator.configure(url);
            return false;

        } catch (final NoClassDefFoundError e) {
            // can happen if log4j is not on the classpath
            println("Failed to initialize logging, class not found: " + e.getMessage());
            return false;
        }
    }

    private static boolean initializeLoggingFromDirectory(final File directory) {
        try {
            final File xmlConfigurationFile = new File(directory, "log4j.xml");
            if (xmlConfigurationFile.exists() && xmlConfigurationFile.isFile()) {
                println("Using custom log configuration: " + xmlConfigurationFile);
                DOMConfigurator.configure(xmlConfigurationFile.toURI().toURL());
                return true;
            }
        } catch (final MalformedURLException e) {
            // no xml logging found, ignore
        }

        try {
            final File propertiesConfigurationFile = new File(directory, "log4j.properties");
            if (propertiesConfigurationFile.exists() && propertiesConfigurationFile.isFile()) {
                println("Using custom log configuration: " + propertiesConfigurationFile);
                PropertyConfigurator.configure(propertiesConfigurationFile.toURI().toURL());
                return true;
            }
        } catch (final MalformedURLException e) {
            // no properties logging found, ignore
        }
        return false;
    }

    /**
     * Prints a message to the console. This mechanism is to be used only before
     * logging is configured.
     *
     * @param string
     */
    private static void println(final String string) {
        System.out.println(string);
    }

    public static void main(final String[] args) {
        main(args, true, true);
    }

    public static void main(final String[] args, final boolean initializeSystemProperties,
            final boolean initializeLogging) {
        if (initializeSystemProperties) {
            initializeSystemProperties(args);
        }

        if (initializeLogging) {
            initializeLogging();
        }

        final BootstrapOptions bootstrapOptions = new DefaultBootstrapOptions(args);
        final Bootstrap bootstrap = new Bootstrap(bootstrapOptions);
        bootstrap.run();
    }
}
