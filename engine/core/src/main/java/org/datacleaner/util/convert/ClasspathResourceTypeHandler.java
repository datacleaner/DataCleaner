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

import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;
import org.apache.metamodel.util.ClasspathResource;
import org.apache.metamodel.util.Resource;

/**
 * {@link ResourceTypeHandler} for {@link ClasspathResource}s.
 */
public class ClasspathResourceTypeHandler implements ResourceTypeHandler<ClasspathResource> {

    @Override
    public boolean isParserFor(Class<? extends Resource> resourceType) {
        return ReflectionUtils.is(resourceType, ClasspathResource.class);
    }

    @Override
    public String getScheme() {
        return "classpath";
    }

    @Override
    public ClasspathResource parsePath(String path) {
        return new ClasspathResource(path);
    }

    @Override
    public String createPath(Resource resource) {
        final ClasspathResource classpathResource = (ClasspathResource) resource;
        return classpathResource.getResourcePath();
    }

}
