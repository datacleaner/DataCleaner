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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.metamodel.util.HdfsResource;
import org.apache.metamodel.util.Resource;
import org.datacleaner.api.Converter;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A converter for {@link Resource}s. Because of different {@link Resource}
 * implementations, this converter delegates to a number of 'handlers' which
 * implement part of the conversion for a specific type of resource.
 */
public class ResourceConverter implements Converter<Resource> {

    private static final Logger logger = LoggerFactory.getLogger(ResourceConverter.class);

    /**
     * Represents the default "default scheme", for representations that does
     * not have a scheme in the path. This default scheme is "file".
     * 
     * @deprecated use {@link #getConfiguredDefaultScheme()} by as a way to
     *             access this
     */
    @Deprecated
    public static final String DEFAULT_DEFAULT_SCHEME = FileResourceTypeHandler.DEFAULT_SCHEME;

    private static final Pattern RESOURCE_PATTERN = Pattern.compile("\\b([a-zA-Z]+)://(.+)");
    private final DataCleanerConfiguration _configuration;

    /**
     * Gets the "default scheme" (see {@link #DEFAULT_DEFAULT_SCHEME}) while
     * taking into account that this may have been configured via
     * {@link SystemProperties#DEFAULT_RESOURCE_SCHEME}.
     * 
     * @return
     */
    public static String getConfiguredDefaultScheme() {
        return SystemProperties.getString(SystemProperties.DEFAULT_RESOURCE_SCHEME, DEFAULT_DEFAULT_SCHEME);
    }

    private static List<ResourceTypeHandler<?>> createDefaultHandlers(DataCleanerConfiguration configuration) {
        final List<ResourceTypeHandler<?>> result = new ArrayList<>();
        result.add(new FileResourceTypeHandler(configuration.getHomeFolder()));
        result.add(new UrlResourceTypeHandler());
        result.add(new HdfsResourceTypeHandler(HdfsResource.SCHEME_HDFS, configuration));
        result.add(new HdfsResourceTypeHandler(HdfsResource.SCHEME_EMRFS, configuration));
        result.add(new HdfsResourceTypeHandler(HdfsResource.SCHEME_MAPRFS, configuration));
        result.add(new HdfsResourceTypeHandler(HdfsResource.SCHEME_S3, configuration));
        result.add(new HdfsResourceTypeHandler(HdfsResource.SCHEME_SWIFT, configuration));
        result.add(new ClasspathResourceTypeHandler());
        result.add(new VfsResourceTypeHandler());
        return result;
    }

    public ResourceConverter withExtraHandlers(Collection<? extends ResourceTypeHandler<?>> extraHandlers) {
        final List<ResourceTypeHandler<?>> handlers = new ArrayList<>(_parsers.values());
        handlers.addAll(extraHandlers);

        return new ResourceConverter(_configuration, handlers);
    }

    /**
     * Represents a component capable of handling the parsing and serializing of
     * a single type of resource.
     */
    public interface ResourceTypeHandler<E extends Resource> {
        public boolean isParserFor(Class<? extends Resource> resourceType);

        public String getScheme();

        public E parsePath(String path);

        public String createPath(Resource resource);
    }

    /**
     * Represents the parsed structure of a serialized resource
     */
    public static class ResourceStructure {
        private final String scheme;
        private final String path;

        public ResourceStructure(String scheme, String path) {
            this.scheme = scheme;
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public String getScheme() {
            return scheme;
        }
    }

    private final Map<String, ResourceTypeHandler<?>> _parsers;
    private final String _defaultScheme;

    public ResourceConverter(DataCleanerConfiguration configuration) {
        this(configuration, getConfiguredDefaultScheme());
    }

    public ResourceConverter(DataCleanerConfiguration configuration, String defaultScheme) {
        this(configuration, createDefaultHandlers(configuration), defaultScheme);
    }

    public ResourceConverter(DataCleanerConfiguration configuration, Collection<? extends ResourceTypeHandler<?>> handlers) {
        this(configuration, handlers, getConfiguredDefaultScheme());
    }

    public ResourceConverter(Collection<? extends ResourceTypeHandler<?>> handlers, String defaultScheme) {
        this(new DataCleanerConfigurationImpl(), handlers, defaultScheme);
    }

    /**
     * Constructs a {@link ResourceConverter} using a set of handlers.
     *
     * @param configuration
     * @param handlers
     * @param defaultScheme
     */
    public ResourceConverter(DataCleanerConfiguration configuration, Collection<? extends ResourceTypeHandler<?>> handlers, String defaultScheme) {
        _defaultScheme = defaultScheme;
        _parsers = new ConcurrentHashMap<>();
        _configuration = configuration;

        for (ResourceTypeHandler<?> handler : handlers) {
            String scheme = handler.getScheme();
            _parsers.put(scheme, handler);
        }
    }

    public ResourceConverter(ResourceTypeHandler<?>... handlers) {
        this(new DataCleanerConfigurationImpl(), Arrays.asList(handlers), getConfiguredDefaultScheme());
    }
    
    @Override
    public Resource fromString(Class<?> type, String serializedForm) {
        final ResourceStructure structure = parseStructure(serializedForm);
        if (structure == null) {
            throw new IllegalStateException("Invalid resource format: " + serializedForm);
        }
        final String scheme = structure.getScheme();
        final ResourceTypeHandler<?> handler = _parsers.get(scheme);
        if (handler == null) {
            throw new IllegalStateException("No handler found for scheme of resource: " + serializedForm);
        }
        final Resource resource = handler.parsePath(structure.getPath());
        return resource;
    }

    @Override
    public String toString(Resource resource) {
        final Class<? extends Resource> resourceType = resource.getClass();
        final Collection<ResourceTypeHandler<?>> values = _parsers.values();
        for (ResourceTypeHandler<?> handler : values) {
            if (handler.isParserFor(resourceType)) {
                final String path = handler.createPath(resource);
                final String scheme = handler.getScheme();
                return scheme + "://" + path;
            }
        }
        throw new IllegalStateException("Could not find a resource handler for resource: " + resource);
    }

    public Collection<ResourceTypeHandler<?>> getResourceTypeHandlers() {
        return Collections.unmodifiableCollection(_parsers.values());
    }

    @Override
    public boolean isConvertable(Class<?> type) {
        return ReflectionUtils.is(type, Resource.class);
    }

    /**
     * Parses a string in order to produce a {@link ResourceStructure} object
     * 
     * @param str
     * @return
     */
    public ResourceStructure parseStructure(String str) {
        Matcher matcher = RESOURCE_PATTERN.matcher(str);
        if (!matcher.find()) {
            logger.info("Did not find any scheme definition in resource path: {}. Using default scheme: {}.", str,
                    _defaultScheme);
            return new ResourceStructure(_defaultScheme, str);
        }
        String scheme = matcher.group(1);
        String path = matcher.group(2);
        return new ResourceStructure(scheme, path);
    }
}
