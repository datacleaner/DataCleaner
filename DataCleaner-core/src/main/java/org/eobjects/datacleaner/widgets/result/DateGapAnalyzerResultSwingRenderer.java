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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

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
import org.eobjects.analyzer.result.renderer.AbstractRenderer;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.analyzer.util.TimeInterval;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.DCModule;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.DataCleanerHome;
import org.eobjects.datacleaner.util.ChartUtils;
import org.eobjects.datacleaner.util.LabelUtils;
import org.eobjects.datacleaner.util.LookAndFeelManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.windows.DetailsResultWindow;
import org.eobjects.metamodel.schema.Table;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PlotEntity;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.gantt.SlidingGanttCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

@RendererBean(SwingRenderingFormat.class)
public class DateGapAnalyzerResultSwingRenderer extends AbstractRenderer<DateGapAnalyzerResult, JComponent> {

	private static final Logger logger = LoggerFactory.getLogger(DateGapAnalyzerResultSwingRenderer.class);

	private static final String LABEL_OVERLAPS = "Overlaps";
	private static final String LABEL_GAPS = "Gaps";
	private static final String LABEL_COMPLETE_DURATION = "Complete duration";
	private static final int GROUPS_VISIBLE = 6;

	@Override
	public JComponent render(DateGapAnalyzerResult result) {

		final TaskSeriesCollection dataset = new TaskSeriesCollection();
		final Set<String> groupNames = result.getGroupNames();
		final TaskSeries completeDurationTaskSeries = new TaskSeries(LABEL_COMPLETE_DURATION);
		final TaskSeries gapsTaskSeries = new TaskSeries(LABEL_GAPS);
		final TaskSeries overlapsTaskSeries = new TaskSeries(LABEL_OVERLAPS);
		for (final String groupName : groupNames) {
			final String groupDisplayName;

			if (groupName == null) {
				if (groupNames.size() == 1) {
					groupDisplayName = "All";
				} else {
					groupDisplayName = LabelUtils.NULL_LABEL;
				}
			} else {
				groupDisplayName = groupName;
			}

			final TimeInterval completeDuration = result.getCompleteDuration(groupName);
			final Task completeDurationTask = new Task(groupDisplayName, createTimePeriod(completeDuration.getFrom(),
					completeDuration.getTo()));
			completeDurationTaskSeries.add(completeDurationTask);

			// plot gaps
			{
				final SortedSet<TimeInterval> gaps = result.getGaps(groupName);

				int i = 1;
				Task rootTask = null;
				for (TimeInterval interval : gaps) {
					final TimePeriod timePeriod = createTimePeriod(interval.getFrom(), interval.getTo());

					if (rootTask == null) {
						rootTask = new Task(groupDisplayName, timePeriod);
						gapsTaskSeries.add(rootTask);
					} else {
						Task task = new Task(groupDisplayName + " gap" + i, timePeriod);
						rootTask.addSubtask(task);
					}

					i++;
				}
			}

			// plot overlaps
			{
				final SortedSet<TimeInterval> overlaps = result.getOverlaps(groupName);

				int i = 1;
				Task rootTask = null;
				for (TimeInterval interval : overlaps) {
					final TimePeriod timePeriod = createTimePeriod(interval.getFrom(), interval.getTo());

					if (rootTask == null) {
						rootTask = new Task(groupDisplayName, timePeriod);
						overlapsTaskSeries.add(rootTask);
					} else {
						Task task = new Task(groupDisplayName + " overlap" + i, timePeriod);
						rootTask.addSubtask(task);
					}

					i++;
				}
			}
		}
		dataset.add(overlapsTaskSeries);
		dataset.add(gapsTaskSeries);
		dataset.add(completeDurationTaskSeries);

		final SlidingGanttCategoryDataset slidingDataset = new SlidingGanttCategoryDataset(dataset, 0, GROUPS_VISIBLE);

		final JFreeChart chart = ChartFactory.createGanttChart("Date gaps and overlaps in " + result.getFromColumnName()
				+ " / " + result.getToColumnName(), result.getGroupColumnName(), "Time", slidingDataset, true, true, false);
		ChartUtils.applyStyles(chart);

		// make sure the 3 timeline types have correct coloring
		{
			final CategoryPlot plot = (CategoryPlot) chart.getPlot();

			plot.setDrawingSupplier(new DCDrawingSupplier(WidgetUtils.ADDITIONAL_COLOR_GREEN_BRIGHT,
					WidgetUtils.ADDITIONAL_COLOR_RED_BRIGHT, WidgetUtils.BG_COLOR_BLUE_BRIGHT));
		}

		final ChartPanel chartPanel = new ChartPanel(chart);

		chartPanel.addChartMouseListener(new ChartMouseListener() {
			@Override
			public void chartMouseMoved(ChartMouseEvent event) {
				Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
				ChartEntity entity = event.getEntity();
				if (entity instanceof PlotEntity) {
					cursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
				}
				chartPanel.setCursor(cursor);
			}

			@Override
			public void chartMouseClicked(ChartMouseEvent event) {
				// do nothing
			}
		});

		final int visibleLines = Math.min(GROUPS_VISIBLE, groupNames.size());

		chartPanel.setPreferredSize(new Dimension(0, visibleLines * 50 + 200));

		final JComponent decoratedChartPanel;

		StringBuilder chartDescription = new StringBuilder();
		chartDescription.append("<html><p>The chart displays the recorded timeline based on FROM and TO dates.<br/><br/>");
		chartDescription
				.append("The <b>red items</b> represent gaps in the timeline and the <b>green items</b> represent points in the timeline where more than one record show activity.<br/><br/>");
		chartDescription
				.append("You can <b>zoom in</b> by clicking and dragging the area that you want to examine in further detail.");

		if (groupNames.size() > GROUPS_VISIBLE) {
			final JScrollBar scroll = new JScrollBar(JScrollBar.VERTICAL);
			scroll.setMinimum(0);
			scroll.setMaximum(groupNames.size());
			scroll.addAdjustmentListener(new AdjustmentListener() {

				@Override
				public void adjustmentValueChanged(AdjustmentEvent e) {
					int value = e.getAdjustable().getValue();
					slidingDataset.setFirstCategoryIndex(value);
				}
			});

			chartPanel.addMouseWheelListener(new MouseWheelListener() {

				@Override
				public void mouseWheelMoved(MouseWheelEvent e) {
					int scrollType = e.getScrollType();
					if (scrollType == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
						int wheelRotation = e.getWheelRotation();
						scroll.setValue(scroll.getValue() + wheelRotation);
					}
				}
			});

			final DCPanel outerPanel = new DCPanel();
			outerPanel.setLayout(new BorderLayout());
			outerPanel.add(chartPanel, BorderLayout.CENTER);
			outerPanel.add(scroll, BorderLayout.EAST);
			chartDescription.append("<br/><br/>Use the right <b>scrollbar</b> to scroll up and down on the chart.");
			decoratedChartPanel = outerPanel;

		} else {
			decoratedChartPanel = chartPanel;
		}

		chartDescription.append("</p></html>");

		final JLabel chartDescriptionLabel = new JLabel(chartDescription.toString());

		chartDescriptionLabel.setBorder(new EmptyBorder(4, 10, 4, 10));

		final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.add(decoratedChartPanel);
		split.add(chartDescriptionLabel);
		split.setDividerLocation(550);

		return split;
	}

	private TimePeriod createTimePeriod(long from, long to) {
		if (from > to) {
			logger.warn("An illegal from/to combination occurred: {}, {}", from, to);
		}
		return new SimpleTimePeriod(from, to);
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
		final AnalyzerBeansConfiguration conf = new JaxbConfigurationReader().create(new File(DataCleanerHome.get(),
				"conf.xml"));
		final Injector injector = Guice.createInjector(new DCModule(new File(".")));
		final AnalysisJobBuilder ajb = injector.getInstance(AnalysisJobBuilder.class);
		final AnalysisRunner runner = new AnalysisRunnerImpl(conf);

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
		RendererFactory rendererFactory = new RendererFactory(conf.getDescriptorProvider(), null);
		DetailsResultWindow window = new DetailsResultWindow("Example", list, injector.getInstance(WindowContext.class),
				rendererFactory);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		List<AnalyzerResult> results = resultFuture.getResults();
		for (AnalyzerResult analyzerResult : results) {
			JComponent renderedResult = new DateGapAnalyzerResultSwingRenderer()
					.render((DateGapAnalyzerResult) analyzerResult);
			window.addRenderedResult(renderedResult);
		}
		window.repaint();

		window.setPreferredSize(new Dimension(800, 600));

		window.setVisible(true);
	}
}
