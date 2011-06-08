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

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;

public class DatastoreOutputAnalyzerResult implements OutputAnalyzerResult {

	private static final long serialVersionUID = 1L;
	private final int _rowCount;
	private final String _datastoreName;

	public DatastoreOutputAnalyzerResult(int rowCount, String datastoreName) {
		_rowCount = rowCount;
		_datastoreName = datastoreName;
	}

	@Override
	public int getWrittenRowCount() {
		return _rowCount;
	}

	@Override
	public Datastore getDatastore(DatastoreCatalog datastoreCatalog) {
		return datastoreCatalog.getDatastore(_datastoreName);
	}

}
