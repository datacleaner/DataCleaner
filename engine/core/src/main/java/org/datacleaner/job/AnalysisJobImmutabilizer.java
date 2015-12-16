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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.datacleaner.api.MultiStreamComponent;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.api.Transformer;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.LazyFilterOutcome;
import org.datacleaner.job.builder.LazyOutputDataStreamJob;
import org.datacleaner.job.builder.TransformerComponentBuilder;

/**
 * Class that will load mutable and lazy components into their immutable
 * variants. For instance {@link LazyFilterOutcome}.
 * 
 * This is important for serialization and decoupling of object graphs, to
 * ensure that the lazy references (to builder objects) become fixed on
 * immutable job objects.
 */
public final class AnalysisJobImmutabilizer {

    private final Map<LazyFilterOutcome, ImmutableFilterOutcome> _outcomes;
    private final Map<ComponentBuilder, ComponentJob> _componentJobs;

    public AnalysisJobImmutabilizer() {
        _outcomes = new HashMap<>();
        _componentJobs = new IdentityHashMap<ComponentBuilder, ComponentJob>();
    }

    public OutputDataStreamJob[] load(OutputDataStreamJob[] outputDataStreamJobs, boolean validate) {
        if (outputDataStreamJobs == null || outputDataStreamJobs.length == 0) {
            return outputDataStreamJobs;
        }
        final OutputDataStreamJob[] result = new OutputDataStreamJob[outputDataStreamJobs.length];
        for (int i = 0; i < result.length; i++) {
            final OutputDataStreamJob outputDataStreamJob = outputDataStreamJobs[i];
            if (outputDataStreamJob instanceof LazyOutputDataStreamJob) {
                final OutputDataStream outputDataStream = outputDataStreamJob.getOutputDataStream();
                final AnalysisJob job = ((LazyOutputDataStreamJob) outputDataStreamJob).getJob(validate, this);
                result[i] = new ImmutableOutputDataStreamJob(outputDataStream, job);
            } else {
                result[i] = outputDataStreamJob;
            }
        }
        return result;
    }

    public FilterOutcome load(FilterOutcome outcome) {
        if (outcome instanceof LazyFilterOutcome) {
            LazyFilterOutcome lfo = (LazyFilterOutcome) outcome;
            ImmutableFilterOutcome result = _outcomes.get(lfo);
            if (result == null) {
                result = new ImmutableFilterOutcome(lfo.getFilterJob(), lfo.getCategory());
                _outcomes.put(lfo, result);
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

    /**
     * Gets or creates a {@link TransformerJob} for a particular
     * {@link TransformerComponentBuilder}. Since {@link MultiStreamComponent}s
     * are subtypes of {@link Transformer} it is necesary to have this caching
     * mechanism in place in order to allow diamond-shaped component graphs
     * where multiple streams include the same component.
     * 
     * @param validate
     * @param tjb
     * @return
     */
    public TransformerJob getOrCreateTransformerJob(boolean validate, TransformerComponentBuilder<?> tjb) {
        TransformerJob componentJob = (TransformerJob) _componentJobs.get(tjb);
        if (componentJob == null) {
            try {
                componentJob = tjb.toTransformerJob(validate, this);
                _componentJobs.put(tjb, componentJob);
            } catch (IllegalStateException e) {
                throw new IllegalStateException("Could not create transformer job from builder: " + tjb + ", ("
                        + e.getMessage() + ")", e);
            }
        }
        return componentJob;
    }
}
