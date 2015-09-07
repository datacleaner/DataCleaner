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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.metamodel.MetaModelHelper;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.datacleaner.api.ExpressionBasedInputColumn;
import org.datacleaner.api.InputColumn;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.OutputDataStreamDatastore;
import org.datacleaner.connection.SchemaNavigator;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.jaxb.AnalysisType;
import org.datacleaner.job.jaxb.AnalyzerType;
import org.datacleaner.job.jaxb.ColumnType;
import org.datacleaner.job.jaxb.ColumnsType;
import org.datacleaner.job.jaxb.ComponentType;
import org.datacleaner.job.jaxb.ConfiguredPropertiesType;
import org.datacleaner.job.jaxb.ConfiguredPropertiesType.Property;
import org.datacleaner.job.jaxb.DataContextType;
import org.datacleaner.job.jaxb.DescriptorType;
import org.datacleaner.job.jaxb.FilterType;
import org.datacleaner.job.jaxb.InputType;
import org.datacleaner.job.jaxb.Job;
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
import org.datacleaner.util.convert.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class JaxbJobWriter implements JobWriter<OutputStream> {

    private static final String COLUMN_PATH_QUALIFICATION_FULL = "full";
    private static final String COLUMN_PATH_QUALIFICATION_TABLE = "table";
    private static final String COLUMN_PATH_QUALIFICATION_COLUMN = "column";

    private static final Logger logger = LoggerFactory.getLogger(JaxbJobWriter.class);

    private final DataCleanerConfiguration _configuration;
    private final JAXBContext _jaxbContext;
    private final JaxbJobMetadataFactory _jobMetadataFactory;

    public JaxbJobWriter(DataCleanerConfiguration configuration, JaxbJobMetadataFactory jobMetadataFactory) {
        _configuration = configuration;
        _jobMetadataFactory = jobMetadataFactory;
        try {
            _jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(),
                    ObjectFactory.class.getClassLoader());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public JaxbJobWriter(DataCleanerConfiguration configuration) {
        this(configuration, new JaxbJobMetadataFactoryImpl());
    }

    @Override
    public void write(final AnalysisJob analysisJob, final OutputStream outputStream) {
        logger.debug("write({},{}}", analysisJob, outputStream);

        final Job job = new Job();
        configureJobType(analysisJob, job, true);

        try {
            final Marshaller marshaller = _jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setEventHandler(new JaxbValidationEventHandler());
            marshaller.marshal(job, outputStream);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    private void configureJobType(final AnalysisJob analysisJob, final JobType jobType, boolean includeMetadata) {
        if (includeMetadata) {
            try {
                JobMetadataType jobMetadata = _jobMetadataFactory.create(analysisJob);
                jobType.setJobMetadata(jobMetadata);
            } catch (Exception e) {
                logger.warn("Exception occurred while creating job metadata", e);
            }
        }

        final VariablesType variablesType = new VariablesType();
        final Map<String, String> variables = analysisJob.getMetadata().getVariables();
        if ((variables != null) && (variables.size() > 0)) {
            for (Map.Entry<String, String> variableEntry : variables.entrySet()) {
                final VariableType variableType = new VariableType();
                variableType.setId(variableEntry.getKey());
                variableType.setValue(variableEntry.getValue());
                variablesType.getVariable().add(variableType);
            }
        }

        final SourceType sourceType = new SourceType();
        sourceType.setColumns(new ColumnsType());
        if ((variables != null) && (variables.size() > 0)) {
            sourceType.setVariables(variablesType);
        }
        jobType.setSource(sourceType);

        final Datastore datastore = analysisJob.getDatastore();
        if (!(datastore instanceof OutputDataStreamDatastore)) {
            final DataContextType dataContextType = new DataContextType();
            if (datastore == null) {
                logger.warn("No datastore specified for analysis job: {}", analysisJob);
            } else {
                dataContextType.setRef(datastore.getName());
            }
            sourceType.setDataContext(dataContextType);
        }
        // mappings for lookup of ID's
        final BiMap<InputColumn<?>, String> columnMappings = HashBiMap.create(50);
        final Map<FilterOutcome, String> outcomeMappings = new LinkedHashMap<FilterOutcome, String>();

        // mappings for lookup of component's elements
        final Map<TransformerJob, TransformerType> transformerMappings = new LinkedHashMap<TransformerJob, TransformerType>();
        final Map<FilterJob, FilterType> filterMappings = new LinkedHashMap<FilterJob, FilterType>();
        final Map<AnalyzerJob, AnalyzerType> analyzerMappings = new LinkedHashMap<AnalyzerJob, AnalyzerType>();

        // register all source columns
        final Collection<InputColumn<?>> sourceColumns = analysisJob.getSourceColumns();
        final String columnPathQualification = getColumnPathQualification(datastore, sourceColumns);
        for (InputColumn<?> inputColumn : sourceColumns) {
            final ColumnType jaxbColumn = new ColumnType();
            final Column physicalColumn = inputColumn.getPhysicalColumn();
            jaxbColumn.setPath(getColumnPath(physicalColumn, columnPathQualification));
            jaxbColumn.setId(getColumnId(inputColumn, columnMappings));

            final org.apache.metamodel.schema.ColumnType columnType = physicalColumn.getType();
            if (columnType != null) {
                jaxbColumn.setType(columnType.toString());
            }

            sourceType.getColumns().getColumn().add(jaxbColumn);
        }

        // adds all components to the job and their corresponding mappings
        addComponents(jobType, analysisJob, transformerMappings, filterMappings, analyzerMappings);

        // add all transformed columns to their originating components and the
        // mappings
        addTransformedColumns(columnMappings, transformerMappings);

        // register all requirements
        addRequirements(outcomeMappings, transformerMappings, filterMappings, analyzerMappings, columnMappings);

        addConfiguration(analysisJob, transformerMappings, filterMappings, analyzerMappings, columnMappings);
    }

    private String getColumnPath(Column column, String columnPathQualification) {
        switch (columnPathQualification) {
        case COLUMN_PATH_QUALIFICATION_COLUMN:
            final String columnName = column.getName();
            if (Strings.isNullOrEmpty(columnName)) {
                return column.getTable().getName() + '.' + column.getName();
            }
            return column.getName();
        case COLUMN_PATH_QUALIFICATION_TABLE:
            return column.getTable().getName() + '.' + column.getName();
        case COLUMN_PATH_QUALIFICATION_FULL:
        default:
            return column.getQualifiedLabel();
        }
    }

    private String getColumnPathQualification(Datastore datastore, Collection<InputColumn<?>> sourceColumns) {
        if (datastore == null || sourceColumns == null || sourceColumns.isEmpty()) {
            return COLUMN_PATH_QUALIFICATION_FULL;
        }

        try (DatastoreConnection connection = datastore.openConnection()) {
            SchemaNavigator schemaNavigator = connection.getSchemaNavigator();
            Schema[] schemas = schemaNavigator.getSchemas();
            Schema singleSchema = null;
            int realSchemas = 0;
            for (Schema schema : schemas) {
                if (!MetaModelHelper.isInformationSchema(schema)) {
                    realSchemas++;
                    singleSchema = schema;
                }
            }

            if (realSchemas == 1) {
                if (singleSchema.getTableCount() == 1) {
                    return COLUMN_PATH_QUALIFICATION_COLUMN;
                }
                return COLUMN_PATH_QUALIFICATION_TABLE;
            }

            return COLUMN_PATH_QUALIFICATION_FULL;
        }
    }

    private void addConfiguration(final AnalysisJob analysisJob,
            final Map<TransformerJob, TransformerType> transformerMappings,
            final Map<FilterJob, FilterType> filterMappings, final Map<AnalyzerJob, AnalyzerType> analyzerMappings,
            final BiMap<InputColumn<?>, String> columnMappings) {

        final StringConverter stringConverter = new StringConverter(_configuration, analysisJob);

        // configure transformers
        for (Entry<TransformerJob, TransformerType> entry : transformerMappings.entrySet()) {
            TransformerJob job = entry.getKey();
            TransformerType elementType = entry.getValue();
            ComponentConfiguration configuration = job.getConfiguration();

            Set<ConfiguredPropertyDescriptor> configuredProperties = job.getDescriptor()
                    .getConfiguredPropertiesForInput();
            elementType.getInput().addAll(
                    createInputConfiguration(configuration, configuredProperties, columnMappings, stringConverter));

            configuredProperties = job.getDescriptor().getConfiguredProperties();
            elementType.setProperties(createPropertyConfiguration(configuration, configuredProperties, stringConverter,
                    job.getMetadataProperties()));
            elementType.setMetadataProperties(createMetadataProperties(job.getMetadataProperties()));
        }

        // configure filters
        for (Entry<FilterJob, FilterType> entry : filterMappings.entrySet()) {
            FilterJob job = entry.getKey();
            FilterType elementType = entry.getValue();
            ComponentConfiguration configuration = job.getConfiguration();

            Set<ConfiguredPropertyDescriptor> configuredProperties = job.getDescriptor()
                    .getConfiguredPropertiesForInput();
            elementType.getInput().addAll(
                    createInputConfiguration(configuration, configuredProperties, columnMappings, stringConverter));

            configuredProperties = job.getDescriptor().getConfiguredProperties();
            elementType.setProperties(createPropertyConfiguration(configuration, configuredProperties, stringConverter,
                    job.getMetadataProperties()));
            elementType.setMetadataProperties(createMetadataProperties(job.getMetadataProperties()));
        }

        // configure analyzers
        for (Entry<AnalyzerJob, AnalyzerType> entry : analyzerMappings.entrySet()) {
            AnalyzerJob job = entry.getKey();
            AnalyzerType elementType = entry.getValue();
            ComponentConfiguration configuration = job.getConfiguration();

            Set<ConfiguredPropertyDescriptor> configuredProperties = job.getDescriptor()
                    .getConfiguredPropertiesForInput();
            elementType.getInput().addAll(
                    createInputConfiguration(configuration, configuredProperties, columnMappings, stringConverter));

            configuredProperties = job.getDescriptor().getConfiguredProperties();
            elementType.setProperties(createPropertyConfiguration(configuration, configuredProperties, stringConverter,
                    job.getMetadataProperties()));
            elementType.setMetadataProperties(createMetadataProperties(job.getMetadataProperties()));
        }
    }

    private MetadataProperties createMetadataProperties(Map<String, String> metadataProperties) {
        if (metadataProperties == null || metadataProperties.isEmpty()) {
            return null;
        }
        final MetadataProperties result = new MetadataProperties();
        final Set<Entry<String, String>> entries = metadataProperties.entrySet();
        for (Entry<String, String> entry : entries) {
            final org.datacleaner.job.jaxb.MetadataProperties.Property property = new org.datacleaner.job.jaxb.MetadataProperties.Property();
            property.setName(entry.getKey());
            property.setValue(entry.getValue());
            result.getProperty().add(property);
        }
        return result;
    }

    private List<InputType> createInputConfiguration(final ComponentConfiguration configuration,
            Set<ConfiguredPropertyDescriptor> configuredProperties, final BiMap<InputColumn<?>, String> columnMappings,
            final StringConverter stringConverter) {

        // sort the properties in order to make the result deterministic
        configuredProperties = new TreeSet<ConfiguredPropertyDescriptor>(configuredProperties);

        final int numInputProperties = configuredProperties.size();
        final List<InputType> result = new ArrayList<InputType>();
        for (ConfiguredPropertyDescriptor property : configuredProperties) {
            if (property.isInputColumn()) {
                final Object value = configuration.getProperty(property);
                if (value != null) {
                    final InputColumn<?>[] columns;
                    if (property.isArray()) {
                        columns = (InputColumn<?>[]) value;
                    } else {
                        columns = new InputColumn<?>[1];
                        columns[0] = (InputColumn<?>) value;
                    }

                    for (final InputColumn<?> inputColumn : columns) {
                        if (inputColumn != null) {
                            final InputType inputType = new InputType();
                            if (inputColumn instanceof ExpressionBasedInputColumn) {
                                ExpressionBasedInputColumn<?> expressionBasedInputColumn = (ExpressionBasedInputColumn<?>) inputColumn;
                                Object columnValue = expressionBasedInputColumn.getExpression();
                                inputType
                                        .setValue(stringConverter.serialize(columnValue, property.getCustomConverter()));
                            } else {
                                inputType.setRef(getColumnId(inputColumn, columnMappings));
                            }
                            if (numInputProperties != 1) {
                                inputType.setName(property.getName());
                            }
                            result.add(inputType);
                        }
                    }
                }
            }
        }
        return result;
    }

    private ConfiguredPropertiesType createPropertyConfiguration(final ComponentConfiguration configuration,
            Set<ConfiguredPropertyDescriptor> configuredProperties, StringConverter stringConverter,
            Map<String, String> componentMetadataProperties) {

        // sort the properties in order to make the result deterministic
        configuredProperties = new TreeSet<ConfiguredPropertyDescriptor>(configuredProperties);

        List<Property> result = new ArrayList<Property>();
        for (ConfiguredPropertyDescriptor property : configuredProperties) {
            if (!property.isInputColumn()) {
                final Property propertyType = new Property();
                propertyType.setName(property.getName());

                final String variableNameWithPrefix = JaxbJobReader.DATACLEANER_JAXB_VARIABLE_PREFIX
                        + property.getName();
                if (componentMetadataProperties.containsKey(variableNameWithPrefix)) {
                    propertyType.setRef(componentMetadataProperties.get(variableNameWithPrefix));
                } else {
                    Object value = configuration.getProperty(property);
                    String stringValue = stringConverter.serialize(value, property.getCustomConverter());

                    if (stringValue != null && stringValue.indexOf('\n') != -1) {
                        // multi-line values are put as simple content of the
                        // property
                        propertyType.setValue(stringValue);
                    } else {
                        // single-line values are preferred as an attribute for
                        // backwards compatibility
                        propertyType.setValueAttribute(stringValue);
                    }
                }
                result.add(propertyType);
            }
        }
        ConfiguredPropertiesType configuredPropertiesType = new ConfiguredPropertiesType();
        configuredPropertiesType.getProperty().addAll(result);
        return configuredPropertiesType;
    }

    private void addTransformedColumns(final BiMap<InputColumn<?>, String> columnMappings,
            final Map<TransformerJob, TransformerType> transformerMappings) {
        // register all transformed columns
        for (Entry<TransformerJob, TransformerType> entry : transformerMappings.entrySet()) {
            final TransformerJob transformerJob = entry.getKey();
            final TransformerType transformerType = entry.getValue();
            final InputColumn<?>[] columns = transformerJob.getOutput();
            for (InputColumn<?> inputColumn : columns) {
                final String id = getColumnId(inputColumn, columnMappings);
                final OutputType outputType = new OutputType();
                outputType.setId(id);
                outputType.setName(inputColumn.getName());
                if (inputColumn instanceof MutableInputColumn) {
                    final boolean hidden = ((MutableInputColumn<?>) inputColumn).isHidden();
                    if (hidden) {
                        outputType.setHidden(hidden);
                    }
                }

                transformerType.getOutput().add(outputType);
            }
        }
    }

    private void addRequirements(final Map<FilterOutcome, String> outcomeMappings,
            final Map<TransformerJob, TransformerType> transformerMappings,
            final Map<FilterJob, FilterType> filterMappings, final Map<AnalyzerJob, AnalyzerType> analyzerMappings,
            final Map<InputColumn<?>, String> columnMappings) {

        // add requirements based on all transformer requirements
        for (final Entry<TransformerJob, TransformerType> entry : transformerMappings.entrySet()) {
            final TransformerJob job = entry.getKey();
            final ComponentRequirement requirement = job.getComponentRequirement();
            if (requirement != null) {
                String id = getId(requirement, outcomeMappings);
                entry.getValue().setRequires(id);
            }
        }

        // add requirements based on all filter requirements
        for (final Entry<FilterJob, FilterType> entry : filterMappings.entrySet()) {
            final FilterJob job = entry.getKey();
            final ComponentRequirement requirement = job.getComponentRequirement();
            if (requirement != null) {
                String id = getId(requirement, outcomeMappings);
                entry.getValue().setRequires(id);
            }
        }

        // add requirements based on all analyzer requirements
        for (Entry<AnalyzerJob, AnalyzerType> entry : analyzerMappings.entrySet()) {
            final AnalyzerJob job = entry.getKey();
            final ComponentRequirement requirement = job.getComponentRequirement();
            if (requirement != null) {
                String id = getId(requirement, outcomeMappings);
                entry.getValue().setRequires(id);
            }
        }

        // add outcome elements only for those filter requirements that
        // have been mapped
        for (final Entry<FilterJob, FilterType> entry : filterMappings.entrySet()) {
            final FilterJob job = entry.getKey();
            final FilterType filterType = entry.getValue();
            final Collection<FilterOutcome> outcomes = job.getFilterOutcomes();
            for (final FilterOutcome outcome : outcomes) {
                // note that we DONT use the getId(...) method here
                final String id = getId(outcome, outcomeMappings, false);
                // only the outcome element if it is being mapped
                if (id != null) {
                    final OutcomeType outcomeType = new OutcomeType();
                    outcomeType.setCategory(outcome.getCategory().name());
                    outcomeType.setId(id);
                    filterType.getOutcome().add(outcomeType);
                }
            }
        }
    }

    private String getId(ComponentRequirement requirement, Map<FilterOutcome, String> outcomeMappings) {
        if (requirement instanceof AnyComponentRequirement) {
            return AnyComponentRequirement.KEYWORD;
        }

        if (requirement instanceof SimpleComponentRequirement) {
            final FilterOutcome outcome = ((SimpleComponentRequirement) requirement).getOutcome();
            return getId(outcome, outcomeMappings, true);
        }

        if (requirement instanceof CompoundComponentRequirement) {
            final Set<FilterOutcome> outcomes = ((CompoundComponentRequirement) requirement).getOutcomes();
            final StringBuilder sb = new StringBuilder();
            for (FilterOutcome outcome : outcomes) {
                if (sb.length() != 0) {
                    sb.append(" OR ");
                }
                final String id = getId(outcome, outcomeMappings, true);
                sb.append(id);
            }
            return sb.toString();
        }

        throw new UnsupportedOperationException("Unsupported ComponentRequirement type: " + requirement);
    }

    private String getId(FilterOutcome outcome, Map<FilterOutcome, String> outcomeMappings, boolean create) {
        String id = outcomeMappings.get(outcome);
        if (id == null) {
            if (create) {
                id = "outcome_" + outcomeMappings.size();
                outcomeMappings.put(outcome, id);
            }
        }
        return id;
    }

    private void addComponents(final JobType jobType, final AnalysisJob analysisJob,
            final Map<TransformerJob, TransformerType> transformerMappings,
            final Map<FilterJob, FilterType> filterMappings, final Map<AnalyzerJob, AnalyzerType> analyzerMappings) {
        final TransformationType transformationType = new TransformationType();
        jobType.setTransformation(transformationType);

        final AnalysisType analysisType = new AnalysisType();
        jobType.setAnalysis(analysisType);

        // add all transformers to the transformation element
        final Collection<TransformerJob> transformerJobs = analysisJob.getTransformerJobs();
        for (TransformerJob transformerJob : transformerJobs) {
            TransformerType transformerType = new TransformerType();
            transformerType.setName(transformerJob.getName());
            setDescriptor(transformerType, transformerJob.getDescriptor());
            transformationType.getTransformerOrFilter().add(transformerType);
            transformerMappings.put(transformerJob, transformerType);
        }

        // add all filters to the transformation element
        Collection<FilterJob> filterJobs = analysisJob.getFilterJobs();
        for (FilterJob filterJob : filterJobs) {
            FilterType filterType = new FilterType();
            filterType.setName(filterJob.getName());
            setDescriptor(filterType, filterJob.getDescriptor());
            transformationType.getTransformerOrFilter().add(filterType);
            filterMappings.put(filterJob, filterType);
        }

        // add all analyzers to the analysis element
        Collection<AnalyzerJob> analyzerJobs = analysisJob.getAnalyzerJobs();
        for (AnalyzerJob analyzerJob : analyzerJobs) {
            AnalyzerType analyzerType = new AnalyzerType();
            analyzerType.setName(analyzerJob.getName());
            setDescriptor(analyzerType, analyzerJob.getDescriptor());
            final OutputDataStreamJob[] outputDataStreamJobs = analyzerJob.getOutputDataStreamJobs();

            for (OutputDataStreamJob outputDataStreamJob : outputDataStreamJobs) {
                final OutputDataStreamType outputDataStreamType = new OutputDataStreamType();
                outputDataStreamType.setName(outputDataStreamJob.getOutputDataStream().getName());
                final JobType childJobType = new JobType();
                configureJobType(outputDataStreamJob.getJob(), childJobType, false);
                outputDataStreamType.setJob(childJobType);
                analyzerType.getOutputDataStream().add(outputDataStreamType);
            }
            analysisType.getAnalyzer().add(analyzerType);
            analyzerMappings.put(analyzerJob, analyzerType);
        }
    }

    private void setDescriptor(ComponentType componentType, ComponentDescriptor<?> descriptor) {
        DescriptorType descriptorType = new DescriptorType();
        descriptorType.setRef(descriptor.getDisplayName());
        componentType.setDescriptor(descriptorType);
    }

    private static String getColumnId(InputColumn<?> inputColumn, BiMap<InputColumn<?>, String> columnMappings) {
        if (inputColumn == null) {
            throw new IllegalArgumentException("InputColumn cannot be null");
        }

        String id = columnMappings.get(inputColumn);
        if (id == null) {
            final String baseColumnId = getBaseColumnId(inputColumn);
            id = baseColumnId;
            int addition = 1;
            while (columnMappings.containsValue(id)) {
                addition++;
                id = baseColumnId + addition;
            }
            columnMappings.put(inputColumn, id);
        }
        return id;
    }

    private static String getBaseColumnId(InputColumn<?> inputColumn) {
        String cleansedColumnName = "col_" + Strings.nullToEmpty(inputColumn.getName());
        cleansedColumnName = cleansedColumnName.toLowerCase().trim();
        cleansedColumnName = cleansedColumnName.replaceAll("[^a-z0-9_]", "");
        return cleansedColumnName;
    }
}
