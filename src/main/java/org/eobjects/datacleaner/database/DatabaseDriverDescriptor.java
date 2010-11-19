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
package org.eobjects.datacleaner.database;

import java.io.Serializable;

/**
 * An object that provides information about a JDBC database, which will aid the
 * user in selecting correct driver classes, filling out the connection URL etc.
 * 
 * @author Kasper Sørensen
 */
public interface DatabaseDriverDescriptor extends Serializable, Comparable<DatabaseDriverDescriptor> {

	public String getDisplayName();

	public String getIconImagePath();

	public String getDriverClassName();

	/**
	 * @return an array of URLs for the files needed to download to use this
	 *         driver. Typically this will just be a single file (a driver JAR),
	 *         but in some cases the driver has additional dependencies, which
	 *         also needs to be downloaded.
	 */
	public String[] getDownloadUrls();

	public String[] getConnectionUrlTemplates();
}
