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
package org.eobjects.datacleaner.monitor.server;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Principal;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;

import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.server.security.TenantResolver;
import org.eobjects.datacleaner.monitor.server.security.User;
import org.eobjects.datacleaner.monitor.server.security.UserBean;
import org.eobjects.datacleaner.monitor.shared.model.DCSecurityException;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Abstract {@link RemoteServiceServlet} which adds security on top of GWT-RPC
 * method calls.
 * 
 * The security is enforced by checking:
 * 
 * <ul>
 * <li>There is a valid {@link Principal} attached to the request/session.</li>
 * <li>If there is a {@link TenantIdentifier} in the payload of the RPC call, it
 * will be verified that the user pertains to this tenant.</li>
 * <li>If the RPC method in the service interface is annotated with
 * {@link RolesAllowed}, these roles will be verified.</li>
 * </ul>
 */
public class SecureGwtServlet extends RemoteServiceServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(SecureGwtServlet.class);

    protected TenantResolver getTenantResolver() {
        final WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
        final TenantResolver tenantResolver = applicationContext.getBean(TenantResolver.class);
        if (tenantResolver == null) {
            throw new IllegalStateException("No TenantResolver found in application context!");
        }
        return tenantResolver;
    }

    @Override
    protected void doUnexpectedFailure(Throwable exception) {
        if (exception instanceof DCSecurityException) {
            final HttpServletResponse response = getThreadLocalResponse();
            try {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage());
                return;
            } catch (IOException e) {
                logger.error("Failed to send error: " + exception.getMessage(), e);
            }
        } else {
            logger.warn("Unexpected exception occurred in GWT servlet: " + exception.getClass().getName(), exception);
        }

        super.doUnexpectedFailure(exception);
    }

    protected boolean hasRole(String roleName) {
        final Principal principal = getThreadLocalRequest().getUserPrincipal();
        if (principal == null) {
            return false;
        }

        final Authentication authentication = (Authentication) principal;

        final UserBean user = new UserBean(getTenantResolver());
        user.updateUser(authentication);

        return user.hasRole(roleName);
    }

    @Override
    protected void onAfterRequestDeserialized(RPCRequest request) {
        final Principal principal = getThreadLocalRequest().getUserPrincipal();
        if (principal == null || StringUtils.isNullOrEmpty(principal.getName())) {
            throw new DCSecurityException("No user principal - log in to use the system");
        }

        if (!(principal instanceof Authentication)) {
            throw new IllegalStateException("Principal is not an instance of Authentication: " + principal);
        }

        final Authentication authentication = (Authentication) principal;

        final UserBean user = new UserBean(getTenantResolver());
        user.updateUser(authentication);

        final Method method = request.getMethod();

        final RolesAllowed rolesAllowedAnnotation = ReflectionUtils.getAnnotation(method, RolesAllowed.class);
        if (rolesAllowedAnnotation != null) {
            String[] rolesAllowed = rolesAllowedAnnotation.value();
            checkRoles(user, rolesAllowed, method);
        }

        final Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            final Class<?> cls = parameterTypes[i];
            if (cls == TenantIdentifier.class) {
                TenantIdentifier tenantIdentifier = (TenantIdentifier) request.getParameters()[i];
                checkTenant(user, tenantIdentifier);
                break;
            }
        }
    }

    private void checkTenant(User user, TenantIdentifier tenantIdentifier) {
        final String authorizedTenant = user.getTenant();
        final String requestedTenant = tenantIdentifier.getId();
        if (!authorizedTenant.equals(requestedTenant)) {
            throw new DCSecurityException("User " + user.getUsername() + " (" + authorizedTenant
                    + ") is not authorized to access tenant: " + requestedTenant);
        }
    }

    private void checkRoles(User user, String[] rolesAllowed, Method method) {
        for (String role : rolesAllowed) {
            if (user.hasRole(role)) {
                // authorized
                return;
            }
        }
        throw new DCSecurityException("User " + user.getUsername() + " is not authorized to invoke " + method);
    }
}
