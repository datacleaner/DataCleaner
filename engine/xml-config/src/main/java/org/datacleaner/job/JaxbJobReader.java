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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.api.Converter;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.beans.transform.PlainSearchReplaceTransformer;
import org.datacleaner.components.fuse.CoalesceUnit;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.SourceColumnMapping;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.data.ConstantInputColumn;
import org.datacleaner.data.ELInputColumn;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.MutableAnalysisJobMetadata;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.jaxb.AnalysisType;
import org.datacleaner.job.jaxb.ColumnType;
import org.datacleaner.job.jaxb.ColumnsType;
import org.datacleaner.job.jaxb.ComponentType;
import org.datacleaner.job.jaxb.ConfiguredPropertiesType;
import org.datacleaner.job.jaxb.ConfiguredPropertiesType.Property;
import org.datacleaner.job.jaxb.DataContextType;
import org.datacleaner.job.jaxb.DescriptorType;
import org.datacleaner.job.jaxb.FilterType;
import org.datacleaner.job.jaxb.InputType;
import org.datacleaner.job.jaxb.JobMetadataType;
import org.datacleaner.job.jaxb.JobType;
import org.datacleaner.job.jaxb.MetadataProperties;
import org.datacleaner.job.jaxb.ObjectFactory;
import org.datacleaner.job.jaxb.OutcomeType;
import org.datacleaner.job.jaxb.OutputDataStreamType;
import org.datacleaner.job.jaxb.OutputType;
import org.datacleaner.job.jaxb.SourceType;
import org.datacleaner.job.jaxb.TransformationType;
import org.datacleaner.job.jaxb.TransformerType;
import org.datacleaner.job.jaxb.VariableType;
import org.datacleaner.job.jaxb.VariablesType;
import org.datacleaner.util.JaxbValidationEventHandler;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.convert.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

public class JaxbJobReader implements JobReader<InputStream> {

    public static final String DATACLEANER_JAXB_VARIABLE_PREFIX = "datacleaner.jaxb.variable.";

    private static final Logger logger = LoggerFactory.getLogger(JaxbJobReader.class);

    private final JAXBContext _jaxbContext;
    private final DataCleanerConfiguration _configuration;

    public JaxbJobReader(final DataCleanerConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        _configuration = configuration;
        try {
            _jaxbContext = JAXBContext
                    .newInstance(ObjectFactory.class.getPackage().getName(), ObjectFactory.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void processRemovedProperties(final ComponentBuilder builder, final StringConverter stringConverter,
            final ComponentDescriptor<?> descriptor, final Map<String, String> removedProperties) {
        if (descriptor.getComponentClass() == PlainSearchReplaceTransformer.class) {
            PlainSearchReplaceTransformer
                    .processRemovedProperties(builder, stringConverter, descriptor, removedProperties);
        }
    }

    private static boolean isRemovedProperty(final ComponentDescriptor<?> descriptor, final String name) {
        return PlainSearchReplaceTransformer.isRemovedProperty(descriptor, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnalysisJob read(final InputStream inputStream)
            throws NoSuchDatastoreException, NoSuchColumnException, NoSuchComponentException,
            ComponentConfigurationException, IllegalStateException {
        try (AnalysisJobBuilder ajb = create(inputStream)) {
            return ajb.toAnalysisJob();
        }
    }

    @Override
    public AnalysisJob read(final InputStream inputStream, final SourceColumnMapping sourceColumnMapping) {
        try (AnalysisJobBuilder ajb = create(inputStream, sourceColumnMapping)) {
            return ajb.toAnalysisJob();
        }
    }

    public AnalysisJobMetadata readMetadata(final FileObject file) {
        InputStream inputStream = null;
        try {
            inputStream = file.getContent().getInputStream();
            return readMetadata(inputStream);
        } catch (final FileSystemException e) {
            throw new IllegalArgumentException(e);
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }

    public AnalysisJobMetadata readMetadata(final File file) {
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            return readMetadata(inputStream);
        } catch (final FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }

    @Override
    public AnalysisJobMetadata readMetadata(final InputStream inputStream) {
        final JobType job = unmarshallJob(inputStream);
        return readMetadata(job);
    }

    public AnalysisJobMetadata readMetadata(final JobType job) {
        final String datastoreName = job.getSource().getDataContext().getRef();
        final List<String> sourceColumnPaths = getSourceColumnPaths(job);
        final List<org.apache.metamodel.schema.ColumnType> sourceColumnTypes = getSourceColumnTypes(job);
        final Map<String, String> variables = getVariables(job);

        final String jobName;
        final String jobVersion;
        final String jobDescription;
        final String author;
        final Date createdDate;
        final Date updatedDate;
        final Map<String, String> metadataProperties;

        final JobMetadataType metadata = job.getJobMetadata();
        if (metadata == null) {
            jobName = null;
            jobVersion = null;
            jobDescription = null;
            author = null;
            createdDate = null;
            updatedDate = null;
            metadataProperties = Collections.emptyMap();
        } else {
            jobName = metadata.getJobName();
            jobVersion = metadata.getJobVersion();
            jobDescription = metadata.getJobDescription();
            author = metadata.getAuthor();
            metadataProperties = getMetadataProperties(metadata);

            final XMLGregorianCalendar createdDateCal = metadata.getCreatedDate();

            if (createdDateCal == null) {
                createdDate = null;
            } else {
                createdDate = createdDateCal.toGregorianCalendar().getTime();
            }

            final XMLGregorianCalendar updatedDateCal = metadata.getUpdatedDate();

            if (updatedDateCal == null) {
                updatedDate = null;
            } else {
                updatedDate = updatedDateCal.toGregorianCalendar().getTime();
            }
        }

        return new ImmutableAnalysisJobMetadata(jobName, jobVersion, jobDescription, author, createdDate, updatedDate,
                datastoreName, sourceColumnPaths, sourceColumnTypes, variables, metadataProperties);
    }

    private Map<String, String> getMetadataProperties(final JobMetadataType metadata) {
        final MetadataProperties properties = metadata.getMetadataProperties();

        if (properties == null) {
            return Collections.emptyMap();
        }

        final Map<String, String> metadataProperties = new HashMap<>();
        final List<org.datacleaner.job.jaxb.MetadataProperties.Property> property = properties.getProperty();

        for (int i = 0; i < property.size(); i++) {
            final String name = property.get(i).getName();
            final String value = property.get(i).getValue();
            metadataProperties.put(name, value);
        }

        
        return metadataProperties;
    }

    public Map<String, String> getVariables(final JobType job) {
        final Map<String, String> result = new HashMap<>();

        final VariablesType variablesType = job.getSource().getVariables();
        if (variablesType != null) {
            final List<VariableType> variables = variablesType.getVariable();
            for (final VariableType variableType : variables) {
                final String id = variableType.getId();
                final String value = variableType.getValue();
                result.put(id, value);
            }
        }

        return result;
    }

    public List<String> getSourceColumnPaths(final JobType job) {
        final List<String> paths;

        final ColumnsType columnsType = job.getSource().getColumns();
        if (columnsType != null) {
            final List<ColumnType> columns = columnsType.getColumn();
            paths = new ArrayList<>(columns.size());
            for (final ColumnType columnType : columns) {
                final String path = columnType.getPath();
                paths.add(path);
            }
        } else {
            paths = Collections.emptyList();
        }
        return paths;
    }

    private List<org.apache.metamodel.schema.ColumnType> getSourceColumnTypes(final JobType job) {
        final List<org.apache.metamodel.schema.ColumnType> types;

        final ColumnsType columnsType = job.getSource().getColumns();
        if (columnsType != null) {
            final List<ColumnType> columns = columnsType.getColumn();
            types = new ArrayList<>(columns.size());
            for (final ColumnType columnType : columns) {
                final String typeName = columnType.getType();
                if (StringUtils.isNullOrEmpty(typeName)) {
                    types.add(null);
                } else {
                    try {
                        final org.apache.metamodel.schema.ColumnType type =
                                org.apache.metamodel.schema.ColumnTypeImpl.valueOf(typeName);
                        types.add(type);
                    } catch (final IllegalArgumentException e) {
                        // type literal was not a valid ColumnType
                        logger.warn("Unrecognized column type: {}", typeName);
                        types.add(null);
                    }
                }
            }
        } else {
            types = Collections.emptyList();
        }
        return types;
    }

    public AnalysisJobBuilder create(final FileObject file) {
        InputStream inputStream = null;
        try {
            inputStream = file.getContent().getInputStream();
            return create(inputStream);
        } catch (final FileSystemException e) {
            throw new IllegalArgumentException(e);
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }

    public AnalysisJobBuilder create(final File file) {
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            return create(inputStream);
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }

    public AnalysisJobBuilder create(final InputStream inputStream) throws NoSuchDatastoreException {
        return create(unmarshallJob(inputStream), null, null);
    }

    public AnalysisJobBuilder create(final InputStream inputStream, final SourceColumnMapping sourceColumnMapping)
            throws NoSuchDatastoreException {
        return create(inputStream, sourceColumnMapping, null);
    }

    public AnalysisJobBuilder create(final InputStream inputStream, final SourceColumnMapping sourceColumnMapping,
            final Map<String, String> variableOverrides) throws NoSuchDatastoreException {
        return create(unmarshallJob(inputStream), sourceColumnMapping, variableOverrides);
    }

    public AnalysisJobBuilder create(final InputStream inputStream, final Map<String, String> variableOverrides)
            throws NoSuchDatastoreException {
        return create(unmarshallJob(inputStream), null, variableOverrides);
    }

    public AnalysisJobBuilder create(final InputStream inputStream, final Map<String, String> variableOverrides,
            final Datastore datastore) {
        final JobType jobType = unmarshallJob(inputStream);
        final SourceColumnMapping sourceColumnMapping = new SourceColumnMapping(readMetadata(jobType));
        sourceColumnMapping.autoMap(datastore);
        return create(jobType, sourceColumnMapping, variableOverrides);
    }

    private JobType unmarshallJob(final InputStream inputStream) {
        try {
            final Unmarshaller unmarshaller = _jaxbContext.createUnmarshaller();

            unmarshaller.setEventHandler(new JaxbValidationEventHandler());
            return (JobType) unmarshaller.unmarshal(inputStream);
        } catch (final JAXBException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public AnalysisJobBuilder create(final JobType job) {
        return create(job, null, null);
    }

    public AnalysisJobBuilder create(final JobType job, final SourceColumnMapping sourceColumnMapping,
            final Map<String, String> variableOverrides) throws NoSuchDatastoreException {
        if (job == null) {
            throw new IllegalArgumentException("Job cannot be null");
        }

        if (sourceColumnMapping != null && !sourceColumnMapping.isSatisfied()) {
            throw new IllegalArgumentException("Source column mapping is not satisfied!");
        }

        final Map<String, String> variables = getVariables(job);
        if (variableOverrides != null) {
            final Set<Entry<String, String>> entrySet = variableOverrides.entrySet();
            for (final Entry<String, String> entry : entrySet) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                final String originalValue = variables.put(key, value);
                if (originalValue == null) {
                    logger.debug("Setting variable: {}={}", key, value);
                } else {
                    logger.info("Overriding variable: {}={} (original value was {})", key, value, originalValue);
                }
            }
        }

        final JobMetadataType metadata = job.getJobMetadata();
        if (metadata != null) {
            logger.info("Job name: {}", metadata.getJobName());
            logger.info("Job version: {}", metadata.getJobVersion());
            logger.info("Job description: {}", metadata.getJobDescription());
            logger.info("Author: {}", metadata.getAuthor());
            logger.info("Created date: {}", metadata.getCreatedDate());
            logger.info("Updated date: {}", metadata.getUpdatedDate());
            logger.info("Job metadata properties: {}", getMetadataProperties(metadata));
        }

        final AnalysisJobBuilder builder = new AnalysisJobBuilder(_configuration);

        try {
            return create(job, sourceColumnMapping, metadata, variables, builder);
        } catch (final RuntimeException e) {
            FileHelper.safeClose(builder);
            throw e;
        }
    }

    private AnalysisJobBuilder create(final JobType job, SourceColumnMapping sourceColumnMapping,
            final JobMetadataType metadata, final Map<String, String> variables,
            final AnalysisJobBuilder analysisJobBuilder) {

        final Datastore datastore;
        final DatastoreConnection datastoreConnection;
        final SourceType source = job.getSource();

        if (sourceColumnMapping == null) {
            // use automatic mapping if no explicit mapping is supplied
            final DataContextType dataContext = source.getDataContext();
            final String ref = dataContext.getRef();
            if (StringUtils.isNullOrEmpty(ref)) {
                throw new IllegalStateException("Datastore ref cannot be null");
            }

            datastore = _configuration.getDatastoreCatalog().getDatastore(ref);
            if (datastore == null) {
                throw new NoSuchDatastoreException(ref);
            }
            datastoreConnection = datastore.openConnection();

            final List<String> sourceColumnPaths = getSourceColumnPaths(job);
            sourceColumnMapping = new SourceColumnMapping(sourceColumnPaths);
            sourceColumnMapping.autoMap(datastore);
        } else {
            datastore = sourceColumnMapping.getDatastore();
            datastoreConnection = datastore.openConnection();
        }

        try {
            analysisJobBuilder.setDatastore(datastore);
            if (metadata != null) {
                final ImmutableAnalysisJobMetadata immutableAnalysisJobMetadata =
                        new ImmutableAnalysisJobMetadata(metadata.getJobName(), metadata.getJobVersion(),
                                metadata.getJobDescription(), metadata.getAuthor(),
                                convertToDate(metadata.getCreatedDate()), convertToDate(metadata.getUpdatedDate()),
                                datastore.getName(), getSourceColumnPaths(job), getSourceColumnTypes(job), variables,
                                getMetadataProperties(metadata));
                analysisJobBuilder.setAnalysisJobMetadata(immutableAnalysisJobMetadata);
            } else {
                if (variables.size() > 0) {
                    final MutableAnalysisJobMetadata mutableAnalysisJobMetadata = new MutableAnalysisJobMetadata();
                    mutableAnalysisJobMetadata.getVariables().putAll(variables);
                    analysisJobBuilder.setAnalysisJobMetadata(mutableAnalysisJobMetadata);
                }
            }

            // map column id's to input columns
            final Map<String, InputColumn<?>> inputColumns =
                    readSourceColumns(sourceColumnMapping, analysisJobBuilder, source);

            configureComponents(job, variables, analysisJobBuilder, inputColumns, sourceColumnMapping);

            return analysisJobBuilder;
        } finally {
            datastoreConnection.close();
        }
    }

    private void configureComponents(final JobType job, final Map<String, String> variables,
            final AnalysisJobBuilder analysisJobBuilder, final Map<String, InputColumn<?>> inputColumns,
            final SourceColumnMapping sourceColumnMapping) {
        final StringConverter stringConverter = createStringConverter(analysisJobBuilder);
        final DescriptorProvider descriptorProvider = _configuration.getEnvironment().getDescriptorProvider();

        final Map<ComponentType, ComponentBuilder> componentBuilders = new HashMap<>();

        final List<ColumnType> columnsTypes = job.getSource().getColumns().getColumn();
        // iterate to create all the initial component builders without any
        // wiring
        final List<ComponentType> allComponentTypes = getAllComponentTypes(job);
        for (final ComponentType componentType : allComponentTypes) {
            final ComponentBuilder componentBuilder =
                    createComponentBuilder(analysisJobBuilder, descriptorProvider, componentType);

            initializeComponentBuilder(variables, stringConverter, componentBuilders, componentType, componentBuilder,
                    inputColumns, columnsTypes);
        }

        wireInputColumns(inputColumns, componentBuilders);

        wireRequirements(componentBuilders);

        wireOutputDataStreams(componentBuilders, sourceColumnMapping);
    }

    private void wireOutputDataStreams(final Map<ComponentType, ComponentBuilder> componentBuilders,
            final SourceColumnMapping sourceColumnMapping) {
        for (final Map.Entry<ComponentType, ComponentBuilder> entry : componentBuilders.entrySet()) {
            final ComponentType componentType = entry.getKey();
            final ComponentBuilder componentBuilder = entry.getValue();
            for (final OutputDataStreamType outputDataStreamType : componentType.getOutputDataStream()) {
                final String name = outputDataStreamType.getName();
                final OutputDataStream outputDataStream = componentBuilder.getOutputDataStream(name);
                final AnalysisJobBuilder outputDataStreamJobBuilder =
                        componentBuilder.getOutputDataStreamJobBuilder(outputDataStream);
                final JobType job = outputDataStreamType.getJob();

                final List<ColumnType> sourceColumnTypes = job.getSource().getColumns().getColumn();

                final List<MetaModelInputColumn> sourceColumns = outputDataStreamJobBuilder.getSourceColumns();

                // map column id's to input columns
                final Map<String, InputColumn<?>> inputColumns = new HashMap<>();

                for (int i = 0; i < sourceColumnTypes.size(); i++) {
                    final ColumnType sourceColumnPath = sourceColumnTypes.get(i);
                    final Column findSourceColumn = sourceColumnMapping.getColumn(sourceColumnPath.getPath());

                    final String outputStreamColumnPathName;
                    // If there is a mapping for the column in the source
                    // Mapping we set the new path. The 'findSourceColumn' can
                    // be null because it can be a transformer column
                    // such as "Concat of Lastname and Firstname"
                    if (findSourceColumn != null) {
                        outputStreamColumnPathName = getOutputStreamColumnPath(findSourceColumn.getName(),
                                componentType, componentBuilder, i);
                    } else {
                        // keep the path name
                        outputStreamColumnPathName = sourceColumnPath.getPath();
                    }

                    // Set the new path of the column
                    sourceColumnPath.setPath(outputStreamColumnPathName);

                    sourceColumns.stream().filter(inputColumn -> inputColumn.getName().equals(
                            outputStreamColumnPathName)).forEach(inputColumn -> inputColumns.put(sourceColumnPath
                                    .getId(), inputColumn));
                }

                configureComponents(job, getVariables(job), outputDataStreamJobBuilder, inputColumns, sourceColumnMapping);
            }
        }
    }

    private String getOutputStreamColumnPath(final String suggestedPath, final ComponentType componentType,
            final ComponentBuilder componentBuilder, final int sourceColumnIndex) {
        // Stupid special case for FuseStreamsComponent
        if (componentType.getDescriptor().getRef().equals("Union")) {
            final ConfiguredPropertyDescriptor configuredPropertyDescriptor =
                    componentBuilder.getDescriptor().getConfiguredProperty("Units");
            final CoalesceUnit[] units =
                    (CoalesceUnit[]) componentBuilder.getConfiguredProperty(configuredPropertyDescriptor);
            final CoalesceUnit unit = units[sourceColumnIndex];
            return unit.getSuggestedOutputColumnName();
        } else {
            return suggestedPath;
        }
    }

    private void wireRequirements(final Map<ComponentType, ComponentBuilder> componentBuilders) {
        final Map<String, FilterOutcome> outcomeMapping = new HashMap<>();

        // iterate initialize collect all outcomes by their IDs
        for (final ComponentType componentType : componentBuilders.keySet()) {
            if (componentType instanceof FilterType) {
                final FilterType filterType = (FilterType) componentType;
                final FilterComponentBuilder<?, ?> filterBuilder =
                        (FilterComponentBuilder<?, ?>) componentBuilders.get(componentType);

                final List<OutcomeType> outcomeTypes = filterType.getOutcome();
                for (final OutcomeType outcomeType : outcomeTypes) {
                    final String categoryName = outcomeType.getCategory();
                    final Enum<?> category = filterBuilder.getDescriptor().getOutcomeCategoryByName(categoryName);
                    if (category == null) {
                        throw new ComponentConfigurationException(
                                "No such outcome category name: " + categoryName + " (in " + filterBuilder
                                        .getDescriptor().getDisplayName() + ")");
                    }

                    final String id = outcomeType.getId();
                    if (StringUtils.isNullOrEmpty(id)) {
                        throw new IllegalStateException("Outcome id cannot be null");
                    }
                    if (outcomeMapping.containsKey(id)) {
                        throw new ComponentConfigurationException("Outcome id '" + id + "' is not unique");
                    }
                    outcomeMapping.put(id, filterBuilder.getFilterOutcome(category));
                }
            }
        }

        // iterate again to set up filter outcome dependencies
        for (final ComponentType componentType : componentBuilders.keySet()) {
            wireRequirement(outcomeMapping, componentBuilders, componentType);
        }
    }

    private ComponentBuilder createComponentBuilder(final AnalysisJobBuilder analysisJobBuilder,
            final DescriptorProvider descriptorProvider, final ComponentType componentType) {
        final String ref = componentType.getDescriptor().getRef();
        if (StringUtils.isNullOrEmpty(ref)) {
            throw new IllegalStateException(
                    componentType.getClass().getSimpleName() + " descriptor ref cannot be null");
        }

        final ComponentDescriptor<?> descriptor = descriptorProvider.getComponentDescriptorByDisplayName(ref);
        if (descriptor == null) {
            throw new NoSuchComponentException(componentType.getClass(), ref);
        }

        return analysisJobBuilder.addComponent(descriptor);
    }

    private List<ComponentType> getAllComponentTypes(final JobType job) {
        final List<ComponentType> result = new ArrayList<>();
        final TransformationType transformation = job.getTransformation();
        if (transformation != null) {
            result.addAll(transformation.getTransformerOrFilter());
        }
        final AnalysisType analysis = job.getAnalysis();
        if (analysis != null) {
            result.addAll(analysis.getAnalyzer());
        }
        return result;
    }

    /**
     * Wires input columns from either source or transformer output. This
     * process is an iteration to find the next consumer with
     * "satisfied column requirements".
     *
     * @param inputColumns
     * @param componentBuilders
     */
    private void wireInputColumns(final Map<String, InputColumn<?>> inputColumns,
            final Map<ComponentType, ComponentBuilder> componentBuilders) {
        // iterate again to set up column dependencies (one at a time -
        // whichever is possible based on the configuration of the column
        // sources (transformers))
        final List<ComponentType> unconfiguredComponentKeys = new LinkedList<>(componentBuilders.keySet());
        while (!unconfiguredComponentKeys.isEmpty()) {
            boolean progress = false;
            for (final Iterator<ComponentType> it = unconfiguredComponentKeys.iterator(); it.hasNext(); ) {
                boolean configurable = true;

                final ComponentType unconfiguredTransformerKey = it.next();
                final List<InputType> input = unconfiguredTransformerKey.getInput();
                for (final InputType inputType : input) {
                    final String ref = inputType.getRef();
                    if (StringUtils.isNullOrEmpty(ref)) {
                        final String value = inputType.getValue();
                        if (value == null) {
                            throw new IllegalStateException("Component input column ref & value cannot be null");
                        }
                    } else if (!inputColumns.containsKey(ref)) {
                        configurable = false;
                        break;
                    }
                }

                if (configurable) {
                    progress = true;
                    final ComponentBuilder componentBuilder = componentBuilders.get(unconfiguredTransformerKey);

                    applyInputColumns(input, inputColumns, componentBuilder);

                    if (componentBuilder instanceof TransformerComponentBuilder) {
                        final TransformerComponentBuilder<?> transformerBuilder =
                                (TransformerComponentBuilder<?>) componentBuilder;
                        final TransformerType transformerType = (TransformerType) unconfiguredTransformerKey;

                        final List<MutableInputColumn<?>> outputColumns = transformerBuilder.getOutputColumns();
                        final List<OutputType> output = transformerType.getOutput();

                        if (outputColumns.size() < output.size()) {
                            final String message =
                                    "Expected " + outputColumns.size() + " output column(s), but found " + output.size()
                                            + " (" + transformerBuilder + ")";
                            if (outputColumns.isEmpty()) {
                                // typically empty output columns is due to
                                // a component not being configured, we'll
                                // attach the configuration exception as a
                                // cause.
                                try {
                                    transformerBuilder.isConfigured(true);
                                } catch (final Exception e) {
                                    throw new ComponentConfigurationException(message, e);
                                }
                            }
                            throw new ComponentConfigurationException(message);
                        }

                        for (int i = 0; i < output.size(); i++) {
                            final OutputType o1 = output.get(i);
                            final MutableInputColumn<?> o2 = outputColumns.get(i);
                            final String name = o1.getName();
                            if (!StringUtils.isNullOrEmpty(name)) {
                                o2.setName(name);
                            }
                            final Boolean hidden = o1.isHidden();
                            if (hidden != null && hidden.booleanValue()) {
                                o2.setHidden(true);
                            }
                            final String id = o1.getId();
                            if (StringUtils.isNullOrEmpty(id)) {
                                throw new IllegalStateException("Transformer output column id cannot be null");
                            }
                            registerInputColumn(inputColumns, id, o2);
                        }
                    }

                    // remove this component from the "unconfigured" set
                    it.remove();
                }
            }

            if (!progress) {
                // no progress was made in a complete iteration - no satisfied
                // requirements where found. Time to produce an error message...
                final StringBuilder sb = new StringBuilder();
                for (final ComponentType transformerType : unconfiguredComponentKeys) {
                    if (sb.length() != 0) {
                        sb.append(", ");
                    }
                    final DescriptorType descriptor = transformerType.getDescriptor();
                    sb.append(descriptor.getRef());
                    sb.append("(input: ");

                    final List<InputType> input = transformerType.getInput();
                    int i = 0;
                    for (final InputType inputType : input) {
                        if (i != 0) {
                            sb.append(", ");
                        }
                        final String ref = inputType.getRef();
                        if (StringUtils.isNullOrEmpty(ref)) {
                            sb.append("value=" + inputType.getValue());
                        } else {
                            sb.append("ref=" + ref);
                        }
                        i++;
                    }
                    sb.append(")");
                }
                throw new ComponentConfigurationException(
                        "Could not connect column dependencies for components: " + sb.toString());
            }
        }
    }

    private void initializeComponentBuilder(final Map<String, String> variables, final StringConverter stringConverter,
            final Map<ComponentType, ComponentBuilder> componentBuilders, final ComponentType componentType,
            final ComponentBuilder componentBuilder, final Map<String, InputColumn<?>> inputColumns,
            final List<ColumnType> columnsTypes) {
        // shared setting of properties (except for input columns)
        componentBuilder.setName(componentType.getName());

        applyProperties(componentBuilder, componentType.getProperties(), componentType.getMetadataProperties(),
                stringConverter, variables, inputColumns, columnsTypes);

        componentBuilders.put(componentType, componentBuilder);
    }

    private void wireRequirement(final Map<String, FilterOutcome> outcomeMapping,
            final Map<ComponentType, ComponentBuilder> componentBuilders, final ComponentType componentType) {
        final String ref = componentType.getRequires();
        if (ref != null) {
            final ComponentBuilder builder = componentBuilders.get(componentType);
            final ComponentRequirement requirement = getRequirement(ref, outcomeMapping);
            builder.setComponentRequirement(requirement);
        }
    }

    /**
     * Reads the source element of the job to extract a map of column IDs and
     * related source {@link InputColumn}s.
     *
     * @param sourceColumnMapping
     * @param analysisJobBuilder
     * @param source
     * @return
     */
    private Map<String, InputColumn<?>> readSourceColumns(final SourceColumnMapping sourceColumnMapping,
            final AnalysisJobBuilder analysisJobBuilder, final SourceType source) {
        final Map<String, InputColumn<?>> inputColumns = new HashMap<>();
        final ColumnsType columnsType = source.getColumns();
        if (columnsType != null) {
            final List<ColumnType> columns = columnsType.getColumn();
            for (final ColumnType column : columns) {
                final String path = column.getPath();
                if (StringUtils.isNullOrEmpty(path)) {
                    throw new IllegalStateException("Column path cannot be null");
                }
                final Column physicalColumn = sourceColumnMapping.getColumn(path);
                if (physicalColumn == null) {
                    logger.error("Column {} not found in {}", path, sourceColumnMapping);
                    throw new NoSuchColumnException(path);
                }

                final MetaModelInputColumn inputColumn = new MetaModelInputColumn(physicalColumn);
                final String id = column.getId();
                if (StringUtils.isNullOrEmpty(id)) {
                    throw new IllegalStateException("Source column id cannot be null");
                }

                final String expectedType = column.getType();
                if (expectedType != null) {
                    final org.apache.metamodel.schema.ColumnType actualType = physicalColumn.getType();
                    if (actualType != null && !expectedType.equals(actualType.toString())) {
                        logger.warn("Column '{}' had type '{}', but '{}' was expected.",
                                new Object[] { path, actualType, expectedType });
                    }
                }

                registerInputColumn(inputColumns, id, inputColumn);
                analysisJobBuilder.addSourceColumn(inputColumn);
            }
        }
        return inputColumns;
    }

    private Date convertToDate(final XMLGregorianCalendar calendar) {
        if (calendar != null) {
            return calendar.toGregorianCalendar().getTime();
        }
        return null;
    }

    private ComponentRequirement getRequirement(final String ref, final Map<String, FilterOutcome> outcomeMapping) {
        if (AnyComponentRequirement.KEYWORD.equals(ref)) {
            return AnyComponentRequirement.get();
        }

        // check for simple component requirements
        {
            final FilterOutcome filterOutcome = outcomeMapping.get(ref);
            if (filterOutcome != null) {
                return new SimpleComponentRequirement(filterOutcome);
            }
        }

        // check for compound component requirements
        final List<String> tokens = Lists.newArrayList(Splitter.on(" OR ").omitEmptyStrings().trimResults().split(ref));
        if (tokens.size() > 1) {
            final List<FilterOutcome> list = new ArrayList<>(tokens.size());
            for (final String token : tokens) {
                final FilterOutcome filterOutcome = outcomeMapping.get(token);
                if (filterOutcome == null) {
                    throw new ComponentConfigurationException(
                            "Could not resolve outcome '" + token + "' in requirement: " + ref);
                }
                list.add(filterOutcome);
            }
            return new CompoundComponentRequirement(list);
        }

        throw new ComponentConfigurationException("Could not resolve requirement: " + ref);
    }

    private void applyInputColumns(final List<InputType> input, final Map<String, InputColumn<?>> inputColumns,
            final ComponentBuilder componentBuilder) {
        // build a map of inputs first so that we can set the
        // input in one go
        final ListMultimap<ConfiguredPropertyDescriptor, InputColumn<?>> inputMap = ArrayListMultimap.create();

        for (final InputType inputType : input) {
            final String name = inputType.getName();
            final String ref = inputType.getRef();
            final InputColumn<?> inputColumn;
            if (StringUtils.isNullOrEmpty(ref)) {
                inputColumn = createExpressionBasedInputColumn(inputType);
            } else {
                inputColumn = inputColumns.get(ref);
            }
            if (StringUtils.isNullOrEmpty(name)) {
                final ConfiguredPropertyDescriptor propertyDescriptor =
                        componentBuilder.getDefaultConfiguredPropertyForInput();
                inputMap.put(propertyDescriptor, inputColumn);
            } else {
                final ConfiguredPropertyDescriptor propertyDescriptor =
                        componentBuilder.getDescriptor().getConfiguredProperty(name);
                inputMap.put(propertyDescriptor, inputColumn);
            }
        }

        final Set<ConfiguredPropertyDescriptor> keys = inputMap.keySet();
        for (final ConfiguredPropertyDescriptor propertyDescriptor : keys) {
            final List<InputColumn<?>> inputColumnsForProperty = inputMap.get(propertyDescriptor);
            componentBuilder.addInputColumns(inputColumnsForProperty, propertyDescriptor);
        }
    }

    private StringConverter createStringConverter(final AnalysisJobBuilder analysisJobBuilder) {
        final AnalysisJob job = analysisJobBuilder.toAnalysisJob(false);
        return new StringConverter(_configuration, job);
    }

    private InputColumn<?> createExpressionBasedInputColumn(final InputType inputType) {
        final String expression = inputType.getValue();
        if (expression == null) {
            throw new IllegalStateException("Input ref & value cannot both be null");
        }
        if (expression.indexOf("#{") == -1) {
            return new ConstantInputColumn(expression);
        } else {
            return new ELInputColumn(expression);
        }
    }

    private void registerInputColumn(final Map<String, InputColumn<?>> inputColumns, final String id,
            final InputColumn<?> inputColumn) {
        if (StringUtils.isNullOrEmpty(id)) {
            throw new IllegalStateException("Column id cannot be null");
        }
        if (inputColumns.containsKey(id)) {
            throw new ComponentConfigurationException("Column id is not unique: " + id);
        }
        inputColumns.put(id, inputColumn);
    }

    private void applyProperties(final ComponentBuilder builder,
            final ConfiguredPropertiesType configuredPropertiesType, final MetadataProperties metadataPropertiesType,
            final StringConverter stringConverter, final Map<String, String> variables,
            final Map<String, InputColumn<?>> mappingInputColumns, final List<ColumnType> columnsTypes) {
        if (configuredPropertiesType != null) {
            final List<Property> properties = configuredPropertiesType.getProperty();
            final ComponentDescriptor<?> descriptor = builder.getDescriptor();

            final Map<String, String> removedProperties = new HashMap<>();

            for (final Property property : properties) {
                final String name = property.getName();

                if (isRemovedProperty(descriptor, name)) {
                    removedProperties.put(name, getValue(property));
                } else {
                    final ConfiguredPropertyDescriptor configuredProperty = descriptor.getConfiguredProperty(name);

                    if (configuredProperty == null) {
                        throw new ComponentConfigurationException("No such property: " + name);
                    }

                    String stringValue = getValue(property);
                    String templateValue = null;
                    if (stringValue == null) {
                        final String variableRef = property.getRef();

                        if (variableRef == null) {
                            templateValue = property.getTemplate();
                            if (templateValue != null) {
                                for (final Entry<String, String> variable : variables.entrySet()) {
                                    templateValue = templateValue.replace("${" + variable.getKey() + "}", variable.getValue());
                                }
                                stringValue = templateValue;
                            } else {
                                throw new IllegalStateException("Neither value nor ref was specified for property: "
                                        + name);
                            }
                        } else {
                            stringValue = variables.get(variableRef);
                        }
                        if (stringValue == null) {
                            throw new ComponentConfigurationException("No such variable: " + variableRef);
                        }
                        if (variableRef != null && templateValue == null) {
                            builder.getMetadataProperties()
                                    .put(DATACLEANER_JAXB_VARIABLE_PREFIX + configuredProperty.getName(), variableRef);
                        }
                    }

                    final Converter<?> customConverter = configuredProperty.createCustomConverter();
                    final Object value =
                            stringConverter.deserialize(stringValue, configuredProperty.getType(), customConverter);

                    if (value instanceof CoalesceUnit[]) {
                        /*
                         * This part of the code refers to the situation when we
                         * open the job as a template where we need to replace
                         * the name of the columns with the mapped ones
                         */
                        final CoalesceUnit[] units = (CoalesceUnit[]) value;

                        final ArrayList<CoalesceUnit> newUnitsList = new ArrayList<>();
                        final Set<Entry<String, InputColumn<?>>> mappingColumnsSet = mappingInputColumns.entrySet();
                        for (int i = 0; i < units.length; i++) {
                            final String[] oldInputColumns = units[i].getInputColumnNames();
                            final ArrayList<String> newInputColumns = new ArrayList<>();
                            for (int j = 0; j < oldInputColumns.length; j++) {
                                /*
                                 * Eg. <column id="col_given_name"
                                 * path="given_name" type="STRING"/> The path is
                                 * found in the name of the column:
                                 * datastores.customers.csv.given_name
                                 */
                                final String oldColumn = oldInputColumns[j];
                                boolean found = false;
                                for (final Entry<String, InputColumn<?>> entry : mappingColumnsSet) {
                                    final String column_id = entry.getKey();
                                    final String path = getPath(columnsTypes, column_id);
                                    if (oldColumn.contains('.' + path)) {
                                        // add the mapped column.
                                        final InputColumn<?> entryValue = entry.getValue();
                                        if (entryValue.isPhysicalColumn()) {
                                            newInputColumns.add(entryValue.getPhysicalColumn().getQualifiedLabel());
                                        } else {
                                            newInputColumns.add(entryValue.getName());
                                        }
                                        found = true;
                                        break;
                                    }
                                }
                                // If in the coalesce units we have inputcolumns
                                // names
                                // and not the physical names then we keep
                                // the original value. Eg value="[&amp;#91;EQ
                                // name&amp;#44;NEQ name&amp;#93;]"/>
                                if (!found) {
                                    newInputColumns.add(oldInputColumns[j]);
                                }
                            }

                            // create new coalesce unit with the mapped columns.
                            if (!newInputColumns.isEmpty()) {
                                final CoalesceUnit newCoalesceUnit =
                                        new CoalesceUnit(newInputColumns.toArray(new String[0]));
                                newUnitsList.add(newCoalesceUnit);
                            }
                        }

                        if (!newUnitsList.isEmpty()) {
                            final CoalesceUnit[] newUnits = newUnitsList.toArray(new CoalesceUnit[0]);
                            builder.setConfiguredProperty(configuredProperty, newUnits);
                        } else {
                            builder.setConfiguredProperty(configuredProperty, value);
                        }
                    } else {
                        builder.setConfiguredProperty(configuredProperty, value);
                    }
                    logger.debug("Setting property '{}' to {}", name, value);
                }
            }

            processRemovedProperties(builder, stringConverter, descriptor, removedProperties);
        }
        if (metadataPropertiesType != null) {
            final List<org.datacleaner.job.jaxb.MetadataProperties.Property> propertyList =
                    metadataPropertiesType.getProperty();
            for (final org.datacleaner.job.jaxb.MetadataProperties.Property property : propertyList) {
                final String name = property.getName();
                final String value = property.getValue();
                builder.setMetadataProperty(name, value);
            }
        }
    }

    private String getPath(final List<ColumnType> columnsTypes, final String columnId) {
        for (final ColumnType column : columnsTypes) {
            if (columnId.equals(column.getId())) {
                return column.getPath();
            }
        }
        return null;
    }

    private String getValue(final Property property) {
        String value = property.getValue();
        if (StringUtils.isNullOrEmpty(value)) {
            final String valueAttribute = property.getValueAttribute();
            if (value != null) {
                value = valueAttribute;
            }
        }
        return value;
    }
}
