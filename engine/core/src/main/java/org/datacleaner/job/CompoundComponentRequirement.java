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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.metamodel.util.HasName;
import org.datacleaner.api.InputRow;

/**
 * Represents an outcome that is a product of multiple other outcomes and thus
 * requires just any of the provided outcomes.
 */
public class CompoundComponentRequirement implements ComponentRequirement {
    private static final long serialVersionUID = 1L;

    public enum CompundingType implements HasName {
        ANY("OR"), ALL("AND");

        private final String _name;

        CompundingType(String name) {
            _name = name;
        }

        public String getName() {
            return _name;
        }

        public String toString() {
            return " " + getName() + " ";
        }
    }

    private final Set<FilterOutcome> _outcomes;
    private final CompundingType _compoundingType;

    public CompoundComponentRequirement(CompundingType compoundingType, Collection<? extends FilterOutcome> outcomes) {
        _outcomes = new LinkedHashSet<>(outcomes);
        _compoundingType = compoundingType;
    }

    public CompoundComponentRequirement(Collection<? extends FilterOutcome> outcomes) {
        this(CompundingType.ANY, outcomes);
    }

    public CompoundComponentRequirement(CompundingType compoundingType, FilterOutcome... outcomes) {
        _outcomes = new LinkedHashSet<>();
        Collections.addAll(_outcomes, outcomes);
        _compoundingType = compoundingType;
    }

    public CompoundComponentRequirement(FilterOutcome... outcomes) {
        this(CompundingType.ANY, outcomes);
    }

    public CompoundComponentRequirement(CompundingType compoundingType, ComponentRequirement existingRequirement,
            FilterOutcome filterOutcome) {
        _outcomes = new LinkedHashSet<>();
        _outcomes.addAll(existingRequirement.getProcessingDependencies());
        _outcomes.add(filterOutcome);
        _compoundingType = compoundingType;
    }

    public CompoundComponentRequirement(ComponentRequirement existingRequirement, FilterOutcome filterOutcome) {
        this(CompundingType.ANY, existingRequirement, filterOutcome);
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

    public CompundingType getCompoundingType() {
        return _compoundingType;
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
        if(_compoundingType == CompundingType.ANY || row == null) {
            for (FilterOutcome outcome : outcomes.getOutcomes()) {
                if (_outcomes.contains(outcome)) {
                    return true;
                }
            }
            return false;
        } else {
            for (FilterOutcome outcome : outcomes.getOutcomes()) {
                if (!_outcomes.contains(outcome)) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public String getSimpleName() {
        final StringBuilder sb = new StringBuilder();
        for (FilterOutcome outcome : _outcomes) {
            if (sb.length() != 0) {
                sb.append(_compoundingType);
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
                sb.append(_compoundingType);
            }
            sb.append(outcome.toString());
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(_outcomes, _compoundingType);
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

        // TODO: This wasn't anything equivalent to deepEquals before... But should it have been?
        return Objects.equals(_outcomes, other._outcomes) && _compoundingType == other._compoundingType;
    }
}
