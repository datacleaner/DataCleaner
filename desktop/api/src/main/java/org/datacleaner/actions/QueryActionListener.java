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
package org.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import org.datacleaner.connection.Datastore;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.windows.QueryWindow;
import org.apache.metamodel.query.FromClause;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.HasName;

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
        
        String fromClause = _table.getQualifiedLabel();
        if (fromClause.contains(" ")) {
            fromClause = _table.getName();
        }
        
        initialQuery.append("\nFROM " + fromClause + " a");
        
        final QueryWindow window = new QueryWindow(_windowContext, datastore, initialQuery.toString());
        window.open();
    }

}
