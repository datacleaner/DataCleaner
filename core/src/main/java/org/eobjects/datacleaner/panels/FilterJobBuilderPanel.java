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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.datacleaner.actions.DisplayOutputWritersForFilterOutcomeActionListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.widgets.Alignment;
import org.eobjects.datacleaner.widgets.ChangeRequirementButton;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetPanel;

public class FilterJobBuilderPanel extends DCPanel implements FilterJobBuilderPresenter {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();
	private final PropertyWidgetFactory _propertyWidgetFactory;
	private final FilterJobBuilder<?, ?> _filterJobBuilder;
	private final ChangeRequirementButton _requirementButton;
	private final FilterBeanDescriptor<?, ?> _descriptor;

	public FilterJobBuilderPanel(FilterJobBuilder<?, ?> filterJobBuilder, PropertyWidgetFactory propertyWidgetFactory) {
		super();

		_filterJobBuilder = filterJobBuilder;
		_propertyWidgetFactory = propertyWidgetFactory;

		_requirementButton = new ChangeRequirementButton(filterJobBuilder);
		_descriptor = _filterJobBuilder.getDescriptor();

		final JButton removeButton = new JButton("Remove filter", imageManager.getImageIcon("images/actions/remove.png",
				IconUtils.ICON_SIZE_SMALL));
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_filterJobBuilder.getAnalysisJobBuilder().removeFilter(_filterJobBuilder);
			}
		});

		final PropertyWidgetPanel panel = new PropertyWidgetPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected PropertyWidget<?> getPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor) {
				PropertyWidget<?> propertyWidget = createPropertyWidget(_filterJobBuilder, propertyDescriptor);
				getPropertyWidgetFactory().registerWidget(propertyDescriptor, propertyWidget);
				return propertyWidget;
			}
		};
		panel.addProperties(_descriptor.getConfiguredProperties());

		final DCPanel outcomePanel = new DCPanel();
		outcomePanel.setBorder(new TitledBorder("Outcomes"));

		final Set<String> categoryNames = _descriptor.getOutcomeCategoryNames();
		for (final String categoryName : categoryNames) {
			final JButton outcomeButton = new JButton(categoryName, imageManager.getImageIcon(
					"images/component-types/filter-outcome.png", IconUtils.ICON_SIZE_SMALL));
			AnalysisJobBuilder analysisJobBuilder = _filterJobBuilder.getAnalysisJobBuilder();
			outcomeButton.addActionListener(new DisplayOutputWritersForFilterOutcomeActionListener(_filterJobBuilder
					.getAnalysisJobBuilder().getConfiguration(), analysisJobBuilder, _filterJobBuilder, categoryName));
			outcomePanel.add(outcomeButton);
		}

		final DCPanel buttonPanel = DCPanel.flow(Alignment.RIGHT, removeButton, _requirementButton);

		setLayout(new BorderLayout());
		add(buttonPanel, BorderLayout.NORTH);
		add(panel, BorderLayout.CENTER);
		add(DCPanel.flow(Alignment.CENTER, outcomePanel), BorderLayout.SOUTH);
	}

	protected PropertyWidget<?> createPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		return getPropertyWidgetFactory().create(propertyDescriptor);
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
	public JComponent createJComponent() {
		return this;
	}

	@Override
	public void onConfigurationChanged() {
		getPropertyWidgetFactory().onConfigurationChanged();
	}
}
