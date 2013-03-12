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
package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.windows.QueryWindow;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.HasName;

/**
 * Action listener that displays a query window
 */
public class QueryActionListener implements ActionListener {

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final Table _table;
    private final WindowContext _windowContext;
    private final Collection<? extends HasName> _columns;
    
    public QueryActionListener(WindowContext windowContext, AnalysisJobBuilder analysisJobBuilder, Table table) {
        this(windowContext, analysisJobBuilder, table, null);
    }

    public QueryActionListener(WindowContext windowContext, AnalysisJobBuilder analysisJobBuilder, Table table, Collection<? extends HasName> columns) {
        _windowContext = windowContext;
        _analysisJobBuilder = analysisJobBuilder;
        _table = table;
        _columns = columns;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Datastore datastore = _analysisJobBuilder.getDatastore();
        
        final StringBuilder initialQuery = new StringBuilder("SELECT ");
        if (_columns == null || _columns.isEmpty()) {
            initialQuery.append("*");
        } else {
            boolean first = true;
            for (HasName column : _columns) {
                if (!first) {
                    initialQuery.append(", ");
                }
                initialQuery.append("a.");
                initialQuery.append(column.getName());
                first = false;
            }
        }
        
        initialQuery.append("\nFROM " + _table.getQualifiedLabel() + " a");
        
        final QueryWindow window = new QueryWindow(_windowContext, datastore, initialQuery.toString());
        window.open();
    }

}
