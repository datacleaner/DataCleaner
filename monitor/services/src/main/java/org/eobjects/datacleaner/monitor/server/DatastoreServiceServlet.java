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
package org.eobjects.datacleaner.monitor.server;

import java.util.List;

import javax.servlet.ServletException;

import org.eobjects.datacleaner.monitor.shared.DatastoreService;
import org.eobjects.datacleaner.monitor.shared.model.ColumnIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.SchemaIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TableIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

public class DatastoreServiceServlet extends SecureGwtServlet implements DatastoreService {

    private static final long serialVersionUID = 1L;

    private DatastoreService _delegate;

    @Override
    public void init() throws ServletException {
        super.init();

        if (_delegate == null) {
            WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
            DatastoreService delegate = applicationContext.getBean(DatastoreService.class);
            if (delegate == null) {
                throw new ServletException("No delegate found in application context!");
            }
            _delegate = delegate;
        }
    }

    @Override
    public List<DatastoreIdentifier> getAvailableDatastores(TenantIdentifier tenant) {
        return _delegate.getAvailableDatastores(tenant);
    }

    @Override
    public SchemaIdentifier getDefaultSchema(TenantIdentifier tenant, DatastoreIdentifier datastore) {
        return _delegate.getDefaultSchema(tenant, datastore);
    }

    @Override
    public List<SchemaIdentifier> getSchemas(TenantIdentifier tenant, DatastoreIdentifier datastore) {
        return _delegate.getSchemas(tenant, datastore);
    }

    @Override
    public List<TableIdentifier> getTables(TenantIdentifier tenant, SchemaIdentifier schema) {
        return _delegate.getTables(tenant, schema);
    }

    @Override
    public List<ColumnIdentifier> getColumns(TenantIdentifier tenant, TableIdentifier table) {
        return _delegate.getColumns(tenant, table);
    }

}
