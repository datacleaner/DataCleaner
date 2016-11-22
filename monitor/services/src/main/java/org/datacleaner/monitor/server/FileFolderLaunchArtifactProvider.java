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
package org.datacleaner.monitor.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.datacleaner.util.FileFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link LaunchArtifactProvider} implementation based on an external
 * DataCleaner folder which is used to serve JAR files. All JAR files in this
 * folder are assumed to be signed.
 */
public class FileFolderLaunchArtifactProvider implements LaunchArtifactProvider {

    private static final Logger logger = LoggerFactory.getLogger(FileFolderLaunchArtifactProvider.class);

    private final File _libFolder;

    public FileFolderLaunchArtifactProvider(final File libFolder) {
        _libFolder = libFolder;
    }

    @Override
    public List<String> getJarFilenames() {
        final String[] list = _libFolder.list(FileFilters.JAR);
        if (list == null || list.length == 0) {
            logger.error("No JAR files found in launch artifact folder: {}", _libFolder);
            return Collections.emptyList();
        }
        return Arrays.asList(list);
    }

    @Override
    public InputStream readJarFile(final String filename) throws IllegalArgumentException, IllegalStateException {
        final File file = new File(_libFolder, filename);
        if (file.exists()) {
            try {
                final FileInputStream in = new FileInputStream(file);
                return new BufferedInputStream(in);
            } catch (final IOException e) {
                throw new IllegalStateException("Could not read from file: " + file, e);
            }
        }
        throw new IllegalArgumentException("No such file: " + filename);
    }

    @Override
    public boolean isAvailable() {
        if (_libFolder.exists() && _libFolder.isDirectory()) {
            return true;
        }
        return false;
    }

}
