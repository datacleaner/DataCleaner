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
package org.datacleaner.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.metamodel.util.Func;

import com.google.common.collect.Maps;

/**
 * A utility function for reading properties as a {@link Map} of Strings from an
 * {@link InputStream}.
 */
public class InputStreamToPropertiesMapFunc implements Func<InputStream, Map<String, String>> {

    @Override
    public Map<String, String> eval(InputStream in) {
        final Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        final HashMap<String, String> map = Maps.newHashMapWithExpectedSize(properties.size());
        for (Entry<?, ?> e : properties.entrySet()) {
            final String key = (String) e.getKey();
            final String value = (String) e.getValue();
            map.put(key, value);
        }
        return map;
    }

}
