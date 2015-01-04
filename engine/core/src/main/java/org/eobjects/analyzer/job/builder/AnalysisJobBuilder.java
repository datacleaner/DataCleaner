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
package org.eobjects.analyzer.job.builder;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.Predicate;
import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalysisJobImmutabilizer;
import org.eobjects.analyzer.job.AnalysisJobMetadata;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.ComponentRequirement;
import org.eobjects.analyzer.job.ConfigurableBeanJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.job.IdGenerator;
import org.eobjects.analyzer.job.ImmutableAnalysisJob;
import org.eobjects.analyzer.job.ImmutableAnalysisJobMetadata;
import org.eobjects.analyzer.job.InputColumnSourceJob;
import org.eobjects.analyzer.job.PrefixedIdGenerator;
import org.eobjects.analyzer.job.SimpleComponentRequirement;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.util.SchemaNavigator;
import org.eobjects.analyzer.util.SourceColumnFinder;
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

    private final AnalyzerBeansConfiguration _configuration;
    private final IdGenerator _transformedColumnIdGenerator;

    // the configurable components
    private Datastore _datastore;
    private DatastoreConnection _datastoreConnection;

    private final List<MetaModelInputColumn> _sourceColumns;
    private final List<FilterJobBuilder<?, ?>> _filterJobBuilders;
    private final List<TransformerJobBuilder<?>> _transformerJobBuilders;
    private final List<AnalyzerJobBuilder<?>> _analyzerJobBuilders;

    private MutableAnalysisJobMetadata _analysisJobMetadata;

    // listeners, typically for UI that uses the builders
    private final List<SourceColumnChangeListener> _sourceColumnListeners = new ArrayList<SourceColumnChangeListener>();
    private final List<AnalyzerChangeListener> _analyzerChangeListeners = new ArrayList<AnalyzerChangeListener>();
    private final List<TransformerChangeListener> _transformerChangeListeners = new ArrayList<TransformerChangeListener>();
    private final List<FilterChangeListener> _filterChangeListeners = new ArrayList<FilterChangeListener>();
    private ComponentRequirement _defaultRequirement;

    public AnalysisJobBuilder(AnalyzerBeansConfiguration configuration) {
        _configuration = configuration;
        _transformedColumnIdGenerator = new PrefixedIdGenerator("");
        _sourceColumns = new ArrayList<MetaModelInputColumn>();
        _filterJobBuilders = new ArrayList<FilterJobBuilder<?, ?>>();
        _transformerJobBuilders = new ArrayList<TransformerJobBuilder<?>>();
        _analyzerJobBuilders = new ArrayList<AnalyzerJobBuilder<?>>();
    }

    /**
     * Private constructor for {@link #withoutListeners()} method
     */
    private AnalysisJobBuilder(AnalyzerBeansConfiguration configuration, Datastore datastore,
            DatastoreConnection datastoreConnection, MutableAnalysisJobMetadata metadata,
            List<MetaModelInputColumn> sourceColumns, ComponentRequirement defaultRequirement, IdGenerator idGenerator,
            List<TransformerJobBuilder<?>> transformerJobBuilders, List<FilterJobBuilder<?, ?>> filterJobBuilders,
            List<AnalyzerJobBuilder<?>> analyzerJobBuilders) {
        _configuration = configuration;
        _datastore = datastore;
        _analysisJobMetadata = metadata;
        _datastoreConnection = datastoreConnection;
        _sourceColumns = sourceColumns;
        _defaultRequirement = defaultRequirement;
        _transformedColumnIdGenerator = idGenerator;
        _filterJobBuilders = filterJobBuilders;
        _transformerJobBuilders = transformerJobBuilders;
        _analyzerJobBuilders = analyzerJobBuilders;
    }

    public AnalysisJobBuilder(AnalyzerBeansConfiguration configuration, AnalysisJob job) {
        this(configuration);
        importJob(job);
    }

    public AnalysisJobBuilder setDatastore(String datastoreName) {
        Datastore datastore = _configuration.getDatastoreCatalog().getDatastore(datastoreName);
        if (datastore == null) {
            throw new IllegalArgumentException("No such datastore: " + datastoreName);
        }
        return setDatastore(datastore);
    }

    public Datastore getDatastore() {
        return _datastore;
    }

    public AnalysisJobBuilder setDatastore(Datastore datastore) {
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

    public AnalysisJobBuilder setDatastoreConnection(DatastoreConnection datastoreConnection) {
        if (_datastoreConnection != null) {
            _datastoreConnection.close();
        }
        _datastoreConnection = datastoreConnection;

        if (datastoreConnection != null && _datastore == null) {
            Datastore datastore = datastoreConnection.getDatastore();
            if (datastore != null) {
                setDatastore(datastore);
            }
        }

        return this;
    }

    public DatastoreConnection getDatastoreConnection() {
        return _datastoreConnection;
    }

    public AnalyzerBeansConfiguration getConfiguration() {
        return _configuration;
    }

    public AnalysisJobBuilder addSourceColumn(Column column) {
        MetaModelInputColumn inputColumn = new MetaModelInputColumn(column);
        return addSourceColumn(inputColumn);
    }

    public AnalysisJobBuilder addSourceColumn(MetaModelInputColumn inputColumn) {
        if (!_sourceColumns.contains(inputColumn)) {
            _sourceColumns.add(inputColumn);

            List<SourceColumnChangeListener> listeners = new ArrayList<SourceColumnChangeListener>(
                    _sourceColumnListeners);
            for (SourceColumnChangeListener listener : listeners) {
                listener.onAdd(inputColumn);
            }
        }
        return this;
    }

    public AnalysisJobBuilder addSourceColumns(Collection<Column> columns) {
        for (Column column : columns) {
            addSourceColumn(column);
        }
        return this;
    }

    public AnalysisJobBuilder addSourceColumns(Column... columns) {
        for (Column column : columns) {
            addSourceColumn(column);
        }
        return this;
    }

    public AnalysisJobBuilder addSourceColumns(MetaModelInputColumn... inputColumns) {
        for (MetaModelInputColumn metaModelInputColumn : inputColumns) {
            addSourceColumn(metaModelInputColumn);
        }
        return this;
    }

    public AnalysisJobBuilder addSourceColumns(String... columnNames) {
        if (_datastoreConnection == null) {
            throw new IllegalStateException(
                    "Cannot add source columns by name when no Datastore or DatastoreConnection has been set");
        }
        SchemaNavigator schemaNavigator = _datastoreConnection.getSchemaNavigator();
        Column[] columns = new Column[columnNames.length];
        for (int i = 0; i < columns.length; i++) {
            String columnName = columnNames[i];
            Column column = schemaNavigator.convertToColumn(columnName);
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
    public AnalysisJobBuilder removeSourceTable(Table table) {
        final Column[] cols = table.getColumns();
        for (Column col : cols) {
            removeSourceColumn(col);
        }
        return this;
    }

    public AnalysisJobBuilder removeSourceColumn(Column column) {
        MetaModelInputColumn inputColumn = new MetaModelInputColumn(column);
        return removeSourceColumn(inputColumn);
    }

    /**
     * Imports the datastore, components and configuration of a
     * {@link AnalysisJob} into this builder.
     * 
     * @param job
     */
    public void importJob(AnalysisJob job) {
        AnalysisJobBuilderImportHelper helper = new AnalysisJobBuilderImportHelper(this);
        helper.importJob(job);
    }

    public AnalysisJobBuilder removeSourceColumn(MetaModelInputColumn inputColumn) {
        boolean removed = _sourceColumns.remove(inputColumn);
        if (removed) {
            List<SourceColumnChangeListener> listeners = new ArrayList<SourceColumnChangeListener>(
                    _sourceColumnListeners);
            for (SourceColumnChangeListener listener : listeners) {
                listener.onRemove(inputColumn);
            }
        }
        return this;
    }

    public boolean containsSourceColumn(Column column) {
        for (MetaModelInputColumn sourceColumn : _sourceColumns) {
            if (sourceColumn.getPhysicalColumn().equals(column)) {
                return true;
            }
        }
        return false;
    }

    public List<MetaModelInputColumn> getSourceColumns() {
        return Collections.unmodifiableList(_sourceColumns);
    }

    public List<MetaModelInputColumn> getSourceColumnsOfTable(Table table) {
        final List<MetaModelInputColumn> result = new ArrayList<>();
        for (MetaModelInputColumn column : _sourceColumns) {
            final Column physicalColumn = column.getPhysicalColumn();
            if (physicalColumn != null && physicalColumn.getTable() == table) {
                result.add(column);
            }
        }
        return result;
    }

    public <T extends Transformer<?>> TransformerJobBuilder<T> addTransformer(Class<T> transformerClass) {
        TransformerBeanDescriptor<T> descriptor = _configuration.getDescriptorProvider()
                .getTransformerBeanDescriptorForClass(transformerClass);
        if (descriptor == null) {
            throw new IllegalArgumentException("No descriptor found for: " + transformerClass);
        }
        return addTransformer(descriptor);
    }

    public List<TransformerJobBuilder<?>> getTransformerJobBuilders() {
        return Collections.unmodifiableList(_transformerJobBuilders);
    }

    public <T extends Transformer<?>> TransformerJobBuilder<T> addTransformer(TransformerBeanDescriptor<T> descriptor) {
        return addTransformer(descriptor, null, null, null);
    }

    public <T extends Transformer<?>> TransformerJobBuilder<T> addTransformer(TransformerBeanDescriptor<T> descriptor,
            Map<ConfiguredPropertyDescriptor, Object> configuredProperties, ComponentRequirement requirement,
            Map<String, String> metadataProperties) {
        final TransformerJobBuilder<T> transformer = new TransformerJobBuilder<T>(this, descriptor,
                _transformedColumnIdGenerator);
        initializeComponentBuilder(transformer, configuredProperties, requirement, metadataProperties);
        return addTransformer(transformer);
    }

    public <T extends Transformer<?>> TransformerJobBuilder<T> addTransformer(TransformerJobBuilder<T> tjb) {
        if (tjb.getComponentRequirement() == null) {
            tjb.setComponentRequirement(_defaultRequirement);
        }
        _transformerJobBuilders.add(tjb);

        // make a copy since some of the listeners may add additional listeners
        // which will otherwise cause ConcurrentModificationExceptions
        List<TransformerChangeListener> listeners = new ArrayList<TransformerChangeListener>(
                _transformerChangeListeners);
        for (TransformerChangeListener listener : listeners) {
            listener.onAdd(tjb);
        }
        return tjb;
    }

    public AnalysisJobBuilder removeTransformer(TransformerJobBuilder<?> tjb) {
        boolean removed = _transformerJobBuilders.remove(tjb);
        if (removed) {
            tjb.onRemoved();
        }
        return this;
    }

    /**
     * Creates a filter job builder like the incoming filter job. Note that
     * input (columns and requirements) will not be mapped since these depend on
     * the context of the {@link FilterJob} and may not be matched in the
     * {@link AnalysisJobBuilder}.
     * 
     * @param filterJob
     * 
     * @return the builder object for the specific component
     */
    protected Object addComponent(ComponentJob componentJob) {
        final AbstractBeanJobBuilder<?, ?, ?> builder;
        if (componentJob instanceof FilterJob) {
            builder = addFilter((FilterBeanDescriptor<?, ?>) componentJob.getDescriptor());
        } else if (componentJob instanceof TransformerJob) {
            builder = addTransformer((TransformerBeanDescriptor<?>) componentJob.getDescriptor());
        } else if (componentJob instanceof AnalyzerJob) {
            builder = addAnalyzer((AnalyzerBeanDescriptor<?>) componentJob.getDescriptor());
        } else {
            throw new UnsupportedOperationException("Unknown component job type: " + componentJob);
        }

        builder.setName(componentJob.getName());

        if (componentJob instanceof ConfigurableBeanJob<?>) {
            ConfigurableBeanJob<?> configurableBeanJob = (ConfigurableBeanJob<?>) componentJob;
            builder.setConfiguredProperties(configurableBeanJob.getConfiguration());
        }

        if (componentJob instanceof InputColumnSourceJob) {
            InputColumn<?>[] output = ((InputColumnSourceJob) componentJob).getOutput();

            TransformerJobBuilder<?> transformerJobBuilder = (TransformerJobBuilder<?>) builder;
            List<MutableInputColumn<?>> outputColumns = transformerJobBuilder.getOutputColumns();

            assert output.length == outputColumns.size();

            for (int i = 0; i < output.length; i++) {
                MutableInputColumn<?> mutableOutputColumn = outputColumns.get(i);
                mutableOutputColumn.setName(output[i].getName());
            }
        }

        return builder;
    }

    public <F extends Filter<C>, C extends Enum<C>> FilterJobBuilder<F, C> addFilter(Class<F> filterClass) {
        FilterBeanDescriptor<F, C> descriptor = _configuration.getDescriptorProvider().getFilterBeanDescriptorForClass(
                filterClass);
        if (descriptor == null) {
            throw new IllegalArgumentException("No descriptor found for: " + filterClass);
        }
        return addFilter(descriptor);
    }

    public <F extends Filter<C>, C extends Enum<C>> FilterJobBuilder<F, C> addFilter(
            FilterBeanDescriptor<F, C> descriptor) {
        return addFilter(descriptor, null, null, null);
    }

    public <F extends Filter<C>, C extends Enum<C>> FilterJobBuilder<F, C> addFilter(
            FilterBeanDescriptor<F, C> descriptor, Map<ConfiguredPropertyDescriptor, Object> configuredProperties,
            ComponentRequirement requirement, Map<String, String> metadataProperties) {
        final FilterJobBuilder<F, C> filter = new FilterJobBuilder<F, C>(this, descriptor);
        initializeComponentBuilder(filter, configuredProperties, requirement, metadataProperties);
        return addFilter(filter);
    }

    private void initializeComponentBuilder(ComponentBuilder component,
            Map<ConfiguredPropertyDescriptor, Object> configuredProperties, ComponentRequirement requirement,
            Map<String, String> metadataProperties) {
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

    public <F extends Filter<C>, C extends Enum<C>> FilterJobBuilder<F, C> addFilter(FilterJobBuilder<F, C> fjb) {
        _filterJobBuilders.add(fjb);

        if (fjb.getComponentRequirement() == null) {
            fjb.setComponentRequirement(_defaultRequirement);
        }

        List<FilterChangeListener> listeners = new ArrayList<FilterChangeListener>(_filterChangeListeners);
        for (FilterChangeListener listener : listeners) {
            listener.onAdd(fjb);
        }
        return fjb;
    }

    public AnalysisJobBuilder removeFilter(FilterJobBuilder<?, ?> filterJobBuilder) {
        boolean removed = _filterJobBuilders.remove(filterJobBuilder);

        if (removed) {
            final ComponentRequirement previousRequirement = filterJobBuilder.getComponentRequirement();

            // clean up components who depend on this filter
            final Collection<FilterOutcome> outcomes = filterJobBuilder.getFilterOutcomes();
            for (final FilterOutcome outcome : outcomes) {
                if (_defaultRequirement != null && _defaultRequirement.getProcessingDependencies().contains(outcome)) {
                    setDefaultRequirement((ComponentRequirement) null);
                }

                for (final AnalyzerJobBuilder<?> ajb : _analyzerJobBuilders) {
                    final ComponentRequirement requirement = ajb.getComponentRequirement();
                    if (requirement != null && requirement.getProcessingDependencies().contains(outcome)) {
                        ajb.setComponentRequirement(previousRequirement);
                    }
                }

                for (final TransformerJobBuilder<?> tjb : _transformerJobBuilders) {
                    final ComponentRequirement requirement = tjb.getComponentRequirement();
                    if (requirement != null && requirement.getProcessingDependencies().contains(outcome)) {
                        tjb.setComponentRequirement(previousRequirement);
                    }
                }

                for (final FilterJobBuilder<?, ?> fjb : _filterJobBuilders) {
                    final ComponentRequirement requirement = fjb.getComponentRequirement();
                    if (requirement != null && requirement.getProcessingDependencies().contains(outcome)) {
                        fjb.setComponentRequirement(previousRequirement);
                    }
                }
            }

            filterJobBuilder.onRemoved();
        }
        return this;
    }

    public List<AnalyzerJobBuilder<?>> getAnalyzerJobBuilders() {
        return Collections.unmodifiableList(_analyzerJobBuilders);
    }

    public List<FilterJobBuilder<?, ?>> getFilterJobBuilders() {
        return Collections.unmodifiableList(_filterJobBuilders);
    }

    public <A extends Analyzer<?>> AnalyzerJobBuilder<A> addAnalyzer(AnalyzerBeanDescriptor<A> descriptor) {
        return addAnalyzer(descriptor, null, null, null);
    }

    public <A extends Analyzer<?>> AnalyzerJobBuilder<A> addAnalyzer(AnalyzerBeanDescriptor<A> descriptor,
            Map<ConfiguredPropertyDescriptor, Object> configuredProperties, ComponentRequirement requirement,
            Map<String, String> metadataProperties) {
        final AnalyzerJobBuilder<A> analyzerJobBuilder = new AnalyzerJobBuilder<A>(this, descriptor);
        initializeComponentBuilder(analyzerJobBuilder, configuredProperties, requirement, metadataProperties);
        return addAnalyzer(analyzerJobBuilder);
    }

    public <A extends Analyzer<?>> AnalyzerJobBuilder<A> addAnalyzer(AnalyzerJobBuilder<A> analyzerJobBuilder) {
        _analyzerJobBuilders.add(analyzerJobBuilder);

        if (analyzerJobBuilder.getComponentRequirement() == null) {
            analyzerJobBuilder.setComponentRequirement(_defaultRequirement);
        }

        // make a copy since some of the listeners may add additional listeners
        // which will otherwise cause ConcurrentModificationExceptions
        List<AnalyzerChangeListener> listeners = new ArrayList<AnalyzerChangeListener>(_analyzerChangeListeners);
        for (AnalyzerChangeListener listener : listeners) {
            listener.onAdd(analyzerJobBuilder);
        }
        return analyzerJobBuilder;
    }

    public <A extends Analyzer<?>> AnalyzerJobBuilder<A> addAnalyzer(Class<A> analyzerClass) {
        final DescriptorProvider descriptorProvider = _configuration.getDescriptorProvider();
        final AnalyzerBeanDescriptor<A> descriptor = descriptorProvider
                .getAnalyzerBeanDescriptorForClass(analyzerClass);
        if (descriptor == null) {
            throw new IllegalArgumentException("No descriptor found for: " + analyzerClass);
        }
        return addAnalyzer(descriptor);
    }

    public AnalysisJobBuilder removeAnalyzer(AnalyzerJobBuilder<?> ajb) {
        boolean removed = _analyzerJobBuilders.remove(ajb);
        if (removed) {
            ajb.onRemoved();
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
    public List<InputColumn<?>> getAvailableInputColumns(Class<?> dataType) {
        SourceColumnFinder finder = new SourceColumnFinder();
        finder.addSources(this);
        return finder.findInputColumns(dataType);
    }

    /**
     * Used to verify whether or not the builder's configuration is valid and
     * all properties are satisfied.
     * 
     * @param throwException
     *            whether or not an exception should be thrown in case of
     *            invalid configuration. Typically an exception message will
     *            contain more detailed information about the cause of the
     *            validation error, whereas a boolean contains no details.
     * @return true if the analysis job builder is correctly configured
     * @throws IllegalStateException
     */
    public boolean isConfigured(final boolean throwException) throws IllegalStateException,
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

        if (_analyzerJobBuilders.isEmpty()) {
            if (throwException) {
                throw new IllegalStateException("No Analyzers in job");
            }
            return false;
        }

        for (FilterJobBuilder<?, ?> fjb : _filterJobBuilders) {
            if (!fjb.isConfigured(throwException)) {
                return false;
            }
        }

        for (TransformerJobBuilder<?> tjb : _transformerJobBuilders) {
            if (!tjb.isConfigured(throwException)) {
                return false;
            }
        }

        for (AnalyzerJobBuilder<?> ajb : _analyzerJobBuilders) {
            if (!ajb.isConfigured(throwException)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Used to verify whether or not the builder's configuration is valid and
     * all properties are satisfied.
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
    public AnalysisJob toAnalysisJob(boolean validate) throws IllegalStateException {
        if (validate && !isConfigured(true)) {
            throw new IllegalStateException("Analysis job is not correctly configured");
        }

        final AnalysisJobImmutabilizer immutabilizer = new AnalysisJobImmutabilizer();

        final Collection<FilterJob> filterJobs = new LinkedList<FilterJob>();
        for (final FilterJobBuilder<?, ?> fjb : _filterJobBuilders) {
            try {
                final FilterJob filterJob = fjb.toFilterJob(validate, immutabilizer);
                filterJobs.add(filterJob);
            } catch (IllegalStateException e) {
                throw new IllegalStateException("Could not create filter job from builder: " + fjb + ", ("
                        + e.getMessage() + ")", e);
            }
        }

        final Collection<TransformerJob> transformerJobs = new LinkedList<TransformerJob>();
        for (final TransformerJobBuilder<?> tjb : _transformerJobBuilders) {
            try {
                final TransformerJob transformerJob = tjb.toTransformerJob(validate, immutabilizer);
                transformerJobs.add(transformerJob);
            } catch (IllegalStateException e) {
                throw new IllegalStateException("Could not create transformer job from builder: " + tjb + ", ("
                        + e.getMessage() + ")", e);
            }
        }

        final Collection<AnalyzerJob> analyzerJobs = new LinkedList<AnalyzerJob>();
        for (final AnalyzerJobBuilder<?> ajb : _analyzerJobBuilders) {
            try {
                final AnalyzerJob[] analyzerJob = ajb.toAnalyzerJobs(validate, immutabilizer);
                for (AnalyzerJob job : analyzerJob) {
                    analyzerJobs.add(job);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Could not create analyzer job from builder: " + ajb + ", ("
                        + e.getMessage() + ")", e);
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

        final AnalysisJobMetadata metadata = new ImmutableAnalysisJobMetadata(jobName, jobVersion, jobDescription,
                author, createdDate, updatedDate, datastoreName, sourceColumnPaths, sourceColumnTypes, variables,
                properties);
        return metadata;
    }

    /**
     * Creates an analysis job of this {@link AnalysisJobBuilder}.
     * 
     * @return
     * @throws IllegalStateException
     *             if the job is invalidly configured.
     */
    public AnalysisJob toAnalysisJob() throws IllegalStateException {
        return toAnalysisJob(true);
    }

    public InputColumn<?> getSourceColumnByName(String name) {
        if (name != null) {
            for (MetaModelInputColumn inputColumn : _sourceColumns) {
                String qualifiedLabel = inputColumn.getPhysicalColumn().getQualifiedLabel();
                if (name.equalsIgnoreCase(qualifiedLabel)) {
                    return inputColumn;
                }
            }

            for (MetaModelInputColumn inputColumn : _sourceColumns) {
                if (name.equals(inputColumn.getName())) {
                    return inputColumn;
                }
            }

            for (MetaModelInputColumn inputColumn : _sourceColumns) {
                if (name.equalsIgnoreCase(inputColumn.getName())) {
                    return inputColumn;
                }
            }
        }
        return null;
    }

    public TransformerJobBuilder<?> getOriginatingTransformer(InputColumn<?> outputColumn) {
        SourceColumnFinder finder = new SourceColumnFinder();
        finder.addSources(this);
        InputColumnSourceJob source = finder.findInputColumnSource(outputColumn);
        if (source instanceof TransformerJobBuilder) {
            return (TransformerJobBuilder<?>) source;
        }
        return null;
    }

    public Table getOriginatingTable(InputColumn<?> inputColumn) {
        SourceColumnFinder finder = new SourceColumnFinder();
        finder.addSources(this);
        return finder.findOriginatingTable(inputColumn);
    }

    public Table getOriginatingTable(AbstractBeanWithInputColumnsBuilder<?, ?, ?> beanJobBuilder) {
        List<InputColumn<?>> inputColumns = beanJobBuilder.getInputColumns();
        if (inputColumns.isEmpty()) {
            return null;
        } else {
            return getOriginatingTable(inputColumns.get(0));
        }
    }

    public List<AbstractBeanWithInputColumnsBuilder<?, ?, ?>> getAvailableUnfilteredBeans(
            FilterJobBuilder<?, ?> filterJobBuilder) {
        List<AbstractBeanWithInputColumnsBuilder<?, ?, ?>> result = new ArrayList<AbstractBeanWithInputColumnsBuilder<?, ?, ?>>();
        if (filterJobBuilder.isConfigured()) {
            final Table requiredTable = getOriginatingTable(filterJobBuilder);

            for (FilterJobBuilder<?, ?> fjb : _filterJobBuilders) {
                if (fjb != filterJobBuilder) {
                    if (fjb.getComponentRequirement() == null) {
                        Table foundTable = getOriginatingTable(fjb);
                        if (requiredTable == null || requiredTable.equals(foundTable)) {
                            result.add(fjb);
                        }
                    }
                }
            }

            for (TransformerJobBuilder<?> tjb : _transformerJobBuilders) {
                if (tjb.getComponentRequirement() == null) {
                    Table foundTable = getOriginatingTable(tjb);
                    if (requiredTable == null || requiredTable.equals(foundTable)) {
                        result.add(tjb);
                    }
                }
            }

            for (AnalyzerJobBuilder<?> ajb : _analyzerJobBuilders) {
                if (ajb instanceof AnalyzerJobBuilder<?>) {
                    AnalyzerJobBuilder<?> rpajb = (AnalyzerJobBuilder<?>) ajb;
                    if (rpajb.getComponentRequirement() == null) {
                        Table foundTable = getOriginatingTable(rpajb);
                        if (requiredTable == null || requiredTable.equals(foundTable)) {
                            result.add(rpajb);
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
    public void setDefaultRequirement(FilterJobBuilder<?, ?> filterJobBuilder, Enum<?> category) {
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

    public void setDefaultRequirement(final ComponentRequirement defaultRequirement) {
        _defaultRequirement = defaultRequirement;
        if (defaultRequirement != null) {

            final FilterJobBuilder<?, ?> sourceFilterJobBuilder;
            if (defaultRequirement instanceof SimpleComponentRequirement) {
                final FilterOutcome outcome = ((SimpleComponentRequirement) defaultRequirement).getOutcome();
                if (outcome instanceof LazyFilterOutcome) {
                    sourceFilterJobBuilder = ((LazyFilterOutcome) outcome).getFilterJobBuilder();
                } else {
                    logger.warn("Default requirement is not a LazyFilterOutcome. This might cause self-referring requirements.");
                    sourceFilterJobBuilder = null;
                }
            } else {
                logger.warn("Default requirement is not a LazyFilterOutcome. This might cause self-referring requirements.");
                sourceFilterJobBuilder = null;
            }

            // make a set of components that succeeds the requirement
            final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
            sourceColumnFinder.addSources(this);
            final Set<Object> excludedSet = sourceColumnFinder.findAllSourceJobs(defaultRequirement);

            for (AnalyzerJobBuilder<?> ajb : _analyzerJobBuilders) {
                if (ajb instanceof AnalyzerJobBuilder) {
                    final AnalyzerJobBuilder<?> analyzerJobBuilder = (AnalyzerJobBuilder<?>) ajb;
                    final ComponentRequirement requirement = analyzerJobBuilder.getComponentRequirement();
                    if (requirement == null) {
                        analyzerJobBuilder.setComponentRequirement(defaultRequirement);
                    }
                }
            }

            for (TransformerJobBuilder<?> tjb : _transformerJobBuilders) {
                if (tjb.getComponentRequirement() == null && !excludedSet.contains(tjb)) {
                    tjb.setComponentRequirement(defaultRequirement);
                }
            }

            for (FilterJobBuilder<?, ?> fjb : _filterJobBuilders) {
                if (fjb != sourceFilterJobBuilder && fjb.getComponentRequirement() == null
                        && !excludedSet.contains(fjb)) {
                    if (fjb.validateRequirementCandidate(defaultRequirement)) {
                        fjb.setComponentRequirement(defaultRequirement);
                    }
                }
            }
        }
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

    public List<SourceColumnChangeListener> getSourceColumnListeners() {
        return _sourceColumnListeners;
    }

    public List<AnalyzerChangeListener> getAnalyzerChangeListeners() {
        return _analyzerChangeListeners;
    }

    public List<TransformerChangeListener> getTransformerChangeListeners() {
        return _transformerChangeListeners;
    }

    public List<FilterChangeListener> getFilterChangeListeners() {
        return _filterChangeListeners;
    }

    public List<Table> getSourceTables() {
        final List<Table> tables = new ArrayList<Table>();
        final List<MetaModelInputColumn> columns = getSourceColumns();
        for (MetaModelInputColumn column : columns) {
            Table table = column.getPhysicalColumn().getTable();
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
        final List<MetaModelInputColumn> sourceColumns = new ArrayList<MetaModelInputColumn>(_sourceColumns);
        for (MetaModelInputColumn inputColumn : sourceColumns) {
            removeSourceColumn(inputColumn);
        }
        assert _sourceColumns.isEmpty();
    }

    public void removeAllAnalyzers() {
        final List<AnalyzerJobBuilder<?>> analyzers = new ArrayList<AnalyzerJobBuilder<?>>(_analyzerJobBuilders);
        for (AnalyzerJobBuilder<?> ajb : analyzers) {
            removeAnalyzer(ajb);
        }
        assert _analyzerJobBuilders.isEmpty();
    }

    public void removeAllTransformers() {
        final List<TransformerJobBuilder<?>> transformers = new ArrayList<TransformerJobBuilder<?>>(
                _transformerJobBuilders);
        for (TransformerJobBuilder<?> transformerJobBuilder : transformers) {
            removeTransformer(transformerJobBuilder);
        }
        assert _transformerJobBuilders.isEmpty();
    }

    public void removeAllFilters() {
        final List<FilterJobBuilder<?, ?>> filters = new ArrayList<FilterJobBuilder<?, ?>>(_filterJobBuilders);
        for (FilterJobBuilder<?, ?> filterJobBuilder : filters) {
            removeFilter(filterJobBuilder);
        }
        assert _filterJobBuilders.isEmpty();
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
        final AnalysisJobBuilder clone = new AnalysisJobBuilder(_configuration, _datastore, _datastoreConnection,
                metadataClone, _sourceColumns, _defaultRequirement, _transformedColumnIdGenerator,
                _transformerJobBuilders, _filterJobBuilders, _analyzerJobBuilders);
        return clone;
    }

    /**
     * Gets the total number of active components (transformation or analysis)
     * in this job.
     * 
     * @return
     */
    public int getComponentCount() {
        return _filterJobBuilders.size() + _transformerJobBuilders.size() + _analyzerJobBuilders.size();
    }

    /**
     * Gets all component builders contained within this
     * {@link AnalysisJobBuilder}
     * 
     * @return
     */
    public Collection<ComponentBuilder> getComponentBuilders() {
        Collection<ComponentBuilder> result = new ArrayList<>();
        result.addAll(_filterJobBuilders);
        result.addAll(_transformerJobBuilders);
        result.addAll(_analyzerJobBuilders);
        return result;
    }

    /**
     * Gets all available {@link InputColumn}s to map to a particular
     * {@link ComponentBuilder}
     * 
     * @param componentBuilder
     * @return
     */
    public List<InputColumn<?>> getAvailableInputColumns(ComponentBuilder componentBuilder) {
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

        result = CollectionUtils.filter(result, new Predicate<InputColumn<?>>() {
            @Override
            public Boolean eval(InputColumn<?> inputColumn) {
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
                if (sourceComponents.contains(componentBuilder)) {
                    // exclude columns that depend
                    return false;
                }

                return true;
            }
        });

        return result;
    }
}