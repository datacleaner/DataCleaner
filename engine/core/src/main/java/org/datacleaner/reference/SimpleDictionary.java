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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.util.ReadObjectBuilder;
import org.datacleaner.util.ReadObjectBuilder.Adaptor;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

/**
 * The simplest possible Dictionary implementation. Based on an in-memory
 * {@link Set} of values.
 */
public final class SimpleDictionary extends AbstractReferenceData implements Dictionary {

    private static final long serialVersionUID = 1L;

    private final Set<String> _valueSet;
    private final boolean _caseSensitive;

    public SimpleDictionary(final String name, final String... values) {
        this(name, true, values);
    }

    public SimpleDictionary(final String name, final boolean caseSensitive, final String... values) {
        this(name, createValueSet(values, caseSensitive), caseSensitive);
    }

    public SimpleDictionary(final String name, final Collection<String> values) {
        this(name, values, false);
    }

    public SimpleDictionary(final String name, final Collection<String> values, final boolean caseSensitive) {
        super(name);
        if (caseSensitive) {
            _valueSet = Sets.newHashSet(values);
        } else {
            _valueSet = createValueSet(values.iterator(), caseSensitive);
        }
        _caseSensitive = caseSensitive;
    }

    private static Set<String> createValueSet(final Object[] array, final boolean caseSensitive) {
        return createValueSet(Iterators.forArray(array), caseSensitive);
    }

    private static Set<String> createValueSet(final Iterator<?> iterator, final boolean caseSensitive) {
        final Set<String> valueSet = Sets.newHashSet();
        while (iterator.hasNext()) {
            final Object value = iterator.next();
            if (value != null) {
                if (caseSensitive) {
                    valueSet.add(value.toString());
                } else {
                    valueSet.add(value.toString().toLowerCase());
                }
            }
        }
        return valueSet;
    }

    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        final Adaptor adaptor = (getField, serializable) -> {
            final boolean caseSensitive = getField.get("_caseSensitive", true);

            // handle potentially missing caseSensitive flag
            {
                final Field caseSensitiveField = SimpleDictionary.class.getDeclaredField("_caseSensitive");
                caseSensitiveField.setAccessible(true);
                caseSensitiveField.set(serializable, caseSensitive);
            }

            // handle legacy SimpleReferenceValues based data
            try {
                final Object oldValues = getField.get("_values", null);
                if (oldValues != null) {
                    @SuppressWarnings("deprecation") final SimpleReferenceValues srv =
                            (SimpleReferenceValues) oldValues;
                    @SuppressWarnings("deprecation") final Object[] values = srv.getValues();
                    final Set<String> valueSet = createValueSet(values, caseSensitive);

                    final Field valuesField = SimpleDictionary.class.getDeclaredField("_valueSet");
                    valuesField.setAccessible(true);
                    valuesField.set(serializable, valueSet);
                }
            } catch (final IllegalArgumentException e) {
                // happens for newer versions of the object type.
            }
        };
        ReadObjectBuilder.create(this, SimpleDictionary.class).readObject(stream, adaptor);
    }

    @Override
    public boolean equals(final Object obj) {
        if (super.equals(obj)) {
            final SimpleDictionary other = (SimpleDictionary) obj;
            return Objects.equals(_valueSet, other._valueSet) && Objects.equals(_caseSensitive, other._caseSensitive);
        }
        return false;
    }

    @Override
    public DictionaryConnection openConnection(final DataCleanerConfiguration configuration) {
        return new DictionaryConnection() {

            @Override
            public Iterator<String> getAllValues() {
                return _valueSet.iterator();
            }

            @Override
            public Iterator<String> getLengthSortedValues() {
                final SortedSet<String> connectionValueSet = new TreeSet<>(
                        Comparator.comparingInt(String::length).reversed().thenComparing(String::compareTo));
                connectionValueSet.addAll(_valueSet);
                return connectionValueSet.iterator();
            }

            @Override
            public boolean containsValue(String value) {
                if (value == null) {
                    return false;
                }
                if (!_caseSensitive) {
                    value = value.toLowerCase();
                }
                return _valueSet.contains(value);
            }

            @Override
            public void close() {
            }
        };
    }

    public Set<String> getValueSet() {
        return _valueSet;
    }

    @Override
    public boolean isCaseSensitive() {
        return _caseSensitive;
    }
}
