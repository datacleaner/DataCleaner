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

import org.apache.metamodel.query.Query;

/**
 * A filter which can, under certain circumstances, be optimized by using a
 * "push down" technique, where the filter's functionality is expressed in the
 * query that feeds the job that the filter is entered into.
 * 
 * Query optimized filters should implement BOTH the regular categorize(...)
 * method and the optimization-methods in this interface. There is no guarantee
 * that the filter will be optimizing the query, but in cases where a filter is
 * among the first steps in a job, and all succeeding steps depend on a single
 * outcome of the particular filter, it will be allowed to optimize the query.
 * 
 * @param <C>
 *            the filter category enum
 */
public interface QueryOptimizedFilter<C extends Enum<C>> extends Filter<C> {

	/**
	 * Inquires the filter if a given category is optimizable by the use of a
	 * query.
	 * 
	 * @param category
	 *            the category to optimize
	 * @return a boolean indicating whether or not the provided category is
	 *         query optimizable.
	 */
	public boolean isOptimizable(C category);

	/**
	 * Optimizes the filter execution by retrieving a query (as opposed to
	 * invoking the categorize(...) method for each row).
	 * 
	 * This method will only be invoked if a preceding call to
	 * isOptimizable(...) with the same category returned true.
	 * 
	 * @param q
	 *            the query to optimize.
	 * @param category
	 *            the filter category to optimize.
	 * @return a new query (or a mutated version of the parameterized query)
	 *         that includes query optimization.
	 */
	public Query optimizeQuery(Query q, C category);
}
