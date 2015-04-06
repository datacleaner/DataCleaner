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

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.repository.Repository;

/**
 * An {@link InjectionManagerFactory} wrapper that is tenant-aware. Useful for
 * injections that contain components that are tenant-specific.
 */
public class TenantInjectionManagerFactory implements InjectionManagerFactory {

    private final InjectionManagerFactory _delegate;
    private final TenantContext _tenantContext;
    private final Repository _repository;

    public TenantInjectionManagerFactory(InjectionManagerFactory delegate, Repository repository,
            TenantContext tenantContext) {
        _delegate = delegate;
        _repository = repository;
        _tenantContext = tenantContext;
    }
    
    @Override
    public InjectionManager getInjectionManager(DataCleanerConfiguration configuration) {
        return getInjectionManager(configuration, null);
    }

    @Override
    public InjectionManager getInjectionManager(DataCleanerConfiguration configuration, AnalysisJob job) {
        final InjectionManager delegateInjectionManager = _delegate.getInjectionManager(configuration, job);
        return new TenantInjectionManager(delegateInjectionManager, _repository, _tenantContext);
    }

}
