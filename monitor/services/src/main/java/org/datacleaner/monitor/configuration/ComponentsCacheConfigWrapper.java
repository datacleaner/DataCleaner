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

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * Class ComponentsCacheConfigWrapper
 * Simple wrapper for store to cache with expiration time.
 *
 * @since 28.7.15
 */
public class ComponentsCacheConfigWrapper implements Serializable {


    ComponentConfigHolder componentConfigHolder;

    long expirationTime;

    @JsonIgnore
    private boolean update = false;

    public ComponentsCacheConfigWrapper() {
    }

    public ComponentsCacheConfigWrapper(ComponentConfigHolder componentConfigHolder) {
        expirationTime = componentConfigHolder.timeoutMs + System.currentTimeMillis();
        this.componentConfigHolder = componentConfigHolder;
    }

    public ComponentConfigHolder getComponentConfigHolder() {
        return componentConfigHolder;
    }

    public void setComponentConfigHolder(ComponentConfigHolder componentConfigHolder) {
        this.componentConfigHolder = componentConfigHolder;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void updateStore() {
        update = true;
    }

    public boolean mustBeUpdated() {
        return update;
    }

    public void updated() {
        update = false;
    }

    public void updateExpirationTime() {
        expirationTime = componentConfigHolder.timeoutMs + System.currentTimeMillis();
        updateStore();
    }


}
