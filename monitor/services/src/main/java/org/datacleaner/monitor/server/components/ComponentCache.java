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
package org.datacleaner.monitor.server.components;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

import org.datacleaner.monitor.configuration.ComponentStore;
import org.datacleaner.monitor.configuration.ComponentStoreHolder;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class for caching and storing Components and their configurations.
 * @since 24.7.15
 */
@Component
public class ComponentCache {
    /**
     * Thread for checking the timeout for each component and updating the configuration is the store.
     */
    private class TimeoutChecker implements Runnable {
        boolean running = true;
        boolean firstRun = true;
        private Set<String> allIdInCache = null;

        public void stop() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                if (firstRun) {
                    firstRun = false;
                } else {
                    check();
                }

                synchronized (this) {
                    if (running) {
                        try {
                            wait(CHECK_INTERVAL);
                        } catch (final InterruptedException e) {
                            running = false;
                            logger.error("Thread for component cache checking has been interrupted.", e);
                        }
                    }
                }
            }

            logger.info("TimeoutChecker has just finished. ");
        }

        private void check() {
            allIdInCache = new HashSet<>(data.keySet());
            for (final TenantContext tenantContext : _tenantContextFactory.getActiveTenantContexts()) {
                checkTenantComponents(tenantContext);
            }
            for (final String instanceId : allIdInCache) {
                removeConfigurationOnlyFromCache(instanceId);
                logger.info("TimeoutChecker - Configuration {} is not in the store. It was removed from the cache.",
                        instanceId);
            }
        }

        private void checkTenantComponents(final TenantContext tenantContext) {
            final List<ComponentStoreHolder> configurationList = tenantContext.getComponentStore().getList();

            for (final ComponentStoreHolder componentStoreHolder : configurationList) {
                checkComponentStoreHolder(tenantContext, componentStoreHolder);
            }
        }

        private void checkComponentStoreHolder(final TenantContext tenantContext, final ComponentStoreHolder componentStoreHolder) {
            final String instanceId = componentStoreHolder.getInstanceId();
            allIdInCache.remove(instanceId);
            final ComponentCacheConfigWrapper cache = data.get(instanceId);

            if (cache == null) {
                removeFromStore(tenantContext, componentStoreHolder);
            } else {
                final long maxTimestamp = Math.max(
                        cache.getComponentStoreHolder().getUseTimestamp(), componentStoreHolder.getUseTimestamp());

                if (maxTimestamp + componentStoreHolder.getTimeout() < System.currentTimeMillis()) {
                    remove(tenantContext, componentStoreHolder);
                } else {
                    update(cache, tenantContext, componentStoreHolder);
                }
            }
        }

        private void remove(final TenantContext tenantContext, final ComponentStoreHolder componentStoreHolder) {
            final String instanceId = componentStoreHolder.getInstanceId();
            logger.info("TimeoutChecker - Old configuration {} for tenant {} was removed from the store and the cache.",
                    instanceId, tenantContext.getTenantId());
            removeConfigurationOnlyFromCache(instanceId);
            removeConfigurationOnlyFromStore(instanceId, tenantContext);
        }

        private void removeFromStore(final TenantContext tenantContext, final ComponentStoreHolder componentStoreHolder) {
            final String instanceId = componentStoreHolder.getInstanceId();

            if (!componentStoreHolder.isValid()) {
                logger.info("TimeoutChecker - Old configuration {} for tenant {}  was removed from the store.",
                        instanceId, tenantContext.getTenantId());
                removeConfigurationOnlyFromStore(instanceId, tenantContext);
            }
        }

        private void update(final ComponentCacheConfigWrapper cache, final TenantContext tenantContext,
                final ComponentStoreHolder componentStoreHolder) {
            final String instanceId = componentStoreHolder.getInstanceId();

            if (cache.getComponentStoreHolder().getUseTimestamp() <= componentStoreHolder.getUseTimestamp()) {
                logger.info("TimeoutChecker - Timestamp of the component {} was updated in the cache from the store.",
                        instanceId);
                cache.setComponentStoreHolder(componentStoreHolder);
            } else {
                logger.info("TimeoutChecker - Timestamp of the component {} was updated in the store from the cache.",
                        instanceId);
                tenantContext.getComponentStore().store(cache.getComponentStoreHolder());
            }
        }
    }
    private static final Logger logger = LoggerFactory.getLogger(ComponentCache.class);
    private static final long CHECK_INTERVAL = 5 * 60 * 1000;
    private static final long CLOSE_TIMEOUT = 60 * 1000;
    private final Map<String, ComponentCacheConfigWrapper> data = new ConcurrentHashMap<>();
    private final Thread checkerThread;
    private final TimeoutChecker checker;
    private final ComponentHandlerFactory componentHandlerFactory;
    private final TenantContextFactory _tenantContextFactory;

    @Autowired
    public ComponentCache(final ComponentHandlerFactory componentHandlerFactory, final TenantContextFactory tenantCtxFac) {
        this.componentHandlerFactory = componentHandlerFactory;
        this._tenantContextFactory = tenantCtxFac;
        checker = new TimeoutChecker();
        checkerThread = new Thread(checker);
        checkerThread.setDaemon(true);
        checkerThread.start();
    }

    /**
     * Put configuration of component to the cache
     */
    public void put(final String tenant, final TenantContext tenantContext, final ComponentStoreHolder componentsHolder) {
        logger.info("Put component. name: {}, instanceId: {}.", componentsHolder.getComponentName(),
                componentsHolder.getInstanceId());
        final ComponentHandler handler = componentHandlerFactory.createComponent(
                tenantContext,
                componentsHolder.getComponentName(),
                componentsHolder.getCreateInput().configuration);
        final ComponentCacheConfigWrapper wrapper = new ComponentCacheConfigWrapper(tenant, componentsHolder, handler);
        data.put(componentsHolder.getInstanceId(), wrapper);
        tenantContext.getComponentStore().store(wrapper.getComponentStoreHolder());
    }

    /**
     * Read configuration from cache. If configurationHolder is not in cache, is loaded from repository,
     * but in this case in holder is only configuration
     */
    public ComponentCacheConfigWrapper get(final String id, final String tenant, final TenantContext tenantContext) {
        logger.info("Get component with id: " + id);
        ComponentCacheConfigWrapper componentCacheConfigWrapper = data.get(id);
        if (componentCacheConfigWrapper == null) {
            logger.warn("Configuration {} does not exist in cache.", id);
            final ComponentStore store = tenantContext.getComponentStore();
            final ComponentStoreHolder storeConfig = store.get(id);
            if (storeConfig == null) {
                logger.warn("Configuration {} does not exist in store.", id);
                return null;
            } else {
                final ComponentHandler componentHandler = componentHandlerFactory.createComponent(
                        tenantContext,
                        storeConfig.getComponentName(),
                        storeConfig.getCreateInput().configuration);
                componentCacheConfigWrapper = new ComponentCacheConfigWrapper(tenant, storeConfig, componentHandler);
                data.put(id, componentCacheConfigWrapper);
            }
        }
        componentCacheConfigWrapper.updateTimeStamp();
        return componentCacheConfigWrapper;
    }

    /**
     * Remove configuration from memory and store. And this component is destroyed.
     */
    public boolean remove(final String id, final TenantContext tenantContext) {
        final boolean inCache = removeConfigurationOnlyFromCache(id);
        final boolean inStore = removeConfigurationOnlyFromStore(id, tenantContext);
        logger.info("Component {} was removed from cache and closed.", id);
        return inCache || inStore;
    }

    private boolean removeConfigurationOnlyFromCache(final String id) {
        final ComponentCacheConfigWrapper config = data.get(id);
        if (config != null) {
            data.remove(id);
            config.getHandler().closeComponent();
            return true;
        } else {
            return false;
        }
    }

    private boolean removeConfigurationOnlyFromStore(final String id, final TenantContext tenantContext) {
        final ComponentStore store = tenantContext.getComponentStore();
        return store.remove(id);
    }

    /**
     * Close all component in memory. All components configuration are still in repository.
     */
    @PreDestroy
    public void close() throws InterruptedException {
        logger.info("Closing Components cache.");

        synchronized (checker) {
            checker.stop();
            checker.notifyAll();
        }
        final long maxTime = System.currentTimeMillis() + CLOSE_TIMEOUT;
        while (checkerThread.isAlive()) {
            Thread.sleep(500);
            if (maxTime < System.currentTimeMillis()) {
                logger.error("Problem with closing checking thread.");
                break;
            }
        }

        for (final ComponentCacheConfigWrapper componentCacheConfigWrapper : data.values()) {
            componentCacheConfigWrapper.getHandler().closeComponent();
            logger.info("Component with id: {} was closed.",
                    componentCacheConfigWrapper.getComponentStoreHolder().getInstanceId());
            // Configuration is still in store.
        }

        data.clear();
        logger.info("Components cache was closed.");
    }
}
