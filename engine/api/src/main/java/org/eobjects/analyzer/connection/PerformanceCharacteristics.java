/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.connection;

/**
 * Represents the performance characteristics of a {@link Datastore}.
 * Performance characteristics can be used to optimize the execution plan of a
 * job or interaction with the datastore.
 */
public interface PerformanceCharacteristics {

    /**
     * If the datastore has it's own (native) query engine, it is often best to
     * optimize at the query level in order to save IO. Some datastores however,
     * have poor query engines or very good IO metrics, which makes it better to
     * fire just a simple query and do filtering etc. on the client. This method
     * can be used to determine which mode is preferred.
     * 
     * @return true if it is plausible to optimize the usage of this datastore
     *         by intelligent querying, or false if complex querying will not be
     *         of large benefit (typically because the datastore does
     *         client-side query handling).
     */
    public boolean isQueryOptimizationPreferred();

    /**
     * Determines if the datastore's natural order of records is consistent
     * across queries. This is typically used for paginated queries where LIMIT
     * and OFFSET clauses may be added to a query without any additional ORDER
     * BY clause.
     * 
     * Some datastores will always return the same ordering of records when no
     * ORDER BY clause is provided. These datastores will return true on this
     * call.
     * 
     * Other datastores will not have a guaranteed natural ordering, except if
     * an ORDER BY clause is specified. These datastores will return false on
     * this call.
     * 
     * @return true if this datastore has a consistent natural ordering of
     *         records when no ORDER BY clause is specified.
     */
    public boolean isNaturalRecordOrderConsistent();
}
