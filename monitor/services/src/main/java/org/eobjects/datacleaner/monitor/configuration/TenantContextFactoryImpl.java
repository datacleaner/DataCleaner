/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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

import java.util.concurrent.ConcurrentHashMap;

import org.eobjects.analyzer.configuration.InjectionManagerFactory;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory and tenant-wise cache for {@link TenantContext} objects.
 */
@Component("tenantContextFactory")
public class TenantContextFactoryImpl implements TenantContextFactory {

    private final ConcurrentHashMap<String, TenantContext> _contexts;
    private final Repository _repository;
    private final InjectionManagerFactory _injectionManagerFactory;

    @Autowired
    public TenantContextFactoryImpl(Repository repository, InjectionManagerFactory injectionManagerFactory) {
        _repository = repository;
        _injectionManagerFactory = injectionManagerFactory;
        _contexts = new ConcurrentHashMap<String, TenantContext>();
    }

    public TenantContext getContext(TenantIdentifier tenant) {
        return getContext(tenant.getId());
    }

    public TenantContext getContext(String tenantId) {
        TenantContext context = _contexts.get(tenantId);
        if (context == null) {
            final TenantContext newContext = new TenantContextImpl(tenantId, _repository, _injectionManagerFactory);
            context = _contexts.putIfAbsent(tenantId, newContext);
            if (context == null) {
                context = newContext;
            }
        }
        return context;
    }
}
