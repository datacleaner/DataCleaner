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
package org.datacleaner.widgets.table;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.table.JTableHeader;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.WidgetUtils;

/**
 * Defines a panel that wraps a {@link DCTable} (including headers, scrollbars
 * etc).
 */
public final class DCTablePanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private final JScrollPane _scrollPane;
    private final DCTable _table;

    public DCTablePanel(final DCTable table, final boolean scrolleable) {
        _table = table;
        setLayout(new BorderLayout());
        final JTableHeader tableHeader = table.getTableHeader();
        add(tableHeader, BorderLayout.NORTH);

        tableHeader.setBorder(WidgetUtils.BORDER_TABLE_PANEL);
        table.setBorder(WidgetUtils.BORDER_TABLE_PANEL);

        if (scrolleable) {
            _scrollPane = WidgetUtils.scrolleable(table);
            add(_scrollPane, BorderLayout.CENTER);
        } else {
            _scrollPane = null;
            add(table, BorderLayout.CENTER);
        }

    }

    @Override
    public Dimension getPreferredSize() {
        if (_scrollPane == null) {
            return super.getPreferredSize();
        }
        final Dimension tableSize = _table.getPreferredSize();
        final Dimension headerSize = _table.getTableHeader().getPreferredSize();

        final Dimension d = new Dimension();
        d.width = Math.max(tableSize.width, headerSize.width);
        d.height = headerSize.height + (_table.getRowHeight() * _table.getRowCount());

        final Insets insets = getInsets();
        d.height = d.height + insets.top + insets.bottom;

        final JScrollBar scrollBar = _scrollPane.getHorizontalScrollBar();
        final int scrollbarHeight = scrollBar.getHeight();

        d.height = d.height + scrollbarHeight;

        return d;
    }

    @Override
    public void setPreferredSize(final Dimension preferredSize) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isVisible() {
        return _table.isVisible();
    }

    @Override
    public void setVisible(final boolean visible) {
        _table.setVisible(visible);
    }
}
