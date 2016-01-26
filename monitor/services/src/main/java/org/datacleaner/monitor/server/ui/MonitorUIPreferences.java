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
package org.datacleaner.monitor.server.ui;

import org.springframework.stereotype.Component;

/**
 * A spring bean used to define the preferences on the UI side of the
 * DataCleaner monitor. This bean contains properties that will affect which
 * elements are included and which are removed or altered.
 */
@Component("monitorUIPreferences")
public class MonitorUIPreferences {

    private boolean _showDemoAccounts = true;
    private boolean _showWebstartLaunchOptions = false;
    private boolean _showRepositoryUploadOptions = true;
    private boolean _showConfXmlUploadOptions = true;

    public boolean isShowDemoAccounts() {
        return _showDemoAccounts;
    }

    public void setShowDemoAccounts(boolean showDemoAccounts) {
        _showDemoAccounts = showDemoAccounts;
    }

    public boolean isShowWebstartLaunchOptions() {
        return _showWebstartLaunchOptions;
    }

    public void setShowWebstartLaunchOptions(boolean showWebstartLaunchOptions) {
        _showWebstartLaunchOptions = showWebstartLaunchOptions;
    }

    public boolean isShowRepositoryUploadOptions() {
        return _showRepositoryUploadOptions;
    }

    public void setShowRepositoryUploadOptions(boolean showRepositoryUploadOptions) {
        _showRepositoryUploadOptions = showRepositoryUploadOptions;
    }

    public boolean isShowConfXmlUploadOptions() {
        return _showConfXmlUploadOptions;
    }

    public void setShowConfXmlUploadOptions(boolean showConfXmlUploadOptions) {
        _showConfXmlUploadOptions = showConfXmlUploadOptions;
    }

}
