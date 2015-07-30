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

import org.datacleaner.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class ComponentsCache
 * For caching and storing Components and configuration
 *
 * @author k.houzvicka
 * @since 24.7.15
 */
public class ComponentsCache {
    private static final Logger logger = LoggerFactory.getLogger(ComponentsCache.class);

    Repository repository;
    ComponentsStore componentsStore;

    private static final long CHECK_INTERVAL = 60 * 1000;
    private static final long CLOSE_TIMEOUT = 60 * 1000;

    ConcurrentHashMap<String, ComponentsCacheConfigWrapper> data = new ConcurrentHashMap<>();

    Thread checkerThread;
    TimeoutChecker checker;

    /**
     * Create cache object with specific repository
     *
     * @param repository
     */
    public ComponentsCache(Repository repository) {
        this.repository = repository;
        this.componentsStore = new ComponentsStore(repository);
        checker = new TimeoutChecker();
        checkerThread = new Thread(checker);
        checkerThread.start();
    }

    /**
     * Put configuration of component to the cache
     *
     * @param componentConfigHolder
     */
    public void putComponent(ComponentConfigHolder componentConfigHolder) {
        logger.info("Put component. name: {0}, componentId: {1}.", componentConfigHolder.configuration.getComponentName(), componentConfigHolder.getComponentId());
        ComponentsCacheConfigWrapper wrapper = new ComponentsCacheConfigWrapper(componentConfigHolder);
        data.put(componentConfigHolder.componentId, wrapper);
        componentsStore.storeConfiguration(wrapper);
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
            ComponentsCacheConfigWrapper = componentsStore.getConfiguration(id);
            if (ComponentsCacheConfigWrapper == null) {
                logger.warn("Configuration {0} not exists.", id);
                return null;
            }
            logger.info("Component {0} was in store. ", id);
            data.put(id, ComponentsCacheConfigWrapper);
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
        data.remove(id);
        componentsStore.removeConfiguration(id);
        config.close();
        logger.info("Component {0} was removed from cache and closed.", config.getComponentId());
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
            componentsCacheConfigWrapper.componentConfigHolder.close();
            logger.info("Component with id: {0} was closed.", componentsCacheConfigWrapper.componentConfigHolder.getComponentId());
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

        @Override
        public void run() {
            while (running) {
                long currentTime = System.currentTimeMillis();
                HashSet<String> keys = new HashSet<>(data.keySet());
                for (String key : keys) {
                    ComponentsCacheConfigWrapper wrapper = data.get(key);
                    if (wrapper != null && wrapper.expirationTime < currentTime) {
                        // is cache is old configuration
                        ComponentsCacheConfigWrapper storeWrapper = componentsStore.getConfiguration(key);
                        // confirm with object from store
                        if (storeWrapper != null && storeWrapper.expirationTime > currentTime) {
                            // in store is config
                            logger.info("CacheChecker: Component is updated from store.", key);
                            data.put(key, storeWrapper);
                        } else {
                            logger.info("CacheChecker: Component {0} expired.", key);
                            data.remove(key);
                            componentsStore.removeConfiguration(key);
                            storeWrapper.componentConfigHolder.close();
                        }
                    } else if (wrapper != null && wrapper.mustBeUpdated()) {
                        // update store
                        logger.info("CacheChecker: Component {0} is saved to store.", key);
                        componentsStore.storeConfiguration(wrapper);
                        wrapper.updated();
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
