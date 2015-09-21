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
import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.schema.Column;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;

/**
 * Panel that displays {@link OutputDataStream}s published by the component.
 */
public final class OutputDataStreamsViewer extends DCPanel {

    private static final long serialVersionUID = 1L;

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final ComponentBuilder _componentBuilder;
    private final  WindowContext _windowContext;

    public OutputDataStreamsViewer(AnalysisJobBuilder analysisJobBuilder, ComponentBuilder componentBuilder, WindowContext windowsContext) {
        super();
        _analysisJobBuilder = analysisJobBuilder;
        _componentBuilder = componentBuilder;
        _windowContext = windowsContext;
        refresh();
    }

    public void refresh() {
        removeAll();

        for (OutputDataStream outputDataStream : _componentBuilder.getOutputDataStreams()) {
            List<InputColumn<?>> inputColumns = new ArrayList<>();
            for (Column column : outputDataStream.getTable().getColumns()) {
                MetaModelInputColumn inputColumn = new MetaModelInputColumn(column);
                inputColumns.add(inputColumn);
            }
            final ColumnListTable columnListTable = new ColumnListTable(inputColumns, _analysisJobBuilder,
                    true, false, _windowContext);

            DCPanel outputDataStreamPanel = new DCPanel();
            outputDataStreamPanel.add(columnListTable, BorderLayout.AFTER_LAST_LINE);
            add(outputDataStreamPanel, BorderLayout.AFTER_LAST_LINE);
        }
    }

}
