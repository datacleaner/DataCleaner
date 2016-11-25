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

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.CollectionUtils;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Filter;
import org.datacleaner.api.HasAnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.api.Transformer;
import org.datacleaner.api.Validate;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.SchemaNavigator;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.FilterDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalysisJobImmutabilizer;
import org.datacleaner.job.AnalysisJobMetadata;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.ComponentValidationException;
import org.datacleaner.job.FilterJob;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.IdGenerator;
import org.datacleaner.job.ImmutableAnalysisJob;
import org.datacleaner.job.ImmutableAnalysisJobMetadata;
import org.datacleaner.job.InputColumnSourceJob;
import org.datacleaner.job.PrefixedIdGenerator;
import org.datacleaner.job.SimpleComponentRequirement;
import org.datacleaner.job.TransformerJob;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.SourceColumnFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry to the Job Builder API. Use this class to build jobs either
 * programmatically, while parsing a marshalled job-representation (such as an
 * XML job definition) or for making an end-user able to build a job in a UI.
 *
 * The AnalysisJobBuilder supports a wide variety of listeners to make it
 * possible to be informed of changes to the state and dependencies between the
 * components/beans that defines the job.
 */
public final class AnalysisJobBuilder implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisJobBuilder.class);

    private final DataCleanerConfiguration _configuration;
    private final AnalysisJobBuilder _parentBuilder;
    private final IdGenerator _transformedColumnIdGenerator;
    private final List<MetaModelInputColumn> _sourceColumns;
    private final List<FilterComponentBuilder<?, ?>> _filterComponentBuilders;
    private final List<TransformerComponentBuilder<?>> _transformerComponentBuilders;
    private final List<AnalyzerComponentBuilder<?>> _analyzerComponentBuilders;
    // listeners, typically for UI that uses the builders
    private final List<SourceColumnChangeListener> _sourceColumnListeners = new ArrayList<>();
    private final List<AnalyzerChangeListener> _analyzerChangeListeners = new ArrayList<>();
    private final List<TransformerChangeListener> _transformerChangeListeners = new ArrayList<>();
    private final List<FilterChangeListener> _filterChangeListeners = new ArrayList<>();
    private final List<AnalysisJobChangeListener> _analysisJobChangeListeners = new ArrayList<>();
    // the configurable components
    private Datastore _datastore;
    private DatastoreConnection _datastoreConnection;
    private MutableAnalysisJobMetadata _analysisJobMetadata;
    private ComponentRequirement _defaultRequirement;

    public AnalysisJobBuilder(final DataCleanerConfiguration configuration) {
        this(configuration, (AnalysisJobBuilder) null);
    }

    public AnalysisJobBuilder(final DataCleanerConfiguration configuration, final AnalysisJobBuilder parentBuilder) {
        _configuration = configuration;
        _parentBuilder = parentBuilder;
        _transformedColumnIdGenerator = new PrefixedIdGenerator("");
        _sourceColumns = new ArrayList<>();
        _filterComponentBuilders = new ArrayList<>();
        _transformerComponentBuilders = new ArrayList<>();
        _analyzerComponentBuilders = new ArrayList<>();
    }

    /**
     * Private constructor for {@link #withoutListeners()} method
     */
    private AnalysisJobBuilder(final DataCleanerConfiguration configuration, final Datastore datastore,
            final DatastoreConnection datastoreConnection, final MutableAnalysisJobMetadata metadata,
            final List<MetaModelInputColumn> sourceColumns, final ComponentRequirement defaultRequirement,
            final IdGenerator idGenerator, final List<TransformerComponentBuilder<?>> transformerJobBuilders,
            final List<FilterComponentBuilder<?, ?>> filterJobBuilders,
            final List<AnalyzerComponentBuilder<?>> analyzerJobBuilders, final AnalysisJobBuilder parentBuilder) {
        _configuration = configuration;
        _datastore = datastore;
        _analysisJobMetadata = metadata;
        _datastoreConnection = datastoreConnection;
        _sourceColumns = sourceColumns;
        _defaultRequirement = defaultRequirement;
        _transformedColumnIdGenerator = idGenerator;
        _filterComponentBuilders = filterJobBuilders;
        _transformerComponentBuilders = transformerJobBuilders;
        _analyzerComponentBuilders = analyzerJobBuilders;
        _parentBuilder = parentBuilder;
    }

    public AnalysisJobBuilder(final DataCleanerConfiguration configuration, final AnalysisJob job) {
        this(configuration, job, null);
    }

    public AnalysisJobBuilder(final DataCleanerConfiguration configuration, final AnalysisJob job,
            final AnalysisJobBuilder parentBuilder) {
        this(configuration, parentBuilder);
        importJob(job);
    }

    public AnalysisJobBuilder setDatastore(final String datastoreName) {
        final Datastore datastore = _configuration.getDatastoreCatalog().getDatastore(datastoreName);
        if (datastore == null) {
            throw new IllegalArgumentException("No such datastore: " + datastoreName);
        }
        return setDatastore(datastore);
    }

    public Datastore getDatastore() {
        return _datastore;
    }

    public AnalysisJobBuilder setDatastore(final Datastore datastore) {
        _datastore = datastore;
        final DatastoreConnection datastoreConnection;
        if (datastore == null) {
            datastoreConnection = null;
        } else {
            datastoreConnection = datastore.openConnection();
        }
        return setDatastoreConnection(datastoreConnection);
    }

    public MutableAnalysisJobMetadata getAnalysisJobMetadata() {
        if (_analysisJobMetadata == null) {
            _analysisJobMetadata = new MutableAnalysisJobMetadata();
        }
        return _analysisJobMetadata;
    }

    public AnalysisJobBuilder setAnalysisJobMetadata(AnalysisJobMetadata analysisJobMetadata) {
        if (analysisJobMetadata == null) {
            analysisJobMetadata = AnalysisJobMetadata.EMPTY_METADATA;
        }
        if (analysisJobMetadata instanceof MutableAnalysisJobMetadata) {
            _analysisJobMetadata = (MutableAnalysisJobMetadata) analysisJobMetadata;
        } else {
            _analysisJobMetadata = new MutableAnalysisJobMetadata(analysisJobMetadata);
        }
        return this;
    }

    public DatastoreConnection getDatastoreConnection() {
        return _datastoreConnection;
    }

    public AnalysisJobBuilder setDatastoreConnection(final DatastoreConnection datastoreConnection) {
        if (_datastoreConnection != null) {
            _datastoreConnection.close();
        }
        _datastoreConnection = datastoreConnection;

        if (datastoreConnection != null && _datastore == null) {
            final Datastore datastore = datastoreConnection.getDatastore();
            if (datastore != null) {
                setDatastore(datastore);
            }
        }

        return this;
    }

    public DataCleanerConfiguration getConfiguration() {
        return _configuration;
    }

    public AnalysisJobBuilder addSourceColumn(final Column column) {
        final MetaModelInputColumn inputColumn = new MetaModelInputColumn(column);
        return addSourceColumn(inputColumn);
    }

    public AnalysisJobBuilder addSourceColumn(final MetaModelInputColumn inputColumn) {
        if (!_sourceColumns.contains(inputColumn)) {
            _sourceColumns.add(inputColumn);

            final List<SourceColumnChangeListener> listeners = new ArrayList<>(_sourceColumnListeners);
            for (final SourceColumnChangeListener listener : listeners) {
                listener.onAdd(inputColumn);
            }
        }
        return this;
    }

    public AnalysisJobBuilder addSourceColumns(final Collection<Column> columns) {
        for (final Column column : columns) {
            addSourceColumn(column);
        }
        return this;
    }

    public AnalysisJobBuilder addSourceColumns(final Column... columns) {
        for (final Column column : columns) {
            addSourceColumn(column);
        }
        return this;
    }

    public AnalysisJobBuilder addSourceColumns(final MetaModelInputColumn... inputColumns) {
        for (final MetaModelInputColumn metaModelInputColumn : inputColumns) {
            addSourceColumn(metaModelInputColumn);
        }
        return this;
    }

    public AnalysisJobBuilder addSourceColumns(final String... columnNames) {
        if (_datastoreConnection == null) {
            throw new IllegalStateException(
                    "Cannot add source columns by name when no Datastore or DatastoreConnection has been set");
        }
        final SchemaNavigator schemaNavigator = _datastoreConnection.getSchemaNavigator();
        final Column[] columns = new Column[columnNames.length];
        for (int i = 0; i < columns.length; i++) {
            final String columnName = columnNames[i];
            final Column column = schemaNavigator.convertToColumn(columnName);
            if (column == null) {
                throw new IllegalArgumentException("No such column: " + columnName);
            }
            columns[i] = column;
        }
        return addSourceColumns(columns);
    }

    /**
     * Removes the specified table (or rather - all columns of that table) from
     * this job's source.
     *
     * @param table
     */
    public AnalysisJobBuilder removeSourceTable(final Table table) {
        final Column[] cols = table.getColumns();
        for (final Column col : cols) {
            removeSourceColumn(col);
        }
        return this;
    }

    public AnalysisJobBuilder removeSourceColumn(final Column column) {
        final MetaModelInputColumn inputColumn = new MetaModelInputColumn(column);
        return removeSourceColumn(inputColumn);
    }

    /**
     * Imports the datastore, components and configuration of a
     * {@link AnalysisJob} into this builder.
     *
     * @param job
     */
    public void importJob(final AnalysisJob job) {
        final AnalysisJobBuilderImportHelper helper = new AnalysisJobBuilderImportHelper(this);
        helper.importJob(job);
    }

    public AnalysisJobBuilder removeSourceColumn(final MetaModelInputColumn inputColumn) {
        final int index = _sourceColumns.indexOf(inputColumn);
        if (index != -1) {
            final MetaModelInputColumn removedColumn = _sourceColumns.remove(index);
            // remove any references in components
            final Collection<ComponentBuilder> componentBuilders = getComponentBuilders();
            for (final ComponentBuilder componentBuilder : componentBuilders) {
                final Set<ConfiguredPropertyDescriptor> configuredProperties =
                        componentBuilder.getDescriptor().getConfiguredPropertiesByType(InputColumn.class, true);
                for (final ConfiguredPropertyDescriptor configuredPropertyDescriptor : configuredProperties) {
                    componentBuilder.removeInputColumn(removedColumn, configuredPropertyDescriptor);
                }
            }

            // notify listeners
            final List<SourceColumnChangeListener> listeners = new ArrayList<>(_sourceColumnListeners);
            for (final SourceColumnChangeListener listener : listeners) {
                listener.onRemove(removedColumn);
            }
        }
        return this;
    }

    public boolean containsSourceTable(final Table table) {
        return getSourceTables().contains(table);
    }

    public boolean containsSourceColumn(final Column column) {
        for (final MetaModelInputColumn sourceColumn : _sourceColumns) {
            if (sourceColumn.getPhysicalColumn().equals(column)) {
                return true;
            }
        }
        return false;
    }

    public List<MetaModelInputColumn> getSourceColumns() {
        return Collections.unmodifiableList(_sourceColumns);
    }

    public List<MetaModelInputColumn> getSourceColumnsOfTable(final Table table) {
        if (table == null) {
            return Collections.emptyList();
        }
        final List<MetaModelInputColumn> result = new ArrayList<>();
        for (final MetaModelInputColumn column : _sourceColumns) {
            final Column physicalColumn = column.getPhysicalColumn();
            if (physicalColumn != null && table.equals(physicalColumn.getTable())) {
                result.add(column);
            }
        }
        return result;
    }

    public <T extends Transformer> TransformerComponentBuilder<T> addTransformer(final Class<T> transformerClass) {
        final TransformerDescriptor<T> descriptor = _configuration.getEnvironment().getDescriptorProvider()
                .getTransformerDescriptorForClass(transformerClass);
        if (descriptor == null) {
            throw new IllegalArgumentException("No descriptor found for: " + transformerClass);
        }
        return addTransformer(descriptor);
    }

    public List<TransformerComponentBuilder<?>> getTransformerComponentBuilders() {
        return Collections.unmodifiableList(_transformerComponentBuilders);
    }

    public <T extends Transformer> TransformerComponentBuilder<T> addTransformer(
            final TransformerDescriptor<T> descriptor) {
        return addTransformer(descriptor, null, null, null);
    }

    public <T extends Transformer> TransformerComponentBuilder<T> addTransformer(
            final TransformerDescriptor<T> descriptor,
            final Map<ConfiguredPropertyDescriptor, Object> configuredProperties,
            final ComponentRequirement requirement, final Map<String, String> metadataProperties) {
        final TransformerComponentBuilder<T> transformer =
                new TransformerComponentBuilder<>(this, descriptor, _transformedColumnIdGenerator);
        initializeComponentBuilder(transformer, configuredProperties, requirement, metadataProperties);
        return addTransformer(transformer);
    }

    public <T extends Transformer> TransformerComponentBuilder<T> addTransformer(
            final TransformerComponentBuilder<T> tjb) {
        if (tjb.getComponentRequirement() == null) {
            tjb.setComponentRequirement(_defaultRequirement);
        }
        _transformerComponentBuilders.add(tjb);

        // Before triggering component's listeners, so listeners are ready.
        onComponentAdded();

        // make a copy since some of the listeners may add additional listeners
        // which will otherwise cause ConcurrentModificationExceptions
        final List<TransformerChangeListener> listeners = new ArrayList<>(_transformerChangeListeners);
        for (final TransformerChangeListener listener : listeners) {
            listener.onAdd(tjb);
        }
        return tjb;
    }

    public AnalysisJobBuilder removeTransformer(final TransformerComponentBuilder<?> tjb) {
        final boolean removed = _transformerComponentBuilders.remove(tjb);
        if (removed) {
            tjb.onRemoved();

            // Ajb removal last, so listeners gets triggered
            onComponentRemoved();
        }
        return this;
    }

    public ComponentBuilder addComponent(final ComponentDescriptor<?> descriptor,
            final Map<ConfiguredPropertyDescriptor, Object> configuredProperties,
            final ComponentRequirement requirement, final Map<String, String> metadataProperties) {
        final ComponentBuilder builder;
        if (descriptor instanceof FilterDescriptor) {
            builder = addFilter((FilterDescriptor<?, ?>) descriptor, configuredProperties, requirement,
                    metadataProperties);
        } else if (descriptor instanceof TransformerDescriptor) {
            builder = addTransformer((TransformerDescriptor<?>) descriptor, configuredProperties, requirement,
                    metadataProperties);
        } else if (descriptor instanceof AnalyzerDescriptor) {
            builder = addAnalyzer((AnalyzerDescriptor<?>) descriptor, configuredProperties, requirement,
                    metadataProperties);
        } else {
            throw new UnsupportedOperationException("Unknown component type: " + descriptor);
        }

        return builder;
    }

    public ComponentBuilder addComponent(final ComponentDescriptor<?> descriptor) {
        return addComponent(descriptor, null, null, null);
    }

    public ComponentBuilder addComponent(final ComponentBuilder builder) {
        if (builder instanceof FilterComponentBuilder) {
            addFilter((FilterComponentBuilder<?, ?>) builder);
        } else if (builder instanceof TransformerComponentBuilder) {
            addTransformer((TransformerComponentBuilder<?>) builder);
        } else if (builder instanceof AnalyzerComponentBuilder) {
            addAnalyzer((AnalyzerComponentBuilder<?>) builder);
        } else {
            throw new UnsupportedOperationException("Unknown component type: " + builder);
        }
        return builder;
    }

    private void onComponentAdded() {
        if (_parentBuilder != null && getComponentCount() == 1) {
            // make a copy since some of the listeners may add additional
            // listeners
            // which will otherwise cause ConcurrentModificationExceptions
            final List<AnalysisJobChangeListener> listeners =
                    new ArrayList<>(_parentBuilder.getAnalysisJobChangeListeners());
            for (final AnalysisJobChangeListener analysisJobChangeListener : listeners) {
                try {
                    analysisJobChangeListener.onActivation(this);
                } catch (final Exception e) {
                    logger.warn("A listener failed when trying to inform it of activation", e);
                }
            }
        }
    }

    private void onComponentRemoved() {
        if (_parentBuilder != null && getComponentCount() == 0) {
            // make a copy since some of the listeners may add additional
            // listeners
            // which will otherwise cause ConcurrentModificationExceptions
            final List<AnalysisJobChangeListener> listeners =
                    new ArrayList<>(_parentBuilder.getAnalysisJobChangeListeners());
            for (final AnalysisJobChangeListener analysisJobChangeListener : listeners) {
                try {
                    analysisJobChangeListener.onDeactivation(this);
                } catch (final Exception e) {
                    logger.warn("A listener failed when trying to inform it of deactivation", e);
                }
            }
        }
    }

    /**
     * Adds a {@link ComponentBuilder} and removes it from its previous scope.
     *
     * @param builder
     *            The builder to add
     * @return The same builder
     */
    public ComponentBuilder moveComponent(final ComponentBuilder builder) {
        if (builder.getAnalysisJobBuilder() != this) {
            builder.getAnalysisJobBuilder().removeComponent(builder);

            // when moving the component to a different scope we need to first
            // reset
            // the prior input
            builder.clearInputColumns();

            addComponent(builder);
            builder.setAnalysisJobBuilder(this);
        }

        return builder;
    }

    /**
     * Creates a component builder similar to the incoming {@link ComponentJob}.
     * Note that input (columns and requirements) will not be mapped since these
     * depend on the context of the {@link FilterJob} and may not be matched in
     * the {@link AnalysisJobBuilder}.
     *
     * @param componentJob
     *
     * @return the builder object for the specific component
     */
    protected ComponentBuilder addComponent(final ComponentJob componentJob) {
        final ComponentDescriptor<?> descriptor = componentJob.getDescriptor();
        final ComponentBuilder builder = addComponent(descriptor);

        builder.setName(componentJob.getName());
        builder.setConfiguredProperties(componentJob.getConfiguration());
        builder.setMetadataProperties(componentJob.getMetadataProperties());

        if (componentJob instanceof InputColumnSourceJob) {
            final InputColumn<?>[] output = ((InputColumnSourceJob) componentJob).getOutput();

            final TransformerComponentBuilder<?> transformerJobBuilder = (TransformerComponentBuilder<?>) builder;
            final List<MutableInputColumn<?>> outputColumns = transformerJobBuilder.getOutputColumns();

            assert output.length == outputColumns.size();

            for (int i = 0; i < output.length; i++) {
                final MutableInputColumn<?> mutableOutputColumn = outputColumns.get(i);
                mutableOutputColumn.setName(output[i].getName());
            }
        }

        return builder;
    }

    public AnalysisJobBuilder removeComponent(final ComponentBuilder builder) {
        if (builder instanceof FilterComponentBuilder) {
            removeFilter((FilterComponentBuilder<?, ?>) builder);
        } else if (builder instanceof TransformerComponentBuilder) {
            removeTransformer((TransformerComponentBuilder<?>) builder);
        } else if (builder instanceof AnalyzerComponentBuilder) {
            removeAnalyzer((AnalyzerComponentBuilder<?>) builder);
        } else {
            throw new UnsupportedOperationException("Unknown component type: " + builder);
        }

        return this;
    }

    public <F extends Filter<C>, C extends Enum<C>> FilterComponentBuilder<F, C> addFilter(final Class<F> filterClass) {
        final FilterDescriptor<F, C> descriptor =
                _configuration.getEnvironment().getDescriptorProvider().getFilterDescriptorForClass(filterClass);
        if (descriptor == null) {
            throw new IllegalArgumentException("No descriptor found for: " + filterClass);
        }
        return addFilter(descriptor);
    }

    public <F extends Filter<C>, C extends Enum<C>> FilterComponentBuilder<F, C> addFilter(
            final FilterDescriptor<F, C> descriptor) {
        return addFilter(descriptor, null, null, null);
    }

    public <F extends Filter<C>, C extends Enum<C>> FilterComponentBuilder<F, C> addFilter(
            final FilterDescriptor<F, C> descriptor,
            final Map<ConfiguredPropertyDescriptor, Object> configuredProperties,
            final ComponentRequirement requirement, final Map<String, String> metadataProperties) {
        final FilterComponentBuilder<F, C> filter = new FilterComponentBuilder<>(this, descriptor);
        initializeComponentBuilder(filter, configuredProperties, requirement, metadataProperties);
        return addFilter(filter);
    }

    private void initializeComponentBuilder(final ComponentBuilder component,
            final Map<ConfiguredPropertyDescriptor, Object> configuredProperties,
            final ComponentRequirement requirement, final Map<String, String> metadataProperties) {
        if (configuredProperties != null) {
            component.setConfiguredProperties(configuredProperties);
        }
        if (requirement != null) {
            component.setComponentRequirement(requirement);
        }
        if (metadataProperties != null) {
            component.setMetadataProperties(metadataProperties);
        }
    }

    public <F extends Filter<C>, C extends Enum<C>> FilterComponentBuilder<F, C> addFilter(
            final FilterComponentBuilder<F, C> fjb) {
        _filterComponentBuilders.add(fjb);

        if (fjb.getComponentRequirement() == null) {
            fjb.setComponentRequirement(_defaultRequirement);
        }

        // Before triggering component's listeners, so listeners are ready.
        onComponentAdded();

        final List<FilterChangeListener> listeners = new ArrayList<>(_filterChangeListeners);
        for (final FilterChangeListener listener : listeners) {
            listener.onAdd(fjb);
        }
        return fjb;
    }

    public AnalysisJobBuilder removeFilter(final FilterComponentBuilder<?, ?> filterJobBuilder) {
        final boolean removed = _filterComponentBuilders.remove(filterJobBuilder);

        if (removed) {
            final ComponentRequirement previousRequirement = filterJobBuilder.getComponentRequirement();

            // clean up components who depend on this filter
            final Collection<FilterOutcome> outcomes = filterJobBuilder.getFilterOutcomes();
            for (final FilterOutcome outcome : outcomes) {
                if (_defaultRequirement != null && _defaultRequirement.getProcessingDependencies().contains(outcome)) {
                    setDefaultRequirement((ComponentRequirement) null);
                }

                for (final ComponentBuilder cb : getComponentBuilders()) {
                    final ComponentRequirement requirement = cb.getComponentRequirement();
                    if (requirement != null && requirement.getProcessingDependencies().contains(outcome)) {
                        cb.setComponentRequirement(previousRequirement);
                    }
                }
            }

            filterJobBuilder.onRemoved();

            // removal last, so listeners gets triggered
            onComponentRemoved();
        }
        return this;
    }

    public List<AnalyzerComponentBuilder<?>> getAnalyzerComponentBuilders() {
        return Collections.unmodifiableList(_analyzerComponentBuilders);
    }

    public List<FilterComponentBuilder<?, ?>> getFilterComponentBuilders() {
        return Collections.unmodifiableList(_filterComponentBuilders);
    }

    public Optional<FilterComponentBuilder<?, ?>> getFilterComponentBuilderByName(final String name) {
        return _filterComponentBuilders.stream().filter(f -> f.getName() != null && f.getName().equals(name))
                .findFirst();
    }

    public <A extends Analyzer<?>> AnalyzerComponentBuilder<A> addAnalyzer(final AnalyzerDescriptor<A> descriptor) {
        return addAnalyzer(descriptor, null, null, null);
    }

    public <A extends Analyzer<?>> AnalyzerComponentBuilder<A> addAnalyzer(final AnalyzerDescriptor<A> descriptor,
            final Map<ConfiguredPropertyDescriptor, Object> configuredProperties,
            final ComponentRequirement requirement, final Map<String, String> metadataProperties) {
        final AnalyzerComponentBuilder<A> analyzerJobBuilder = new AnalyzerComponentBuilder<>(this, descriptor);
        initializeComponentBuilder(analyzerJobBuilder, configuredProperties, requirement, metadataProperties);
        return addAnalyzer(analyzerJobBuilder);
    }

    public <A extends Analyzer<?>> AnalyzerComponentBuilder<A> addAnalyzer(
            final AnalyzerComponentBuilder<A> analyzerJobBuilder) {
        _analyzerComponentBuilders.add(analyzerJobBuilder);

        if (analyzerJobBuilder.getComponentRequirement() == null) {
            analyzerJobBuilder.setComponentRequirement(_defaultRequirement);
        }

        // Before triggering component's listeners, so listeners are ready.
        onComponentAdded();

        // make a copy since some of the listeners may add additional listeners
        // which will otherwise cause ConcurrentModificationExceptions
        final List<AnalyzerChangeListener> listeners = new ArrayList<>(_analyzerChangeListeners);
        for (final AnalyzerChangeListener listener : listeners) {
            listener.onAdd(analyzerJobBuilder);
        }
        return analyzerJobBuilder;
    }

    public <A extends Analyzer<?>> AnalyzerComponentBuilder<A> addAnalyzer(final Class<A> analyzerClass) {
        final DescriptorProvider descriptorProvider = _configuration.getEnvironment().getDescriptorProvider();
        final AnalyzerDescriptor<A> descriptor = descriptorProvider.getAnalyzerDescriptorForClass(analyzerClass);
        if (descriptor == null) {
            throw new IllegalArgumentException("No descriptor found for: " + analyzerClass);
        }
        return addAnalyzer(descriptor);
    }

    public AnalysisJobBuilder removeAnalyzer(final AnalyzerComponentBuilder<?> acb) {
        final boolean removed = _analyzerComponentBuilders.remove(acb);
        if (removed) {
            acb.onRemoved();

            // Ajb removal last, so listeners gets triggered
            onComponentRemoved();
        }
        return this;
    }

    /**
     * Finds the available input columns (source or transformed) that match the
     * given data type specification.
     *
     * @param dataType
     *            the data type to look for
     * @return a list of matching input columns
     */
    public List<InputColumn<?>> getAvailableInputColumns(final Class<?> dataType) {
        final SourceColumnFinder finder = new SourceColumnFinder();
        finder.addSources(this);
        return finder.findInputColumns(dataType);
    }

    /**
     * Used to verify whether or not the builder's and its immediate children
     * configuration is valid and all properties are satisfied.
     *
     * @param throwException
     *            whether or not an exception should be thrown in case of
     *            invalid configuration. Typically an exception message will
     *            contain more detailed information about the cause of the
     *            validation error, whereas a boolean contains no details.
     * @return true if the analysis job builder is correctly configured
     * @throws UnconfiguredConfiguredPropertyException
     *             if a required property is not configured
     * @throws ComponentValidationException
     *             if custom validation (using {@link Validate} method or so) of
     *             a component fails.
     * @throws NoResultProducingComponentsException
     *             if no result producing components (see
     *             {@link HasAnalyzerResult}) exist in the job.
     * @throws IllegalStateException
     *             if any other (mostly unexpected) configuration issue occurs
     */
    public boolean isConfigured(final boolean throwException)
            throws UnconfiguredConfiguredPropertyException, ComponentValidationException,
            NoResultProducingComponentsException, IllegalStateException {
        return checkConfiguration(throwException) && isConsumedOutDataStreamsJobBuilderConfigured(throwException);
    }

    private boolean checkConfiguration(final boolean throwException)
            throws IllegalStateException, NoResultProducingComponentsException, ComponentValidationException,
            UnconfiguredConfiguredPropertyException {
        if (_datastoreConnection == null) {
            if (throwException) {
                throw new IllegalStateException("No Datastore or DatastoreConnection set");
            }
            return false;
        }

        if (_sourceColumns.isEmpty()) {
            if (throwException) {
                throw new IllegalStateException("No source columns in job");
            }
            return false;
        }

        if (getResultProducingComponentBuilders().isEmpty() && getConsumedOutputDataStreamsJobBuilders().isEmpty()) {
            if (throwException) {
                throw new NoResultProducingComponentsException();
            }
            return false;
        }

        for (final FilterComponentBuilder<?, ?> fjb : _filterComponentBuilders) {
            if (!fjb.isConfigured(throwException)) {
                return false;
            }
        }

        for (final TransformerComponentBuilder<?> tjb : _transformerComponentBuilders) {
            if (!tjb.isConfigured(throwException)) {
                return false;
            }
        }

        for (final AnalyzerComponentBuilder<?> ajb : _analyzerComponentBuilders) {
            if (!ajb.isConfigured(throwException)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Used to verify whether or not the builder's and its immediate children
     * configuration is valid and all properties are satisfied.
     *
     * @return true if the analysis job builder is correctly configured
     */
    public boolean isConfigured() {
        return isConfigured(false);
    }

    /**
     * Creates an analysis job of this {@link AnalysisJobBuilder}.
     *
     * @param validate
     *            whether or not to validate job configuration while building
     * @return
     * @throws IllegalStateException
     *             if the job is invalidly configured.
     */
    public AnalysisJob toAnalysisJob(final boolean validate) throws IllegalStateException {
        return toAnalysisJob(validate, new AnalysisJobImmutabilizer());
    }

    protected AnalysisJob toAnalysisJob(final boolean validate, final AnalysisJobImmutabilizer immutabilizer)
            throws IllegalStateException {
        if (validate && !isConfigured(true)) {
            throw new IllegalStateException("Analysis job is not correctly configured");
        }

        final Collection<FilterJob> filterJobs = new LinkedList<>();
        for (final FilterComponentBuilder<?, ?> fjb : _filterComponentBuilders) {
            try {
                final FilterJob filterJob = fjb.toFilterJob(validate, immutabilizer);
                filterJobs.add(filterJob);
            } catch (final IllegalStateException e) {
                throw new IllegalStateException(
                        "Could not create filter job from builder: " + fjb + ", (" + e.getMessage() + ")", e);
            }
        }

        final Collection<TransformerJob> transformerJobs = new LinkedList<>();
        for (final TransformerComponentBuilder<?> tjb : _transformerComponentBuilders) {
            final TransformerJob componentJob = immutabilizer.getOrCreateTransformerJob(validate, tjb);
            transformerJobs.add(componentJob);
        }

        final Collection<AnalyzerJob> analyzerJobs = new LinkedList<>();
        for (final AnalyzerComponentBuilder<?> ajb : _analyzerComponentBuilders) {
            try {
                final AnalyzerJob[] analyzerJob = ajb.toAnalyzerJobs(validate, immutabilizer);
                Collections.addAll(analyzerJobs, analyzerJob);
            } catch (final IllegalArgumentException e) {
                throw new IllegalStateException(
                        "Could not create analyzer job from builder: " + ajb + ", (" + e.getMessage() + ")", e);
            }
        }

        final Datastore datastore = getDatastore();

        final AnalysisJobMetadata metadata;
        if (_analysisJobMetadata == null) {
            metadata = createMetadata();
        } else {
            metadata = _analysisJobMetadata;
        }

        return new ImmutableAnalysisJob(metadata, datastore, _sourceColumns, filterJobs, transformerJobs, analyzerJobs);
    }

    public AnalysisJobMetadata createMetadata() {
        final MutableAnalysisJobMetadata mutableAnalysisJobMetadata = getAnalysisJobMetadata();

        final Datastore datastore = getDatastore();
        final String datastoreName = (datastore == null ? null : datastore.getName());

        final List<MetaModelInputColumn> sourceColumns = getSourceColumns();
        final List<String> sourceColumnPaths = new ArrayList<>(sourceColumns.size());
        final List<ColumnType> sourceColumnTypes = new ArrayList<>(sourceColumns.size());
        for (final MetaModelInputColumn sourceColumn : sourceColumns) {
            final Column column = sourceColumn.getPhysicalColumn();
            final String path = column.getQualifiedLabel();
            final ColumnType type = column.getType();

            sourceColumnPaths.add(path);
            sourceColumnTypes.add(type);
        }

        final Map<String, String> properties = mutableAnalysisJobMetadata.getProperties();
        final Map<String, String> variables = mutableAnalysisJobMetadata.getVariables();

        final String jobName = mutableAnalysisJobMetadata.getJobName();
        final String jobVersion = mutableAnalysisJobMetadata.getJobVersion();
        final String jobDescription = mutableAnalysisJobMetadata.getJobDescription();
        final String author = mutableAnalysisJobMetadata.getAuthor();
        final Date createdDate = mutableAnalysisJobMetadata.getCreatedDate();
        final Date updatedDate = mutableAnalysisJobMetadata.getUpdatedDate();

        return new ImmutableAnalysisJobMetadata(jobName, jobVersion, jobDescription, author, createdDate, updatedDate,
                datastoreName, sourceColumnPaths, sourceColumnTypes, variables, properties);
    }

    /**
     * Creates an analysis job of this {@link AnalysisJobBuilder}.
     *
     * @return
     * @throws IllegalStateException
     *             if the job is invalidly configured. See
     *             {@link #isConfigured(boolean)} for detailed exception
     *             descriptions.
     */
    public AnalysisJob toAnalysisJob() throws RuntimeException {
        return toAnalysisJob(true);
    }

    public InputColumn<?> getSourceColumnByName(final String name) {
        if (name != null) {
            for (final MetaModelInputColumn inputColumn : _sourceColumns) {
                final String qualifiedLabel = inputColumn.getPhysicalColumn().getQualifiedLabel();
                if (name.equalsIgnoreCase(qualifiedLabel)) {
                    return inputColumn;
                }
            }

            for (final MetaModelInputColumn inputColumn : _sourceColumns) {
                if (name.equals(inputColumn.getName())) {
                    return inputColumn;
                }
            }

            for (final MetaModelInputColumn inputColumn : _sourceColumns) {
                if (name.equalsIgnoreCase(inputColumn.getName())) {
                    return inputColumn;
                }
            }
        }
        return null;
    }

    public TransformerComponentBuilder<?> getOriginatingTransformer(final InputColumn<?> outputColumn) {
        final SourceColumnFinder finder = new SourceColumnFinder();
        finder.addSources(this);
        final InputColumnSourceJob source = finder.findInputColumnSource(outputColumn);
        if (source instanceof TransformerComponentBuilder) {
            return (TransformerComponentBuilder<?>) source;
        }
        return null;
    }

    public Table getOriginatingTable(final InputColumn<?> inputColumn) {
        final SourceColumnFinder finder = new SourceColumnFinder();
        finder.addSources(this);
        return finder.findOriginatingTable(inputColumn);
    }

    public Table getOriginatingTable(final ComponentBuilder componentBuilder) {
        final InputColumn<?>[] inputColumns = componentBuilder.getInput();
        if (inputColumns.length == 0) {
            return null;
        } else {
            return getOriginatingTable(inputColumns[0]);
        }
    }

    public List<ComponentBuilder> getAvailableUnfilteredBeans(final FilterComponentBuilder<?, ?> filterJobBuilder) {
        final List<ComponentBuilder> result = new ArrayList<>();
        if (filterJobBuilder.isConfigured()) {
            final Table requiredTable = getOriginatingTable(filterJobBuilder);

            for (final FilterComponentBuilder<?, ?> fjb : _filterComponentBuilders) {
                if (fjb != filterJobBuilder) {
                    if (fjb.getComponentRequirement() == null) {
                        final Table foundTable = getOriginatingTable(fjb);
                        if (requiredTable == null || requiredTable.equals(foundTable)) {
                            result.add(fjb);
                        }
                    }
                }
            }

            for (final TransformerComponentBuilder<?> tjb : _transformerComponentBuilders) {
                if (tjb.getComponentRequirement() == null) {
                    final Table foundTable = getOriginatingTable(tjb);
                    if (requiredTable == null || requiredTable.equals(foundTable)) {
                        result.add(tjb);
                    }
                }
            }

            for (final AnalyzerComponentBuilder<?> ajb : _analyzerComponentBuilders) {
                if (ajb != null) {
                    if (ajb.getComponentRequirement() == null) {
                        final Table foundTable = getOriginatingTable(ajb);
                        if (requiredTable == null || requiredTable.equals(foundTable)) {
                            result.add(ajb);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Sets a default requirement for all newly added and existing row
     * processing component, unless they have another requirement.
     *
     * @param filterJobBuilder
     * @param category
     */
    public void setDefaultRequirement(final FilterComponentBuilder<?, ?> filterJobBuilder, final Enum<?> category) {
        setDefaultRequirement(filterJobBuilder.getFilterOutcome(category));
    }

    /**
     * Sets a default requirement for all newly added and existing row
     * processing component, unless they have another requirement.
     *
     * @param defaultRequirement
     */
    public void setDefaultRequirement(final FilterOutcome defaultRequirement) {
        setDefaultRequirement(new SimpleComponentRequirement(defaultRequirement));
    }

    /**
     * Gets a default requirement, which will be applied to all newly added row
     * processing components.
     *
     * @return a default requirement, which will be applied to all newly added
     *         row processing components.
     */
    public ComponentRequirement getDefaultRequirement() {
        return _defaultRequirement;
    }

    public void setDefaultRequirement(final ComponentRequirement defaultRequirement) {
        _defaultRequirement = defaultRequirement;
        if (defaultRequirement != null) {

            final FilterComponentBuilder<?, ?> sourceFilterJobBuilder;
            if (defaultRequirement instanceof SimpleComponentRequirement) {
                final FilterOutcome outcome = ((SimpleComponentRequirement) defaultRequirement).getOutcome();
                if (outcome instanceof LazyFilterOutcome) {
                    sourceFilterJobBuilder = ((LazyFilterOutcome) outcome).getFilterJobBuilder();
                } else {
                    logger.warn(
                            "Default requirement is not a LazyFilterOutcome. This might cause self-referring requirements.");
                    sourceFilterJobBuilder = null;
                }
            } else {
                logger.warn(
                        "Default requirement is not a LazyFilterOutcome. This might cause self-referring requirements.");
                sourceFilterJobBuilder = null;
            }

            // make a set of components that succeeds the requirement
            final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
            sourceColumnFinder.addSources(this);
            final Set<Object> excludedSet = sourceColumnFinder.findAllSourceJobs(defaultRequirement);

            for (final AnalyzerComponentBuilder<?> ajb : _analyzerComponentBuilders) {
                if (ajb != null) {
                    final ComponentRequirement requirement = ajb.getComponentRequirement();
                    if (requirement == null) {
                        ajb.setComponentRequirement(defaultRequirement);
                    }
                }
            }

            for (final TransformerComponentBuilder<?> tjb : _transformerComponentBuilders) {
                if (tjb.getComponentRequirement() == null && !excludedSet.contains(tjb)) {
                    tjb.setComponentRequirement(defaultRequirement);
                }
            }

            for (final FilterComponentBuilder<?, ?> fjb : _filterComponentBuilders) {
                if (fjb != sourceFilterJobBuilder && fjb.getComponentRequirement() == null && !excludedSet
                        .contains(fjb)) {
                    if (fjb.validateRequirementCandidate(defaultRequirement)) {
                        fjb.setComponentRequirement(defaultRequirement);
                    }
                }
            }
        }
    }

    public void addSourceColumnChangeListener(final SourceColumnChangeListener sourceColumnChangeListener) {
        if (!_sourceColumnListeners.contains(sourceColumnChangeListener)) {
            _sourceColumnListeners.add(sourceColumnChangeListener);
        }
    }

    public void removeSourceColumnChangeListener(final SourceColumnChangeListener sourceColumnChangeListener) {
        _sourceColumnListeners.remove(sourceColumnChangeListener);
    }

    public void addTransformerChangeListener(final TransformerChangeListener transformerChangeListener) {
        if (!_transformerChangeListeners.contains(transformerChangeListener)) {
            _transformerChangeListeners.add(transformerChangeListener);
        }
    }

    public void removeTransformerChangeListener(final TransformerChangeListener transformerChangeListener) {
        _transformerChangeListeners.remove(transformerChangeListener);
    }

    public void addAnalyzerChangeListener(final AnalyzerChangeListener analyzerChangeListener) {
        if (!_analyzerChangeListeners.contains(analyzerChangeListener)) {
            _analyzerChangeListeners.add(analyzerChangeListener);
        }
    }

    public void removeAnalyzerChangeListener(final AnalyzerChangeListener analyzerChangeListener) {
        _analyzerChangeListeners.remove(analyzerChangeListener);
    }

    public void addFilterChangeListener(final FilterChangeListener filterChangeListener) {
        _filterChangeListeners.add(filterChangeListener);
    }

    public void removeFilterChangeListener(final FilterChangeListener filterChangeListener) {
        _filterChangeListeners.remove(filterChangeListener);
    }

    public void addAnalysisJobChangeListener(final AnalysisJobChangeListener analysisJobChangeListener) {
        if (!_analysisJobChangeListeners.contains(analysisJobChangeListener)) {
            _analysisJobChangeListeners.add(analysisJobChangeListener);
        }
    }

    public void removeAnalysisJobChangeListener(final AnalysisJobChangeListener analysisJobChangeListener) {
        _analysisJobChangeListeners.remove(analysisJobChangeListener);
    }

    /**
     * @deprecated Use
     *             {@link #addSourceColumnChangeListener(SourceColumnChangeListener)}
     *             and
     *             {@link #removeSourceColumnChangeListener(SourceColumnChangeListener)}
     */
    @Deprecated
    public List<SourceColumnChangeListener> getSourceColumnListeners() {
        return _sourceColumnListeners;
    }

    /**
     * @deprecated Use
     *             {@link #addAnalyzerChangeListener(AnalyzerChangeListener)}
     *             and
     *             {@link #removeAnalyzerChangeListener(AnalyzerChangeListener)}
     */
    @Deprecated
    public List<AnalyzerChangeListener> getAnalyzerChangeListeners() {
        return _analyzerChangeListeners;
    }

    /**
     * @deprecated Use
     *             {@link #addTransformerChangeListener(TransformerChangeListener)}
     *             and
     *             {@link #removeTransformerChangeListener(TransformerChangeListener)}
     */
    @Deprecated
    public List<TransformerChangeListener> getTransformerChangeListeners() {
        return _transformerChangeListeners;
    }

    /**
     * @deprecated Use {@link #addFilterChangeListener(FilterChangeListener)}
     *             and {@link #removeFilterChangeListener(FilterChangeListener)}
     */
    @Deprecated
    public List<FilterChangeListener> getFilterChangeListeners() {
        return _filterChangeListeners;
    }

    private List<AnalysisJobChangeListener> getAnalysisJobChangeListeners() {
        return _analysisJobChangeListeners;
    }

    public List<Table> getSourceTables() {
        final List<Table> tables = new ArrayList<>();
        final List<MetaModelInputColumn> columns = getSourceColumns();
        for (final MetaModelInputColumn column : columns) {
            final Table table = column.getPhysicalColumn().getTable();
            if (!tables.contains(table)) {
                tables.add(table);
            }
        }
        return tables;
    }

    /**
     * Removes all source columns and all components from the job
     */
    public void reset() {
        setAnalysisJobMetadata(AnalysisJobMetadata.EMPTY_METADATA);
        removeAllSourceColumns();
        removeAllFilters();
        removeAllTransformers();
        removeAllAnalyzers();
    }

    public void removeAllSourceColumns() {
        final Collection<ComponentBuilder> componentBuilders = getComponentBuilders();

        for (final ComponentBuilder componentBuilder : componentBuilders) {
            componentBuilder.clearInputColumns();
        }

        final List<SourceColumnChangeListener> listeners = new ArrayList<>(_sourceColumnListeners);

        for (final SourceColumnChangeListener listener : listeners) {
            for (final MetaModelInputColumn inputColumn : _sourceColumns) {
                listener.onRemove(inputColumn);
            }
        }

        _sourceColumns.clear();
    }

    public void removeAllAnalyzers() {
        final List<AnalyzerComponentBuilder<?>> analyzers = new ArrayList<>(_analyzerComponentBuilders);
        for (final AnalyzerComponentBuilder<?> ajb : analyzers) {
            removeAnalyzer(ajb);
        }
        assert _analyzerComponentBuilders.isEmpty();
    }

    public void removeAllTransformers() {
        final List<TransformerComponentBuilder<?>> transformers = new ArrayList<>(_transformerComponentBuilders);
        for (final TransformerComponentBuilder<?> transformerJobBuilder : transformers) {
            removeTransformer(transformerJobBuilder);
        }
        assert _transformerComponentBuilders.isEmpty();
    }

    public void removeAllComponents() {
        removeAllAnalyzers();
        removeAllFilters();
        removeAllTransformers();
    }

    public void removeAllFilters() {
        final List<FilterComponentBuilder<?, ?>> filters = new ArrayList<>(_filterComponentBuilders);
        for (final FilterComponentBuilder<?, ?> filterJobBuilder : filters) {
            removeFilter(filterJobBuilder);
        }
        assert _filterComponentBuilders.isEmpty();
    }

    /**
     * Gets a mutable {@link Map} for setting properties that will eventually be
     * available via {@link AnalysisJobMetadata#getProperties()}.
     *
     * @return
     */
    public Map<String, String> getMetadataProperties() {
        return getAnalysisJobMetadata().getProperties();
    }

    @Override
    public void close() {
        if (_datastoreConnection != null) {
            _datastoreConnection.close();
        }
    }

    public AnalysisJobBuilder withoutListeners() {
        final MutableAnalysisJobMetadata metadataClone = new MutableAnalysisJobMetadata(getAnalysisJobMetadata());
        return new AnalysisJobBuilder(_configuration, _datastore, _datastoreConnection, metadataClone, _sourceColumns,
                _defaultRequirement, _transformedColumnIdGenerator, _transformerComponentBuilders,
                _filterComponentBuilders, _analyzerComponentBuilders, _parentBuilder);
    }

    /**
     * Gets the total number of active components (transformation or analysis)
     * in this job.
     *
     * @return
     */
    public int getComponentCount() {
        return _filterComponentBuilders.size() + _transformerComponentBuilders.size() + _analyzerComponentBuilders
                .size();
    }

    /**
     * Gets all component builders contained within this
     * {@link AnalysisJobBuilder}
     *
     * @return
     */
    public Collection<ComponentBuilder> getComponentBuilders() {
        final Collection<ComponentBuilder> result = new ArrayList<>();
        result.addAll(_filterComponentBuilders);
        result.addAll(_transformerComponentBuilders);
        result.addAll(_analyzerComponentBuilders);
        return result;
    }

    /**
     * Gets all component builders that are expected to generate an
     * {@link AnalyzerResult}.
     *
     * @return
     */
    public Collection<ComponentBuilder> getResultProducingComponentBuilders() {
        final Collection<ComponentBuilder> componentBuilders = getComponentBuilders();

        return CollectionUtils.filter(componentBuilders, componentBuilder -> {
            final ComponentDescriptor<?> descriptor = componentBuilder.getDescriptor();
            return ReflectionUtils.is(descriptor.getComponentClass(), HasAnalyzerResult.class);
        });
    }

    /**
     * Gets all available {@link InputColumn}s to map to a particular
     * {@link ComponentBuilder}
     *
     * @param componentBuilder
     * @return
     */
    public List<InputColumn<?>> getAvailableInputColumns(final ComponentBuilder componentBuilder) {
        return getAvailableInputColumns(componentBuilder, Object.class);
    }

    /**
     * Gets all available {@link InputColumn}s of a particular type to map to a
     * particular {@link ComponentBuilder}
     *
     * @param componentBuilder
     * @param dataType
     * @return
     */
    public List<InputColumn<?>> getAvailableInputColumns(final ComponentBuilder componentBuilder,
            final Class<?> dataType) {
        List<InputColumn<?>> result = getAvailableInputColumns(dataType);

        final SourceColumnFinder finder = new SourceColumnFinder();
        finder.addSources(this);

        result = CollectionUtils.filter(result, inputColumn -> {
            if (inputColumn.isPhysicalColumn()) {
                return true;
            }

            final InputColumnSourceJob origin = finder.findInputColumnSource(inputColumn);
            if (origin == null) {
                return true;
            }

            if (origin == componentBuilder) {
                // exclude columns from the component itself
                return false;
            }

            final Set<Object> sourceComponents = finder.findAllSourceJobs(origin);
            return !sourceComponents.contains(componentBuilder);

        });

        return result;
    }

    public boolean isRootJobBuilder() {
        return getParentJobBuilder() == null;
    }

    public AnalysisJobBuilder getParentJobBuilder() {
        return _parentBuilder;
    }

    public AnalysisJobBuilder getRootJobBuilder() {
        @SuppressWarnings("resource") AnalysisJobBuilder builder = this;
        AnalysisJobBuilder tempBuilder = builder._parentBuilder;
        while (tempBuilder != null) {
            builder = tempBuilder;
            tempBuilder = builder._parentBuilder;
        }

        return builder;
    }

    /**
     * This gets all job builders from consumed {@link OutputDataStream}s. This
     * only pertains to immediate children.
     */
    public List<AnalysisJobBuilder> getConsumedOutputDataStreamsJobBuilders() {
        final List<AnalysisJobBuilder> consumedOutputDataStreamJobBuilders = new ArrayList<>();
        for (final ComponentBuilder builder : getComponentBuilders()) {
            for (final OutputDataStream outputDataStream : builder.getOutputDataStreams()) {
                if (builder.isOutputDataStreamConsumed(outputDataStream)) {
                    consumedOutputDataStreamJobBuilders.add(builder.getOutputDataStreamJobBuilder(outputDataStream));
                }
            }
        }
        return consumedOutputDataStreamJobBuilders;
    }

    /**
     * Checks if a job children are configured.
     **/
    private boolean isConsumedOutDataStreamsJobBuilderConfigured(final boolean throwException) {
        final List<AnalysisJobBuilder> consumedOutputDataStreamsJobBuilders = getConsumedOutputDataStreamsJobBuilders();
        for (final AnalysisJobBuilder analysisJobBuilder : consumedOutputDataStreamsJobBuilders) {
            if (!analysisJobBuilder.isConfigured(throwException)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if the job being built is going to be distributable in a
     * cluster execution environment.
     *
     * @return
     */
    public boolean isDistributable() {
        final Collection<ComponentBuilder> componentBuilders = getComponentBuilders();
        for (final ComponentBuilder componentBuilder : componentBuilders) {
            if (!componentBuilder.isDistributable()) {
                return false;
            }
        }

        final List<AnalysisJobBuilder> childJobBuilders = getConsumedOutputDataStreamsJobBuilders();
        for (final AnalysisJobBuilder childJobBuilder : childJobBuilders) {
            if (!childJobBuilder.isDistributable()) {
                return false;
            }
        }

        return true;
    }
}
