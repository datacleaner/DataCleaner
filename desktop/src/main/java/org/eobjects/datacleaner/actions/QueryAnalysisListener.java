/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.windows.QueryWindow;
import org.eobjects.metamodel.schema.Table;

/**
 * Action listener that displays a query window
 */
public class QueryAnalysisListener implements ActionListener {

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final Table _table;
    private final WindowContext _windowContext;

    public QueryAnalysisListener(WindowContext windowContext, AnalysisJobBuilder analysisJobBuilder, Table table) {
        _windowContext = windowContext;
        _analysisJobBuilder = analysisJobBuilder;
        _table = table;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Datastore datastore = _analysisJobBuilder.getDatastore();
        final String initialQuery = "SELECT *\nFROM " + _table.getQualifiedLabel();
        
        final QueryWindow window = new QueryWindow(_windowContext, datastore, initialQuery);
        window.open();
    }

}
