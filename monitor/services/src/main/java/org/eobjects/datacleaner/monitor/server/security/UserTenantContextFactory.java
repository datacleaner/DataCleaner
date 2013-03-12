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
package org.eobjects.datacleaner.monitor.server.security;

import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 * Factory for {@link TenantContext} objects which are provided typically to
 * render JSF pages.
 */
@Component("tenantContext")
@Scope(WebApplicationContext.SCOPE_REQUEST)
public class UserTenantContextFactory implements FactoryBean<TenantContext> {

    @Autowired
    TenantContextFactory tenantContextFactory;

    @Autowired
    User user;

    @Override
    public TenantContext getObject() throws Exception {
        return tenantContextFactory.getContext(user.getTenant());
    }

    @Override
    public Class<?> getObjectType() {
        return TenantContext.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
