package org.eobjects.datacleaner.output.datastore;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.datacleaner.output.OutputWriter;

public final class DatastoreOutputWriterFactory {

	private static File outputDirectory = new File("temp");
	private static DatastoreCreationDelegate datastoreCreationDelegate = new DatastoreCreationDelegateImpl();

	public static OutputWriter getWriter(String datastoreName, InputColumn<?>... columns) {
		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}

		cleanFiles(datastoreName);
		final File outputFile = new File(outputDirectory, datastoreName + ".script");

		return new DatastoreOutputWriter(datastoreName, outputFile, columns, datastoreCreationDelegate);
	}

	private static void cleanFiles(final String datastoreName) {
		File[] files = outputDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(datastoreName);
			}
		});
		for (File file : files) {
			file.delete();
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
