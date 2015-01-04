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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.metamodel.util.BaseObject;

public final class SimpleStringReferenceValues extends BaseObject implements ReferenceValues<String> {

    private final Set<String> _values;
    private final boolean _caseSensitive;

    public SimpleStringReferenceValues(String[] values, boolean caseSensitive) {
        _values = new HashSet<String>();
        for (String value : values) {
            _values.add(value);
        }
        _caseSensitive = caseSensitive;
    }

    public SimpleStringReferenceValues(Collection<String> values, boolean caseSensitive) {
        if (values instanceof Set<?>) {
            _values = (Set<String>) values;
        } else {
            _values = new HashSet<String>(values);
        }
        _caseSensitive = caseSensitive;
    }

    @Override
    protected void decorateIdentity(List<Object> identifiers) {
        identifiers.add(_values);
        identifiers.add(_caseSensitive);

    }

    @Override
    public Collection<String> getValues() {
        return Collections.unmodifiableSet(_values);
    }

    @Override
    public boolean containsValue(String value) {
        boolean contains = _values.contains(value);

        if (contains) {
            return true;
        }

        if (_caseSensitive) {
            return false;
        }

        for (String v : _values) {
            if (value.equalsIgnoreCase(v)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "SimpleStringReferenceValues[" + _values + "]";
    }
}
