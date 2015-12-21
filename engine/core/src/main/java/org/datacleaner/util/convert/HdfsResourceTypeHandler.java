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
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;

/**
 * {@link ResourceTypeHandler} for {@link HdfsResource} aka files on Hadoop
 * HDFS.
 */
public class HdfsResourceTypeHandler implements ResourceTypeHandler<HdfsResource> {

    private final String _scheme;

    /**
     * Default constructor for the "hdfs" scheme. Use of this constructor is
     * discouraged since we support now many other schemes.
     * 
     * @deprecated use {@link #HdfsResourceTypeHandler(String)} instead
     */
    public HdfsResourceTypeHandler() {
        this("hdfs");
    }

    /**
     * Creates a {@link HdfsResourceTypeHandler} for a particular scheme.
     * 
     * @param scheme
     *            a scheme such as "hdfs", "emrfs", "maprfs" etc.
     */
    public HdfsResourceTypeHandler(String scheme) {
        _scheme = scheme;
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
    public HdfsResource parsePath(String path) {
        final String prefix = getScheme() + "://";
        if (!path.startsWith(prefix)) {
            path = prefix + path;
        }
        return new HdfsResource(path);
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
