/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.PropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.guice.InjectorBuilder;

import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * Represents a factory and a catalog of widgets used for @Configured
 * properties. A widget wanting to represent an AnalyzerBeans component with @Configured
 * properties should keep it's reference to the property widget factory and use
 * it to retrieve the properties in case of listener callbacks.
 * 
 * @author Kasper Sørensen
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

	@Inject
	protected PropertyWidgetFactory(
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			InjectorBuilder injectorBuilder) {
		_beanJobBuilder = beanJobBuilder;
		_injectorBuilder = injectorBuilder;
	}

	public Collection<PropertyWidget<?>> getWidgets() {
		return _widgets.values();
	}

	public PropertyWidget<?> getWidget(
			ConfiguredPropertyDescriptor propertyDescriptor) {
		return _widgets.get(propertyDescriptor);
	}

	public AbstractBeanJobBuilder<?, ?, ?> getBeanJobBuilder() {
		return _beanJobBuilder;
	}

	public Injector getInjectorForPropertyWidgets(
			ConfiguredPropertyDescriptor propertyDescriptor) {
		return _injectorBuilder.inherit(TYPELITERAL_BEAN_JOB_BUILDER)
				.with(ConfiguredPropertyDescriptor.class, propertyDescriptor)
				.with(PropertyDescriptor.class, propertyDescriptor)
				.createInjector();
	}

	public PropertyWidget<?> create(String propertyName) {
		BeanDescriptor<?> descriptor = _beanJobBuilder.getDescriptor();
		ConfiguredPropertyDescriptor propertyDescriptor = descriptor
				.getConfiguredProperty(propertyName);
		if (propertyDescriptor == null) {
			throw new IllegalArgumentException("No such property: "
					+ propertyName);
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
	public PropertyWidget<?> create(
			ConfiguredPropertyDescriptor propertyDescriptor) {
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
				if (_beanJobBuilder.getDescriptor()
						.getConfiguredPropertiesForInput().size() == 1) {
					// if there is only a single input column property, it will
					// be displayed using radiobuttons.
					widgetClass = SingleInputColumnRadioButtonPropertyWidget.class;
				} else {
					// if there are multiple input column properties, they will
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
		final PropertyWidget<?> result = injector.getInstance(widgetClass);

		registerWidget(propertyDescriptor, result);
		return result;
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
	public void registerWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			PropertyWidget<?> widget) {
		if (widget == null) {
			_widgets.remove(propertyDescriptor);
		} else {
			_widgets.put(propertyDescriptor, widget);
			@SuppressWarnings("unchecked")
			PropertyWidget<Object> objectWidget = (PropertyWidget<Object>) widget;
			Object value = _beanJobBuilder.getConfiguredProperty(objectWidget
					.getPropertyDescriptor());
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
			final ConfiguredPropertyDescriptor propertyDescriptor = objectWidget
					.getPropertyDescriptor();
			final Object value = _beanJobBuilder
					.getConfiguredProperty(propertyDescriptor);
			objectWidget.onValueTouched(value);
		}
	}
}
