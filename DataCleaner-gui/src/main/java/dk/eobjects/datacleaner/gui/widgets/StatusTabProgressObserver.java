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

	// Status instance variables
	private int _currentTable;
	private int _numTables;
	private long _currentRow;
	private long _currentTableNumRows;

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

	/**
	 * Updates labels for tables during execution
	 */
	private void updateStatusLabel() {
		if (_verboseMonitoringCheckBox.isSelected()) {
			addLogMessage(_currentRow + " rows processed.");
		}
		String statusString = _currentRow + "/" + _currentTableNumRows;
		TableModel model = _statusTable.getModel();
		model.setValueAt(statusString, _currentTable, 1);
		_statusTable.setModel(model);
	}

	private void updateStatusBarAfterExecution() {
		if (_currentTable + 1 == _numTables) {
			_workingIconLabel.setVisible(false);
		}
	}

	public synchronized void init(Table[] tablesToProcess) {
		// Panel for table information
		_statusTable = new DataCleanerTable(TABLE_COLUMNS);
		_currentTable = -1;
		_numTables = tablesToProcess.length;
		DefaultTableModel model = new DefaultTableModel(TABLE_COLUMNS,
				_numTables);
		for (int i = 0; i < tablesToProcess.length; i++) {
			Table table = tablesToProcess[i];
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

	public void notifyBeginning(Table tableToProcess, long numRows) {
		_currentTable++;
		_currentTableNumRows = numRows;
		_currentRow = 0;
		addLogMessage("Analysis begin: " + tableToProcess.getName());
		addLogMessage("Rows in table: " + numRows);
		updateStatusLabel();
	}

	public void notifyProgress(long numRowsProcessed) {
		_currentRow = numRowsProcessed;
		updateStatusLabel();
	}

	public void notifySuccess(Table processedTable, long numRowsProcessed) {
		_currentRow = numRowsProcessed;
		updateStatusLabel();
		updateStatusBarAfterExecution();
		addLogMessage("Analysis success: " + processedTable.getName());
	}

	public void notifyFailure(Table processedTable, Throwable throwable,
			long lastRow) {
		_currentRow = lastRow;
		updateStatusLabel();
		updateStatusBarAfterExecution();

		addLogMessage("Analysis failure: " + processedTable.getName());

		if (throwable != null) {
			StringWriter stringWriter = new StringWriter();
			throwable.printStackTrace(new PrintWriter(stringWriter));
			addLogMessage(stringWriter.toString());
		}
	}
}
