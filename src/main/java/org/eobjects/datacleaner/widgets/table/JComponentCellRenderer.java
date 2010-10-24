package org.eobjects.datacleaner.widgets.table;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class JComponentCellRenderer implements TableCellRenderer {

	private static final TableCellRenderer instance = new JComponentCellRenderer();

	public static TableCellRenderer getInstance() {
		return instance;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		assert value instanceof JComponent;
		JComponent component = (JComponent) value;
		component.setOpaque(true);
		return component;
	}

}
