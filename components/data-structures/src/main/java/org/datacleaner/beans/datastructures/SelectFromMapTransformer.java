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

import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;

import org.apache.metamodel.util.CollectionUtils;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.MappedProperty;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.DataStructuresCategory;

/**
 * Transformer for selecting values from maps.
 */
@Named("Select values from key/value map")
@Description("Given a specified list of keys, this transformer will select the values from a key/value map and place them as columns within the record")
@Categorized(DataStructuresCategory.class)
public class SelectFromMapTransformer implements Transformer {

    private static final String PROPERTY_KEYS = "Keys";

    @Inject
    @Configured
    InputColumn<Map<String, ?>> mapColumn;

    @Inject
    @Configured(PROPERTY_KEYS)
    String[] keys;

    @Inject
    @Configured
    @MappedProperty(PROPERTY_KEYS)
    Class<?>[] types;

    @Inject
    @Configured
    @Description("Verify that expected type and actual type are the same")
    boolean verifyTypes = false;

    public void setKeys(String[] keys) {
        this.keys = keys;
    }

    public void setTypes(Class<?>[] types) {
        this.types = types;
    }

    public void setMapColumn(InputColumn<Map<String, ?>> mapColumn) {
        this.mapColumn = mapColumn;
    }

    public void setVerifyTypes(boolean verifyTypes) {
        this.verifyTypes = verifyTypes;
    }

    @Override
    public OutputColumns getOutputColumns() {
        String[] keys = this.keys;
        Class<?>[] types = this.types;
        if (keys.length != types.length) {
            // odd case sometimes encountered with invalid configurations or
            // while building a job
            final int length = Math.min(keys.length, types.length);
            keys = Arrays.copyOf(keys, length);
            types = Arrays.copyOf(types, length);
        }
        return new OutputColumns(keys, types);
    }

    @Override
    public Object[] transform(final InputRow row) {
        final Map<String, ?> map = row.getValue(mapColumn);
        final Object[] result = new Object[keys.length];

        if (map == null) {
            return result;
        }

        for (int i = 0; i < keys.length; i++) {
            Object value = find(map, keys[i]);
            if (verifyTypes) {
                value = types[i].cast(value);
            }
            result[i] = value;
        }

        return result;
    }

    /**
     * Searches a map for a given key. The key can be a regular map key, or a
     * simple expression of the form:
     * 
     * <ul>
     * <li>foo.bar (will lookup 'foo', and then 'bar' in a potential nested map)
     * </li>
     * <li>foo.bar[0].baz (will lookup 'foo', then 'bar' in a potential nested
     * map, then pick the first element in case it is a list/array and then pick
     * 'baz' from the potential map at that position).
     * </ul>
     * 
     * @param map
     *            the map to search in
     * @param key
     *            the key to resolve
     * @return the object in the map with the given key/expression. Or null if
     *         it does not exist.
     */
    public static Object find(Map<String, ?> map, String key) {
        return CollectionUtils.find(map, key);
    }
}
