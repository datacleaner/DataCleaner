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
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.FileDatastore;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.datacleaner.Main;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.server.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 * Component with convenience methods primarily intended to aid JSF/EL code
 * which is not always as expressive as Java.
 */
@Component("jsfHelper")
@Scope(WebApplicationContext.SCOPE_REQUEST)
public class JsfHelper {

	DatastoreCatalog datasourceCatalog;

	@Autowired
	TenantContextFactory tenantContextFactory;

	@Autowired
	User user;

	DatastoreBeanWrapper[] datastoreBeanWrapper;

	public boolean isFileDatastore(Datastore datastore) {
		return datastore instanceof FileDatastore;
	}

	public boolean isJdbcDatastore(Datastore datastore) {
		return datastore instanceof JdbcDatastore;
	}

	public boolean isFileFound(Datastore datastore) {
		if (datastore instanceof FileDatastore) {
			String filename = ((FileDatastore) datastore).getFilename();
			File file = new File(filename);
			return file.exists();
		} else {
			return false;
		}
	}

	public boolean isCompositeDatastore(Datastore datastore) {
		return datastore instanceof CompositeDatastore;
	}

	public boolean isHostnameBasedDatastore(Datastore datastore) {
		try {
			return datastore.getClass().getDeclaredMethod("getHostname") != null;
		} catch (Exception e) {
			return false;
		}
	}

	public String getVersion() {
		return Main.VERSION;
	}

	public String getSimpleClassName(Object object) {
		return object.getClass().getSimpleName();
	}

	public DatastoreBeanWrapper[] getDatastoreBeanWrapper() {
		if (datastoreBeanWrapper != null) {
			return datastoreBeanWrapper;
		}

		datasourceCatalog = tenantContextFactory.getContext(user.getTenant())
				.getConfiguration().getDatastoreCatalog();

		datastoreBeanWrapper = prepareDatastoreWrappers(datasourceCatalog
				.getDatastoreNames());
		return datastoreBeanWrapper;
	}

	private DatastoreBeanWrapper[] prepareDatastoreWrappers(
			String[] datastoreNames) {
		DatastoreBeanWrapper[] beanWrapperArray = new DatastoreBeanWrapper[datastoreNames.length];

		int count = 0;

		for (String datastore : datastoreNames) {
			beanWrapperArray[count] = new DatastoreBeanWrapper();
			beanWrapperArray[count].setDatastore(datasourceCatalog
					.getDatastore(datastore));
			count++;
		}

		return beanWrapperArray;

	}
}
