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
package org.datacleaner.metamodel.datahub;

import org.apache.metamodel.AbstractUpdateCallback;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.create.TableCreationBuilder;
import org.apache.metamodel.delete.RowDeletionBuilder;
import org.apache.metamodel.drop.TableDropBuilder;
import org.apache.metamodel.insert.RowInsertionBuilder;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.RowUpdationBuilder;

public class DataHubUpdateCallback extends AbstractUpdateCallback implements UpdateCallback {

    private final DataHubDataContext _dataContext;
    private DataHubConnection _connection;

    public DataHubUpdateCallback(DataHubDataContext dataContext) {
        super(dataContext);
        _dataContext = dataContext;
    }

    protected final DataHubConnection getConnection() {
        if (_connection == null) {
            _connection = _dataContext.getConnection();
        }
        return _connection;
    }

    @Override
    public TableCreationBuilder createTable(Schema arg0, String arg1) throws IllegalArgumentException,
            IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public TableDropBuilder dropTable(Table arg0) throws IllegalArgumentException, IllegalStateException,
            UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDropTableSupported() {
        return false;
    }

    @Override
    public RowInsertionBuilder insertInto(Table arg0) throws IllegalArgumentException, IllegalStateException,
            UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public RowDeletionBuilder deleteFrom(Table arg0) throws IllegalArgumentException, IllegalStateException,
            UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDeleteSupported() {
        return false;
    }

    @Override
    public boolean isUpdateSupported() {
        return true;
    }

    @Override
    public RowUpdationBuilder update(Table table) throws IllegalArgumentException, IllegalStateException,
            UnsupportedOperationException {
        return new DataHubUpdateBuilder(this, table);
    }

    public void executeUpdate(String query) {
        // TODO Auto-generated method stub
        
    }

}
