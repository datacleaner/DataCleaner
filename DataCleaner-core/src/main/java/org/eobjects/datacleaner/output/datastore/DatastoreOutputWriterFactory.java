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
package org.eobjects.datacleaner.output.datastore;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.storage.SqlDatabaseUtils;
import org.eobjects.datacleaner.output.OutputWriter;
import org.eobjects.datacleaner.user.UserPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for output writers that write new datastores.
 * 
 * @author Kasper Sørensen
 */
public final class DatastoreOutputWriterFactory {

	private static final Logger logger = LoggerFactory.getLogger(DatastoreOutputWriterFactory.class);

	private static final DatastoreCreationDelegate DEFAULT_CREATION_DELEGATE = new DatastoreCreationDelegateImpl();
	private static final Map<String, AtomicInteger> counters = new HashMap<String, AtomicInteger>();

	public static OutputWriter getWriter(String datastoreName, String tableName, InputColumn<?>... columns) {
		return getWriter(datastoreName, tableName, true, columns);
	}

	public static OutputWriter getWriter(String datastoreName, String tableName, boolean truncate, InputColumn<?>... columns) {
		return getWriter(getDefaultOutputDirectory(), DEFAULT_CREATION_DELEGATE, datastoreName, tableName, truncate, columns);
	}

	public static OutputWriter getWriter(DatastoreCreationDelegate creationDelegate, String datastoreName, String tableName,
			InputColumn<?>... columns) {
		return getWriter(getDefaultOutputDirectory(), creationDelegate, datastoreName, tableName, columns);
	}

	public static OutputWriter getWriter(File directory, DatastoreCreationDelegate creationDelegate, String datastoreName,
			String tableName, List<InputColumn<?>> columns) {
		return getWriter(directory, creationDelegate, datastoreName, tableName,
				columns.toArray(new InputColumn<?>[columns.size()]));
	}

	public static OutputWriter getWriter(DatastoreCreationDelegate creationDelegate, String datastoreName, String tableName,
			List<InputColumn<?>> columns) {
		return getWriter(getDefaultOutputDirectory(), creationDelegate, datastoreName, tableName,
				columns.toArray(new InputColumn<?>[columns.size()]));
	}

	public static OutputWriter getWriter(String datastoreName, String tableName, List<InputColumn<?>> columns) {
		return getWriter(getDefaultOutputDirectory(), DEFAULT_CREATION_DELEGATE, datastoreName, tableName,
				columns.toArray(new InputColumn<?>[columns.size()]));
	}

	public static OutputWriter getWriter(File directory, DatastoreCreationDelegate creationDelegate, String datastoreName,
			String tableName, InputColumn<?>... columns) {
		return getWriter(directory, creationDelegate, datastoreName, tableName, true, columns);
	}

	public static OutputWriter getWriter(File directory, DatastoreCreationDelegate creationDelegate, String datastoreName,
			String tableName, boolean truncate, InputColumn<?>... columns) {
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				logger.error("Failed to create directory for datastores: {}", directory);
			}
		}

		synchronized (counters) {
			final DatastoreOutputWriter outputWriter = new DatastoreOutputWriter(datastoreName, tableName, directory,
					columns, creationDelegate, truncate);

			AtomicInteger counter = counters.get(outputWriter.getJdbcUrl());
			if (counter == null) {
				counter = new AtomicInteger();
				counters.put(outputWriter.getJdbcUrl(), counter);
			}
			counter.incrementAndGet();

			return outputWriter;
		}
	}

	private static File getDefaultOutputDirectory() {
		return UserPreferences.getInstance().getSaveDatastoreDirectory();
	}

	protected static void release(DatastoreOutputWriter writer) {
		synchronized (counters) {
			int count = counters.get(writer.getJdbcUrl()).decrementAndGet();
			if (count == 0) {
				counters.remove(writer.getJdbcUrl());

				Connection connection = writer.getConnection();
				Statement st = null;

				try {
					st = connection.createStatement();
					st.execute("SHUTDOWN");
				} catch (SQLException e) {
					logger.error("Could not invoke SHUTDOWN", e);
				} finally {
					SqlDatabaseUtils.safeClose(null, st);
				}

				try {
					logger.info("Closing connection: {}", connection);
					connection.close();
				} catch (SQLException e) {
					logger.error("Could not close connection", e);
					throw new IllegalStateException(e);
				}
			}
		}
	}
}
