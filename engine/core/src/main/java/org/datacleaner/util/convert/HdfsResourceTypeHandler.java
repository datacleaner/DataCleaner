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

import org.apache.metamodel.util.HdfsResource;
import org.apache.metamodel.util.Resource;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.util.HadoopResource;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;

/**
 * {@link ResourceTypeHandler} for {@link HdfsResource} aka files on Hadoop
 * HDFS.
 */
public class HdfsResourceTypeHandler implements ResourceTypeHandler<HadoopResource> {
    private final String _scheme;
    private final DataCleanerConfiguration _dataCleanerConfiguration;

    /**
     * Default constructor for the "hdfs" scheme. Use of this constructor is
     * discouraged since we support now many other schemes.
     *
     * @deprecated use {@link #HdfsResourceTypeHandler(String, DataCleanerConfiguration)} instead
     */
    public HdfsResourceTypeHandler() {
        this("hdfs");
    }

    public HdfsResourceTypeHandler(String scheme) {
        this(scheme, new DataCleanerConfigurationImpl());
    }


        /**
         * Creates a {@link HdfsResourceTypeHandler} for a particular scheme.
         *
         * @param scheme
         *            a scheme such as "hdfs", "emrfs", "maprfs" etc.
         */
    public HdfsResourceTypeHandler(String scheme, DataCleanerConfiguration dataCleanerConfiguration) {
        _scheme = scheme;
        _dataCleanerConfiguration = dataCleanerConfiguration;
    }

    @Override
    public boolean isParserFor(Class<? extends Resource> resourceType) {
        return ReflectionUtils.is(resourceType, HdfsResource.class);
    }

    @Override
    public String getScheme() {
        return _scheme;
    }

    @Override
    public HadoopResource parsePath(String path) {
        final String prefix = getScheme() + "://";
        if (!path.startsWith(prefix)) {
            path = prefix + path;
        }

        HadoopResourceBuilder
                builder = new HadoopResourceBuilder(_dataCleanerConfiguration.getServerInformationCatalog(), path);

        return builder.build();
    }

    @Override
    public String createPath(Resource resource) {
        final String prefix = getScheme() + "://";
        String path = resource.getQualifiedPath();
        if (path.startsWith(prefix)) {
            path = path.substring(prefix.length());
        }

        return path;
    }

}
