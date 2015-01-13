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
package org.datacleaner.monitor.shared;

import java.util.MissingResourceException;

import org.datacleaner.monitor.shared.model.TenantIdentifier;

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
        String tenantId = getString("tenant_id");
        if (tenantId == null) {
            return null;
        }
        return new TenantIdentifier(tenantId);
    }

    @Override
    public boolean isDefaultDashboardGroupDisplayed() {
        return getBoolean("dashboard_default_group", true);
    }

    @Override
    public boolean isInformercialDisplayed() {
        return getBoolean("dashboard_infomercial", true);
    }

    @Override
    public boolean isJobEditor() {
        return getBoolean("role_job_editor", false);
    }

    @Override
    public boolean isScheduleEditor() {
        return getBoolean("role_schedule_editor", false);
    }

    @Override
    public boolean isConfigurationEditor() {
        return getBoolean("role_configuration_editor", false);
    }
    
    @Override
    public boolean isDashboardEditor() {
        return getBoolean("role_dashboard_editor", false);
    }

    private String getString(String key) {
        try {
            return _dictionary.get(key);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        final String str = getString(key);
        if (str == null) {
            // default is true
            return defaultValue;
        }
        return "true".equalsIgnoreCase(str);
    }
}
