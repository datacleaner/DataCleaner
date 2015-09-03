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
package org.datacleaner.components.rest;

/**
 * @since 02. 09. 2015
 */
public interface ComponentRESTClient {
    /**
     * It returns a list of all available components including their name, description and properties.
     * @return
     */
    public String getAllComponents();

    /**
     * It returns information about a particular component.
     * @param componentName
     * @return
     */
    public String getComponentInfo(final String componentName);

    /**
     * It returns a response for the processed configuration and input data (statelessly).
     * @param componentName
     * @param configurationAndData
     * @return
     */
    public String processStateless(final String componentName, final String configurationAndData);

    /**
     * It returns an unique instance ID (instance of the component and its configuration).
     * @param componentName
     * @param timeout
     * @param configuration
     * @return
     */
    public String createComponent(final String componentName, final String timeout, final String configuration);

    /**
     * It returns a response for the processed input (statefully).
     * @param instanceId
     * @param inputData
     * @return
     */
    public String processComponent(final String instanceId, final String inputData);

    /**
     * It returns a final result of the specified instance ID.
     * @param instanceId
     * @return
     */
    public String getFinalResult(final String instanceId);

    /**
     * It removes the given instance ID.
     * @param instanceId
     */
    public void deleteComponent(final String instanceId);
}
