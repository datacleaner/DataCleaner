package org.eobjects.datacleaner.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.storage.SqlDatabaseUtils;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.util.WindowManager;

import dk.eobjects.metamodel.util.FileHelper;

/**
 * Creates a new datastore based on a data set.
 * 
 * Internally this uses HSQLDB to store the datastore. The user doesn't have to
 * know this of course, which is why just the datastore name is required to use
 * the class.
 * 
 * @author Kasper Sørensen
 */
public class DatastoreDataSetWriter implements DataSetWriter {

	/**
	 * Defines the directory in which to store the database files
	 */
	private static File storageDirectory = new File("temp");
	private final String _datastoreName;

	/**
	 * @param datastoreName
	 * @throws IllegalArgumentException
	 *             in case the datastore name already exists
	 */
	public DatastoreDataSetWriter(String datastoreName) throws IllegalArgumentException {
		if (!storageDirectory.exists()) {
			storageDirectory.mkdirs();
		}
		_datastoreName = datastoreName;
	}

	private String getJdbcUrl(File outputFile) {
		String dbName = outputFile.getPath();
		int lastIndexOf = dbName.lastIndexOf(".script");
		dbName = dbName.substring(0, lastIndexOf);
		return "jdbc:hsqldb:file:" + dbName + ";readonly=true";
	}

	public static String safeName(String str) {
		// replaces whitespaces, commas and parentheses with underscore
		str = str.replaceAll("[\\ \\,\\(\\)]+", "_");
		return str;
	}

	@Override
	public void write(List<InputColumn<?>> columns, InputRow[] rows) {
		AnalyzerBeansConfiguration configuration = WindowManager.getInstance().getMainWindow().getConfiguration();
		MutableDatastoreCatalog datastoreCatalog = (MutableDatastoreCatalog) configuration.getDatastoreCatalog();

		File outputFile = new File(storageDirectory, safeName(_datastoreName) + ".script");

		try {
			outputFile.createNewFile();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		InputColumn<?>[] cols = columns.toArray(new InputColumn<?>[columns.size()]);

		BufferedWriter writer = FileHelper.getBufferedWriter(outputFile);

		try {
			writer.write("CREATE SCHEMA PUBLIC AUTHORIZATION DBA");
			writer.write('\n');
			writer.write("CREATE MEMORY TABLE dataset (");
			for (int i = 0; i < cols.length; i++) {
				if (i != 0) {
					writer.write(',');
				}
				InputColumn<?> inputColumn = cols[i];
				String name = inputColumn.getName();
				writer.write(safeName(name));
				writer.write(' ');
				String type = SqlDatabaseUtils.getSqlType(inputColumn.getDataType());
				writer.write(type);
			}
			writer.write(")");
			writer.write('\n');
			writer.write("CREATE USER SA PASSWORD \"\"");
			writer.write('\n');
			writer.write("GRANT DBA TO SA");
			writer.write('\n');
			writer.write("SET WRITE_DELAY 20");
			writer.write('\n');
			writer.write("SET SCHEMA PUBLIC");

			for (InputRow row : rows) {
				writer.write('\n');
				writer.write("INSERT INTO dataset VALUES (");
				for (int i = 0; i < cols.length; i++) {
					if (i != 0) {
						writer.write(',');
					}
					Object value = row.getValue(cols[i]);
					if (value == null) {
						writer.write("null");
					} else {
						boolean requiresQuote = requiresQuote(value);
						if (requiresQuote) {
							writer.write('\'');
						}
						writer.write(escape(value.toString()));

						if (requiresQuote) {
							writer.write('\'');
						}
					}
				}
				writer.write(')');
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}

		String jdbcUrl = getJdbcUrl(outputFile);

		Datastore ds = new JdbcDatastore(_datastoreName, jdbcUrl, "org.hsqldb.jdbcDriver", "SA", "");
		datastoreCatalog.addDatastore(ds);

	}

	private boolean requiresQuote(Object value) {
		return value instanceof String || value instanceof Date || value instanceof Character;
	}

	private String escape(String str) {
		str = str.replaceAll("\'", "\'\'");
		return str;
	}

	public static void setStorageDirectory(File storageDirectory) {
		DatastoreDataSetWriter.storageDirectory = storageDirectory;
	}
}
