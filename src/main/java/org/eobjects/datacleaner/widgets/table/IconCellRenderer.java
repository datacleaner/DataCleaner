package org.eobjects.datacleaner.widgets.table;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class IconCellRenderer implements TableCellRenderer {

	private final static TableCellRenderer instance = new IconCellRenderer();

	public static TableCellRenderer getInstance() {
		return instance;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		assert value instanceof Icon;
		JLabel label = new JLabel((Icon) value);
		label.setOpaque(true);
		return label;
	}

}
