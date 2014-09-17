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
package org.eobjects.datacleaner.widgets;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.eobjects.analyzer.util.StringUtils;
import org.apache.metamodel.util.HasName;

/**
 * {@link ListCellRenderer} for enums in a combobox.
 */
public class EnumComboBoxListRenderer extends DCListCellRenderer {

	private static final long serialVersionUID = 1L;

	private final Icon _icon;

	public EnumComboBoxListRenderer() {
		this(null);
	}

	public EnumComboBoxListRenderer(Icon icon) {
		super();
		_icon = icon;
	}

	@Override
	public JLabel getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		final JLabel result = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		if (value == null) {
			result.setText("- none -");
		} else if (value instanceof HasName) {
			String name = ((HasName) value).getName();
			if (!StringUtils.isNullOrEmpty(name)) {
				result.setText(name);
			}
		}

		if (_icon != null) {
			result.setIcon(_icon);
		}

		return result;
	}
}
