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
package org.eobjects.analyzer.reference;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.metamodel.util.BaseObject;

/**
 * Simple array/in-memory based implementation of the {@link ReferenceValues}
 * interface.
 * 
 * 
 * 
 * @param <E>
 *            the type of values
 */
public final class SimpleReferenceValues<E> extends BaseObject implements ReferenceValues<E>, Serializable {

	private static final long serialVersionUID = 1L;

	private final E[] _values;

	@SafeVarargs
    public SimpleReferenceValues(E... values) {
		_values = values;
	}

	@Override
	public boolean containsValue(E value) {
		if (value == null) {
			for (E v : _values) {
				if (v == null) {
					return true;
				}
			}
		} else {
			for (E v : _values) {
				if (value.equals(v)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Collection<E> getValues() {
		return Arrays.asList(_values);
	}

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		identifiers.add(_values);
	}
}
