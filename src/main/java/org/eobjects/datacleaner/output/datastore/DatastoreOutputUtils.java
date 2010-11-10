package org.eobjects.datacleaner.output.datastore;

import java.io.File;

final class DatastoreOutputUtils {

	private DatastoreOutputUtils() {
		// prevent instantiation
	}

	public static String safeName(String str) {
		// replaces whitespaces, commas and parentheses with underscore
		str = str.replaceAll("[\\ \\,\\(\\)]+", "_");
		return str;
	}

	public static String getCreateJdbcUrl(File outputFile) {
		String dbName = outputFile.getPath();
		int lastIndexOf = dbName.lastIndexOf(".script");
		dbName = dbName.substring(0, lastIndexOf);
		return "jdbc:hsqldb:file:" + dbName + ";create=true";
	}

	public static String getReadOnlyJdbcUrl(File outputFile) {
		String dbName = outputFile.getPath();
		int lastIndexOf = dbName.lastIndexOf(".script");
		dbName = dbName.substring(0, lastIndexOf);
		return "jdbc:hsqldb:file:" + dbName + ";readonly=true";
	}
}
