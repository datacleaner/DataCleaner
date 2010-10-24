package org.eobjects.datacleaner.windows;

import java.awt.Image;

import javax.swing.JComponent;

import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.widgets.table.DCTable;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.query.Query;

public class DataSetWindow extends AbstractWindow {

	private static final long serialVersionUID = 1L;
	private final Query _query;
	private final DataContext _dataContext;
	private final int _maxRows;

	public DataSetWindow(Query query, DataContext dataContext) {
		this(query, dataContext, -1);
	}

	public DataSetWindow(Query query, DataContext dataContext, int maxRows) {
		super();
		_query = query;
		_dataContext = dataContext;
		_maxRows = maxRows;
	}

	@Override
	protected String getWindowTitle() {
		String title = "DataSet: " + _query.toSql();
		if (_maxRows > 0) {
			title = title + " (first " + _maxRows + " rows)";
		}
		return title;
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	protected Image getWindowIcon() {
		return ImageManager.getInstance().getImage("images/actions/preview_data.png");
	}

	@Override
	protected JComponent getWindowContent() {
		if (_maxRows > 0) {
			_query.setMaxRows(_maxRows);
		}
		DataSet dataSet = _dataContext.executeQuery(_query);
		return new DCTable(dataSet.toTableModel()).toPanel();
	}

	@Override
	protected boolean isCentered() {
		return true;
	}
}
