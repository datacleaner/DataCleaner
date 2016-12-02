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
package org.datacleaner.monitor.server.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.google.common.base.Strings;

@Component("user")
@Scope(WebApplicationContext.SCOPE_SESSION)
public class UserBean implements User, Serializable {

    private static final long serialVersionUID = 1L;

    private final Set<String> _roles;
    private String _username;
    private String _tenant;

    /**
     *
     * @param tenantResolver
     * @deprecated use {@link #UserBean()} instead
     */
    @Deprecated
    public UserBean(final TenantResolver tenantResolver) {
        this();
    }

    public UserBean() {
        _roles = new HashSet<>();
    }

    public void updateUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        updateUser(authentication);
    }

    public void updateUser(final Authentication authentication) {
        final WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
        final TenantResolver tenantResolver = applicationContext.getBean(TenantResolver.class);
        updateUser(authentication, tenantResolver);
    }

    public void updateUser(final Authentication authentication, final TenantResolver tenantResolver) {
        _roles.clear();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            _username = null;
            _tenant = null;
        } else {
            _username = authentication.getName();
            if (tenantResolver == null) {
                // can happen in deserialized object cases
                if (Strings.isNullOrEmpty(_tenant)) {
                    throw new IllegalArgumentException("TenantResolver cannot be null when tenant is also null");
                }
            } else {
                _tenant = tenantResolver.getTenantId(_username);
            }

            final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (final GrantedAuthority authority : authorities) {
                _roles.add(authority.getAuthority());
            }
        }
    }

    @Override
    public String getUsername() {
        if (_username == null) {
            updateUser();
        }
        return _username;
    }

    @Override
    public boolean hasRole(final String role) {
        if (_roles.isEmpty()) {
            updateUser();
        }

        if (_roles.contains(SecurityRoles.GOD) || _roles.contains(SecurityRoles.ADMIN)) {
            return true;
        }

        if (SecurityRoles.VIEWER.equals(role) || SecurityRoles.SCHEDULE_EDITOR.equals(role) || SecurityRoles.JOB_EDITOR
                .equals(role)) {
            if (_roles.contains(SecurityRoles.ENGINEER)) {
                // ENGINEER is a super-role of SCHEDULE_EDITOR and JOB_EDITOR
                return true;
            }
        }

        return _roles.contains(role);
    }

    @Override
    public String getTenant() {
        if (Strings.isNullOrEmpty(_tenant)) {
            updateUser();
        }
        return _tenant;
    }

    public List<String> getRoles() {
        if (_roles.isEmpty()) {
            updateUser();
        }
        return new ArrayList<>(_roles);
    }

    @Override
    public boolean isLoggedIn() {
        return isAuthenticated();
    }

    @Override
    public boolean isAuthenticated() {
        return getUsername() != null;
    }

    @Override
    public String toString() {
        return "User[username=" + _username + ",tenant=" + _tenant + "]";
    }

    @Override
    public boolean isGod() {
        return hasRole(SecurityRoles.GOD);
    }

    @Override
    public boolean isAdmin() {
        return hasRole(SecurityRoles.ADMIN);
    }

    @Override
    public boolean isEngineer() {
        return hasRole(SecurityRoles.ENGINEER);
    }

    @Override
    public boolean isJobEditor() {
        return hasRole(SecurityRoles.JOB_EDITOR);
    }

    @Override
    public boolean isDashboardEditor() {
        return hasRole(SecurityRoles.DASHBOARD_EDITOR);
    }

    @Override
    public boolean isScheduleEditor() {
        return hasRole(SecurityRoles.SCHEDULE_EDITOR);
    }

    @Override
    public boolean isViewer() {
        return hasRole(SecurityRoles.VIEWER);
    }

    @Override
    public boolean isQueryAllowed() {
        return hasRole(SecurityRoles.TASK_QUERY);
    }

    @Override
    public boolean isConfigurationEditor() {
        return hasRole(SecurityRoles.CONFIGURATION_EDITOR);
    }
}
