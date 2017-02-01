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
package org.datacleaner.widgets;

import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import org.datacleaner.metadata.HasColumnMeaning;

/**
 * Defines a group combo-box class that has a more convenient listening mechanism.
 */
public class DCGroupComboBox extends DCComboBox {
    private static class Delimiter {
        private final String _text;

        private Delimiter(String text) {
            _text = text;
        }

        @Override
        public String toString() {
            return _text.toString();
        }
    }

    public DCGroupComboBox() {
        setModel(new ExtendedComboBoxModel());
        setRenderer(new ExtendedListCellRenderer());
    }

    public void addDelimiter(String text) {
        addItem(new Delimiter(text));
    }

    private static class ExtendedComboBoxModel extends DefaultComboBoxModel {
        @Override
        public void setSelectedItem(final Object item) {
            if (!(item instanceof Delimiter)) {
                super.setSelectedItem(item);
            } else {
                final int index = getIndexOf(item);

                if (index < getSize()) {
                    setSelectedItem(getElementAt(index + 1));
                }
            }
        }
    }

    private static class ExtendedListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(final JList list, final Object value, final int index,
                final boolean isSelected, final boolean cellHasFocus) {
            if (value instanceof Delimiter) {
                final JLabel label = new JLabel(value.toString());
                final Font font = label.getFont();
                label.setFont(font.deriveFont(font.getStyle() | Font.BOLD | Font.ITALIC));

                return label;
            } else {
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        }
    }
}
