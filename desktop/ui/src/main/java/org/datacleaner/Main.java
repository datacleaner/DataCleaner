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
package org.datacleaner;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.datacleaner.bootstrap.Bootstrap;
import org.datacleaner.bootstrap.BootstrapOptions;
import org.datacleaner.bootstrap.DefaultBootstrapOptions;
import org.datacleaner.user.DataCleanerHome;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

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
     * Initializes logging, specifically by looking for logback.xml
     * files in DataCleaner's classpath, home directory or execution directory.
     *
     * @return true if a logging configuration file was found, or false
     *         otherwise
     */
    protected static boolean initializeLogging() {
        final ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        if (!(loggerFactory instanceof LoggerContext)) {
            println("Using a non-logback logging framework. Logging will not be configured by DataCleaner.");
            return false;
        }
        
        final LoggerContext loggerContext = (LoggerContext) loggerFactory;
        final JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        loggerContext.reset();
        
        try {

            // initial logging config, used before anything else
            {
                final URL url = Main.class.getResource("logback-initial.xml");
                assert url != null;
                configurator.doConfigure(url);
            }

            final File dataCleanerHome = DataCleanerHome.getAsFile();
            if (initializeLoggingFromDirectory(configurator, dataCleanerHome)) {
                return true;
            }

            if (initializeLoggingFromDirectory(configurator, new File("."))) {
                return true;
            }

            // fall back to default logback.xml file in classpath
            final URL url = Main.class.getResource("logback-default.xml");
            assert url != null;
            println("Using default log configuration: " + url);
            configurator.doConfigure(url);
            return false;

        } catch (final NoClassDefFoundError e) {
            // can happen if logback is not on the classpath
            println("Failed to initialize logging, class not found: " + e.getMessage());
            return false;
        } catch (JoranException e) {
            println("Failed to initialize logging, logback error: " + e.getMessage());
            return false;
        }
    }

    private static boolean initializeLoggingFromDirectory(final JoranConfigurator configurator, final File directory) throws JoranException {
        try {
            final File xmlConfigurationFile = new File(directory, "logback.xml");
            if (xmlConfigurationFile.exists() && xmlConfigurationFile.isFile()) {
                println("Using custom log configuration: " + xmlConfigurationFile);
                configurator.doConfigure(xmlConfigurationFile.toURI().toURL());
                return true;
            }
        } catch (final MalformedURLException e) {
            // no xml logging found, ignore
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
