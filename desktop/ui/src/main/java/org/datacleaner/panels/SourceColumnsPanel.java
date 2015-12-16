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

import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.border.EmptyBorder;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.InputColumn;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.SourceColumnChangeListener;
import org.datacleaner.panels.maxrows.MaxRowsFilterShortcutPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Panel that presents the source columns of a job.
 */
public final class SourceColumnsPanel extends DCPanel implements SourceColumnChangeListener {

    private static final long serialVersionUID = 1L;

    private final List<ColumnListTable> _sourceColumnTables = new ArrayList<ColumnListTable>();
    private final DCLabel _hintLabel;
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final MaxRowsFilterShortcutPanel _maxRowsFilterShortcutPanel;
    private final WindowContext _windowContext;

    public SourceColumnsPanel(AnalysisJobBuilder analysisJobBuilder, WindowContext windowContext) {
        super(ImageManager.get().getImage("images/window/source-tab-background.png"), 0, 100);
        _analysisJobBuilder = analysisJobBuilder;
        _windowContext = windowContext;

        _maxRowsFilterShortcutPanel = createMaxRowsFilterShortcutPanel();

        _hintLabel = DCLabel.darkMultiLine("Please select the source columns of your job in the tree to the left.\n\n"
                + "Source columns define where to retrieve the input of your analysis.");
        _hintLabel.setFont(WidgetUtils.FONT_HEADER2);
        _hintLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        _hintLabel.setIconTextGap(20);
        _hintLabel.setIcon(ImageManager.get().getImageIcon(IconUtils.MODEL_COLUMN));

        setBorder(WidgetUtils.BORDER_EMPTY);
        setLayout(new VerticalLayout(4));

        add(_maxRowsFilterShortcutPanel);
        add(_hintLabel);
        add(Box.createVerticalStrut(10));

        final List<MetaModelInputColumn> sourceColumns = analysisJobBuilder.getSourceColumns();
        for (InputColumn<?> column : sourceColumns) {
            onAdd(column);
        }
        analysisJobBuilder.addSourceColumnChangeListener(this);
    }

    private MaxRowsFilterShortcutPanel createMaxRowsFilterShortcutPanel() {
        MaxRowsFilterShortcutPanel maxRowsFilterShortcutPanel = null;
        List<FilterComponentBuilder<?, ?>> filterJobBuilders = _analysisJobBuilder.getFilterComponentBuilders();
        for (FilterComponentBuilder<?, ?> filterJobBuilder : filterJobBuilders) {
            if (MaxRowsFilterShortcutPanel.isFilter(filterJobBuilder)) {
                maxRowsFilterShortcutPanel = new MaxRowsFilterShortcutPanel(_analysisJobBuilder, filterJobBuilder);
                break;
            }
        }
        if (maxRowsFilterShortcutPanel == null) {
            maxRowsFilterShortcutPanel = new MaxRowsFilterShortcutPanel(_analysisJobBuilder);
        }
        maxRowsFilterShortcutPanel.setEnabled(false);
        return maxRowsFilterShortcutPanel;
    }

    @Override
    public void onAdd(InputColumn<?> sourceColumn) {
        _hintLabel.setVisible(false);
        _maxRowsFilterShortcutPanel.setEnabled(true);

        Column column = sourceColumn.getPhysicalColumn();
        Table table = column.getTable();

        ColumnListTable sourceColumnTable = getColumnListTable(table);
        sourceColumnTable.addColumn(sourceColumn);
    }

    @Override
    public void onRemove(InputColumn<?> sourceColumn) {
        Column column = sourceColumn.getPhysicalColumn();
        Table table = column.getTable();
        ColumnListTable sourceColumnTable = getColumnListTable(table);
        sourceColumnTable.removeColumn(sourceColumn);
        if (sourceColumnTable.getColumnCount() == 0) {
            this.remove(sourceColumnTable);
            _sourceColumnTables.remove(sourceColumnTable);

            if (_analysisJobBuilder.getSourceColumns().isEmpty()) {
                _hintLabel.setVisible(true);
                _maxRowsFilterShortcutPanel.setEnabled(false);
            }

            // force UI update because sometimes the removed panel doesn't go
            // away automatically
            updateUI();
        }
    }

    private ColumnListTable getColumnListTable(Table table) {
        ColumnListTable sourceColumnTable = null;
        for (ColumnListTable sct : _sourceColumnTables) {
            if (sct.getTable() == table) {
                sourceColumnTable = sct;
                break;
            }
        }

        if (sourceColumnTable == null) {
            sourceColumnTable = new ColumnListTable(table, _analysisJobBuilder, true, _windowContext);
            this.add(sourceColumnTable);
            _sourceColumnTables.add(sourceColumnTable);
            updateUI();
        }
        return sourceColumnTable;
    }

    @Override
    public void removeNotify() {
        _analysisJobBuilder.removeSourceColumnChangeListener(this);
        super.removeNotify();
    }

    @Override
    public void addNotify() {
        _analysisJobBuilder.addSourceColumnChangeListener(this);
        super.addNotify();
    }

    public MaxRowsFilterShortcutPanel getMaxRowsFilterShortcutPanel() {
        return _maxRowsFilterShortcutPanel;
    }
}
