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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultColumnMeaningCollection implements ColumnMeaningCollection {
    private static Map<String, HasColumnMeaning> _matchingMap;

    private static Map<String, HasColumnMeaning> getMatchingMap() {
        if (_matchingMap == null) {
            _matchingMap = new HashMap<>();
            final EnumSet<ColumnMeaning> meanings = EnumSet.allOf(ColumnMeaning.class);

            for (final HasColumnMeaning columnMeaning : meanings) {
                populateMatchMap(columnMeaning.getName(), columnMeaning);

                for (final String alias : columnMeaning.getAliases()) {
                    populateMatchMap(alias, columnMeaning);
                }
            }
        }

        return _matchingMap;
    }

    private static void populateMatchMap(String key, final HasColumnMeaning columnMeaning) {
        key = standardizeForMatching(key);
        final HasColumnMeaning oldValue = getMatchingMap().put(key, columnMeaning);

        if (oldValue != null) {
            throw new IllegalStateException("Multiple ColumnMeanings with name/alias: " + key);
        }
    }

    private static String standardizeForMatching(String key) {
        key = key.trim().toLowerCase();
        key = replaceAll(key, ".", "");
        key = replaceAll(key, ",", "");
        key = replaceAll(key, "'", "");
        key = replaceAll(key, " ", "");
        key = replaceAll(key, "_", "");
        key = replaceAll(key, "-", "");
        // remove all the numbers at the end of a string to avoid words like
        // ADDRESSLINE1 being mapped to OTHER
        key = key.replaceAll("\\d*$", "");
        // remove the 'FLD' prefix of some fields such as FLD_FIRSTNAME
        // so it can be mapped properly
        key = replaceAll(key, "fld" , "");

        return key;
    }

    private static String replaceAll(String str, final String searchFor, final String replaceWith) {
        while (str.contains(searchFor)) {
            str = str.replace(searchFor, replaceWith);
        }

        return str;
    }

    @Override
    public Collection<HasColumnMeaning> getColumnMeanings() {
        final Set<HasColumnMeaning> set = new HashSet<>();

        for (final HasColumnMeaning meaning : getMatchingMap().values()) {
            set.add(meaning);
        }

        return set;
    }

    @Override
    public HasColumnMeaning find(final String name) {
        return getMatchingMap().get(standardizeForMatching(name));
    }

    @Override
    public HasColumnMeaning getDefault() {
        return ColumnMeaning.OTHER;
    }
}
