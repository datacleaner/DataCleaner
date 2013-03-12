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
package org.eobjects.datacleaner.monitor.shared;

import java.util.List;

import org.eobjects.datacleaner.monitor.shared.model.ColumnIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.SchemaIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TableIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async variant of {@link DatastoreService}
 */
public interface DatastoreServiceAsync {

    public void getAvailableDatastores(TenantIdentifier tenant, AsyncCallback<List<DatastoreIdentifier>> callback);

    public void getDefaultSchema(TenantIdentifier tenant, DatastoreIdentifier datastore,
            AsyncCallback<SchemaIdentifier> callback);

    public void getSchemas(TenantIdentifier tenant, DatastoreIdentifier datastore,
            AsyncCallback<List<SchemaIdentifier>> callback);

    public void getTables(TenantIdentifier tenant, SchemaIdentifier schema,
            AsyncCallback<List<TableIdentifier>> callback);

    public void getColumns(TenantIdentifier tenant, TableIdentifier table,
            AsyncCallback<List<ColumnIdentifier>> callback);
}
