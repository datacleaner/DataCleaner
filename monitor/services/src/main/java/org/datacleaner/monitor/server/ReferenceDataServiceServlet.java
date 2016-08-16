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
package org.datacleaner.monitor.server;

import java.util.List;

import javax.servlet.ServletException;

import org.datacleaner.monitor.referencedata.ReferenceDataItem;
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
            ReferenceDataService delegate = applicationContext.getBean(ReferenceDataServiceImpl.class);
            
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
    public List<ReferenceDataItem> getDictionaries(TenantIdentifier tenant) {
        return _delegate.getDictionaries(tenant);
    }

    @Override
    public List<ReferenceDataItem> getSynonymCatalogs(TenantIdentifier tenant) {
        return _delegate.getSynonymCatalogs(tenant);
    }

    @Override
    public List<ReferenceDataItem> getStringPatterns(TenantIdentifier tenant) {
        return _delegate.getStringPatterns(tenant);
    }
}
