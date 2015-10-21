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
package org.datacleaner.connection;

import org.datacleaner.windows.AbstractDatastoreDialog;

/**
 * A descriptor class for a datastore types in DataCleaner.
 *
 */
public interface DatastoreDescriptor {

    public String getName();

    public String getDescription();

    public Class<? extends Datastore> getDatastoreClass();

    public Class<? extends AbstractDatastoreDialog<? extends Datastore>> getDatastoreDialogClass();

    public String getIconPath();

    /**
     * Determines if the datastore is promoted, fx: should have its own icon in
     * the @{link DatastoreManagementPanel} or be hidden in the dropdown menu.
     */
    public boolean isPromoted();

    public boolean isUpdatable();

}