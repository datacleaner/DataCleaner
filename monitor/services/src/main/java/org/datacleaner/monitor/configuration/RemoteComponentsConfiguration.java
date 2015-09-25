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
package org.datacleaner.monitor.configuration;

import java.util.Map;

import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.PropertyDescriptor;

/**
 * Class RemoteComponentsConfiguration
 *
 */
public interface RemoteComponentsConfiguration {


    /**
     * Is the component allowed?
     *
     * @param componentDescriptor
     * @return
     */
    public boolean isAllowed(ComponentDescriptor componentDescriptor);

    /**
     * Is the component allowed?
     *
     * @param componentDisplayName
     * @return
     */
    public boolean isAllowed(String componentDisplayName);

    /**
     * Method returns map with default values of properties.
     *
     * @param componentDescriptor
     */
    public Map<PropertyDescriptor, Object> getDefaultValues(ComponentDescriptor componentDescriptor);
}
