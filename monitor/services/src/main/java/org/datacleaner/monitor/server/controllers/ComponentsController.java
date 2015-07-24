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

import org.datacleaner.monitor.server.crates.ComponentConfiguration;
import org.datacleaner.monitor.server.crates.ComponentDataInput;
import org.datacleaner.monitor.server.crates.ComponentDataOutput;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for DataCleaner components (transformers and analyzers). It enables to use a particular component
 * and provide the input data separately without any need of the whole job or datastore dcConfiguration.
 * @author j.horcicka (GMC)
 * @since 24. 07. 2015
 */
@RequestMapping("/dc-rest-v1/{tenant}/components")
public interface ComponentsController {
    public static final String MIME_TYPE_JSON = "application/json";

    /**
     * It creates a new component with provided configuration.
     * @param tenant
     * @param inputData
     * @return
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, consumes = MIME_TYPE_JSON, produces = MIME_TYPE_JSON)
    @ResponseStatus(HttpStatus.CREATED)
    public ComponentConfiguration createComponent(final String tenant, final ComponentDataInput inputData);

    /**
     * It returns the component configuration.
     * @param tenant
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MIME_TYPE_JSON)
    public ComponentConfiguration getComponentConfiguration(final String tenant, final int id);

    /**
     * It returns the component's results.
     * @param tenant
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/{id}/results", method = RequestMethod.GET, produces = MIME_TYPE_JSON)
    public String getComponentResult(final String tenant, final int id);

    /**
     * It runs the specified component and returns its results.
     * @param tenant
     * @param inputData
     * @return
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.PUT, produces = MIME_TYPE_JSON)
    public ComponentDataOutput provideInputAndGetResultStateless(final String tenant, final ComponentDataInput inputData);

    /**
     * It runs the component and returns the results.
     * @param tenant
     * @param id
     * @param inputData
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MIME_TYPE_JSON)
    public ComponentDataOutput provideInputAndGetResult(final String tenant, final int id, final ComponentDataInput inputData);

    /**
     * It returns a list of all components' configurations.
     * @param tenant
     * @return
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MIME_TYPE_JSON)
    public List<ComponentConfiguration> getAllComponents(final String tenant);

    /**
     * It returns a list of active components' configurations.
     * @param tenant
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/active", method = RequestMethod.GET, produces = MIME_TYPE_JSON)
    public Map<Integer, ComponentConfiguration> getActiveComponents(final String tenant);

    /**
     * It deletes the component.
     * @param tenant
     * @param id
     */
    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MIME_TYPE_JSON)
    @ResponseStatus(HttpStatus.OK)
    public void deleteComponent(final String tenant, final int id);
}
