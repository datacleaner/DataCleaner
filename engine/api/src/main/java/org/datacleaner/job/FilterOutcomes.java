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
package org.datacleaner.job;


/**
 * Interface for RowProcessingConsumers to add outcomes to and for others to
 * detect the {@link FilterOutcome} state of a record.
 */
public interface FilterOutcomes extends Cloneable {

    /**
     * Adds a {@link FilterOutcome} to the set of active outcomes
     * 
     * @param filterOutcome
     */
    public void add(FilterOutcome filterOutcome);

    /**
     * Gets the currently active outcomes.
     * 
     * @return
     */
    public FilterOutcome[] getOutcomes();

    /**
     * Determines if a particular outcome is active in the current state.
     * 
     * @param outcome
     * @return
     */
    public boolean contains(FilterOutcome outcome);

    /**
     * Clones the instance.
     * 
     * @return
     */
    public FilterOutcomes clone();
}
