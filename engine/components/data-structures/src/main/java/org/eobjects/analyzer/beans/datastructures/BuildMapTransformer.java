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
package org.eobjects.analyzer.beans.datastructures;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Alias;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.DataStructuresCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transformer for building maps based on values in a row.
 */
@TransformerBean("Build key/value map")
@Alias("Build map")
@Description("Build a map with a variable amount of keys and values. "
        + "Adds the capability to store complex structures with named entries within it.")
@Categorized(DataStructuresCategory.class)
public class BuildMapTransformer implements Transformer<Map<String, ?>> {

    private static final Logger logger = LoggerFactory.getLogger(BuildMapTransformer.class);

    @Inject
    @Configured
    InputColumn<?>[] values;

    @Inject
    @Configured
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

    public void setIncludeNullValues(boolean includeNullValues) {
        this.includeNullValues = includeNullValues;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }

    public void setRetainKeyOrder(boolean retainKeyOrder) {
        this.retainKeyOrder = retainKeyOrder;
    }

    public void setValues(InputColumn<?>[] values) {
        this.values = values;
    }

    @Override
    public OutputColumns getOutputColumns() {
        StringBuilder sb = new StringBuilder("Map: ");
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            sb.append(key);
            if (sb.length() > 30) {
                sb.append("...");
                break;
            }

            if (i + 1 < keys.length) {
                sb.append(",");
            }
        }
        OutputColumns outputColumns = new OutputColumns(new String[] { sb.toString() }, new Class[] { Map.class });
        return outputColumns;
    }

    @Override
    public Map<String, ?>[] transform(InputRow row) {
        final Map<String, Object> existingMap;
        if (addToExistingMap != null) {
            existingMap = row.getValue(addToExistingMap);
        } else {
            existingMap = Collections.emptyMap();
        }

        final Map<String, Object> map;
        if (retainKeyOrder) {
            map = new LinkedHashMap<String, Object>(existingMap);
        } else {
            map = new HashMap<String, Object>(existingMap);
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

        @SuppressWarnings("unchecked")
        Map<String, ?>[] result = new Map[] { map };

        return result;
    }

}
