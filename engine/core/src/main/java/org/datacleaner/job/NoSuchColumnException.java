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
package org.datacleaner.job;

/**
 * Exception thrown in case a job is being opened and it references an
 * unexisting column in the datastore.
 * 
 * 
 */
public class NoSuchColumnException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final String _columnName;

	public NoSuchColumnException(String columnName) {
		super();
		_columnName = columnName;
	}

	public String getColumnName() {
        return _columnName;
    }

	@Override
	public String getMessage() {
		return "No such column: " + _columnName;
	}
}
