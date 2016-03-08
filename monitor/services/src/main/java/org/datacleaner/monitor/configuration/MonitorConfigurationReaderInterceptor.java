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
import java.util.List;

import org.datacleaner.configuration.ConfigurationReaderInterceptor;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.configuration.DataCleanerHomeFolder;
import org.datacleaner.configuration.DefaultConfigurationReaderInterceptor;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.file.FileRepository;
import org.datacleaner.util.convert.RepositoryFileResourceTypeHandler;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;

/**
 * {@link ConfigurationReaderInterceptor} with overrides suitable for the
 * DataCleaner monitor webapp.
 */
public class MonitorConfigurationReaderInterceptor extends DefaultConfigurationReaderInterceptor {

    // defines the basis of the configuration for all tenants - shared task
    // runner (thread pool) and shared descriptor provider (classpath scanning
    // only done once)
    private static final DataCleanerEnvironmentImpl BASE_ENVIRONMENT = new DataCleanerEnvironmentImpl().withTaskRunner(
            new SharedTaskRunner()).withDescriptorProvider(new SharedDescriptorProvider());

    private final TenantContext _tenantContext;
    private final Repository _repository;
    
    public static DataCleanerEnvironmentImpl createBaseEnvironment(InjectionManagerFactory injectionManagerFactory) {
        return BASE_ENVIRONMENT.withInjectionManagerFactory(injectionManagerFactory);
    }

    public MonitorConfigurationReaderInterceptor(Repository repository, TenantContext tenantContext,
            InjectionManagerFactory injectionManagerFactory) {
        super(createBaseEnvironment(injectionManagerFactory));
        _repository = repository;
        _tenantContext = tenantContext;
    }

    /**
     * 
     * @param repository
     * @param tenantContext
     * @param environment
     * 
     * @deprecated use
     *             {@link #MonitorConfigurationReaderInterceptor(Repository, TenantContext)}
     *             instead
     */
    @Deprecated
    public MonitorConfigurationReaderInterceptor(Repository repository, TenantContext tenantContext,
            DataCleanerEnvironment environment) {
        this(repository, tenantContext, environment.getInjectionManagerFactory());
    }

    @Override
    protected File getRelativeParentDirectory() {
        if (_repository instanceof FileRepository) {
            return super.getRelativeParentDirectory();
        }

        // repository is not file based - we suggest to use a tenant-specific
        // folder inside user home.
        final String userHome = System.getProperty("user.home");
        final String result = userHome + File.separator + ".datacleaner/repository/" + _tenantContext.getTenantId();
        return new File(result);
    }

    @Override
    protected List<ResourceTypeHandler<?>> getExtraResourceTypeHandlers() {
        List<ResourceTypeHandler<?>> handlers = super.getExtraResourceTypeHandlers();
        handlers.add(new RepositoryFileResourceTypeHandler(_repository, _tenantContext.getTenantId()));
        return handlers;
    }

    @Override
    public DataCleanerHomeFolder getHomeFolder() {
        return new TenantHomeFolder(_tenantContext.getTenantRootFolder());
    }
}
