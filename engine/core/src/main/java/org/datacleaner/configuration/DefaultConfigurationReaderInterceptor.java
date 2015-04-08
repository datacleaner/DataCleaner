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
import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Resource;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.repository.file.FileRepositoryFolder;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.convert.ClasspathResourceTypeHandler;
import org.datacleaner.util.convert.FileResourceTypeHandler;
import org.datacleaner.util.convert.ResourceConverter;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;
import org.datacleaner.util.convert.UrlResourceTypeHandler;
import org.datacleaner.util.convert.VfsResourceTypeHandler;

/**
 * Defines a default implementation of the
 * {@link ConfigurationReaderInterceptor} interface. This implementation does
 * not intercept or perform any special treatment when invoked.
 */
public class DefaultConfigurationReaderInterceptor implements ConfigurationReaderInterceptor {

    @Override
    public String createFilename(String filename) {
        if (filename == null) {
            return null;
        }

        final File file;
        {
            final File nonParentCandidate = new File(filename);
            if (nonParentCandidate.isAbsolute()) {
                file = nonParentCandidate;
            } else {
                final File relativeParentDirectory = getRelativeParentDirectory();
                if (relativeParentDirectory == null) {
                    file = nonParentCandidate;
                } else {
                    file = new File(relativeParentDirectory, filename);
                }
            }
        }

        // some normalization (because filenames are often used to compare
        // datastores)
        String path = file.getPath();
        path = StringUtils.replaceAll(path, "\\", "/");
        if (path.startsWith("./")) {
            path = path.substring(2);
        }
        return path;
    }

    @Override
    public Resource createResource(String resourceUrl) {
        final ResourceConverter converter = new ResourceConverter(getResourceTypeHandlers(),
                ResourceConverter.DEFAULT_DEFAULT_SCHEME);
        final Resource resource = converter.fromString(Resource.class, resourceUrl);
        return resource;
    }

    /**
     * Creates a list of {@link ResourceTypeHandler}s. Subclasses can optionally
     * override this method and add more handlers to the list.
     * 
     * @return
     */
    protected List<ResourceTypeHandler<?>> getResourceTypeHandlers() {
        final List<ResourceTypeHandler<?>> handlers = new ArrayList<ResourceTypeHandler<?>>();
        handlers.add(new FileResourceTypeHandler(getHomeFolder()));
        handlers.add(new UrlResourceTypeHandler());
        handlers.add(new ClasspathResourceTypeHandler());
        handlers.add(new VfsResourceTypeHandler());
        return handlers;
    }

    /**
     * Returns the parent directory of relative files. Can be overridden by
     * subclasses to specify a "root" of the relative files loaded.
     * 
     * @return
     */
    protected File getRelativeParentDirectory() {
        final RepositoryFolder homeFolder = getHomeFolder();
        if (homeFolder instanceof FileRepositoryFolder) {
            final File relativeParentDirectory = ((FileRepositoryFolder) homeFolder).getFile();
            return relativeParentDirectory;
        }
        return null;
    }

    @Override
    public String getTemporaryStorageDirectory() {
        return FileHelper.getTempDir().getAbsolutePath();
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    @Override
    public String getPropertyOverride(String variablePath) {
        String result = System.getProperty(variablePath);
        return result;
    }

    @Override
    public RepositoryFolder getHomeFolder() {
        return DataCleanerConfigurationImpl.defaultHomeFolder();
    }

    @Override
    public DataCleanerEnvironment createBaseEnvironment() {
        return new DataCleanerEnvironmentImpl();
    }
}
