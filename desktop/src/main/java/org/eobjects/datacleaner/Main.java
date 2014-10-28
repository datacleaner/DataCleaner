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
package org.eobjects.datacleaner;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.eobjects.analyzer.util.ClassLoaderUtils;
import org.eobjects.datacleaner.bootstrap.Bootstrap;
import org.eobjects.datacleaner.bootstrap.BootstrapOptions;
import org.eobjects.datacleaner.bootstrap.DefaultBootstrapOptions;
import org.eobjects.datacleaner.user.DataCleanerHome;

/**
 * The main executable class of DataCleaner. This class primarily sets up
 * logging, system properties and delegates to the {@link Bootstrap} class for
 * actual application startup.
 */
public final class Main {

    public static void main(String[] args) {
        main(args, true, true);
    }

    public static void main(String[] args, boolean initializeSystemProperties, boolean initializeLogging) {
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

    /**
     * Initializes system properties based on the arguments passed
     * 
     * @param args
     * @return
     */
    protected static Map<String, String> initializeSystemProperties(String[] args) {
        final Map<String, String> result = new HashMap<String, String>();
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

            final FileObject dataCleanerHome = DataCleanerHome.get();

            try {
                final FileObject xmlConfigurationFile = dataCleanerHome.resolveFile("log4j.xml");
                if (xmlConfigurationFile.exists() && xmlConfigurationFile.getType() == FileType.FILE) {
                    println("Using custom log configuration: " + xmlConfigurationFile);
                    DOMConfigurator.configure(xmlConfigurationFile.getURL());
                    return true;
                }
            } catch (FileSystemException e) {
                // no xml logging found, ignore
            }

            try {
                final FileObject propertiesConfigurationFile = dataCleanerHome.resolveFile("log4j.properties");
                if (propertiesConfigurationFile.exists() && propertiesConfigurationFile.getType() == FileType.FILE) {
                    println("Using custom log configuration: " + propertiesConfigurationFile);
                    PropertyConfigurator.configure(propertiesConfigurationFile.getURL());
                    return true;
                }
            } catch (FileSystemException e) {
                // no xml logging found, ignore
            }

            // fall back to default log4j.xml file in classpath
            final URL url = Main.class.getResource("log4j-default.xml");
            assert url != null;
            println("Using default log configuration: " + url);
            DOMConfigurator.configure(url);
            return false;

        } catch (NoClassDefFoundError e) {
            // can happen if log4j is not on the classpath
            println("Failed to initialize logging, class not found: " + e.getMessage());
            return false;
        }
    }

    /**
     * Prints a message to the console. This mechanism is to be used only before
     * logging is configured.
     * 
     * @param string
     */
    private static void println(String string) {
        System.out.println(string);
    }
}
