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
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterChangeListener;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.datacleaner.actions.AddFilterActionListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.builder.WidgetFactory;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

public class FilterListPanel extends DCPanel implements FilterChangeListener {

	private static final long serialVersionUID = 1L;

	private final AnalyzerBeansConfiguration _configuration;
	private final Map<FilterJobBuilder<?, ?>, JXTaskPane> _taskPanes;
	private final Map<FilterJobBuilder<?, ?>, FilterJobBuilderPanel> _panels;
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final JXTaskPaneContainer _taskPaneContainer;

	public FilterListPanel(AnalyzerBeansConfiguration configuration, AnalysisJobBuilder analysisJobBuilder) {
		super(ImageManager.getInstance().getImage("images/window/filters-tab-background.png"), 95, 95,
				WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		setLayout(new BorderLayout());
		_configuration = configuration;
		_taskPanes = new IdentityHashMap<FilterJobBuilder<?, ?>, JXTaskPane>();
		_panels = new IdentityHashMap<FilterJobBuilder<?, ?>, FilterJobBuilderPanel>();
		_analysisJobBuilder = analysisJobBuilder;
		_analysisJobBuilder.getFilterChangeListeners().add(this);

		JToolBar toolBar = WidgetFactory.createToolBar();
		toolBar.add(new JSeparator(JSeparator.VERTICAL));

		// Add filter
		ImageManager imageManager = ImageManager.getInstance();
		JButton addFilterButton = new JButton("Add filter", imageManager.getImageIcon("images/component-types/filter.png"));
		addFilterButton.addActionListener(new AddFilterActionListener(configuration, _analysisJobBuilder, this));
		toolBar.add(addFilterButton);

		add(toolBar, BorderLayout.NORTH);

		_taskPaneContainer = WidgetFactory.createTaskPaneContainer();
		_taskPaneContainer.setOpaque(false);
		add(_taskPaneContainer, BorderLayout.CENTER);
	}

	public void applyPropertyValues() {
		Set<FilterJobBuilder<?, ?>> filterJobBuilders = _panels.keySet();
		for (FilterJobBuilder<?, ?> filterJobBuilder : filterJobBuilders) {
			PropertyWidgetFactory propertyWidgetFactory = _panels.get(filterJobBuilder).getPropertyWidgetFactory();
			for (PropertyWidget<?> propertyWidget : propertyWidgetFactory.getWidgets()) {
				if (propertyWidget.isSet()) {
					Object value = propertyWidget.getValue();
					ConfiguredPropertyDescriptor propertyDescriptor = propertyWidget.getPropertyDescriptor();
					filterJobBuilder.setConfiguredProperty(propertyDescriptor, value);
				}
			}
		}
	}

	private JXTaskPane createTaskPane(final FilterJobBuilder<?, ?> fjb) {
		final JXTaskPane taskPane = new JXTaskPane();

		final FilterBeanDescriptor<?, ?> descriptor = fjb.getDescriptor();
		taskPane.setTitle(descriptor.getDisplayName());
		taskPane.setIcon(IconUtils.getDescriptorIcon(fjb.getDescriptor(), IconUtils.ICON_SIZE_SMALL));

		taskPane.add(_panels.get(fjb));
		return taskPane;
	}

	@Override
	public void removeNotify() {
		_analysisJobBuilder.getFilterChangeListeners().remove(this);
		super.removeNotify();
	}

	@Override
	public void onAdd(FilterJobBuilder<?, ?> fjb) {
		final FilterJobBuilderPanel panel = new FilterJobBuilderPanel(_configuration, _analysisJobBuilder, fjb);
		_panels.put(fjb, panel);
		JXTaskPane taskPane = createTaskPane(fjb);
		_taskPanes.put(fjb, taskPane);
		_taskPaneContainer.add(taskPane);
	}

	@Override
	public void onRemove(FilterJobBuilder<?, ?> fjb) {
		_panels.remove(fjb);
		JXTaskPane taskPane = _taskPanes.remove(fjb);
		_taskPaneContainer.remove(taskPane);
		updateUI();
	}

	@Override
	public void onConfigurationChanged(FilterJobBuilder<?, ?> filterJobBuilder) {
		FilterJobBuilderPanel panel = _panels.get(filterJobBuilder);
		if (panel != null) {
			panel.getPropertyWidgetFactory().onConfigurationChanged();
		}
	}

	@Override
	public void onRequirementChanged(FilterJobBuilder<?, ?> filterJobBuilder) {
		FilterJobBuilderPanel panel = _panels.get(filterJobBuilder);
		if (panel != null) {
			panel.onRequirementChanged();
		}
	}
}
