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

import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;

import org.apache.metamodel.util.HasName;
import org.datacleaner.util.StringUtils;

/**
 * Defines a group combo-box class that has a more convenient listening mechanism.
 */
public class DCGroupComboBox extends DCComboBox<HasName> {
    private static final long serialVersionUID = 1L;
    private static final String GROUP_ITEM_INDENTATION = "  ";
    private boolean _groupExists = false;

    private static class Delimiter implements HasName {
        private final String _text;

        private Delimiter(String text) {
            _text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

        @Override
        public String getName() {
            return _text;
        }
    }

    public DCGroupComboBox() {
        super(new GroupComboBoxModel());
        setRenderer(new GroupComboBoxRenderer());
    }

    public void addDelimiter(String text) {
        _groupExists = true;
        addItem(new Delimiter(text));
    }

    private static class GroupComboBoxModel extends DefaultComboBoxModel<HasName> {
        private static final long serialVersionUID = 1L;

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

    private class GroupComboBoxRenderer extends EnumComboBoxListRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public JLabel getListCellRendererComponent(final JList<?> list, final Object value, final int index,
                final boolean isSelected, final boolean cellHasFocus) {
            final JLabel label;

            if (value == null) {
                label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setText("- none -");
            } else if (value instanceof Delimiter) {
                label = super.getListCellRendererComponent(list, value, index, false, cellHasFocus);
                final Font font = label.getFont();
                label.setFont(font.deriveFont(font.getStyle() | Font.BOLD | Font.ITALIC));
            } else if (value instanceof HasName) {
                final String name = _groupExists ? new String(GROUP_ITEM_INDENTATION + value.toString())
                        : new String(value.toString());
                label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (!StringUtils.isNullOrEmpty(name)) {
                    label.setText(name);
                }
            } else {
                label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }

            return label;
        }
    }
}
