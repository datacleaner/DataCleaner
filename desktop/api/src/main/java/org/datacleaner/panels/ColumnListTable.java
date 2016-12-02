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
package org.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.CompoundBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.actions.PreviewSourceDataActionListener;
import org.datacleaner.actions.QueryActionListener;
import org.datacleaner.api.InputColumn;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.table.DCTable;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Panel that displays columns in a table-layout. Used for both the "Source" tab
 * and for transformer panels in DataCleaner desktop application.
 */
public final class ColumnListTable extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final String[] HEADERS_WITH_ACTION_COLUMN = new String[] { "Name", "Type", "" };
    private static final String[] HEADERS_WITHOUT_ACTIONS = new String[] { "Name", "Type" };

    private final ImageManager imageManager = ImageManager.get();
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final Table _table;
    private final DCTable _columnTable;

    private final SortedMap<InputColumn<?>, JComponent> _columns = new TreeMap<>();
    private final WindowContext _windowContext;
    private final boolean _addShadowBorder;

    private final boolean _editable;

    public ColumnListTable(final Collection<? extends InputColumn<?>> columns,
            final AnalysisJobBuilder analysisJobBuilder, final boolean addShadowBorder,
            final WindowContext windowContext) {
        this(null, columns, analysisJobBuilder, addShadowBorder, true, windowContext);
    }

    public ColumnListTable(final Collection<? extends InputColumn<?>> columns,
            final AnalysisJobBuilder analysisJobBuilder, final boolean addShadowBorder, final boolean editable,
            final WindowContext windowContext) {
        this(null, columns, analysisJobBuilder, addShadowBorder, editable, windowContext);
    }

    public ColumnListTable(final Table table, final AnalysisJobBuilder analysisJobBuilder,
            final boolean addShadowBorder, final WindowContext windowContext) {
        this(table, null, analysisJobBuilder, addShadowBorder, true, windowContext);
    }

    public ColumnListTable(final Table table, final AnalysisJobBuilder analysisJobBuilder,
            final boolean addShadowBorder, final boolean editable, final WindowContext windowContext) {
        this(table, null, analysisJobBuilder, addShadowBorder, editable, windowContext);
    }

    private ColumnListTable(final Table table, final Collection<? extends InputColumn<?>> columns,
            final AnalysisJobBuilder analysisJobBuilder, final boolean addShadowBorder, final boolean editable,
            final WindowContext windowContext) {
        super();
        _table = table;
        _analysisJobBuilder = analysisJobBuilder;
        _addShadowBorder = addShadowBorder;
        _editable = editable;
        _windowContext = windowContext;

        setLayout(new BorderLayout());

        if (table != null) {
            final DCPanel headerPanel = new DCPanel();
            headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            final JLabel tableNameLabel = new JLabel(table.getQualifiedLabel(),
                    imageManager.getImageIcon(IconUtils.MODEL_COLUMN, IconUtils.ICON_SIZE_SMALL), JLabel.LEFT);
            tableNameLabel.setOpaque(false);
            tableNameLabel.setFont(WidgetUtils.FONT_HEADER1);
            headerPanel.add(tableNameLabel);

            if (_windowContext != null) {
                final JButton previewButton = WidgetFactory.createSmallButton(IconUtils.ACTION_PREVIEW);
                previewButton.setToolTipText("Preview table rows");
                previewButton.addActionListener(
                        new PreviewSourceDataActionListener(_windowContext, _analysisJobBuilder.getDatastore(),
                                _columns.keySet()));
                headerPanel.add(Box.createHorizontalStrut(4));
                headerPanel.add(previewButton);
            }

            if (_windowContext != null) {
                final JButton queryButton = WidgetFactory.createSmallButton(IconUtils.MODEL_QUERY);
                queryButton.setToolTipText("Ad-hoc query");
                queryButton.addActionListener(
                        new QueryActionListener(_windowContext, _analysisJobBuilder, _table, _columns.keySet()));
                headerPanel.add(Box.createHorizontalStrut(4));
                headerPanel.add(queryButton);
            }

            if (_editable) {
                final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE_DARK);
                removeButton.setToolTipText("Remove table from source");
                removeButton.addActionListener(e -> _analysisJobBuilder.removeSourceTable(_table));
                headerPanel.add(Box.createHorizontalStrut(4));
                headerPanel.add(removeButton);
            }

            add(headerPanel, BorderLayout.NORTH);
        }

        _columnTable = new DCTable(HEADERS_WITH_ACTION_COLUMN);
        _columnTable.setSortable(false);
        _columnTable.setColumnControlVisible(false);
        _columnTable.setRowHeight(IconUtils.ICON_SIZE_SMALL + 4);

        if (columns != null) {
            for (final InputColumn<?> column : columns) {
                addColumn(column, false);
            }
        }
        updateComponents();
    }

    private void updateComponents() {
        boolean hasPhysicalColumns = false;
        for (final InputColumn<?> column : _columns.keySet()) {
            if (column.isPhysicalColumn()) {
                hasPhysicalColumns = true;
                break;
            }
        }

        final String[] headers;
        if (hasPhysicalColumns && _editable) {
            headers = HEADERS_WITH_ACTION_COLUMN;
        } else {
            headers = HEADERS_WITHOUT_ACTIONS;
        }

        final TableModel model = new DefaultTableModel(headers, _columns.size());
        int i = 0;
        for (final Entry<InputColumn<?>, JComponent> entry : _columns.entrySet()) {
            final InputColumn<?> column = entry.getKey();
            final JComponent panel = entry.getValue();
            model.setValueAt(panel, i, 0);

            final Class<?> dataType = column.getDataType();
            final String dataTypeString = LabelUtils.getDataTypeLabel(dataType);
            model.setValueAt(dataTypeString, i, 1);

            if (column.isPhysicalColumn() && _editable) {
                final DCPanel buttonPanel = new DCPanel();
                buttonPanel.setLayout(new GridBagLayout());
                final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE_DARK);
                removeButton.setToolTipText("Remove column from source");
                removeButton.addActionListener(e -> _analysisJobBuilder.removeSourceColumn(column.getPhysicalColumn()));
                WidgetUtils.addToGridBag(removeButton, buttonPanel, 0, 0);
                model.setValueAt(buttonPanel, i, 2);
            }

            i++;
        }
        _columnTable.setModel(model);

        if (hasPhysicalColumns && _editable) {
            final TableColumnExt columnExt = _columnTable.getColumnExt(2);
            columnExt.setMinWidth(26);
            columnExt.setMaxWidth(80);
            columnExt.setPreferredWidth(30);
        }

        _columnTable.setRowHeight(DCTable.EDITABLE_TABLE_ROW_HEIGHT);

        final DCPanel tablePanel = _columnTable.toPanel();
        if (_addShadowBorder) {
            tablePanel.setBorder(new CompoundBorder(WidgetUtils.BORDER_SHADOW, WidgetUtils.BORDER_THIN));
        }
        add(tablePanel, BorderLayout.CENTER);
    }

    protected JComponent createComponentForColumn(final InputColumn<?> column) {
        if (column instanceof MutableInputColumn<?>) {
            final MutableInputColumn<?> mutableInputColumn = (MutableInputColumn<?>) column;

            return new MutableInputColumnListPanel(_analysisJobBuilder, mutableInputColumn, _columnTable);
        }

        final Icon icon = IconUtils.getColumnIcon(column, IconUtils.ICON_SIZE_MEDIUM);
        return new JLabel(column.getName(), icon, JLabel.LEFT);
    }

    public Table getTable() {
        return _table;
    }

    public void addColumn(final InputColumn<?> column) {
        addColumn(column, true);
    }

    public void addColumn(final InputColumn<?> column, final boolean updatePanel) {
        if (_columns.containsKey(column)) {
            return;
        }
        _columns.put(column, createComponentForColumn(column));
        if (updatePanel) {
            updateComponents();
        }
    }

    public void removeColumn(final InputColumn<?> column) {
        removeColumn(column, true);
    }

    public void removeColumn(final InputColumn<?> column, final boolean updatePanel) {
        if (!_columns.containsKey(column)) {
            return;
        }
        final JComponent panel = _columns.remove(column);
        if (panel instanceof Closeable) {
            FileHelper.safeClose(panel);
        }
        if (updatePanel) {
            updateComponents();
        }
    }

    public void setColumns(final List<? extends InputColumn<?>> columns) {
        final List<InputColumn<?>> copyOfOldList = new ArrayList<>(_columns.keySet());
        for (final InputColumn<?> column : copyOfOldList) {
            removeColumn(column, false);
        }

        assert _columns.isEmpty();
        _columns.clear();

        for (final InputColumn<?> column : columns) {
            addColumn(column, false);
        }

        updateComponents();
    }

    public int getColumnCount() {
        return _columns.size();
    }

    public boolean isEditable() {
        return _editable;
    }

}
