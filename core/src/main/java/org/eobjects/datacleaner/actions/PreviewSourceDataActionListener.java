/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.windows.DataSetWindow;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;

public class PreviewSourceDataActionListener implements ActionListener {

	private static final int PAGE_SIZE = 35;
	private final DatastoreConnection _datastoreConnection;
	private final Column[] _columns;
	private final Collection<? extends InputColumn<?>> _inputColumns;
	private final WindowContext _windowContext;

	public PreviewSourceDataActionListener(WindowContext windowContext, DatastoreConnection datastoreConnection,
			Column... columns) {
		_windowContext = windowContext;
		_datastoreConnection = datastoreConnection;
		_columns = columns;
		_inputColumns = null;
	}

	public PreviewSourceDataActionListener(WindowContext windowContext, DatastoreConnection datastoreConnection, Table table) {
		this(windowContext, datastoreConnection, table.getColumns());
	}

	public PreviewSourceDataActionListener(WindowContext windowContext, DatastoreConnection datastoreConnection,
			Collection<? extends InputColumn<?>> inputColumns) {
		_windowContext = windowContext;
		_datastoreConnection = datastoreConnection;
		_inputColumns = inputColumns;
		_columns = null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Column[] columns = _columns;
		if (columns == null) {
			List<Column> cols = new ArrayList<Column>();
			for (InputColumn<?> col : _inputColumns) {
				if (col.isPhysicalColumn()) {
					cols.add(col.getPhysicalColumn());
				}
			}
			columns = cols.toArray(new Column[cols.size()]);
		}
		DataContext dc = _datastoreConnection.getDataContext();
		Query q = dc.query().from(columns[0].getTable()).select(columns).toQuery();

		DataSetWindow window = new DataSetWindow(q, dc, PAGE_SIZE, _windowContext);
		window.setVisible(true);
	}
}
