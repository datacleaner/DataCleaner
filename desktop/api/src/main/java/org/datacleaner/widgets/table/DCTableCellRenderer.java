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

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Reader;
import java.sql.Clob;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.lang.ArrayUtils;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DCTableCellRenderer implements TableCellRenderer {

    private static final Logger logger = LoggerFactory.getLogger(DCTableCellRenderer.class);

    private final DCTable _table;
    private final Map<Integer, Alignment> _alignmentOverrides;
    private final DefaultTableCellRenderer _delegate;

    public DCTableCellRenderer(final DCTable table) {
        super();
        _table = table;
        _alignmentOverrides = new HashMap<>();
        _delegate = new DefaultTableCellRenderer();
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, Object value, final boolean isSelected,
            final boolean hasFocus, final int row, final int column) {
        logger.debug("getTableCellRendererComponent({},{})", row, column);

        if (value != null && value.getClass().isArray()) {
            // arrays are printed nicely this way
            value = ArrayUtils.toString(value);
        }

        // icons are displayed as labels
        if (value instanceof Icon) {
            final JLabel label = new JLabel((Icon) value);
            label.setOpaque(true);
            value = label;
        }

        if (value instanceof Clob) {
            final Clob clob = (Clob) value;
            try {
                final Reader reader = clob.getCharacterStream();
                value = FileHelper.readAsString(reader);
            } catch (final Exception e) {
                logger.error("Failed to read String from CLOB: {}", clob, e);
            }
        }

        final Component result;

        // render components directly
        if (value instanceof JComponent) {
            final JComponent component = (JComponent) value;

            component.setOpaque(true);

            if (component.getMouseListeners().length == 0) {
                component.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(final MouseEvent e) {
                        final MouseEvent newEvent = SwingUtilities.convertMouseEvent(component, e, _table);
                        _table.consumeMouseClick(newEvent);
                    }
                });
            }

            result = component;
        } else {
            result = _delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            assert result instanceof JLabel;
        }

        // alignment is applied to all labels or panels (with flowlayout)
        Alignment alignment = _alignmentOverrides.get(column);
        if (alignment == null) {
            alignment = Alignment.LEFT;
        }

        // set alignment
        if (value instanceof JPanel) {
            final LayoutManager layout = ((JPanel) value).getLayout();
            if (layout instanceof FlowLayout) {
                final FlowLayout flowLayout = (FlowLayout) layout;
                flowLayout.setAlignment(alignment.getFlowLayoutAlignment());
            }
        } else if (result instanceof JLabel) {
            final JLabel label = (JLabel) result;
            label.setHorizontalAlignment(alignment.getSwingContstantsAlignment());

            WidgetUtils.setAppropriateFont(label);
        }

        return result;
    }

    public void setAlignment(final int column, final Alignment alignment) {
        _alignmentOverrides.put(column, alignment);
    }

}
