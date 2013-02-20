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
package org.eobjects.datacleaner.monitor.shared;

import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.eobjects.datacleaner.monitor.shared.model.ColumnIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.SchemaIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;
import org.eobjects.datacleaner.monitor.shared.model.TableIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * A service for exploring datastores
 */
@RemoteServiceRelativePath("datastoreService")
public interface DatastoreService extends RemoteService {

    @RolesAllowed(SecurityRoles.VIEWER)
    public List<DatastoreIdentifier> getAvailableDatastores(TenantIdentifier tenant);

    @RolesAllowed(SecurityRoles.ADMIN)
    public SchemaIdentifier getDefaultSchema(TenantIdentifier tenant, DatastoreIdentifier datastore);

    @RolesAllowed(SecurityRoles.ADMIN)
    public List<SchemaIdentifier> getSchemas(TenantIdentifier tenant, DatastoreIdentifier datastore);

    @RolesAllowed(SecurityRoles.ADMIN)
    public List<TableIdentifier> getTables(TenantIdentifier tenant, SchemaIdentifier schema);

    @RolesAllowed(SecurityRoles.ADMIN)
    public List<ColumnIdentifier> getColumns(TenantIdentifier tenant, TableIdentifier table);
}
