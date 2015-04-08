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

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.repository.file.FileRepositoryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for resolving {@link File}s based on their path and a
 * base-directory and vice-versa.
 */
public class FileResolver {

    private static final Logger logger = LoggerFactory.getLogger(FileResolver.class);
    private final File _baseDir;

    public FileResolver(DataCleanerConfiguration configuration) {
        this(configuration == null ? null : configuration.getHomeFolder());
    }

    public FileResolver(RepositoryFolder homeFolder) {
        if (homeFolder instanceof FileRepositoryFolder) {
            _baseDir = ((FileRepositoryFolder) homeFolder).getFile();
        } else {
            logger.info("Home folder '{}' is not file-based, using default home folder (.)", homeFolder);
            _baseDir = DataCleanerConfigurationImpl.defaultHomeFolder().getFile();
        }
    }

    public FileResolver(File baseDir) {
        if (baseDir == null) {
            throw new IllegalArgumentException("Base directory cannot be null");
        }
        _baseDir = baseDir;
    }

    public File getBaseDirectory() {
        return _baseDir;
    }

    public File toFile(String filename) {
        if (filename == null) {
            return null;
        }

        final File file;
        final File nonParentCandidate = new File(filename);
        if (nonParentCandidate.isAbsolute()) {
            file = nonParentCandidate;
        } else {
            if (_baseDir == null || ".".equals(_baseDir.getPath())) {
                file = nonParentCandidate;
            } else {
                file = new File(_baseDir, filename);
            }
        }
        return file;
    }

    public String toPath(File file) {
        if (file == null) {
            return null;
        }

        String path = file.getPath();

        // Make relative if possible
        final String basePath = FilenameUtils.normalize(_baseDir.getAbsolutePath(), true);
        final String filePath = FilenameUtils.normalize(file.getAbsolutePath(), true);

        final boolean absolute;
        if (filePath.startsWith(basePath)) {
            path = filePath.substring(basePath.length());
            absolute = false;
        } else {
            absolute = file.isAbsolute();
        }

        path = StringUtils.replaceAll(path, "\\", "/");
        if (!absolute) {
            // some normalization (because filenames are often used to compare
            // datastores)
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (path.startsWith("./")) {
                path = path.substring(2);
            }
        }
        return path;
    }
}
