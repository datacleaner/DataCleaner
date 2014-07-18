/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
import java.util.concurrent.Callable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableModel;

import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.LoadingIcon;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.data.DataSetTableModel;
import org.eobjects.metamodel.query.Query;

public class DataSetWindow extends AbstractWindow {

    private static final long serialVersionUID = 1L;
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
                DataSet dataSet = dataContext.executeQuery(_query);
                return new DataSetTableModel(dataSet);
            }
        };
        _previousPageButton = WidgetFactory.createButton("Previous page", "images/actions/back.png");
        _previousPageButton.setEnabled(false);
        _nextPageButton = WidgetFactory.createButton("Next page", "images/actions/forward.png");
        _nextPageButton.setEnabled(false);
    }

    public DataSetWindow(final String title, final Callable<TableModel> tableModelCallable, final WindowContext windowContext) {
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
        return ImageManager.get().getImage("images/actions/preview_data.png");
    }

    @Override
    protected JComponent getWindowContent() {
        updateTable();

        final DCPanel tablePanel = _table.toPanel();

        final DCPanel pagingButtonPanel = createPagingButtonPanel();

        DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        _loadingIcon.setPreferredSize(300, 300);
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
                return _tableModelCallable.call();
            };

            protected void done() {
                try {
                    TableModel tableModel = get();
                    _table.setModel(tableModel);

                    if (_table.getColumnCount() > 10) {
                        _table.setHorizontalScrollEnabled(true);
                    }
                    updatePagingButtons();

                    _loadingIcon.setVisible(false);
                    _table.setVisible(true);

                    if (getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                        Dimension dimensions = autoSetSize();
                        getContentPane().setSize(dimensions);
                        pack();
                        centerOnScreen();
                    }

                } catch (Exception e) {
                    DataSetWindow.this.dispose();
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    }
                    throw new IllegalStateException(e);
                }
            };
        }.execute();

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

        final DCPanel buttonPanel = new DCPanel(WidgetUtils.BG_COLOR_DARK, WidgetUtils.BG_COLOR_DARK);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 10));
        buttonPanel.setBorder(new MatteBorder(1, 0, 0, 0, WidgetUtils.BG_COLOR_MEDIUM));
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
