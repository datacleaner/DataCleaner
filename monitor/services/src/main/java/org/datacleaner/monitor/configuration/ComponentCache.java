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

/**
 * @since 31. 08. 2015
 */
public interface ComponentCache {
    /**
     * It stores component's configuration wrapper by its ID into the cache and
     * it also stores component's store for the tenant's name.
     *
     * @param tenantName
     * @param tenantContext
     * @param componentStoreHolder
     */
    public void put(String tenantName, TenantContext tenantContext, ComponentStoreHolder componentStoreHolder);

    /**
     * It returns the component's configuration. If it is not in the cache then it is loaded from the repository
     * (In this case component store holder contains only the configuration).
     *
     * @param componentId
     * @param tenantName
     * @param tenantContext
     * @return
     */
    public ComponentCacheConfigWrapper get(String componentId, String tenantName, TenantContext tenantContext);

    /**
     * It removes the configuration from both, the memory and the store. Component is not available anymore.
     *
     * @param componentId
     * @param tenantContext
     */
    public boolean remove(String componentId, TenantContext tenantContext);

    /**
     * It closes all components in the memory. All component configurations are still in the repository.
     *
     * @throws InterruptedException
     */
    public void close() throws InterruptedException;
}
