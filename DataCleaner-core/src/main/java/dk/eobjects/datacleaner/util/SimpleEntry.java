/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.util;

import java.util.Map.Entry;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class SimpleEntry<K, V> implements Entry<K, V> {

	private K _key;
	private V _value;

	public SimpleEntry(K key, V value) {
		_key = key;
		_value = value;
	}

	public K getKey() {
		return _key;
	}

	public V getValue() {
		return _value;
	}

	public V setValue(V value) {
		_value = value;
		return _value;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hcb = new HashCodeBuilder();
		return hcb.append(_key).append(_value).toHashCode();
	}

	@Override
	public String toString() {
		return "SimpleEntry[key=" + _key + ",value=" + _value + "]";
	}
}
