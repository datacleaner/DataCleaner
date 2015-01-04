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
package org.eobjects.analyzer.result;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class ValueCountListImpl implements ValueCountList {

	private static final long serialVersionUID = 1L;

	private final boolean _retainHighest;
	private final int _maxSize;
	private final LinkedList<ValueFrequency> _values = new LinkedList<ValueFrequency>();

	public static ValueCountListImpl createFullList() {
		return new ValueCountListImpl(-1, true);
	}

	public static ValueCountList emptyList() {
		return new ValueCountListImpl(0, true);
	}

	public static ValueCountListImpl createTopList(int topFrequentValues) {
		return new ValueCountListImpl(topFrequentValues, true);
	}

	public static ValueCountListImpl createBottomList(int bottomFrequentValues) {
		return new ValueCountListImpl(bottomFrequentValues, false);
	}

	private ValueCountListImpl(int maxSize, boolean retainHighest) {
		_maxSize = maxSize;
		_retainHighest = retainHighest;
	}

	public void register(ValueFrequency valueCount) {
		boolean inserted = false;
		if (_retainHighest) {
			for (ListIterator<ValueFrequency> it = _values.listIterator(); it.hasNext();) {
				ValueFrequency v = it.next();
				if (valueCount.getCount() > v.getCount()) {
					it.previous();
					it.add(valueCount);
					inserted = true;
					it.next();
					trimValues();
					break;
				}
			}
		} else {
			for (ListIterator<ValueFrequency> it = _values.listIterator(); it.hasNext();) {
				ValueFrequency v = it.next();
				if (valueCount.getCount() < v.getCount()) {
					it.previous();
					it.add(valueCount);
					inserted = true;
					it.next();
					trimValues();
					break;
				}
			}
		}
		if (!inserted && (_maxSize == -1 || _maxSize > _values.size())) {
			_values.add(valueCount);
		}
	}

	private void trimValues() {
		if (_maxSize == -1) {
			return;
		}
		while (_values.size() > _maxSize) {
			_values.removeLast();
		}
	}

	public List<ValueFrequency> getValueCounts() {
		return _values;
	}

	public int getMaxSize() {
		return _maxSize;
	}

	public int getActualSize() {
		return _values.size();
	}

	@Override
	public String toString() {
		return "ValueCountList[" + _values + "]";
	}
}
