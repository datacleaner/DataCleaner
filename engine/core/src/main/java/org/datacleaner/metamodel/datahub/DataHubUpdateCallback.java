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

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.AbstractUpdateCallback;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.create.TableCreationBuilder;
import org.apache.metamodel.delete.RowDeletionBuilder;
import org.apache.metamodel.drop.TableDropBuilder;
import org.apache.metamodel.insert.RowInsertionBuilder;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.RowUpdationBuilder;
import org.datacleaner.metamodel.datahub.update.SourceRecordIdentifier;
import org.datacleaner.metamodel.datahub.update.UpdateData;

public class DataHubUpdateCallback extends AbstractUpdateCallback implements UpdateCallback, Closeable {

    public static final int INSERT_BATCH_SIZE = 100;
    public static final int DELETE_BATCH_SIZE = 100;
    private final DataHubDataContext _dataContext;
    private List<UpdateData> _pendingUpdates;
    private List<SourceRecordIdentifier> _pendingSourceDeletes;
    private List<String> _pendingGoldenRecordDeletes;

    /**
     * Constructor. Initializes pending updates and deletes to be empty.
     * 
     * @param dataContext
     *            The data context.
     */
    public DataHubUpdateCallback(DataHubDataContext dataContext) {
        super(dataContext);
        _dataContext = dataContext;
        _pendingUpdates = null;
        _pendingSourceDeletes = null;
        _pendingGoldenRecordDeletes = null;
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
    public RowDeletionBuilder deleteFrom(Table table) throws IllegalArgumentException, IllegalStateException,
            UnsupportedOperationException {
        return new DataHubDeleteBuilder(this,table);
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

    /**
     * Invokes update REST method on DataHub, using the updates collected by the {@link DataHubUpdateBuilder}.
     * The incoming updates are buffered and send to DataHub in batches of size <code>INSERT_BATCH_SIZE</code>.
     * @param updateData Contains the records and fields to be updated.
     */
    public void executeUpdate(UpdateData updateData) {
        if (_pendingUpdates == null) {
            _pendingUpdates = new ArrayList<UpdateData>();
        }
        _pendingUpdates.add(updateData);
        
        if (_pendingUpdates.size() >= INSERT_BATCH_SIZE) {
            flushUpdates();
        }

    }

    /**
     * Deletes a golden record by its golden record id. The deletes are buffered
     * and executed in batches.
     * 
     * @param grId
     *            The golden record id to delete.
     */
    public void executeDeleteGoldenRecord(String grId) {
        if (_pendingGoldenRecordDeletes == null) {
            _pendingGoldenRecordDeletes = new ArrayList<String>();
        }
        _pendingGoldenRecordDeletes.add(grId);
        
        if (_pendingGoldenRecordDeletes.size() >= DELETE_BATCH_SIZE) {
            flushGoldenRecordDeletes();
        }
    }

    /**
     * Delete a DataHub source record. The deletes are buffered and sent to
     * DataHub in batches.
     * 
     * @param source
     *            The name of the source system
     * @param id
     *            The source record identifier.
     * @param recordType
     *            The record type.
     */
    public void executeDeleteSourceRecord(String source, String id, String recordType) {
        if (_pendingSourceDeletes == null) {
            _pendingSourceDeletes = new ArrayList<SourceRecordIdentifier>();
        }
        _pendingSourceDeletes.add(new SourceRecordIdentifier(source, id, null, recordType));

        if (_pendingSourceDeletes.size() >= DELETE_BATCH_SIZE) {
            flushSourceDeletes();
        }
    }
    
    /**
     * Closes the callback. All remaining updates and deletes are flushed.
     */
    @Override
    public void close() {
        flushUpdates();
        flushSourceDeletes();
        flushGoldenRecordDeletes();
    }

    private void flushUpdates() {
        if (_pendingUpdates == null || _pendingUpdates.isEmpty()) {
            return;
        }
        _dataContext.executeUpdates(_pendingUpdates);
        _pendingUpdates = null;
    }

    private void flushSourceDeletes() {
        if (_pendingSourceDeletes == null || _pendingSourceDeletes.isEmpty()) {
            return;
        }
        _dataContext.executeSourceDelete(_pendingSourceDeletes);
        _pendingSourceDeletes = null;
    }
    
    private void flushGoldenRecordDeletes() {
        if (_pendingGoldenRecordDeletes == null || _pendingGoldenRecordDeletes.isEmpty()) {
            return;
        }
        _dataContext.executeGoldenRecordDelete(_pendingGoldenRecordDeletes);
        _pendingGoldenRecordDeletes = null;
    }

}
