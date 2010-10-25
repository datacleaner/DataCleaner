package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterChangeListener;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.datacleaner.actions.AddFilterActionListener;
import org.eobjects.datacleaner.actions.AddFilterMappingActionListener;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.builder.WidgetFactory;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

public class FilterListPanel extends DCPanel implements FilterChangeListener {

	private static final long serialVersionUID = 1L;

	private final Map<FilterJobBuilder<?, ?>, JXTaskPane> _filterJobBuilders;
	private final Map<FilterJobBuilder<?, ?>, List<PropertyWidget<?>>> _propertyWidgets;
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final JXTaskPaneContainer _taskPaneContainer;

	public FilterListPanel(AnalyzerBeansConfiguration configuration, AnalysisJobBuilder analysisJobBuilder) {
		super(ImageManager.getInstance().getImage("images/window/filters-tab-background.png"), 95, 95,
				WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		setLayout(new BorderLayout());
		_filterJobBuilders = new IdentityHashMap<FilterJobBuilder<?, ?>, JXTaskPane>();
		_propertyWidgets = new IdentityHashMap<FilterJobBuilder<?, ?>, List<PropertyWidget<?>>>();
		_analysisJobBuilder = analysisJobBuilder;
		_analysisJobBuilder.getFilterChangeListeners().add(this);

		JToolBar toolBar = WidgetFactory.createToolBar();
		toolBar.add(new JSeparator(JSeparator.VERTICAL));

		// Add filter
		ImageManager imageManager = ImageManager.getInstance();
		JButton addFilterButton = new JButton("Add filter", imageManager.getImageIcon("images/component-types/filter.png"));
		addFilterButton.addActionListener(new AddFilterActionListener(configuration, _analysisJobBuilder));
		toolBar.add(addFilterButton);

		add(toolBar, BorderLayout.NORTH);

		_taskPaneContainer = new JXTaskPaneContainer();
		_taskPaneContainer.setOpaque(false);
		add(_taskPaneContainer, BorderLayout.CENTER);
	}

	public void applyPropertyValues() {
		Set<FilterJobBuilder<?, ?>> filterJobBuilders = _propertyWidgets.keySet();
		for (FilterJobBuilder<?, ?> filterJobBuilder : filterJobBuilders) {
			List<PropertyWidget<?>> propertyWidgets = _propertyWidgets.get(filterJobBuilder);
			for (PropertyWidget<?> propertyWidget : propertyWidgets) {
				if (propertyWidget.isSet()) {
					Object value = propertyWidget.getValue();
					ConfiguredPropertyDescriptor propertyDescriptor = propertyWidget.getPropertyDescriptor();
					filterJobBuilder.setConfiguredProperty(propertyDescriptor, value);
				}
			}
		}
	}

	private JXTaskPane createTaskPane(final FilterJobBuilder<?, ?> fjb) {
		JXTaskPane taskPane = new JXTaskPane();
		List<PropertyWidget<?>> propertyWidgets = _propertyWidgets.get(fjb);
		FilterBeanDescriptor<?, ?> descriptor = fjb.getDescriptor();
		taskPane.setTitle(descriptor.getDisplayName());

		DCPanel panel = new DCPanel();

		JButton removeButton = new JButton("Remove filter");
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_analysisJobBuilder.removeFilter(fjb);
			}
		});

		WidgetUtils.addToGridBag(removeButton, panel, 2, 0, 1, 1, GridBagConstraints.NORTHEAST, 4);

		int i = 0;
		for (ConfiguredPropertyDescriptor propertyDescriptor : descriptor.getConfiguredProperties()) {
			JLabel label = new JLabel(propertyDescriptor.getName());
			label.setOpaque(false);
			WidgetUtils.addToGridBag(label, panel, 0, i, 1, 1, GridBagConstraints.NORTHEAST, 4);

			PropertyWidget<?> propertyWidget = PropertyWidgetFactory.create(_analysisJobBuilder, fjb, propertyDescriptor);
			propertyWidgets.add(propertyWidget);
			WidgetUtils.addToGridBag(propertyWidget.getWidget(), panel, 1, i, 1, 1, GridBagConstraints.NORTHWEST, 4);
			i++;
		}

		DCPanel outcomePanel = new DCPanel();
		outcomePanel.setBorder(new TitledBorder("Outcome mapping"));

		Set<String> categoryNames = descriptor.getCategoryNames();
		for (String categoryName : categoryNames) {
			final Enum<?> category = descriptor.getCategoryByName(categoryName);
			JButton categoryButton = new JButton(categoryName);
			categoryButton.addActionListener(new AddFilterMappingActionListener(categoryButton, fjb, category,
					_analysisJobBuilder));

			outcomePanel.add(categoryButton);
		}

		WidgetUtils.addToGridBag(outcomePanel, panel, 1, i, 2, 1, GridBagConstraints.NORTHWEST, 4);

		taskPane.add(panel);
		return taskPane;
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		_analysisJobBuilder.getFilterChangeListeners().remove(this);
	}

	@Override
	public void onAdd(FilterJobBuilder<?, ?> fjb) {
		_propertyWidgets.put(fjb, new ArrayList<PropertyWidget<?>>());
		JXTaskPane taskPane = createTaskPane(fjb);
		_filterJobBuilders.put(fjb, taskPane);
		_taskPaneContainer.add(taskPane);
	}

	@Override
	public void onRemove(FilterJobBuilder<?, ?> fjb) {
		_propertyWidgets.remove(fjb);
		JXTaskPane taskPane = _filterJobBuilders.remove(fjb);
		_taskPaneContainer.remove(taskPane);
	}
}
