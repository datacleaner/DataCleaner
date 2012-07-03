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
package org.eobjects.datacleaner.monitor.server.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component("user")
@Scope(WebApplicationContext.SCOPE_SESSION)
public class UserBean implements User {

    private final TenantResolver _tenantResolver;
    private final Set<String> _roles;
    private String _username;
    private String _tenant;

    @Autowired
    public UserBean(TenantResolver tenantResolver) {
        _tenantResolver = tenantResolver;
        _roles = new HashSet<String>();
    }

    public void updateUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        updateUser(authentication);
    }

    public void updateUser(final Authentication authentication) {
        _roles.clear();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            _username = null;
            _tenant = null;
        } else {
            _username = authentication.getName();
            _tenant = _tenantResolver.getTenantId(_username);

            final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (GrantedAuthority authority : authorities) {
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
    public boolean hasRole(String role) {
        if (_roles.isEmpty()) {
            updateUser();
        }

        if (_roles.contains(SecurityRoles.ADMIN)) {
            return true;
        }

        if (SecurityRoles.VIEWER.equals(role) || SecurityRoles.SCHEDULE_EDITOR.equals(role)
                || SecurityRoles.JOB_EDITOR.equals(role)) {
            if (_roles.contains(SecurityRoles.ENGINEER)) {
                // ENGINEER is a super-role of SCHEDULE_EDITOR and JOB_EDITOR
                return true;
            }
        }

        return _roles.contains(role);
    }

    @Override
    public String getTenant() {
        if (_tenant == null) {
            updateUser();
        }
        return _tenant;
    }
    
    @Override
    public boolean isLoggedIn() {
        return getUsername() != null;
    }

    @Override
    public String toString() {
        return "User[username=" + _username + ",tenant=" + _tenant + "]";
    }
}
