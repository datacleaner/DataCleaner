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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Func;
import org.apache.metamodel.util.Resource;
import org.datacleaner.util.FileResolver;
import org.datacleaner.util.convert.ClasspathResourceTypeHandler;
import org.datacleaner.util.convert.FileResourceTypeHandler;
import org.datacleaner.util.convert.HdfsResourceTypeHandler;
import org.datacleaner.util.convert.ResourceConverter;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;
import org.datacleaner.util.convert.UrlResourceTypeHandler;
import org.datacleaner.util.convert.VfsResourceTypeHandler;

import com.google.common.collect.Maps;

/**
 * Defines a default implementation of the
 * {@link ConfigurationReaderInterceptor} interface. This implementation does
 * not intercept or perform any special treatment when invoked.
 */
public class DefaultConfigurationReaderInterceptor implements ConfigurationReaderInterceptor {

    private final Map<String, String> _propertyOverrides;

    public DefaultConfigurationReaderInterceptor() {
        this((Resource) null);
    }

    public DefaultConfigurationReaderInterceptor(Map<String, String> propertyOverrides) {
        if (propertyOverrides == null) {
            _propertyOverrides = Collections.emptyMap();
        } else {
            _propertyOverrides = propertyOverrides;
        }
    }

    public DefaultConfigurationReaderInterceptor(Resource propertiesResource) {
        if (propertiesResource == null || !propertiesResource.isExists()) {
            _propertyOverrides = Collections.emptyMap();
        } else {
            _propertyOverrides = propertiesResource.read(new Func<InputStream, Map<String, String>>() {
                @Override
                public Map<String, String> eval(InputStream in) {
                    final Properties properties = new Properties();
                    try {
                        properties.load(in);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    final HashMap<String, String> map = Maps.newHashMapWithExpectedSize(properties.size());
                    for (Entry<?,?> e : properties.entrySet()) {
                        final String key = (String) e.getKey();
                        final String value = (String) e.getValue();
                        map.put(key, value);
                    }
                    return map;
                }
            });
        }
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
        handlers.add(new HdfsResourceTypeHandler());
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
        return new DataCleanerEnvironmentImpl();
    }
}
