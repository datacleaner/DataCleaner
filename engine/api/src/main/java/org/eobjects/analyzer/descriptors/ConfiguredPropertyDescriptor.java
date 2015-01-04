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

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Convertable;
import org.eobjects.analyzer.beans.api.Converter;
import org.eobjects.analyzer.util.HasAliases;

/**
 * Descriptor for user-configured properties. Typically such properties are
 * registered using the {@link Configured} annotation.
 * 
 * @see Configured
 * 
 * 
 */
public interface ConfiguredPropertyDescriptor extends PropertyDescriptor, HasAliases {

    /**
     * Determines whether this configured property is an input column (either
     * single instance or array)
     * 
     * @return true if the configured property is a input column type.
     */
    public boolean isInputColumn();

    /**
     * Gets the optional description of the configured property
     * 
     * @return a humanly readable description of the property
     */
    public String getDescription();

    /**
     * Determines whether or not the configured property is required in order
     * for it's component to execute.
     * 
     * @return true if the configured property is required
     */
    public boolean isRequired();

    /**
     * Gets an optional custom {@link Converter} class, in case the configured
     * property should be converted using custom rules.
     * 
     * @see Convertable
     * 
     * @return a custom converter, or null
     */
    public Class<? extends Converter<?>> getCustomConverter();
}
