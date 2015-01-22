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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.result.ValueCountingAnalyzerResult;
import org.datacleaner.result.ValueFrequency;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.ChartUtils;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.table.DCTable;
import org.datacleaner.windows.DetailsResultWindow;
import org.jdesktop.swingx.VerticalLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.ShortTextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegate for {@link ValueDistributionResultSwingRenderer}, which renders a
 * single Value Distribution group result.
 */
final class ValueDistributionResultSwingRendererGroupDelegate {

    private static final Logger logger = LoggerFactory
            .getLogger(ValueDistributionResultSwingRendererGroupDelegate.class);

    private static final Color[] SLICE_COLORS = DCDrawingSupplier.DEFAULT_FILL_COLORS;
    private static final int DEFAULT_PREFERRED_SLICES = 32;

    private final Map<String, Color> _valueColorMap;
    private final DefaultCategoryDataset _dataset = new DefaultCategoryDataset();
    private final JButton _backButton = WidgetFactory.createDefaultButton("Back", IconUtils.ACTION_BACK);
    private final int _preferredSlices;
    private final String _groupOrColumnName;
    private final DCTable _table;
    private final RendererFactory _rendererFactory;
    private final WindowContext _windowContext;

    private Collection<ValueFrequency> _valueCounts;

    /**
     * Default constructor
     */
    public ValueDistributionResultSwingRendererGroupDelegate(String groupOrColumnName, RendererFactory rendererFactory,
            WindowContext windowContext) {
        this(groupOrColumnName, DEFAULT_PREFERRED_SLICES, rendererFactory, windowContext);
    }

    /**
     * Alternative constructor (primarily used for testing) with customizable
     * slice count
     * 
     * @param groupOrColumnName
     * @param preferredSlices
     * @param rendererFactory
     * @param windowContext
     */
    public ValueDistributionResultSwingRendererGroupDelegate(String groupOrColumnName, int preferredSlices,
            RendererFactory rendererFactory, WindowContext windowContext) {
        _groupOrColumnName = groupOrColumnName;
        _preferredSlices = preferredSlices;
        _rendererFactory = rendererFactory;
        _windowContext = windowContext;
        _table = new DCTable("Value", LabelUtils.COUNT_LABEL);
        _table.setRowHeight(22);

        // create a map of predefined color mappings
        _valueColorMap = new HashMap<String, Color>();
        _valueColorMap.put(LabelUtils.BLANK_LABEL.toUpperCase(), WidgetUtils.BG_COLOR_BRIGHTEST);
        _valueColorMap.put(LabelUtils.UNIQUE_LABEL.toUpperCase(), WidgetUtils.BG_COLOR_BRIGHT);
        _valueColorMap.put(LabelUtils.NULL_LABEL.toUpperCase(), WidgetUtils.BG_COLOR_DARKEST);
        _valueColorMap.put(LabelUtils.UNEXPECTED_LABEL.toUpperCase(), WidgetUtils.BG_COLOR_LESS_DARK);
        _valueColorMap.put("RED", WidgetUtils.ADDITIONAL_COLOR_RED_BRIGHT);
        _valueColorMap.put("ORANGE", WidgetUtils.BG_COLOR_ORANGE_BRIGHT);
        _valueColorMap.put("GREEN", WidgetUtils.ADDITIONAL_COLOR_GREEN_BRIGHT);
        _valueColorMap.put("PURPLE", WidgetUtils.ADDITIONAL_COLOR_PURPLE_BRIGHT);
        _valueColorMap.put("CYAN", WidgetUtils.ADDITIONAL_COLOR_CYAN_BRIGHT);
        _valueColorMap.put("BLUE", WidgetUtils.BG_COLOR_BLUE_BRIGHT);
        _valueColorMap.put("NOT_PROCESSED", WidgetUtils.BG_COLOR_LESS_DARK);
        _valueColorMap.put("FAILURE", WidgetUtils.BG_COLOR_DARKEST);
    }

    public JSplitPane renderGroupResult(final ValueCountingAnalyzerResult result) {
        final Integer distinctCount = result.getDistinctCount();
        final Integer unexpectedValueCount = result.getUnexpectedValueCount();
        final int totalCount = result.getTotalCount();

        _valueCounts = result.getReducedValueFrequencies(_preferredSlices);
        for (ValueFrequency valueCount : _valueCounts) {
            setDataSetValue(valueCount.getName(), valueCount.getCount());
        }

        logger.info("Rendering with {} slices", getDataSetItemCount());
        drillToOverview(result);

        // chart for display of the dataset
        String title = "Value distribution of " + _groupOrColumnName;
        final JFreeChart chart = ChartFactory.createBarChart(title, "Value", "Count", _dataset,
                PlotOrientation.HORIZONTAL, true, true, false);

        List<Title> titles = new ArrayList<Title>();
        titles.add(new ShortTextTitle("Total count: " + totalCount));
        if (distinctCount != null) {
            titles.add(new ShortTextTitle("Distinct count: " + distinctCount));
        }
        if (unexpectedValueCount != null) {
            titles.add(new ShortTextTitle("Unexpected value count: " + unexpectedValueCount));
        }
        chart.setSubtitles(titles);

        ChartUtils.applyStyles(chart);

        // code-block for tweaking style and coloring of chart
        {
            final CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.getDomainAxis().setVisible(false);

            int colorIndex = 0;
            for (int i = 0; i < getDataSetItemCount(); i++) {
                final String key = getDataSetKey(i);
                final Color color;
                final String upperCaseKey = key.toUpperCase();
                if (_valueColorMap.containsKey(upperCaseKey)) {
                    color = _valueColorMap.get(upperCaseKey);
                } else {
                    if (i == getDataSetItemCount() - 1) {
                        // the last color should not be the same as the first
                        if (colorIndex == 0) {
                            colorIndex++;
                        }
                    }

                    Color colorCandidate = SLICE_COLORS[colorIndex];
                    int darkAmount = i / SLICE_COLORS.length;
                    for (int j = 0; j < darkAmount; j++) {
                        colorCandidate = WidgetUtils.slightlyDarker(colorCandidate);
                    }
                    color = colorCandidate;

                    colorIndex++;
                    if (colorIndex >= SLICE_COLORS.length) {
                        colorIndex = 0;
                    }
                }

                plot.getRenderer().setSeriesPaint(i, color);
            }
        }

        final ChartPanel chartPanel = new ChartPanel(chart);

        // chartPanel.addChartMouseListener(new ChartMouseListener() {
        //
        // @Override
        // public void chartMouseMoved(ChartMouseEvent event) {
        // ChartEntity entity = event.getEntity();
        // if (entity instanceof PieSectionEntity) {
        // PieSectionEntity pieSectionEntity = (PieSectionEntity) entity;
        // String sectionKey = (String) pieSectionEntity.getSectionKey();
        // if (_groups.containsKey(sectionKey)) {
        // chartPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // } else {
        // chartPanel.setCursor(Cursor.getDefaultCursor());
        // }
        // } else {
        // chartPanel.setCursor(Cursor.getDefaultCursor());
        // }
        // }
        //
        // @Override
        // public void chartMouseClicked(ChartMouseEvent event) {
        // ChartEntity entity = event.getEntity();
        // if (entity instanceof PieSectionEntity) {
        // PieSectionEntity pieSectionEntity = (PieSectionEntity) entity;
        // String sectionKey = (String) pieSectionEntity.getSectionKey();
        // if (_groups.containsKey(sectionKey)) {
        // drillToGroup(result, sectionKey, true);
        // }
        // }
        // }
        // });

        final DCPanel leftPanel = new DCPanel();
        leftPanel.setLayout(new VerticalLayout());
        leftPanel.add(WidgetUtils.decorateWithShadow(chartPanel, true, 4));

        _backButton.setMargin(new Insets(0, 0, 0, 0));
        _backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drillToOverview(result);
            }
        });

        final DCPanel rightPanel = new DCPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(_backButton, BorderLayout.NORTH);
        rightPanel.add(_table.toPanel(), BorderLayout.CENTER);

        final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setOpaque(false);
        split.add(leftPanel);
        split.add(rightPanel);
        split.setDividerLocation(550);

        return split;
    }

    public CategoryDataset getDataset() {
        return _dataset;
    }

    private void drillToOverview(final ValueCountingAnalyzerResult result) {
        final TableModel model = new DefaultTableModel(new String[] { "Value", LabelUtils.COUNT_LABEL },
                _valueCounts.size());

        int i = 0;
        for (final ValueFrequency valueFreq : _valueCounts) {
            final String key = valueFreq.getName();
            final int count = valueFreq.getCount();
            model.setValueAt(key, i, 0);

            if (valueFreq.isComposite() && valueFreq.getChildren() != null && !valueFreq.getChildren().isEmpty()) {
                DCPanel panel = new DCPanel();
                panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

                JLabel label = new JLabel(count + "");
                JButton button = WidgetFactory.createSmallButton("images/actions/drill-to-detail.png");
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        drillToGroup(result, valueFreq, true);
                    }
                });

                panel.add(label);
                panel.add(Box.createHorizontalStrut(4));
                panel.add(button);

                model.setValueAt(panel, i, 1);
            } else {
                setCountValue(result, model, i, valueFreq);
            }
            i++;
        }
        _table.setModel(model);
        _backButton.setVisible(false);
    }

    protected void setDataSetValue(String label, Integer value) {
        _dataset.setValue(value, label, "");
    }

    protected int getDataSetValue(int i) {
        Number value = _dataset.getValue(i, 0);
        return value.intValue();
    }

    public int getDataSetValue(String label) {
        Number value = _dataset.getValue(label, "");
        return value.intValue();
    }

    protected String getDataSetKey(int i) {
        Comparable<?> key = _dataset.getRowKey(i);
        assert key instanceof String;
        return key.toString();
    }

    protected int getDataSetItemCount() {
        return _dataset.getRowCount();
    }

    private void setCountValue(final ValueCountingAnalyzerResult result, final TableModel model, int i,
            final ValueFrequency vc) {
        final String value = vc.getName();
        final int count = vc.getCount();
        final boolean hasAnnotation;
        final boolean isNullValue = value == null || LabelUtils.NULL_LABEL.equals(value);
        final boolean isUnexpectedValues = LabelUtils.UNEXPECTED_LABEL.equals(value);
        final boolean isBlank = "".equals(value) || LabelUtils.BLANK_LABEL.equals(value);
        if (count == 0) {
            hasAnnotation = false;
        } else {
            if (isNullValue) {
                hasAnnotation = result.hasAnnotatedRows(null);
            } else if (isUnexpectedValues) {
                hasAnnotation = result.hasAnnotatedRows(null);
            } else if (isBlank) {
                hasAnnotation = result.hasAnnotatedRows("");
            } else {
                hasAnnotation = result.hasAnnotatedRows(value);
            }
        }

        if (hasAnnotation) {
            ActionListener action = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent action) {
                    String title = "Detailed results for [" + value + "]";
                    List<AnalyzerResult> results = new ArrayList<AnalyzerResult>();
                    final AnnotatedRowsResult annotatedRows;
                    if (isNullValue) {
                        annotatedRows = result.getAnnotatedRowsForNull();
                    } else if (isUnexpectedValues) {
                        annotatedRows = result.getAnnotatedRowsForUnexpectedValues();
                    } else if (isBlank) {
                        annotatedRows = result.getAnnotatedRowsForValue("");
                    } else {
                        annotatedRows = result.getAnnotatedRowsForValue(value);
                    }
                    results.add(annotatedRows);
                    DetailsResultWindow window = new DetailsResultWindow(title, results, _windowContext,
                            _rendererFactory);
                    window.setVisible(true);
                }
            };

            DCPanel panel = AbstractCrosstabResultSwingRenderer.createActionableValuePanel(count, Alignment.LEFT,
                    action, AbstractCrosstabResultSwingRenderer.IMAGE_PATH_DRILL_TO_DETAIL);

            model.setValueAt(panel, i, 1);
        } else {
            model.setValueAt(count, i, 1);
        }
    }

    private void drillToGroup(ValueCountingAnalyzerResult result, ValueFrequency valueFrequency, boolean showBackButton) {
        final List<ValueFrequency> children = valueFrequency.getChildren();
        final TableModel model = new DefaultTableModel(new String[] { valueFrequency.getName() + " value",
                LabelUtils.COUNT_LABEL }, children.size());

        final Iterator<ValueFrequency> valueCounts = children.iterator();
        int i = 0;
        while (valueCounts.hasNext()) {
            ValueFrequency vc = valueCounts.next();
            model.setValueAt(LabelUtils.getLabel(vc.getValue()), i, 0);
            setCountValue(result, model, i, vc);
            i++;
        }
        _table.setModel(model);
        _backButton.setVisible(showBackButton);
    }
}
