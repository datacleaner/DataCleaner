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
package org.datacleaner.descriptors;

import java.util.EnumSet;
import java.util.Set;

import org.datacleaner.api.Filter;
import org.datacleaner.api.Optimizeable;
import org.datacleaner.api.QueryOptimizedFilter;

/**
 * {@link ComponentDescriptor} interface for {@link Filter}s.
 * 
 * @param <B>
 *            the type of {@link Filter}
 */
public interface FilterDescriptor<F extends Filter<C>, C extends Enum<C>> extends ComponentDescriptor<F> {

    public Class<C> getOutcomeCategoryEnum();

    public EnumSet<C> getOutcomeCategories();

    public Set<String> getOutcomeCategoryNames();

    public Enum<C> getOutcomeCategoryByName(String category);

    /**
     * Determine whether this {@link Filter} is query optimizable or not. See
     * {@link QueryOptimizedFilter} and {@link Optimizeable} for more details.
     * 
     * @return true if this filter can be query optimized.
     */
    public boolean isQueryOptimizable();
}
