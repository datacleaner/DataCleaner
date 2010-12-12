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

import java.awt.Cursor;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
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
import org.eobjects.datacleaner.util.LabelConstants;
import org.eobjects.datacleaner.util.LookAndFeelManager;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.eobjects.datacleaner.windows.DetailsResultWindow;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.eobjects.metamodel.schema.Table;

@RendererBean(SwingRenderingFormat.class)
public class ValueDistributionResultSwingRenderer implements Renderer<ValueDistributionResult, JComponent> {

	private static final Logger logger = LoggerFactory.getLogger(ValueDistributionResultSwingRenderer.class);

	private static final int PREFERRED_SLICES = 42;
	private static final int MAX_SLICES = 50;

	private final Map<String, PieSliceGroup> _groups = new HashMap<String, PieSliceGroup>();
	private final DefaultPieDataset _dataset = new DefaultPieDataset();

	@Override
	public JComponent render(ValueDistributionResult result) {
		final String columnName = result.getColumnName();

		// create a special group for the unique values
		final Collection<String> uniqueValues = result.getUniqueValues();
		if (uniqueValues != null && !uniqueValues.isEmpty()) {
			PieSliceGroup pieSliceGroup = new PieSliceGroup(LabelConstants.UNIQUE_LABEL, uniqueValues, 1);
			_groups.put(pieSliceGroup.getName(), pieSliceGroup);
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

			if (userSpecifiedGroups && topValueCounts.size() + bottomValueCounts.size() < MAX_SLICES) {
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
				
				for (PieSliceGroup group : _groups.values()) {
					_dataset.setValue(group.getName(), group.getTotalCount());
				}
				
				for (ValueCount valueCount : valueCounts) {
					_dataset.setValue(valueCount.getValue(), valueCount.getCount());
				}
			}
		}

		logger.info("Rendering with {} slices", _dataset.getItemCount());

		// table for drill-to-detail information
		final DCTable drillableValuesTable = new DCTable("Value", LabelConstants.COUNT_LABEL);

		// chart for display of the dataset
		final JFreeChart chart = ChartFactory.createPieChart3D(columnName, _dataset, false, true, false);
		chart.setTextAntiAlias(true);
		final ChartPanel chartPanel = new ChartPanel(chart);
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
						final PieSliceGroup group = _groups.get(sectionKey);
						final TableModel model = new DefaultTableModel(new String[] { sectionKey + " value",
								LabelConstants.COUNT_LABEL }, group.size());

						final Iterator<ValueCount> valueCounts = group.getValueCounts();
						int i = 0;
						while (valueCounts.hasNext()) {
							ValueCount vc = valueCounts.next();
							model.setValueAt(vc.getValue(), i, 0);
							model.setValueAt(vc.getCount(), i, 1);
							i++;
						}
						drillableValuesTable.setModel(model);
					}
				}
			}
		});

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		split.add(chartPanel);
		split.add(drillableValuesTable.toPanel());
		split.setDividerLocation(550);

		return split;
	}

	private void createGroups(List<ValueCount> valueCounts) {
		// this map will contain frequency counts that are not groupable in
		// this block because there is only one occurrence
		final Set<Integer> skipCounts = new HashSet<Integer>();

		int previousGroupFrequency = 1;

		int datasetItemCount = _dataset.getItemCount();
		while (datasetItemCount + _groups.size() + valueCounts.size() > PREFERRED_SLICES
				&& _groups.size() <= PREFERRED_SLICES) {
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

			logger.info("Lowest repeated frequency above {} found: {}. Fetching from {} ungrouped values", new Object[] {
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
				logger.debug("Values removed from ungrouped values");
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
			if (datasetItemCount + _groups.size() + valueCounts.size() > MAX_SLICES) {
				final int aggregatedGroupCount = MAX_SLICES - _groups.size();
				logger.info("Amount of groups outgrowed the preferred count, creating {} aggregated groups",
						aggregatedGroupCount);

				int maxFrequency = -1;
				int minFrequency = -1;

				for (ValueCount vc : valueCounts) {
					final int count = vc.getCount();
					if (maxFrequency == -1) {
						maxFrequency = count;
						minFrequency = count;
					} else {
						maxFrequency = Math.max(count, maxFrequency);
						minFrequency = Math.min(count, minFrequency);
					}
				}

				final int diffFrequency = maxFrequency - minFrequency;

				logger.info("Creating {} groups for counts between {} and {}", new Object[] { aggregatedGroupCount,
						minFrequency, maxFrequency });

				final PieSliceGroup[] aggregatedGroups = new PieSliceGroup[aggregatedGroupCount];
				final int[] minCounts = new int[aggregatedGroupCount];
				final int[] maxCounts = new int[aggregatedGroupCount];
				for (int i = 0; i < aggregatedGroupCount; i++) {
					// set the interval of counts that this group will contain
					// values for
					minCounts[i] = minFrequency + (i * diffFrequency / aggregatedGroupCount);
					maxCounts[i] = minFrequency + ((i + 1) * diffFrequency / aggregatedGroupCount) - 1;
					if (i + 1 == aggregatedGroupCount) {
						maxCounts[i]++;
					}

					logger.info("Building group({}) for counts between {} and {}", new Object[] { i, minCounts[i],
							maxCounts[i] });
					String groupName = "<count=[" + minCounts[i] + "-" + maxCounts[i] + "]>";

					aggregatedGroups[i] = new PieSliceGroup(groupName, new ArrayList<ValueCount>());

					_groups.put(groupName, aggregatedGroups[i]);
				}

				for (Iterator<ValueCount> it = valueCounts.iterator(); it.hasNext();) {
					ValueCount vc = it.next();

					boolean found = false;
					int count = vc.getCount();
					for (int i = 0; i < aggregatedGroupCount; i++) {
						if (count >= minCounts[i] && count <= maxCounts[i]) {

							PieSliceGroup group = aggregatedGroups[i];
							group.addValueCount(vc);

							found = true;
							break;
						}
					}

					if (found) {
						it.remove();
					}
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
		Table table = dcp.getSchemaNavigator().convertToTable("PUBLIC.PAYMENTS");
		ajb.setDatastore(ds);
		ajb.addSourceColumns(table.getColumns());
		ajb.addRowProcessingAnalyzer(ValueDistributionAnalyzer.class).addInputColumns(ajb.getSourceColumns());
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
