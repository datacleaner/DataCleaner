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
package org.eobjects.datacleaner.widgets.properties;

import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;

/**
 * Contains the {@link PropertyWidget}s of a particular component
 */
public class PropertyWidgetCollection {

    private final AbstractBeanJobBuilder<?, ?, ?> _beanJobBuilder;
    private final Map<ConfiguredPropertyDescriptor, PropertyWidget<?>> _widgets;
    private final Map<ConfiguredPropertyDescriptor, PropertyWidgetMapping> _propertyWidgetMappings;

    public PropertyWidgetCollection(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
        _beanJobBuilder = beanJobBuilder;
        _widgets = new HashMap<ConfiguredPropertyDescriptor, PropertyWidget<?>>();
        _propertyWidgetMappings = new IdentityHashMap<ConfiguredPropertyDescriptor, PropertyWidgetMapping>();
    }

    public Collection<PropertyWidget<?>> getWidgets() {
        return _widgets.values();
    }

    public PropertyWidget<?> getWidget(ConfiguredPropertyDescriptor propertyDescriptor) {
        return _widgets.get(propertyDescriptor);
    }

    public void putMappedPropertyWidget(ConfiguredPropertyDescriptor mappedProperty,
            PropertyWidgetMapping propertyWidgetMapping) {
        _propertyWidgetMappings.put(mappedProperty, propertyWidgetMapping);
    }

    public PropertyWidget<?> getMappedPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor) {
        final PropertyWidgetMapping existingMapping = _propertyWidgetMappings.get(propertyDescriptor);
        if (existingMapping != null) {
            PropertyWidget<?> propertyWidget = existingMapping.getMapping(propertyDescriptor);
            if (propertyWidget != null) {
                return propertyWidget;
            }
        }
        return null;
    }

    /**
     * Registers a widget in this factory in rare cases when the factory is not
     * used to actually instantiate the widget, but it is still needed to
     * register the widget for compliancy with eg. the onConfigurationChanged()
     * behaviour.
     * 
     * @param propertyDescriptor
     * @param widget
     */
    public void registerWidget(ConfiguredPropertyDescriptor propertyDescriptor, PropertyWidget<?> widget) {
        if (widget == null) {
            _widgets.remove(propertyDescriptor);
        } else {
            _widgets.put(propertyDescriptor, widget);
            @SuppressWarnings("unchecked")
            PropertyWidget<Object> objectWidget = (PropertyWidget<Object>) widget;
            Object value = _beanJobBuilder.getConfiguredProperty(objectWidget.getPropertyDescriptor());
            objectWidget.initialize(value);
        }
    }

    /**
     * Invoked whenever a configured property within this widget factory is
     * changed.
     */
    public void onConfigurationChanged() {
        final Collection<PropertyWidget<?>> widgets = getWidgets();

        for (PropertyWidget<?> widget : widgets) {
            @SuppressWarnings("unchecked")
            final PropertyWidget<Object> objectWidget = (PropertyWidget<Object>) widget;
            final ConfiguredPropertyDescriptor propertyDescriptor = objectWidget.getPropertyDescriptor();
            final Object value = _beanJobBuilder.getConfiguredProperty(propertyDescriptor);
            objectWidget.onValueTouched(value);
        }
    }
}
