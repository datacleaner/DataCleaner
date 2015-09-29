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
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HdfsUtils {

    public static Configuration getHadoopConfiguration(URI uri) {
        final Configuration conf = new Configuration();
        conf.set("fs.defaultFS", uri.toString());
        return conf;
    }

    public static FileSystem getFileSystemFromUri(URI uri) {
        try {
            URI baseUri = UriBuilder.fromUri(uri).replacePath("/").build();
            return FileSystem.newInstance(getHadoopConfiguration(baseUri));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static FileSystem getFileSystemFromPath(Path path){
        return getFileSystemFromUri(path.toUri());
    }
}
