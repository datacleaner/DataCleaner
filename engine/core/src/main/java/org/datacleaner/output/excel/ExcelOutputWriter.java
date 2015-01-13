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
package org.datacleaner.output.excel;

import org.datacleaner.api.InputColumn;
import org.datacleaner.output.AbstractMetaModelOutputWriter;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.schema.Table;

final class ExcelOutputWriter extends AbstractMetaModelOutputWriter {

	private final String _filename;
	private final Table _table;

	public ExcelOutputWriter(UpdateableDataContext dataContext, String filename, Table table, InputColumn<?>[] columns) {
		super(dataContext, columns, 2000);
		_filename = filename;
		_table = table;
	}
	
	@Override
	public void afterClose() {
		ExcelOutputWriterFactory.release(_filename);
	}

	@Override
	protected Table getTable() {
		return _table;
	}

}
