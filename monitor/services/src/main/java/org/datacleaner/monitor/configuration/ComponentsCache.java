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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class ComponentsCache
 * For caching and storing Components and configuration
 *
 * @since 24.7.15
 */
public class ComponentsCache {
    private static final Logger logger = LoggerFactory.getLogger(ComponentsCache.class);
    private static final long CHECK_INTERVAL = 5 * 60 * 1000;
    private static final long CLOSE_TIMEOUT = 60 * 1000;

    private TenantContextFactory _tenantContextFactory;

    private ConcurrentHashMap<String, ComponentsCacheConfigWrapper> data = new ConcurrentHashMap<>();
    private Thread checkerThread;
    private TimeoutChecker checker;

    public ComponentsCache(TenantContextFactory _tenantContextFactory) {
        this._tenantContextFactory = _tenantContextFactory;
        checker = new TimeoutChecker();
        checkerThread = new Thread(checker);
        checkerThread.start();
    }

    /**
     * Put configuration of component to the cache
     *
     * @param tenant
     * @param tenantContext
     * @param componentsHolder
     */
    public void putComponent(String tenant, TenantContext tenantContext, ComponentsStoreHolder componentsHolder) {
        logger.info("Put component. name: {}, componentId: {}.", componentsHolder.getComponentName(), componentsHolder.getComponentId());
        ComponentHandler handler = ComponentsFactory.createComponent(tenantContext, componentsHolder.getComponentName(), componentsHolder.getCreateInput().configuration);
        ComponentsCacheConfigWrapper wrapper = new ComponentsCacheConfigWrapper(tenant, componentsHolder, handler);
        data.put(componentsHolder.getComponentId(), wrapper);
        tenantContext.getComponentsStore().storeConfiguration(wrapper.getComponentsStoreHolder());
    }

    /**
     * Read configuration from cache. If configurationHolder is not in cache, is loaded from repository, but in this case in holder is only configuration
     *
     * @param id
     * @return
     */
    public ComponentsCacheConfigWrapper getConfigHolder(String id, String tenant, TenantContext tenantContext) {
        logger.info("Get component with id: " + id);
        ComponentsCacheConfigWrapper componentsCacheConfigWrapper = data.get(id);
        if (componentsCacheConfigWrapper == null) {
            logger.warn("Configuration {} does not exist in cache.", id);
            ComponentsStore store = tenantContext.getComponentsStore();
            ComponentsStoreHolder storeConfig = store.getConfiguration(id);
            if (storeConfig == null) {
                logger.warn("Configuration {} does not exist in store.", id);
                return null;
            } else {
                ComponentHandler componentHandler = ComponentsFactory.createComponent(tenantContext, storeConfig.getComponentName(), storeConfig.getCreateInput().configuration);
                componentsCacheConfigWrapper = new ComponentsCacheConfigWrapper(tenant, storeConfig, componentHandler);
                data.put(id, componentsCacheConfigWrapper);
            }
        }
        componentsCacheConfigWrapper.updateTimeStamp();
        return componentsCacheConfigWrapper;
    }

    /**
     * Remove configuration from memory and store. And this component is destroyed.
     *
     * @param id
     * @param tenantContext
     */
    public boolean removeConfiguration(String id, TenantContext tenantContext) {
        boolean inCache = removeConfigurationOnlyFromCache(id);
        boolean inStore = removeConfigurationOnlyFromStore(id, tenantContext);
        logger.info("Component {} was removed from cache and closed.", id);
        return inCache || inStore;
    }


    private boolean removeConfigurationOnlyFromCache(String id) {
        ComponentsCacheConfigWrapper config = data.get(id);
        if (config != null) {
            data.remove(id);
            config.getHandler().closeComponent();
            return true;
        } else {
            return false;
        }
    }

    private boolean removeConfigurationOnlyFromStore(String id, TenantContext tenantContext) {
        ComponentsStore store = tenantContext.getComponentsStore();
        return store.removeConfiguration(id);
    }

    /**
     * Close all component in memory. All components configuration are still in repository.
     *
     * @throws InterruptedException
     */
    public void close() throws InterruptedException {
        logger.info("Closing Components cache.");

        synchronized (checker) {
            checker.stop();
            checker.notifyAll();
        }
        long maxTime = System.currentTimeMillis() + CLOSE_TIMEOUT;
        while (checkerThread.isAlive()) {
            Thread.sleep(500);
            if (maxTime < System.currentTimeMillis()) {
                logger.error("Problem with closing checking thread.");
                break;
            }
        }

        for (ComponentsCacheConfigWrapper componentsCacheConfigWrapper : data.values()) {
            componentsCacheConfigWrapper.getHandler().closeComponent();
            logger.info("Component with id: {} was closed.", componentsCacheConfigWrapper.getComponentsStoreHolder().getComponentId());
            // Configuration is still in store.
        }

        data.clear();
        logger.info("Components cache was closed.");
    }


    /**
     * Thread for checking timeout for each components and also do update configuration is store.
     */
    private class TimeoutChecker implements Runnable {
        boolean running = true;
        boolean firstRun = true;

        @Override
        public void run() {
            while (running) {
                if (firstRun) {
                    firstRun = false;
                } else {
                    Set<String> tenants = _tenantContextFactory.getAllTenantsName();
                    Set<String> allIdInCache = new HashSet<>(data.keySet());
                    for (String tenant : tenants) {
                        TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
                        List<ComponentsStoreHolder> configurationList = tenantContext.getComponentsStore().getAllConfiguration();
                        for (ComponentsStoreHolder storeHolder : configurationList) {
                            String componentId = storeHolder.getComponentId();
                            allIdInCache.remove(componentId);
                            //is in cache?
                            ComponentsCacheConfigWrapper cache = data.get(componentId);
                            if (cache == null) {
                                //is only in store
                                if (!storeHolder.isValid()) {
                                    //remove from store
                                    logger.info("CacheChecker - Remove old configuration {} from store of tenant {}.", componentId, tenantContext.getTenantId());
                                    removeConfigurationOnlyFromStore(componentId, tenantContext);
                                }
                            } else {
                                long maxTimestamp = Math.max(cache.getComponentsStoreHolder().getUseTimestamp(), storeHolder.getUseTimestamp());
                                if (maxTimestamp + storeHolder.getTimeout() < System.currentTimeMillis()) {
                                    // too old
                                    logger.info("CacheChecker - Remove old configuration {} from store and cache of tenant {}.", componentId, tenantContext.getTenantId());
                                    removeConfigurationOnlyFromCache(componentId);
                                    removeConfigurationOnlyFromStore(componentId, tenantContext);
                                } else {
                                    if (cache.getComponentsStoreHolder().getUseTimestamp() <= storeHolder.getUseTimestamp()) {
                                        //update cache
                                        logger.info("CacheChecker - Update timestamp of component {} in cache from store.", componentId);
                                        cache.setComponentsStoreHolder(storeHolder);
                                    } else {
                                        //update store
                                        logger.info("CacheChecker - Update timestamp of component {} in store from cache.", componentId);
                                        tenantContext.getComponentsStore().storeConfiguration(cache.getComponentsStoreHolder());
                                    }
                                }
                            }
                        }
                    }

                    for (String componentId : allIdInCache) {
                        //These components are without configuration in store
                        removeConfigurationOnlyFromCache(componentId);
                        logger.info("CacheChecker - Configuration {} is not in store. It was removed from cache.", componentId);
                    }

                }
                synchronized (this) {
                    if (running) {
                        try {
                            wait(CHECK_INTERVAL);
                        } catch (InterruptedException e) {
                            running = false;
                            logger.error("Thread for checking component cache was been interrupted.", e);
                        }
                    }
                }
            }
            logger.info("CacheChecker close");
        }

        public void stop() {
            running = false;
        }
    }
}
