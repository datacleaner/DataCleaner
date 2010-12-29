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
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollBar;

import org.eobjects.analyzer.beans.DateGapAnalyzer;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.convert.ConvertToStringTransformer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.JaxbConfigurationReader;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.DateGapAnalyzerResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.analyzer.util.TimeInterval;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.LabelConstants;
import org.eobjects.datacleaner.util.LookAndFeelManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.windows.DetailsResultWindow;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.gantt.SlidingGanttCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriod;

import dk.eobjects.metamodel.schema.Table;

@RendererBean(SwingRenderingFormat.class)
public class DateGapAnalyzerResultSwingRenderer implements Renderer<DateGapAnalyzerResult, JComponent> {

	private static final int GROUPS_VISIBLE = 6;

	@Override
	public JComponent render(DateGapAnalyzerResult result) {

		final TaskSeriesCollection dataset = new TaskSeriesCollection();
		final Set<String> groupNames = result.getGroupNames();
		final TaskSeries completeDurationTaskSeries = new TaskSeries("Complete duration");
		final TaskSeries gapsTaskSeries = new TaskSeries("Gaps");
		final TaskSeries overlapsTaskSeries = new TaskSeries("Overlaps");
		for (final String groupName : groupNames) {
			final String groupDisplayName;

			if (groupName == null) {
				if (groupNames.size() == 1) {
					groupDisplayName = "All";
				} else {
					groupDisplayName = LabelConstants.NULL_LABEL;
				}
			} else {
				groupDisplayName = groupName;
			}

			final TimeInterval completeDuration = result.getCompleteDuration(groupName);
			final Task completeDurationTask = new Task(groupDisplayName, new SimpleTimePeriod(completeDuration.getFrom(),
					completeDuration.getTo()));
			completeDurationTaskSeries.add(completeDurationTask);

			final SortedSet<TimeInterval> gaps = result.getGaps(groupName);

			for (TimeInterval interval : gaps) {
				final TimePeriod timePeriod = new SimpleTimePeriod(interval.getFrom(), interval.getTo());
				Task task = new Task(groupDisplayName, timePeriod);
				gapsTaskSeries.add(task);
			}

			final SortedSet<TimeInterval> overlaps = result.getOverlaps(groupName);

			for (TimeInterval interval : overlaps) {
				final TimePeriod timePeriod = new SimpleTimePeriod(interval.getFrom(), interval.getTo());
				Task task = new Task(groupDisplayName, timePeriod);
				overlapsTaskSeries.add(task);
			}
		}
		dataset.add(overlapsTaskSeries);
		dataset.add(gapsTaskSeries);
		dataset.add(completeDurationTaskSeries);

		final SlidingGanttCategoryDataset slidingDataset = new SlidingGanttCategoryDataset(dataset, 0, GROUPS_VISIBLE);

		final JFreeChart chart = ChartFactory.createGanttChart("Date gaps and overlaps in " + result.getFromColumnName()
				+ " / " + result.getToColumnName(), result.getGroupColumnName(), "Time", slidingDataset, true, true, true);

		// tweaks of the look and feel of the graph
		{
			chart.getTitle().setFont(WidgetUtils.FONT_HEADER);
			chart.getLegend().setItemFont(WidgetUtils.FONT_SMALL);

			final CategoryPlot plot = (CategoryPlot) chart.getPlot();

			plot.setDomainGridlinesVisible(true);
			plot.setDomainGridlinePaint(WidgetUtils.BG_COLOR_DARK);
			plot.setDomainGridlinePosition(CategoryAnchor.END);

			CategoryAxis domainAxis = plot.getDomainAxis();
			domainAxis.setTickLabelFont(WidgetUtils.FONT_SMALL);
			domainAxis.setLabelFont(WidgetUtils.FONT_NORMAL);

			ValueAxis rangeAxis = plot.getRangeAxis();
			rangeAxis.setTickLabelFont(WidgetUtils.FONT_SMALL);
			rangeAxis.setLabelFont(WidgetUtils.FONT_NORMAL);

			plot.setDrawingSupplier(new DCDrawingSupplier(WidgetUtils.ADDITIONAL_COLOR_GREEN_BRIGHT,
					WidgetUtils.ADDITIONAL_COLOR_RED_BRIGHT, WidgetUtils.BG_COLOR_BLUE_BRIGHT));
		}

		final ChartPanel chartPanel = new ChartPanel(chart);

		final int visibleLines = Math.min(GROUPS_VISIBLE, groupNames.size());

		chartPanel.setPreferredSize(new Dimension(0, visibleLines * 50 + 200));

		JScrollBar scroll = new JScrollBar(JScrollBar.VERTICAL);
		scroll.setMinimum(0);
		scroll.setMaximum(groupNames.size());
		scroll.addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				int value = e.getAdjustable().getValue();
				slidingDataset.setFirstCategoryIndex(value);
			}
		});

		final JComponent renderedComponent;

		if (groupNames.size() > GROUPS_VISIBLE) {
			final DCPanel outerPanel = new DCPanel();
			outerPanel.setLayout(new BorderLayout());
			outerPanel.add(chartPanel, BorderLayout.CENTER);
			outerPanel.add(scroll, BorderLayout.EAST);
			renderedComponent = outerPanel;
		} else {
			renderedComponent = chartPanel;
		}

		return renderedComponent;
	}

	/**
	 * A main method that will display the results of a few example value
	 * distributions. Useful for tweaking the charts and UI.
	 * 
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
		LookAndFeelManager.getInstance().init();

		// run a small job
		AnalyzerBeansConfiguration conf = new JaxbConfigurationReader().create(new File("conf.xml"));
		AnalysisRunner runner = new AnalysisRunnerImpl(conf);
		AnalysisJobBuilder ajb = new AnalysisJobBuilder(conf);

		Datastore ds = conf.getDatastoreCatalog().getDatastore("orderdb");
		ajb.setDatastore(ds);

		DataContextProvider dcp = ds.getDataContextProvider();
		Table table = dcp.getSchemaNavigator().convertToTable("PUBLIC.ORDERS");

		ajb.addSourceColumn(table.getColumnByName("ORDERDATE"));
		ajb.addSourceColumn(table.getColumnByName("SHIPPEDDATE"));
		ajb.addSourceColumn(table.getColumnByName("CUSTOMERNUMBER"));

		@SuppressWarnings("unchecked")
		InputColumn<Date> orderDateColumn = (InputColumn<Date>) ajb.getSourceColumnByName("ORDERDATE");
		@SuppressWarnings("unchecked")
		InputColumn<Date> shippedDateColumn = (InputColumn<Date>) ajb.getSourceColumnByName("SHIPPEDDATE");
		@SuppressWarnings("unchecked")
		InputColumn<Integer> customerNumberColumn = (InputColumn<Integer>) ajb.getSourceColumnByName("CUSTOMERNUMBER");
		@SuppressWarnings("unchecked")
		MutableInputColumn<String> customerNumberAsStringColumn = (MutableInputColumn<String>) ajb
				.addTransformer(ConvertToStringTransformer.class).addInputColumn(customerNumberColumn).getOutputColumns()
				.get(0);

		DateGapAnalyzer dga = ajb.addRowProcessingAnalyzer(DateGapAnalyzer.class).getConfigurableBean();
		dga.setFromColumn(orderDateColumn);
		dga.setToColumn(shippedDateColumn);
		dga.setGroupColumn(customerNumberAsStringColumn);

		AnalysisResultFuture resultFuture = runner.run(ajb.toAnalysisJob());

		if (resultFuture.isErrornous()) {
			throw resultFuture.getErrors().get(0);
		}

		List<AnalyzerResult> list = Collections.emptyList();
		DetailsResultWindow window = new DetailsResultWindow("Example", list);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		List<AnalyzerResult> results = resultFuture.getResults();
		for (AnalyzerResult analyzerResult : results) {
			JComponent renderedResult = new DateGapAnalyzerResultSwingRenderer()
					.render((DateGapAnalyzerResult) analyzerResult);
			window.addRenderedResult(renderedResult);
			window.repaint();
		}

		window.setVisible(true);
	}
}
