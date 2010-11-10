package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterChangeListener;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.datacleaner.actions.AddFilterActionListener;
import org.eobjects.datacleaner.output.beans.CsvOutputAnalyzer;
import org.eobjects.datacleaner.output.beans.DatastoreOutputAnalyzer;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.builder.WidgetFactory;
import org.eobjects.datacleaner.widgets.properties.ChangeRequirementButton;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;

public class FilterListPanel extends DCPanel implements FilterChangeListener {

	private static final long serialVersionUID = 1L;

	private final Map<FilterJobBuilder<?, ?>, JXTaskPane> _filterJobBuilders;
	private final Map<FilterJobBuilder<?, ?>, PropertyWidgetFactory> _propertyWidgetFactories;
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final JXTaskPaneContainer _taskPaneContainer;

	public FilterListPanel(AnalyzerBeansConfiguration configuration, AnalysisJobBuilder analysisJobBuilder) {
		super(ImageManager.getInstance().getImage("images/window/filters-tab-background.png"), 95, 95,
				WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		setLayout(new BorderLayout());
		_filterJobBuilders = new IdentityHashMap<FilterJobBuilder<?, ?>, JXTaskPane>();
		_propertyWidgetFactories = new IdentityHashMap<FilterJobBuilder<?, ?>, PropertyWidgetFactory>();
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
		Set<FilterJobBuilder<?, ?>> filterJobBuilders = _propertyWidgetFactories.keySet();
		for (FilterJobBuilder<?, ?> filterJobBuilder : filterJobBuilders) {
			PropertyWidgetFactory propertyWidgetFactory = _propertyWidgetFactories.get(filterJobBuilder);
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
		final PropertyWidgetFactory propertyWidgetFactory = _propertyWidgetFactories.get(fjb);
		final FilterBeanDescriptor<?, ?> descriptor = fjb.getDescriptor();
		taskPane.setTitle(descriptor.getDisplayName());
		taskPane.setIcon(IconUtils.getDescriptorIcon(fjb.getDescriptor(), IconUtils.ICON_SIZE_SMALL));

		final DCPanel panel = new DCPanel();

		final ChangeRequirementButton requirementButton = new ChangeRequirementButton(_analysisJobBuilder, fjb);

		final JButton removeButton = new JButton("Remove filter", ImageManager.getInstance().getImageIcon(
				"images/actions/remove.png", IconUtils.ICON_SIZE_SMALL));
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_analysisJobBuilder.removeFilter(fjb);
				FilterListPanel.this.updateUI();
			}
		});

		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new VerticalLayout(4));
		buttonPanel.add(requirementButton);
		buttonPanel.add(removeButton);

		WidgetUtils.addToGridBag(buttonPanel, panel, 2, 0, 1, 1, GridBagConstraints.NORTHEAST, 4);

		int i = 0;
		for (ConfiguredPropertyDescriptor propertyDescriptor : descriptor.getConfiguredProperties()) {
			JLabel label = new JLabel(propertyDescriptor.getName());
			label.setOpaque(false);
			WidgetUtils.addToGridBag(label, panel, 0, i, 1, 1, GridBagConstraints.NORTHEAST, 4);

			PropertyWidget<?> propertyWidget = propertyWidgetFactory.create(propertyDescriptor);
			WidgetUtils.addToGridBag(propertyWidget.getWidget(), panel, 1, i, 1, 1, GridBagConstraints.NORTHWEST, 4);
			i++;
		}

		final DCPanel outcomePanel = new DCPanel();
		outcomePanel.setBorder(new TitledBorder("Outcomes"));

		final Set<String> categoryNames = descriptor.getCategoryNames();
		for (final String categoryName : categoryNames) {
			final JButton outcomeButton = new JButton(categoryName);
			outcomeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JPopupMenu popup = new JPopupMenu();

					JMenuItem saveAsDatastoreMenuItem = new JMenuItem("Create new datastore from outcome");
					saveAsDatastoreMenuItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {

							RowProcessingAnalyzerJobBuilder<DatastoreOutputAnalyzer> ajb = _analysisJobBuilder
									.addRowProcessingAnalyzer(DatastoreOutputAnalyzer.class);
							ajb.getConfigurableBean().setDatastoreName(
									"output-" + descriptor.getDisplayName() + "-" + categoryName);
							ajb.setRequirement(fjb, categoryName);
							ajb.onConfigurationChanged();
						}
					});
					popup.add(saveAsDatastoreMenuItem);

					JMenuItem saveToCsvFileMenuItem = new JMenuItem("Create CSV file from outcome");
					saveToCsvFileMenuItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {

							RowProcessingAnalyzerJobBuilder<CsvOutputAnalyzer> ajb = _analysisJobBuilder
									.addRowProcessingAnalyzer(CsvOutputAnalyzer.class);
							File file = new File("output-" + descriptor.getDisplayName() + "-" + categoryName + ".csv");
							ajb.getConfigurableBean().setFile(file);
							ajb.setRequirement(fjb, categoryName);
							ajb.onConfigurationChanged();
						}
					});
					popup.add(saveToCsvFileMenuItem);

					popup.show(outcomeButton, 0, outcomeButton.getHeight());
				}
			});
			outcomePanel.add(outcomeButton);
		}

		WidgetUtils.addToGridBag(outcomePanel, panel, 1, i, 2, 1, GridBagConstraints.NORTHWEST, 4);

		taskPane.add(panel);
		return taskPane;
	}

	@Override
	public void removeNotify() {
		_analysisJobBuilder.getFilterChangeListeners().remove(this);
		super.removeNotify();
	}

	@Override
	public void onAdd(FilterJobBuilder<?, ?> fjb) {
		_propertyWidgetFactories.put(fjb, new PropertyWidgetFactory(_analysisJobBuilder, fjb));
		JXTaskPane taskPane = createTaskPane(fjb);
		_filterJobBuilders.put(fjb, taskPane);
		_taskPaneContainer.add(taskPane);
	}

	@Override
	public void onRemove(FilterJobBuilder<?, ?> fjb) {
		_propertyWidgetFactories.remove(fjb);
		JXTaskPane taskPane = _filterJobBuilders.remove(fjb);
		_taskPaneContainer.remove(taskPane);
	}

	@Override
	public void onConfigurationChanged(FilterJobBuilder<?, ?> filterJobBuilder) {
	}

	@Override
	public void onRequirementChanged(FilterJobBuilder<?, ?> filterJobBuilder) {
	}
}
