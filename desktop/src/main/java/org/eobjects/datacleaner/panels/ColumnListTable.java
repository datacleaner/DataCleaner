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
package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.util.LabelUtils;
import org.eobjects.datacleaner.actions.PreviewSourceDataActionListener;
import org.eobjects.datacleaner.actions.QueryActionListener;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.FileHelper;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Panel that displays columns in a table-layout. Used for both the "Source" tab
 * and for transformer panels in DataCleaner desktop application.
 */
public final class ColumnListTable extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final String[] headers = new String[] { "Name", "Type", "" };

    private final ImageManager imageManager = ImageManager.get();
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final Table _table;
    private final DCTable _columnTable;

    private final SortedMap<InputColumn<?>, JComponent> _columns = new TreeMap<InputColumn<?>, JComponent>();

    private final WindowContext _windowContext;

    public ColumnListTable(Collection<? extends InputColumn<?>> columns, AnalysisJobBuilder analysisJobBuilder,
            boolean addShadowBorder, WindowContext windowContext) {
        this(null, columns, analysisJobBuilder, addShadowBorder, windowContext);
    }

    public ColumnListTable(Table table, AnalysisJobBuilder analysisJobBuilder, boolean addShadowBorder,
            WindowContext windowContext) {
        this(table, null, analysisJobBuilder, addShadowBorder, windowContext);
    }

    private ColumnListTable(Table table, Collection<? extends InputColumn<?>> columns,
            AnalysisJobBuilder analysisJobBuilder, boolean addShadowBorder, WindowContext windowContext) {
        super();
        _table = table;
        _analysisJobBuilder = analysisJobBuilder;
        _windowContext = windowContext;

        setLayout(new BorderLayout());

        if (table != null) {
            final DCPanel headerPanel = new DCPanel();
            headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            final JLabel tableNameLabel = new JLabel(table.getQualifiedLabel(), imageManager.getImageIcon(
                    IconUtils.MODEL_COLUMN, IconUtils.ICON_SIZE_SMALL), JLabel.LEFT);
            tableNameLabel.setOpaque(false);
            tableNameLabel.setFont(WidgetUtils.FONT_HEADER1);

            final JButton previewButton = WidgetFactory.createSmallButton("images/actions/preview_data.png");
            previewButton.setToolTipText("Preview table rows");
            previewButton.addActionListener(new PreviewSourceDataActionListener(_windowContext, _analysisJobBuilder
                    .getDatastore(), _columns.keySet()));

            final JButton queryButton = WidgetFactory.createSmallButton(IconUtils.MODEL_QUERY);
            queryButton.setToolTipText("Ad-hoc query");
            queryButton.addActionListener(new QueryActionListener(_windowContext, _analysisJobBuilder, _table, _columns
                    .keySet()));

            final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE);
            removeButton.setToolTipText("Remove table from source");
            removeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Column[] cols = _table.getColumns();
                    for (Column col : cols) {
                        _analysisJobBuilder.removeSourceColumn(col);
                    }
                }
            });

            headerPanel.add(tableNameLabel);
            headerPanel.add(Box.createHorizontalStrut(4));
            headerPanel.add(previewButton);
            headerPanel.add(Box.createHorizontalStrut(4));
            headerPanel.add(queryButton);
            headerPanel.add(Box.createHorizontalStrut(4));
            headerPanel.add(removeButton);
            add(headerPanel, BorderLayout.NORTH);
        }

        _columnTable = new DCTable(headers);
        _columnTable.setSortable(false);
        _columnTable.setColumnControlVisible(false);
        _columnTable.setRowHeight(IconUtils.ICON_SIZE_SMALL + 4);

        JPanel tablePanel = _columnTable.toPanel();

        if (addShadowBorder) {
            tablePanel.setBorder(new CompoundBorder(WidgetUtils.BORDER_SHADOW, WidgetUtils.BORDER_THIN));
        }
        add(tablePanel, BorderLayout.CENTER);

        if (columns != null) {
            for (InputColumn<?> column : columns) {
                addColumn(column, false);
            }
        }
        updateComponents();
    }

    private void updateComponents() {
        TableModel model = new DefaultTableModel(headers, _columns.size());
        int i = 0;
        for (final Entry<InputColumn<?>, JComponent> entry : _columns.entrySet()) {
            final InputColumn<?> column = entry.getKey();
            final JComponent panel = entry.getValue();
            model.setValueAt(panel, i, 0);

            final Class<?> dataType = column.getDataType();
            final String dataTypeString = LabelUtils.getDataTypeLabel(dataType);
            model.setValueAt(dataTypeString, i, 1);

            JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE);
            removeButton.setToolTipText("Remove column from source");
            removeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    _analysisJobBuilder.removeSourceColumn(column.getPhysicalColumn());
                }
            });

            DCPanel buttonPanel = new DCPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));
            // buttonPanel.add(transformButton);
            // buttonPanel.add(filterButton);
            if (column.isPhysicalColumn()) {
                buttonPanel.add(removeButton);
            }

            model.setValueAt(buttonPanel, i, 2);
            i++;
        }
        _columnTable.setModel(model);

        TableColumnExt columnExt = _columnTable.getColumnExt(2);
        columnExt.setMinWidth(26);
        columnExt.setMaxWidth(80);
        columnExt.setPreferredWidth(30);
    }

    protected JComponent createComponentForColumn(InputColumn<?> column) {
        if (column instanceof MutableInputColumn<?>) {
            final MutableInputColumn<?> mutableInputColumn = (MutableInputColumn<?>) column;

            final MutableInputColumnListPanel panel = new MutableInputColumnListPanel(_analysisJobBuilder,
                    mutableInputColumn);

            return panel;
        }

        final Icon icon = IconUtils.getColumnIcon(column, IconUtils.ICON_SIZE_MEDIUM);
        return new JLabel(column.getName(), icon, JLabel.LEFT);
    }

    public Table getTable() {
        return _table;
    }

    public void addColumn(InputColumn<?> column) {
        addColumn(column, true);
    }

    public void addColumn(InputColumn<?> column, boolean updatePanel) {
        if (_columns.containsKey(column)) {
            return;
        }
        _columns.put(column, createComponentForColumn(column));
        if (updatePanel) {
            updateComponents();
        }
    }

    public void removeColumn(InputColumn<?> column) {
        removeColumn(column, true);
    }

    public void removeColumn(InputColumn<?> column, boolean updatePanel) {
        if (!_columns.containsKey(column)) {
            return;
        }
        JComponent panel = _columns.remove(column);
        if (panel instanceof Closeable) {
            FileHelper.safeClose(panel);
        }
        if (updatePanel) {
            updateComponents();
        }
    }

    public void setColumns(List<? extends InputColumn<?>> columns) {
        final List<InputColumn<?>> copyOfOldList = new ArrayList<InputColumn<?>>(_columns.keySet());
        for (InputColumn<?> column : copyOfOldList) {
            removeColumn(column, false);
        }

        assert _columns.isEmpty();
        _columns.clear();

        for (InputColumn<?> column : columns) {
            addColumn(column, false);
        }

        updateComponents();
    }

    public int getColumnCount() {
        return _columns.size();
    }
}
