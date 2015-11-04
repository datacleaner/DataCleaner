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
package org.datacleaner.monitor.server.ui;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import org.datacleaner.connection.CompositeDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.FileDatastore;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.connection.ResourceDatastore;
import org.datacleaner.connection.UsernameDatastore;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.HasNameMapper;
import org.apache.metamodel.util.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for datastore to facilitate property retrieval in ui
 * 
 * @author anand
 */
public class DatastoreBeanWrapper {

    private static final Logger logger = LoggerFactory.getLogger(DatastoreBeanWrapper.class);

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

    public boolean isUsernameDatastore() {
        return _datastore instanceof UsernameDatastore;
    }

    public String getUsername() {
        if (isUsernameDatastore()) {
            return ((UsernameDatastore) _datastore).getUsername();
        }
        return null;
    }

    public String getFilename() {
        if (_datastore instanceof FileDatastore) {
            String filename = ((FileDatastore) _datastore).getFilename();
            return filename;
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

    public String getHostname() {
        try {
            Method hostnameMethod = _datastore.getClass().getDeclaredMethod("getHostname");
            if (hostnameMethod != null) {
                hostnameMethod.setAccessible(true);
                Object result = hostnameMethod.invoke(_datastore);
                if (result != null && result instanceof String) {
                    return (String) result;
                }
            }
            return null;
        } catch (Exception e) {
            logger.debug("Failed to invoke method 'getHostname'", e);
            return null;
        }
    }

    public boolean isFileFound() {
        if (_datastore instanceof ResourceDatastore) {
            Resource resource = ((ResourceDatastore) _datastore).getResource();
            if (resource != null) {
                return resource.isExists();
            }
        }

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
            logger.debug("Failed to get method 'getHostname'", e);
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
