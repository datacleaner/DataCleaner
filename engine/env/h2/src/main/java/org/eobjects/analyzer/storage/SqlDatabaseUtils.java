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
package org.eobjects.analyzer.storage;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eobjects.analyzer.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SqlDatabaseUtils {

	private final static Logger logger = LoggerFactory.getLogger(SqlDatabaseUtils.class);

	public final static String CREATE_TABLE_PREFIX = "CREATE CACHED TABLE ";

	private SqlDatabaseUtils() {
		// prevent instantiation
	}

	public static String getSqlType(Class<?> valueType) {
		if (String.class == valueType) {
			return "VARCHAR";
		}
		if (Number.class == valueType) {
			return "DOUBLE";
		}
		if (Integer.class == valueType) {
			return "INTEGER";
		}
		if (Long.class == valueType) {
			return "BIGINT";
		}
		if (Double.class == valueType) {
			return "DOUBLE";
		}
		if (Short.class == valueType) {
			return "SMALLINT";
		}
		if (Float.class == valueType) {
			return "FLOAT";
		}
		if (BigInteger.class == valueType) {
			return "BIGINT";
		}
		if (Character.class == valueType) {
			return "CHAR";
		}
		if (Boolean.class == valueType) {
			return "BOOLEAN";
		}
		if (Byte.class == valueType) {
			return "BINARY";
		}
		if (ReflectionUtils.isDate(valueType)) {
			return "TIMESTAMP";
		}
		if (ReflectionUtils.isByteArray(valueType)) {
			return "BLOB";
		}
		throw new UnsupportedOperationException("Unsupported value type: " + valueType);
	}

	public static void performUpdate(Connection connection, String sql) {
		Statement st = null;
		try {
			st = connection.createStatement();
			st.executeUpdate(sql);
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		} finally {
			safeClose(null, st);
		}
	}

	public static void safeClose(ResultSet rs, Statement st) {
		if (rs != null) {
			boolean close = true;

			try {
				if (rs.isClosed()) {
					close = false;
					if (logger.isInfoEnabled()) {
						logger.info("result set is already closed: {}", rs);
						StackTraceElement[] stackTrace = new Throwable().getStackTrace();
						for (int i = 0; i < stackTrace.length && i < 5; i++) {
							logger.info(" - stack frame {}: {}", i, stackTrace[i]);
						}
					}
				}
			} catch (Throwable e) {
				logger.debug("could not determine if result set is already closed", e);
			}

			if (close) {
				logger.debug("closing result set: {}", rs);
				try {
					rs.close();
				} catch (SQLException e) {
					logger.warn("could not close result set", e);
				}
			}
		}

		if (st != null) {
			boolean close = true;

			try {
				if (st.isClosed()) {
					close = false;
					if (logger.isInfoEnabled()) {
						logger.info("statement is already closed: {}", st);
						StackTraceElement[] stackTrace = new Throwable().getStackTrace();
						for (int i = 0; i < stackTrace.length && i < 5; i++) {
							logger.info(" - stack frame {}: {}", i, stackTrace[i]);
						}
					}
				}
			} catch (Throwable e) {
				logger.debug("could not determine if statement is already closed", e);
			}

			if (close) {
				logger.debug("closing statement: {}", st);
				try {
					st.close();
				} catch (SQLException e) {
					logger.warn("could not close statement", e);
				}
			}
		}
	}
}
