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
package org.datacleaner.configuration;

import java.io.File;

import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.repository.file.FileRepositoryFolder;
import org.datacleaner.util.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default (immutable) implementation of {@link DataCleanerHomeFolder}.
 */
public class DataCleanerHomeFolderImpl implements DataCleanerHomeFolder {

    private static final Logger logger = LoggerFactory.getLogger(DataCleanerHomeFolder.class);

    private final RepositoryFolder _repositoryFolder;
    private final File _file;

    public DataCleanerHomeFolderImpl(RepositoryFolder repositoryFolder) {
        if (repositoryFolder == null) {
            throw new IllegalArgumentException("RepositoryFolder cannot be null");
        }
        _repositoryFolder = repositoryFolder;
        if (repositoryFolder instanceof FileRepositoryFolder) {
            _file = ((FileRepositoryFolder) repositoryFolder).getFile();
            logger.debug("RepositoryFolder is file-based, using file representation: {}", _file);
        } else {
            _file = new File(SystemProperties.getString("user.home", ".") + "/.datacleaner");
            _file.mkdirs();
            logger.warn("RepositoryFolder is NOT file-based, using file representation: {}", _file);
        }
    }

    @Override
    public File toFile() {
        return _file;
    }

    @Override
    public RepositoryFolder toRepositoryFolder() {
        return _repositoryFolder;
    }

    @Override
    public String toString() {
        return _repositoryFolder.toString();
    }
}
