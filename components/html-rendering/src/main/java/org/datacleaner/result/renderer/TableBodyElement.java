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
package org.datacleaner.result.renderer;

import javax.swing.table.TableModel;

import org.apache.commons.lang.ArrayUtils;
import org.datacleaner.result.html.BodyElement;
import org.datacleaner.result.html.HtmlRenderingContext;
import org.datacleaner.util.LabelUtils;

/**
 * Body element that renders a HTML table based on a {@link TableModel}.
 */
public class TableBodyElement implements BodyElement {

    private final TableModel _tableModel;
    private final String _tableClassName;
    private final int[] _highlightedColumns;

    /**
     * Constructs a table body element.
     * 
     * @param tableModel
     *            the table model to render
     * @param tableClassName
     *            a CSS class name to to set to the table
     * @param highlightedColumns
     *            an optional array of column indexes that should be highlighted
     */
    public TableBodyElement(TableModel tableModel, String tableClassName, int[] highlightedColumns) {
        _tableModel = tableModel;
        _tableClassName = tableClassName;
        _highlightedColumns = highlightedColumns;
    }

    public TableModel getTableModel() {
        return _tableModel;
    }

    public int[] getHighlightedColumns() {
        return _highlightedColumns;
    }

    public String getTableClassName() {
        return _tableClassName;
    }

    @Override
    public String toHtml(HtmlRenderingContext context) {
        final int columnCount = _tableModel.getColumnCount();

        final StringBuilder sb = new StringBuilder();

        if (_tableClassName == null) {
            sb.append("<table>");
        } else {
            sb.append("<table class=\"" + getTableClassName() + "\">");
        }

        int rowNumber = 0;
        rowNumber++;

        sb.append("<tr class=\"" + (rowNumber % 2 == 0 ? "even" : "odd") + "\">");
        for (int col = 0; col < columnCount; col++) {
            String columnName = _tableModel.getColumnName(col);
            sb.append("<th>");
            sb.append(getHeaderValue(context, col, columnName));
            sb.append("</th>");
        }
        sb.append("</tr>");

        final int rowCount = _tableModel.getRowCount();
        for (int row = 0; row < rowCount; row++) {
            rowNumber++;
            sb.append("<tr class=\"" + (rowNumber % 2 == 0 ? "even" : "odd") + "\">");
            for (int col = 0; col < columnCount; col++) {
                final Object value = _tableModel.getValueAt(row, col);
                final String cellClass = getCellClass(context, row, col);
                if (cellClass == null) {
                    sb.append("<td>");
                } else {
                    sb.append("<td class=\"" + cellClass + "\">");
                }
                sb.append(getCellValue(context, row, col, value));
                sb.append("</td>");
            }
            sb.append("</tr>");
        }

        sb.append("</table>");

        return sb.toString();
    }

    /**
     * Overrideable method for defining the literal HTML table header of a
     * particular column.
     * 
     * @param context
     * @param col
     * @param columnName
     * @return
     */
    protected String getHeaderValue(HtmlRenderingContext context, int col, String columnName) {
        return context.escapeHtml(columnName);
    }

    /**
     * Overrideable method for setting the class of a cell (the <td>element) in
     * the table
     * 
     * @param context
     * @param row
     * @param col
     * @return
     */
    protected String getCellClass(HtmlRenderingContext context, int row, int col) {
        if (ArrayUtils.indexOf(_highlightedColumns, col) == -1) {
            return null;
        }
        return "highlighted";
    }

    /**
     * Overrideable method for defining a cell's literal HTML value in the table
     * 
     * @param context
     * @param row
     * @param col
     * @param value
     * @return
     */
    protected String getCellValue(HtmlRenderingContext context, int row, int col, Object value) {
        String stringValue = LabelUtils.getValueLabel(value);
        String result = context.escapeHtml(stringValue);
        return result;
    }
}
