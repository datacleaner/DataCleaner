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

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicComboBoxUI;

import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;

/**
 * Custom {@link ComboBoxUI} for DataCleaner
 */
public class DCComboBoxUI extends BasicComboBoxUI {

    /**
     * Factory method used by Swing to instantiate the {@link DCComboBoxUI}
     * 
     * @param c
     *            the component
     * @return
     */
    public static ComponentUI createUI(JComponent c) {
        return new DCComboBoxUI();
    }

    @Override
    protected JButton createArrowButton() {
        final JButton arrowButton = WidgetFactory.createSmallButton(IconUtils.ACTION_SCROLLDOWN_DARK);
        return arrowButton;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected ListCellRenderer createRenderer() {
        return new DCListCellRenderer();
    }
}
