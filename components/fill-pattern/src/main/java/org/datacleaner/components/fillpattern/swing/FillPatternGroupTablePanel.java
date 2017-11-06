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
package org.datacleaner.components.fillpattern.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.components.fillpattern.FillPattern;
import org.datacleaner.components.fillpattern.FillPatternAnalyzer;
import org.datacleaner.components.fillpattern.FillPatternGroup;
import org.datacleaner.components.fillpattern.FillPatternResult;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.result.AbstractCrosstabResultSwingRenderer;
import org.datacleaner.widgets.table.DCTable;
import org.datacleaner.widgets.table.DCTableCellRenderer;
import org.datacleaner.windows.DetailsResultWindow;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.Highlighter;

public class FillPatternGroupTablePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public FillPatternGroupTablePanel(WindowContext windowContext, RendererFactory rendererFactory,
            FillPatternResult result, FillPatternGroup group) {

        final List<InputColumn<?>> inspectedColumns = result.getInspectedColumns();
        final String[] headers = new String[1 + inspectedColumns.size()];

        headers[0] = LabelUtils.COUNT_LABEL;
        for (int i = 0; i < inspectedColumns.size(); i++) {
            headers[1 + i] = inspectedColumns.get(i).getName();
        }

        final TableModel tableModel = new DefaultTableModel(headers, group.getPatternCount());
        int row = 0;
        for (FillPattern fillPattern : group) {
            int column = 0;

            final int observationCount = fillPattern.getObservationCount();
            final Object observationCountValue =
                    createObservationCountValue(windowContext, rendererFactory, result, fillPattern, observationCount);
            tableModel.setValueAt(observationCountValue, row, column);
            column++;

            final List<Object> fillOutcomes = fillPattern.getFillOutcomes();
            for (Object fillOutcome : fillOutcomes) {
                tableModel.setValueAt(fillOutcome, row, column);
                column++;
            }
            row++;
        }

        final DCTable table = new DCTable(tableModel);
        table.setHighlighters(new InternalHighlighter());
        table.setColumnControlVisible(false);

        // right align them all
        for (int i = 0; i < headers.length; i++) {
            table.getDCTableCellRenderer().setAlignment(i, Alignment.RIGHT);
        }

        setLayout(new BorderLayout());
        add(table.toPanel(), BorderLayout.CENTER);
    }

    private class InternalHighlighter implements Highlighter {

        private ColorHighlighter _normalHighlighter;
        private ColorHighlighter _blankHighlighter;
        private ColorHighlighter _filledHighlighter;
        private ColorHighlighter _nullHighlighter;

        public InternalHighlighter() {
            _normalHighlighter = new ColorHighlighter(WidgetUtils.BG_COLOR_BRIGHTEST, WidgetUtils.BG_COLOR_DARKEST,
                    WidgetUtils.BG_COLOR_BRIGHTEST, WidgetUtils.BG_COLOR_DARKEST);
            _filledHighlighter = new ColorHighlighter(WidgetUtils.BG_COLOR_BRIGHTEST, WidgetUtils.BG_COLOR_BLUE_DARK,
                    WidgetUtils.BG_COLOR_BRIGHTEST, WidgetUtils.BG_COLOR_BLUE_DARK);
            _blankHighlighter =
                    new ColorHighlighter(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.ADDITIONAL_COLOR_RED_BRIGHT,
                            WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.ADDITIONAL_COLOR_RED_BRIGHT);
            _nullHighlighter =
                    new ColorHighlighter(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.ADDITIONAL_COLOR_PURPLE_BRIGHT,
                            WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.ADDITIONAL_COLOR_PURPLE_BRIGHT);
        }

        @Override
        public Component highlight(Component component, ComponentAdapter adapter) {
            if (adapter.getValue() instanceof String) {
                final String str = (String) adapter.getValue();
                switch (str) {
                case LabelUtils.NULL_LABEL:
                    return _nullHighlighter.highlight(component, adapter);
                case LabelUtils.BLANK_LABEL:
                    return _blankHighlighter.highlight(component, adapter);
                case FillPatternAnalyzer.FILLED_LABEL:
                    return _filledHighlighter.highlight(component, adapter);
                default:
                    return _normalHighlighter.highlight(component, adapter);
                }
            }
            return component;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

        @Override
        public ChangeListener[] getChangeListeners() {
            return new ChangeListener[0];
        }
    }

    protected DCTableCellRenderer createTableCellRenderer(DCTable table) {
        return new DCTableCellRenderer(table) {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                final JComponent component = (JComponent) super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);
                if (value instanceof String) {
                    final String str = (String) value;
                    switch (str) {
                    case LabelUtils.NULL_LABEL:
                        component.setBackground(WidgetUtils.BG_COLOR_DARK);
                        component.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
                        component.setOpaque(true);
                        break;
                    case LabelUtils.BLANK_LABEL:
                        component.setBackground(WidgetUtils.BG_COLOR_MEDIUM);
                        component.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
                        component.setOpaque(true);
                        break;
                    case FillPatternAnalyzer.FILLED_LABEL:
                        component.setBackground(WidgetUtils.BG_COLOR_BLUE_BRIGHT);
                        component.setForeground(WidgetUtils.BG_COLOR_DARKEST);
                        component.setOpaque(true);
                        break;
                    default:
                        break;
                    }
                }
                return component;
            }
        };
    }

    private Object createObservationCountValue(WindowContext windowContext, RendererFactory rendererFactory,
            FillPatternResult result, FillPattern fillPattern, int observationCount) {
        final RowAnnotationFactory rowAnnotationFactory = result.getRowAnnotationFactory();
        if (rowAnnotationFactory == null) {
            return observationCount;
        }

        ActionListener actionListener = (e) -> {
            final InputColumn<?>[] highlightedColumns = result.getInspectedColumns().toArray(new InputColumn[0]);
            final AnalyzerResult analyzerResult =
                    new AnnotatedRowsResult(fillPattern.getRowAnnotation(), rowAnnotationFactory, highlightedColumns);
            final String windowTitle = "Details for " + fillPattern.getFillOutcomes();
            final DetailsResultWindow window =
                    new DetailsResultWindow(windowTitle, Arrays.asList(analyzerResult), windowContext, rendererFactory);
            window.open();
        };
        final DCPanel observationCountPanel = AbstractCrosstabResultSwingRenderer.createActionableValuePanel(
                observationCount, Alignment.LEFT, actionListener, IconUtils.ACTION_DRILL_TO_DETAIL);
        return observationCountPanel;
    }
}
