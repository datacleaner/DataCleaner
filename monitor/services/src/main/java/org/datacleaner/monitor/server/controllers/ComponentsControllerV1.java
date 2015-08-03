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

import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.server.components.*;
import org.datacleaner.monitor.server.components.ProcessStatelessInput;
import org.datacleaner.monitor.server.components.ProcessStatelessOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
    private static final String TENANT = "tenant";

    @Autowired
    TenantContextFactory _tenantContextFactory;

    /**
     * It returns a list of all components and their configurations.
     * @param tenant
     * @return
     */
    public ComponentList getAllComponents(@PathVariable(TENANT) final String tenant) {
        // TODO
        return null;
    }

    /**
     * It creates a new component with the provided configuration, runs it and returns the result.
     * @param tenant
     * @param name
     * @param processStatelessInput
     * @return
     */
    public ProcessStatelessOutput processStateless(
            @PathVariable(TENANT) final String tenant,
            @PathVariable("name") final String name,
            @RequestBody final ProcessStatelessInput processStatelessInput) {
        LOGGER.debug("Running '" + name + "'");
        ComponentHandler handler = createComponent(tenant, name, processStatelessInput.configuration);
        ProcessStatelessOutput output = new ProcessStatelessOutput();
        output.rows = handler.runComponent(processStatelessInput.data);
        output.result = handler.closeComponent();
        return output;
    }

    /**
     * It runs the component and returns the results.
     */
    public String createComponent(
            @PathVariable(TENANT) final String tenant,
            @PathVariable("name") final String name,
            @PathVariable("timeout") final String timeout,
            @RequestBody final CreateInput createInput) {
        ComponentHandler handler = createComponent(tenant, name, createInput.configuration);
        String id = UUID.randomUUID().toString();
        // TODO: Use cache
        // put "createInput" and "name" to storage and "handler" to cache under the key "id".
        return id;
    }

    /**
     * It returns the continuous result of the component for the provided input data.
     */
    public ProcessOutput processComponent(
            @PathVariable(TENANT) final String tenant,
            @PathVariable("id") final int id,
            @RequestBody final ProcessInput processInput)
            throws ComponentNotFoundException {
        // TODO: Use cache.
        // get handler from cache by "id".
        // If not in cache, get CreateInput and component name object from storage and create the handler and put it to the cache.
        // Use the private method createComponent(...) for it.
        ComponentHandler handler = null;

        ProcessOutput out = new ProcessOutput();
        out.rows = handler.runComponent(processInput.data);
        return out;
    }

    /**
     * It returns the component's final result.
     */
    public ProcessResult getFinalResult(
            @PathVariable(TENANT) final String tenant,
            @PathVariable("id") final int id)
            throws ComponentNotFoundException {
        // TODO - only for analyzers, implement it later after the architecture
        // decisions regarding the load-balancing and failover.
        return null;
    }

    /**
     * It deletes the component.
     */
    public void deleteComponent(
            @PathVariable(TENANT) final String tenant,
            @PathVariable("id") final int id)
            throws ComponentNotFoundException {
        // TODO - delete from cache and storage.
    }

    private ComponentHandler createComponent(String tenant, String componentName, ComponentConfiguration configuration) {
        ComponentHandler handler = new ComponentHandler(
                _tenantContextFactory.getContext(tenant).getConfiguration(),
                componentName);
        handler.createComponent(configuration);
        return handler;
    }

}
