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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JButton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.comparator.ColumnComparator;
import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.gui.DataCleanerGui;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.windows.ComparatorResultWindow;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.OrderByItem;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;

public class RunComparatorButton extends JButton implements ActionListener {

	private static final long serialVersionUID = 3477825266323556984L;
	private static final Log _log = LogFactory
			.getLog(RunComparatorButton.class);
	private DataContextSelection _leftDataContextSelection;
	private DataContextSelection _rightDataContextSelection;
	private ColumnSelection _leftColumnSelection;
	private ColumnSelection _rightColumnSelection;

	@Override
	public void removeNotify() {
		super.removeNotify();
		_log.debug("removeNotify()");
		_leftDataContextSelection = null;
		_rightDataContextSelection = null;
		_leftColumnSelection = null;
		_rightColumnSelection = null;
	}

	public RunComparatorButton(DataContextSelection leftDataContextSelection,
			DataContextSelection rightDataContextSelection,
			ColumnSelection leftColumnSelection,
			ColumnSelection rightColumnSelection) {
		super("Run comparison", GuiHelper
				.getImageIcon("images/toolbar_run.png"));
		_leftDataContextSelection = leftDataContextSelection;
		_rightDataContextSelection = rightDataContextSelection;
		_leftColumnSelection = leftColumnSelection;
		_rightColumnSelection = rightColumnSelection;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent event) {
		Iterator<Column> leftIterator = _leftColumnSelection.getColumns()
				.iterator();
		Iterator<Column> rightIterator = _rightColumnSelection.getColumns()
				.iterator();

		ComparatorResultWindow resultWindow = new ComparatorResultWindow();
		DataCleanerGui.getMainWindow().addWindow(resultWindow);

		while (leftIterator.hasNext() && rightIterator.hasNext()) {
			Column leftColumn = leftIterator.next();
			Column rightColumn = rightIterator.next();

			try {

				DataSet leftData = executeQuery(_leftDataContextSelection,
						leftColumn);
				DataSet rightData = executeQuery(_rightDataContextSelection,
						rightColumn);

				ColumnComparator columnComparator = new ColumnComparator();
				columnComparator.initialize(leftColumn, rightColumn);

				boolean leftNext = leftData.next();
				boolean rightNext = rightData.next();
				while (leftNext || rightNext) {
					if (leftNext) {
						Row row = leftData.getRow();
						Long count = ((Number) row.getValue(1)).longValue();
						columnComparator.processValue(leftColumn, row
								.getValue(0), count);
						leftNext = leftData.next();
					}
					if (rightNext) {
						Row row = rightData.getRow();
						Long count = ((Number) row.getValue(1)).longValue();
						columnComparator.processValue(rightColumn, row
								.getValue(0), count);
						rightNext = rightData.next();
					}
				}

				leftData.close();
				rightData.close();

				resultWindow.addResults(columnComparator);

			} catch (Exception e) {
				_log.error(e);
			}
		}
	}

	private DataSet executeQuery(DataContextSelection dataContextSelection,
			Column column) {
		Query q = new Query();
		q.select(column).selectCount();
		q.from(column.getTable());
		q.groupBy(column);
		q.orderBy(new OrderByItem(new SelectItem(column)));
		return dataContextSelection.getDataContext().executeQuery(q);
	}
}