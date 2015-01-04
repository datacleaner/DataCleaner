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

import java.util.Arrays;
import java.util.Collection;

import org.datacleaner.data.InputRow;
import org.datacleaner.job.runner.FilterOutcomes;
import org.datacleaner.util.LabelUtils;

public class SimpleComponentRequirement implements ComponentRequirement {

    private static final long serialVersionUID = 1L;

    private final FilterOutcome _outcome;

    public SimpleComponentRequirement(FilterOutcome outcome) {
        if (outcome == null) {
            throw new IllegalArgumentException("FilterOutcome cannot be null");
        }
        _outcome = outcome;
    }
    
    /**
     * Gets the outcome that this {@link ComponentRequirement} represents
     * @return
     */
    public FilterOutcome getOutcome() {
        return _outcome;
    }
    
    @Override
    public Collection<FilterOutcome> getProcessingDependencies() {
        return Arrays.asList(_outcome);
    }

    @Override
    public boolean isSatisfied(InputRow row, FilterOutcomes outcomes) {
        return outcomes.contains(_outcome);
    }

    @Override
    public String toString() {
        final String filterLabel = LabelUtils.getLabel(_outcome.getFilterJob());
        return filterLabel + "=" + _outcome.getCategory();
    }
    
    @Override
    public String getSimpleName() {
        return _outcome.getCategory() + "";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_outcome == null) ? 0 : _outcome.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SimpleComponentRequirement other = (SimpleComponentRequirement) obj;
        if (_outcome == null) {
            if (other._outcome != null)
                return false;
        } else if (!_outcome.equals(other._outcome))
            return false;
        return true;
    }
}
