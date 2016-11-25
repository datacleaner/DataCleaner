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
package org.datacleaner.widgets.result;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseWheelEvent;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollBar;

import org.apache.metamodel.schema.Table;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.RendererBean;
import org.datacleaner.beans.dategap.DateGapAnalyzer;
import org.datacleaner.beans.dategap.DateGapAnalyzerResult;
import org.datacleaner.beans.dategap.TimeInterval;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.components.convert.ConvertToStringTransformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.guice.DCModuleImpl;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.result.renderer.AbstractRenderer;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.result.renderer.SwingRenderingFormat;
import org.datacleaner.util.ChartUtils;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.LookAndFeelManager;
import org.datacleaner.util.VFSUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.windows.DetailsResultWindow;
import org.jdesktop.swingx.VerticalLayout;
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
    private static final int GROUPS_VISIBLE = 8;

    @Override
    public JComponent render(final DateGapAnalyzerResult result) {

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
            final Task completeDurationTask =
                    new Task(groupDisplayName, createTimePeriod(completeDuration.getFrom(), completeDuration.getTo()));
            completeDurationTaskSeries.add(completeDurationTask);

            // plot gaps
            {
                final SortedSet<TimeInterval> gaps = result.getGaps(groupName);

                int i = 1;
                Task rootTask = null;
                for (final TimeInterval interval : gaps) {
                    final TimePeriod timePeriod = createTimePeriod(interval.getFrom(), interval.getTo());

                    if (rootTask == null) {
                        rootTask = new Task(groupDisplayName, timePeriod);
                        gapsTaskSeries.add(rootTask);
                    } else {
                        final Task task = new Task(groupDisplayName + " gap" + i, timePeriod);
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
                for (final TimeInterval interval : overlaps) {
                    final TimePeriod timePeriod = createTimePeriod(interval.getFrom(), interval.getTo());

                    if (rootTask == null) {
                        rootTask = new Task(groupDisplayName, timePeriod);
                        overlapsTaskSeries.add(rootTask);
                    } else {
                        final Task task = new Task(groupDisplayName + " overlap" + i, timePeriod);
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

        final JFreeChart chart = ChartFactory.createGanttChart(
                "Date gaps and overlaps in " + result.getFromColumnName() + " / " + result.getToColumnName(),
                result.getGroupColumnName(), "Time", slidingDataset, true, true, false);
        ChartUtils.applyStyles(chart);

        // make sure the 3 timeline types have correct coloring
        {
            final CategoryPlot plot = (CategoryPlot) chart.getPlot();

            plot.setDrawingSupplier(
                    new DCDrawingSupplier(WidgetUtils.BG_COLOR_GREEN_BRIGHT, WidgetUtils.ADDITIONAL_COLOR_RED_BRIGHT,
                            WidgetUtils.BG_COLOR_BLUE_BRIGHT));
        }

        final int visibleLines = Math.min(GROUPS_VISIBLE, groupNames.size());
        final ChartPanel chartPanel = ChartUtils.createPanel(chart, ChartUtils.WIDTH_WIDE, visibleLines * 50 + 200);

        chartPanel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseMoved(final ChartMouseEvent event) {
                Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
                final ChartEntity entity = event.getEntity();
                if (entity instanceof PlotEntity) {
                    cursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                }
                chartPanel.setCursor(cursor);
            }

            @Override
            public void chartMouseClicked(final ChartMouseEvent event) {
                // do nothing
            }
        });

        final JComponent decoratedChartPanel;

        final StringBuilder chartDescription = new StringBuilder("<html>");
        chartDescription.append("<p>The chart displays the recorded timeline based on FROM and TO dates.</p>");
        chartDescription.append("<p>The <b>red items</b> represent gaps in the timeline and the <b>green items</b> "
                + "represent points in the timeline where more than one record show activity.</p>");
        chartDescription.append("<p>You can <b>zoom in</b> by clicking and dragging the area that you want to "
                + "examine in further detail.</p>");

        if (groupNames.size() > GROUPS_VISIBLE) {
            final JScrollBar scroll = new JScrollBar(JScrollBar.VERTICAL);
            scroll.setMinimum(0);
            scroll.setMaximum(groupNames.size());
            scroll.addAdjustmentListener(e -> {
                final int value = e.getAdjustable().getValue();
                slidingDataset.setFirstCategoryIndex(value);
            });

            chartPanel.addMouseWheelListener(e -> {
                final int scrollType = e.getScrollType();
                if (scrollType == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                    final int wheelRotation = e.getWheelRotation();
                    scroll.setValue(scroll.getValue() + wheelRotation);
                }
            });

            final DCPanel outerPanel = new DCPanel();
            outerPanel.setLayout(new BorderLayout());
            outerPanel.add(chartPanel, BorderLayout.CENTER);
            outerPanel.add(scroll, BorderLayout.EAST);
            chartDescription.append("<p>Use the right <b>scrollbar</b> to scroll up and down on the chart.</p>");
            decoratedChartPanel = outerPanel;
        } else {
            decoratedChartPanel = chartPanel;
        }

        chartDescription.append("</html>");

        final JLabel chartDescriptionLabel = new JLabel(chartDescription.toString());

        final DCPanel panel = new DCPanel();
        panel.setLayout(new VerticalLayout());
        panel.add(chartDescriptionLabel);
        panel.add(decoratedChartPanel);

        return panel;
    }

    private TimePeriod createTimePeriod(final long from, final long to) {
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
    public static void main(final String[] args) throws Throwable {
        LookAndFeelManager.get().init();

        final Injector injector =
                Guice.createInjector(new DCModuleImpl(VFSUtils.getFileSystemManager().resolveFile("."), null));

        // run a small job
        final AnalysisJobBuilder ajb = injector.getInstance(AnalysisJobBuilder.class);
        final Datastore ds = injector.getInstance(DatastoreCatalog.class).getDatastore("orderdb");
        ajb.setDatastore(ds);

        final DataCleanerConfiguration conf = injector.getInstance(DataCleanerConfiguration.class);
        final AnalysisRunner runner = new AnalysisRunnerImpl(conf);

        final DatastoreConnection con = ds.openConnection();
        final Table table = con.getSchemaNavigator().convertToTable("PUBLIC.ORDERS");

        ajb.addSourceColumn(table.getColumnByName("ORDERDATE"));
        ajb.addSourceColumn(table.getColumnByName("SHIPPEDDATE"));
        ajb.addSourceColumn(table.getColumnByName("CUSTOMERNUMBER"));

        @SuppressWarnings("unchecked") final InputColumn<Date> orderDateColumn =
                (InputColumn<Date>) ajb.getSourceColumnByName("ORDERDATE");
        @SuppressWarnings("unchecked") final InputColumn<Date> shippedDateColumn =
                (InputColumn<Date>) ajb.getSourceColumnByName("SHIPPEDDATE");
        @SuppressWarnings("unchecked") final InputColumn<Integer> customerNumberColumn =
                (InputColumn<Integer>) ajb.getSourceColumnByName("CUSTOMERNUMBER");
        @SuppressWarnings("unchecked") final MutableInputColumn<String> customerNumberAsStringColumn =
                (MutableInputColumn<String>) ajb.addTransformer(ConvertToStringTransformer.class)
                        .addInputColumn(customerNumberColumn).getOutputColumns().get(0);

        final DateGapAnalyzer dga = ajb.addAnalyzer(DateGapAnalyzer.class).getComponentInstance();
        dga.setFromColumn(orderDateColumn);
        dga.setToColumn(shippedDateColumn);
        dga.setGroupColumn(customerNumberAsStringColumn);

        final AnalysisResultFuture resultFuture = runner.run(ajb.toAnalysisJob());

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        final List<AnalyzerResult> list = Collections.emptyList();
        final RendererFactory rendererFactory = new RendererFactory(conf);
        final DetailsResultWindow window =
                new DetailsResultWindow("Example", list, injector.getInstance(WindowContext.class), rendererFactory);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final List<AnalyzerResult> results = resultFuture.getResults();
        for (final AnalyzerResult analyzerResult : results) {
            final JComponent renderedResult =
                    new DateGapAnalyzerResultSwingRenderer().render((DateGapAnalyzerResult) analyzerResult);
            window.addRenderedResult(renderedResult);
        }
        window.repaint();

        window.setPreferredSize(new Dimension(800, 600));

        window.setVisible(true);
    }
}
