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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.InputColumn;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.SourceColumnChangeListener;
import org.datacleaner.panels.ColumnListTable;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;

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

        final JButton closeButton = WidgetFactory.createPrimaryButton("Close", IconUtils.ACTION_CLOSE);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SourceTableConfigurationDialog.this.dispose();
            }
        });

        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        panel.setLayout(new BorderLayout());
        panel.add(_columnListTable, BorderLayout.CENTER);
        panel.add(DCPanel.flow(Alignment.CENTER, closeButton), BorderLayout.SOUTH);
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

    @Override
    protected boolean isWindowResizable() {
        return true;
    }
}
