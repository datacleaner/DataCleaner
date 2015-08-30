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
package org.datacleaner.reference;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import org.datacleaner.configuration.DataCleanerConfiguration;

import com.google.common.collect.Sets;

/**
 * The simplest possible Dictionary implementation. Based on an in-memory
 * {@link Set} of values.
 */
public final class SimpleDictionary extends AbstractReferenceData implements Dictionary {

    private static final long serialVersionUID = 1L;

    private final Set<String> _values;

    public SimpleDictionary(String name, String... values) {
        super(name);
        _values = Sets.newHashSet(values);
    }

    public SimpleDictionary(String name, Collection<String> values) {
        super(name);
        _values = Sets.newHashSet(values);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            final SimpleDictionary other = (SimpleDictionary) obj;
            return Objects.equals(_values, other._values);
        }
        return false;
    }

    @Override
    public DictionaryConnection openConnection(DataCleanerConfiguration configuration) {
        return new DictionaryConnection() {
            @Override
            public Iterator<String> getAllValues() {
                return _values.iterator();
            }

            @Override
            public boolean containsValue(String value) {
                return _values.contains(value);
            }

            @Override
            public void close() {
            }
        };
    }

    public Set<String> getValues() {
        return _values;
    }
}
