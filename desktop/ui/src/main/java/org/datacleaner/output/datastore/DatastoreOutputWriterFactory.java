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
package org.datacleaner.output.datastore;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.datacleaner.api.InputColumn;
import org.datacleaner.output.OutputWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for output writers that write new datastores.
 */
public final class DatastoreOutputWriterFactory {

    private static final Logger logger = LoggerFactory.getLogger(DatastoreOutputWriterFactory.class);

    private static final Map<String, AtomicInteger> counters = new HashMap<>();

    public static OutputWriter getWriter(final File directory, final DatastoreCreationDelegate creationDelegate,
            final String datastoreName, final String tableName, final InputColumn<?>... columns) {
        return getWriter(directory, creationDelegate, datastoreName, tableName, true, columns);
    }

    public static DatastoreOutputWriter getWriter(final File directory,
            final DatastoreCreationDelegate creationDelegate, final String datastoreName, final String tableName,
            final boolean truncate, final InputColumn<?>... columns) {
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                logger.error("Failed to create directory for datastores: {}", directory);
            }
        }

        synchronized (counters) {
            final DatastoreOutputWriter outputWriter =
                    new DatastoreOutputWriter(datastoreName, tableName, directory, columns, creationDelegate, truncate);

            AtomicInteger counter = counters.get(outputWriter.getJdbcUrl());
            if (counter == null) {
                counter = new AtomicInteger();
                counters.put(outputWriter.getJdbcUrl(), counter);
            }
            counter.incrementAndGet();

            return outputWriter;
        }
    }

    /**
     * Gets the actual table name written by an {@link OutputWriter}, which may
     * differ from the requested name if the name was not a valid RDBMS table
     * name or if the table name was already used and truncation was disabled.
     *
     * @param outputWriter
     * @return
     */
    public static String getActualTableName(final OutputWriter outputWriter) {
        assert outputWriter instanceof DatastoreOutputWriter;
        final DatastoreOutputWriter dow = (DatastoreOutputWriter) outputWriter;
        return dow.getTableName();
    }

    protected static void release(final DatastoreOutputWriter writer) {
        synchronized (counters) {
            final int count = counters.get(writer.getJdbcUrl()).decrementAndGet();
            if (count == 0) {
                counters.remove(writer.getJdbcUrl());

                try (Connection connection = writer.getConnection()) {

                    try (Statement st = connection.createStatement()) {
                        st.execute("SHUTDOWN");
                    } catch (final SQLException e) {
                        logger.error("Could not invoke SHUTDOWN", e);
                    }

                } catch (final SQLException e) {
                    logger.error("Could not close connection", e);
                    throw new IllegalStateException(e);
                }
            }
        }
    }
}
