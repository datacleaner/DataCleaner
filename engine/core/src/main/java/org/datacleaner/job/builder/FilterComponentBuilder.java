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
import org.datacleaner.descriptors.FilterComponentDescriptor;
import org.datacleaner.job.AnalysisJobImmutabilizer;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.FilterJob;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.HasFilterOutcomes;
import org.datacleaner.job.ImmutableComponentConfiguration;
import org.datacleaner.job.ImmutableFilterJob;

/**
 * A {@link ComponentBuilder} for {@link Filter}s.
 *
 * @param <F>
 *            the type of {@link Filter} being built
 * @param <C>
 *            the {@link Filter}s category enum
 */
public final class FilterComponentBuilder<F extends Filter<C>, C extends Enum<C>> extends
        AbstractComponentBuilder<FilterComponentDescriptor<F, C>, F, FilterComponentBuilder<F, C>> implements HasFilterOutcomes {

    // We keep a cached version of the resulting filter job because of
    // references coming from other objects, particular LazyFilterOutcome.
    private FilterJob _cachedJob;
    private EnumMap<C, FilterOutcome> _outcomes;

    private final List<FilterChangeListener> _localChangeListeners;

    public FilterComponentBuilder(AnalysisJobBuilder analysisJobBuilder, FilterComponentDescriptor<F, C> descriptor) {
        super(analysisJobBuilder, descriptor, FilterComponentBuilder.class);
        _outcomes = new EnumMap<C, FilterOutcome>(descriptor.getOutcomeCategoryEnum());
        EnumSet<C> categories = descriptor.getOutcomeCategories();
        for (C category : categories) {
            _outcomes.put(category, new LazyFilterOutcome(this, category));
        }

        _localChangeListeners = new ArrayList<FilterChangeListener>(0);
    }

    public FilterJob toFilterJob() {
        return toFilterJob(true);
    }

    public FilterJob toFilterJob(AnalysisJobImmutabilizer immutabilizer) {
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

        if (_cachedJob == null) {
            _cachedJob = new ImmutableFilterJob(getName(), getDescriptor(), new ImmutableComponentConfiguration(
                    getConfiguredProperties()), componentRequirement, getMetadataProperties());
        } else {
            final ImmutableFilterJob newFilterJob = new ImmutableFilterJob(getName(), getDescriptor(),
                    new ImmutableComponentConfiguration(getConfiguredProperties()), componentRequirement,
                    getMetadataProperties());
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
        List<FilterChangeListener> globalChangeListeners = getAnalysisJobBuilder().getFilterChangeListeners();
        List<FilterChangeListener> list = new ArrayList<FilterChangeListener>(globalChangeListeners.size()
                + _localChangeListeners.size());
        list.addAll(globalChangeListeners);
        list.addAll(_localChangeListeners);
        return list;
    }

    @Override
    public String toString() {
        return "FilterJobBuilder[filter=" + getDescriptor().getDisplayName() + ",inputColumns=" + getInputColumns()
                + "]";
    }

    @Override
    public void onConfigurationChanged() {
        super.onConfigurationChanged();
        List<FilterChangeListener> listeners = getAllListeners();
        for (FilterChangeListener listener : listeners) {
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
        final Collection<FilterOutcome> outcomes = _outcomes.values();
        return outcomes;
    }

    /**
     * @deprecated use {@link #getFilterOutcome(Enum)} instead
     */
    @Deprecated
    public FilterOutcome getOutcome(C category) {
        return getFilterOutcome(category);
    }

    public FilterOutcome getFilterOutcome(C category) {
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
    public FilterOutcome getOutcome(Object category) {
        return getFilterOutcome(category);
    }

    public FilterOutcome getFilterOutcome(Object category) {
        final FilterOutcome outcome = _outcomes.get(category);
        if (outcome == null) {
            throw new IllegalArgumentException(category + " is not a valid category for " + this);
        }
        return outcome;
    }

    /**
     * Notification method invoked when transformer is removed.
     */
    protected void onRemoved() {
        List<FilterChangeListener> listeners = getAllListeners();
        for (FilterChangeListener listener : listeners) {
            listener.onRemove(this);
        }
    }

    /**
     * Adds a change listener to this component
     * 
     * @param listener
     */
    public void addChangeListener(FilterChangeListener listener) {
        _localChangeListeners.add(listener);
    }

    /**
     * Removes a change listener from this component
     * 
     * @param listener
     * @return whether or not the listener was found and removed.
     */
    public boolean removeChangeListener(FilterChangeListener listener) {
        return _localChangeListeners.remove(listener);
    }
}
