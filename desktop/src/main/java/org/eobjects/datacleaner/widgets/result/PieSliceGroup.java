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
package org.eobjects.datacleaner.widgets.result;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eobjects.analyzer.result.ValueCount;
import org.eobjects.metamodel.util.BaseObject;

/**
 * Represents a set of values that has been grouped together for the purpose of
 * visualization in a pie chart.
 * 
 * @author Kasper Sørensen
 */
public class PieSliceGroup extends BaseObject implements Iterable<ValueCount> {

	private final String _name;
	private final int _totalSize;
	private final Collection<String> _values;
	private final int _fixedValueCount;
	private final List<ValueCount> _valueCounts;

	public PieSliceGroup(String name, Collection<String> values, int fixedValueCount) {
		this(name, -1, values, fixedValueCount);
	}

	public PieSliceGroup(String name, int totalSize, Collection<String> values, int fixedValueCount) {
		_name = name;
		_totalSize = totalSize;
		_values = values;
		_fixedValueCount = fixedValueCount;
		_valueCounts = null;
	}

	public PieSliceGroup(String name, List<ValueCount> valueCounts) {
		_name = name;
		_totalSize = -1;
		_values = null;
		_fixedValueCount = -1;
		_valueCounts = valueCounts;
	}

	public String getName() {
		return _name;
	}

	public int size() {
		if (_valueCounts == null) {
			return _values.size();
		}
		return _valueCounts.size();
	}

	public Iterator<ValueCount> getValueCounts() {
		if (_valueCounts == null) {
			final Iterator<String> it = _values.iterator();
			return new Iterator<ValueCount>() {
				@Override
				public boolean hasNext() {
					return it.hasNext();
				}

				@Override
				public ValueCount next() {
					String value = it.next();
					return new ValueCount(value, _fixedValueCount);
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

			};
		}
		return _valueCounts.iterator();
	}

	public int getTotalCount() {
		if (_totalSize != -1) {
			return _totalSize;
		}
		if (_valueCounts != null) {
			int result = 0;
			for (ValueCount vc : _valueCounts) {
				result += vc.getCount();
			}
			return result;
		}
		return _values.size() * _fixedValueCount;
	}

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		identifiers.add(_name);
	}

	public void addValueCount(ValueCount vc) {
		if (_valueCounts == null) {
			assert vc.getCount() == _fixedValueCount;
			_values.add(vc.getValue());
		} else {
			_valueCounts.add(vc);
		}
	}

	@Override
	public Iterator<ValueCount> iterator() {
		return getValueCounts();
	}
}
