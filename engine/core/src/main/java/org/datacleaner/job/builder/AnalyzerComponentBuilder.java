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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.metamodel.schema.Table;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.ColumnProperty;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.AnalysisJobImmutabilizer;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.ComponentConfigurationException;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.ImmutableAnalyzerJob;
import org.datacleaner.job.ImmutableComponentConfiguration;
import org.datacleaner.job.OutputDataStreamJob;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ComponentBuilder} for {@link Analyzer}s.
 * 
 * @param <A>
 *            the type of {@link Analyzer} being built.
 */
public final class AnalyzerComponentBuilder<A extends Analyzer<?>> extends
        AbstractComponentBuilder<AnalyzerDescriptor<A>, A, AnalyzerComponentBuilder<A>> {

    public static final String METADATA_PROPERTY_BUILDER_ID = "org.datacleaner.componentbuilder.id";
    public static final String METADATA_PROPERTY_BUILDER_PARTITION_INDEX =
            "org.datacleaner.componentbuilder.partition.index";

    private static final Logger logger = LoggerFactory.getLogger(AnalysisJobBuilder.class);

    /**
     * Field that determines if this analyzer is applicable for building
     * multiple jobs where the input columns have been partitioned based on
     * input size (single or multiple) and originating table
     */
    private final boolean _multipleJobsSupported;
    private final List<InputColumn<?>> _escalatingInputColumns;
    private final ConfiguredPropertyDescriptor _escalatingInputProperty;
    private final List<AnalyzerChangeListener> _localChangeListeners;

    public AnalyzerComponentBuilder(AnalysisJobBuilder analysisJobBuilder, AnalyzerDescriptor<A> descriptor) {
        super(analysisJobBuilder, descriptor, AnalyzerComponentBuilder.class);

        final Set<ConfiguredPropertyDescriptor> requiredInputProperties = descriptor.getConfiguredPropertiesForInput(
                false);
        if (requiredInputProperties.size() == 1) {
            _escalatingInputProperty = requiredInputProperties.iterator().next();
            final ColumnProperty columnProperty = _escalatingInputProperty.getAnnotation(ColumnProperty.class);
            _multipleJobsSupported = columnProperty != null && !_escalatingInputProperty.isArray() && columnProperty
                    .escalateToMultipleJobs();
            _escalatingInputColumns = new ArrayList<InputColumn<?>>();
        } else {
            _multipleJobsSupported = false;
            _escalatingInputProperty = null;
            _escalatingInputColumns = Collections.emptyList();
        }

        _localChangeListeners = new ArrayList<AnalyzerChangeListener>(0);
    }

    /**
     * Builds a temporary list of all listeners, both global and local
     * 
     * @return
     */
    private List<AnalyzerChangeListener> getAllListeners() {
        @SuppressWarnings("deprecation")
        List<AnalyzerChangeListener> globalChangeListeners = getAnalysisJobBuilder().getAnalyzerChangeListeners();
        List<AnalyzerChangeListener> list = new ArrayList<>(globalChangeListeners.size() + _localChangeListeners
                .size());
        list.addAll(globalChangeListeners);
        list.addAll(_localChangeListeners);
        return list;
    }

    public boolean isMultipleJobsDeterminedBy(ConfiguredPropertyDescriptor propertyDescriptor) {
        return _multipleJobsSupported && !propertyDescriptor.isArray() && propertyDescriptor.isInputColumn()
                && propertyDescriptor.isRequired();
    }

    public AnalyzerJob toAnalyzerJob() throws IllegalStateException {
        return toAnalyzerJob(true);
    }

    public AnalyzerJob toAnalyzerJob(boolean validate) throws IllegalStateException {
        AnalyzerJob[] analyzerJobs = toAnalyzerJobs(validate);

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

        final List<InputColumn<?>> inputColumns;
        if (_escalatingInputProperty != null && !_escalatingInputColumns.isEmpty()) {
            inputColumns = _escalatingInputColumns;
        } else {
            inputColumns = getInputColumns();
        }
        if (validate && inputColumns.isEmpty()) {
            throw new IllegalStateException("No input column(s) configured");
        }

        final List<InputColumn<?>> tableLessColumns = new ArrayList<InputColumn<?>>();
        final Map<Table, List<InputColumn<?>>> originatingTables = new LinkedHashMap<Table, List<InputColumn<?>>>();
        for (InputColumn<?> inputColumn : inputColumns) {
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
                logger.info("Only a single source table is available, so the source of analyzer '{}' is inferred",
                        this);
                Table table = sourceTables.get(0);
                originatingTables.put(table, new ArrayList<InputColumn<?>>());
            } else {
                throw new IllegalStateException("Could not determine source for analyzer '" + this + "'");
            }
        }

        if (!isMultipleJobsSupported() && originatingTables.size() == 1) {
            // there's only a single table involved - leave the input columns
            // untouched and keep the output data stream
            final OutputDataStreamJob[] outputDataStreamJobs = immutabilizer.load(getOutputDataStreamJobs(), validate);
            ImmutableAnalyzerJob job = new ImmutableAnalyzerJob(getName(), getDescriptor(),
                    new ImmutableComponentConfiguration(configuredProperties), componentRequirement,
                    getMetadataProperties(), outputDataStreamJobs);
            return new AnalyzerJob[] { job };
        }

        for (Entry<Table, List<InputColumn<?>>> entry : originatingTables.entrySet()) {
            entry.getValue().addAll(tableLessColumns);
        }

        final List<AnalyzerJob> jobs = new ArrayList<AnalyzerJob>();
        final Set<Entry<Table, List<InputColumn<?>>>> entrySet = originatingTables.entrySet();
        int partitionIndex = 0;
        for (Entry<Table, List<InputColumn<?>>> entry : entrySet) {
            final List<InputColumn<?>> columnsOfTable = entry.getValue();
            if (_escalatingInputProperty == null || _escalatingInputProperty.isArray()) {
                // escalation will happen only for multi-table input
                jobs.add(createPartitionedJob(null, columnsOfTable, configuredProperties, partitionIndex++));
            } else {
                for (InputColumn<?> escalatingColumn : columnsOfTable) {
                    // escalation happens for each column
                    jobs.add(createPartitionedJob(escalatingColumn, columnsOfTable, configuredProperties,
                            partitionIndex++));
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
            _escalatingInputColumns.add(inputColumn);
            return this;
        } else {
            return super.addInputColumn(inputColumn, propertyDescriptor);
        }
    }

    @Override
    public AnalyzerComponentBuilder<A> removeInputColumn(InputColumn<?> inputColumn,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        assert propertyDescriptor.isInputColumn();
        if (inputColumn == null) {
            throw new IllegalArgumentException("InputColumn cannot be null");
        }
        if (isMultipleJobsDeterminedBy(propertyDescriptor)) {
            _escalatingInputColumns.remove(inputColumn);
            return this;
        } else {
            return super.removeInputColumn(inputColumn, propertyDescriptor);
        }
    }

    @Override
    public boolean isConfigured(ConfiguredPropertyDescriptor configuredProperty, boolean throwException) {
        if (isMultipleJobsSupported() && configuredProperty == _escalatingInputProperty) {
            if (_escalatingInputColumns.isEmpty()) {
                Object propertyValue = super.getConfiguredProperty(configuredProperty);
                if (propertyValue != null) {
                    if (propertyValue.getClass().isArray() && Array.getLength(propertyValue) > 0) {
                        setConfiguredProperty(configuredProperty, propertyValue);
                        return isConfigured(configuredProperty, throwException);
                    }
                }
                if (throwException) {
                    throw new ComponentConfigurationException("No input columns configured for " + LabelUtils.getLabel(
                            this));
                } else {
                    return false;
                }
            }
            return true;
        }
        return super.isConfigured(configuredProperty, throwException);
    }

    private AnalyzerJob createPartitionedJob(InputColumn<?> escalatingColumnValue, Collection<InputColumn<?>> availableColumns,
            Map<ConfiguredPropertyDescriptor, Object> configuredProperties, int partitionIndex) {
        final Map<ConfiguredPropertyDescriptor, Object> jobProperties = new HashMap<>(
                configuredProperties);
        for (Entry<ConfiguredPropertyDescriptor, Object> jobProperty : jobProperties.entrySet()) {
            final ConfiguredPropertyDescriptor propertyDescriptor = jobProperty.getKey();
            if (propertyDescriptor.isInputColumn()) {
                final Object unpartitionedValue;
                if (escalatingColumnValue != null && _escalatingInputProperty == propertyDescriptor) {
                    unpartitionedValue = escalatingColumnValue;
                } else {
                    unpartitionedValue = jobProperty.getValue();
                }
                final Object partitionedValue = partitionValue(propertyDescriptor, unpartitionedValue, availableColumns);
                jobProperty.setValue(partitionedValue);
            }
        }

        // set the component builder ID property to allow correlating partion
        // jobs back to their builder
        final Map<String, String> metadataProperties = new LinkedHashMap<>(getMetadataProperties());
        metadataProperties.put(METADATA_PROPERTY_BUILDER_ID, "" + System.identityHashCode(this));
        metadataProperties.put(METADATA_PROPERTY_BUILDER_PARTITION_INDEX, "" + partitionIndex);

        // we do not currently support this combination of multiple analyzer
        // jobs and having output data streams
        final OutputDataStreamJob[] outputDataStreamJobs = new OutputDataStreamJob[0];

        final ComponentRequirement componentRequirement = new AnalysisJobImmutabilizer().load(
                getComponentRequirement());
        final ImmutableAnalyzerJob job = new ImmutableAnalyzerJob(getName(), getDescriptor(),
                new ImmutableComponentConfiguration(jobProperties), componentRequirement, metadataProperties,
                outputDataStreamJobs);
        return job;
    }

    private Object partitionValue(ConfiguredPropertyDescriptor key, Object unpartitionedValue,
            Collection<InputColumn<?>> availableColumns) {
        if (unpartitionedValue instanceof InputColumn[]) {
            final InputColumn<?>[] array = (InputColumn<?>[]) unpartitionedValue;
            final List<InputColumn<?>> result = new ArrayList<>();
            for (InputColumn<?> inputColumn : array) {
                if (availableColumns.contains(inputColumn)) {
                    result.add(inputColumn);
                }
            }
            if (!key.isArray()) {
                if (result.isEmpty()) {
                    return null;
                }
                return result.get(0);
            }
            return result.toArray(new InputColumn<?>[result.size()]);
        }
        return unpartitionedValue;
    }

    @Override
    public String toString() {
        return "AnalyzerComponentBuilder[analyzer=" + getDescriptor().getDisplayName() + ",inputColumns="
                + getInputColumns() + "]";
    }

    @Override
    public AnalyzerComponentBuilder<A> setConfiguredProperty(ConfiguredPropertyDescriptor configuredProperty,
            Object value) {
        if (isMultipleJobsDeterminedBy(configuredProperty)) {

            // the dummy value is used just to pass something to the underlying
            // prototype bean.
            final InputColumn<?> dummyValue;

            _escalatingInputColumns.clear();
            if (value == null) {
                dummyValue = null;
            } else if (ReflectionUtils.isArray(value)) {
                int length = Array.getLength(value);
                for (int i = 0; i < length; i++) {
                    final InputColumn<?> inputColumn = (InputColumn<?>) Array.get(value, i);
                    _escalatingInputColumns.add(inputColumn);
                }
                if (_escalatingInputColumns.isEmpty()) {
                    dummyValue = null;
                } else {
                    dummyValue = _escalatingInputColumns.iterator().next();
                }
            } else {
                final InputColumn<?> col = (InputColumn<?>) value;
                _escalatingInputColumns.add(col);
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
            return _escalatingInputColumns.toArray(new InputColumn[_escalatingInputColumns.size()]);
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

    @Override
    public List<OutputDataStream> getOutputDataStreams() {
        if (isMultipleJobsSupported()) {
            return Collections.emptyList();
        }
        return super.getOutputDataStreams();
    }

    @Override
    protected Map<ConfiguredPropertyDescriptor, Object> getConfiguredPropertiesForQuestioning() {
        final Map<ConfiguredPropertyDescriptor, Object> properties = super.getConfiguredPropertiesForQuestioning();
        if (!isMultipleJobsSupported()) {
            return properties;
        }

        // create a mutable copy and replace the property values that are
        final Map<ConfiguredPropertyDescriptor, Object> map = new HashMap<>(properties);
        for (Entry<ConfiguredPropertyDescriptor, Object> entry : map.entrySet()) {
            if (isMultipleJobsDeterminedBy(entry.getKey())) {
                final Object value = entry.getValue();
                if (Array.getLength(value) > 1) {
                    // pick the first element
                    final Object element = Array.get(value, 0);
                    entry.setValue(element);
                }
            }
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Notification method invoked when transformer is removed.
     */
    @Override
    protected void onRemovedInternal() {
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
