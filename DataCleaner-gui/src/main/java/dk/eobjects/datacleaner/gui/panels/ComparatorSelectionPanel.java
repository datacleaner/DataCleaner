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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.widgets.DataCleanerTable;
import dk.eobjects.datacleaner.gui.widgets.RunComparatorButton;
import dk.eobjects.datacleaner.util.WeakObservable;
import dk.eobjects.datacleaner.util.WeakObserver;
import dk.eobjects.metamodel.schema.Column;

public class ComparatorSelectionPanel extends JPanel implements WeakObserver {

	protected final Log _log = LogFactory.getLog(getClass());
	private static final Object[] TABLE_HEADERS = new Object[] {
			"Compare what?", "To what?" };
	private static final long serialVersionUID = 5627810850053499114L;
	private DataContextSelection _leftDataContextSelection;
	private DataContextSelection _rightDataContextSelection;
	private ColumnSelection _leftColumnSelection;
	private ColumnSelection _rightColumnSelection;
	private DataCleanerTable _table;

	@Override
	public void removeNotify() {
		super.removeNotify();
		_log.debug("removeNotify()");
		_leftDataContextSelection = null;
		_rightDataContextSelection = null;
		_leftColumnSelection.deleteObserver(this);
		_leftColumnSelection = null;
		_rightColumnSelection.deleteObserver(this);
		_rightColumnSelection = null;
	}

	public ComparatorSelectionPanel(
			DataContextSelection leftDataContextSelection,
			DataContextSelection rightDataContextSelection,
			ColumnSelection leftColumnSelection,
			ColumnSelection rightColumnSelection) {
		super(new BorderLayout());
		_leftDataContextSelection = leftDataContextSelection;
		_rightDataContextSelection = rightDataContextSelection;
		_leftColumnSelection = leftColumnSelection;
		_rightColumnSelection = rightColumnSelection;
		_leftColumnSelection.addObserver(this);
		_rightColumnSelection.addObserver(this);

		// Top toolbar
		JToolBar toolbar = GuiHelper.createToolBar();
		toolbar.add(GuiHelper.createSeparator());
		toolbar.add(new RunComparatorButton(_leftDataContextSelection,
				_rightDataContextSelection, _leftColumnSelection,
				_rightColumnSelection));
		add(toolbar, BorderLayout.NORTH);

		// Table for column selection display
		_table = new DataCleanerTable();
		_table.setModel(new DefaultTableModel(TABLE_HEADERS, 15));
		add(_table.toPanel(), BorderLayout.CENTER);

		// Clear left button
		JButton clearLeftButton = GuiHelper.createButton(
				"Clear left selection", "images/toolbar_clear_selection.png")
				.toComponent();
		clearLeftButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_leftColumnSelection.clearSelection();
			}
		});

		// Clear right button
		JButton clearRightButton = GuiHelper.createButton(
				"Clear right selection", "images/toolbar_clear_selection.png")
				.toComponent();
		clearRightButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_rightColumnSelection.clearSelection();
			}
		});

		// Bottom toolbar
		JPanel bottomPanel = GuiHelper.createPanel().applyBorderLayout().toComponent();
		bottomPanel.add(clearLeftButton, BorderLayout.WEST);
		bottomPanel.add(clearRightButton, BorderLayout.EAST);
		add(bottomPanel, BorderLayout.SOUTH);
	}

	public void update(WeakObservable o) {
		if (o instanceof ColumnSelection) {
			List<Column> leftColumns = _leftColumnSelection.getColumns();
			List<Column> rightColumns = _rightColumnSelection.getColumns();

			DefaultTableModel model = new DefaultTableModel(TABLE_HEADERS, 15);
			for (int i = 0; i < leftColumns.size(); i++) {
				String columnName = leftColumns.get(i).getName();
				model.setValueAt(columnName, i, 0);
			}
			for (int i = 0; i < rightColumns.size(); i++) {
				String columnName = rightColumns.get(i).getName();
				model.setValueAt(columnName, i, 1);
			}

			_table.setModel(model);
		}
	}

	public ColumnSelection getLeftDataSelection() {
		return _leftColumnSelection;
	}

	public ColumnSelection getRightDataSelection() {
		return _rightColumnSelection;
	}
}