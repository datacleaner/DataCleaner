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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.ColumnProperty;
import org.datacleaner.api.InputColumn;
import org.datacleaner.descriptors.AnalyzerComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.AnalysisJobImmutabilizer;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.ComponentConfigurationException;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.ImmutableAnalyzerJob;
import org.datacleaner.job.ImmutableComponentConfiguration;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.ReflectionUtils;
import org.apache.metamodel.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ComponentBuilder} for {@link Analyzer}s.
 * 
 * @param <A>
 *            the type of {@link Analyzer} being built.
 */
public final class AnalyzerComponentBuilder<A extends Analyzer<?>> extends
        AbstractComponentBuilder<AnalyzerComponentDescriptor<A>, A, AnalyzerComponentBuilder<A>> {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisJobBuilder.class);

    /**
     * Field that determines if this analyzer is applicable for building
     * multiple jobs where the input columns have been partitioned based on
     * input size (single or multiple) and originating table
     */
    private final boolean _multipleJobsSupported;
    private final List<InputColumn<?>> _inputColumns;
    private final ConfiguredPropertyDescriptor _inputProperty;
    private final List<AnalyzerChangeListener> _localChangeListeners;

    public AnalyzerComponentBuilder(AnalysisJobBuilder analysisJobBuilder, AnalyzerComponentDescriptor<A> descriptor) {
        super(analysisJobBuilder, descriptor, AnalyzerComponentBuilder.class);

        Set<ConfiguredPropertyDescriptor> inputProperties = descriptor.getConfiguredPropertiesForInput(false);
        if (inputProperties.size() == 1) {
            _inputProperty = inputProperties.iterator().next();
            final ColumnProperty columnProperty = _inputProperty.getAnnotation(ColumnProperty.class);
            _multipleJobsSupported = columnProperty != null && columnProperty.escalateToMultipleJobs();
            _inputColumns = new ArrayList<InputColumn<?>>();
        } else {
            _multipleJobsSupported = false;
            _inputColumns = null;
            _inputProperty = null;
        }

        _localChangeListeners = new ArrayList<AnalyzerChangeListener>(0);
    }

    /**
     * Builds a temporary list of all listeners, both global and local
     * 
     * @return
     */
    private List<AnalyzerChangeListener> getAllListeners() {
        List<AnalyzerChangeListener> globalChangeListeners = getAnalysisJobBuilder().getAnalyzerChangeListeners();
        List<AnalyzerChangeListener> list = new ArrayList<AnalyzerChangeListener>(globalChangeListeners.size()
                + _localChangeListeners.size());
        list.addAll(globalChangeListeners);
        list.addAll(_localChangeListeners);
        return list;
    }

    public boolean isMultipleJobsDeterminedBy(ConfiguredPropertyDescriptor propertyDescriptor) {
        return _multipleJobsSupported && propertyDescriptor.isInputColumn() && propertyDescriptor.isRequired();
    }

    public AnalyzerJob toAnalyzerJob() throws IllegalStateException {
        return toAnalyzerJob(true);
    }

    public AnalyzerJob toAnalyzerJob(boolean validate) throws IllegalStateException {
        AnalyzerJob[] analyzerJobs = toAnalyzerJobs();

        if (analyzerJobs == null || analyzerJobs.length == 0) {
            return null;
        }

        if (validate && analyzerJobs.length > 1) {
            throw new IllegalStateException("This builder generates " + analyzerJobs.length
                    + " jobs, but a single job was requested");
        }

        return analyzerJobs[0];
    }

    public AnalyzerJob[] toAnalyzerJobs() throws IllegalStateException {
        return toAnalyzerJobs(true);
    }

    public AnalyzerJob[] toAnalyzerJobs(AnalysisJobImmutabilizer immutabilizer) throws IllegalStateException {
        return toAnalyzerJobs(true, immutabilizer);
    }

    public AnalyzerJob[] toAnalyzerJobs(boolean validate) throws IllegalStateException {
        return toAnalyzerJobs(validate, new AnalysisJobImmutabilizer());
    }

    public AnalyzerJob[] toAnalyzerJobs(boolean validate, AnalysisJobImmutabilizer immutabilizer)
            throws IllegalStateException {
        final Map<ConfiguredPropertyDescriptor, Object> configuredProperties = getConfiguredProperties();

        final ComponentRequirement componentRequirement = immutabilizer.load(getComponentRequirement());

        if (!_multipleJobsSupported) {
            ImmutableAnalyzerJob job = new ImmutableAnalyzerJob(getName(), getDescriptor(),
                    new ImmutableComponentConfiguration(configuredProperties), componentRequirement, getMetadataProperties());
            return new AnalyzerJob[] { job };
        }

        if (validate && _inputColumns.isEmpty()) {
            throw new IllegalStateException("No input column configured");
        }

        final List<InputColumn<?>> tableLessColumns = new ArrayList<InputColumn<?>>();
        final Map<Table, List<InputColumn<?>>> originatingTables = new LinkedHashMap<Table, List<InputColumn<?>>>();
        for (InputColumn<?> inputColumn : _inputColumns) {
            Table table = getAnalysisJobBuilder().getOriginatingTable(inputColumn);
            if (table == null) {
                // some columns (such as those based on an expression) don't
                // originate from a table. They should be applied to all jobs.
                tableLessColumns.add(inputColumn);
            } else {
                List<InputColumn<?>> list = originatingTables.get(table);
                if (list == null) {
                    list = new ArrayList<InputColumn<?>>();
                }
                list.add(inputColumn);
                originatingTables.put(table, list);
            }
        }

        if (validate && originatingTables.isEmpty()) {
            final List<Table> sourceTables = getAnalysisJobBuilder().getSourceTables();
            if (sourceTables.size() == 1) {
                logger.info("Only a single source table is available, so the source of analyzer '{}' is inferred", this);
                Table table = sourceTables.get(0);
                originatingTables.put(table, new ArrayList<InputColumn<?>>());
            } else {
                throw new IllegalStateException("Could not determine source for analyzer '" + this + "'");
            }
        }

        if (originatingTables.size() == 1 && _inputProperty.isArray()) {
            // there's only a single table involved - leave the input columns
            // untouched
            ImmutableAnalyzerJob job = new ImmutableAnalyzerJob(getName(), getDescriptor(),
                    new ImmutableComponentConfiguration(configuredProperties), componentRequirement, getMetadataProperties());
            return new AnalyzerJob[] { job };
        }

        for (Entry<Table, List<InputColumn<?>>> entry : originatingTables.entrySet()) {
            entry.getValue().addAll(tableLessColumns);
        }

        List<AnalyzerJob> jobs = new ArrayList<AnalyzerJob>();
        Set<Entry<Table, List<InputColumn<?>>>> entrySet = originatingTables.entrySet();
        for (Iterator<Entry<Table, List<InputColumn<?>>>> iterator = entrySet.iterator(); iterator.hasNext();) {
            Entry<Table, List<InputColumn<?>>> entry = (Entry<Table, List<InputColumn<?>>>) iterator.next();
            List<InputColumn<?>> columns = entry.getValue();
            if (_inputProperty.isArray()) {
                jobs.add(createPartitionedJob(columns.toArray(new InputColumn[columns.size()]), configuredProperties));
            } else {
                for (InputColumn<?> column : columns) {
                    jobs.add(createPartitionedJob(column, configuredProperties));
                }
            }
        }
        if (validate && !isConfigured()) {
            throw new IllegalStateException("Row processing Analyzer job is not correctly configured");
        }

        return jobs.toArray(new AnalyzerJob[jobs.size()]);
    }

    @Override
    public AnalyzerComponentBuilder<A> addInputColumn(InputColumn<?> inputColumn,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        assert propertyDescriptor.isInputColumn();
        if (inputColumn == null) {
            throw new IllegalArgumentException("InputColumn cannot be null");
        }
        if (isMultipleJobsDeterminedBy(propertyDescriptor)) {
            _inputColumns.add(inputColumn);
            return this;
        } else {
            return super.addInputColumn(inputColumn, propertyDescriptor);
        }
    }

    @Override
    public boolean isConfigured(ConfiguredPropertyDescriptor configuredProperty, boolean throwException) {
        if (_multipleJobsSupported && configuredProperty == _inputProperty) {
            if (_inputColumns.isEmpty()) {
                Object propertyValue = super.getConfiguredProperty(configuredProperty);
                if (propertyValue != null) {
                    if (propertyValue.getClass().isArray() && Array.getLength(propertyValue) > 0) {
                        setConfiguredProperty(configuredProperty, propertyValue);
                        return isConfigured(configuredProperty, throwException);
                    }
                }
                if (throwException) {
                    throw new ComponentConfigurationException("No input columns configured for "
                            + LabelUtils.getLabel(this));
                } else {
                    return false;
                }
            }
            return true;
        }
        return super.isConfigured(configuredProperty, throwException);
    }

    private AnalyzerJob createPartitionedJob(Object columnValue,
            Map<ConfiguredPropertyDescriptor, Object> configuredProperties) {
        Map<ConfiguredPropertyDescriptor, Object> jobProperties = new HashMap<ConfiguredPropertyDescriptor, Object>(
                configuredProperties);
        jobProperties.put(_inputProperty, columnValue);
        ComponentRequirement componentRequirement = new AnalysisJobImmutabilizer().load(getComponentRequirement());
        ImmutableAnalyzerJob job = new ImmutableAnalyzerJob(getName(), getDescriptor(), new ImmutableComponentConfiguration(
                jobProperties), componentRequirement, getMetadataProperties());
        return job;
    }

    @Override
    public String toString() {
        return "AnalyzerJobBuilder[analyzer=" + getDescriptor().getDisplayName() + ",inputColumns=" + getInputColumns()
                + "]";
    }

    @Override
    public AnalyzerComponentBuilder<A> setConfiguredProperty(ConfiguredPropertyDescriptor configuredProperty,
            Object value) {
        if (isMultipleJobsDeterminedBy(configuredProperty)) {

            // the dummy value is used just to pass something to the underlying
            // prototype bean.
            final InputColumn<?> dummyValue;

            _inputColumns.clear();
            if (ReflectionUtils.isArray(value)) {
                int length = Array.getLength(value);
                for (int i = 0; i < length; i++) {
                    InputColumn<?> inputColumn = (InputColumn<?>) Array.get(value, i);
                    _inputColumns.add(inputColumn);
                }
                if (_inputColumns.isEmpty()) {
                    dummyValue = null;
                } else {
                    dummyValue = _inputColumns.iterator().next();
                }
            } else {
                InputColumn<?> col = (InputColumn<?>) value;
                _inputColumns.add(col);
                dummyValue = col;
            }

            if (configuredProperty.isArray()) {
                final InputColumn<?>[] inputColumsArray;
                if (dummyValue == null) {
                    inputColumsArray = new InputColumn[0];
                } else {
                    inputColumsArray = new InputColumn[] { dummyValue };
                }
                return super.setConfiguredProperty(configuredProperty, inputColumsArray);
            } else {
                return super.setConfiguredProperty(configuredProperty, dummyValue);
            }

        } else {
            return super.setConfiguredProperty(configuredProperty, value);
        }
    }

    @Override
    public Object getConfiguredProperty(ConfiguredPropertyDescriptor propertyDescriptor) {
        if (isMultipleJobsDeterminedBy(propertyDescriptor)) {
            return _inputColumns.toArray(new InputColumn[_inputColumns.size()]);
        } else {
            return super.getConfiguredProperty(propertyDescriptor);
        }
    }

    @Override
    public void onConfigurationChanged() {
        super.onConfigurationChanged();
        List<AnalyzerChangeListener> listeners = getAllListeners();
        for (AnalyzerChangeListener listener : listeners) {
            listener.onConfigurationChanged(this);
        }
    }

    @Override
    public void onRequirementChanged() {
        super.onRequirementChanged();
        List<AnalyzerChangeListener> listeners = getAllListeners();
        for (AnalyzerChangeListener listener : listeners) {
            listener.onRequirementChanged(this);
        }
    }

    public boolean isMultipleJobsSupported() {
        return _multipleJobsSupported;
    }

    /**
     * Notification method invoked when transformer is removed.
     */
    protected void onRemoved() {
        List<AnalyzerChangeListener> listeners = getAllListeners();
        for (AnalyzerChangeListener listener : listeners) {
            listener.onRemove(this);
        }
    }

    /**
     * Adds a change listener to this component
     * 
     * @param listener
     */
    public void addChangeListener(AnalyzerChangeListener listener) {
        _localChangeListeners.add(listener);
    }

    /**
     * Removes a change listener from this component
     * 
     * @param listener
     * @return whether or not the listener was found and removed.
     */
    public boolean removeChangeListener(AnalyzerChangeListener listener) {
        return _localChangeListeners.remove(listener);
    }
}
