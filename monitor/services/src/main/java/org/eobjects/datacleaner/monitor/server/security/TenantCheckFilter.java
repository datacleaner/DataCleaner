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

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eobjects.datacleaner.monitor.shared.model.DCSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.GenericFilterBean;

public class TenantCheckFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(TenantCheckFilter.class);

    private final Pattern _pattern = Pattern.compile("/repository/([a-zA-Z0-9]+)/.*");

    private TenantResolver _tenantResolver;

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        final WebApplicationContext applicationContext = WebApplicationContextUtils
                .getWebApplicationContext(getServletContext());
        _tenantResolver = applicationContext.getBean(TenantResolver.class);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
            ServletException {
        if (req instanceof HttpServletRequest) {
            final String path = ((HttpServletRequest) req).getRequestURI();
            final Matcher matcher = _pattern.matcher(path);
            if (matcher != null && matcher.find()) {
                final String urlTenantId = matcher.group(1);
                logger.debug("Matched tenant id: '{}' in servlet path: {}", urlTenantId, path);

                final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null) {
                    final String username = authentication.getName();

                    final String userTenantId = _tenantResolver.getTenantId(username);

                    if (!userTenantId.equals(urlTenantId)) {
                        final String message = "User " + username + " (" + userTenantId
                                + ") is not authorized to access tenant: " + urlTenantId;
                        if (resp instanceof HttpServletResponse) {
                            HttpServletResponse response = (HttpServletResponse) resp;
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
                            return;
                        } else {
                            throw new DCSecurityException(message);
                        }
                    }
                }
            } else {
                logger.debug("Could not match any tenant id in servlet path: {}", path);
            }
        }

        chain.doFilter(req, resp);
    }

}
