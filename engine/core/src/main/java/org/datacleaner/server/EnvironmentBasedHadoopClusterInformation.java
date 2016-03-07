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
package org.datacleaner.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;

/**
 * Environment based configuration
 */
public class EnvironmentBasedHadoopClusterInformation extends DirectoryBasedHadoopClusterInformation
        implements HadoopClusterInformation {
    private static final long serialVersionUID = 1L;
    public static final String YARN_CONF_DIR = "YARN_CONF_DIR";
    public static final String HADOOP_CONF_DIR = "HADOOP_CONF_DIR";
    private static final String[] CONFIGURATION_VARIABLES = { HADOOP_CONF_DIR, YARN_CONF_DIR };

    public EnvironmentBasedHadoopClusterInformation(final String name, final String description) {
        super(name, description, getConfigurationDirectories());
    }

    private static String[] getConfigurationDirectories() {
        final List<String> configDirectories = new ArrayList<>();
        
        // first read system properties
        for (String configVariable : CONFIGURATION_VARIABLES) {
            final String propertyValues = System.getProperty(configVariable);
            if (propertyValues != null) {
                configDirectories.add(propertyValues);
            }
        }

        // if no system properties defined, then check environment variables
        // (don't mix the two)
        if (configDirectories.isEmpty()) {
            for (String configVariable : CONFIGURATION_VARIABLES) {
                final String environmentValue = System.getenv(configVariable);
                if (environmentValue != null) {
                    configDirectories.add(environmentValue);
                }
            }
        }

        return configDirectories.toArray(new String[configDirectories.size()]);
    }

    @Override
    public Configuration getConfiguration() {
        try {
            return super.getConfiguration();
        } catch (IllegalStateException e) {
            if(getDirectories().length == 0) {
                throw new IllegalStateException(
                        "None of the standard Hadoop environment variables (HADOOP_CONF_DIR, YARN_CONF_DIR) has been set.", e);
            } else {
                throw e;
            }
        }
    }
}
