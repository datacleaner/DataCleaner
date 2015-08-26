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
package org.datacleaner.monitor.server.controllers;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.monitor.configuration.*;
import org.datacleaner.monitor.server.components.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.UUID;

/**
 * Controller for DataCleaner components (transformers and analyzers). It enables to use a particular component
 * and provide the input data separately without any need of the whole job or datastore configuration.
 * @author k.houzvicka, j.horcicka
 * @since 8. 7. 2015
 */
@Controller
public class ComponentsControllerV1 implements ComponentsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentsControllerV1.class);
    private ComponentsCache _componentsCache = null;
    private static final String PARAMETER_NAME_TENANT = "tenant";
    private static final String PARAMETER_NAME_ID = "id";
    private static final String PARAMETER_NAME_NAME = "name";

    @Autowired
    TenantContextFactory _tenantContextFactory;
  

    @PostConstruct
    public void init() {
        _componentsCache = new ComponentsCache(_tenantContextFactory);
    }

    @PreDestroy
    public void close() throws InterruptedException {
        _componentsCache.close();
    }

    /**
     * It returns a list of all components and their configurations.
     * @param tenant
     * @return
     */
    public ComponentList getAllComponents(@PathVariable(PARAMETER_NAME_TENANT) final String tenant) {
        DataCleanerConfiguration configuration = _tenantContextFactory.getContext(tenant).getConfiguration();
        Collection<TransformerDescriptor<?>> transformerDescriptors = configuration.getEnvironment()
                .getDescriptorProvider()
                .getTransformerDescriptors();
        ComponentList componentList = new ComponentList();

        for (TransformerDescriptor descriptor : transformerDescriptors) {
            componentList.add(tenant, descriptor);
        }

        return componentList;
    }

    /**
     * It creates a new component with the provided configuration, runs it and returns the result.
     * @param tenant
     * @param name
     * @param processStatelessInput
     * @return
     */
    public ProcessStatelessOutput processStateless(
            @PathVariable(PARAMETER_NAME_TENANT) final String tenant,
            @PathVariable(PARAMETER_NAME_NAME) final String name,
            @RequestBody final ProcessStatelessInput processStatelessInput) {
        String decodedName = unURLify(name);
        LOGGER.debug("Running '" + decodedName + "'");
        TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        ComponentHandler handler =  ComponentsFactory.createComponent(tenantContext, decodedName, processStatelessInput.configuration);
        ProcessStatelessOutput output = new ProcessStatelessOutput();
        output.rows = handler.runComponent(processStatelessInput.data);
        output.result = handler.closeComponent();
        return output;
    }

    /**
     * It runs the component and returns the results.
     */
    public String createComponent(
            @PathVariable(PARAMETER_NAME_TENANT) final String tenant,
            @PathVariable(PARAMETER_NAME_NAME) final String name,              //1 day
            @RequestParam(value = "timeout", required = false, defaultValue = "86400000") final String timeout,
            @RequestBody final CreateInput createInput) {
        String decodedName = unURLify(name);
        TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        String id = UUID.randomUUID().toString();
        long longTimeout = Long.parseLong(timeout);
        _componentsCache.putComponent(
                tenant,
                tenantContext,
                new ComponentsStoreHolder(longTimeout, createInput, id, decodedName)
        );
        return id;
    }

    /**
     * It returns the continuous result of the component for the provided input data.
     */
    public ProcessOutput processComponent(
            @PathVariable(PARAMETER_NAME_TENANT) final String tenant,
            @PathVariable(PARAMETER_NAME_ID) final String id,
            @RequestBody final ProcessInput processInput)
            throws ComponentNotFoundException {
        TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        ComponentsCacheConfigWrapper config = _componentsCache.getConfigHolder(id, tenant, tenantContext);
        if(config == null){
            LOGGER.warn("Component with id {} does not exist.", id);
            throw ComponentNotFoundException.createInstanceNotFound(id);
        }
        ComponentHandler handler = config.getHandler();
        ProcessOutput out = new ProcessOutput();
        out.rows = handler.runComponent(processInput.data);
        return out;
    }

    /**
     * It returns the component's final result.
     */
    public ProcessResult getFinalResult(
            @PathVariable(PARAMETER_NAME_TENANT) final String tenant,
            @PathVariable(PARAMETER_NAME_ID) final String id)
            throws ComponentNotFoundException {
        // TODO - only for analyzers, implement it later after the architecture
        // decisions regarding the load-balancing and failover.
        return null;
    }

    /**
     * It deletes the component.
     */
    public void deleteComponent(
            @PathVariable(PARAMETER_NAME_TENANT) final String tenant,
            @PathVariable(PARAMETER_NAME_ID) final String id)
            throws ComponentNotFoundException {
        TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        boolean isHere = _componentsCache.removeConfiguration(id, tenantContext);
        if (!isHere) {
            LOGGER.warn("Instance of component {} not found in the cache and in the store", id);
            throw ComponentNotFoundException.createInstanceNotFound(id);
        }
    }

    private String unURLify(String url) {
        return url.replace("_@_", "/");
    }
}
