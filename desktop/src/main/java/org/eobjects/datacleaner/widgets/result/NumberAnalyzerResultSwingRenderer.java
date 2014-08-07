/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

import java.awt.BasicStroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.eobjects.analyzer.beans.NumberAnalyzer;
import org.eobjects.analyzer.beans.NumberAnalyzerResult;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.analyzer.util.VFSUtils;
import org.eobjects.datacleaner.guice.DCModule;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.ChartUtils;
import org.eobjects.datacleaner.util.LookAndFeelManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.Alignment;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.eobjects.datacleaner.windows.ResultWindow;
import org.apache.metamodel.schema.Table;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.function.NormalDistributionFunction2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import com.google.inject.Guice;
import com.google.inject.Injector;

@RendererBean(SwingRenderingFormat.class)
public class NumberAnalyzerResultSwingRenderer extends AbstractCrosstabResultSwingRenderer<NumberAnalyzerResult> {

    @Override
    protected void decorate(NumberAnalyzerResult result, DCTable table, final DisplayChartCallback displayChartCallback) {
        // find the std. deviation row number.
        int rowNumber = -1;
        {
            for (int i = 0; i < table.getRowCount(); i++) {
                Object value = table.getValueAt(i, 0);
                if (NumberAnalyzer.MEASURE_STANDARD_DEVIATION.equals(value)) {
                    rowNumber = i;
                    break;
                }
            }
            if (rowNumber == -1) {
                throw new IllegalStateException("Could not determine Std. deviation row number!");
            }
        }

        Crosstab<?> crosstab = result.getCrosstab();

        final InputColumn<? extends Number>[] columns = result.getColumns();
        int columnNumber = 1;
        for (final InputColumn<? extends Number> column : columns) {
            final CrosstabNavigator<?> nav = crosstab.where(NumberAnalyzer.DIMENSION_COLUMN, column.getName());

            final Number numRows = (Number) nav.where(NumberAnalyzer.DIMENSION_MEASURE,
                    NumberAnalyzer.MEASURE_ROW_COUNT).get();
            if (numRows.intValue() > 0) {
                final Number standardDeviation = (Number) nav.where(NumberAnalyzer.DIMENSION_MEASURE,
                        NumberAnalyzer.MEASURE_STANDARD_DEVIATION).get();
                if (standardDeviation != null) {

                    ActionListener action = new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            final Number mean = (Number) nav.where(NumberAnalyzer.DIMENSION_MEASURE,
                                    NumberAnalyzer.MEASURE_MEAN).get();
                            final Number min = (Number) nav.where(NumberAnalyzer.DIMENSION_MEASURE,
                                    NumberAnalyzer.MEASURE_LOWEST_VALUE).get();
                            final Number max = (Number) nav.where(NumberAnalyzer.DIMENSION_MEASURE,
                                    NumberAnalyzer.MEASURE_HIGHEST_VALUE).get();

                            final NormalDistributionFunction2D normalDistributionFunction = new NormalDistributionFunction2D(
                                    mean.doubleValue(), standardDeviation.doubleValue());
                            final XYDataset dataset = DatasetUtilities.sampleFunction2D(normalDistributionFunction,
                                    min.doubleValue(), max.doubleValue(), 100, "Normal");

                            final JFreeChart chart = ChartFactory.createXYLineChart(
                                    "Normal distribution of " + column.getName(), column.getName(), "", dataset,
                                    PlotOrientation.VERTICAL, false, true, false);
                            ChartUtils.applyStyles(chart);
                            Marker meanMarker = new ValueMarker(mean.doubleValue(), WidgetUtils.BG_COLOR_BLUE_DARK,
                                    new BasicStroke(2f));
                            meanMarker.setLabel("Mean");
                            meanMarker.setLabelOffset(new RectangleInsets(70d, 25d, 0d, 0d));
                            meanMarker.setLabelFont(WidgetUtils.FONT_SMALL);
                            chart.getXYPlot().addDomainMarker(meanMarker);

                            final ChartPanel chartPanel = new ChartPanel(chart);
                            displayChartCallback.displayChart(chartPanel);
                        }
                    };

                    DCPanel panel = AbstractCrosstabResultSwingRenderer.createActionableValuePanel(standardDeviation,
                            Alignment.RIGHT, action, "images/chart-types/line.png");
                    table.setValueAt(panel, rowNumber, columnNumber);
                }
            }

            columnNumber++;
        }

        super.decorate(result, table, displayChartCallback);
    }

    /**
     * A main method that will display the results of a few example number
     * analyzers. Useful for tweaking the charts and UI.
     * 
     * @param args
     */
    public static void main(String[] args) throws Exception {
        LookAndFeelManager.get().init();

        Injector injector = Guice.createInjector(new DCModule(VFSUtils.getFileSystemManager().resolveFile("."), null));

        // run a small job
        final AnalysisJobBuilder ajb = injector.getInstance(AnalysisJobBuilder.class);
        Datastore ds = injector.getInstance(DatastoreCatalog.class).getDatastore("orderdb");
        DatastoreConnection con = ds.openConnection();
        Table table = con.getSchemaNavigator().convertToTable("PUBLIC.CUSTOMERS");
        ajb.setDatastore(ds);
        ajb.addSourceColumns(table.getNumberColumns());
        ajb.addAnalyzer(NumberAnalyzer.class).addInputColumns(ajb.getSourceColumns());

        ResultWindow resultWindow = injector.getInstance(ResultWindow.class);
        resultWindow.setVisible(true);
        resultWindow.startAnalysis();
    }
}
