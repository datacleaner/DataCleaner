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
package org.datacleaner.monitor.server;

import java.util.List;

import javax.servlet.ServletException;

import org.datacleaner.monitor.shared.DatastoreService;
import org.datacleaner.monitor.shared.model.ColumnIdentifier;
import org.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.datacleaner.monitor.shared.model.SchemaIdentifier;
import org.datacleaner.monitor.shared.model.TableIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

public class DatastoreServiceServlet extends SecureGwtServlet implements DatastoreService {

    private static final long serialVersionUID = 1L;

    private DatastoreService _delegate;

    @Override
    public void init() throws ServletException {
        super.init();

        if (_delegate == null) {
            final WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
            final DatastoreService delegate = applicationContext.getBean(DatastoreService.class);
            if (delegate == null) {
                throw new ServletException("No delegate found in application context!");
            }
            _delegate = delegate;
        }
    }

    @Override
    public List<DatastoreIdentifier> getAvailableDatastores(final TenantIdentifier tenant) {
        return _delegate.getAvailableDatastores(tenant);
    }

    @Override
    public SchemaIdentifier getDefaultSchema(final TenantIdentifier tenant, final DatastoreIdentifier datastore) {
        return _delegate.getDefaultSchema(tenant, datastore);
    }

    @Override
    public List<SchemaIdentifier> getSchemas(final TenantIdentifier tenant, final DatastoreIdentifier datastore) {
        return _delegate.getSchemas(tenant, datastore);
    }

    @Override
    public List<TableIdentifier> getTables(final TenantIdentifier tenant, final SchemaIdentifier schema) {
        return _delegate.getTables(tenant, schema);
    }

    @Override
    public List<ColumnIdentifier> getColumns(final TenantIdentifier tenant, final TableIdentifier table) {
        return _delegate.getColumns(tenant, table);
    }

}
