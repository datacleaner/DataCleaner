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

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

/**
 * Environment based configuration
 */
public class DirectoryBasedHadoopClusterInformation extends AbstractServerInformation
        implements HadoopClusterInformation {
    private static final long serialVersionUID = 1L;
    private final String[] _directories;

    public DirectoryBasedHadoopClusterInformation(final String name, final String description, String... paths) {
        super(name, description);
        _directories = paths;
    }

    @Override
    public Configuration getConfiguration() {
        final Configuration configuration = new Configuration();
        final Map<String, File> configurationFiles = new HashMap<>();

        Arrays.stream(getDirectories()).map(File::new).filter(File::isDirectory).forEach(c -> {
            final File[] array = c.listFiles();
            assert (array != null);
            Arrays.stream(array).filter(File::isFile).filter(f -> !configurationFiles.containsKey(f.getName()))
                    .filter(f -> FilenameUtils.getExtension(f.getName()).equalsIgnoreCase("xml"))
                    .forEach(f -> configurationFiles.put(f.getName(), f));
        });

        if (configurationFiles.size() == 0) {
            throw new IllegalStateException(
                    "Specified directories does not contain any Hadoop configuration files");
        }

        configurationFiles.values().stream().map(File::toURI).map(Path::new).forEach(configuration::addResource);

        return configuration;
    }

    public String[] getDirectories() {
        return _directories;
    }
}
