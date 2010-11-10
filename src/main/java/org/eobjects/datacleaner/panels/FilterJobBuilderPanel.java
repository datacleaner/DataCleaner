package org.eobjects.datacleaner.panels;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.TitledBorder;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.datacleaner.output.beans.CsvOutputAnalyzer;
import org.eobjects.datacleaner.output.beans.DatastoreOutputAnalyzer;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.properties.ChangeRequirementButton;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.jdesktop.swingx.VerticalLayout;

public class FilterJobBuilderPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private final PropertyWidgetFactory _propertyWidgetFactory;
	private final FilterJobBuilder<?, ?> _filterJobBuilder;
	private final ChangeRequirementButton _requirementButton;
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final FilterBeanDescriptor<?, ?> _descriptor;

	public FilterJobBuilderPanel(AnalysisJobBuilder analysisJobBuilder, FilterJobBuilder<?, ?> filterJobBuilder) {
		_analysisJobBuilder = analysisJobBuilder;
		_filterJobBuilder = filterJobBuilder;
		_propertyWidgetFactory = new PropertyWidgetFactory(analysisJobBuilder, filterJobBuilder);
		_requirementButton = new ChangeRequirementButton(analysisJobBuilder, filterJobBuilder);
		_descriptor = _filterJobBuilder.getDescriptor();

		final JButton removeButton = new JButton("Remove filter", ImageManager.getInstance().getImageIcon(
				"images/actions/remove.png", IconUtils.ICON_SIZE_SMALL));
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_analysisJobBuilder.removeFilter(_filterJobBuilder);
			}
		});

		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new VerticalLayout(4));
		buttonPanel.add(_requirementButton);
		buttonPanel.add(removeButton);

		WidgetUtils.addToGridBag(buttonPanel, this, 2, 0, 1, 1, GridBagConstraints.NORTHEAST, 4);

		int i = 0;
		for (ConfiguredPropertyDescriptor propertyDescriptor : _descriptor.getConfiguredProperties()) {
			JLabel label = new JLabel(propertyDescriptor.getName());
			label.setOpaque(false);
			WidgetUtils.addToGridBag(label, this, 0, i, 1, 1, GridBagConstraints.NORTHEAST, 4);

			PropertyWidget<?> propertyWidget = _propertyWidgetFactory.create(propertyDescriptor);
			WidgetUtils.addToGridBag(propertyWidget.getWidget(), this, 1, i, 1, 1, GridBagConstraints.NORTHWEST, 4);
			i++;
		}

		final DCPanel outcomePanel = new DCPanel();
		outcomePanel.setBorder(new TitledBorder("Outcomes"));

		final Set<String> categoryNames = _descriptor.getCategoryNames();
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
									"output-" + _descriptor.getDisplayName() + "-" + categoryName);
							ajb.setRequirement(_filterJobBuilder, categoryName);
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
							File file = new File("output-" + _descriptor.getDisplayName() + "-" + categoryName + ".csv");
							ajb.getConfigurableBean().setFile(file);
							ajb.setRequirement(_filterJobBuilder, categoryName);
							ajb.onConfigurationChanged();
						}
					});
					popup.add(saveToCsvFileMenuItem);

					popup.show(outcomeButton, 0, outcomeButton.getHeight());
				}
			});
			outcomePanel.add(outcomeButton);
		}

		WidgetUtils.addToGridBag(outcomePanel, this, 1, i, 2, 1, GridBagConstraints.NORTHWEST, 4);
	}

	public PropertyWidgetFactory getPropertyWidgetFactory() {
		return _propertyWidgetFactory;
	}

	public void onRequirementChanged() {
		_requirementButton.updateText();
	}
}
