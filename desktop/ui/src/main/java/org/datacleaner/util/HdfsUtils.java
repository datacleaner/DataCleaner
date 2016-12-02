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
package org.datacleaner.util;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.datacleaner.server.HadoopClusterInformation;

public class HdfsUtils {

    public static Configuration getHadoopConfiguration(final URI uri) {
        final Configuration conf = getHadoopConfigurationWithTimeout(null);
        conf.set("fs.defaultFS", uri.toString());
        return conf;
    }

    public static FileSystem getFileSystemFromUri(final URI uri) {
        try {
            final URI baseUri = UriBuilder.fromUri(uri).replacePath("/").build();
            return FileSystem.newInstance(getHadoopConfiguration(baseUri));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static FileSystem getFileSystemFromPath(final Path path) {
        return getFileSystemFromUri(path.toUri());
    }

    public static Configuration getHadoopConfigurationWithTimeout(final HadoopClusterInformation clusterInformation) {
        Configuration configuration = null;

        if (clusterInformation == null) {
            configuration = new Configuration();
        } else {
            configuration = clusterInformation.getConfiguration();
        }

        configuration.set(CommonConfigurationKeysPublic.IPC_CLIENT_CONNECT_MAX_RETRIES_ON_SOCKET_TIMEOUTS_KEY,
                String.valueOf(1));
        return configuration;
    }
}
