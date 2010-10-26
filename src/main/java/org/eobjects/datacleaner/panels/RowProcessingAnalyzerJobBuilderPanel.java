package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.datacleaner.widgets.properties.ChangeRequirementButton;
import org.eobjects.datacleaner.widgets.properties.MultipleInputColumnsPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;

public class RowProcessingAnalyzerJobBuilderPanel extends AbstractJobBuilderPanel {

	private static final long serialVersionUID = 1L;

	private final RowProcessingAnalyzerJobBuilder<?> _analyzerJobBuilder;

	public RowProcessingAnalyzerJobBuilderPanel(AnalysisJobBuilderWindow parentWindow, AnalysisJobBuilder analysisJobBuilder,
			RowProcessingAnalyzerJobBuilder<?> analyzerJobBuilder) {
		super(parentWindow, "images/window/analyzer-tab-background.png", analysisJobBuilder);
		_analyzerJobBuilder = analyzerJobBuilder;

		AnalyzerBeanDescriptor<?> descriptor = _analyzerJobBuilder.getDescriptor();
		init(descriptor, analyzerJobBuilder);
		
		ChangeRequirementButton requirementButton = new ChangeRequirementButton(analysisJobBuilder, analyzerJobBuilder);
		DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(requirementButton);
		add(buttonPanel, BorderLayout.NORTH);
	}

	@Override
	protected PropertyWidget<?> createPropertyWidget(AnalysisJobBuilder analysisJobBuilder,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder, ConfiguredPropertyDescriptor propertyDescriptor) {
		if (_analyzerJobBuilder.isMultipleJobsSupported()) {
			if (propertyDescriptor.isInputColumn()) {
				MultipleInputColumnsPropertyWidget propertyWidget = new MultipleInputColumnsPropertyWidget(
						analysisJobBuilder, beanJobBuilder, propertyDescriptor);
				propertyWidget.addListener(this);
				return propertyWidget;
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
