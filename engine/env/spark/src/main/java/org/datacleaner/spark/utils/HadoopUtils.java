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
package org.datacleaner.spark.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.metamodel.util.FileHelper;

import com.google.common.base.Strings;

public class HadoopUtils {
    private static void addResourceIfExists(Configuration conf, File hadoopConfigurationDirectory, String filename) {
        final File file = new File(hadoopConfigurationDirectory, filename);
        if (file.exists()) {
            final InputStream inputStream = FileHelper.getInputStream(file);
            conf.addResource(inputStream, filename);
        }
    }

    /**
     * Gets a candidate directory based on a file path, if it exists, and if it
     * another candidate hasn't already been resolved.
     *
     * @param existingCandidate
     *            an existing candidate directory. If this is non-null, it will
     *            be returned immediately.
     * @param path
     *            the path of a directory
     * @return a candidate directory, or null if none was resolved.
     */
    private static File getDirectoryIfExists(File existingCandidate, String path) {
        if (existingCandidate != null) {
            return existingCandidate;
        }
        if (!Strings.isNullOrEmpty(path)) {
            final File directory = new File(path);
            if (directory.exists() && directory.isDirectory()) {
                return directory;
            }
        }
        return null;
    }

    public static File getHadoopConfigurationDirectoryToUse() {
        File candidate  = getDirectoryIfExists(null, System.getProperty("YARN_CONF_DIR"));
        candidate = getDirectoryIfExists(candidate, System.getProperty("HADOOP_CONF_DIR"));
        candidate = getDirectoryIfExists(candidate, System.getenv("YARN_CONF_DIR"));
        candidate = getDirectoryIfExists(candidate, System.getenv("HADOOP_CONF_DIR"));
        return candidate;
    }

    public static Configuration getHadoopConfiguration() {
        return getHadoopConfiguration(getHadoopConfigurationDirectoryToUse());
    }

    public static Configuration getHadoopConfiguration(final File hadoopConfigurationDirectory) {
        final Configuration conf = new Configuration();
        if (hadoopConfigurationDirectory == null) {
            throw new IllegalStateException("Environment variable YARN_CONF_DIR or HADOOP_CONF_DIR must be set");
        }

        addResourceIfExists(conf, hadoopConfigurationDirectory, "core-site.xml");
        addResourceIfExists(conf, hadoopConfigurationDirectory, "hdfs-site.xml");

        return conf;
    }

    public static FileSystem getFileSystem() throws IOException {
        return  FileSystem.newInstance(HadoopUtils.getHadoopConfiguration(getHadoopConfigurationDirectoryToUse()));
    }
}
