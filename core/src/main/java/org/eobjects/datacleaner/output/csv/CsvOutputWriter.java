/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.output.csv;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.datacleaner.output.AbstractMetaModelOutputWriter;
import org.eobjects.metamodel.UpdateableDataContext;
import org.eobjects.metamodel.schema.Table;

final class CsvOutputWriter extends AbstractMetaModelOutputWriter {

	private final String _filename;
	private final Table _table;

	public CsvOutputWriter(UpdateableDataContext dataContext, String filename, Table table, InputColumn<?>[] columns) {
		super(dataContext, columns, 100);
		_filename = filename;
		_table = table;
	}

	@Override
	public void afterClose() {
		CsvOutputWriterFactory.release(_filename);
	}

	@Override
	protected Table getTable() {
		return _table;
	}

}
