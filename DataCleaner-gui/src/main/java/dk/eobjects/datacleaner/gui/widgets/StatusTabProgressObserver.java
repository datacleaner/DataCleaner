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
package dk.eobjects.datacleaner.gui.widgets;

import java.awt.BorderLayout;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.lang.ArrayUtils;
import org.joda.time.DateTime;

import dk.eobjects.datacleaner.execution.IProgressObserver;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.windows.ResultWindow;
import dk.eobjects.datacleaner.profiler.trivial.TimeAnalysisProfile;
import dk.eobjects.metamodel.schema.Table;

public class StatusTabProgressObserver implements IProgressObserver {

	private static final String[] TABLE_COLUMNS = new String[] { "Table",
			"Processed rows" };
	private DataCleanerTable _statusTable;
	private JCheckBox _verboseMonitoringCheckBox;
	private JTextArea _logTextArea;
	private JLabel _workingIconLabel;
	private ResultWindow _resultWindow;

	private Table[] _tables;
	private long[] _totalRows;
	private long[] _processedRows;
	private int _processedTables;

	public StatusTabProgressObserver(ResultWindow resultWindow) {
		_logTextArea = GuiHelper.createLabelTextArea().toComponent();
		_resultWindow = resultWindow;
		_verboseMonitoringCheckBox = GuiHelper.createCheckBox(
				"Verbose monitoring", true).toComponent();

		// Working icon
		ImageIcon workingIcon = GuiHelper.getImageIcon("images/working.gif");
		_workingIconLabel = new JLabel(workingIcon);
		_workingIconLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
		workingIcon.setImageObserver(_workingIconLabel);
	}

	private String getTimestamp() {
		return new DateTime()
				.toString(TimeAnalysisProfile.DATE_AND_TIME_PATTERN)
				+ ": ";
	}

	public void addLogMessage(String message) {
		_logTextArea.append(getTimestamp() + message + '\n');
	}

	public synchronized void init(Table[] tables) {
		// Panel for table information
		_statusTable = new DataCleanerTable(TABLE_COLUMNS);
		_tables = tables;
		_totalRows = new long[_tables.length];
		_processedRows = new long[_tables.length];
		DefaultTableModel model = new DefaultTableModel(TABLE_COLUMNS,
				_tables.length);
		for (int i = 0; i < tables.length; i++) {
			Table table = tables[i];
			String name = table.getName();

			model.setValueAt(name, i, 0);
			model.setValueAt("", i, 1);
		}
		_statusTable.setModel(model);

		// Add the status tab
		_resultWindow.addTab("Status", GuiHelper
				.getImageIcon("images/tab_log.png"), getStatusPanel());
	}

	/**
	 * Creates the tab's content panel
	 */
	private JComponent getStatusPanel() {
		JPanel rightPanel = GuiHelper.createPanel().applyBorderLayout()
				.toComponent();
		rightPanel.add(new JScrollPane(_logTextArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		rightPanel.add(_verboseMonitoringCheckBox, BorderLayout.SOUTH);

		// Left panel with icon and table information
		JPanel leftPanel = GuiHelper.createPanel().applyBorderLayout()
				.toComponent();
		leftPanel.add(_workingIconLabel, BorderLayout.NORTH);
		leftPanel.add(_statusTable.toPanel(), BorderLayout.CENTER);

		// The whole tab's panel
		return new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel,
				rightPanel);
	}

	private void updateStatusLabel(int tableIndex) {
		TableModel model = _statusTable.getModel();
		model.setValueAt(_processedRows[tableIndex] + " / "
				+ _totalRows[tableIndex], tableIndex, 1);
		_statusTable.setModel(model);
	}

	public void notifyBeginning(Table table, long numRows) {
		addLogMessage("Analysis begin: " + table.getName());
		addLogMessage("Rows in table: " + numRows);
		int tableIndex = ArrayUtils.indexOf(_tables, table);
		_totalRows[tableIndex] = numRows;
		updateStatusLabel(tableIndex);
	}

	public synchronized void notifyProgress(Table table, long numRowsProcessed) {
		int tableIndex = ArrayUtils.indexOf(_tables, table);
		_processedRows[tableIndex] += numRowsProcessed;
		updateStatusLabel(tableIndex);
		addLogMessage(table.getName() + ": " + _processedRows[tableIndex]
				+ " rows processed.");
	}

	public synchronized void notifySuccess(Table table, long numRowsProcessed) {
		_processedTables++;
		if (_processedTables == _tables.length) {
			_workingIconLabel.setVisible(false);
		}
		int tableIndex = ArrayUtils.indexOf(_tables, table);
		_processedRows[tableIndex] = numRowsProcessed;
		updateStatusLabel(tableIndex);
		addLogMessage(table.getName() + ": Analysis success!");
	}

	public synchronized void notifyFailure(Table table, Throwable throwable,
			Long lastRow) {
		_processedTables++;
		if (_processedTables == _tables.length) {
			_workingIconLabel.setVisible(false);
		}
		if (lastRow != null) {
			int tableIndex = ArrayUtils.indexOf(_tables, table);
			_processedRows[tableIndex] = lastRow;
			updateStatusLabel(tableIndex);
		}
		addLogMessage(table.getName() + ": Analysis failure!");

		if (throwable != null) {
			StringWriter stringWriter = new StringWriter();
			throwable.printStackTrace(new PrintWriter(stringWriter));
			addLogMessage(stringWriter.toString());
		}
	}
}
