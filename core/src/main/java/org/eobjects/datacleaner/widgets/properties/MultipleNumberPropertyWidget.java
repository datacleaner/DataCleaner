package org.eobjects.datacleaner.widgets.properties;

import javax.inject.Inject;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;

public class MultipleNumberPropertyWidget extends AbstractMultipleNumberPropertyWidget<Number[]> {

	private static final long serialVersionUID = 1L;

	@Inject
	public MultipleNumberPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(propertyDescriptor, beanJobBuilder);
	}
}
