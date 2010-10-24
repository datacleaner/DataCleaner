package org.eobjects.datacleaner.panels;

import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.datacleaner.widgets.properties.MultipleInputColumnsPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;

public class RowProcessingAnalyzerJobBuilderPanel extends AbstractJobBuilderPanel {

	private static final long serialVersionUID = 1L;

	private final RowProcessingAnalyzerJobBuilder<?> _analyzerJobBuilder;

	public RowProcessingAnalyzerJobBuilderPanel(AnalysisJobBuilder analysisJobBuilder,
			RowProcessingAnalyzerJobBuilder<?> analyzerJobBuilder) {
		super("images/window/analyzer-tab-background.png", analysisJobBuilder);
		_analyzerJobBuilder = analyzerJobBuilder;

		AnalyzerBeanDescriptor<?> descriptor = _analyzerJobBuilder.getDescriptor();
		init(descriptor, analyzerJobBuilder);
	}

	@Override
	protected PropertyWidget<?> createPropertyWidget(AnalysisJobBuilder analysisJobBuilder,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder, ConfiguredPropertyDescriptor propertyDescriptor) {
		if (_analyzerJobBuilder.isMultipleJobsSupported()) {
			if (propertyDescriptor.isInputColumn()) {
				return new MultipleInputColumnsPropertyWidget(analysisJobBuilder, beanJobBuilder, propertyDescriptor);
			}
		}
		return super.createPropertyWidget(analysisJobBuilder, beanJobBuilder, propertyDescriptor);
	}

	public RowProcessingAnalyzerJobBuilder<?> getAnalyzerJobBuilder() {
		return _analyzerJobBuilder;
	}

	@Override
	protected void setConfiguredProperty(ConfiguredPropertyDescriptor propertyDescriptor, Object value) {
		_analyzerJobBuilder.setConfiguredProperty(propertyDescriptor, value);
	}
}
