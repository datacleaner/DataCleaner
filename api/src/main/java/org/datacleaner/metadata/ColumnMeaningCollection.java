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
import java.util.stream.Collectors;

/**
 * A collection of {@link HasColumnMeaning} items.
 */
public interface ColumnMeaningCollection {
    /**
     * Returns all column meanings.
     * @return collection of column meanings
     */
    Collection<HasColumnMeaning> getColumnMeanings();

    /**
     * Default sorting of all column meanings.
     * @return sorted collection of column meanings
     */
    default Collection<HasColumnMeaning> getSortedColumnMeanings() {
        return getColumnMeanings().stream().sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the first column meaning found by a given name.
     * @param name name of the meaning
     * @return column meaning
     */
    HasColumnMeaning find(String name);

    /**
     * Returns the default/empty column meaning.
     * @return column meaning
     */
    default HasColumnMeaning getDefault() {
        return getColumnMeanings().iterator().next();
    }
}
