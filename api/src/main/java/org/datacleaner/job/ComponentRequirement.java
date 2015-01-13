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

import java.io.Serializable;
import java.util.Collection;

import org.datacleaner.api.InputRow;

/**
 * Represents a requirement set on a {@link ComponentJob}, to only run it
 * conditionally.
 */
public interface ComponentRequirement extends Serializable {

    /**
     * Determines if the requirement is satisfied or not, given the available
     * outcomes of previous components.
     * 
     * @param row
     *            the current input row being processed. During flow process
     *            ordering, this parameter will be null.
     * @param availableOutcomes
     *            the {@link FilterOutcomes} that are currently active/available
     *            in the flow.
     * @return
     */
    public boolean isSatisfied(InputRow row, FilterOutcomes availableOutcomes);

    /**
     * Gets the {@link FilterOutcome}s that this requirement depends on at
     * processing time. During processing, each {@link FilterOutcome} returned
     * from this method will have been evaluated before calling
     * {@link #isSatisfied(FilterOutcomes)}.
     * 
     * @return
     */
    public Collection<FilterOutcome> getProcessingDependencies();

    /**
     * Gets a simple representation of this {@link ComponentRequirement}'s name,
     * whereas {@link #toString()} will usually contain more context and
     * technical information.
     * 
     * @return
     */
    public String getSimpleName();
}
