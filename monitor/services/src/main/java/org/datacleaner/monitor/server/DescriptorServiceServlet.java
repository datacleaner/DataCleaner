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

import java.util.Collection;

import javax.servlet.ServletException;

import org.datacleaner.monitor.shared.DescriptorService;
import org.datacleaner.monitor.shared.model.DCSecurityException;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.JobMetrics;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * Servlet wrapper/proxy for the {@link DescriptorService}. Passes all service
 * requests on to a delegate, see {@link #setDelegate(DescriptorService)} and
 * {@link #getDelegate()}.
 */
public class DescriptorServiceServlet extends SecureGwtServlet implements DescriptorService {

    private static final long serialVersionUID = 1L;

    private DescriptorService _delegate;

    @Override
    public void init() throws ServletException {
        super.init();

        if (_delegate == null) {
            final WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
            final DescriptorService delegate = applicationContext.getBean(DescriptorService.class);
            if (delegate == null) {
                throw new ServletException("No delegate found in application context!");
            }
            _delegate = delegate;
        }
    }

    public DescriptorService getDelegate() {
        return _delegate;
    }

    public void setDelegate(final DescriptorService delegate) {
        _delegate = delegate;
    }

    @Override
    public JobMetrics getJobMetrics(final TenantIdentifier tenant, final JobIdentifier job) throws DCSecurityException {
        return _delegate.getJobMetrics(tenant, job);
    }

    @Override
    public Collection<String> getMetricParameterSuggestions(final TenantIdentifier tenant,
            final JobIdentifier jobIdentifier, final MetricIdentifier metric) throws DCSecurityException {
        return _delegate.getMetricParameterSuggestions(tenant, jobIdentifier, metric);
    }
}
