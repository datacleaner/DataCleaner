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
package org.eobjects.analyzer.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eobjects.analyzer.job.builder.LazyFilterOutcome;

/**
 * Class that will load mutable and lazy components into their immutable
 * variants. For instance {@link LazyFilterOutcome}.
 * 
 * This is important for serialization and decoupling of object graphs, to
 * ensure that the lazy references (to builder objects) become fixed on
 * immutable job objects.
 */
public final class AnalysisJobImmutabilizer {

    private final HashMap<LazyFilterOutcome, ImmutableFilterOutcome> _referenceMap;

    public AnalysisJobImmutabilizer() {
        _referenceMap = new HashMap<>();
        // prevent instantiation
    }

    public FilterOutcome load(FilterOutcome outcome) {
        if (outcome instanceof LazyFilterOutcome) {
            LazyFilterOutcome lfo = (LazyFilterOutcome) outcome;
            ImmutableFilterOutcome result = _referenceMap.get(lfo);
            if (result == null) {
                result = new ImmutableFilterOutcome(lfo.getFilterJob(), lfo.getCategory());
                _referenceMap.put(lfo, result);
            }
            return result;
        }
        return outcome;
    }

    public ComponentRequirement load(ComponentRequirement req) {
        if (req instanceof SimpleComponentRequirement) {
            final FilterOutcome originalOutcome = ((SimpleComponentRequirement) req).getOutcome();
            final FilterOutcome loadedOutcome = load(originalOutcome);
            if (loadedOutcome != originalOutcome) {
                return new SimpleComponentRequirement(loadedOutcome);
            }
        } else if (req instanceof CompoundComponentRequirement) {
            boolean changed = false;
            final Set<FilterOutcome> originalOutcomes = ((CompoundComponentRequirement) req).getOutcomes();
            final List<FilterOutcome> loadedOutcomes = new ArrayList<>(originalOutcomes.size());
            for (final FilterOutcome originalOutcome : originalOutcomes) {
                final FilterOutcome loadedOutcome = load(originalOutcome);
                if (loadedOutcome != originalOutcome) {
                    changed = true;
                }
                loadedOutcomes.add(loadedOutcome);
            }
            if (changed) {
                return new CompoundComponentRequirement(loadedOutcomes);
            }
        }
        return req;
    }
}
