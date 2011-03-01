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
package org.eobjects.datacleaner.windows;

import java.awt.Dimension;
import java.awt.Image;

import javax.swing.JComponent;
import javax.swing.table.TableModel;

import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.widgets.table.DCTable;

import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.query.Query;

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
	public String getWindowTitle() {
		return _title;
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	public Image getWindowIcon() {
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
