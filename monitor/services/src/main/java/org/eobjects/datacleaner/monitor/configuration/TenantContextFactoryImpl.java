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
package org.eobjects.datacleaner.monitor.configuration;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eobjects.analyzer.configuration.InjectionManagerFactory;
import org.eobjects.analyzer.configuration.InjectionManagerFactoryImpl;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.job.JobEngineManager;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Factory and tenant-wise cache for {@link TenantContext} objects.
 */
@Component("tenantContextFactory")
public class TenantContextFactoryImpl implements TenantContextFactory {

    private static final Logger logger = LoggerFactory.getLogger(TenantContextFactoryImpl.class);

    private final LoadingCache<String, TenantContext> _contexts;
    private final Repository _repository;
    private final InjectionManagerFactory _parentInjectionManagerFactory;
    private final JobEngineManager _jobEngineManager;

    /**
     * Constructs a {@link TenantContextFactoryImpl}.
     * 
     * @param repository
     * @deprecated use
     *             {@link #TenantContextFactoryImpl(Repository, InjectionManagerFactory)}
     *             instead.
     */
    @Deprecated
    public TenantContextFactoryImpl(Repository repository) {
        this(repository, new InjectionManagerFactoryImpl(), null);
    }

    /**
     * Constructs a {@link TenantContextFactoryImpl}.
     * 
     * @param repository
     * @param parentInjectionManagerFactory
     */
    @Autowired
    public TenantContextFactoryImpl(Repository repository, InjectionManagerFactory parentInjectionManagerFactory,
            JobEngineManager jobEngineManager) {
        _repository = repository;
        _parentInjectionManagerFactory = parentInjectionManagerFactory;
        _jobEngineManager = jobEngineManager;
        _contexts = buildTenantContextCache();
    }

    private LoadingCache<String, TenantContext> buildTenantContextCache() {
        LoadingCache<String, TenantContext> cache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS)
                .build(new CacheLoader<String, TenantContext>() {
                    @Override
                    public TenantContext load(String tenantId) throws Exception {
                        logger.info("Initializing tenant context: {}", tenantId);
                        final TenantContext context = new TenantContextImpl(tenantId, _repository,
                                _parentInjectionManagerFactory, _jobEngineManager);
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
        if (StringUtils.isNullOrEmpty(tenantId)) {
            throw new IllegalArgumentException("Tenant cannot be null or empty string");
        }
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
}
