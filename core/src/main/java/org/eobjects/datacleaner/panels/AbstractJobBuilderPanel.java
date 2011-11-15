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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.UnconfiguredConfiguredPropertyException;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCTaskPaneContainer;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetPanel;
import org.jdesktop.swingx.JXTaskPane;

public abstract class AbstractJobBuilderPanel extends DCPanel implements ComponentJobBuilderPresenter {

	private static final long serialVersionUID = 1L;

	private final DCTaskPaneContainer _taskPaneContainer;
	private final PropertyWidgetFactory _propertyWidgetFactory;
	private final AbstractBeanJobBuilder<?, ?, ?> _beanJobBuilder;
	private final BeanDescriptor<?> _descriptor;

	protected AbstractJobBuilderPanel(String backgroundImagePath, AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			PropertyWidgetFactory propertyWidgetFactory) {
		super(ImageManager.getInstance().getImage(backgroundImagePath), 95, 95, WidgetUtils.BG_COLOR_BRIGHT,
				WidgetUtils.BG_COLOR_BRIGHTEST);
		_taskPaneContainer = WidgetFactory.createTaskPaneContainer();
		_beanJobBuilder = beanJobBuilder;
		_descriptor = beanJobBuilder.getDescriptor();
		_propertyWidgetFactory = propertyWidgetFactory;

		setLayout(new BorderLayout());
		add(WidgetUtils.scrolleable(_taskPaneContainer), BorderLayout.CENTER);
	}

	@Override
	public final JComponent createJComponent() {
		init();
		return decorate(this);
	}

	@Override
	public AbstractBeanJobBuilder<?, ?, ?> getJobBuilder() {
		return _beanJobBuilder;
	}

	/**
	 * Can be implemented by subclasses to intercept the created JComponent
	 * before returning.
	 * 
	 * @param panel
	 * @return
	 */
	protected JComponent decorate(DCPanel panel) {
		return panel;
	}

	private final void init() {
		final ImageManager imageManager = ImageManager.getInstance();
		final List<ConfiguredPropertyTaskPane> propertyTaskPanes = createPropertyTaskPanes();
		for (ConfiguredPropertyTaskPane propertyTaskPane : propertyTaskPanes) {
			buildTaskPane(propertyTaskPane.getProperties(),
					imageManager.getImageIcon(propertyTaskPane.getIconImagePath(), IconUtils.ICON_SIZE_SMALL),
					propertyTaskPane.getTitle(), _beanJobBuilder, propertyTaskPane.isExpanded());
		}
	}

	protected List<ConfiguredPropertyTaskPane> createPropertyTaskPanes() {
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

		final List<ConfiguredPropertyTaskPane> result = new ArrayList<ConfiguredPropertyTaskPane>();
		result.add(new ConfiguredPropertyTaskPane("Input columns", "images/model/column.png", inputProperties));
		result.add(new ConfiguredPropertyTaskPane("Required properties", IconUtils.MENU_OPTIONS, requiredProperties));
		result.add(new ConfiguredPropertyTaskPane("Optional properties", "images/actions/edit.png", optionalProperties));

		return result;
	}
	
	protected void buildTaskPane(List<ConfiguredPropertyDescriptor> properties, Icon icon, String title,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		buildTaskPane(properties, icon, title, beanJobBuilder, true);
	}

	protected void buildTaskPane(List<ConfiguredPropertyDescriptor> properties, Icon icon, String title,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder, boolean expanded) {
		if (!properties.isEmpty()) {
			final PropertyWidgetPanel panel = new PropertyWidgetPanel() {

				private static final long serialVersionUID = 1L;

				@Override
				protected PropertyWidget<?> getPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor) {
					PropertyWidget<?> propertyWidget = createPropertyWidget(_beanJobBuilder, propertyDescriptor);
					getPropertyWidgetFactory().registerWidget(propertyDescriptor, propertyWidget);
					return propertyWidget;
				}
			};
			panel.addProperties(properties);
			addTaskPane(icon, title, panel, expanded);
		}
	}

	protected PropertyWidget<?> createPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		return getPropertyWidgetFactory().create(propertyDescriptor);
	}
	
	protected void addTaskPane(Icon icon, String title, JComponent content) {
		addTaskPane(icon, title, content, true);
	}

	protected void addTaskPane(Icon icon, String title, JComponent content, boolean expanded) {
		JXTaskPane taskPane = WidgetFactory.createTaskPane(title, icon);
		taskPane.add(content);
		if (!expanded) {
			taskPane.setCollapsed(true);
		}
		_taskPaneContainer.add(taskPane);
	}

	public final void applyPropertyValues() {
		applyPropertyValues(true);
	}

	/**
	 * @param errorAware
	 *            defines whether or not the method should throw an exception in
	 *            case some of the applied properties are missing or errornous
	 */
	public final void applyPropertyValues(boolean errorAware) {
		for (PropertyWidget<?> propertyWidget : getPropertyWidgetFactory().getWidgets()) {
			ConfiguredPropertyDescriptor propertyDescriptor = propertyWidget.getPropertyDescriptor();
			if (propertyWidget.isSet()) {
				Object value = propertyWidget.getValue();
				setConfiguredProperty(propertyDescriptor, value);
			} else {
				if (errorAware && propertyDescriptor.isRequired()) {
					throw new UnconfiguredConfiguredPropertyException(_beanJobBuilder, propertyDescriptor);
				}
			}
		}
	}

	public final PropertyWidgetFactory getPropertyWidgetFactory() {
		return _propertyWidgetFactory;
	}

	public AnalysisJobBuilder getAnalysisJobBuilder() {
		return _beanJobBuilder.getAnalysisJobBuilder();
	}

	protected abstract void setConfiguredProperty(ConfiguredPropertyDescriptor propertyDescriptor, Object value);
}
