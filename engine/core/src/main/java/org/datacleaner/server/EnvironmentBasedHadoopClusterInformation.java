package org.datacleaner.server;

import org.apache.hadoop.conf.Configuration;

import com.google.common.base.Joiner;

/**
 * Environment based configuration
 */
public class EnvironmentBasedHadoopClusterInformation extends DirectoryBasedHadoopClusterInformation
        implements HadoopClusterInformation {
    public static final String YARN_CONF_DIR = "YARN_CONF_DIR";
    public static final String HADOOP_CONF_DIR = "HADOOP_CONF_DIR";
    private static final String[] CONFIGURATION_DIRECTORIES =
            { System.getenv(HADOOP_CONF_DIR), System.getenv(YARN_CONF_DIR) };

    public EnvironmentBasedHadoopClusterInformation(final String name, final String description) {
        super(name, description, CONFIGURATION_DIRECTORIES);
    }

    @Override
    public Configuration getConfiguration() {
        try {
            return super.getConfiguration();
        } catch (IllegalStateException e) {
            throw new IllegalStateException(
                    "None of the standard Hadoop environment variables (" + Joiner.on(",")
                            .join(CONFIGURATION_DIRECTORIES) + ") has been set.");
        }
    }
}
