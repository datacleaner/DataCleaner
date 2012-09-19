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
package org.eobjects.datacleaner.monitor.shared;

import java.util.MissingResourceException;

import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.i18n.client.Dictionary;

/**
 * A {@link ClientConfig} based on a {@link Dictionary} in the host GWT page,
 * called by the name "dq_monitor_client_config".
 */
public class DictionaryClientConfig implements ClientConfig {

    private final Dictionary _dictionary;

    public DictionaryClientConfig() {
        _dictionary = Dictionary.getDictionary("dq_monitor_client_config");
    }

    @Override
    public TenantIdentifier getTenant() {
        String tenantId = get("tenant_id");
        if (tenantId == null) {
            return null;
        }
        return new TenantIdentifier(tenantId);
    }

    private String get(String key) {
        try {
            return _dictionary.get(key);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    @Override
    public boolean isDefaultDashboardGroupDisplayed() {
        final String str = get("dashboard_default_group");
        if (str == null) {
            // default is true
            return true;
        }
        return "true".equalsIgnoreCase(str);
    }

    @Override
    public boolean isInformercialDisplayed() {
        final String str = get("dashboard_infomercial");
        if (str == null) {
            // default is true
            return true;
        }
        return "true".equalsIgnoreCase(str);
    }
}
