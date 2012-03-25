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
package org.eobjects.datacleaner.widgets.database;

import javax.swing.JComponent;

import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.datacleaner.database.DatabaseDriverDescriptor;

/**
 * Describes an interface for presenters/views of a database connection. The
 * responsibility of this presenter is to allow the user to fill in JDBC URL,
 * username and password
 * 
 * @author kasper
 * 
 */
public interface DatabaseConnectionPresenter {

	public void initialize(JdbcDatastore datastore);

	public JComponent getWidget();

	public String getJdbcUrl();

	public String getUsername();

	public String getPassword();

	public void setSelectedDatabaseDriver(DatabaseDriverDescriptor driver);
}
