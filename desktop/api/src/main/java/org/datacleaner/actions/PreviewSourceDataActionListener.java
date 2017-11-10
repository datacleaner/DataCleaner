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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.InputColumn;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.windows.DataSetWindow;

public class PreviewSourceDataActionListener implements ActionListener {

    private static final int PAGE_SIZE = 35;
    private final Datastore _datastore;
    private final List<Column> _columns;
    private final Collection<? extends InputColumn<?>> _inputColumns;
    private final WindowContext _windowContext;

    public PreviewSourceDataActionListener(final WindowContext windowContext, final Datastore datastore,
            final List<Column> columns) {
        _windowContext = windowContext;
        _datastore = datastore;
        _columns = columns;
        _inputColumns = null;
    }

    public PreviewSourceDataActionListener(final WindowContext windowContext, final Datastore datastore,
            final Table table) {
        this(windowContext, datastore, table.getColumns());
    }

    public PreviewSourceDataActionListener(final WindowContext windowContext, final Datastore datastore,
            final Collection<? extends InputColumn<?>> inputColumns) {
        _windowContext = windowContext;
        _datastore = datastore;
        _inputColumns = inputColumns;
        _columns = null;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        List<Column> columns = _columns;
        if (_columns == null) {
            final List<Column> cols = new ArrayList<>();
            for (final InputColumn<?> col : _inputColumns) {
                if (col.isPhysicalColumn()) {
                    cols.add(col.getPhysicalColumn());
                }
            }
            columns = cols;
        }

        if (columns.isEmpty()) {
            throw new IllegalStateException("No columns found - could not determine which columns to query");
        }

        try (DatastoreConnection con = _datastore.openConnection()) {
            final DataContext dc = con.getDataContext();
            final Query q = dc.query().from(columns.get(0).getTable()).select(columns).toQuery();

            final DataSetWindow window = new DataSetWindow(q, dc, PAGE_SIZE, _windowContext);
            window.open();
        }
    }
}
