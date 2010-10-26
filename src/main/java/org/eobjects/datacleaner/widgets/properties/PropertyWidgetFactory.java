package org.eobjects.datacleaner.widgets.properties;

import java.io.File;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.util.ReflectionUtils;

public final class PropertyWidgetFactory {

	private PropertyWidgetFactory() {
		// prevent instantiation
	}

	public static PropertyWidget<?> create(AnalysisJobBuilder analysisJobBuilder,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder, ConfiguredPropertyDescriptor propertyDescriptor) {

		if (propertyDescriptor.isInputColumn()) {
			if (propertyDescriptor.isArray()) {
				// use checkboxes but only enable selection with a single row
				// processor (table)
				return new MultipleInputColumnsPropertyWidget(analysisJobBuilder, beanJobBuilder, propertyDescriptor);
			} else {
				// use radio
				return new SingleInputColumnPropertyWidget(analysisJobBuilder, beanJobBuilder, propertyDescriptor);
			}
		}

		Class<?> type = propertyDescriptor.getType();

		if (propertyDescriptor.isArray()) {

		} else {
			if (ReflectionUtils.isCharacter(type)) {
				return new SingleCharacterPropertyWidget(propertyDescriptor, beanJobBuilder);
			}
			if (ReflectionUtils.isString(type)) {
				return new SingleStringPropertyWidget(propertyDescriptor, beanJobBuilder);
			}
			if (ReflectionUtils.isBoolean(type)) {
				return new SingleBooleanPropertyWidget(propertyDescriptor, beanJobBuilder);
			}
			if (ReflectionUtils.isNumber(type)) {
				return new SingleNumberPropertyWidget(propertyDescriptor, beanJobBuilder);
			}
			if (ReflectionUtils.isDate(type)) {
				return new SingleDatePropertyWidget(propertyDescriptor, beanJobBuilder);
			}
			if (type.isEnum()) {
				return new SingleEnumPropertyWidget(propertyDescriptor, beanJobBuilder);
			}
			if (type == File.class) {
				return new SingleFilePropertyWidget(propertyDescriptor, beanJobBuilder);
			}
		}

		// for now we just return this dummy
		return new DummyPropertyWidget(propertyDescriptor);
	}

}
