/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableModel;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.DataSetTableModel;
import org.apache.metamodel.query.Query;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.ErrorUtils;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.LoadingIcon;
import org.datacleaner.widgets.table.DCTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * A window that presents a tabular dataset to the end user. The dataset may be
 * based on queried data, transformed data, a record sample or other such
 * source.
 */
public class DataSetWindow extends AbstractWindow {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(DataSetWindow.class);

    private final Query _query;
    private final int _pageSize;
    private final String _title;
    private final Callable<TableModel> _tableModelCallable;
    private final DCTable _table;
    private final LoadingIcon _loadingIcon = new LoadingIcon();
    private JButton _previousPageButton;
    private JButton _nextPageButton;

    public DataSetWindow(final Query query, final DataContext dataContext, WindowContext windowContext) {
        this(query, dataContext, -1, windowContext);
    }

    public DataSetWindow(final Query query, final DataContext dataContext, int pageSize, WindowContext windowContext) {
        super(windowContext);
        _table = new DCTable();
        _query = query;
        _pageSize = pageSize;
        _title = "DataSet: " + _query.toSql();
        _tableModelCallable = new Callable<TableModel>() {
            @Override
            public TableModel call() throws Exception {
                final DataSet dataSet = dataContext.executeQuery(_query);
                return new DataSetTableModel(dataSet);
            }
        };
        _previousPageButton = WidgetFactory.createDefaultButton("Previous page", IconUtils.ACTION_BACK);
        _previousPageButton.setEnabled(false);
        _nextPageButton = WidgetFactory.createDefaultButton("Next page", IconUtils.ACTION_FORWARD);
        _nextPageButton.setHorizontalTextPosition(SwingConstants.LEFT);
        _nextPageButton.setEnabled(false);
    }

    public DataSetWindow(final String title, final Callable<TableModel> tableModelCallable,
            final WindowContext windowContext) {
        super(windowContext);
        _table = new DCTable();
        _query = null;
        _pageSize = -1;
        _title = title;
        _tableModelCallable = tableModelCallable;
        _previousPageButton = null;
        _nextPageButton = null;
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
        return ImageManager.get().getImage(IconUtils.ACTION_PREVIEW);
    }

    @Override
    protected JComponent getWindowContent() {
        updateTable();

        _table.setColumnControlVisible(false);
        final DCPanel tablePanel = _table.toPanel();
        final DCPanel pagingButtonPanel = createPagingButtonPanel();

        final DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        _loadingIcon.setPreferredSize(700, 300);
        panel.add(_loadingIcon, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);
        if (pagingButtonPanel != null) {
            panel.add(pagingButtonPanel, BorderLayout.SOUTH);
        }
        return panel;
    }

    private void updateTable() {
        _loadingIcon.setVisible(true);
        _table.setVisible(false);

        if (_query != null) {
            if (_pageSize > 0) {
                _query.setMaxRows(_pageSize);
            }
        }

        new SwingWorker<TableModel, Void>() {
            protected TableModel doInBackground() throws Exception {
                final TableModel tableModel = _tableModelCallable.call();
                touchTableModel(tableModel);
                return tableModel;
            };

            protected void done() {
                try {
                    final TableModel tableModel = get();
                    _table.setModel(tableModel);

                    final int columnCount = _table.getColumnCount();
                    if (columnCount > 10) {
                        _table.setHorizontalScrollEnabled(true);
                    }
                    updatePagingButtons();

                    _loadingIcon.setVisible(false);
                    _table.setVisible(true);

                } catch (Throwable e) {
                    e = ErrorUtils.unwrapForPresentation(e);
                    logger.error("Unexpected error occurred while building DataSetWindow contents", e);
                    DataSetWindow.this.dispose();
                    
                    final String exceptionMessage = e.getMessage();
                    if (Strings.isNullOrEmpty(exceptionMessage)) {
                        WidgetUtils.showErrorMessage("Unexpected error",
                                "An unexpected error occurred while building data set.\n\nSee logs for details.");
                    } else {
                        WidgetUtils.showErrorMessage("Unexpected error",
                                "An unexpected error occurred while building data set:\n" + exceptionMessage + "\n\nSee logs for details.");
                    }
                }
            };
        }.execute();

    }

    /**
     * Touches the table model values in order to ensure that it will be able to
     * retrieve values. This is a bit of a silly thing to do, but it ensures
     * that errors during {@link TableModel#getValueAt(int, int)} will not
     * affect the user at rendering time. If errors occur, we encounter them
     * early by invoking this method
     * 
     * @param tableModel
     */
    private void touchTableModel(TableModel tableModel) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                tableModel.getValueAt(row, col);
            }
        }
    }

    private DCPanel createPagingButtonPanel() {
        if (_query == null) {
            return null;
        }

        final Integer maxRows = _query.getMaxRows();
        if (maxRows == null) {
            // no paging needed when there are no max rows property
            return null;
        }

        _previousPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int newFirstRow = getFirstRow() - maxRows;
                if (newFirstRow <= 0) {
                    newFirstRow = 1;
                }
                _query.setFirstRow(newFirstRow);
                updateTable();
            }
        });

        _nextPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int newFirstRow = getFirstRow() + maxRows;
                _query.setFirstRow(newFirstRow);
                updateTable();
            }
        });

        final DCPanel buttonPanel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        buttonPanel.setBorder(new MatteBorder(1, 0, 0, 0, WidgetUtils.BG_COLOR_LESS_BRIGHT));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 10));
        buttonPanel.add(_previousPageButton);
        buttonPanel.add(_nextPageButton);

        return buttonPanel;
    }

    private void updatePagingButtons() {
        if (_nextPageButton != null) {
            if (_table.getRowCount() < _query.getMaxRows()) {
                _nextPageButton.setEnabled(false);
            } else {
                _nextPageButton.setEnabled(true);
            }
        }

        if (_previousPageButton != null) {
            if (getFirstRow() <= 1) {
                _previousPageButton.setEnabled(false);
            } else {
                _previousPageButton.setEnabled(true);
            }
        }
    }

    private int getFirstRow() {
        return _query.getFirstRow() == null ? 1 : _query.getFirstRow();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = super.getPreferredSize();
        if (preferredSize.width < 300) {
            preferredSize.width = 300;
        }
        if (preferredSize.height < 200) {
            preferredSize.height = 200;
        }
        return preferredSize;
    }

    @Override
    protected boolean isCentered() {
        return true;
    }
}
