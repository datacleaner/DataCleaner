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

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.monitor.job.JobEngineManager;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Factory and tenant-wise cache for {@link TenantContext} objects.
 */
public class TenantContextFactoryImpl implements TenantContextFactory {

    private static final char[] ILLEGAL_TENANT_ID_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*',
            '\\', '<', '>', '|', '\"', ':' };

    private static final Logger logger = LoggerFactory.getLogger(TenantContextFactoryImpl.class);

    private final LoadingCache<String, TenantContext> _contexts;
    private final Repository _repository;
    private final JobEngineManager _jobEngineManager;
    private final DataCleanerEnvironment _environment;

    /**
     * Constructs a {@link TenantContextFactoryImpl}.
     * 
     * @param repository
     * @deprecated use
     *             {@link #TenantContextFactoryImpl(Repository, DataCleanerEnvironment, JobEngineManager)}
     *             instead.
     */
    @Deprecated
    public TenantContextFactoryImpl(Repository repository) {
        this(repository, new DataCleanerEnvironmentImpl(), null);
    }

    /**
     * 
     * @param repository
     * @param injectionManagerFactory
     * @param jobEngineManager
     * 
     * @deprecated use
     *             {@link #TenantContextFactoryImpl(Repository, DataCleanerEnvironment, JobEngineManager)}
     *             instead
     */
    @Deprecated
    public TenantContextFactoryImpl(Repository repository, InjectionManagerFactory injectionManagerFactory,
            JobEngineManager jobEngineManager) {
        this(repository, new DataCleanerEnvironmentImpl(), jobEngineManager);
    }

    /**
     * Constructs a {@link TenantContextFactoryImpl}.
     * 
     * @param repository
     * @param environment
     * @param jobEngineManager
     */
    @Autowired
    public TenantContextFactoryImpl(Repository repository, DataCleanerEnvironment environment,
            JobEngineManager jobEngineManager) {
        _repository = repository;
        _environment = environment;
        _jobEngineManager = jobEngineManager;
        _contexts = buildTenantContextCache();
    }

    private LoadingCache<String, TenantContext> buildTenantContextCache() {
        LoadingCache<String, TenantContext> cache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS)
                .build(new CacheLoader<String, TenantContext>() {
                    @Override
                    public TenantContext load(String tenantId) throws Exception {
                        logger.info("Initializing tenant context: {}", tenantId);
                        final TenantContext context = new TenantContextImpl(tenantId, _repository, _environment,
                                _jobEngineManager);
                        return context;
                    }
                });
        return cache;
    }

    public TenantContext getContext(TenantIdentifier tenant) {
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant cannot be null");
        }
        return getContext(tenant.getId());
    }

    public TenantContext getContext(String tenantId) {
        if (Strings.isNullOrEmpty(tenantId)) {
            throw new IllegalArgumentException("Tenant ID cannot be null or empty string");
        }

        tenantId = getStandardizedTenantName(tenantId);

        try {
            return _contexts.get(tenantId);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Iterator<RepositoryFolder> getRepositoryFolderIterator() {
        return _repository.getFolders().iterator();
    }

    private String getStandardizedTenantName(final String tenantId) {
        String standardizedTenantId = tenantId.trim().toLowerCase();

        for (int i = 0; i < ILLEGAL_TENANT_ID_CHARACTERS.length; i++) {
            char c = ILLEGAL_TENANT_ID_CHARACTERS[i];
            standardizedTenantId = org.apache.commons.lang.StringUtils.remove(standardizedTenantId, c);
        }

        if (Strings.isNullOrEmpty(standardizedTenantId)) {
            throw new IllegalArgumentException("Tenant ID contained only invalid characters: " + tenantId);
        }

        if (logger.isDebugEnabled()) {
            if (!tenantId.equals(standardizedTenantId)) {
                logger.debug("Tenant ID '{}' standardized into '{}'", tenantId, standardizedTenantId);
            }
        }

        return standardizedTenantId;
    }
}
