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
package org.datacleaner.job.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import org.datacleaner.api.Filter;
import org.datacleaner.descriptors.FilterDescriptor;
import org.datacleaner.job.AnalysisJobImmutabilizer;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.FilterJob;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.HasFilterOutcomes;
import org.datacleaner.job.ImmutableComponentConfiguration;
import org.datacleaner.job.ImmutableFilterJob;
import org.datacleaner.job.OutputDataStreamJob;

/**
 * A {@link ComponentBuilder} for {@link Filter}s.
 *
 * @param <F>
 *            the type of {@link Filter} being built
 * @param <C>
 *            the {@link Filter}s category enum
 */
public final class FilterComponentBuilder<F extends Filter<C>, C extends Enum<C>>
        extends AbstractComponentBuilder<FilterDescriptor<F, C>, F, FilterComponentBuilder<F, C>>
        implements HasFilterOutcomes {

    private final List<FilterChangeListener> _localChangeListeners;
    // We keep a cached version of the resulting filter job because of
    // references coming from other objects, particular LazyFilterOutcome.
    private FilterJob _cachedJob;
    private EnumMap<C, FilterOutcome> _outcomes;

    public FilterComponentBuilder(final AnalysisJobBuilder analysisJobBuilder,
            final FilterDescriptor<F, C> descriptor) {
        super(analysisJobBuilder, descriptor, FilterComponentBuilder.class);
        _outcomes = new EnumMap<>(descriptor.getOutcomeCategoryEnum());
        final EnumSet<C> categories = descriptor.getOutcomeCategories();
        for (final C category : categories) {
            _outcomes.put(category, new LazyFilterOutcome(this, category));
        }

        _localChangeListeners = new ArrayList<>(0);
    }

    public FilterJob toFilterJob() {
        return toFilterJob(true);
    }

    public FilterJob toFilterJob(final AnalysisJobImmutabilizer immutabilizer) {
        return toFilterJob(true, immutabilizer);
    }

    public FilterJob toFilterJob(final boolean validate) {
        return toFilterJob(validate, new AnalysisJobImmutabilizer());
    }

    public FilterJob toFilterJob(final boolean validate, final AnalysisJobImmutabilizer immutabilizer) {
        if (validate && !isConfigured(true)) {
            throw new IllegalStateException("Filter job is not correctly configured");
        }

        final ComponentRequirement componentRequirement = immutabilizer.load(getComponentRequirement());
        final OutputDataStreamJob[] outputDataStreamJobs = immutabilizer.load(getOutputDataStreamJobs(), validate);

        if (_cachedJob == null) {
            _cachedJob = new ImmutableFilterJob(getName(), getDescriptor(),
                    new ImmutableComponentConfiguration(getConfiguredProperties()), componentRequirement,
                    getMetadataProperties(), outputDataStreamJobs);
        } else {
            final ImmutableFilterJob newFilterJob = new ImmutableFilterJob(getName(), getDescriptor(),
                    new ImmutableComponentConfiguration(getConfiguredProperties()), componentRequirement,
                    getMetadataProperties(), outputDataStreamJobs);
            if (!newFilterJob.equals(_cachedJob)) {
                _cachedJob = newFilterJob;
            }
        }
        return _cachedJob;
    }

    /**
     * Builds a temporary list of all listeners, both global and local
     *
     * @return
     */
    private List<FilterChangeListener> getAllListeners() {
        @SuppressWarnings("deprecation") final List<FilterChangeListener> globalChangeListeners =
                getAnalysisJobBuilder().getFilterChangeListeners();
        final List<FilterChangeListener> list =
                new ArrayList<>(globalChangeListeners.size() + _localChangeListeners.size());
        list.addAll(globalChangeListeners);
        list.addAll(_localChangeListeners);
        return list;
    }

    @Override
    public String toString() {
        return "FilterComponentBuilder[filter=" + getDescriptor().getDisplayName() + ",inputColumns="
                + getInputColumns() + "]";
    }

    @Override
    public void onConfigurationChanged() {
        super.onConfigurationChanged();
        final List<FilterChangeListener> listeners = getAllListeners();
        for (final FilterChangeListener listener : listeners) {
            listener.onConfigurationChanged(this);
        }
    }

    @Override
    public void onRequirementChanged() {
        super.onRequirementChanged();
        final List<FilterChangeListener> listeners = getAllListeners();
        for (final FilterChangeListener listener : listeners) {
            listener.onRequirementChanged(this);
        }
    }

    @Override
    public Collection<FilterOutcome> getFilterOutcomes() {
        return _outcomes.values();
    }

    /**
     * @deprecated use {@link #getFilterOutcome(Enum)} instead
     */
    @Deprecated
    public FilterOutcome getOutcome(final C category) {
        return getFilterOutcome(category);
    }

    public FilterOutcome getFilterOutcome(final C category) {
        final FilterOutcome outcome = _outcomes.get(category);
        if (outcome == null) {
            throw new IllegalArgumentException(category + " is not a valid category for " + this);
        }
        return outcome;
    }

    /**
     * @deprecated use {@link #getFilterOutcome(Object)} instead
     */
    @Deprecated
    public FilterOutcome getOutcome(final Object category) {
        return getFilterOutcome(category);
    }

    public FilterOutcome getFilterOutcome(Object category) {
        if (category instanceof String) {
            final EnumSet<?> categories = getDescriptor().getOutcomeCategories();
            for (final Enum<?> c : categories) {
                if (c.name().equals(category)) {
                    category = c;
                    break;
                }
            }
        }
        final FilterOutcome outcome = _outcomes.get(category);
        if (outcome == null) {
            throw new IllegalArgumentException(category + " is not a valid category for " + this);
        }
        return outcome;
    }

    @Override
    protected void onRemovedInternal() {
        final List<FilterChangeListener> listeners = getAllListeners();
        for (final FilterChangeListener listener : listeners) {
            listener.onRemove(this);
        }
    }

    /**
     * Adds a change listener to this component
     *
     * @param listener
     */
    public void addChangeListener(final FilterChangeListener listener) {
        _localChangeListeners.add(listener);
    }

    /**
     * Removes a change listener from this component
     *
     * @param listener
     * @return whether or not the listener was found and removed.
     */
    public boolean removeChangeListener(final FilterChangeListener listener) {
        return _localChangeListeners.remove(listener);
    }
}
