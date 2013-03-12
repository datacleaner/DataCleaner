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

import java.util.Map;

/**
 * TenantResolver based on a map and a default tenant (in case resolving tenant
 * in the map is unsuccesful).
 */
public class MapBasedTenantResolver implements TenantResolver {

    private Map<String, String> _userTenantMap;
    private String _defaultTenant;

    @Override
    public String getTenantId(String username) {
        if (_userTenantMap != null && _userTenantMap.containsKey(username)) {
            return _userTenantMap.get(username);
        }
        return _defaultTenant;
    }

    public String getDefaultTenant() {
        return _defaultTenant;
    }

    public void setDefaultTenant(String defaultTenant) {
        _defaultTenant = defaultTenant;
    }

    public Map<String, String> getUserTenantMap() {
        return _userTenantMap;
    }

    public void setUserTenantMap(Map<String, String> userTenantMap) {
        _userTenantMap = userTenantMap;
    }
}
