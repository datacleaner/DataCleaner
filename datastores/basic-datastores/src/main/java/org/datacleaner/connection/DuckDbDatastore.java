/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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

import java.sql.Connection;
import java.sql.SQLException;

import org.datacleaner.metamodel.UpdateableDataContext;
import org.datacleaner.metamodel.duckdb.DuckDBDataContext;
import org.datacleaner.metamodel.duckdb.DuckDBQueryRewriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Datastore implementation for DuckDB databases, accessed through the DuckDB
 * JDBC driver and {@link DuckDBDataContext} from MetaModel.
 */
public class DuckDbDatastore extends JdbcDatastore implements UpdateableDatastore {

    public static final String DRIVER_CLASS = "org.duckdb.DuckDBDriver";

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(DuckDbDatastore.class);

    public DuckDbDatastore(final String name, final String jdbcUrl) {
        super(name, jdbcUrl, DRIVER_CLASS);
    }

    public DuckDbDatastore(final String name, final String jdbcUrl, final boolean multipleConnections) {
        super(name, jdbcUrl, DRIVER_CLASS, null, null, multipleConnections);
    }

    @Override
    protected UsageAwareDatastoreConnection<UpdateableDataContext> createDatastoreConnection() {
        final Connection connection = createConnection();
        try {
            connection.setAutoCommit(false);
        } catch (final SQLException e) {
            logger.error("Could not set autocommit false for DuckDB datastore '{}'", getName());
            throw new IllegalStateException(e);
        }
        final DuckDBDataContext dataContext = new DuckDBDataContext(connection);
        dataContext.setQueryRewriter(new DuckDBQueryRewriter(dataContext));
        return new UpdateableDatastoreConnectionImpl<>(dataContext, this);
    }
}
