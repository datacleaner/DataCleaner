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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.datacleaner.api.RendererPrecedence;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.reference.SimpleStringPattern;
import org.datacleaner.result.CrosstabResult;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.user.MutableReferenceDataCatalog;
import org.datacleaner.util.ChartUtils;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.table.CrosstabPanel;
import org.datacleaner.widgets.table.DCTable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Unregistered renderer for crosstab results that are programmatically created
 * in {@link PatternFinderResultSwingRenderer}.
 * 
 * @author Kasper Sørensen
 */
class PatternFinderResultSwingRendererCrosstabDelegate extends AbstractCrosstabResultSwingRenderer<CrosstabResult> {

    private final MutableReferenceDataCatalog _catalog;

    public PatternFinderResultSwingRendererCrosstabDelegate(WindowContext windowContext,
            RendererFactory rendererFactory, MutableReferenceDataCatalog referenceDataCatalog) {
        super(windowContext, rendererFactory);
        _catalog = referenceDataCatalog;
    }

    @Override
    public RendererPrecedence getPrecedence(CrosstabResult renderable) {
        throw new UnsupportedOperationException(
                "This renderer is programmatically invoked, don't register it in the descriptor catalog.");
    }

    @Override
    public JComponent render(CrosstabResult result) {
        final CrosstabPanel crosstabPanel = super.renderInternal(result);
        final DCTable table = crosstabPanel.getTable();
        if (isInitiallyCharted(table) || isTooLimitedToChart(table)) {
            return crosstabPanel;
        }

        final DCPanel headerPanel = new DCPanel();
        headerPanel.setLayout(new FlowLayout(Alignment.RIGHT.getFlowLayoutAlignment(), 1, 1));

        final JButton chartButton = WidgetFactory.createDefaultButton("Show distribution chart", IconUtils.CHART_BAR);
        chartButton.setMargin(new Insets(1, 1, 1, 1));
        chartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                headerPanel.setVisible(false);
                displayChart(table, crosstabPanel.getDisplayChartCallback());
            }
        });

        headerPanel.add(chartButton);

        final DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(crosstabPanel, BorderLayout.CENTER);

        return panel;
    }

    protected void displayChart(DCTable table, DisplayChartCallback displayChartCallback) {
        final int rowCount = table.getRowCount();
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < rowCount; i++) {
            final Object expressionObject = table.getValueAt(i, 0);
            final String expression = extractString(expressionObject);

            final Object countObject = table.getValueAt(i, 1);
            final String countString = extractString(countObject);
            final int count = Integer.parseInt(countString);
            dataset.addValue(count, expression, "");
        }

        // only show legend if there are not too many patterns
        final boolean showLegend = dataset.getRowCount() < 25;
        
        JFreeChart chart = ChartFactory.createBarChart("", "", "Match count", dataset, PlotOrientation.VERTICAL, showLegend,
                true, false);
        ChartUtils.applyStyles(chart);
        
        final ChartPanel chartPanel = ChartUtils.createPanel(chart, true);
        
        displayChartCallback.displayChart(chartPanel);
    }

    @Override
    protected void decorate(CrosstabResult result, DCTable table, DisplayChartCallback displayChartCallback) {
        super.decorate(result, table, displayChartCallback);

        table.setAlignment(1, Alignment.RIGHT);

        final int rowCount = table.getRowCount();

        for (int i = 0; i < rowCount; i++) {
            final Object expressionObject = table.getValueAt(i, 0);
            final String label = extractString(expressionObject);
            final String expression = extractExpression(label);

            final String stringPatternName = "PF: " + label;

            if (!_catalog.containsStringPattern(stringPatternName)) {
                DCPanel panel = new DCPanel();
                panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

                panel.add(Box.createHorizontalStrut(4));
                panel.add(new JLabel(label));

                final JButton button = WidgetFactory.createSmallButton(IconUtils.ACTION_SAVE_DARK);
                button.setToolTipText("Save as string pattern");
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        _catalog.addStringPattern(new SimpleStringPattern(stringPatternName, expression));
                        button.setEnabled(false);
                    }
                });
                panel.add(Box.createHorizontalStrut(4));
                panel.add(button);

                table.setValueAt(panel, i, 0);
            }
        }

        if (isInitiallyCharted(table)) {
            displayChart(table, displayChartCallback);
        }
    }

    private String extractExpression(String string) {
        if (LabelUtils.BLANK_LABEL.equals(string)) {
            return "";
        }
        return string;
    }

    private boolean isInitiallyCharted(DCTable table) {
        return table.getRowCount() >= 8;
    }

    private boolean isTooLimitedToChart(DCTable table) {
        return table.getRowCount() <= 1;
    }

    private String extractString(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof JPanel) {
            Component[] components = ((JPanel) obj).getComponents();
            for (Component component : components) {
                if (component instanceof JLabel) {
                    return extractString(component);
                }
            }
            return null;
        } else if (obj instanceof JLabel) {
            return ((JLabel) obj).getText();
        } else {
            return obj.toString();
        }
    }
}
