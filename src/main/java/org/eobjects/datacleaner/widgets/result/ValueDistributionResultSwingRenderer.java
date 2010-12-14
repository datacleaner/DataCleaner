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
package org.eobjects.datacleaner.widgets.result;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.valuedist.ValueCount;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.JaxbConfigurationReader;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.ValueDistributionResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.LabelConstants;
import org.eobjects.datacleaner.util.LookAndFeelManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.eobjects.datacleaner.windows.DetailsResultWindow;
import org.jdesktop.swingx.VerticalLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.schema.Table;

@RendererBean(SwingRenderingFormat.class)
public class ValueDistributionResultSwingRenderer implements Renderer<ValueDistributionResult, JComponent> {

	private static final Logger logger = LoggerFactory.getLogger(ValueDistributionResultSwingRenderer.class);

	private static final Color[] SLICE_COLORS = new Color[] { WidgetUtils.BG_COLOR_BLUE_BRIGHT,
			WidgetUtils.ADDITIONAL_COLOR_GREEN_BRIGHT, WidgetUtils.ADDITIONAL_COLOR_RED_BRIGHT, WidgetUtils.BG_COLOR_MEDIUM };

	private static final int DEFAULT_PREFERRED_SLICES = 32;
	private static final int DEFAULT_MAX_SLICES = 40;

	private final Map<String, PieSliceGroup> _groups = new HashMap<String, PieSliceGroup>();
	private final DefaultPieDataset _dataset = new DefaultPieDataset();
	private final JButton _backButton = WidgetFactory.createButton("Back",
			ImageManager.getInstance().getImageIcon("images/actions/back.png"));
	private final int _preferredSlices;
	private final int _maxSlices;

	/**
	 * Default constructor
	 */
	public ValueDistributionResultSwingRenderer() {
		this(DEFAULT_PREFERRED_SLICES, DEFAULT_MAX_SLICES);
	}

	/**
	 * Alternative constructor (primarily used for testing) with customizable
	 * slice count
	 * 
	 * @param preferredSlices
	 * @param maxSlices
	 */
	public ValueDistributionResultSwingRenderer(int preferredSlices, int maxSlices) {
		_preferredSlices = preferredSlices;
		_maxSlices = maxSlices;
	}

	@Override
	public JComponent render(ValueDistributionResult result) {
		final String columnName = result.getColumnName();

		// create a special group for the unique values
		final Collection<String> uniqueValues = result.getUniqueValues();
		if (uniqueValues != null && !uniqueValues.isEmpty()) {
			PieSliceGroup pieSliceGroup = new PieSliceGroup(LabelConstants.UNIQUE_LABEL, uniqueValues, 1);
			_groups.put(pieSliceGroup.getName(), pieSliceGroup);
		} else {
			int uniqueCount = result.getUniqueCount();
			if (uniqueCount > 0) {
				_dataset.setValue(LabelConstants.UNIQUE_LABEL, uniqueCount);
			}
		}

		// create a special slice for null values
		final int nullCount = result.getNullCount();
		if (nullCount > 0) {
			_dataset.setValue(LabelConstants.NULL_LABEL, nullCount);
		}

		// create the remaining "normal" slices, either individually or in
		// groups
		{
			final List<ValueCount> topValueCounts = result.getTopValues().getValueCounts();
			final List<ValueCount> bottomValueCounts = result.getBottomValues().getValueCounts();

			// result can be GC'ed now
			result = null;

			// if the user has specified the values of interest then show the
			// graph correspondingly without any grouping
			boolean userSpecifiedGroups = !topValueCounts.isEmpty() && !bottomValueCounts.isEmpty();

			if (userSpecifiedGroups || topValueCounts.size() + bottomValueCounts.size() < _preferredSlices) {
				// vanilla scenario for cleanly distributed datasets
				for (ValueCount valueCount : topValueCounts) {
					_dataset.setValue(valueCount.getValue(), valueCount.getCount());
				}
				for (ValueCount valueCount : bottomValueCounts) {
					_dataset.setValue(valueCount.getValue(), valueCount.getCount());
				}
			} else {
				// create groups of values

				List<ValueCount> valueCounts = topValueCounts;
				if (!bottomValueCounts.isEmpty()) {
					valueCounts = bottomValueCounts;
				}

				createGroups(valueCounts);

				for (ValueCount valueCount : valueCounts) {
					_dataset.setValue(valueCount.getValue(), valueCount.getCount());
				}
			}

			for (PieSliceGroup group : _groups.values()) {
				_dataset.setValue(group.getName(), group.getTotalCount());
			}
		}

		logger.info("Rendering with {} slices", _dataset.getItemCount());

		// table for drill-to-detail information
		final DCTable drillableValuesTable = new DCTable("Value", LabelConstants.COUNT_LABEL);
		drillableValuesTable.setRowHeight(22);
		drillToOverview(drillableValuesTable);

		// chart for display of the dataset
		final JFreeChart chart = ChartFactory.createPieChart3D(columnName, _dataset, false, true, false);

		// code-block for tweaking style and coloring of chart
		{
			chart.setTextAntiAlias(true);
			chart.setTitle(new TextTitle(columnName, WidgetUtils.FONT_HEADER));
			final PiePlot plot = (PiePlot) chart.getPlot();
			plot.setLabelFont(WidgetUtils.FONT_SMALL);
			plot.setSectionOutlinesVisible(false);

			int colorIndex = 0;
			for (int i = 0; i < _dataset.getItemCount(); i++) {
				final String key = (String) _dataset.getKey(i);
				if (!LabelConstants.UNIQUE_LABEL.equals(key) && !LabelConstants.NULL_LABEL.equals(key)) {
					if (i == _dataset.getItemCount() - 1) {
						// the last color should not be the same as the first
						if (colorIndex == 0) {
							colorIndex++;
						}
					}

					Color color = SLICE_COLORS[colorIndex];
					int darkAmount = i / SLICE_COLORS.length;
					for (int j = 0; j < darkAmount; j++) {
						color = WidgetUtils.slightlyDarker(color);
					}

					plot.setSectionPaint(key, color);

					colorIndex++;
					if (colorIndex >= SLICE_COLORS.length) {
						colorIndex = 0;
					}
				}
			}
			plot.setSectionPaint(LabelConstants.UNIQUE_LABEL, WidgetUtils.BG_COLOR_ORANGE_BRIGHT);
			plot.setSectionPaint(LabelConstants.NULL_LABEL, WidgetUtils.BG_COLOR_ORANGE_DARK);
		}

		final ChartPanel chartPanel = new ChartPanel(chart);
		int chartHeight = 450;
		if (_dataset.getItemCount() > 32) {
			chartHeight += 200;
		} else if (_dataset.getItemCount() > 25) {
			chartHeight += 100;
		}

		chartPanel.setPreferredSize(new Dimension(0, chartHeight));
		chartPanel.addChartMouseListener(new ChartMouseListener() {

			@Override
			public void chartMouseMoved(ChartMouseEvent event) {
				ChartEntity entity = event.getEntity();
				if (entity instanceof PieSectionEntity) {
					PieSectionEntity pieSectionEntity = (PieSectionEntity) entity;
					String sectionKey = (String) pieSectionEntity.getSectionKey();
					if (_groups.containsKey(sectionKey)) {
						chartPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					} else {
						chartPanel.setCursor(Cursor.getDefaultCursor());
					}
				} else {
					chartPanel.setCursor(Cursor.getDefaultCursor());
				}
			}

			@Override
			public void chartMouseClicked(ChartMouseEvent event) {
				ChartEntity entity = event.getEntity();
				if (entity instanceof PieSectionEntity) {
					PieSectionEntity pieSectionEntity = (PieSectionEntity) entity;
					String sectionKey = (String) pieSectionEntity.getSectionKey();
					if (_groups.containsKey(sectionKey)) {
						drillToGroup(sectionKey, drillableValuesTable);
					}
				}
			}
		});

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		DCPanel leftPanel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHTEST, WidgetUtils.BG_COLOR_BRIGHTEST);
		leftPanel.setLayout(new BorderLayout());
		leftPanel.add(chartPanel, BorderLayout.NORTH);

		DCPanel rightPanel = new DCPanel();
		rightPanel.setLayout(new VerticalLayout(2));
		_backButton.setMargin(new Insets(0, 0, 0, 0));
		_backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				drillToOverview(drillableValuesTable);
			}
		});
		rightPanel.add(_backButton);
		rightPanel.add(drillableValuesTable.toPanel());

		split.add(leftPanel);
		split.add(rightPanel);
		split.setDividerLocation(550);

		return split;
	}

	public Map<String, PieSliceGroup> getGroups() {
		return _groups;
	}

	public DefaultPieDataset getDataset() {
		return _dataset;
	}

	private void drillToOverview(final JTable table) {
		final TableModel model = new DefaultTableModel(new String[] { "Value", LabelConstants.COUNT_LABEL },
				_dataset.getItemCount());
		for (int i = 0; i < _dataset.getItemCount(); i++) {
			final String key = (String) _dataset.getKey(i);
			final int count = _dataset.getValue(i).intValue();
			model.setValueAt(key, i, 0);

			if (_groups.containsKey(key)) {
				DCPanel panel = new DCPanel();
				panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

				JLabel label = new JLabel(count + "");
				JButton button = WidgetFactory.createSmallButton("images/actions/drill-to-detail.png");
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						drillToGroup(key, table);
					}
				});

				panel.add(label);
				panel.add(Box.createHorizontalStrut(4));
				panel.add(button);

				model.setValueAt(panel, i, 1);
			} else {
				model.setValueAt(count, i, 1);
			}
		}
		table.setModel(model);
		_backButton.setVisible(false);
	}

	private void drillToGroup(String groupName, JTable table) {
		final PieSliceGroup group = _groups.get(groupName);
		final TableModel model = new DefaultTableModel(new String[] { groupName + " value", LabelConstants.COUNT_LABEL },
				group.size());

		final Iterator<ValueCount> valueCounts = group.getValueCounts();
		int i = 0;
		while (valueCounts.hasNext()) {
			ValueCount vc = valueCounts.next();
			model.setValueAt(vc.getValue(), i, 0);
			model.setValueAt(vc.getCount(), i, 1);
			i++;
		}
		table.setModel(model);
		_backButton.setVisible(true);
	}

	protected void createGroups(List<ValueCount> valueCounts) {
		// this map will contain frequency counts that are not groupable in
		// this block because there is only one occurrence
		final Set<Integer> skipCounts = new HashSet<Integer>();

		int previousGroupFrequency = 1;

		final int datasetItemCount = _dataset.getItemCount();
		while (datasetItemCount + _groups.size() < _preferredSlices) {

			if (skipCounts.size() == valueCounts.size()) {
				// only individual counted values remain
				break;
			}

			int groupFrequency = -1;

			for (ValueCount vc : valueCounts) {

				int count = vc.getCount();
				if (groupFrequency == -1) {
					groupFrequency = count;
				} else {
					if (!skipCounts.contains(count)) {
						groupFrequency = Math.min(groupFrequency, count);
					}
				}
				if (groupFrequency == previousGroupFrequency + 1) {
					// look no further - we've found the next lowest
					// frequency, none can be lower
					break;
				}
			}

			if (groupFrequency < previousGroupFrequency) {
				// could not find next group frequency - stop searching
				break;
			}

			final String groupName = "<count=" + groupFrequency + ">";
			final List<ValueCount> groupContent = new ArrayList<ValueCount>();

			logger.debug("Lowest repeated frequency above {} found: {}. Fetching from {} ungrouped values", new Object[] {
					previousGroupFrequency, groupFrequency, valueCounts.size() });

			for (Iterator<ValueCount> it = valueCounts.iterator(); it.hasNext();) {
				ValueCount vc = (ValueCount) it.next();
				final int count = vc.getCount();
				if (groupFrequency == count) {
					groupContent.add(vc);
					it.remove();
				}
			}

			if (groupContent.size() == 1) {
				logger.debug("Skipping this group because it has only one occurrence");
				skipCounts.add(groupFrequency);
				valueCounts.add(groupContent.get(0));
			} else {
				logger.info("Grouping {} values to group: {}", groupContent.size(), groupName);
				Collection<String> groupContentValues = new ArrayList<String>(groupContent.size());
				for (ValueCount valueCount : groupContent) {
					groupContentValues.add(valueCount.getValue());
				}

				PieSliceGroup group = new PieSliceGroup(groupName, groupContentValues, groupFrequency);
				_groups.put(groupName, group);
			}

			previousGroupFrequency = groupFrequency;
		}

		// code block for creating aggregated groups if slice count is still too
		// high
		{
			if (datasetItemCount + _groups.size() + valueCounts.size() > _maxSlices) {
				final int aggregatedGroupCount = _maxSlices - _groups.size();
				logger.info("Amount of groups outgrowed the preferred count, creating {} aggregated groups",
						aggregatedGroupCount);

				//
				//
				// final int diffFrequency = maxFrequency - minFrequency;
				final int aggregatedGroupSize = valueCounts.size() / aggregatedGroupCount;

				logger.info("Creating {} range groups", aggregatedGroupCount);

				for (int i = 0; i < aggregatedGroupCount; i++) {
					final LinkedList<ValueCount> groupContent = new LinkedList<ValueCount>();

					while (groupContent.size() < aggregatedGroupSize && !valueCounts.isEmpty()) {
						int minFrequency = -1;

						for (ValueCount vc : valueCounts) {
							final int count = vc.getCount();
							if (minFrequency == -1) {
								minFrequency = count;
							} else {
								minFrequency = Math.min(count, minFrequency);
							}
						}

						logger.debug("Adding values with count={} to range group {}.", minFrequency, i + 1);
						for (Iterator<ValueCount> it = valueCounts.iterator(); it.hasNext();) {
							ValueCount vc = it.next();
							if (vc.getCount() == minFrequency) {
								groupContent.add(vc);
								it.remove();
							}
						}
					}

					if (groupContent.isEmpty()) {
						break;
					}

					String groupName = "<count=[" + groupContent.getFirst().getCount() + "-"
							+ groupContent.getLast().getCount() + "]>";
					PieSliceGroup group = new PieSliceGroup(groupName, groupContent);
					_groups.put(groupName, group);
				}
			}
		}
	}

	/**
	 * A main method that will display the results of a few example value
	 * distributions. Useful for tweaking the charts and UI.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		LookAndFeelManager.getInstance().init();

		// run a small job
		AnalyzerBeansConfiguration conf = new JaxbConfigurationReader().create(new File("conf.xml"));
		AnalysisRunner runner = new AnalysisRunnerImpl(conf);
		AnalysisJobBuilder ajb = new AnalysisJobBuilder(conf);
		Datastore ds = conf.getDatastoreCatalog().getDatastore("orderdb");
		DataContextProvider dcp = ds.getDataContextProvider();
		Table table = dcp.getSchemaNavigator().convertToTable("PUBLIC.TRIAL_BALANCE");
		ajb.setDatastore(ds);
		ajb.addSourceColumns(table.getColumns());
		ajb.addRowProcessingAnalyzer(ValueDistributionAnalyzer.class).addInputColumns(ajb.getSourceColumns())
				.getConfigurableBean().setRecordUniqueValues(true);
		AnalysisResultFuture resultFuture = runner.run(ajb.toAnalysisJob());

		List<AnalyzerResult> list = Collections.emptyList();
		DetailsResultWindow window = new DetailsResultWindow("Example", list);

		List<AnalyzerResult> results = resultFuture.getResults();
		for (AnalyzerResult analyzerResult : results) {
			JComponent renderedResult = new ValueDistributionResultSwingRenderer()
					.render((ValueDistributionResult) analyzerResult);
			window.addRenderedResult(renderedResult);
			window.repaint();
		}

		window.setVisible(true);
	}
}
