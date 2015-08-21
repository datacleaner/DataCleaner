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
package org.datacleaner.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.datacleaner.util.ReflectionUtils;
import org.apache.metamodel.util.Ref;
import org.apache.metamodel.util.SerializableRef;

public final class Crosstab<E extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<CrosstabDimension> dimensions;
    private final Map<String, E> values = new HashMap<String, E>();
    private final Class<E> valueClass;
    private final Map<String, Ref<ResultProducer>> resultProducers = new HashMap<String, Ref<ResultProducer>>();

    public Crosstab(Class<E> valueClass, CrosstabDimension... dimensions) {
        this.valueClass = valueClass;
        this.dimensions = Arrays.asList(dimensions);
    }

    public Crosstab(Class<E> valueClass, Collection<CrosstabDimension> dimensions) {
        this.valueClass = valueClass;
        this.dimensions = new ArrayList<CrosstabDimension>(dimensions);
    }

    public Crosstab(Class<E> valueClass, String... dimensionNames) {
        this.valueClass = valueClass;
        dimensions = new ArrayList<CrosstabDimension>();
        for (String name : dimensionNames) {
            dimensions.add(new CrosstabDimension(name));
        }
    }

    public Class<E> getValueClass() {
        return valueClass;
    }

    @SuppressWarnings("unchecked")
    public <T extends Serializable> Crosstab<T> castValueClass(Class<T> valueClass) {
        if (ReflectionUtils.is(this.valueClass, valueClass)) {
            return (Crosstab<T>) this;
        }
        throw new IllegalArgumentException("Unable to cast [" + this.valueClass + "] to [" + valueClass + "]");
    }

    public List<CrosstabDimension> getDimensions() {
        return Collections.unmodifiableList(dimensions);
    }

    private String getKey(String[] categories) throws IllegalArgumentException, NullPointerException {
        if (categories.length != dimensions.size()) {
            throw new IllegalArgumentException(
                    "Not all dimensions have been specified (differences in size of parameter and Crosstab's dimensions)");
        }
        for (int i = 0; i < categories.length; i++) {
            if (categories[i] == null) {
                CrosstabDimension dimension = dimensions.get(i);
                throw new NullPointerException("Not all dimensions have been specified ('" + dimension.getName()
                        + "' is null)");

            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < categories.length; i++) {
            CrosstabDimension dimension = dimensions.get(i);
            String category = categories[i];
            if (!dimension.containsCategory(category)) {
                throw new IllegalArgumentException("Unknown category [" + category + "] for dimension ["
                        + dimension.getName() + "]");
            }
            if (i != 0) {
                sb.append('^');
            }
            sb.append(category);
        }
        return sb.toString();
    }

    public CrosstabNavigator<E> navigate() {
        return new CrosstabNavigator<E>(this);
    }

    protected E getValue(String[] categories) throws IllegalArgumentException, NullPointerException {
        String key = getKey(categories);
        return values.get(key);
    }

    public CrosstabNavigator<E> where(String dimension, String isCategory) {
        return navigate().where(dimension, isCategory);
    }

    public CrosstabNavigator<E> where(CrosstabDimension dimension, String isCategory) {
        return navigate().where(dimension, isCategory);
    }

    protected void putValue(E value, String[] categories) throws IllegalArgumentException, NullPointerException {
        if (value != null) {
            if (!ReflectionUtils.is(value.getClass(), valueClass)) {
                throw new IllegalArgumentException("Cannot put value [" + value + "] of type [" + value.getClass()
                        + "] when Crosstab.valueClass is [" + valueClass + "]");
            }
        }
        String key = getKey(categories);
        values.put(key, value);
    }

    public int getDimensionCount() {
        return dimensions.size();
    }

    public String[] getDimensionNames() {
        int size = dimensions.size();
        String[] result = new String[size];
        for (int i = 0; i < size; i++) {
            result[i] = dimensions.get(i).getName();
        }
        return result;
    }

    public int getDimensionIndex(CrosstabDimension dimension) {
        if (dimension != null) {
            int size = dimensions.size();
            for (int i = 0; i < size; i++) {
                if (dimension.equals(dimensions.get(i))) {
                    return i;
                }
            }
        }
        throw new IllegalArgumentException("No such dimension: " + dimension);
    }

    public int getDimensionIndex(String dimensionName) {
        if (dimensionName != null) {
            int size = dimensions.size();
            for (int i = 0; i < size; i++) {
                if (dimensionName.equals(dimensions.get(i).getName())) {
                    return i;
                }
            }
        }
        throw new IllegalArgumentException("No such dimension: " + dimensionName);
    }

    public CrosstabDimension getDimension(int i) {
        return dimensions.get(i);
    }

    public CrosstabDimension getDimension(String dimensionName) {
        return getDimension(getDimensionIndex(dimensionName));
    }

    protected void attachResultProducer(ResultProducer resultProducer, String[] categories)
            throws IllegalArgumentException, NullPointerException {
        String key = getKey(categories);

        if (resultProducer == null) {
            resultProducers.remove(key);
        } else {
            Ref<ResultProducer> resultProducerRef = new SerializableRef<ResultProducer>(resultProducer);
            resultProducers.put(key, resultProducerRef);
        }
    }

    protected ResultProducer explore(String[] categories) {
        String key = getKey(categories);
        Ref<ResultProducer> resultProducerRef = resultProducers.get(key);
        if (resultProducerRef == null) {
            return null;
        }
        return resultProducerRef.get();
    }

    @Override
    public String toString() {
        return toString(8);
    }

    /**
     * Returns a string representation with a maximum restraint on the amount of
     * crosstab entries to include.
     * 
     * @param maxEntries
     *            the maximum amount of crosstab entries to include, or negative
     *            if all entries should be included.
     * @return
     */
    public String toString(int maxEntries) {
        StringBuilder sb = new StringBuilder("Crosstab:");

        Set<String> keySet = new TreeSet<String>(values.keySet());
        for (String key : keySet) {
            if (maxEntries == 0) {
                break;
            }
            sb.append('\n');
            sb.append(key.replaceAll("\\^", ","));
            sb.append(": ");
            E value = values.get(key);
            sb.append(value);

            maxEntries--;
        }

        return sb.toString();
    }
}
