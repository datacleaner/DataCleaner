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
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.server.components.ComponentHandler;
import org.datacleaner.monitor.server.components.ComponentNotFoundException;
import org.datacleaner.monitor.server.crates.ComponentConfiguration;
import org.datacleaner.monitor.server.crates.ComponentDataInput;
import org.datacleaner.monitor.server.crates.ComponentDataOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for DataCleaner components (transformers and analyzers). It enables to use a particular component
 * and provide the input data separately without any need of the whole job or datastore dcConfiguration.
 * @author k.houzvicka, j.horcicka
 * @since 8. 7. 2015
 */
@Controller
public class ComponentsControllerV1 implements ComponentsController {
    private static final Logger logger = LoggerFactory.getLogger(ComponentsControllerV1.class);
    private static final String TENANT = "tenant";

    @Autowired
    TenantContextFactory _tenantContextFactory;
    private TenantContext tenantContext;
    private DataCleanerConfiguration dcConfiguration;
    // This object will be in tenant Config object
    private Map<Integer, ComponentConfiguration> configurationMap = new HashMap<>();

    public ComponentConfiguration createComponent(@PathVariable(TENANT) final String tenant,
                                                  @RequestBody final ComponentDataInput inputData) {
        init(tenant);
        ComponentConfiguration componentConfiguration = inputData.getConfiguration();
        new ComponentHandler(dcConfiguration, inputData);
        configurationMap.put(componentConfiguration.getId(), componentConfiguration);
        logger.info("Component " + componentConfiguration.getId() + " was created. ");

        return inputData.getConfiguration();
    }

    public ComponentConfiguration getComponentConfiguration(@PathVariable(TENANT) final String tenant,
                                                            @PathVariable final int id) {
        init(tenant);
        checkExistingComponent(id);

        return configurationMap.get(id);
    }

    public String getComponentResult(@PathVariable(TENANT) final String tenant,
                                     @PathVariable final int id) {
        init(tenant);
        checkExistingComponent(id);

        return "Test result " + id;
    }

    public ComponentDataOutput provideInputAndGetResultStateless(@PathVariable(TENANT) final String tenant,
                                                                 @RequestBody final ComponentDataInput inputData) {
        init(tenant);
        ComponentHandler componentHandler = new ComponentHandler(dcConfiguration, inputData);
        ComponentDataOutput output = new ComponentDataOutput();
        output.setResults(componentHandler.getResults());

        return output;
    }

    public ComponentDataOutput provideInputAndGetResult(@PathVariable(TENANT) final String tenant,
                                                        @PathVariable final int id,
                                                        @RequestBody final ComponentDataInput inputData) {
        init(tenant);
        checkExistingComponent(id);
        ComponentHandler componentHandler = new ComponentHandler(dcConfiguration, inputData);
        ComponentDataOutput output = new ComponentDataOutput();
        output.setResults(componentHandler.getResults());

        return output;
    }

    public List<ComponentConfiguration> getAllComponents(@PathVariable(TENANT) final String tenant) {
        init(tenant);

        ComponentConfiguration componentAConfiguration = new ComponentConfiguration();
        HashMap<String, String> componentAMap = new HashMap<>();
        componentAMap.put("A", "X");
        componentAConfiguration.setPropertiesMap(componentAMap);

        ComponentConfiguration componentBConfiguration = new ComponentConfiguration();
        HashMap<String, String> componentBMap = new HashMap<>(componentAMap);
        componentBMap.put("B", "Y");
        componentBConfiguration.setPropertiesMap(componentBMap);

        return Arrays.asList(componentAConfiguration, componentBConfiguration);
    }

    public Map<Integer, ComponentConfiguration> getActiveComponents(@PathVariable(TENANT) final String tenant) {
        init(tenant);

        return configurationMap;
    }

    public void deleteComponent(@PathVariable(TENANT) final String tenant,
                                @PathVariable final int id) {
        init(tenant);

        checkExistingComponent(id);
        configurationMap.remove(id);
        logger.info("Component " + id + " was deleted. ");
    }

    private void checkExistingComponent(int id) throws ComponentNotFoundException {
        if (! configurationMap.containsKey(id)) {
            throw new ComponentNotFoundException(id);
        }
    }

    private void init(String tenant) {
        tenantContext = _tenantContextFactory.getContext(tenant);
        dcConfiguration = tenantContext.getConfiguration();
    }
}
