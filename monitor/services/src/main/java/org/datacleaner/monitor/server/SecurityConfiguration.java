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

/**
 * Specifies the security configuration of DataCleaner monitor. This object can
 * optionally be provided in the spring application context in order to instruct
 * other components how to expose certain details of the application.
 */
public class SecurityConfiguration {

    private String casServerUrl;
    private String securityMode;

    public String getSecurityMode() {
        return securityMode;
    }

    public void setSecurityMode(final String securityMode) {
        this.securityMode = securityMode;
    }

    public String getCasServerUrl() {
        return casServerUrl;
    }

    public void setCasServerUrl(final String casServerUrl) {
        this.casServerUrl = casServerUrl;
    }
}
