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

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.datacleaner.actions.DisplayOutputWritersForFilterOutcomeActionListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.ChangeRequirementButton;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.jdesktop.swingx.VerticalLayout;

public class FilterJobBuilderPanel extends DCPanel implements FilterJobBuilderPresenter {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();
	private final AnalyzerBeansConfiguration _configuration;
	private final PropertyWidgetFactory _propertyWidgetFactory;
	private final FilterJobBuilder<?, ?> _filterJobBuilder;
	private final ChangeRequirementButton _requirementButton;
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final FilterBeanDescriptor<?, ?> _descriptor;

	public FilterJobBuilderPanel(AnalyzerBeansConfiguration configuration, AnalysisJobBuilder analysisJobBuilder,
			FilterJobBuilder<?, ?> filterJobBuilder) {
		_configuration = configuration;
		_analysisJobBuilder = analysisJobBuilder;
		_filterJobBuilder = filterJobBuilder;
		_propertyWidgetFactory = new PropertyWidgetFactory(analysisJobBuilder, filterJobBuilder);
		_requirementButton = new ChangeRequirementButton(analysisJobBuilder, filterJobBuilder);
		_descriptor = _filterJobBuilder.getDescriptor();

		final JButton removeButton = new JButton("Remove filter", imageManager.getImageIcon("images/actions/remove.png",
				IconUtils.ICON_SIZE_SMALL));
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_analysisJobBuilder.removeFilter(_filterJobBuilder);
			}
		});

		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new VerticalLayout(4));
		buttonPanel.add(_requirementButton);
		buttonPanel.add(removeButton);

		int buttonPanelHeight = _descriptor.getConfiguredProperties().size();
		if (buttonPanelHeight == 0) {
			buttonPanelHeight = 1;
		}

		WidgetUtils.addToGridBag(buttonPanel, this, 2, 0, 1, buttonPanelHeight, GridBagConstraints.NORTHEAST, 4);

		int i = 0;
		for (ConfiguredPropertyDescriptor propertyDescriptor : _descriptor.getConfiguredProperties()) {
			JLabel nameLabel = new JLabel(propertyDescriptor.getName());
			WidgetUtils.addToGridBag(nameLabel, this, 0, i, 1, 1, GridBagConstraints.NORTHEAST, 4);

			String description = propertyDescriptor.getDescription();
			if (description != null) {
				description = description.replaceAll("\n", "</p><p>");
				description = "<html><p>" + description + "</p></html>";
				JLabel descLabel = new JLabel(description);
				descLabel.setBorder(new EmptyBorder(0, 4, 4, 0));
				descLabel.setFont(WidgetUtils.FONT_SMALL);
				WidgetUtils.addToGridBag(descLabel, this, 0, i + 1, 1, 1, GridBagConstraints.NORTHEAST, 0);
			}

			PropertyWidget<?> propertyWidget = _propertyWidgetFactory.create(propertyDescriptor);
			WidgetUtils.addToGridBag(propertyWidget.getWidget(), this, 1, i, 1, 2, GridBagConstraints.NORTHWEST, 4);
			i = i + 2;
		}

		final DCPanel outcomePanel = new DCPanel();
		outcomePanel.setBorder(new TitledBorder("Outcomes"));

		final Set<String> categoryNames = _descriptor.getCategoryNames();
		for (final String categoryName : categoryNames) {
			final JButton outcomeButton = new JButton(categoryName, imageManager.getImageIcon(
					"images/component-types/filter-outcome.png", IconUtils.ICON_SIZE_SMALL));
			outcomeButton.addActionListener(new DisplayOutputWritersForFilterOutcomeActionListener(_configuration,
					_analysisJobBuilder, _filterJobBuilder, categoryName));
			outcomePanel.add(outcomeButton);
		}

		WidgetUtils.addToGridBag(outcomePanel, this, 1, i, 2, 1, GridBagConstraints.NORTHWEST, 4);
	}

	public PropertyWidgetFactory getPropertyWidgetFactory() {
		return _propertyWidgetFactory;
	}

	@Override
	public void onRequirementChanged() {
		_requirementButton.updateText();
	}

	@Override
	public FilterJobBuilder<?, ?> getJobBuilder() {
		return _filterJobBuilder;
	}

	@Override
	public void applyPropertyValues() {
		for (PropertyWidget<?> propertyWidget : getPropertyWidgetFactory().getWidgets()) {
			if (propertyWidget.isSet()) {
				Object value = propertyWidget.getValue();
				ConfiguredPropertyDescriptor propertyDescriptor = propertyWidget.getPropertyDescriptor();
				_filterJobBuilder.setConfiguredProperty(propertyDescriptor, value);
			}
		}
	}

	@Override
	public JComponent getJComponent() {
		return this;
	}
	
	@Override
	public void onConfigurationChanged() {
		getPropertyWidgetFactory().onConfigurationChanged();
	}
}
