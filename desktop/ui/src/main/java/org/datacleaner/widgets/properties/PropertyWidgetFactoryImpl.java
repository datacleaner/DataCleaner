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
package org.datacleaner.widgets.properties;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.metamodel.util.Resource;
import org.datacleaner.api.ColumnProperty;
import org.datacleaner.api.HiddenProperty;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.MappedProperty;
import org.datacleaner.api.SchemaProperty;
import org.datacleaner.api.TableProperty;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.UpdateableDatastore;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.guice.DCModule;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.util.ReflectionUtils;

import com.google.inject.Injector;

/**
 * Represents a factory and a catalog of widgets used for @Configured
 * properties. A widget wanting to represent a component with @Configured
 * properties should keep it's reference to the property widget factory and use
 * it to retrieve the properties in case of listener callbacks.
 */
public final class PropertyWidgetFactoryImpl implements PropertyWidgetFactory {

    private final ComponentBuilder _componentBuilder;
    private final PropertyWidgetCollection _propertyWidgetCollection;
    private final DCModule _dcModule;

    @Inject
    protected PropertyWidgetFactoryImpl(final ComponentBuilder componentBuilder, final DCModule dcModule) {
        _componentBuilder = componentBuilder;
        _dcModule = dcModule;
        _propertyWidgetCollection = new PropertyWidgetCollection(componentBuilder);

        final Set<ConfiguredPropertyDescriptor> mappedProperties =
                componentBuilder.getDescriptor().getConfiguredPropertiesByAnnotation(MappedProperty.class);
        for (final ConfiguredPropertyDescriptor mappedProperty : mappedProperties) {
            final MappedProperty annotation = mappedProperty.getAnnotation(MappedProperty.class);
            final String mappedToName = annotation.value();
            final ConfiguredPropertyDescriptor mappedToProperty =
                    componentBuilder.getDescriptor().getConfiguredProperty(mappedToName);

            final PropertyWidgetMapping propertyWidgetMapping =
                    buildMappedPropertyWidget(mappedProperty, mappedToProperty);

            _propertyWidgetCollection.putMappedPropertyWidget(mappedProperty, propertyWidgetMapping);
            _propertyWidgetCollection.putMappedPropertyWidget(mappedToProperty, propertyWidgetMapping);
        }
    }

    @Override
    public PropertyWidgetCollection getPropertyWidgetCollection() {
        return _propertyWidgetCollection;
    }

    protected PropertyWidgetMapping buildMappedPropertyWidget(final ConfiguredPropertyDescriptor mappedProperty,
            final ConfiguredPropertyDescriptor mappedToProperty) {
        if (mappedProperty.isArray() && mappedToProperty.isArray() && mappedToProperty.isInputColumn()) {
            // mapped strings
            if (mappedProperty.getBaseType() == String.class) {
                final MultipleMappedStringsPropertyWidget propertyWidget =
                        new MultipleMappedStringsPropertyWidget(getComponentBuilder(), mappedToProperty,
                                mappedProperty);
                final PropertyWidgetMapping mapping = new PropertyWidgetMapping();
                mapping.putMapping(mappedProperty, propertyWidget.getMappedStringsPropertyWidget());
                mapping.putMapping(mappedToProperty, propertyWidget);
                return mapping;
            }

            // mapped enums
            if (mappedProperty.getBaseType().isEnum()) {
                final MultipleMappedEnumsPropertyWidget propertyWidget =
                        new MultipleMappedEnumsPropertyWidget(getComponentBuilder(), mappedToProperty, mappedProperty);
                final PropertyWidgetMapping mapping = new PropertyWidgetMapping();
                mapping.putMapping(mappedProperty, propertyWidget.getMappedEnumsPropertyWidget());
                mapping.putMapping(mappedToProperty, propertyWidget);
                return mapping;
            }
        }

        // schema structure mapping
        if (mappedProperty.getBaseType() == String.class && !mappedToProperty.isArray()) {
            // save the "mappedToPropertyWidget" since it may be need to be
            // reused when there is a chain of dependencies between mapped
            // properties
            final PropertyWidget<?> mappedToPropertyWidget =
                    _propertyWidgetCollection.getMappedPropertyWidget(mappedToProperty);

            // mapped schema name
            if (mappedProperty.getAnnotation(SchemaProperty.class) != null && (
                    mappedToProperty.getBaseType() == Datastore.class
                            || mappedToProperty.getBaseType() == UpdateableDatastore.class)) {
                final SchemaNamePropertyWidget schemaPropertyWidget =
                        new SchemaNamePropertyWidget(getComponentBuilder(), mappedProperty);
                final SingleDatastorePropertyWidget datastorePropertyWidget;
                if (mappedToPropertyWidget == null) {
                    final DatastoreCatalog datastoreCatalog =
                            getComponentBuilder().getAnalysisJobBuilder().getConfiguration().getDatastoreCatalog();
                    datastorePropertyWidget =
                            new SingleDatastorePropertyWidget(getComponentBuilder(), mappedToProperty, datastoreCatalog,
                                    _dcModule);
                } else {
                    datastorePropertyWidget = (SingleDatastorePropertyWidget) mappedToPropertyWidget;
                }

                datastorePropertyWidget.addComboListener(schemaPropertyWidget::setDatastore);

                final PropertyWidgetMapping mapping = new PropertyWidgetMapping();
                mapping.putMapping(mappedProperty, schemaPropertyWidget);
                mapping.putMapping(mappedToProperty, datastorePropertyWidget);
                return mapping;
            }

            // mapped table name
            if (mappedProperty.getAnnotation(TableProperty.class) != null
                    && mappedToProperty.getAnnotation(SchemaProperty.class) != null) {

                final SingleTableNamePropertyWidget tablePropertyWidget =
                        new SingleTableNamePropertyWidget(getComponentBuilder(), mappedProperty, getWindowContext());
                final SchemaNamePropertyWidget schemaPropertyWidget;
                if (mappedToPropertyWidget == null) {
                    schemaPropertyWidget = new SchemaNamePropertyWidget(getComponentBuilder(), mappedToProperty);
                } else {
                    schemaPropertyWidget = (SchemaNamePropertyWidget) mappedToPropertyWidget;
                }

                schemaPropertyWidget.connectToTableNamePropertyWidget(tablePropertyWidget);

                final PropertyWidgetMapping mapping = new PropertyWidgetMapping();
                mapping.putMapping(mappedProperty, tablePropertyWidget);
                mapping.putMapping(mappedToProperty, schemaPropertyWidget);
                return mapping;
            }

            // mapped column name(s)
            if (mappedProperty.getAnnotation(ColumnProperty.class) != null
                    && mappedToProperty.getAnnotation(TableProperty.class) != null) {

                final SingleTableNamePropertyWidget tablePropertyWidget;
                if (mappedToPropertyWidget == null) {
                    tablePropertyWidget = new SingleTableNamePropertyWidget(getComponentBuilder(), mappedToProperty,
                            getWindowContext());
                } else {
                    tablePropertyWidget = (SingleTableNamePropertyWidget) mappedToPropertyWidget;
                }

                if (mappedProperty.isArray()) {
                    // multiple mapped column names

                    // TODO: Not yet implemented. This case needs to take care
                    // of the fact that usually this is then ALSO mapped to an
                    // array of input columns.
                } else {
                    // mapped column name

                    final SingleColumnNamePropertyWidget columnPropertyWidget =
                            new SingleColumnNamePropertyWidget(mappedProperty, getComponentBuilder());
                    tablePropertyWidget.addComboListener(columnPropertyWidget::setTable);

                    final PropertyWidgetMapping mapping = new PropertyWidgetMapping();
                    mapping.putMapping(mappedProperty, columnPropertyWidget);
                    mapping.putMapping(mappedToProperty, tablePropertyWidget);
                    return mapping;
                }
            }
        }

        return null;
    }

    private WindowContext getWindowContext() {
        return _dcModule.createChildInjectorForComponent(_componentBuilder).getInstance(WindowContext.class);
    }

    @Override
    public ComponentBuilder getComponentBuilder() {
        return _componentBuilder;
    }

    protected Injector getInjectorForPropertyWidgets(final ConfiguredPropertyDescriptor propertyDescriptor) {
        return _dcModule.createChildInjectorForProperty(_componentBuilder, propertyDescriptor);
    }

    @Override
    public PropertyWidget<?> create(final String propertyName) {
        final ComponentDescriptor<?> descriptor = _componentBuilder.getDescriptor();
        final ConfiguredPropertyDescriptor propertyDescriptor = descriptor.getConfiguredProperty(propertyName);
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
    @Override
    public PropertyWidget<?> create(final ConfiguredPropertyDescriptor propertyDescriptor) {
        // first check if there is a mapping created for this property
        // descriptor
        final PropertyWidget<?> propertyWidget = _propertyWidgetCollection.getMappedPropertyWidget(propertyDescriptor);
        if (propertyWidget != null) {
            return propertyWidget;
        }
        final HiddenProperty hiddenProperty = propertyDescriptor.getAnnotation(HiddenProperty.class);
        if (hiddenProperty != null && hiddenProperty.hiddenForLocalAccess()) {
            return null;
        }
        if (propertyDescriptor.getAnnotation(Deprecated.class) != null) {
            return null;
        }

        if (getComponentBuilder() instanceof AnalyzerComponentBuilder) {
            final AnalyzerComponentBuilder<?> analyzer = (AnalyzerComponentBuilder<?>) getComponentBuilder();
            if (analyzer.isMultipleJobsSupported()) {
                if (analyzer.isMultipleJobsDeterminedBy(propertyDescriptor)) {
                    return new MultipleInputColumnsPropertyWidget(analyzer, propertyDescriptor);
                }
            }
        }

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
                if (_componentBuilder.getDescriptor().getConfiguredPropertiesByType(InputColumn.class, true).size()
                        == 1) {
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
            } else if (type == Map.class) {
                final Class<?> genericType1 = propertyDescriptor.getTypeArgument(0);
                final Class<?> genericType2 = propertyDescriptor.getTypeArgument(1);
                if (genericType1 == String.class && genericType2 == String.class) {
                    widgetClass = MapStringToStringPropertyWidget.class;
                } else {
                    // not yet implemented
                    widgetClass = DummyPropertyWidget.class;
                }
            } else {
                // not yet implemented
                widgetClass = DummyPropertyWidget.class;
            }
        }

        final Injector injector = getInjectorForPropertyWidgets(propertyDescriptor);
        return injector.getInstance(widgetClass);
    }
}
