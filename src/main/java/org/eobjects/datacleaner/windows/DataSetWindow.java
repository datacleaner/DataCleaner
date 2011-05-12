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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableModel;

import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.query.Query;

public class DataSetWindow extends AbstractWindow {

	private static final long serialVersionUID = 1L;
	private final Query _query;
	private final DataContext _dataContext;
	private final int _pageSize;
	private final String _title;
	private final TableModel _tableModel;
	private final DCTable _table;

	public DataSetWindow(Query query, DataContext dataContext) {
		this(query, dataContext, -1);
	}

	public DataSetWindow(Query query, DataContext dataContext, int pageSize) {
		super();
		_table = new DCTable();
		_query = query;
		_dataContext = dataContext;
		_pageSize = pageSize;
		_title = "DataSet: " + _query.toSql();
		_tableModel = null;
	}

	public DataSetWindow(String title, TableModel tableModel) {
		super();
		_table = new DCTable();
		_query = null;
		_dataContext = null;
		_pageSize = -1;
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
			if (_pageSize > 0) {
				_query.setMaxRows(_pageSize);
			}
			updateTableByQuery();
		} else {
			_table.setModel(_tableModel);
		}

		if (_table.getColumnCount() > 10) {
			_table.setHorizontalScrollEnabled(true);
		}
		
		final DCPanel tablePanel = _table.toPanel();
		
		if (_query == null) {
			return tablePanel;
		}

		Integer maxRows = _query.getMaxRows();
		if (maxRows == null) {
			// no paging needed when there are no max rows property
			return tablePanel;
		}

		final DCPanel pagingButtonPanel = createPagingButtonPanel();
		if (pagingButtonPanel == null) {
			// paging not needed because the actual amount of rows where low
			return tablePanel;
		}

		DCPanel panel = new DCPanel();
		panel.setLayout(new BorderLayout());
		panel.add(tablePanel, BorderLayout.CENTER);
		panel.add(pagingButtonPanel, BorderLayout.SOUTH);
		return panel;
	}

	private DCPanel createPagingButtonPanel() {
		final int maxRows = _query.getMaxRows();
		final JButton previousPageButton = WidgetFactory.createButton("Previous page", "images/actions/back.png");
		final JButton nextPageButton = WidgetFactory.createButton("Next page", "images/actions/forward.png");

		previousPageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int newFirstRow = getFirstRow() - maxRows;
				if (newFirstRow < 0) {
					newFirstRow = 0;
				}
				_query.setFirstRow(newFirstRow);
				updateTableByQuery();
				updatePagingButtons(previousPageButton, nextPageButton);
			}
		});

		nextPageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int newFirstRow = getFirstRow() + maxRows;
				_query.setFirstRow(newFirstRow);
				updateTableByQuery();
				updatePagingButtons(previousPageButton, nextPageButton);
			}
		});

		updatePagingButtons(previousPageButton, nextPageButton);

		if (!previousPageButton.isEnabled() && !nextPageButton.isEnabled()) {
			return null;
		}

		final DCPanel buttonPanel = new DCPanel(WidgetUtils.BG_COLOR_DARK, WidgetUtils.BG_COLOR_DARK);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 10));
		buttonPanel.setBorder(new MatteBorder(1, 0, 0, 0, WidgetUtils.BG_COLOR_MEDIUM));
		buttonPanel.add(previousPageButton);
		buttonPanel.add(nextPageButton);

		return buttonPanel;
	}

	private void updatePagingButtons(JButton previousPageButton, JButton nextPageButton) {
		if (_table.getRowCount() < _query.getMaxRows()) {
			nextPageButton.setEnabled(false);
		} else {
			nextPageButton.setEnabled(true);
		}

		if (getFirstRow() <= 0) {
			previousPageButton.setEnabled(false);
		} else {
			previousPageButton.setEnabled(true);
		}

	}

	private int getFirstRow() {
		return _query.getFirstRow() == null ? 0 : _query.getFirstRow();
	}

	private void updateTableByQuery() {
		DataSet dataSet = _dataContext.executeQuery(_query);
		_table.setModel(dataSet.toTableModel());
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
