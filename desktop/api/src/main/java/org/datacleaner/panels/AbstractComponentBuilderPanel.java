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
package org.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.datacleaner.api.HiddenProperty;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.UnconfiguredConfiguredPropertyException;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.ChangeRequirementButton;
import org.datacleaner.widgets.DCTaskPaneContainer;
import org.datacleaner.widgets.properties.PropertyWidget;
import org.datacleaner.widgets.properties.PropertyWidgetCollection;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.datacleaner.widgets.properties.PropertyWidgetPanel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractComponentBuilderPanel extends DCPanel implements ComponentBuilderPresenter {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(AbstractComponentBuilderPanel.class);

    private final ImageManager imageManager = ImageManager.get();
    private final DCTaskPaneContainer _taskPaneContainer;
    private final PropertyWidgetFactory _propertyWidgetFactory;
    private final PropertyWidgetCollection _propertyWidgetCollection;
    private final ComponentBuilder _componentBuilder;
    private final ComponentDescriptor<?> _descriptor;
    private final JComponent _buttonPanel;
    private final OutputDataStreamsViewer _outputDataStreamsViewer;
    private JXTaskPane _outputDataStreamsTaskPane;

    protected AbstractComponentBuilderPanel(final String watermarkImagePath, final ComponentBuilder componentBuilder,
            final PropertyWidgetFactory propertyWidgetFactory) {
        this(ImageManager.get().getImage(watermarkImagePath), 95, 95, componentBuilder, propertyWidgetFactory);
    }

    protected AbstractComponentBuilderPanel(final Image watermarkImage, final int watermarkHorizontalPosition,
            final int watermarkVerticalPosition, final ComponentBuilder componentBuilder,
            final PropertyWidgetFactory propertyWidgetFactory) {
        super(watermarkImage, watermarkHorizontalPosition, watermarkVerticalPosition,
                WidgetUtils.COLOR_WELL_BACKGROUND);
        _taskPaneContainer = WidgetFactory.createTaskPaneContainer();
        _taskPaneContainer.setLayout(new VerticalLayout(4));
        _componentBuilder = componentBuilder;
        _descriptor = componentBuilder.getDescriptor();
        _propertyWidgetFactory = propertyWidgetFactory;
        _propertyWidgetCollection = propertyWidgetFactory.getPropertyWidgetCollection();
        _outputDataStreamsViewer = new OutputDataStreamsViewer(_componentBuilder);

        setLayout(new BorderLayout());

        final JScrollPane scrolleable = WidgetUtils.scrolleable(_taskPaneContainer);
        add(scrolleable, BorderLayout.CENTER);

        _buttonPanel = createTopButtonPanel();
        add(_buttonPanel, BorderLayout.NORTH);
    }

    public void addToButtonPanel(final JComponent component) {
        _buttonPanel.add(component);
    }

    protected JComponent createTopButtonPanel() {
        final DCPanel buttonPanel = new DCPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        return buttonPanel;
    }

    protected JXTaskPaneContainer getTaskPaneContainer() {
        return _taskPaneContainer;
    }

    @Override
    public final JComponent createJComponent() {
        init();
        return decorateMainPanel(this);
    }

    @Override
    public ComponentBuilder getComponentBuilder() {
        return _componentBuilder;
    }

    /**
     * Can be implemented by subclasses to intercept the created JComponent
     * before returning.
     *
     * @param panel
     * @return
     */
    protected JComponent decorateMainPanel(final DCPanel panel) {
        return panel;
    }

    private void init() {
        final ComponentBuilder componentBuilder = getComponentBuilder();
        final List<ConfiguredPropertyTaskPane> propertyTaskPanes = createPropertyTaskPanes();

        final Set<ConfiguredPropertyDescriptor> unconfiguredPropertyDescriptors = new HashSet<>();
        unconfiguredPropertyDescriptors.addAll(componentBuilder.getDescriptor().getConfiguredProperties());

        for (final ConfiguredPropertyTaskPane propertyTaskPane : propertyTaskPanes) {
            buildTaskPane(propertyTaskPane.getProperties(), imageManager
                            .getImageIcon(propertyTaskPane.getIconImagePath(), IconUtils.ICON_SIZE_TASK_PANE,
                                    getClass().getClassLoader()), propertyTaskPane.getTitle(), componentBuilder,
                    propertyTaskPane.isExpanded());

            unconfiguredPropertyDescriptors.removeAll(propertyTaskPane.getProperties());
        }

        if (!unconfiguredPropertyDescriptors.isEmpty()) {
            for (final ConfiguredPropertyDescriptor property : unconfiguredPropertyDescriptors) {
                logger.warn("No property widget was found in task panes for property: {}", property);

                // add it to the property widget collection just to be sure
                final PropertyWidget<?> propertyWidget = createPropertyWidget(componentBuilder, property);
                getPropertyWidgetCollection().registerWidget(property, propertyWidget);
            }
        }

        onOutputDataStreamsChanged();
    }

    protected List<ConfiguredPropertyTaskPane> createPropertyTaskPanes() {
        final Set<ConfiguredPropertyDescriptor> configuredProperties =
                new TreeSet<>(_descriptor.getConfiguredProperties());

        final List<ConfiguredPropertyDescriptor> inputProperties = new ArrayList<>();
        final List<ConfiguredPropertyDescriptor> requiredProperties = new ArrayList<>();
        final List<ConfiguredPropertyDescriptor> optionalProperties = new ArrayList<>();

        for (final ConfiguredPropertyDescriptor propertyDescriptor : configuredProperties) {
            final HiddenProperty hiddenProperty = propertyDescriptor.getAnnotation(HiddenProperty.class);
            if (hiddenProperty == null || !hiddenProperty.hiddenForLocalAccess()) {
                final boolean required = propertyDescriptor.isRequired();
                if (required && propertyDescriptor.isInputColumn()) {
                    inputProperties.add(propertyDescriptor);
                } else if (required) {
                    requiredProperties.add(propertyDescriptor);
                } else {
                    optionalProperties.add(propertyDescriptor);
                }
            }
        }

        final List<ConfiguredPropertyTaskPane> result = new ArrayList<>();
        result.add(new ConfiguredPropertyTaskPane("Input columns", IconUtils.MODEL_COLUMN, inputProperties));
        result.add(new ConfiguredPropertyTaskPane("Required properties", IconUtils.MENU_OPTIONS, requiredProperties));
        result.add(new ConfiguredPropertyTaskPane("Optional properties (" + optionalProperties.size() + ")",
                IconUtils.ACTION_EDIT, optionalProperties, false));

        return result;
    }

    protected void buildTaskPane(final List<ConfiguredPropertyDescriptor> properties, final Icon icon,
            final String title, final ComponentBuilder componentBuilder) {
        buildTaskPane(properties, icon, title, componentBuilder, true);
    }

    protected void buildTaskPane(final List<ConfiguredPropertyDescriptor> properties, final Icon icon,
            final String title, final ComponentBuilder componentBuilder, final boolean expanded) {
        if (!properties.isEmpty()) {
            final PropertyWidgetPanel panel = new PropertyWidgetPanel() {

                private static final long serialVersionUID = 1L;

                @Override
                protected PropertyWidget<?> getPropertyWidget(final ConfiguredPropertyDescriptor propertyDescriptor) {
                    final PropertyWidget<?> propertyWidget =
                            createPropertyWidget(getComponentBuilder(), propertyDescriptor);
                    getPropertyWidgetCollection().registerWidget(propertyDescriptor, propertyWidget);
                    return propertyWidget;
                }
            };
            panel.addProperties(properties);

            if (!panel.isEmpty()) {
                addTaskPane(icon, title, panel, expanded);
            }
        }
    }

    protected PropertyWidget<?> createPropertyWidget(final ComponentBuilder componentBuilder,
            final ConfiguredPropertyDescriptor propertyDescriptor) {
        return getPropertyWidgetFactory().create(propertyDescriptor);
    }

    protected JXTaskPane addTaskPane(final String iconImagePath, final String title, final JComponent content) {
        return addTaskPane(iconImagePath, title, content, true);
    }

    protected JXTaskPane addTaskPane(final String iconImagePath, final String title, final JComponent content,
            final boolean expanded) {
        final ImageIcon icon = imageManager.getImageIcon(iconImagePath, IconUtils.ICON_SIZE_TASK_PANE);
        return addTaskPane(icon, title, content, expanded);
    }

    protected JXTaskPane addTaskPane(final Icon icon, final String title, final JComponent content) {
        return addTaskPane(icon, title, content, true);
    }

    protected JXTaskPane addTaskPane(final Icon icon, final String title, final JComponent content,
            final boolean expanded) {
        final JXTaskPane taskPane = WidgetFactory.createTaskPane(title, icon);
        taskPane.setCollapsed(!expanded);
        taskPane.add(content);
        _taskPaneContainer.add(taskPane);
        return taskPane;
    }

    public final void applyPropertyValues() {
        applyPropertyValues(true);
    }

    /**
     * @param errorAware
     *            defines whether or not the method should throw an exception in
     *            case some of the applied properties are missing or erroneous
     */
    public final void applyPropertyValues(final boolean errorAware) {
        for (final PropertyWidget<?> propertyWidget : getPropertyWidgetCollection().getWidgets()) {
            final ConfiguredPropertyDescriptor propertyDescriptor = propertyWidget.getPropertyDescriptor();
            if (propertyWidget.isSet()) {
                final Object value = propertyWidget.getValue();
                setConfiguredProperty(propertyDescriptor, value);
            } else {
                if (errorAware && propertyDescriptor.isRequired()) {
                    throw new UnconfiguredConfiguredPropertyException(getComponentBuilder(), propertyDescriptor);
                }
            }
        }
    }

    public final PropertyWidgetFactory getPropertyWidgetFactory() {
        return _propertyWidgetFactory;
    }

    public PropertyWidgetCollection getPropertyWidgetCollection() {
        return _propertyWidgetCollection;
    }

    public AnalysisJobBuilder getAnalysisJobBuilder() {
        return getComponentBuilder().getAnalysisJobBuilder();
    }

    protected void setConfiguredProperty(final ConfiguredPropertyDescriptor propertyDescriptor, final Object value) {
        getComponentBuilder().setConfiguredProperty(propertyDescriptor, value);
    }

    /**
     * Convenience method made available to subclasses to inform that the
     * configuration of this component has changed
     */
    protected void onConfigurationChanged() {
        getPropertyWidgetCollection().onConfigurationChanged();

        onOutputDataStreamsChanged();
    }

    private void onOutputDataStreamsChanged() {
        _taskPaneContainer.remove(_outputDataStreamsTaskPane);
        _outputDataStreamsViewer.refresh();
        if (_outputDataStreamsViewer.isEnabled()) {
            _outputDataStreamsTaskPane =
                    addTaskPane(IconUtils.OUTPUT_DATA_STREAM_PATH, "Output data streams", _outputDataStreamsViewer);
            _taskPaneContainer.updateUI();
        }
    }

    /**
     * Convenience method made available to subclasses to inform that the
     * requirement on this component has changed
     *
     * @deprecated no longer has any effect since
     *             {@link ChangeRequirementButton} has been removed from this
     *             panel
     */
    @Deprecated
    protected void onRequirementChanged() {
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
