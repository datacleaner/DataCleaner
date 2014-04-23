/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

package org.eobjects.datacleaner.widgets.properties;

import java.util.IdentityHashMap;
import java.util.Map;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;

class PropertyWidgetMapping {

    private final Map<ConfiguredPropertyDescriptor, PropertyWidget<?>> _mapping;

    public PropertyWidgetMapping() {
        _mapping = new IdentityHashMap<ConfiguredPropertyDescriptor, PropertyWidget<?>>(3);
    }

    public void putMapping(ConfiguredPropertyDescriptor property, PropertyWidget<?> propertyWidget) {
        _mapping.put(property, propertyWidget);
    }

    public PropertyWidget<?> getMapping(ConfiguredPropertyDescriptor property) {
        return _mapping.get(property);
    }
}
