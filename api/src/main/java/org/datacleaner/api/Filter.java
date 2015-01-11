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
package org.datacleaner.api;

import javax.inject.Named;

/**
 * Interface for components that filter/categorize rows.
 * 
 * A filter will process incoming rows and label them with a category. A
 * category is defined as a value in an enum. When a row is categorized, this
 * category can then be used to set up a requirement for succeeding row
 * processing.
 * 
 * Use of the {@link Named} annotation is required for the filter to be
 * automatically discovered.
 * 
 * A sub-interface of Filter exists, {@link QueryOptimizedFilter}, which allows
 * filter functionality to be pushed down to the query in certain circumstances
 * where it is desirable to do so.
 * 
 * @param <C>
 *            an enum type with the available categories
 */
public interface Filter<C extends Enum<C>> {

    /**
     * Categorizes/filters a single row.
     * 
     * @param inputRow
     *            the row to categorize.
     * @return an enum representing the category applied to the row.
     */
    public C categorize(InputRow inputRow);
}
