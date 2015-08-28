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

import org.datacleaner.monitor.configuration.CreateInput;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller for DataCleaner components (transformers and analyzers). It enables to use a particular component
 * and provide the input data separately without any need of the whole job or datastore dcConfiguration.
 * @since 24. 07. 2015
 */
@RequestMapping("/{tenant}/components")
public interface ComponentsController {
    /**
     * It returns a list of all components and their configurations.
     * @param tenant
     * @return
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ComponentList getAllComponents(final String tenant);

    @ResponseBody
    @RequestMapping(value = "/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ComponentList.ComponentInfo getComponentInfo(final String tenant, final String name);

    /**
     * It creates a new component with the provided configuration, runs it and returns the result.
     * @param tenant
     * @param name
     * @param processStatelessInput
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/{name}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ProcessStatelessOutput processStateless(final String tenant, final String name,
        final ProcessStatelessInput processStatelessInput);

    /**
     * It runs the component and returns the results.
     *
     * @param tenant
     * @param name
     * @param timeout
     * @param config
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/{name}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public String createComponent(final String tenant, final String name, final String timeout,
        final CreateInput config);

    /**
     * It returns the continuous result of the component for the provided input data.
     * @param tenant
     * @param id
     * @param processInput
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/_instance/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProcessOutput processComponent(final String tenant, final String id, final ProcessInput processInput)
            throws ComponentNotFoundException;

    /**
     * It returns the component's final result.
     * @param tenant
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/{id}/result", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProcessResult getFinalResult(final String tenant, final String id)
            throws ComponentNotFoundException;

    /**
     * It deletes the component.
     * @param tenant
     * @param id
     */
    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteComponent(final String tenant, final String id)
            throws ComponentNotFoundException;
}
