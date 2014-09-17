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
package org.eobjects.datacleaner.output.datastore;

import org.eobjects.analyzer.connection.Datastore;

/**
 * Delegate/callback interface for picking up the event that a datastore has
 * been created. Typically the UI layer will implement this interface in order
 * to put the datastore into a list or a catalog of datastores.
 * 
 * If using the DatastoreOutputWriterFactory for other purposes this interface
 * can also be implemented to handle the datastore produced when the
 * outputwriter is being closed.
 * 
 * @see DatastoreOutputWriterFactory
 * 
 * @author Kasper Sørensen
 */
public interface DatastoreCreationDelegate {

	public void createDatastore(Datastore datastore);
}
