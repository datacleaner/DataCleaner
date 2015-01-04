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
package org.eobjects.analyzer.descriptors;

import java.util.Set;

import org.eobjects.analyzer.beans.api.Distributed;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.util.HasAliases;

/**
 * Defines an abstract descriptor for beans (Analyzers, Transformers, Filters)
 * that support configured properties, provided properties, initialize methods
 * and close methods.
 * 
 * @param <B>
 *            the Bean type
 */
public interface BeanDescriptor<B> extends ComponentDescriptor<B>, HasAliases {

    /**
     * Determines if the bean is a distributable component or not.
     * 
     * @return true if the component can be distributed.
     * 
     * @see Distributed
     */
    public boolean isDistributable();

    /**
     * Gets the configured properties that have {@link InputColumn} type.
     * 
     * @return a set containing all configured property descriptors of
     *         {@link InputColumn}s in the bean.
     */
    public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesForInput();

    /**
     * Gets the configured properties that have {@link InputColumn} type.
     * 
     * @param onlyRequired
     *            a boolean indicating if optional properties should be
     *            returned. If false, only required properties will be included.
     * @return a set containing all configured property descriptors of
     *         {@link InputColumn}s in the bean.
     */
    public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesForInput(boolean includeOptional);
}
