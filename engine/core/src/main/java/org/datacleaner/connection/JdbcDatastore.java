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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.datacleaner.util.ReadObjectBuilder;
import org.datacleaner.util.StringUtils;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.schema.TableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Datastore implementation for JDBC based connections. Connections can either
 * be based on JDBC urls or JNDI urls.
 */
public class JdbcDatastore extends UsageAwareDatastore<UpdateableDataContext> implements UpdateableDatastore,
        UsernameDatastore {

    private static final long serialVersionUID = 1L;

    public static final String SYSTEM_PROPERTY_CONNECTION_POOL_MAX_SIZE = "datastore.jdbc.connection.pool.max.size";
    public static final String SYSTEM_PROPERTY_CONNECTION_POOL_MIN_EVICTABLE_IDLE_TIME_MILLIS = "datastore.jdbc.connection.pool.idle.timeout";
    public static final String SYSTEM_PROPERTY_CONNECTION_POOL_TIME_BETWEEN_EVICTION_RUNS_MILLIS = "datastore.jdbc.connection.pool.eviction.period.millis";

    private static final Logger logger = LoggerFactory.getLogger(JdbcDatastore.class);

    private final String _jdbcUrl;
    private final String _username;
    private final String _password;
    private final String _driverClass;
    private final boolean _multipleConnections;
    private final String _datasourceJndiUrl;
    private final TableType[] _tableTypes;
    private final String _catalogName;

    private JdbcDatastore(String name, String jdbcUrl, String driverClass, String username, String password,
            String datasourceJndiUrl, boolean multipleConnections, TableType[] tableTypes, String catalogName) {
        super(name);
        _jdbcUrl = jdbcUrl;
        _driverClass = driverClass;
        _username = username;
        _password = password;
        _datasourceJndiUrl = datasourceJndiUrl;
        _multipleConnections = multipleConnections;
        _tableTypes = tableTypes;
        _catalogName = catalogName;
    }

    public JdbcDatastore(String name, String jdbcUrl, String driverClass) {
        this(name, jdbcUrl, driverClass, null, null, true);
    }

    public JdbcDatastore(String name, String jdbcUrl, String driverClass, String username, String password,
            boolean multipleConnections, TableType[] tableTypes, String catalogName) {
        this(name, jdbcUrl, driverClass, username, password, null, multipleConnections, tableTypes, catalogName);
    }

    public JdbcDatastore(String name, String jdbcUrl, String driverClass, String username, String password,
            boolean multipleConnections) {
        this(name, jdbcUrl, driverClass, username, password, multipleConnections, null, null);
    }

    public JdbcDatastore(String name, String datasourceJndiUrl) {
        this(name, datasourceJndiUrl, (TableType[]) null, null);
    }

    public JdbcDatastore(String name, String datasourceJndiUrl, TableType[] tableTypes, String catalogName) {
        this(name, null, null, null, null, datasourceJndiUrl, false, tableTypes, catalogName);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ReadObjectBuilder.create(this, JdbcDatastore.class).readObject(stream);
    }

    @Override
    public UpdateableDatastoreConnection openConnection() {
        DatastoreConnection connection = super.openConnection();
        return (UpdateableDatastoreConnection) connection;
    }

    /**
     * Alternative constructor usable only for in-memory (ie. non-persistent)
     * datastores, because the datastore will not be able to create new
     * connections.
     * 
     * @param name
     * @param dc
     */
    public JdbcDatastore(String name, UpdateableDataContext dc) {
        this(name, null, null, null, null, null, false, null, null);
        setDataContextProvider(new UpdateableDatastoreConnectionImpl<UpdateableDataContext>(dc, this));
    }

    @Override
    protected void decorateIdentity(List<Object> identifiers) {
        super.decorateIdentity(identifiers);
        identifiers.add(_driverClass);
        identifiers.add(_jdbcUrl);
        identifiers.add(_datasourceJndiUrl);
        identifiers.add(_username);
        identifiers.add(_password);
        identifiers.add(_multipleConnections);
        identifiers.add(getTableTypes());
    }

    public boolean isMultipleConnections() {
        return _multipleConnections;
    }

    public TableType[] getTableTypes() {
        final TableType[] tableTypes;
        if (_tableTypes == null) {
            tableTypes = TableType.DEFAULT_TABLE_TYPES;
        } else {
            tableTypes = _tableTypes;
        }
        return Arrays.copyOf(tableTypes, tableTypes.length);
    }

    public String getCatalogName() {
        return _catalogName;
    }

    public String getJdbcUrl() {
        return _jdbcUrl;
    }

    @Override
    public String getUsername() {
        return _username;
    }

    public String getPassword() {
        return _password;
    }

    public String getDriverClass() {
        return _driverClass;
    }

    public String getDatasourceJndiUrl() {
        return _datasourceJndiUrl;
    }

    public Connection createConnection() throws IllegalStateException {
        initializeDriver();

        try {
            if (_username != null && _password != null) {
                return DriverManager.getConnection(_jdbcUrl, _username, _password);
            } else {
                return DriverManager.getConnection(_jdbcUrl);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create connection", e);
        }
    }

    public DataSource createDataSource() {
        initializeDriver();

        BasicDataSource ds = new BasicDataSource();
        ds.setDefaultAutoCommit(false);
        ds.setUrl(_jdbcUrl);

        ds.setMaxActive(getSystemPropertyValue(SYSTEM_PROPERTY_CONNECTION_POOL_MAX_SIZE, -1));
        ds.setMinEvictableIdleTimeMillis(getSystemPropertyValue(
                SYSTEM_PROPERTY_CONNECTION_POOL_MIN_EVICTABLE_IDLE_TIME_MILLIS, 500));
        ds.setTimeBetweenEvictionRunsMillis(getSystemPropertyValue(
                SYSTEM_PROPERTY_CONNECTION_POOL_TIME_BETWEEN_EVICTION_RUNS_MILLIS, 1000));

        if (_username != null && _password != null) {
            ds.setUsername(_username);
            ds.setPassword(_password);
        }
        return ds;
    }

    private int getSystemPropertyValue(String property, int defaultValue) {
        String str = System.getProperty(property);
        if (str == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            logger.debug("Failed to parse system property '{}': '{}'", property, str);
            return defaultValue;
        }
    }

    private void initializeDriver() {
        if (_jdbcUrl == null) {
            throw new IllegalStateException("JDBC URL is null, cannot create connection!");
        }

        logger.debug("Determining if driver initialization is nescesary");

        // it's best to avoid initializing the driver, so we do this check.
        // It may already have been initialized and Class.forName(...) does
        // not always work if the driver is in a different classloader
        boolean installDriver = true;

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                if (driver.acceptsURL(_jdbcUrl)) {
                    installDriver = false;
                    break;
                }
            } catch (Exception e) {
                logger.warn("Driver threw exception when acceptURL(...) was invoked", e);
            }
        }

        if (installDriver) {
            try {
                Class.forName(_driverClass);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Could not initialize JDBC driver", e);
            }
        }
    }

    @Override
    protected UsageAwareDatastoreConnection<UpdateableDataContext> createDatastoreConnection() {
        if (StringUtils.isNullOrEmpty(_datasourceJndiUrl)) {
            if (isMultipleConnections()) {
                final DataSource dataSource = createDataSource();
                return new DataSourceDatastoreConnection(dataSource, getTableTypes(), _catalogName, this);
            } else {
                final Connection connection = createConnection();
                final UpdateableDataContext dataContext = new JdbcDataContext(connection, getTableTypes(), _catalogName);
                return new UpdateableDatastoreConnectionImpl<UpdateableDataContext>(dataContext, this);
            }
        } else {
            try {
                Context initialContext = getJndiNamingContext();
                DataSource dataSource = (DataSource) initialContext.lookup(_datasourceJndiUrl);
                return new DataSourceDatastoreConnection(dataSource, getTableTypes(), _catalogName, this);
            } catch (Exception e) {
                logger.error("Could not retrieve DataSource '{}'", _datasourceJndiUrl);
                throw new IllegalStateException(e);
            }
        }
    }

    protected Context getJndiNamingContext() throws NamingException {
        return new InitialContext();
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(true, false);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("JdbcDatastore[name=");
        sb.append(getName());
        if (_jdbcUrl != null) {
            sb.append(",url=");
            sb.append(_jdbcUrl);
        } else {
            sb.append(",jndi=");
            sb.append(_datasourceJndiUrl);
        }
        sb.append("]");
        return sb.toString();
    }
}
