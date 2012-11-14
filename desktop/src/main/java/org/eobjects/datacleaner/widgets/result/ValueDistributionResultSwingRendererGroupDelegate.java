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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.beans.valuedist.ValueCount;
import org.eobjects.analyzer.beans.valuedist.ValueDistributionGroupResult;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.util.LabelUtils;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.ChartUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.Alignment;
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
import org.jfree.chart.title.ShortTextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegate for {@link ValueDistributionResultSwingRenderer}, which renders a
 * single Value Distribution group result.
 * 
 * @author Kasper Sørensen
 */
final class ValueDistributionResultSwingRendererGroupDelegate {

    private static final Logger logger = LoggerFactory
            .getLogger(ValueDistributionResultSwingRendererGroupDelegate.class);

    private static final Color[] SLICE_COLORS = DCDrawingSupplier.DEFAULT_FILL_COLORS;

    private static final int DEFAULT_PREFERRED_SLICES = 32;
    private static final int DEFAULT_MAX_SLICES = 40;

    private final Map<String, Color> _valueColorMap;
    private final Map<String, PieSliceGroup> _groups = new HashMap<String, PieSliceGroup>();
    private final DefaultPieDataset _dataset = new DefaultPieDataset();
    private final JButton _backButton = WidgetFactory.createButton("Back", "images/actions/back.png");
    private final int _preferredSlices;
    private final int _maxSlices;
    private final String _groupOrColumnName;
    private final DCTable _table;
    private final RendererFactory _rendererFactory;
    private final WindowContext _windowContext;

    /**
     * Default constructor
     */
    public ValueDistributionResultSwingRendererGroupDelegate(String groupOrColumnName, RendererFactory rendererFactory,
            WindowContext windowContext) {
        this(groupOrColumnName, DEFAULT_PREFERRED_SLICES, DEFAULT_MAX_SLICES, rendererFactory, windowContext);
    }

    /**
     * Alternative constructor (primarily used for testing) with customizable
     * slice count
     * 
     * @param preferredSlices
     * @param maxSlices
     * @param windowContext
     */
    public ValueDistributionResultSwingRendererGroupDelegate(String groupOrColumnName, int preferredSlices,
            int maxSlices, RendererFactory rendererFactory, WindowContext windowContext) {
        _groupOrColumnName = groupOrColumnName;
        _preferredSlices = preferredSlices;
        _maxSlices = maxSlices;
        _rendererFactory = rendererFactory;
        _windowContext = windowContext;
        _table = new DCTable("Value", LabelUtils.COUNT_LABEL);
        _table.setRowHeight(22);

        // create a map of predefined color mappings
        _valueColorMap = new HashMap<String, Color>();
        _valueColorMap.put(LabelUtils.BLANK_LABEL.toUpperCase(), WidgetUtils.BG_COLOR_BRIGHTEST);
        _valueColorMap.put(LabelUtils.UNIQUE_LABEL.toUpperCase(), WidgetUtils.BG_COLOR_BRIGHT);
        _valueColorMap.put(LabelUtils.NULL_LABEL.toUpperCase(), WidgetUtils.BG_COLOR_DARKEST);
        _valueColorMap.put("RED", WidgetUtils.ADDITIONAL_COLOR_RED_BRIGHT);
        _valueColorMap.put("ORANGE", WidgetUtils.BG_COLOR_ORANGE_BRIGHT);
        _valueColorMap.put("GREEN", WidgetUtils.ADDITIONAL_COLOR_GREEN_BRIGHT);
        _valueColorMap.put("PURPLE", WidgetUtils.ADDITIONAL_COLOR_PURPLE_BRIGHT);
        _valueColorMap.put("CYAN", WidgetUtils.ADDITIONAL_COLOR_CYAN_BRIGHT);
        _valueColorMap.put("BLUE", WidgetUtils.BG_COLOR_BLUE_BRIGHT);
        _valueColorMap.put("NOT_PROCESSED", WidgetUtils.BG_COLOR_LESS_DARK);
    }

    public JSplitPane renderGroupResult(final ValueDistributionGroupResult result) {
        // create a special group for the unique values
        final int uniqueCount = result.getUniqueCount();
        final int distinctCount = result.getDistinctCount();
        final int totalCount = result.getTotalCount();

        final Collection<String> uniqueValues = result.getUniqueValues();
        if (uniqueValues != null && !uniqueValues.isEmpty()) {
            PieSliceGroup pieSliceGroup = new PieSliceGroup(LabelUtils.UNIQUE_LABEL, uniqueCount, uniqueValues, 1);
            _groups.put(pieSliceGroup.getName(), pieSliceGroup);
        } else {
            if (uniqueCount > 0) {
                _dataset.setValue(LabelUtils.UNIQUE_LABEL, uniqueCount);
            }
        }

        // create a special slice for null values
        final int nullCount = result.getNullCount();
        if (nullCount > 0) {
            _dataset.setValue(LabelUtils.NULL_LABEL, nullCount);
        }

        // create the remaining "normal" slices, either individually or in
        // groups
        {
            final List<ValueCount> topValueCounts = result.getTopValues().getValueCounts();
            final List<ValueCount> bottomValueCounts = result.getBottomValues().getValueCounts();

            // if the user has specified the values of interest then show the
            // graph correspondingly without any grouping
            boolean userSpecifiedGroups = !topValueCounts.isEmpty() && !bottomValueCounts.isEmpty();

            if (userSpecifiedGroups || topValueCounts.size() + bottomValueCounts.size() < _preferredSlices) {
                // vanilla scenario for cleanly distributed datasets
                for (ValueCount valueCount : topValueCounts) {
                    _dataset.setValue(LabelUtils.getLabel(valueCount.getValue()), valueCount.getCount());
                }
                for (ValueCount valueCount : bottomValueCounts) {
                    _dataset.setValue(LabelUtils.getLabel(valueCount.getValue()), valueCount.getCount());
                }
            } else {
                // create groups of values

                List<ValueCount> valueCounts = topValueCounts;
                if (!bottomValueCounts.isEmpty()) {
                    valueCounts = bottomValueCounts;
                }

                createGroups(valueCounts);

                for (ValueCount valueCount : valueCounts) {
                    _dataset.setValue(LabelUtils.getLabel(valueCount.getValue()), valueCount.getCount());
                }
            }

            // if the is only a single group and it's size (plus the existing
            // size) is smaller than
            // _maxSlices, then "drill to detail" from the start.
            boolean singleGroupExploded = false;
            if (_groups.size() == 1) {
                // only a single group in the complete value distribution!
                PieSliceGroup singleGroup = _groups.values().iterator().next();
                if (singleGroup.size() + _dataset.getItemCount() <= _preferredSlices) {
                    singleGroupExploded = true;
                    for (ValueCount vc : singleGroup) {
                        _dataset.setValue(LabelUtils.getLabel(vc.getValue()), vc.getCount());
                    }
                    _dataset.sortByValues(SortOrder.DESCENDING);
                }
            }

            if (!singleGroupExploded) {
                for (PieSliceGroup group : _groups.values()) {
                    _dataset.setValue(group.getName(), group.getTotalCount());
                }
            }
        }

        logger.info("Rendering with {} slices", _dataset.getItemCount());
        drillToOverview(result);

        // chart for display of the dataset
        final JFreeChart chart = ChartFactory.createPieChart("Value distribution of " + _groupOrColumnName, _dataset,
                false, true, false);
        Title totalCountSubtitle = new ShortTextTitle("Total count: " + totalCount);
        Title distinctCountSubtitle = new ShortTextTitle("Distinct count: " + distinctCount);
        chart.setSubtitles(Arrays.asList(totalCountSubtitle, distinctCountSubtitle));

        ChartUtils.applyStyles(chart);

        // code-block for tweaking style and coloring of chart
        {
            final PiePlot plot = (PiePlot) chart.getPlot();

            int colorIndex = 0;
            for (int i = 0; i < _dataset.getItemCount(); i++) {
                final String key = (String) _dataset.getKey(i);
                final Color color;
                final String upperCaseKey = key.toUpperCase();
                if (_valueColorMap.containsKey(upperCaseKey)) {
                    color = _valueColorMap.get(upperCaseKey);
                } else {
                    if (i == _dataset.getItemCount() - 1) {
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
                plot.setSectionPaint(key, color);
            }
            plot.setSectionPaint(LabelUtils.UNIQUE_LABEL, WidgetUtils.BG_COLOR_BRIGHT);
            plot.setSectionPaint(LabelUtils.NULL_LABEL, WidgetUtils.BG_COLOR_DARKEST);
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
                        drillToGroup(result, sectionKey, true);
                    }
                }
            }
        });

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
        rightPanel.getSize().height = chartHeight;

        final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setOpaque(false);
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

    private void drillToOverview(final ValueDistributionGroupResult result) {
        final TableModel model = new DefaultTableModel(new String[] { "Value", LabelUtils.COUNT_LABEL },
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
                        drillToGroup(result, key, true);
                    }
                });

                panel.add(label);
                panel.add(Box.createHorizontalStrut(4));
                panel.add(button);

                model.setValueAt(panel, i, 1);
            } else {
                setCountValue(result, model, i, new ValueCount(key, count));
            }
        }
        _table.setModel(model);
        _backButton.setVisible(false);
    }

    private void setCountValue(final ValueDistributionGroupResult result, final TableModel model, int i,
            final ValueCount vc) {
        final String value = vc.getValue();
        final boolean hasAnnotation;
        final boolean isNullValue = value == null || LabelUtils.NULL_LABEL.equals(value);
        final boolean isBlank = "".equals(value) || LabelUtils.BLANK_LABEL.equals(value);
        if (isNullValue) {
            hasAnnotation = result.isAnnotationsEnabled();
        } else if (isBlank) {
            hasAnnotation = result.isAnnotationsEnabled() && result.hasAnnotation("");
        } else {
            hasAnnotation = result.isAnnotationsEnabled() && result.hasAnnotation(value);
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
                    } else if (isBlank) {
                        annotatedRows = result.getAnnotatedRows("");
                    } else {
                        annotatedRows = result.getAnnotatedRows(value);
                    }
                    results.add(annotatedRows);
                    DetailsResultWindow window = new DetailsResultWindow(title, results, _windowContext,
                            _rendererFactory);
                    window.setVisible(true);
                }
            };

            DCPanel panel = AbstractCrosstabResultSwingRenderer.createActionableValuePanel(vc.getCount(),
                    Alignment.LEFT, action, AbstractCrosstabResultSwingRenderer.IMAGE_PATH_DRILL_TO_DETAIL);

            model.setValueAt(panel, i, 1);
        } else {
            model.setValueAt(vc.getCount(), i, 1);
        }
    }

    private void drillToGroup(ValueDistributionGroupResult result, String groupName, boolean showBackButton) {
        final PieSliceGroup group = _groups.get(groupName);
        final TableModel model = new DefaultTableModel(new String[] { groupName + " value", LabelUtils.COUNT_LABEL },
                group.size());

        final Iterator<ValueCount> valueCounts = group.getValueCounts();
        int i = 0;
        while (valueCounts.hasNext()) {
            ValueCount vc = valueCounts.next();
            model.setValueAt(LabelUtils.getLabel(vc.getValue()), i, 0);
            setCountValue(result, model, i, vc);
            i++;
        }
        _table.setModel(model);
        _backButton.setVisible(showBackButton);
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

            logger.debug("Lowest repeated frequency above {} found: {}. Fetching from {} ungrouped values",
                    new Object[] { previousGroupFrequency, groupFrequency, valueCounts.size() });

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
}
