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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.datacleaner.api.InputRow;

/**
 * Represents an outcome that is a product of multiple other outcomes and thus
 * requires just any of the provided outcomes.
 */
public class CompoundComponentRequirement implements ComponentRequirement {

    private static final long serialVersionUID = 1L;

    private final Set<FilterOutcome> _outcomes;

    public CompoundComponentRequirement(Collection<? extends FilterOutcome> outcomes) {
        _outcomes = new LinkedHashSet<FilterOutcome>();
        for (FilterOutcome outcome : outcomes) {
            _outcomes.add(outcome);
        }
    }

    public CompoundComponentRequirement(FilterOutcome... outcomes) {
        _outcomes = new LinkedHashSet<FilterOutcome>();
        for (FilterOutcome outcome : outcomes) {
            _outcomes.add(outcome);
        }
    }

    public CompoundComponentRequirement(ComponentRequirement existingRequirement, FilterOutcome filterOutcome) {
        _outcomes = new LinkedHashSet<FilterOutcome>();

        _outcomes.addAll(existingRequirement.getProcessingDependencies());
        _outcomes.add(filterOutcome);
    }

    /**
     * Gets the {@link FilterOutcome} that this
     * {@link CompoundComponentRequirement} represents.
     * 
     * @return
     */
    public Set<FilterOutcome> getOutcomes() {
        return _outcomes;
    }

    public Set<FilterOutcome> getOutcomesFrom(HasFilterOutcomes producingComponent) {
        Set<FilterOutcome> result = new LinkedHashSet<>();
        for (FilterOutcome outcome : _outcomes) {
            final HasFilterOutcomes source = outcome.getSource();
            if (producingComponent.equals(source)) {
                result.add(outcome);
            }
        }
        return result;
    }

    public boolean hasMultipleRequirementsFrom(HasFilterOutcomes producingComponent) {
        int count = 0;
        for (FilterOutcome outcome : _outcomes) {
            final HasFilterOutcomes source = outcome.getSource();
            if (producingComponent.equals(source)) {
                count++;
            }
        }
        return count > 1;
    }

    @Override
    public Collection<FilterOutcome> getProcessingDependencies() {
        return getOutcomes();
    }

    @Override
    public boolean isSatisfied(InputRow row, FilterOutcomes outcomes) {
        for (FilterOutcome outcome : outcomes.getOutcomes()) {
            if (_outcomes.contains(outcome)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getSimpleName() {
        final StringBuilder sb = new StringBuilder();
        for (FilterOutcome outcome : _outcomes) {
            if (sb.length() != 0) {
                sb.append(" OR ");
            }
            sb.append(outcome.getSimpleName());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (FilterOutcome outcome : _outcomes) {
            if (sb.length() != 0) {
                sb.append(" OR ");
            }
            sb.append(outcome.toString());
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_outcomes == null) ? 0 : _outcomes.hashCode());
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
        final CompoundComponentRequirement other = (CompoundComponentRequirement) obj;
        if (_outcomes == null) {
            if (other._outcomes != null)
                return false;
        } else if (!_outcomes.equals(other._outcomes))
            return false;
        return true;
    }
}
