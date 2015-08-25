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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Class ComponentsCache
 * For caching and storing Components and configuration
 *
 * @since 24.7.15
 */
public class ComponentsCache {
    private static final Logger logger = LoggerFactory.getLogger(ComponentsCache.class);

    ConcurrentHashMap<String, ComponentsCacheConfigWrapper> data = new ConcurrentHashMap<>();

    /**
     * Put configuration of component to the cache
     *
     * @param componentConfigHolder
     */
    public void putComponent(ComponentConfigHolder componentConfigHolder) {
        logger.info("Put component. name: {}, componentId: {}.", componentConfigHolder.getComponentName(), componentConfigHolder.getComponentId());
        ComponentsCacheConfigWrapper wrapper = new ComponentsCacheConfigWrapper(componentConfigHolder);
        data.put(componentConfigHolder.componentId, wrapper);
    }

    /**
     * Contains cache component?
     * 
     * @param id
     * @return
     */
    public boolean contains(String id) {
        return data.get(id) != null;
    }


    /**
     * Read configuration from cache. If configurationHolder is not in cache, is loaded from repository, but in this case in holder is only configuration
     *
     * @param id
     * @return
     */
    public ComponentConfigHolder getConfigHolder(String id) {
        logger.info("Get component with id: " + id);
        ComponentsCacheConfigWrapper ComponentsCacheConfigWrapper = data.get(id);
        if (ComponentsCacheConfigWrapper == null) {
             logger.warn("Configuration {} not exists in cache.", id);
             return null;
        }
        ComponentsCacheConfigWrapper.updateExpirationTime();
        return ComponentsCacheConfigWrapper.componentConfigHolder;
    }

    /**
     * Remove configuration from memory and store. And this component is destroyed.
     *
     * @param id
     */
    public void removeConfiguration(String id) {
        ComponentConfigHolder config = getConfigHolder(id);
        if(config == null){
           return;
        }
        data.remove(id);
        config.close();
        logger.info("Component {} was removed from cache and closed.", config.getComponentId());
    }

    /**
     * Close all component in memory. All components configuration are still in repository.
     *
     * @throws InterruptedException
     */
    public void close() throws InterruptedException {
        logger.info("Closing Components cache.");
        for (ComponentsCacheConfigWrapper componentsCacheConfigWrapper : data.values()) {
            componentsCacheConfigWrapper.componentConfigHolder.close();
            logger.info("Component with id: {} was closed.", componentsCacheConfigWrapper.componentConfigHolder.getComponentId());
            // Configuration is still in store.
        }

        data.clear();
        logger.info("Components cache was closed.");
    }
}
