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

final class DatastoreOutputUtils {

	private DatastoreOutputUtils() {
		// prevent instantiation
	}

	public static String safeName(String str) {
		// replaces whitespaces, commas and parentheses with underscore
		str = str.replaceAll("[\\ \\,\\(\\)]+", "_");
		return str;
	}

	public static String getJdbcUrl(File outputFile) {
		String dbName = outputFile.getPath();
		int lastIndexOf = dbName.lastIndexOf(".script");
		dbName = dbName.substring(0, lastIndexOf);
		return "jdbc:hsqldb:file:" + dbName + ";shutdown=true";
	}
}
