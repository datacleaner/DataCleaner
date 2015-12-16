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
import java.util.Map;

import javax.annotation.PostConstruct;

import org.datacleaner.job.runner.AnalysisListener;
import org.datacleaner.job.runner.CompositeAnalysisListener;
import org.datacleaner.monitor.configuration.RemoteComponentsConfiguration;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.server.components.ComponentHandler;
import org.datacleaner.restclient.ComponentConfiguration;
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
        return new ComponentHandler(tenantContext.getConfiguration(), componentName, configuration, _remoteComponentsConfiguration, analysisListener);
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
}
