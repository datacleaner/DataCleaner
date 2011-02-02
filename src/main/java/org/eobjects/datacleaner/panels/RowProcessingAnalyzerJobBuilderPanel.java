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
package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JComponent;

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
	private final DCPanel _buttonPanel;

	public RowProcessingAnalyzerJobBuilderPanel(AnalysisJobBuilder analysisJobBuilder,
			RowProcessingAnalyzerJobBuilder<?> analyzerJobBuilder) {
		this(analysisJobBuilder, analyzerJobBuilder, true);
	}

	public RowProcessingAnalyzerJobBuilderPanel(AnalysisJobBuilder analysisJobBuilder,
			RowProcessingAnalyzerJobBuilder<?> analyzerJobBuilder, boolean displayRequirementButton) {
		super("images/window/analyzer-tab-background.png", analysisJobBuilder, analyzerJobBuilder);
		_analyzerJobBuilder = analyzerJobBuilder;

		init();

		_requirementButton = new ChangeRequirementButton(analysisJobBuilder, analyzerJobBuilder);

		_buttonPanel = new DCPanel();
		_buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		if (displayRequirementButton) {
			addToButtonPanel(_requirementButton);
		}
		add(_buttonPanel, BorderLayout.NORTH);
	}
	
	public void addToButtonPanel(JComponent component) {
		_buttonPanel.add(component);
	}

	@Override
	protected PropertyWidget<?> createPropertyWidget(AnalysisJobBuilder analysisJobBuilder,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder, ConfiguredPropertyDescriptor propertyDescriptor) {
		if (_analyzerJobBuilder.isMultipleJobsSupported()) {
			if (propertyDescriptor.isInputColumn()) {
				MultipleInputColumnsPropertyWidget propertyWidget = new MultipleInputColumnsPropertyWidget(
						analysisJobBuilder, beanJobBuilder, propertyDescriptor);
				getPropertyWidgetFactory().registerWidget(propertyDescriptor, propertyWidget);
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
