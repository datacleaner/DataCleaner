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

import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.builder.WidgetFactory;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetListener;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

public abstract class AbstractJobBuilderPanel extends DCPanel implements PropertyWidgetListener {

	private static final long serialVersionUID = 1L;

	private final List<PropertyWidget<?>> _propertyWidgets = new ArrayList<PropertyWidget<?>>();
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final JXTaskPaneContainer _taskPaneContainer;
	private final AnalysisJobBuilderWindow _parentWindow;

	public AbstractJobBuilderPanel(AnalysisJobBuilderWindow parentWindow, String backgroundImagePath,
			AnalysisJobBuilder analysisJobBuilder) {
		super(ImageManager.getInstance().getImage(backgroundImagePath), 95, 95, WidgetUtils.BG_COLOR_BRIGHT,
				WidgetUtils.BG_COLOR_BRIGHTEST);
		_parentWindow = parentWindow;
		_analysisJobBuilder = analysisJobBuilder;
		_taskPaneContainer = WidgetFactory.createTaskPaneContainer();
		setLayout(new BorderLayout());
		add(WidgetUtils.scrolleable(_taskPaneContainer), BorderLayout.CENTER);
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

		ImageManager imageManager = ImageManager.getInstance();
		buildTaskPane(inputProperties, imageManager.getImageIcon("images/model/column.png", IconUtils.ICON_SIZE_SMALL),
				"Input columns", beanJobBuilder);
		buildTaskPane(requiredProperties, imageManager.getImageIcon("images/menu/options.png", IconUtils.ICON_SIZE_SMALL),
				"Required properties", beanJobBuilder);
		buildTaskPane(optionalProperties, imageManager.getImageIcon("images/actions/edit.png", IconUtils.ICON_SIZE_SMALL),
				"Optional properties", beanJobBuilder);
	}

	protected void buildTaskPane(List<ConfiguredPropertyDescriptor> properties, Icon icon, String title,
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
			addTaskPane(icon, title, panel);
		}
	}

	protected PropertyWidget<?> createPropertyWidget(AnalysisJobBuilder analysisJobBuilder,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder, ConfiguredPropertyDescriptor propertyDescriptor) {
		PropertyWidget<?> propertyWidget = PropertyWidgetFactory.create(analysisJobBuilder, beanJobBuilder,
				propertyDescriptor);
		propertyWidget.addListener(this);
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

	@Override
	public final void onValueChanged(PropertyWidget<?> widget, Object value) {
		if (widget.isSet()) {
			setConfiguredProperty(widget.getPropertyDescriptor(), value);
		}
		_parentWindow.updateStatusLabel();
	}

	protected abstract void setConfiguredProperty(ConfiguredPropertyDescriptor propertyDescriptor, Object value);
}
