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
import org.apache.metamodel.util.InMemoryResource;
import org.apache.metamodel.util.Resource;

/**
 * A {@link ResourceTypeHandler} that handles resource with the "repo" scheme,
 * produced by the DataCleaner monitor. Since the desktop client is not
 * connected to the monitor repository, we will only serve empty
 * {@link InMemoryResource}s for these requests. While this may cause data to be
 * missing from these resources, it does cover the basic scenarios of being able
 * to load jobs etc.
 */
public class DummyRepositoryResourceFileTypeHandler implements ResourceTypeHandler<InMemoryResource> {

    @Override
    public String createPath(Resource res) {
        InMemoryResource resource = (InMemoryResource) res;
        return resource.getPath();
    }

    @Override
    public String getScheme() {
        return "repo";
    }

    @Override
    public boolean isParserFor(Class<? extends Resource> resourceClass) {
        return ReflectionUtils.is(resourceClass, InMemoryResource.class);
    }

    @Override
    public InMemoryResource parsePath(String path) {
        return new InMemoryResource(path);
    }

}
