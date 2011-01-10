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
package org.eobjects.datacleaner.output.beans;

import java.io.File;

import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;

import org.eobjects.metamodel.util.FileHelper;

public class CsvOutputAnalyzerResult implements OutputAnalyzerResult {

	private static final long serialVersionUID = 1L;
	private final int _rowCount;
	private File _file;
	private char _separatorChar;
	private char _quoteChar;

	public CsvOutputAnalyzerResult(File file, char separatorChar, char quoteChar, int rowCount) {
		_file = file;
		_separatorChar = separatorChar;
		_quoteChar = quoteChar;
		_rowCount = rowCount;
	}

	@Override
	public int getWrittenRowCount() {
		return _rowCount;
	}

	@Override
	public Datastore getDatastore(DatastoreCatalog datastoreCatalog) {
		return new CsvDatastore(_file.getName(), _file.getAbsolutePath(), _quoteChar, _separatorChar,
				FileHelper.DEFAULT_ENCODING);
	}

}
