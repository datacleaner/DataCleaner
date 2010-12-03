package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.datacleaner.windows.DataSetWindow;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class PreviewSourceDataActionListener implements ActionListener {

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
			Set<Column> cols = new HashSet<Column>();
			for (InputColumn<?> col : _inputColumns) {
				if (col.isPhysicalColumn()) {
					cols.add(col.getPhysicalColumn());
				}
			}
			columns = cols.toArray(new Column[cols.size()]);
		}
		DataContext dc = _dataContextProvider.getDataContext();
		Query q = dc.query().from(columns[0].getTable()).select(columns).toQuery();
		DataSetWindow window = new DataSetWindow(q, dc, DEFAULT_PREVIEW_ROWS);
		window.setVisible(true);
	}
}
