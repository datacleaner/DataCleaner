/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AbstractBeanWithInputColumnsBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.UnconfiguredConfiguredPropertyException;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.ChangeRequirementButton;
import org.eobjects.datacleaner.widgets.DCTaskPaneContainer;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetPanel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;

public abstract class AbstractJobBuilderPanel extends DCPanel implements ComponentJobBuilderPresenter {

    private static final long serialVersionUID = 1L;

    private final ImageManager imageManager = ImageManager.get();
    private final DCTaskPaneContainer _taskPaneContainer;
    private final PropertyWidgetFactory _propertyWidgetFactory;
    private final AbstractBeanJobBuilder<?, ?, ?> _beanJobBuilder;
    private final BeanDescriptor<?> _descriptor;
    private final ChangeRequirementButton _requirementButton;
    private final JComponent _buttonPanel;

    protected AbstractJobBuilderPanel(String watermarkImagePath, AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            PropertyWidgetFactory propertyWidgetFactory) {
        this(ImageManager.get().getImage(watermarkImagePath), 95, 95, beanJobBuilder, propertyWidgetFactory);
    }

    protected AbstractJobBuilderPanel(Image watermarkImage, int watermarkHorizontalPosition,
            int watermarkVerticalPosition, AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            PropertyWidgetFactory propertyWidgetFactory) {
        this(watermarkImage, watermarkHorizontalPosition, watermarkVerticalPosition, beanJobBuilder,
                propertyWidgetFactory, true);
    }

    protected AbstractJobBuilderPanel(Image watermarkImage, int watermarkHorizontalPosition,
            int watermarkVerticalPosition, AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            PropertyWidgetFactory propertyWidgetFactory, boolean displayRequirementButton) {
        super(watermarkImage, watermarkHorizontalPosition, watermarkVerticalPosition, WidgetUtils.BG_COLOR_BRIGHT,
                WidgetUtils.BG_COLOR_BRIGHTEST);
        _taskPaneContainer = WidgetFactory.createTaskPaneContainer();
        _taskPaneContainer.setLayout(new VerticalLayout(4));
        _beanJobBuilder = beanJobBuilder;
        _descriptor = beanJobBuilder.getDescriptor();
        _propertyWidgetFactory = propertyWidgetFactory;

        setLayout(new BorderLayout());

        final JScrollPane scrolleable = WidgetUtils.scrolleable(_taskPaneContainer);
        add(scrolleable, BorderLayout.CENTER);

        if (displayRequirementButton && beanJobBuilder instanceof AbstractBeanWithInputColumnsBuilder) {
            _requirementButton = new ChangeRequirementButton(
                    (AbstractBeanWithInputColumnsBuilder<?, ?, ?>) beanJobBuilder);
        } else {
            _requirementButton = null;
        }

        _buttonPanel = createTopButtonPanel();
        add(_buttonPanel, BorderLayout.NORTH);
    }

    public void addToButtonPanel(JComponent component) {
        _buttonPanel.add(component);
    }

    protected JComponent createTopButtonPanel() {
        final DCPanel buttonPanel = new DCPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        if (_requirementButton != null) {
            buttonPanel.add(_requirementButton);
        }
        return buttonPanel;
    }

    public ChangeRequirementButton getRequirementButton() {
        return _requirementButton;
    }

    protected JXTaskPaneContainer getTaskPaneContainer() {
        return _taskPaneContainer;
    }

    @Override
    public final JComponent createJComponent() {
        init();
        JComponent decorate = decorate(this);
        return decorate;
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
        final List<ConfiguredPropertyTaskPane> propertyTaskPanes = createPropertyTaskPanes();
        for (ConfiguredPropertyTaskPane propertyTaskPane : propertyTaskPanes) {
            buildTaskPane(propertyTaskPane.getProperties(), imageManager.getImageIcon(
                    propertyTaskPane.getIconImagePath(), IconUtils.ICON_SIZE_SMALL, getClass().getClassLoader()),
                    propertyTaskPane.getTitle(), getJobBuilder(), propertyTaskPane.isExpanded());
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
        result.add(new ConfiguredPropertyTaskPane("Input columns", IconUtils.MODEL_COLUMN, inputProperties));
        result.add(new ConfiguredPropertyTaskPane("Required properties", IconUtils.MENU_OPTIONS, requiredProperties));
        result.add(new ConfiguredPropertyTaskPane("Optional properties (" + optionalProperties.size() + ")",
                "images/actions/edit.png", optionalProperties, false));

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
                    PropertyWidget<?> propertyWidget = createPropertyWidget(getJobBuilder(), propertyDescriptor);
                    getPropertyWidgetFactory().registerWidget(propertyDescriptor, propertyWidget);
                    return propertyWidget;
                }
            };
            panel.addProperties(properties);

            if (!panel.isEmpty()) {
                addTaskPane(icon, title, panel, expanded);
            }
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
        taskPane.setCollapsed(!expanded);
        taskPane.add(content);
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
                    throw new UnconfiguredConfiguredPropertyException(getJobBuilder(), propertyDescriptor);
                }
            }
        }
    }

    public final PropertyWidgetFactory getPropertyWidgetFactory() {
        return _propertyWidgetFactory;
    }

    public AnalysisJobBuilder getAnalysisJobBuilder() {
        return getJobBuilder().getAnalysisJobBuilder();
    }

    protected void setConfiguredProperty(ConfiguredPropertyDescriptor propertyDescriptor, Object value) {
        getJobBuilder().setConfiguredProperty(propertyDescriptor, value);
    }

    /**
     * Convenience method made available to subclasses to inform that the
     * configuration of this component has changed
     */
    protected void onConfigurationChanged() {
        getPropertyWidgetFactory().onConfigurationChanged();
    }

    /**
     * Convenience method made available to subclasses to inform that the
     * requirement on this component has changed
     */
    protected void onRequirementChanged() {
        if (_requirementButton != null) {
            _requirementButton.updateText();
        }
    }

    /**
     * @deprecated not used anymore
     * @return
     */
    @Deprecated
    protected final boolean showContextVisualization() {
        return false;
    }
}
