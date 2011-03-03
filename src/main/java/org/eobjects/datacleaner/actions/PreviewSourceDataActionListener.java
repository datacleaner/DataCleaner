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

import javax.swing.JOptionPane;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.datacleaner.windows.DataSetWindow;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreviewSourceDataActionListener implements ActionListener {

	private static final Logger logger = LoggerFactory.getLogger(PreviewSourceDataActionListener.class);

	private static final int DEFAULT_PREVIEW_ROWS = 400;
	private final DataContextProvider _dataContextProvider;
	private final Column[] _columns;
	private final Collection<? extends InputColumn<?>> _inputColumns;

	public PreviewSourceDataActionListener(DataContextProvider dataContextProvider, Column... columns) {
		_dataContextProvider = dataContextProvider;
		_columns = columns;
		_inputColumns = null;
	}

	public PreviewSourceDataActionListener(DataContextProvider dataContextProvider, Table table) {
		this(dataContextProvider, table.getColumns());
	}

	public PreviewSourceDataActionListener(DataContextProvider dataContextProvider,
			Collection<? extends InputColumn<?>> inputColumns) {
		_dataContextProvider = dataContextProvider;
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
		DataContext dc = _dataContextProvider.getDataContext();
		Query q = dc.query().from(columns[0].getTable()).select(columns).toQuery();

		String previewRowsString = JOptionPane.showInputDialog("Max number of records?", DEFAULT_PREVIEW_ROWS);
		int previewRows;
		try {
			previewRows = Integer.parseInt(previewRowsString);
		} catch (NumberFormatException ex) {
			logger.warn("Entered value could not be parsed as an int: " + previewRowsString, ex);
			previewRows = DEFAULT_PREVIEW_ROWS;
		}

		DataSetWindow window = new DataSetWindow(q, dc, previewRows);
		window.setVisible(true);
	}
}
