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
package org.eobjects.datacleaner.monitor.server.controllers;

import java.io.File;

import org.eobjects.analyzer.connection.CompositeDatastore;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.FileDatastore;
import org.eobjects.analyzer.connection.JdbcDatastore;

/**
 * 
 * Wrapper for datastore to facilitate property retrieval in ui
 * 
 * @author anand
 * 
 */
public class DatastoreBeanWrapper {

	private Datastore datastore;

	/**
	 * @return the name
	 */
	public String getName() {
		return datastore.getName();
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return datastore.getDescription();
	}

	/**
	 * @return the datastore
	 */
	public Datastore getDatastore() {
		return datastore;
	}

	/**
	 * @param datastore
	 *            the datastore to set
	 */
	public void setDatastore(Datastore datastore) {
		this.datastore = datastore;
	}

	public boolean isFileDatastore() {
		return datastore instanceof FileDatastore;
	}

	public boolean isJdbcDatastore() {
		return datastore instanceof JdbcDatastore;
	}

	public boolean isFileFound() {
		if (datastore instanceof FileDatastore) {
			String filename = ((FileDatastore) datastore).getFilename();
			File file = new File(filename);
			return file.exists();
		} else {
			return false;
		}
	}

	public boolean isCompositeDatastore() {
		return datastore instanceof CompositeDatastore;
	}

	public boolean isHostnameBasedDatastore() {
		try {
			return datastore.getClass().getDeclaredMethod("getHostname") != null;
		} catch (Exception e) {
			return false;
		}
	}

	public String getSimpleClassName() {
		return datastore.getClass().getSimpleName();
	}

}
