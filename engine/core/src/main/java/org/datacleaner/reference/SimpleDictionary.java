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
import java.io.ObjectInputStream.GetField;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

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

    public SimpleDictionary(String name, String... values) {
        this(name, true, values);
    }

    public SimpleDictionary(String name, boolean caseSensitive, String... values) {
        this(name, createValueSet(values, caseSensitive), caseSensitive);
    }

    public SimpleDictionary(String name, Collection<String> values) {
        this(name, values, false);
    }

    public SimpleDictionary(String name, Collection<String> values, boolean caseSensitive) {
        super(name);
        if (caseSensitive) {
            _valueSet = Sets.newHashSet(values);
        } else {
            _valueSet = createValueSet(values.iterator(), caseSensitive);
        }
        _caseSensitive = caseSensitive;
    }

    private static Set<String> createValueSet(Object[] array, boolean caseSensitive) {
        return createValueSet(Iterators.forArray(array), caseSensitive);
    }

    private static Set<String> createValueSet(Iterator<?> iterator, boolean caseSensitive) {
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

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        Adaptor adaptor = new Adaptor() {
            @Override
            public void deserialize(GetField getField, Serializable serializable) throws Exception {
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
                        @SuppressWarnings("deprecation")
                        SimpleReferenceValues srv = (SimpleReferenceValues) oldValues;
                        @SuppressWarnings("deprecation")
                        final Object[] values = srv.getValues();
                        final Set<String> valueSet = createValueSet(values, caseSensitive);
                        
                        final Field valuesField = SimpleDictionary.class.getDeclaredField("_valueSet");
                        valuesField.setAccessible(true);
                        valuesField.set(serializable, valueSet);
                    }
                } catch (IllegalArgumentException e) {
                    // happens for newer versions of the object type.
                }
            }
        };
        ReadObjectBuilder.create(this, SimpleDictionary.class).readObject(stream, adaptor);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            final SimpleDictionary other = (SimpleDictionary) obj;
            return Objects.equals(_valueSet, other._valueSet) && Objects.equals(_caseSensitive, other._caseSensitive);
        }
        return false;
    }

    @Override
    public DictionaryConnection openConnection(DataCleanerConfiguration configuration) {
        return new DictionaryConnection() {
            @Override
            public Iterator<String> getAllValues() {
                return _valueSet.iterator();
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

    public boolean isCaseSensitive() {
        return _caseSensitive;
    }
}
