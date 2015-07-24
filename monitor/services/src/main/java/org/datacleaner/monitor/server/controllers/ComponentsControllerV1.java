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
import org.datacleaner.monitor.server.crates.ComponentDataInput;
import org.datacleaner.monitor.server.crates.ComponentDataOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for DataCleaner components (transformers and analyzers). It enables to use a particular component
 * and provide the input data separately without any need of the whole job or datastore configuration.
 * @author k.houzvicka, j.horcicka
 * @since 8. 7. 2015
 */
@Controller
public class ComponentsControllerV1 implements ComponentsController {
    private static final Logger logger = LoggerFactory.getLogger(ComponentsControllerV1.class);
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
     * @param componentDataInput
     * @return
     */
    public ComponentDataOutput processStateless(@PathVariable(TENANT) final String tenant,
                                                @PathVariable("name") final String name,
                                                @RequestBody final ComponentDataInput componentDataInput) {
        ComponentHandler componentHandler = new ComponentHandler(
                _tenantContextFactory.getContext(tenant).getConfiguration(), componentDataInput);
        ComponentDataOutput output = new ComponentDataOutput();
        output.setResults(componentHandler.getResults());

        return output;
    }

    /**
     * It runs the component and returns the results.
     * @param tenant
     * @param name
     * @param timeout
     * @param componentProperties
     * @return
     */
    public String createComponent(@PathVariable(TENANT) final String tenant,
                                  @PathVariable("name") final String name,
                                  @PathVariable("timeout") final String timeout,
                                  @RequestBody final ComponentProperties componentProperties) {
        // TODO
        return null;
    }

    /**
     * It returns the continuous result of the component for the provided input data.
     * @param tenant
     * @param id
     * @param inputData
     * @return
     */
    public ComponentResult getContinuousResult(@PathVariable(TENANT) final String tenant,
                                               @PathVariable("id") final int id,
                                               @RequestBody final InputData inputData) {
        // TODO
        return null;
    }

    /**
     * It returns the component's final result.
     * @param tenant
     * @param id
     * @return
     */
    public ComponentResult getFinalResult(@PathVariable(TENANT) final String tenant,
                                          @PathVariable("id") final int id) {
        // TODO
        return null;
    }

    /**
     * It deletes the component.
     * @param tenant
     * @param id
     */
    public void deleteComponent(@PathVariable(TENANT) final String tenant,
                                @PathVariable("id") final int id) {
        // TODO
    }

}
