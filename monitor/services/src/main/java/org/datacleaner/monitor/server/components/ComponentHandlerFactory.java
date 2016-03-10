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

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.job.runner.AnalysisListener;
import org.datacleaner.job.runner.CompositeAnalysisListener;
import org.datacleaner.monitor.configuration.RemoteComponentsConfiguration;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.shared.ComponentNotAllowed;
import org.datacleaner.monitor.shared.ComponentNotFoundException;
import org.datacleaner.restclient.ComponentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Factory for ComponentHandler instances
 * 
 * @since 18.8.15
 */

@Component
public class ComponentHandlerFactory {

    private static final Logger logger = LoggerFactory.getLogger(ComponentHandlerFactory.class);

    private RemoteComponentsConfiguration _remoteComponentsConfiguration;

    private AnalysisListener analysisListener;

    @Autowired
    private ApplicationContext appCtx;

    @Autowired
    public ComponentHandlerFactory(RemoteComponentsConfiguration remoteComponentsConfiguration) {
        this._remoteComponentsConfiguration = remoteComponentsConfiguration;
    }

    /**
     * Creates new Handler from configuration
     * 
     * @param tenantContext
     * @param componentName
     * @param configuration
     * @return
     * @throws RuntimeException
     */
    public ComponentHandler createComponent(TenantContext tenantContext, String componentName,
            ComponentConfiguration configuration) throws RuntimeException {
        return new ComponentHandler(
                tenantContext.getConfiguration(),
                resolveDescriptor(tenantContext.getConfiguration().getEnvironment(), componentName),
                configuration, _remoteComponentsConfiguration, analysisListener);
    }

    @PostConstruct
    private void initialize() {
        Collection<AnalysisListener> listeners = BeanFactoryUtils.beansOfTypeIncludingAncestors(appCtx, AnalysisListener.class).values();
        if(listeners.size() == 1) {
            analysisListener = listeners.iterator().next();
        } else if(listeners.size() > 1) {
            analysisListener = new CompositeAnalysisListener(listeners.toArray(new AnalysisListener[listeners.size()]));
        }
    }

    public ComponentDescriptor<?> resolveDescriptor(DataCleanerEnvironment env, String componentName) {
        ComponentDescriptor<?> descriptor = env.getDescriptorProvider()
                .getComponentDescriptorByDisplayName(componentName);
        if (descriptor == null) {
            logger.info("Component {} not found.", componentName);
            throw ComponentNotFoundException.createTypeNotFound(componentName);
        }
        if (!_remoteComponentsConfiguration.isAllowed(descriptor)) {
            logger.info("Component {} is not allowed.", componentName);
            throw ComponentNotAllowed.createInstanceNotAllowed(componentName);
        }
        return descriptor;
    }

}
