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
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

public abstract class AbstractJobBuilderPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private final AnalysisJobBuilder _analysisJobBuilder;
	private final JXTaskPaneContainer _taskPaneContainer;
	private final PropertyWidgetFactory _propertyWidgetFactory;
	private final AbstractBeanJobBuilder<?, ?, ?> _beanJobBuilder;
	private final BeanDescriptor<?> _descriptor;

	public AbstractJobBuilderPanel(String backgroundImagePath, AnalysisJobBuilder analysisJobBuilder,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(ImageManager.getInstance().getImage(backgroundImagePath), 95, 95, WidgetUtils.BG_COLOR_BRIGHT,
				WidgetUtils.BG_COLOR_BRIGHTEST);
		_analysisJobBuilder = analysisJobBuilder;
		_taskPaneContainer = WidgetFactory.createTaskPaneContainer();
		_beanJobBuilder = beanJobBuilder;
		_descriptor = beanJobBuilder.getDescriptor();
		_propertyWidgetFactory = new PropertyWidgetFactory(analysisJobBuilder, beanJobBuilder);
		setLayout(new BorderLayout());
		add(WidgetUtils.scrolleable(_taskPaneContainer), BorderLayout.CENTER);
	}

	protected void init() {
		Set<ConfiguredPropertyDescriptor> configuredProperties = new TreeSet<ConfiguredPropertyDescriptor>(
				_descriptor.getConfiguredProperties());

		List<ConfiguredPropertyDescriptor> inputProperties = new ArrayList<ConfiguredPropertyDescriptor>();
		List<ConfiguredPropertyDescriptor> requiredProperties = new ArrayList<ConfiguredPropertyDescriptor>();
		List<ConfiguredPropertyDescriptor> optionalProperties = new ArrayList<ConfiguredPropertyDescriptor>();
		for (ConfiguredPropertyDescriptor propertyDescriptor : configuredProperties) {
			boolean required = propertyDescriptor.isRequired();
			if (required && propertyDescriptor.isInputColumn()) {
				inputProperties.add(propertyDescriptor);
			} else if (required) {
				requiredProperties.add(propertyDescriptor);
			} else {
				optionalProperties.add(propertyDescriptor);
			}
		}

		ImageManager imageManager = ImageManager.getInstance();
		buildTaskPane(inputProperties, imageManager.getImageIcon("images/model/column.png", IconUtils.ICON_SIZE_SMALL),
				"Input columns", _beanJobBuilder);
		buildTaskPane(requiredProperties, imageManager.getImageIcon("images/menu/options.png", IconUtils.ICON_SIZE_SMALL),
				"Required properties", _beanJobBuilder);
		buildTaskPane(optionalProperties, imageManager.getImageIcon("images/actions/edit.png", IconUtils.ICON_SIZE_SMALL),
				"Optional properties", _beanJobBuilder);
	}

	protected void buildTaskPane(List<ConfiguredPropertyDescriptor> properties, Icon icon, String title,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		if (!properties.isEmpty()) {
			DCPanel panel = new DCPanel();
			int i = 0;
			for (ConfiguredPropertyDescriptor propertyDescriptor : properties) {
				JLabel nameLabel = new JLabel(propertyDescriptor.getName());
				WidgetUtils.addToGridBag(nameLabel, panel, 0, i, 1, 1, GridBagConstraints.NORTHEAST, 4);

				String description = propertyDescriptor.getDescription();
				if (description != null) {
					description = description.replaceAll("\n", "</p><p>");
					description = "<html><p>" + description + "</p></html>";
					JLabel descLabel = new JLabel(description);
					descLabel.setFont(WidgetUtils.FONT_SMALL);
					descLabel.setBorder(new EmptyBorder(0, 4, 4, 0));
					WidgetUtils.addToGridBag(descLabel, panel, 0, i + 1, 1, 1, GridBagConstraints.NORTHEAST, 0);
				}

				PropertyWidget<?> propertyWidget = createPropertyWidget(_analysisJobBuilder, beanJobBuilder,
						propertyDescriptor);
				WidgetUtils.addToGridBag(propertyWidget.getWidget(), panel, 1, i, 1, 2, GridBagConstraints.NORTHWEST, 4);
				i = i + 2;
			}
			addTaskPane(icon, title, panel);
		}
	}

	protected PropertyWidget<?> createPropertyWidget(AnalysisJobBuilder analysisJobBuilder,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder, ConfiguredPropertyDescriptor propertyDescriptor) {
		PropertyWidget<?> propertyWidget = _propertyWidgetFactory.create(propertyDescriptor);
		return propertyWidget;
	}

	protected void addTaskPane(Icon icon, String title, JComponent content) {
		JXTaskPane taskPane = new JXTaskPane();
		if (icon != null) {
			taskPane.setIcon(icon);
		}
		taskPane.setTitle(title);
		taskPane.add(content);
		_taskPaneContainer.add(taskPane);
	}

	public void applyPropertyValues() {
		applyPropertyValues(true);
	}

	/**
	 * @param errorAware
	 *            defines whether or not the method should throw an exception in
	 *            case some of the applied properties are missing or errornous
	 */
	public void applyPropertyValues(boolean errorAware) {
		for (PropertyWidget<?> propertyWidget : _propertyWidgetFactory.getWidgets()) {
			ConfiguredPropertyDescriptor propertyDescriptor = propertyWidget.getPropertyDescriptor();
			if (propertyWidget.isSet()) {
				Object value = propertyWidget.getValue();
				setConfiguredProperty(propertyDescriptor, value);
			} else {
				if (errorAware && propertyDescriptor.isRequired()) {
					throw new IllegalStateException("Required property not set: " + propertyDescriptor.getName());
				}
			}
		}
	}

	public PropertyWidgetFactory getPropertyWidgetFactory() {
		return _propertyWidgetFactory;
	}

	public AnalysisJobBuilder getAnalysisJobBuilder() {
		return _analysisJobBuilder;
	}

	protected abstract void setConfiguredProperty(ConfiguredPropertyDescriptor propertyDescriptor, Object value);
}
