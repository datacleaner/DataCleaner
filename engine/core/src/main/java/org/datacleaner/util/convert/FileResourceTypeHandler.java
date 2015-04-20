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

import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerHomeFolder;
import org.datacleaner.util.FileResolver;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;

/**
 * {@link ResourceTypeHandler} for {@link FileResource}s.
 */
public class FileResourceTypeHandler implements ResourceTypeHandler<FileResource> {

    /**
     * The default scheme value for a {@link FileResourceTypeHandler}.
     */
    public static final String DEFAULT_SCHEME = "file";

    private final String _scheme;
    private final File _homeFolder;

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
    
    public FileResourceTypeHandler(DataCleanerHomeFolder homeFolder) {
        this(homeFolder.toFile());
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
        _homeFolder = relativeParentDirectory;
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
        final FileResolver fileResolver = new FileResolver(_homeFolder);
        final File file = fileResolver.toFile(path);
        return new FileResource(file);
    }

    @Override
    public String createPath(Resource resource) {
        final File file = ((FileResource) resource).getFile();
        final FileResolver fileResolver = new FileResolver(_homeFolder);
        return fileResolver.toPath(file);
    }

}
