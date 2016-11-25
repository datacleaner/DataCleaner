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
package org.datacleaner.beans.datastructures;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.api.Alias;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.MappedProperty;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.DataStructuresCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transformer for building maps based on values in a row.
 */
@Named("Build key/value map")
@Alias("Build map")
@Description("Build a map with a variable amount of keys and values. "
        + "Adds the capability to store complex structures with named entries within it.")
@Categorized(DataStructuresCategory.class)
public class BuildMapTransformer implements Transformer {

    private static final Logger logger = LoggerFactory.getLogger(BuildMapTransformer.class);

    private static final String PROPERTY_VALUES = "Values";

    @Inject
    @Configured(PROPERTY_VALUES)
    InputColumn<?>[] values;

    @Inject
    @Configured
    @MappedProperty(PROPERTY_VALUES)
    String[] keys;

    @Inject
    @Configured
    boolean retainKeyOrder = false;

    @Inject
    @Configured
    boolean includeNullValues = false;

    @Inject
    @Configured(required = false)
    @Description("Add key/value pairs to this (optional) existing map")
    InputColumn<Map<String, Object>> addToExistingMap;

    public void setIncludeNullValues(final boolean includeNullValues) {
        this.includeNullValues = includeNullValues;
    }

    public void setKeys(final String[] keys) {
        this.keys = keys;
    }

    public void setRetainKeyOrder(final boolean retainKeyOrder) {
        this.retainKeyOrder = retainKeyOrder;
    }

    public void setValues(final InputColumn<?>[] values) {
        this.values = values;
    }

    @Override
    public OutputColumns getOutputColumns() {
        final StringBuilder sb = new StringBuilder("Map: ");
        for (int i = 0; i < keys.length; i++) {
            final String key = keys[i];
            sb.append(key);
            if (sb.length() > 30) {
                sb.append("...");
                break;
            }

            if (i + 1 < keys.length) {
                sb.append(",");
            }
        }
        return new OutputColumns(new String[] { sb.toString() }, new Class[] { Map.class });
    }

    @Override
    public Map<String, ?>[] transform(final InputRow row) {
        final Map<String, Object> existingMap;
        if (addToExistingMap != null) {
            existingMap = row.getValue(addToExistingMap);
        } else {
            existingMap = Collections.emptyMap();
        }

        final Map<String, Object> map;
        if (retainKeyOrder) {
            map = new LinkedHashMap<>(existingMap);
        } else {
            map = new HashMap<>(existingMap);
        }

        for (int i = 0; i < keys.length; i++) {
            final String key = keys[i];
            final Object value = row.getValue(values[i]);
            if (value == null && !includeNullValues) {
                logger.debug("Ignoring null value for {} in row: {}", key, row);
            } else {
                map.put(key, value);
            }
        }

        @SuppressWarnings("unchecked") final Map<String, ?>[] result = new Map[] { map };

        return result;
    }

}
