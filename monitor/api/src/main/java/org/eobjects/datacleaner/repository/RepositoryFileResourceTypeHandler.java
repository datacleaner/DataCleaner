/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.repository;

import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.analyzer.util.convert.ResourceConverter.ResourceTypeHandler;
import org.apache.metamodel.util.Resource;

/**
 * {@link ResourceTypeHandler} for {@link RepositoryFileResource}s.
 * 
 * Unlike most other {@link ResourceTypeHandler}s, this one is contextual, since
 * it needs a reference to the repository of the system and it is also specific
 * to a single tenant to ensure isolation between loaded repository content.
 */
public class RepositoryFileResourceTypeHandler implements ResourceTypeHandler<RepositoryFileResource> {

    private final String _tenantId;
    private final Repository _repository;

    public RepositoryFileResourceTypeHandler(Repository repository, String tenantId) {
        _tenantId = tenantId;
        _repository = repository;
    }
    
    public String getTenantId() {
        return _tenantId;
    }

    @Override
    public boolean isParserFor(Class<? extends Resource> cls) {
        return ReflectionUtils.is(cls, RepositoryFileResource.class);
    }

    @Override
    public String createPath(Resource resource) {
        final RepositoryFileResource repositoryFileResource = (RepositoryFileResource) resource;
        final String qualifiedPath = repositoryFileResource.getQualifiedPath();
        final String tenantPart = "/" + _tenantId + "/";
        if (!qualifiedPath.startsWith(tenantPart)) {
            throw new IllegalArgumentException(
                    "This RepositoryFileResourceTypeHandler can only handle repository file from tenant '" + _tenantId
                            + "'. Got: " + qualifiedPath);
        }

        final String relativePath = qualifiedPath.substring(tenantPart.length());

        return relativePath;
    }

    @Override
    public String getScheme() {
        return "repo";
    }

    @Override
    public RepositoryFileResource parsePath(String path) {
        final String qualifiedPath = "/" + _tenantId + "/" + path;
        return new RepositoryFileResource(_repository, qualifiedPath);
    }

}
