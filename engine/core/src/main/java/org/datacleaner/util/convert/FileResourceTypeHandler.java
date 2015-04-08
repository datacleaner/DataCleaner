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
package org.datacleaner.util.convert;

import java.io.File;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.repository.file.FileRepository;
import org.datacleaner.repository.file.FileRepositoryFolder;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;

/**
 * {@link ResourceTypeHandler} for {@link FileResource}s.
 */
public class FileResourceTypeHandler implements ResourceTypeHandler<FileResource> {

    private static final Logger logger = LoggerFactory.getLogger(FileResourceTypeHandler.class);

    /**
     * The default scheme value for a {@link FileResourceTypeHandler}.
     */
    public static final String DEFAULT_SCHEME = "file";

    private final String _scheme;
    private final RepositoryFolder _homeFolder;

    /**
     * Construct a {@link FileResourceTypeHandler} using defaults.
     * 
     * @deprecated use another constructor instead
     */
    @Deprecated
    public FileResourceTypeHandler() {
        this(DataCleanerConfigurationImpl.defaultHomeFolder());
    }

    public FileResourceTypeHandler(DataCleanerConfiguration configuration) {
        this(configuration.getHomeFolder());
    }

    /**
     * Constructs a {@link FileResourceTypeHandler} using a specified parent
     * directory for relative paths.
     * 
     * @param relativeParentDirectory
     */
    public FileResourceTypeHandler(File relativeParentDirectory) {
        this(DEFAULT_SCHEME, relativeParentDirectory);
    }

    public FileResourceTypeHandler(RepositoryFolder homeFolder) {
        this(DEFAULT_SCHEME, homeFolder);
    }

    public FileResourceTypeHandler(String scheme, RepositoryFolder homeFolder) {
        _scheme = scheme;
        _homeFolder = homeFolder;
    }

    /**
     * Constructs a {@link FileResourceTypeHandler} using a specified parent
     * directory for relative paths.
     * 
     * @param scheme
     *            the scheme of this resource type, e.g. "file"
     * @param relativeParentDirectory
     */
    public FileResourceTypeHandler(String scheme, File relativeParentDirectory) {
        _scheme = scheme;
        _homeFolder = new FileRepository(relativeParentDirectory);
    }

    @Override
    public boolean isParserFor(Class<? extends Resource> resourceType) {
        return ReflectionUtils.is(resourceType, FileResource.class);
    }

    @Override
    public String getScheme() {
        return _scheme;
    }

    @Override
    public FileResource parsePath(String path) {
        final File file;
        if (isAbsolute(path)) {
            file = new File(path);
        } else {
            final File directory;
            if (_homeFolder instanceof FileRepositoryFolder) {
                directory = ((FileRepositoryFolder) _homeFolder).getFile();
            } else {
                logger.warn("Home folder '{}' is not file-based, using default home folder (.)", _homeFolder);
                directory = DataCleanerConfigurationImpl.defaultHomeFolder().getFile();
            }
            file = new File(directory, path);
        }

        return new FileResource(file);
    }

    private boolean isAbsolute(String path) {
        return new File(path).isAbsolute();
    }

    @Override
    public String createPath(Resource resource) {
        String path = ((FileResource) resource).getFile().getPath();
        path = path.replaceAll("\\\\", "/");
        return path;
    }

}
