/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

final class SqlDatabaseSetIterator<E> implements Iterator<E> {

	private final SqlDatabaseSet<E> _set;
	private final ResultSet _rs;
	private final Statement _st;

	private volatile boolean _hasNext;
	private volatile E _currentValue;
	private volatile E _nextValue;

	public SqlDatabaseSetIterator(SqlDatabaseSet<E> set, ResultSet rs, Statement st) {
		_set = set;
		_rs = rs;
		_st = st;
		moveNext();
	}

	@SuppressWarnings("unchecked")
	private void moveNext() {
		try {
			_currentValue = _nextValue;
			_hasNext = _rs.next();
			if (_hasNext) {
				_nextValue = (E) _rs.getObject(1);
			} else {
				_nextValue = null;
				SqlDatabaseUtils.safeClose(_rs, _st);
			}
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean hasNext() {
		return _hasNext;
	}

	@Override
	public E next() {
		moveNext();
		return _currentValue;
	}

	@Override
	public void remove() {
		_set.remove(_currentValue);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (_hasNext) {
			SqlDatabaseUtils.safeClose(_rs, _st);
		}
	}
}
