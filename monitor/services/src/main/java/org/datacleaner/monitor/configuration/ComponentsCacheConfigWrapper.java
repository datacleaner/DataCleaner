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
 * 
 * @author k.houzvicka
 * @since 28.7.15
 */
public class ComponentsCacheConfigWrapper {
    private String tenantName;

    private ComponentsStoreHolder componentsStoreHolder;

    private ComponentHandler handler;

    public ComponentsCacheConfigWrapper() {
    }

    public ComponentsCacheConfigWrapper(String tenantName, ComponentsStoreHolder componentsStoreHolder, ComponentHandler handler) {
        this.tenantName = tenantName;
        this.componentsStoreHolder = componentsStoreHolder;
        this.handler = handler;
        componentsStoreHolder.updateTimeStamp();
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public ComponentsStoreHolder getComponentsStoreHolder() {
        return componentsStoreHolder;
    }

    public void setComponentsStoreHolder(ComponentsStoreHolder componentsStoreHolder) {
        this.componentsStoreHolder = componentsStoreHolder;
    }

    public ComponentHandler getHandler() {
        return handler;
    }

    public void setHandler(ComponentHandler handler) {
        this.handler = handler;
    }

    public void updateTimeStamp(){
        componentsStoreHolder.updateTimeStamp();
    }

    /**
     * Check expiration of configuration
     *
     * @return
     */
    public boolean isValid() {
        return componentsStoreHolder.isValid();
    }


}
