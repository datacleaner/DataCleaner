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
import org.datacleaner.configuration.DefaultConfigurationReaderInterceptor;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.repository.file.FileRepositoryFolder;
import org.datacleaner.util.FileResolver;
import org.datacleaner.util.convert.RepositoryFileResourceTypeHandler;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ConfigurationReaderInterceptor} with overrides suitable for the
 * DataCleaner monitor webapp.
 */
public class MonitorConfigurationReaderInterceptor extends DefaultConfigurationReaderInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(MonitorConfigurationReaderInterceptor.class);

    private final TenantContext _tenantContext;
    private final DataCleanerEnvironment _environment;
    private final Repository _repository;

    public MonitorConfigurationReaderInterceptor(Repository repository, TenantContext tenantContext,
            DataCleanerEnvironment environment) {
        _repository = repository;
        _tenantContext = tenantContext;
        _environment = environment;
    }

    @Override
    protected File getRelativeParentDirectory() {
        final File relativeParentDirectory = super.getRelativeParentDirectory();
        if (relativeParentDirectory == null) {
            final String userHome = System.getProperty("user.home");
            final String result = userHome + File.separator + ".datacleaner/repository/" + _tenantContext.getTenantId();
            return new File(result);
        }
        return relativeParentDirectory;
    }

    @Override
    protected FileResolver createFileResolver() {
        final RepositoryFolder tenantRootFolder = _tenantContext.getTenantRootFolder();
        if (tenantRootFolder instanceof FileRepositoryFolder) {
            return super.createFileResolver();
        }

        final String userHome = System.getProperty("user.home");
        final String directoryName = userHome + File.separator + ".datacleaner/repository/"
                + _tenantContext.getTenantId();

        logger.warn("Repository is not file-based. Using directory: {}", directoryName);

        return new FileResolver(new File(directoryName));
    }

    @Override
    protected List<ResourceTypeHandler<?>> getResourceTypeHandlers() {
        List<ResourceTypeHandler<?>> handlers = super.getResourceTypeHandlers();
        handlers.add(new RepositoryFileResourceTypeHandler(_repository, _tenantContext.getTenantId()));
        return handlers;
    }

    @Override
    public DataCleanerEnvironment createBaseEnvironment() {
        return _environment;
    }

    @Override
    public RepositoryFolder getHomeFolder() {
        return _tenantContext.getTenantRootFolder();
    }
}
