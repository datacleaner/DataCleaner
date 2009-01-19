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
package dk.eobjects.datacleaner.gui.windows;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.commons.lang.ArrayUtils;
import org.jdesktop.swingx.JXStatusBar;

import dk.eobjects.datacleaner.execution.IProgressObserver;
import dk.eobjects.datacleaner.gui.GuiBuilder;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.metamodel.schema.Table;
import dk.eobjects.thirdparty.tabs.CloseableTabbedPane;

public abstract class ResultWindow extends AbstractWindow implements
		IProgressObserver {

	private String _title;
	private CloseableTabbedPane _tabbedPane;
	private JProgressBar _progressBar;
	private JLabel _progressLabel;

	// Instance variables for execution status monitoring
	private boolean _failures = false;
	private Table[] _tables;
	private long[] _processedRows;
	private long _totalRows;
	private int _processedTables = 0;

	@Override
	public void disposeInternal() {
		super.disposeInternal();
		_tabbedPane = null;
		_progressBar = null;
		_progressLabel = null;
		_title = null;
	}

	public ResultWindow(String title) {
		_title = title;
		new GuiBuilder<JPanel>(_panel).applyBorderLayout()
				.applyDarkBlueBackground();
		_tabbedPane = new CloseableTabbedPane();

		_panel.add(_tabbedPane, BorderLayout.CENTER);

		JXStatusBar statusBar = new GuiBuilder<JXStatusBar>(new JXStatusBar())
				.applyLightBackground().toComponent();
		JXStatusBar.Constraint c1 = new JXStatusBar.Constraint(
				JXStatusBar.Constraint.ResizeBehavior.FILL);
		_progressLabel = new JLabel("");
		_progressLabel.setFont(GuiHelper.FONT_NORMAL.deriveFont(Font.BOLD));
		statusBar.add(_progressLabel, c1);

		_progressBar = new JProgressBar(0, 100);
		_progressBar.setForeground(GuiHelper.BG_COLOR_DARKBLUE);
		statusBar.add(_progressBar, new JXStatusBar.Constraint(300));
		_panel.add(statusBar, BorderLayout.SOUTH);
	}

	public void addTab(String title, ImageIcon icon, JComponent component) {
		int tabCount = _tabbedPane.getTabCount();
		_tabbedPane.setUnclosableTab(tabCount);
		_tabbedPane.addTab(title, icon, component);
	}

	public JProgressBar getProgressBar() {
		return _progressBar;
	}

	@Override
	public String getTitle() {
		return _title;
	}

	/**
	 * Updates statusbar after execution
	 */
	private void updateStatusBarAfterExecution() {
		if (_totalRows > 0) {
			long processedRows = 0l;
			for (long count : _processedRows) {
				processedRows += count;
			}
			int progress = Math.round(100 * processedRows / _totalRows);
			_progressBar.setValue(progress);
		}
		if (_processedTables == _tables.length) {
			if (_failures) {
				_progressLabel.setText("Done, but with failures.");
			} else {
				_progressLabel.setText("Done.");
			}
		}
	}

	public void init(Table[] tables) {
		_tables = tables;
		_processedRows = new long[_tables.length];
	}

	public void notifyBeginning(Table table, long numRows) {
		_totalRows += numRows;
	}

	public synchronized void notifyProgress(Table table, long numRowsProcessed) {
		int tableIndex = ArrayUtils.indexOf(_tables, table);
		_processedRows[tableIndex] += numRowsProcessed;
		updateStatusBarAfterExecution();
	}

	public synchronized void notifySuccess(Table table, long numRowsProcessed) {
		int tableIndex = ArrayUtils.indexOf(_tables, table);
		_processedRows[tableIndex] = numRowsProcessed;
		_processedTables++;
		updateStatusBarAfterExecution();
	}

	public synchronized void notifyFailure(Table table, Throwable throwable,
			Long lastRow) {
		if (lastRow != null) {
			int tableIndex = ArrayUtils.indexOf(_tables, table);
			_processedRows[tableIndex] = lastRow;
		}
		_processedTables++;
		_failures = true;
		updateStatusBarAfterExecution();
	}
}