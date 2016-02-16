package org.datacleaner.server;


import org.apache.hadoop.conf.Configuration;
import org.datacleaner.configuration.ServerInformation;

/**
 * Represents a connection to a hadoop cluster. Either by namenode or other means (i.e. in the case of MapR).
 */
public interface HadoopClusterInformation extends ServerInformation {
    Configuration getConfiguration();
}
