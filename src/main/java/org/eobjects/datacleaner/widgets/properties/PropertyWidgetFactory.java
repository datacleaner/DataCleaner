package org.eobjects.datacleaner.widgets.properties;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.analyzer.util.ReflectionUtils;

/**
 * Represents a factory and a catalog of widgets used for @Configured
 * properties. A widget wanting to represent an AnalyzerBeans component with @Configured
 * properties should keep it's reference to the property widget factory and use
 * it to retrieve the properties in case of listener callbacks.
 * 
 * @author Kasper Sørensen
 */
public final class PropertyWidgetFactory {

	private final AbstractBeanJobBuilder<?, ?, ?> _beanJobBuilder;
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final Map<ConfiguredPropertyDescriptor, PropertyWidget<?>> _widgets = new HashMap<ConfiguredPropertyDescriptor, PropertyWidget<?>>();

	public PropertyWidgetFactory(AnalysisJobBuilder analysisJobBuilder, AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		_analysisJobBuilder = analysisJobBuilder;
		_beanJobBuilder = beanJobBuilder;
	}

	public Collection<PropertyWidget<?>> getWidgets() {
		return _widgets.values();
	}

	public PropertyWidget<?> create(ConfiguredPropertyDescriptor propertyDescriptor) {
		final PropertyWidget<?> result;
		final Class<?> type = propertyDescriptor.getBaseType();

		if (propertyDescriptor.isArray()) {
			if (propertyDescriptor.isInputColumn()) {
				result = new MultipleInputColumnsPropertyWidget(_analysisJobBuilder, _beanJobBuilder, propertyDescriptor);
			} else if (ReflectionUtils.isString(type)) {
				result = new MultipleStringPropertyWidget(propertyDescriptor, _beanJobBuilder);
			} else if (type == Dictionary.class) {
				result = new MultipleDictionariesPropertyWidget(_beanJobBuilder, propertyDescriptor);
			} else if (type == SynonymCatalog.class) {
				result = new MultipleSynonymCatalogsPropertyWidget(_beanJobBuilder, propertyDescriptor);
			} else {
				// not yet implemented
				result = new DummyPropertyWidget(propertyDescriptor);
			}
		} else {
			if (propertyDescriptor.isInputColumn()) {
				result = new SingleInputColumnPropertyWidget(_analysisJobBuilder, _beanJobBuilder, propertyDescriptor);
			} else if (ReflectionUtils.isCharacter(type)) {
				result = new SingleCharacterPropertyWidget(propertyDescriptor, _beanJobBuilder);
			} else if (ReflectionUtils.isString(type)) {
				result = new SingleStringPropertyWidget(propertyDescriptor, _beanJobBuilder);
			} else if (ReflectionUtils.isBoolean(type)) {
				result = new SingleBooleanPropertyWidget(propertyDescriptor, _beanJobBuilder);
			} else if (ReflectionUtils.isNumber(type)) {
				result = new SingleNumberPropertyWidget(propertyDescriptor, _beanJobBuilder);
			} else if (ReflectionUtils.isDate(type)) {
				result = new SingleDatePropertyWidget(propertyDescriptor, _beanJobBuilder);
			} else if (type == Dictionary.class) {
				result = new SingleDictionaryPropertyWidget(propertyDescriptor, _beanJobBuilder);
			} else if (type == SynonymCatalog.class) {
				result = new SingleSynonymCatalogPropertyWidget(propertyDescriptor, _beanJobBuilder);
			} else if (type.isEnum()) {
				result = new SingleEnumPropertyWidget(propertyDescriptor, _beanJobBuilder);
			} else if (type == File.class) {
				result = new SingleFilePropertyWidget(propertyDescriptor, _beanJobBuilder);
			} else if (type == Pattern.class) {
				result = new PatternPropertyWidget(propertyDescriptor, _beanJobBuilder);
			} else {
				// not yet implemented
				result = new DummyPropertyWidget(propertyDescriptor);
			}
		}

		_widgets.put(propertyDescriptor, result);
		return result;
	}
}
