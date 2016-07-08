package org.datacleaner.monitor.server;

import javax.servlet.ServletException;

import org.datacleaner.monitor.referencedata.ReferenceDataService;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

public class ReferenceDataServiceServlet extends SecureGwtServlet implements ReferenceDataService {

    private static final long serialVersionUID = 1L;

    private ReferenceDataService _delegate;

    @Override
    public void init() throws ServletException {
        super.init();

        if (_delegate == null) {
            WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
            ReferenceDataService delegate = applicationContext.getBean(ReferenceDataService.class);
            if (delegate == null) {
                throw new ServletException("No delegate found in application context!");
            }
            _delegate = delegate;
        }

    }

    public void setDelegate(ReferenceDataService delegate) {
        _delegate = delegate;
    }

    public ReferenceDataService getDelegate() {
        return _delegate;
    }


    @Override
    public String getDictionaries(TenantIdentifier tenant) {
        return _delegate.getDictionaries(tenant);
    }

    @Override
    public void getSynonymsCatalog(TenantIdentifier tenant) {
        // TODO Auto-generated method stub

    }

    @Override
    public void getStringPatterns(TenantIdentifier tenant) {
        // TODO Auto-generated method stub

    }

}
