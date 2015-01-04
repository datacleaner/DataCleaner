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
package org.eobjects.analyzer.job;

import org.eobjects.analyzer.descriptors.DescriptorProvider;

/**
 * Exception thrown in case a job is being opened and it references an
 * unexisting component, such as an analyzer or transformer name which is not
 * resolved using the {@link DescriptorProvider}.
 * 
 * 
 */
public class NoSuchComponentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Class<?> _componentType;
    private final String _componentName;

    public NoSuchComponentException(Class<?> componentType, String componentName) {
        super();
        _componentType = componentType;
        _componentName = componentName;
    }

    @Override
    public String getMessage() {
        return "No such " + (_componentType == null ? "component" : _componentType.getSimpleName())
                + " descriptor: " + _componentName;
    }
    
    public String getComponentName() {
        return _componentName;
    }
    
    public Class<?> getComponentType() {
        return _componentType;
    }
}
