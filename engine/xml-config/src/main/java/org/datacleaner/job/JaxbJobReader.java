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
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Converter;
import org.datacleaner.api.Filter;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Transformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.SourceColumnMapping;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.data.ConstantInputColumn;
import org.datacleaner.data.ELInputColumn;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.FilterDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.jaxb.AnalysisType;
import org.datacleaner.job.jaxb.AnalyzerType;
import org.datacleaner.job.jaxb.ColumnType;
import org.datacleaner.job.jaxb.ColumnsType;
import org.datacleaner.job.jaxb.ConfiguredPropertiesType;
import org.datacleaner.job.jaxb.ConfiguredPropertiesType.Property;
import org.datacleaner.job.jaxb.DataContextType;
import org.datacleaner.job.jaxb.FilterType;
import org.datacleaner.job.jaxb.InputType;
import org.datacleaner.job.jaxb.JobType;
import org.datacleaner.job.jaxb.JobMetadataType;
import org.datacleaner.job.jaxb.MetadataProperties;
import org.datacleaner.job.jaxb.ObjectFactory;
import org.datacleaner.job.jaxb.OutcomeType;
import org.datacleaner.job.jaxb.OutputType;
import org.datacleaner.job.jaxb.SourceType;
import org.datacleaner.job.jaxb.TransformationType;
import org.datacleaner.job.jaxb.TransformerDescriptorType;
import org.datacleaner.job.jaxb.TransformerType;
import org.datacleaner.job.jaxb.VariableType;
import org.datacleaner.job.jaxb.VariablesType;
import org.datacleaner.util.JaxbValidationEventHandler;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.convert.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;

public class JaxbJobReader implements JobReader<InputStream> {

    private static final Logger logger = LoggerFactory.getLogger(JaxbJobReader.class);

    private final JAXBContext _jaxbContext;
    private final DataCleanerConfiguration _configuration;

    public JaxbJobReader(DataCleanerConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        _configuration = configuration;
        try {
            _jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(),
                    ObjectFactory.class.getClassLoader());
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnalysisJob read(InputStream inputStream) throws NoSuchDatastoreException, NoSuchColumnException,
            NoSuchComponentException, ComponentConfigurationException, IllegalStateException {
        try (AnalysisJobBuilder ajb = create(inputStream)) {
            return ajb.toAnalysisJob();
        }
    }

    @Override
    public AnalysisJob read(InputStream inputStream, SourceColumnMapping sourceColumnMapping) {
        try (AnalysisJobBuilder ajb = create(inputStream, sourceColumnMapping)) {
            return ajb.toAnalysisJob();
        }
    }

    public AnalysisJobMetadata readMetadata(FileObject file) {
        InputStream inputStream = null;
        try {
            inputStream = file.getContent().getInputStream();
            return readMetadata(inputStream);
        } catch (FileSystemException e) {
            throw new IllegalArgumentException(e);
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }

    public AnalysisJobMetadata readMetadata(File file) {
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            return readMetadata(inputStream);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }

    @Override
    public AnalysisJobMetadata readMetadata(InputStream inputStream) {
        JobType job = unmarshallJob(inputStream);
        return readMetadata(job);
    }

    public AnalysisJobMetadata readMetadata(JobType job) {
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

        JobMetadataType metadata = job.getJobMetadata();
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

    private Map<String, String> getMetadataProperties(JobMetadataType metadata) {
        MetadataProperties properties = metadata.getMetadataProperties();

        if (properties == null) {
            return Collections.emptyMap();
        }

        Map<String, String> metadataProperties = new HashMap<String, String>();
        List<org.datacleaner.job.jaxb.MetadataProperties.Property> property = properties.getProperty();

        for (int i = 0; i < property.size(); i++) {
            String name = property.get(i).getName();
            String value = property.get(i).getValue();
            metadataProperties.put(name, value);
        }

        return metadataProperties;
    }

    public Map<String, String> getVariables(JobType job) {
        final Map<String, String> result = new HashMap<String, String>();

        VariablesType variablesType = job.getSource().getVariables();
        if (variablesType != null) {
            List<VariableType> variables = variablesType.getVariable();
            for (VariableType variableType : variables) {
                String id = variableType.getId();
                String value = variableType.getValue();
                result.put(id, value);
            }
        }

        return result;
    }

    public List<String> getSourceColumnPaths(JobType job) {
        final List<String> paths;

        final ColumnsType columnsType = job.getSource().getColumns();
        if (columnsType != null) {
            final List<ColumnType> columns = columnsType.getColumn();
            paths = new ArrayList<String>(columns.size());
            for (ColumnType columnType : columns) {
                final String path = columnType.getPath();
                paths.add(path);
            }
        } else {
            paths = Collections.emptyList();
        }
        return paths;
    }

    private List<org.apache.metamodel.schema.ColumnType> getSourceColumnTypes(JobType job) {
        final List<org.apache.metamodel.schema.ColumnType> types;

        final ColumnsType columnsType = job.getSource().getColumns();
        if (columnsType != null) {
            final List<ColumnType> columns = columnsType.getColumn();
            types = new ArrayList<org.apache.metamodel.schema.ColumnType>(columns.size());
            for (ColumnType columnType : columns) {
                final String typeName = columnType.getType();
                if (StringUtils.isNullOrEmpty(typeName)) {
                    types.add(null);
                } else {
                    try {
                        final org.apache.metamodel.schema.ColumnType type = org.apache.metamodel.schema.ColumnTypeImpl
                                .valueOf(typeName);
                        types.add(type);
                    } catch (IllegalArgumentException e) {
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

    public AnalysisJobBuilder create(FileObject file) {
        InputStream inputStream = null;
        try {
            inputStream = file.getContent().getInputStream();
            return create(inputStream);
        } catch (FileSystemException e) {
            throw new IllegalArgumentException(e);
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }

    public AnalysisJobBuilder create(File file) {
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            return create(inputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }

    public AnalysisJobBuilder create(InputStream inputStream) throws NoSuchDatastoreException {
        return create(unmarshallJob(inputStream), null, null);
    }

    public AnalysisJobBuilder create(InputStream inputStream, SourceColumnMapping sourceColumnMapping)
            throws NoSuchDatastoreException {
        return create(inputStream, sourceColumnMapping, null);
    }

    public AnalysisJobBuilder create(InputStream inputStream, SourceColumnMapping sourceColumnMapping,
            Map<String, String> variableOverrides) throws NoSuchDatastoreException {
        return create(unmarshallJob(inputStream), sourceColumnMapping, variableOverrides);
    }

    public AnalysisJobBuilder create(InputStream inputStream, Map<String, String> variableOverrides)
            throws NoSuchDatastoreException {
        return create(unmarshallJob(inputStream), null, variableOverrides);
    }

    private JobType unmarshallJob(InputStream inputStream) {
        try {
            Unmarshaller unmarshaller = _jaxbContext.createUnmarshaller();

            unmarshaller.setEventHandler(new JaxbValidationEventHandler());
            JobType job = (JobType) unmarshaller.unmarshal(inputStream);
            return job;
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public AnalysisJobBuilder create(JobType job) {
        return create(job, null, null);
    }

    public AnalysisJobBuilder create(JobType job, SourceColumnMapping sourceColumnMapping,
            Map<String, String> variableOverrides) throws NoSuchDatastoreException {
        if (job == null) {
            throw new IllegalArgumentException("Job cannot be null");
        }
        if (sourceColumnMapping != null && !sourceColumnMapping.isSatisfied()) {
            throw new IllegalArgumentException("Source column mapping is not satisfied!");
        }

        final Map<String, String> variables = getVariables(job);
        if (variableOverrides != null) {
            final Set<Entry<String, String>> entrySet = variableOverrides.entrySet();
            for (Entry<String, String> entry : entrySet) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                String originalValue = variables.put(key, value);
                logger.info("Overriding variable: {}={} (original value was {})", new Object[] { key, value,
                        originalValue });
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
            final AnalysisJobBuilder result = create(job, sourceColumnMapping, metadata, variables, builder);
            return result;
        } catch (RuntimeException e) {
            FileHelper.safeClose(builder);
            throw e;
        }
    }

    private AnalysisJobBuilder create(JobType job, SourceColumnMapping sourceColumnMapping, JobMetadataType metadata,
            final Map<String, String> variables, final AnalysisJobBuilder analysisJobBuilder) {
        String ref;

        final Datastore datastore;
        final DatastoreConnection datastoreConnection;
        final SourceType source = job.getSource();

        if (sourceColumnMapping == null) {
            // use automatic mapping if no explicit mapping is supplied
            DataContextType dataContext = source.getDataContext();
            ref = dataContext.getRef();
            if (StringUtils.isNullOrEmpty(ref)) {
                throw new IllegalStateException("Datastore ref cannot be null");
            }

            datastore = _configuration.getDatastoreCatalog().getDatastore(ref);
            if (datastore == null) {
                throw new NoSuchDatastoreException(ref);
            }
            datastoreConnection = datastore.openConnection();

            List<String> sourceColumnPaths = getSourceColumnPaths(job);
            sourceColumnMapping = new SourceColumnMapping(sourceColumnPaths);
            sourceColumnMapping.autoMap(datastore);
        } else {
            datastore = sourceColumnMapping.getDatastore();
            final DatastoreConnection connection = datastore.openConnection();
            datastoreConnection = connection;
        }

        // map column id's to input columns

        analysisJobBuilder.setDatastore(datastore);
        if (metadata != null) {
            final ImmutableAnalysisJobMetadata immutableAnalysisJobMetadata = new ImmutableAnalysisJobMetadata(
                    metadata.getJobName(), metadata.getJobVersion(), metadata.getJobDescription(),
                    metadata.getAuthor(), convertToDate(metadata.getCreatedDate()),
                    convertToDate(metadata.getUpdatedDate()), datastore.getName(), getSourceColumnPaths(job),
                    getSourceColumnTypes(job), variables, getMetadataProperties(metadata));
            analysisJobBuilder.setAnalysisJobMetadata(immutableAnalysisJobMetadata);
        }

        final Map<String, InputColumn<?>> inputColumns = new HashMap<String, InputColumn<?>>();

        final ColumnsType columnsType = source.getColumns();
        if (columnsType != null) {
            final List<ColumnType> columns = columnsType.getColumn();
            for (ColumnType column : columns) {
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
                    org.apache.metamodel.schema.ColumnType actualType = physicalColumn.getType();
                    if (actualType != null && !expectedType.equals(actualType.toString())) {
                        logger.warn("Column '{}' had type '{}', but '{}' was expected.", new Object[] { path,
                                actualType, expectedType });
                    }
                }

                registerInputColumn(inputColumns, id, inputColumn);
                analysisJobBuilder.addSourceColumn(inputColumn);
            }
        }

        final StringConverter stringConverter = createStringConverter(analysisJobBuilder);

        final Map<String, FilterOutcome> outcomeMapping = new HashMap<String, FilterOutcome>();

        final TransformationType transformation = job.getTransformation();
        if (transformation != null) {

            final List<Object> transformersAndFilters = transformation.getTransformerOrFilter();

            final Map<TransformerType, TransformerComponentBuilder<?>> transformerJobBuilders = new HashMap<>();
            final Map<FilterType, FilterComponentBuilder<?, ?>> filterJobBuilders = new HashMap<>();

            // iterate to initialize transformers
            for (Object o : transformersAndFilters) {
                if (o instanceof TransformerType) {
                    TransformerType transformer = (TransformerType) o;
                    ref = transformer.getDescriptor().getRef();
                    if (StringUtils.isNullOrEmpty(ref)) {
                        throw new IllegalStateException("Transformer descriptor ref cannot be null");
                    }
                    final TransformerDescriptor<?> transformerBeanDescriptor = _configuration.getEnvironment()
                            .getDescriptorProvider().getTransformerDescriptorByDisplayName(ref);
                    if (transformerBeanDescriptor == null) {
                        throw new NoSuchComponentException(Transformer.class, ref);
                    }
                    final TransformerComponentBuilder<?> transformerJobBuilder = analysisJobBuilder
                            .addTransformer(transformerBeanDescriptor);

                    transformerJobBuilder.setName(transformer.getName());

                    applyProperties(transformerJobBuilder, transformer.getProperties(),
                            transformer.getMetadataProperties(), stringConverter, variables);

                    transformerJobBuilders.put(transformer, transformerJobBuilder);
                }
            }

            // iterate again to set up transformed column dependencies
            List<TransformerType> unconfiguredTransformerKeys = new LinkedList<TransformerType>(
                    transformerJobBuilders.keySet());
            while (!unconfiguredTransformerKeys.isEmpty()) {
                boolean progress = false;
                for (Iterator<TransformerType> it = unconfiguredTransformerKeys.iterator(); it.hasNext();) {
                    boolean configurable = true;

                    TransformerType unconfiguredTransformerKey = it.next();
                    List<InputType> input = unconfiguredTransformerKey.getInput();
                    for (InputType inputType : input) {
                        ref = inputType.getRef();
                        if (StringUtils.isNullOrEmpty(ref)) {
                            String value = inputType.getValue();
                            if (value == null) {
                                throw new IllegalStateException("Transformer input column ref & value cannot be null");
                            }
                        } else if (!inputColumns.containsKey(ref)) {
                            configurable = false;
                            break;
                        }
                    }

                    if (configurable) {
                        progress = true;
                        TransformerComponentBuilder<?> transformerJobBuilder = transformerJobBuilders
                                .get(unconfiguredTransformerKey);

                        applyInputColumns(input, inputColumns, transformerJobBuilder);

                        List<MutableInputColumn<?>> outputColumns = transformerJobBuilder.getOutputColumns();
                        List<OutputType> output = unconfiguredTransformerKey.getOutput();

                        if (outputColumns.size() != output.size()) {
                            final String message = "Expected " + outputColumns.size() + " output column(s), but found "
                                    + output.size() + " (" + transformerJobBuilder + ")";
                            if (outputColumns.isEmpty()) {
                                // typically empty output columns is due to a
                                // component not being configured, we'll attach
                                // the configuration exception as a cause.
                                try {
                                    transformerJobBuilder.isConfigured(true);
                                } catch (Exception e) {
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

                        it.remove();
                    }
                }

                if (!progress) {
                    StringBuilder sb = new StringBuilder();
                    for (TransformerType transformerType : unconfiguredTransformerKeys) {
                        if (sb.length() != 0) {
                            sb.append(", ");
                        }
                        TransformerDescriptorType descriptor = transformerType.getDescriptor();
                        sb.append(descriptor.getRef());
                        sb.append("(input: ");

                        List<InputType> input = transformerType.getInput();
                        int i = 0;
                        for (InputType inputType : input) {
                            if (i != 0) {
                                sb.append(", ");
                            }
                            ref = inputType.getRef();
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
                            "Could not connect column dependencies for transformers: " + sb.toString());
                }
            }

            // iterate again to initialize all Filters and collect all outcomes
            for (Object o : transformersAndFilters) {
                if (o instanceof FilterType) {
                    FilterType filter = (FilterType) o;

                    ref = filter.getDescriptor().getRef();

                    if (StringUtils.isNullOrEmpty(ref)) {
                        throw new IllegalStateException("Filter descriptor ref cannot be null");
                    }
                    FilterDescriptor<?, ?> filterBeanDescriptor = _configuration.getEnvironment()
                            .getDescriptorProvider().getFilterDescriptorByDisplayName(ref);
                    if (filterBeanDescriptor == null) {
                        throw new NoSuchComponentException(Filter.class, ref);
                    }
                    FilterComponentBuilder<?, ?> filterJobBuilder = analysisJobBuilder.addFilter(filterBeanDescriptor);

                    filterJobBuilder.setName(filter.getName());

                    List<InputType> input = filter.getInput();
                    applyInputColumns(input, inputColumns, filterJobBuilder);
                    applyProperties(filterJobBuilder, filter.getProperties(), filter.getMetadataProperties(),
                            stringConverter, variables);

                    filterJobBuilders.put(filter, filterJobBuilder);

                    List<OutcomeType> outcomeTypes = filter.getOutcome();
                    for (OutcomeType outcomeType : outcomeTypes) {
                        String categoryName = outcomeType.getCategory();
                        Enum<?> category = filterJobBuilder.getDescriptor().getOutcomeCategoryByName(categoryName);
                        if (category == null) {
                            throw new ComponentConfigurationException("No such outcome category name: " + categoryName
                                    + " (in " + filterJobBuilder.getDescriptor().getDisplayName() + ")");
                        }

                        String id = outcomeType.getId();
                        if (StringUtils.isNullOrEmpty(id)) {
                            throw new IllegalStateException("Outcome id cannot be null");
                        }
                        if (outcomeMapping.containsKey(id)) {
                            throw new ComponentConfigurationException("Outcome id '" + id + "' is not unique");
                        }
                        outcomeMapping.put(id, filterJobBuilder.getFilterOutcome(category));
                    }
                }
            }

            // iterate again to set up filter outcome dependencies
            for (Object o : transformersAndFilters) {
                if (o instanceof TransformerType) {
                    ref = ((TransformerType) o).getRequires();
                    if (ref != null) {
                        final TransformerComponentBuilder<?> builder = transformerJobBuilders.get(o);
                        final ComponentRequirement requirement = getRequirement(ref, outcomeMapping);
                        builder.setComponentRequirement(requirement);
                    }
                } else if (o instanceof FilterType) {
                    ref = ((FilterType) o).getRequires();
                    if (ref != null) {
                        final FilterComponentBuilder<?, ?> builder = filterJobBuilders.get(o);
                        final ComponentRequirement requirement = getRequirement(ref, outcomeMapping);
                        builder.setComponentRequirement(requirement);
                    }
                } else {
                    throw new IllegalStateException("Unexpected transformation child element: " + o);
                }
            }
        }

        AnalysisType analysis = job.getAnalysis();

        List<AnalyzerType> analyzers = analysis.getAnalyzer();
        for (AnalyzerType analyzerType : analyzers) {
            ref = analyzerType.getDescriptor().getRef();
            if (StringUtils.isNullOrEmpty(ref)) {
                throw new IllegalStateException("Analyzer descriptor ref cannot be null");
            }

            AnalyzerDescriptor<?> descriptor = _configuration.getEnvironment().getDescriptorProvider()
                    .getAnalyzerDescriptorByDisplayName(ref);

            if (descriptor == null) {
                throw new NoSuchComponentException(Analyzer.class, ref);
            }

            Class<? extends Analyzer<?>> beanClass = descriptor.getComponentClass();
            AnalyzerComponentBuilder<? extends Analyzer<?>> analyzerJobBuilder = analysisJobBuilder
                    .addAnalyzer(beanClass);
            analyzerJobBuilder.setName(analyzerType.getName());

            List<InputType> input = analyzerType.getInput();

            applyInputColumns(input, inputColumns, analyzerJobBuilder);
            applyProperties(analyzerJobBuilder, analyzerType.getProperties(), analyzerType.getMetadataProperties(),
                    stringConverter, variables);

            ref = analyzerType.getRequires();
            if (ref != null) {
                final ComponentRequirement requirement = getRequirement(ref, outcomeMapping);
                analyzerJobBuilder.setComponentRequirement(requirement);
            }

        }

        datastoreConnection.close();

        return analysisJobBuilder;
    }

    private Date convertToDate(XMLGregorianCalendar calendar) {
        if (calendar != null) {
            return calendar.toGregorianCalendar().getTime();
        }
        return null;
    }

    private ComponentRequirement getRequirement(String ref, Map<String, FilterOutcome> outcomeMapping) {
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
        final List<String> tokens = Splitter.on(" OR ").omitEmptyStrings().trimResults().splitToList(ref);
        if (tokens.size() > 1) {
            final List<FilterOutcome> list = new ArrayList<>(tokens.size());
            for (final String token : tokens) {
                final FilterOutcome filterOutcome = outcomeMapping.get(token);
                if (filterOutcome == null) {
                    throw new ComponentConfigurationException("Could not resolve outcome '" + token
                            + "' in requirement: " + ref);
                }
                list.add(filterOutcome);
            }
            return new CompoundComponentRequirement(list);
        }

        throw new ComponentConfigurationException("Could not resolve requirement: " + ref);
    }

    private void applyInputColumns(List<InputType> input, Map<String, InputColumn<?>> inputColumns,
            ComponentBuilder componentBuilder) {
        // build a map of inputs first so that we can set the
        // input in one go
        final ListMultimap<ConfiguredPropertyDescriptor, InputColumn<?>> inputMap = MultimapBuilder.hashKeys()
                .arrayListValues().build();

        for (InputType inputType : input) {
            String name = inputType.getName();
            String ref = inputType.getRef();
            InputColumn<?> inputColumn;
            if (StringUtils.isNullOrEmpty(ref)) {
                inputColumn = createExpressionBasedInputColumn(inputType);
            } else {
                inputColumn = inputColumns.get(ref);
            }
            if (StringUtils.isNullOrEmpty(name)) {
                ConfiguredPropertyDescriptor propertyDescriptor = componentBuilder
                        .getDefaultConfiguredPropertyForInput();
                inputMap.put(propertyDescriptor, inputColumn);
            } else {
                ConfiguredPropertyDescriptor propertyDescriptor = componentBuilder.getDescriptor()
                        .getConfiguredProperty(name);
                inputMap.put(propertyDescriptor, inputColumn);
            }
        }

        final Set<ConfiguredPropertyDescriptor> keys = inputMap.keySet();
        for (ConfiguredPropertyDescriptor propertyDescriptor : keys) {
            List<InputColumn<?>> inputColumnsForProperty = inputMap.get(propertyDescriptor);
            componentBuilder.addInputColumns(inputColumnsForProperty, propertyDescriptor);
        }
    }

    private StringConverter createStringConverter(final AnalysisJobBuilder analysisJobBuilder) {
        final AnalysisJob job = analysisJobBuilder.toAnalysisJob(false);
        return new StringConverter(_configuration, job);
    }

    private InputColumn<?> createExpressionBasedInputColumn(InputType inputType) {
        String expression = inputType.getValue();
        if (expression == null) {
            throw new IllegalStateException("Input ref & value cannot both be null");
        }
        if (expression.indexOf("#{") == -1) {
            return new ConstantInputColumn(expression);
        } else {
            return new ELInputColumn(expression);
        }
    }

    private void registerInputColumn(Map<String, InputColumn<?>> inputColumns, String id, InputColumn<?> inputColumn) {
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
            final StringConverter stringConverter, final Map<String, String> variables) {
        if (configuredPropertiesType != null) {
            final List<Property> properties = configuredPropertiesType.getProperty();
            final ComponentDescriptor<?> descriptor = builder.getDescriptor();
            for (Property property : properties) {
                final String name = property.getName();
                final ConfiguredPropertyDescriptor configuredProperty = descriptor.getConfiguredProperty(name);

                if (configuredProperty == null) {
                    throw new ComponentConfigurationException("No such property: " + name);
                }

                String stringValue = getValue(property);
                if (stringValue == null) {
                    String variableRef = property.getRef();
                    if (variableRef == null) {
                        throw new IllegalStateException("Neither value nor ref was specified for property: " + name);
                    }

                    stringValue = variables.get(variableRef);

                    if (stringValue == null) {
                        throw new ComponentConfigurationException("No such variable: " + variableRef);
                    }
                }

                final Class<? extends Converter<?>> customConverter = configuredProperty.getCustomConverter();
                final Object value = stringConverter.deserialize(stringValue, configuredProperty.getType(),
                        customConverter);

                logger.debug("Setting property '{}' to {}", name, value);
                builder.setConfiguredProperty(configuredProperty, value);
            }
        }
        if (metadataPropertiesType != null) {
            final List<org.datacleaner.job.jaxb.MetadataProperties.Property> propertyList = metadataPropertiesType
                    .getProperty();
            for (org.datacleaner.job.jaxb.MetadataProperties.Property property : propertyList) {
                final String name = property.getName();
                final String value = property.getValue();
                builder.setMetadataProperty(name, value);
            }
        }
    }

    private String getValue(Property property) {
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
