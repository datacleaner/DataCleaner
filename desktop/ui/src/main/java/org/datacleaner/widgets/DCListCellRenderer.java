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

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.datacleaner.util.WidgetUtils;

/**
 * A {@link ListCellRenderer} for DataCleaner components
 */
public class DCListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		final Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		if (isSelected) {
			result.setForeground(WidgetUtils.BG_COLOR_BLUE_DARK);
			result.setBackground(WidgetUtils.BG_COLOR_BRIGHTEST);
		} else {
			result.setForeground(WidgetUtils.BG_COLOR_LESS_BRIGHT);
			result.setBackground(WidgetUtils.BG_COLOR_DARKEST);
		}

		return result;
	}
}
