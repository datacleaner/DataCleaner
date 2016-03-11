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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Resource;
import org.datacleaner.util.FileResolver;
import org.datacleaner.util.InputStreamToPropertiesMapFunc;
import org.datacleaner.util.convert.ResourceConverter;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;

/**
 * Defines a default implementation of the
 * {@link ConfigurationReaderInterceptor} interface. This implementation does
 * not intercept or perform any special treatment when invoked.
 */
public class DefaultConfigurationReaderInterceptor implements ConfigurationReaderInterceptor {
private final Map<String, String> _propertyOverrides;
    private final DataCleanerEnvironment _baseEnvironment;

    public DefaultConfigurationReaderInterceptor() {
        this((Resource) null);
    }

    public DefaultConfigurationReaderInterceptor(DataCleanerEnvironment baseEnvironment) {
        this((Resource) null, baseEnvironment);
    }

    public DefaultConfigurationReaderInterceptor(Map<String, String> propertyOverrides) {
        this(propertyOverrides, new DataCleanerEnvironmentImpl());
    }

    public DefaultConfigurationReaderInterceptor(Map<String, String> propertyOverrides,
            DataCleanerEnvironment baseEnvironment) {
        if (propertyOverrides == null) {
            _propertyOverrides = Collections.emptyMap();
        } else {
            _propertyOverrides = propertyOverrides;
        }
        _baseEnvironment = baseEnvironment;
    }

    public DefaultConfigurationReaderInterceptor(Resource propertiesResource) {
        this(propertiesResource, new DataCleanerEnvironmentImpl());
    }

    public DefaultConfigurationReaderInterceptor(Resource propertiesResource, DataCleanerEnvironment baseEnvironment) {
        if (propertiesResource == null || !propertiesResource.isExists()) {
            _propertyOverrides = Collections.emptyMap();
        } else {
            _propertyOverrides = propertiesResource.read(new InputStreamToPropertiesMapFunc());
        }
        _baseEnvironment = baseEnvironment;
    }

    @Override
    public final String createFilename(String filename) {
        if (filename == null) {
            return null;
        }

        // pass it through the file resolver to apply relative resolving of the
        // file and path normalization
        final FileResolver resolver = createFileResolver();
        final File file = resolver.toFile(filename);
        return resolver.toPath(file);
    }

    protected FileResolver createFileResolver() {
        return new FileResolver(getHomeFolder());
    }

    @Override
    public Resource createResource(String resourceUrl, DataCleanerConfiguration temporaryConfiguration) {
        final ResourceConverter converter = new ResourceConverter(temporaryConfiguration,
                ResourceConverter.getConfiguredDefaultScheme()).withExtraHandlers(getExtraResourceTypeHandlers());

        final Resource resource = converter.fromString(Resource.class, resourceUrl);
        return resource;
    }

    /**
     * Creates a list of {@link ResourceTypeHandler}s. Subclasses can optionally
     * override this method and add more handlers to the list.
     * 
     * @return
     */
    protected List<ResourceTypeHandler<?>> getExtraResourceTypeHandlers() {
        return new ArrayList<>();
    }

    /**
     * Returns the parent directory of relative files. Can be overridden by
     * subclasses to specify a "root" of the relative files loaded.
     * 
     * @return
     */
    protected File getRelativeParentDirectory() {
        return getHomeFolder().toFile();
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
        String result = _propertyOverrides.get(variablePath);
        if (result == null) {
            result = System.getProperty(variablePath);
        }
        return result;
    }

    @Override
    public DataCleanerHomeFolder getHomeFolder() {
        return DataCleanerConfigurationImpl.defaultHomeFolder();
    }

    @Override
    public DataCleanerEnvironment createBaseEnvironment() {
        return _baseEnvironment;
    }
}
