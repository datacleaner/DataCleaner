package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.datacleaner.widgets.properties.ChangeRequirementButton;
import org.eobjects.datacleaner.widgets.properties.MultipleInputColumnsPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;

public class RowProcessingAnalyzerJobBuilderPanel extends AbstractJobBuilderPanel {

	private static final long serialVersionUID = 1L;

	private final RowProcessingAnalyzerJobBuilder<?> _analyzerJobBuilder;
	private final ChangeRequirementButton _requirementButton;

	public RowProcessingAnalyzerJobBuilderPanel(AnalysisJobBuilder analysisJobBuilder,
			RowProcessingAnalyzerJobBuilder<?> analyzerJobBuilder) {
		super("images/window/analyzer-tab-background.png", analysisJobBuilder, analyzerJobBuilder);
		_analyzerJobBuilder = analyzerJobBuilder;

		init();

		_requirementButton = new ChangeRequirementButton(analysisJobBuilder, analyzerJobBuilder);

		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(_requirementButton);
		add(buttonPanel, BorderLayout.NORTH);
	}

	@Override
	protected PropertyWidget<?> createPropertyWidget(AnalysisJobBuilder analysisJobBuilder,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder, ConfiguredPropertyDescriptor propertyDescriptor) {
		if (_analyzerJobBuilder.isMultipleJobsSupported()) {
			if (propertyDescriptor.isInputColumn()) {
				MultipleInputColumnsPropertyWidget propertyWidget = new MultipleInputColumnsPropertyWidget(
						analysisJobBuilder, beanJobBuilder, propertyDescriptor);
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

	public void onRequirementChanged() {
		_requirementButton.updateText();
	}
}
