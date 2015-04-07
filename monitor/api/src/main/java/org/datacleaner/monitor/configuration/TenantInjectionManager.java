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
package org.datacleaner.monitor.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.configuration.InjectionPoint;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryFileResource;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.repository.file.FileRepositoryFolder;
import org.datacleaner.util.convert.ClasspathResourceTypeHandler;
import org.datacleaner.util.convert.FileResourceTypeHandler;
import org.datacleaner.util.convert.RepositoryFileResourceTypeHandler;
import org.datacleaner.util.convert.ResourceConverter;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;
import org.datacleaner.util.convert.UrlResourceTypeHandler;
import org.datacleaner.util.convert.VfsResourceTypeHandler;

/**
 * A {@link InjectionManager} wrapper that is tenant-aware.
 * 
 * TODO: This class only services to fix issues with resource loading of
 * {@link RepositoryFileResource}s. That stuff should be generalized since
 * {@link RepositoryFolder} is now a generally used thing.
 */
public class TenantInjectionManager implements InjectionManager {

    private final InjectionManager _delegate;
    private final TenantContext _tenantContext;
    private final Repository _repository;

    public TenantInjectionManager(InjectionManager delegate, Repository repository, TenantContext tenantContext) {
        _delegate = delegate;
        _repository = repository;
        _tenantContext = tenantContext;
    }

    public String getTenantId() {
        return _tenantContext.getTenantId();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E getInstance(InjectionPoint<E> injectionPoint) {
        final Class<E> baseType = injectionPoint.getBaseType();
        if (baseType == ResourceConverter.class) {
            return (E) createResourceConverter();
        }
        return _delegate.getInstance(injectionPoint);
    }

    private ResourceConverter createResourceConverter() {
        List<ResourceTypeHandler<?>> handlers = new ArrayList<ResourceTypeHandler<?>>();
        handlers.add(new FileResourceTypeHandler(getRelativeParentDirectory()));
        handlers.add(new UrlResourceTypeHandler());
        handlers.add(new ClasspathResourceTypeHandler());
        handlers.add(new VfsResourceTypeHandler());

        String tenantId = _tenantContext.getTenantId();

        handlers.add(new RepositoryFileResourceTypeHandler(_repository, tenantId));

        return new ResourceConverter(handlers, "repo");
    }

    private File getRelativeParentDirectory() {
        final RepositoryFolder tenantRootFolder = _tenantContext.getTenantRootFolder();
        if (tenantRootFolder instanceof FileRepositoryFolder) {
            return ((FileRepositoryFolder) tenantRootFolder).getFile();
        }
        return null;
    }
}
