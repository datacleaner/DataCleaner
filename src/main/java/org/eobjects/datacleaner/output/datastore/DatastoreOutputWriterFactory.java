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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DatastoreOutputWriterFactory {

	private static final Logger logger = LoggerFactory.getLogger(DatastoreOutputWriterFactory.class);

	private static File outputDirectory = new File("temp");
	private static DatastoreCreationDelegate datastoreCreationDelegate = new DatastoreCreationDelegateImpl();

	public static OutputWriter getWriter(String datastoreName, InputColumn<?>... columns) {
		if (!outputDirectory.exists()) {
			if (!outputDirectory.mkdirs()) {
				logger.error("Failed to create directory for datastores: {}", outputDirectory);
			}
		}

		synchronized (DatastoreOutputWriterFactory.class) {
			cleanFiles(datastoreName);
		}

		return new DatastoreOutputWriter(datastoreName, outputDirectory, columns, datastoreCreationDelegate);
	}

	private static void cleanFiles(final String datastoreName) {
		File[] files = outputDirectory.listFiles(new FilenameFilter() {
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

	public static OutputWriter getWriter(String datastoreName, List<InputColumn<?>> columns) {
		return getWriter(datastoreName, columns.toArray(new InputColumn<?>[columns.size()]));
	}

	public static void setDatastoreCreationDelegate(DatastoreCreationDelegate datastoreCreationDelegate) {
		DatastoreOutputWriterFactory.datastoreCreationDelegate = datastoreCreationDelegate;
	}

	public static void setOutputDirectory(File outputDirectory) {
		DatastoreOutputWriterFactory.outputDirectory = outputDirectory;
	}
}
