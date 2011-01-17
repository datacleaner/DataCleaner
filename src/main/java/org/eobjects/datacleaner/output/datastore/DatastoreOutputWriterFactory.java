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
import java.io.FilenameFilter;
import java.util.List;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.datacleaner.output.OutputWriter;
import org.eobjects.datacleaner.user.DataCleanerHome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for output writers that write new datastores.
 * 
 * @author Kasper Sørensen
 */
public final class DatastoreOutputWriterFactory {

	private static final Logger logger = LoggerFactory.getLogger(DatastoreOutputWriterFactory.class);

	private static final File DEFAULT_OUTPUT_DIRECTORY = new File(DataCleanerHome.get(), "temp");
	private static final DatastoreCreationDelegate DEFAULT_CREATION_DELEGATE = new DatastoreCreationDelegateImpl();

	public static OutputWriter getWriter(String datastoreName, InputColumn<?>... columns) {
		return getWriter(DEFAULT_OUTPUT_DIRECTORY, DEFAULT_CREATION_DELEGATE, datastoreName, columns);
	}

	public static OutputWriter getWriter(DatastoreCreationDelegate creationDelegate, String datastoreName,
			InputColumn<?>... columns) {
		return getWriter(DEFAULT_OUTPUT_DIRECTORY, creationDelegate, datastoreName, columns);
	}

	public static OutputWriter getWriter(File directory, DatastoreCreationDelegate creationDelegate, String datastoreName,
			List<InputColumn<?>> columns) {
		return getWriter(directory, creationDelegate, datastoreName, columns.toArray(new InputColumn<?>[columns.size()]));
	}

	public static OutputWriter getWriter(DatastoreCreationDelegate creationDelegate, String datastoreName,
			List<InputColumn<?>> columns) {
		return getWriter(DEFAULT_OUTPUT_DIRECTORY, creationDelegate, datastoreName,
				columns.toArray(new InputColumn<?>[columns.size()]));
	}

	public static OutputWriter getWriter(String datastoreName, List<InputColumn<?>> columns) {
		return getWriter(DEFAULT_OUTPUT_DIRECTORY, DEFAULT_CREATION_DELEGATE, datastoreName,
				columns.toArray(new InputColumn<?>[columns.size()]));
	}

	public static OutputWriter getWriter(File directory, DatastoreCreationDelegate creationDelegate, String datastoreName,
			InputColumn<?>... columns) {
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				logger.error("Failed to create directory for datastores: {}", directory);
			}
		}

		synchronized (DatastoreOutputWriterFactory.class) {
			cleanFiles(directory, datastoreName);
		}

		return new DatastoreOutputWriter(datastoreName, directory, columns, creationDelegate);
	}

	private static void cleanFiles(final File directory, final String datastoreName) {
		File[] files = directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(datastoreName);
			}
		});
		for (File file : files) {
			if (!file.delete()) {
				logger.error("Failed to clean up (delete) file: {}", file);
			}
		}
	}
}
