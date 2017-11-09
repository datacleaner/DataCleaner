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
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
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
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.result.AbstractCrosstabResultSwingRenderer;
import org.datacleaner.widgets.table.DCTable;
import org.datacleaner.widgets.table.DCTableCellRenderer;
import org.datacleaner.windows.DetailsResultWindow;

public class FillPatternGroupListPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public FillPatternGroupListPanel(WindowContext windowContext, RendererFactory rendererFactory,
            FillPatternResult result, FillPatternGroup group) {

        final String[] headers = new String[2];

        headers[0] = LabelUtils.COUNT_LABEL;
        headers[1] = "Filled columns";

        final TableModel tableModel = new DefaultTableModel(headers, group.getPatternCount());
        int row = 0;
        for (FillPattern fillPattern : group) {
            final int observationCount = fillPattern.getObservationCount();
            final Object observationCountValue =
                    createObservationCountValue(windowContext, rendererFactory, result, fillPattern, observationCount);
            tableModel.setValueAt(observationCountValue, row, 0);

            final List<InputColumn<?>> inspectedColumns = result.getInspectedColumns();

            final DCPanel columnListPanel = new DCPanel();
            columnListPanel.setLayout(new FlowLayout(Alignment.LEFT.getFlowLayoutAlignment(), 10, 2));
            final List<Object> fillOutcomes = fillPattern.getFillOutcomes();
            for (int i = 0; i < fillOutcomes.size(); i++) {
                final Object fillOutcome = fillOutcomes.get(i);
                if (fillOutcome instanceof String) {
                    switch ((String) fillOutcome) {
                    case LabelUtils.NULL_LABEL:
                    case LabelUtils.BLANK_LABEL:
                        // skip this - not listed
                        continue;
                    default:
                        break;
                    }
                }
                columnListPanel.add(createColumnLabel(inspectedColumns.get(i)));
            }

            tableModel.setValueAt(columnListPanel, row, 1);
            row++;
        }

        final DCTable table = new DCTable(tableModel);
        table.setColumnControlVisible(false);

        // right align the first column all
        table.getDCTableCellRenderer().setAlignment(0, Alignment.RIGHT);

        setLayout(new BorderLayout());
        add(table.toPanel(), BorderLayout.CENTER);
    }

    private JComponent createColumnLabel(InputColumn<?> inputColumn) {
        final JLabel label = new JLabel(inputColumn.getName(),
                ImageManager.get().getImageIcon(IconUtils.MODEL_COLUMN, IconUtils.ICON_SIZE_SMALL),
                Alignment.LEFT.getLabelAlignment());
        return label;
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
