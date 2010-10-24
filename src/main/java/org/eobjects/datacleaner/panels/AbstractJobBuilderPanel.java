package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

public abstract class AbstractJobBuilderPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private final List<PropertyWidget<?>> _propertyWidgets = new ArrayList<PropertyWidget<?>>();
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final JXTaskPaneContainer _taskPaneContainer;

	public AbstractJobBuilderPanel(String backgroundImagePath, AnalysisJobBuilder analysisJobBuilder) {
		super(ImageManager.getInstance().getImage(backgroundImagePath), 95, 95);
		_analysisJobBuilder = analysisJobBuilder;
		_taskPaneContainer = new JXTaskPaneContainer();
		_taskPaneContainer.setOpaque(false);
		setLayout(new BorderLayout());
		JScrollPane scroll = new JScrollPane(_taskPaneContainer);
		scroll.setOpaque(false);
		add(_taskPaneContainer);
	}

	protected void init(BeanDescriptor<?> descriptor, AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		Set<ConfiguredPropertyDescriptor> configuredProperties = new TreeSet<ConfiguredPropertyDescriptor>(
				descriptor.getConfiguredProperties());

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

		buildTaskPane(inputProperties, "Input columns", beanJobBuilder);
		buildTaskPane(requiredProperties, "Required properties", beanJobBuilder);
		buildTaskPane(optionalProperties, "Optional properties", beanJobBuilder);
	}

	protected void buildTaskPane(List<ConfiguredPropertyDescriptor> properties, String title,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		if (!properties.isEmpty()) {
			DCPanel panel = new DCPanel();
			int i = 0;
			for (ConfiguredPropertyDescriptor propertyDescriptor : properties) {
				JLabel label = new JLabel(propertyDescriptor.getName());
				label.setOpaque(false);
				WidgetUtils.addToGridBag(label, panel, 0, i, 1, 1, GridBagConstraints.NORTHEAST, 4);

				PropertyWidget<?> propertyWidget = createPropertyWidget(_analysisJobBuilder, beanJobBuilder,
						propertyDescriptor);
				_propertyWidgets.add(propertyWidget);
				WidgetUtils.addToGridBag(propertyWidget.getWidget(), panel, 1, i, 1, 1, GridBagConstraints.NORTHWEST, 4);
				i++;
			}
			addTaskPane(title, panel);
		}
	}

	protected PropertyWidget<?> createPropertyWidget(AnalysisJobBuilder analysisJobBuilder,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder, ConfiguredPropertyDescriptor propertyDescriptor) {
		return PropertyWidgetFactory.create(analysisJobBuilder, beanJobBuilder, propertyDescriptor);
	}

	protected void addTaskPane(String title, JComponent content) {
		JXTaskPane taskPane = new JXTaskPane();
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
		for (PropertyWidget<?> propertyWidget : _propertyWidgets) {
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

	public AnalysisJobBuilder getAnalysisJobBuilder() {
		return _analysisJobBuilder;
	}

	protected abstract void setConfiguredProperty(ConfiguredPropertyDescriptor propertyDescriptor, Object value);
}
