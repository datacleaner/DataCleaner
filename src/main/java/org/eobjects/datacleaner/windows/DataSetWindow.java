package org.eobjects.datacleaner.windows;

import java.awt.Dimension;
import java.awt.Image;

import javax.swing.JComponent;
import javax.swing.table.TableModel;

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
	private final String _title;
	private TableModel _tableModel;

	public DataSetWindow(Query query, DataContext dataContext) {
		this(query, dataContext, -1);
	}

	public DataSetWindow(Query query, DataContext dataContext, int maxRows) {
		super();
		_query = query;
		_dataContext = dataContext;
		_maxRows = maxRows;
		_title = "DataSet: " + _query.toSql() + (_maxRows > 0 ? " (first " + _maxRows + " rows)" : "");
	}

	public DataSetWindow(String title, TableModel tableModel) {
		super();
		_query = null;
		_dataContext = null;
		_maxRows = -1;
		_title = title;
		_tableModel = tableModel;
	}

	@Override
	protected String getWindowTitle() {
		return _title;
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
		if (_tableModel == null) {
			if (_maxRows > 0) {
				_query.setMaxRows(_maxRows);
			}
			DataSet dataSet = _dataContext.executeQuery(_query);
			_tableModel = dataSet.toTableModel();
		}
		return new DCTable(_tableModel).toPanel();
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension preferredSize = super.getPreferredSize();
		if (preferredSize.width < 300) {
			preferredSize.width = 300;
		}
		return preferredSize;
	}

	@Override
	protected boolean isCentered() {
		return true;
	}
}
