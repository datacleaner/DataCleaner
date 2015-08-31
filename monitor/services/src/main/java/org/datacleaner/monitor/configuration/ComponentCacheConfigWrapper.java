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
package org.datacleaner.monitor.configuration;

import org.datacleaner.monitor.server.components.ComponentHandler;

/**
 * Class ComponentsCacheConfigWrapper Simple wrapper for store to cache with expiration time.
 * @since 28.7.15
 */
public class ComponentCacheConfigWrapper {
    private String tenantName;

    private ComponentStoreHolder componentStoreHolder;

    private ComponentHandler handler;

    public ComponentCacheConfigWrapper() {
    }

    public ComponentCacheConfigWrapper(String tenantName, ComponentStoreHolder componentStoreHolder, ComponentHandler handler) {
        this.tenantName = tenantName;
        this.componentStoreHolder = componentStoreHolder;
        this.handler = handler;
        componentStoreHolder.updateTimeStamp();
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public ComponentStoreHolder getComponentStoreHolder() {
        return componentStoreHolder;
    }

    public void setComponentStoreHolder(ComponentStoreHolder componentStoreHolder) {
        this.componentStoreHolder = componentStoreHolder;
    }

    public ComponentHandler getHandler() {
        return handler;
    }

    public void setHandler(ComponentHandler handler) {
        this.handler = handler;
    }

    public void updateTimeStamp(){
        componentStoreHolder.updateTimeStamp();
    }

    /**
     * Check expiration of configuration
     *
     * @return
     */
    public boolean isValid() {
        return componentStoreHolder.isValid();
    }
}
