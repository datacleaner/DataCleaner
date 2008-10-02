/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.gui.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.gui.DataCleanerGui;
import dk.eobjects.datacleaner.gui.GuiBuilder;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.windows.PreviewDataWindow;
import dk.eobjects.datacleaner.util.WeakObservable;
import dk.eobjects.datacleaner.util.WeakObserver;
import dk.eobjects.metamodel.MetaModelHelper;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class ColumnSelectionPanel extends JPanel implements WeakObserver {

	protected final Log _log = LogFactory.getLog(getClass());
	private static final long serialVersionUID = 7635979788279430168L;
	private DataContextSelection _dataContextSelection;
	private ColumnSelection _columnSelection;
	private JTextField _tableField;
	private JTextArea _columnsField;

	@Override
	public void removeNotify() {
		super.removeNotify();
		_log.debug("removeNotify()");
		_columnSelection.deleteObserver(this);
		_columnSelection = null;
		_dataContextSelection = null;
	}

	public ColumnSelectionPanel(DataContextSelection schemaSelection,
			ColumnSelection dataSelection) {
		super();
		_columnSelection = dataSelection;
		_columnSelection.addObserver(this);
		_dataContextSelection = schemaSelection;
		setLayout(null);
		new GuiBuilder<JPanel>(this).applyLightBackground();

		JTextArea label = new JTextArea(
				"Double-click items in the schema tree to add them to the data selection.");
		Dimension dimension = new Dimension(400, 28);
		label.setPreferredSize(dimension);
		label.setSize(dimension);
		label.setLocation(110, 10);
		label.setLineWrap(true);
		label.setWrapStyleWord(true);
		label.setEditable(false);
		label.setBackground(getBackground());
		label.setFont(GuiHelper.FONT_NORMAL);
		add(label);

		JLabel tableLabel = new JLabel("Table(s):");
		tableLabel.setLocation(10, 50);
		tableLabel.setSize(100, 20);
		add(tableLabel);

		_tableField = new GuiBuilder<JTextField>(new JTextField())
				.applyDarkBackground().applyBorder().applySize(400, 20)
				.toComponent();
		_tableField.setEditable(false);
		_tableField.setLocation(110, 50);
		add(_tableField);

		JLabel columnsLabel = new JLabel("Column(s):");
		columnsLabel.setLocation(10, 75);
		columnsLabel.setSize(100, 20);
		add(columnsLabel);

		_columnsField = GuiHelper.createLabelTextArea().applyDarkBackground()
				.toComponent();
		_columnsField.setName("selected_columns");
		_columnsField.setColumns(50);
		_columnsField.setBorder(null);
		JScrollPane scrollPane = new GuiBuilder<JScrollPane>(new JScrollPane(
				_columnsField)).applySize(400, 120).toComponent();
		scrollPane.setBorder(new LineBorder(Color.DARK_GRAY));
		scrollPane.setLocation(110, 75);
		add(scrollPane);

		JButton clearButton = GuiHelper.createButton("Clear selection",
				"images/toolbar_clear_selection.png").applySize(170, 28)
				.toComponent();
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_columnSelection.clearSelection();
				_tableField.setText("");
				_columnsField.setText("");
			}
		});
		clearButton.setLocation(180, 205);
		add(clearButton);

		JButton previewButton = GuiHelper.createButton("Preview data",
				"images/toolbar_preview_data.png").applySize(150, 28)
				.toComponent();
		previewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				previewData();
			}
		});
		previewButton.setLocation(360, 205);
		add(previewButton);
	}

	private void updateDataFields() {
		StringBuilder tableSb = new StringBuilder();
		StringBuilder columnSb = new StringBuilder();

		List<Column> columns = _columnSelection.getColumns();
		Table[] tables = MetaModelHelper.getTables(columns);
		for (int i = 0; i < tables.length; i++) {
			if (i != 0) {
				tableSb.append(", ");
			}
			tableSb.append(tables[i].getName());
		}
		_tableField.setText(tableSb.toString());

		for (int i = 0; i < columns.size(); i++) {
			if (i != 0) {
				columnSb.append(", ");
			}
			Column column = columns.get(i);
			GuiHelper.getLabelForColumn(column);
			columnSb.append(GuiHelper.getLabelForColumn(column));
		}
		_columnsField.setText(columnSb.toString());
	}

	public void update(WeakObservable o) {
		if (o instanceof ColumnSelection) {
			updateDataFields();
		}
	}

	private void previewData() {
		if (_dataContextSelection != null
				&& _columnSelection.getColumns().size() > 0) {
			int numRecords = 400;
			String userInput = JOptionPane.showInputDialog(
					"Max. number of records?", numRecords);
			if (userInput != null) {
				try {
					numRecords = Integer.parseInt(userInput);
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null,
							"The provided max. number of records is not a number. Using default: "
									+ numRecords, "Error!",
							JOptionPane.WARNING_MESSAGE);
				}
				List<Column> configuredColumns = _columnSelection.getColumns();
				Table[] allTables = MetaModelHelper
						.getTables(configuredColumns);
				for (Table table : allTables) {
					Column[] tableColumns = MetaModelHelper.getTableColumns(
							table, configuredColumns);
					PreviewDataWindow previewDataWindow = new PreviewDataWindow(
							table, tableColumns, _dataContextSelection
									.getDataContext(), numRecords);
					DataCleanerGui.getMainWindow().addWindow(previewDataWindow);
				}
			}
		} else {
			JOptionPane
					.showMessageDialog(
							null,
							"No data selected. Doubleclick the tree to the left to select data.",
							"Error!", JOptionPane.WARNING_MESSAGE);
		}
	}
}