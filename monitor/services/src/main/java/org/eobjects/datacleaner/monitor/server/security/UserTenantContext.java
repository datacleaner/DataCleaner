package org.eobjects.datacleaner.monitor.server.security;

import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component("tenantContext")
@Scope(WebApplicationContext.SCOPE_SESSION)
public class UserTenantContext implements FactoryBean<TenantContext> {

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
