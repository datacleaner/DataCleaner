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

import org.apache.commons.vfs2.FileObject;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.VFSUtils;
import org.datacleaner.util.VfsResource;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;
import org.apache.metamodel.util.Resource;

public class VfsResourceTypeHandler implements ResourceTypeHandler<VfsResource> {

    @Override
    public boolean isParserFor(Class<? extends Resource> resourceType) {
        return ReflectionUtils.is(resourceType, VfsResource.class);
    }

    @Override
    public String getScheme() {
        return "vfs";
    }

    @Override
    public VfsResource parsePath(String path) {
        try {
            final FileObject fileObject = VFSUtils.getFileSystemManager().resolveFile(path);
            return new VfsResource(fileObject);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String createPath(Resource resource) {
        final VfsResource vfsResource = (VfsResource) resource;
        return vfsResource.getFileObject().getName().getFriendlyURI();
    }

}
