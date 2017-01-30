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
package org.datacleaner.metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DefaultColumnMeaningCollection implements ColumnMeaningCollection {
    private static Map<String, HasColumnMeaning> _matchingMap;

    static {
        _matchingMap = new HashMap<>();
        final HasColumnMeaning[] values = ColumnMeaning.class.getEnumConstants();

        for (final HasColumnMeaning columnMeaning : values) {
            populateMatchMap(columnMeaning.getName(), columnMeaning);
            final String[] aliases = columnMeaning.getAliases();

            for (final String alias : aliases) {
                populateMatchMap(alias, columnMeaning);
            }
        }
    }

    private static void populateMatchMap(String key, final HasColumnMeaning columnMeaning) {
        key = standardizeForMatching(key);
        final HasColumnMeaning oldValue = _matchingMap.put(key, columnMeaning);

        if (oldValue != null) {
            throw new IllegalStateException("Multiple ColumnMeanings with name/alias: " + key);
        }
    }

    private static String standardizeForMatching(String key) {
        key = key.trim();
        key = replaceAll(key, ".", "");
        key = replaceAll(key, ",", "");
        key = replaceAll(key, "'", "");
        key = replaceAll(key, " ", "");
        key = replaceAll(key, "_", "");
        key = replaceAll(key, "-", "");

        return key.toLowerCase();
    }

    private static String replaceAll(String str, final String searchFor, final String replaceWith) {
        while (str.indexOf(searchFor) != -1) {
            str = str.replace(searchFor, replaceWith);
        }

        return str;
    }

    /**
     * Returns all column meanings.
     * @return collection of column meanings
     */
    @Override
    public Collection<HasColumnMeaning> getColumnMeanings() {
        return _matchingMap.values();
    }

    /**
     * Returns the first column meaning found by a given name.
     * @param name
     * @return column meaning
     */
    @Override
    public HasColumnMeaning find(String name) {
        return _matchingMap.get(name);
    }
}
