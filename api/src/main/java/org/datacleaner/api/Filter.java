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
 * Interface for a {@link Component} that filters/categorizes rows.
 * 
 * See {@link Component} for general information about all components. Like all
 * components, {@link Analyzer} require a {@link Named} annotation in order to
 * be discovered.
 * 
 * A {@link Filter} will process incoming rows and label them with a category. A
 * category is defined as a value in an enum. When a row is categorized, this
 * category can then be used to set up a requirement for succeeding row
 * processing.
 * 
 * A sub-interface of Filter exists, {@link QueryOptimizedFilter}, which allows
 * filter functionality to be pushed down to the query in certain circumstances
 * where it is desirable to do so. Also check out the {@link Optimizeable}
 * annotation which may be useful when applying {@link QueryOptimizedFilter}.
 * 
 * @param <C>
 *            an enum type with the available categories
 * 
 * @since 4.0
 */
public interface Filter<C extends Enum<C>> extends Component {

    /**
     * Categorizes/filters a single row.
     * 
     * @param inputRow
     *            the row to categorize.
     * @return an enum representing the category applied to the row.
     */
    public C categorize(InputRow inputRow);
}
