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
import java.util.List;

import org.eobjects.analyzer.connection.CompositeDatastore;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.FileDatastore;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.HasNameMapper;

/**
 * Wrapper for datastore to facilitate property retrieval in ui
 * 
 * @author anand
 */
public class DatastoreBeanWrapper {

    private final Datastore _datastore;

    public DatastoreBeanWrapper(Datastore datastore) {
        _datastore = datastore;
    }

    /**
     * @return the name
     */
    public String getName() {
        return _datastore.getName();
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return _datastore.getDescription();
    }

    /**
     * @return the datastore
     */
    public Datastore getDatastore() {
        return _datastore;
    }

    public boolean isFileDatastore() {
        return _datastore instanceof FileDatastore;
    }

    public boolean isJdbcDatastore() {
        return _datastore instanceof JdbcDatastore;
    }

    public String getFilename() {
        if (_datastore instanceof FileDatastore) {
            String filename = ((FileDatastore) _datastore).getFilename();
            return filename;
        } else {
            return null;
        }
    }

    public String getUsername() {
        if (_datastore instanceof JdbcDatastore) {
            JdbcDatastore jdbcDatastore = (JdbcDatastore) _datastore;
            String username = jdbcDatastore.getUsername();
            return username;
        } else {
            return null;
        }
    }

    public String getJdbcUrl() {
        if (_datastore instanceof JdbcDatastore) {
            JdbcDatastore jdbcDatastore = (JdbcDatastore) _datastore;
            String url = jdbcDatastore.getJdbcUrl();
            if (url == null) {
                url = jdbcDatastore.getDatasourceJndiUrl();
            }
            return url;
        } else {
            return null;
        }
    }

    public boolean isFileFound() {
        String filename = getFilename();
        if (filename == null) {
            return false;
        }
        File file = new File(filename);
        return file.exists();
    }

    public boolean isCompositeDatastore() {
        return _datastore instanceof CompositeDatastore;
    }

    public boolean isHostnameBasedDatastore() {
        try {
            return _datastore.getClass().getDeclaredMethod("getHostname") != null;
        } catch (Exception e) {
            return false;
        }
    }

    public String getSimpleClassName() {
        return _datastore.getClass().getSimpleName();
    }

    public String getChildDatastores() {
        if (!isCompositeDatastore()) {
            return null;
        }
        CompositeDatastore compositeDatastore = (CompositeDatastore) _datastore;
        List<? extends Datastore> childDatastores = compositeDatastore.getDatastores();
        List<String> names = CollectionUtils.map(childDatastores, new HasNameMapper());
        return names.toString();
    }
}
