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

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.apache.metamodel.schema.Column;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Panel that displays {@link OutputDataStream}s published by the component.
 */
public final class OutputDataStreamsViewer extends DCPanel {

    private static final long serialVersionUID = 1L;

    private final ComponentBuilder _componentBuilder;

    public OutputDataStreamsViewer(ComponentBuilder componentBuilder) {
        super();
        _componentBuilder = componentBuilder;
        
        setLayout(new VerticalLayout(4));
    }

    public void refresh() {
        removeAll();
        
        setEnabled(false);
        for (OutputDataStream outputDataStream : _componentBuilder.getOutputDataStreams()) {
            setEnabled(true);
            
            List<InputColumn<?>> inputColumns = new ArrayList<>();
            for (Column column : outputDataStream.getTable().getColumns()) {
                MetaModelInputColumn inputColumn = new MetaModelInputColumn(column);
                inputColumns.add(inputColumn);
            }
            final JLabel tableNameLabel = new JLabel(outputDataStream.getName(), ImageManager.get().getImageIcon(
                    IconUtils.MODEL_COLUMN, IconUtils.ICON_SIZE_SMALL), JLabel.LEFT);
            tableNameLabel.setOpaque(false);
            tableNameLabel.setFont(WidgetUtils.FONT_HEADER1);
            tableNameLabel.setBorder(new EmptyBorder(5, 5, 0, 5));
            final ColumnListTable columnListTable = new ColumnListTable(inputColumns, _componentBuilder.getAnalysisJobBuilder(),
                    true, false, null);
            columnListTable.setBorder(new EmptyBorder(0, 5, 5, 5));

            add(tableNameLabel);
            add(columnListTable);
        }
    }

}
