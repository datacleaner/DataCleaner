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
package org.datacleaner.job;

import org.datacleaner.api.Validate;
import org.datacleaner.descriptors.ComponentDescriptor;

/**
 * Exception thrown if validation (typically using {@link Validate} annotated
 * methods) of a component fails.
 */
public class ComponentValidationException extends ComponentConfigurationException {

    private static final long serialVersionUID = 1L;

    private final ComponentDescriptor<?> _componentDescriptor;
    private final Object _componentInstance;

    public ComponentValidationException(ComponentDescriptor<?> componentDescriptor, Object componentInstance,
            Throwable cause) {
        super(cause.getMessage(), cause);
        _componentDescriptor = componentDescriptor;
        _componentInstance = componentInstance;
    }

    public ComponentDescriptor<?> getComponentDescriptor() {
        return _componentDescriptor;
    }

    public Object getComponentInstance() {
        return _componentInstance;
    }
}
