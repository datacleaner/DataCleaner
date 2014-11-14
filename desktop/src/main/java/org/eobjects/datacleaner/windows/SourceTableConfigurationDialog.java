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
package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.SourceColumnChangeListener;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.ColumnListTable;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.NeopostToolbarButton;

/**
 * Dialog used for configuring a source table of a job.
 */
public class SourceTableConfigurationDialog extends AbstractDialog implements SourceColumnChangeListener {

    private static final long serialVersionUID = 1L;
    private final Table _table;
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final ColumnListTable _columnListTable;

    public SourceTableConfigurationDialog(WindowContext windowContext, AnalysisJobBuilder analysisJobBuilder,
            Table table) {
        super(windowContext, ImageManager.get().getImage("images/window/banner-tabledef.png"));

        _table = table;
        _analysisJobBuilder = analysisJobBuilder;

        _columnListTable = new ColumnListTable(table, analysisJobBuilder, true, windowContext);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        _analysisJobBuilder.getSourceColumnListeners().add(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        _analysisJobBuilder.getSourceColumnListeners().remove(this);
    }

    @Override
    public String getWindowTitle() {
        return _table.getName();
    }

    @Override
    protected String getBannerTitle() {
        return _table.getName();
    }

    @Override
    protected int getDialogWidth() {
        return 500;
    }

    @Override
    protected JComponent getDialogContent() {
        final List<MetaModelInputColumn> columns = _analysisJobBuilder.getSourceColumnsOfTable(_table);
        for (MetaModelInputColumn metaModelInputColumn : columns) {
            _columnListTable.addColumn(metaModelInputColumn);
        }

        final JButton closeButton = WidgetFactory.createButton("Close", "images/actions/save.png");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SourceTableConfigurationDialog.this.dispose();
            }
        });

        final JToolBar toolBar = WidgetFactory.createToolBar();
        toolBar.add(new NeopostToolbarButton());
        toolBar.add(WidgetFactory.createToolBarSeparator());
        toolBar.add(closeButton);

        final DCPanel toolBarPanel = new DCPanel(WidgetUtils.BG_COLOR_DARKEST, WidgetUtils.BG_COLOR_DARKEST);
        toolBarPanel.setLayout(new BorderLayout());
        toolBarPanel.add(toolBar, BorderLayout.CENTER);

        final DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHT);
        panel.setLayout(new BorderLayout());
        panel.add(_columnListTable, BorderLayout.CENTER);
        panel.add(toolBarPanel, BorderLayout.SOUTH);
        return panel;
    }

    @Override
    public void onAdd(InputColumn<?> column) {
        Column physicalColumn = column.getPhysicalColumn();
        if (physicalColumn != null) {
            if (physicalColumn.getTable() == _table) {
                _columnListTable.addColumn(column);
            }
        }
    }

    @Override
    public void onRemove(InputColumn<?> column) {
        _columnListTable.removeColumn(column);
    }

}
