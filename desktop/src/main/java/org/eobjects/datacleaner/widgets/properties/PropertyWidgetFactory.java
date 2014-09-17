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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.ColumnProperty;
import org.eobjects.analyzer.beans.api.MappedProperty;
import org.eobjects.analyzer.beans.api.SchemaProperty;
import org.eobjects.analyzer.beans.api.TableProperty;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.UpdateableDatastore;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.PropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.guice.InjectorBuilder;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.Resource;

import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * Represents a factory and a catalog of widgets used for @Configured
 * properties. A widget wanting to represent an AnalyzerBeans component with @Configured
 * properties should keep it's reference to the property widget factory and use
 * it to retrieve the properties in case of listener callbacks.
 */
public final class PropertyWidgetFactory {

    /**
     * Convenient type literal that can be used with Guice's binding mechanism
     * to bind to the {@link AbstractBeanJobBuilder} argument of this class's
     * constructor.
     */
    public static final TypeLiteral<AbstractBeanJobBuilder<?, ?, ?>> TYPELITERAL_BEAN_JOB_BUILDER = new TypeLiteral<AbstractBeanJobBuilder<?, ?, ?>>() {
    };

    private final AbstractBeanJobBuilder<?, ?, ?> _beanJobBuilder;
    private final Map<ConfiguredPropertyDescriptor, PropertyWidget<?>> _widgets = new HashMap<ConfiguredPropertyDescriptor, PropertyWidget<?>>();
    private final InjectorBuilder _injectorBuilder;

    private final Map<ConfiguredPropertyDescriptor, PropertyWidgetMapping> _propertyWidgetMappings;

    @Inject
    protected PropertyWidgetFactory(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder, InjectorBuilder injectorBuilder) {
        _beanJobBuilder = beanJobBuilder;
        _injectorBuilder = injectorBuilder;
        _propertyWidgetMappings = new IdentityHashMap<ConfiguredPropertyDescriptor, PropertyWidgetMapping>();

        Set<ConfiguredPropertyDescriptor> mappedProperties = beanJobBuilder.getDescriptor()
                .getConfiguredPropertiesByAnnotation(MappedProperty.class);
        for (ConfiguredPropertyDescriptor mappedProperty : mappedProperties) {
            MappedProperty annotation = mappedProperty.getAnnotation(MappedProperty.class);
            String mappedToName = annotation.value();
            ConfiguredPropertyDescriptor mappedToProperty = beanJobBuilder.getDescriptor().getConfiguredProperty(
                    mappedToName);

            PropertyWidgetMapping propertyWidgetMapping = buildMappedPropertyWidget(mappedProperty, mappedToProperty);

            _propertyWidgetMappings.put(mappedProperty, propertyWidgetMapping);
            _propertyWidgetMappings.put(mappedToProperty, propertyWidgetMapping);
        }
    }

    protected PropertyWidgetMapping buildMappedPropertyWidget(ConfiguredPropertyDescriptor mappedProperty,
            ConfiguredPropertyDescriptor mappedToProperty) {
        if (mappedProperty.isArray() && mappedToProperty.isArray() && mappedToProperty.isInputColumn()) {
            // mapped strings
            if (mappedProperty.getBaseType() == String.class) {
                final MultipleMappedStringsPropertyWidget propertyWidget = new MultipleMappedStringsPropertyWidget(
                        getBeanJobBuilder(), mappedToProperty, mappedProperty);
                final PropertyWidgetMapping mapping = new PropertyWidgetMapping();
                mapping.putMapping(mappedProperty, propertyWidget);
                mapping.putMapping(mappedToProperty, propertyWidget.getMappedStringsPropertyWidget());
                return mapping;
            }

            // mapped enums
            if (mappedProperty.getBaseType().isEnum()) {
                final MultipleMappedEnumsPropertyWidget<Enum<?>> propertyWidget = new MultipleMappedEnumsPropertyWidget<Enum<?>>(
                        getBeanJobBuilder(), mappedToProperty, mappedProperty);
                final PropertyWidgetMapping mapping = new PropertyWidgetMapping();
                mapping.putMapping(mappedProperty, propertyWidget);
                mapping.putMapping(mappedToProperty, propertyWidget.getMappedEnumsPropertyWidget());
                return mapping;
            }
        }

        // schema structure mapping
        if (mappedProperty.getBaseType() == String.class && !mappedToProperty.isArray()) {
            // save the "mappedToPropertyWidget" since it may be need to be
            // reused when there is a chain of dependencies between mapped
            // properties
            final PropertyWidgetMapping propertyWidgetMapping = _propertyWidgetMappings.get(mappedToProperty);
            final PropertyWidget<?> mappedToPropertyWidget;
            if (propertyWidgetMapping == null) {
                mappedToPropertyWidget = null;
            } else {
                mappedToPropertyWidget = propertyWidgetMapping.getMapping(mappedToProperty);
            }

            // mapped schema name
            if (mappedProperty.getAnnotation(SchemaProperty.class) != null
                    && (mappedToProperty.getBaseType() == Datastore.class || mappedToProperty.getBaseType() == UpdateableDatastore.class)) {
                final SchemaNamePropertyWidget schemaPropertyWidget = new SchemaNamePropertyWidget(getBeanJobBuilder(),
                        mappedProperty);
                final SingleDatastorePropertyWidget datastorePropertyWidget;
                if (mappedToPropertyWidget == null) {
                    final DatastoreCatalog datastoreCatalog = getBeanJobBuilder().getAnalysisJobBuilder()
                            .getConfiguration().getDatastoreCatalog();
                    datastorePropertyWidget = new SingleDatastorePropertyWidget(getBeanJobBuilder(), mappedToProperty,
                            datastoreCatalog);
                } else {
                    datastorePropertyWidget = (SingleDatastorePropertyWidget) mappedToPropertyWidget;
                }

                datastorePropertyWidget.addComboListener(new DCComboBox.Listener<Datastore>() {
                    @Override
                    public void onItemSelected(Datastore item) {
                        schemaPropertyWidget.setDatastore(item);
                    }
                });

                final PropertyWidgetMapping mapping = new PropertyWidgetMapping();
                mapping.putMapping(mappedProperty, schemaPropertyWidget);
                mapping.putMapping(mappedToProperty, datastorePropertyWidget);
                return mapping;
            }

            // mapped table name
            if (mappedProperty.getAnnotation(TableProperty.class) != null
                    && mappedToProperty.getAnnotation(SchemaProperty.class) != null) {

                final TableNamePropertyWidget tablePropertyWidget = new TableNamePropertyWidget(getBeanJobBuilder(),
                        mappedProperty);
                final SchemaNamePropertyWidget schemaPropertyWidget;
                if (mappedToPropertyWidget == null) {
                    schemaPropertyWidget = new SchemaNamePropertyWidget(getBeanJobBuilder(), mappedToProperty);
                } else {
                    schemaPropertyWidget = (SchemaNamePropertyWidget) mappedToPropertyWidget;
                }

                schemaPropertyWidget.addComboListener(new DCComboBox.Listener<Schema>() {
                    @Override
                    public void onItemSelected(Schema item) {
                        tablePropertyWidget.setSchema(item);
                    }
                });

                final PropertyWidgetMapping mapping = new PropertyWidgetMapping();
                mapping.putMapping(mappedProperty, tablePropertyWidget);
                mapping.putMapping(mappedToProperty, schemaPropertyWidget);
                return mapping;
            }

            // mapped column name(s)
            if (mappedProperty.getAnnotation(ColumnProperty.class) != null
                    && mappedToProperty.getAnnotation(TableProperty.class) != null) {

                final TableNamePropertyWidget tablePropertyWidget;
                if (mappedToPropertyWidget == null) {
                    tablePropertyWidget = new TableNamePropertyWidget(getBeanJobBuilder(), mappedToProperty);
                } else {
                    tablePropertyWidget = (TableNamePropertyWidget) mappedToPropertyWidget;
                }

                if (mappedProperty.isArray()) {
                    // multiple mapped column names

                    // TODO: Not yet implemented. This case needs to take care
                    // of the fact that usually this is then ALSO mapped to an
                    // array of input columns.
                } else {
                    // mapped column name

                    final ColumnNamePropertyWidget columnPropertyWidget = new ColumnNamePropertyWidget(mappedProperty,
                            getBeanJobBuilder());
                    tablePropertyWidget.addComboListener(new DCComboBox.Listener<Table>() {
                        @Override
                        public void onItemSelected(Table item) {
                            columnPropertyWidget.setTable(item);
                        }
                    });

                    final PropertyWidgetMapping mapping = new PropertyWidgetMapping();
                    mapping.putMapping(mappedProperty, columnPropertyWidget);
                    mapping.putMapping(mappedToProperty, tablePropertyWidget);
                    return mapping;
                }
            }
        }

        return null;
    }

    public Collection<PropertyWidget<?>> getWidgets() {
        return _widgets.values();
    }

    public PropertyWidget<?> getWidget(ConfiguredPropertyDescriptor propertyDescriptor) {
        return _widgets.get(propertyDescriptor);
    }

    public AbstractBeanJobBuilder<?, ?, ?> getBeanJobBuilder() {
        return _beanJobBuilder;
    }

    public Injector getInjectorForPropertyWidgets(ConfiguredPropertyDescriptor propertyDescriptor) {
        return _injectorBuilder.inherit(TYPELITERAL_BEAN_JOB_BUILDER)
                .with(ConfiguredPropertyDescriptor.class, propertyDescriptor)
                .with(PropertyDescriptor.class, propertyDescriptor).createInjector();
    }

    public PropertyWidget<?> create(String propertyName) {
        BeanDescriptor<?> descriptor = _beanJobBuilder.getDescriptor();
        ConfiguredPropertyDescriptor propertyDescriptor = descriptor.getConfiguredProperty(propertyName);
        if (propertyDescriptor == null) {
            throw new IllegalArgumentException("No such property: " + propertyName);
        }
        return create(propertyDescriptor);
    }

    /**
     * Creates (and registers) a widget that fits the specified configured
     * property.
     * 
     * @param propertyDescriptor
     * @return
     */
    public PropertyWidget<?> create(ConfiguredPropertyDescriptor propertyDescriptor) {
        final PropertyWidget<?> result;

        // first check if there is a mapping created for this property
        // descriptor
        PropertyWidget<?> propertyWidget = getMappedPropertyWidget(propertyDescriptor);
        if (propertyWidget != null) {
            result = propertyWidget;
        } else {
            // check for fitting property widgets by type
            final Class<?> type = propertyDescriptor.getBaseType();

            final Class<? extends PropertyWidget<?>> widgetClass;
            if (propertyDescriptor.isArray()) {
                if (propertyDescriptor.isInputColumn()) {
                    widgetClass = MultipleInputColumnsPropertyWidget.class;
                } else if (ReflectionUtils.isString(type)) {
                    widgetClass = MultipleStringPropertyWidget.class;
                } else if (type == Dictionary.class) {
                    widgetClass = MultipleDictionariesPropertyWidget.class;
                } else if (type == SynonymCatalog.class) {
                    widgetClass = MultipleSynonymCatalogsPropertyWidget.class;
                } else if (type == StringPattern.class) {
                    widgetClass = MultipleStringPatternPropertyWidget.class;
                } else if (type.isEnum()) {
                    widgetClass = MultipleEnumPropertyWidget.class;
                } else if (type == Class.class) {
                    widgetClass = MultipleClassesPropertyWidget.class;
                } else if (type == char.class) {
                    widgetClass = MultipleCharPropertyWidget.class;
                } else if (ReflectionUtils.isNumber(type)) {
                    widgetClass = MultipleNumberPropertyWidget.class;
                } else {
                    // not yet implemented
                    widgetClass = DummyPropertyWidget.class;
                }
            } else {

                if (propertyDescriptor.isInputColumn()) {
                    if (_beanJobBuilder.getDescriptor().getConfiguredPropertiesForInput().size() == 1) {
                        // if there is only a single input column property, it
                        // will
                        // be displayed using radiobuttons.
                        widgetClass = SingleInputColumnRadioButtonPropertyWidget.class;
                    } else {
                        // if there are multiple input column properties, they
                        // will
                        // be displayed using combo boxes.
                        widgetClass = SingleInputColumnComboBoxPropertyWidget.class;
                    }
                } else if (ReflectionUtils.isCharacter(type)) {
                    widgetClass = SingleCharacterPropertyWidget.class;
                } else if (ReflectionUtils.isString(type)) {
                    widgetClass = SingleStringPropertyWidget.class;
                } else if (ReflectionUtils.isBoolean(type)) {
                    widgetClass = SingleBooleanPropertyWidget.class;
                } else if (ReflectionUtils.isNumber(type)) {
                    widgetClass = SingleNumberPropertyWidget.class;
                } else if (ReflectionUtils.isDate(type)) {
                    widgetClass = SingleDatePropertyWidget.class;
                } else if (type == Dictionary.class) {
                    widgetClass = SingleDictionaryPropertyWidget.class;
                } else if (type == SynonymCatalog.class) {
                    widgetClass = SingleSynonymCatalogPropertyWidget.class;
                } else if (type == StringPattern.class) {
                    widgetClass = SingleStringPatternPropertyWidget.class;
                } else if (type.isEnum()) {
                    widgetClass = SingleEnumPropertyWidget.class;
                } else if (ReflectionUtils.is(type, Resource.class)) {
                    widgetClass = SingleResourcePropertyWidget.class;
                } else if (type == File.class) {
                    widgetClass = SingleFilePropertyWidget.class;
                } else if (type == Pattern.class) {
                    widgetClass = SinglePatternPropertyWidget.class;
                } else if (ReflectionUtils.is(type, Datastore.class)) {
                    widgetClass = SingleDatastorePropertyWidget.class;
                } else if (type == Class.class) {
                    widgetClass = SingleClassPropertyWidget.class;
                } else {
                    // not yet implemented
                    widgetClass = DummyPropertyWidget.class;
                }
            }

            final Injector injector = getInjectorForPropertyWidgets(propertyDescriptor);
            result = injector.getInstance(widgetClass);
        }

        registerWidget(propertyDescriptor, result);
        return result;
    }

    private PropertyWidget<?> getMappedPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor) {
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
