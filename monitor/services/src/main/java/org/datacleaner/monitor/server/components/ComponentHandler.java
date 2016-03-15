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
package org.datacleaner.monitor.server.components;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.metamodel.data.DataSetHeader;
import org.apache.metamodel.data.DefaultRow;
import org.apache.metamodel.data.SimpleDataSetHeader;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.ColumnTypeImpl;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableTable;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Component;
import org.datacleaner.api.ComponentContext;
import org.datacleaner.api.HasAnalyzerResult;
import org.datacleaner.api.HiddenProperty;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.api.Transformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.configuration.InjectionPoint;
import org.datacleaner.connection.Datastore;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MetaModelInputRow;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.PropertyDescriptor;
import org.datacleaner.descriptors.ProvidedPropertyDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalysisJobMetadata;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.ComponentValidationException;
import org.datacleaner.job.FilterJob;
import org.datacleaner.job.ImmutableComponentConfiguration;
import org.datacleaner.job.OutputDataStreamJob;
import org.datacleaner.job.TransformerJob;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.job.concurrent.ThreadLocalOutputRowCollector;
import org.datacleaner.job.runner.AnalysisListener;
import org.datacleaner.job.runner.ComponentContextImpl;
import org.datacleaner.job.tasks.Task;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.monitor.configuration.RemoteComponentsConfiguration;
import org.datacleaner.restclient.ComponentConfiguration;
import org.datacleaner.restclient.Serializator;
import org.datacleaner.util.convert.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * This class is a component type independent wrapper that decides the proper
 * handler and provides its results.
 *
 * @since 14. 07. 2015
 */
public class ComponentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentHandler.class);
    public static final ObjectMapper mapper = Serializator.getJacksonObjectMapper();
    private final DataCleanerConfiguration _dcConfiguration;
    private StringConverter _stringConverter;
    private final ComponentDescriptor<?> descriptor;
    private final Map<String, MutableColumn> columns;
    private final Map<String, InputColumn<?>> inputColumns;
    private final InputColumn<?>[] configuredInputColumns;
    private final List<InputColumn<?>> inputColumnsList;
    private final MutableTable table;
    private final Component component;
    private final LifeCycleHelper lifeCycleHelper;
    private RemoteComponentsConfiguration _remoteComponentsConfiguration;
    private final org.datacleaner.job.ComponentConfiguration config;
    private final ComponentContext componentContext;

    public ComponentHandler(DataCleanerConfiguration dcConfiguration, ComponentDescriptor<?> componentDescriptor, ComponentConfiguration componentConfiguration, RemoteComponentsConfiguration remoteComponentsConfiguration, AnalysisListener analysisListener) {
        Objects.requireNonNull(componentConfiguration, "Component configuration cannot be null");
        
        _remoteComponentsConfiguration = remoteComponentsConfiguration;
        _dcConfiguration = dcConfiguration;
        columns = new HashMap<>();
        inputColumns = new HashMap<>();
        descriptor = componentDescriptor;
        table = new MutableTable("inputData");
        component = (Component) descriptor.newInstance();

        // TODO: differentiate between transformer/analyzer for ComponentJob parameter of ComponentContextImpl
        AnalysisJob analysisJob = new ComponentHandlerAnalysisJob();
        componentContext = new ComponentContextImpl(analysisJob, new ComponentHandlerTransformerJob(), analysisListener);

        // create "table" according to the columns specification (for now only a
        // list of names)
        int index = 0;
        inputColumnsList = new ArrayList<>();
        for (JsonNode columnSpec : componentConfiguration.getColumns()) {
            String columnName;
            String columnTypeName;
            if (columnSpec.isObject()) {
                ObjectNode columnSpecO = (ObjectNode) columnSpec;
                columnName = columnSpecO.get("name").asText();
                if (columnSpecO.get("type") == null) {
                    columnTypeName = ColumnType.VARCHAR.getName();
                } else {
                    columnTypeName = columnSpecO.get("type").asText();
                }
            } else {
                columnName = columnSpec.asText();
                columnTypeName = ColumnType.VARCHAR.getName();
            }

            MutableColumn column = columns.get(columnName);
            if (column != null) {
                throw new IllegalArgumentException("Multiple column definition of name '" + columnName + "'");
            }
            ColumnType columnType = ColumnTypeImpl.valueOf(columnTypeName);
            if (columnType == null) {
                throw new IllegalArgumentException("Column '" + columnName + "' has unknown type '" + columnTypeName + "'");
            }
            column = new MutableColumn(columnName, columnType, table, index, true);
            columns.put(columnName, column);
            InputColumn<?> inputColumn = new MetaModelInputColumn(column);
            inputColumns.put(columnName, inputColumn);
            inputColumnsList.add(inputColumn);
            table.addColumn(index, column);
            index++;
        }

        // Set the configured properties

        // First, copy current values = the defaults
        Map<PropertyDescriptor, Object> configuredProperties = new HashMap<>();
        Set<ConfiguredPropertyDescriptor> props = descriptor.getConfiguredProperties();
        for (ConfiguredPropertyDescriptor propDesc : props) {
            Object defaultValue = propDesc.getValue(component);
            if (defaultValue != null) {
                configuredProperties.put(propDesc, defaultValue);
            }
        }

        //Admin properties from xml context
        Map<PropertyDescriptor, Object> remoteDefaultPropertiesMap = _remoteComponentsConfiguration.getDefaultValues(descriptor);
        configuredProperties.putAll(remoteDefaultPropertiesMap);

        List<InputColumn<?>> configuredInputColumnsList = new ArrayList<>();
        //User properties
        for(String propertyName: componentConfiguration.getProperties().keySet()) {
            ConfiguredPropertyDescriptor propDesc = descriptor.getConfiguredProperty(propertyName);
            if (propDesc == null) {
                LOGGER.debug("Unknown configuration property '{}'. ", propertyName);
                continue;
            }

            final HiddenProperty hiddenProperty = propDesc.getAnnotation(HiddenProperty.class);
            if (hiddenProperty != null && hiddenProperty.hiddenForRemoteAccess()) {
                LOGGER.debug("Hidden property '{}' is skipped. ", propertyName);
                continue;
            }

            final JsonNode userPropValue = componentConfiguration.getProperties().get(propertyName);
            if (userPropValue != null) {
                if (propDesc.isInputColumn()) {
                    List<String> colNames = convertToStringArray(userPropValue);
                    List<InputColumn<?>> inputCols = new ArrayList<>();
                    for (String columnName : colNames) {
                        InputColumn<?> inputCol = getOrCreateInputColumn(columnName, propertyName);
                        inputCols.add(inputCol);
                        configuredInputColumnsList.add(inputCol);
                    }
                    configuredProperties.put(propDesc, inputCols.toArray(new InputColumn[inputCols.size()]));
                } else {
                    Object value = convertPropertyValue(propDesc, userPropValue);
                    configuredProperties.put(propDesc, value);
                }
            }
        }

        configuredInputColumns = configuredInputColumnsList.toArray(new InputColumn[inputColumnsList.size()]);

        config = new ImmutableComponentConfiguration(configuredProperties);

        InjectionManager origInjMan = _dcConfiguration.getEnvironment().getInjectionManagerFactory().getInjectionManager(_dcConfiguration, analysisJob);
        InjectionManager injMan = new ComponentHandlerInjectionManager(origInjMan);
        lifeCycleHelper = new LifeCycleHelper(injMan, false);
        lifeCycleHelper.assignConfiguredProperties(descriptor, component, config);
        lifeCycleHelper.assignProvidedProperties(descriptor, component);
        try {
            lifeCycleHelper.validate(descriptor, component);
        } catch(Exception e) {
            if(e instanceof ComponentValidationException) {
                throw (ComponentValidationException)e;
            }
            throw new ComponentValidationException(descriptor, component, e);
        }
        lifeCycleHelper.initialize(descriptor, component);
    }

    public OutputColumns getOutputColumns() {
        return ((Transformer) component).getOutputColumns();
    }

    public Collection<List<Object[]>> runComponent(JsonNode data, int maxBatchSize) {
        if (data == null) {
            return null;
        }
        if(data.size() > maxBatchSize){
            throw new BatchMaxSizeException(data.size(), maxBatchSize);
        }else if (component instanceof Transformer) {
            return runTransformer(data);
        } else if (component instanceof Analyzer) {
            throw new IllegalArgumentException("Analyzers are not yet implemented");
        } else {
            throw new IllegalArgumentException("Unknown component type " + component.getClass());
        }
    }

    public AnalyzerResult closeComponent() {
        lifeCycleHelper.close(descriptor, component, true);
        if (component instanceof HasAnalyzerResult) {
            return ((HasAnalyzerResult<?>) component).getResult();
        } else {
            return null;
        }
    }

    private Collection<List<Object[]>> runTransformer(JsonNode data) {
        final DataSetHeader header = new SimpleDataSetHeader(table.getColumns());
        final List<Throwable> errors = new ArrayList<>();
        final AtomicInteger tasksPending = new AtomicInteger();
        final TaskRunner taskRunner = _dcConfiguration.getEnvironment().getTaskRunner();

        // Results will be collected in a tree map, sorted by row ID, to return the rows
        // in the same order. It is needed because we do the transformation in threads and
        // results could be computed in different order.
        final Map<Long, List<Object[]>> results = new TreeMap<>();

        final SecurityContext securityContext = SecurityContextHolder.getContext();

        int id = 0;
        for (JsonNode jsonRow : data) {
            final DefaultRow row = new DefaultRow(header, toRowValues((ArrayNode) jsonRow));
            final InputRow inputRow = new MetaModelInputRow(id, row);
            id++;

            if(!errors.isEmpty()) {
                break;
            }

            final Map<String, String> mdcCopy = MDC.getCopyOfContextMap();

            taskRunner.run(new Task() {
                @Override
                public void execute() throws Exception {
                    try {
                        if(mdcCopy != null) {
                            MDC.setContextMap(mdcCopy);
                        }
                        if (!errors.isEmpty()) {
                            LOGGER.debug("Skipping row " + inputRow + " because of previous errors");
                            return;
                        }
                        try {
                            SecurityContextHolder.setContext(securityContext);
                            transform(inputRow, results);
                        } catch (Throwable t) {
                            synchronized (errors) {
                                errors.add(t);
                            }
                        } finally {
                            SecurityContextHolder.clearContext();
                        }
                    } finally {
                        tasksPending.decrementAndGet();
                        MDC.clear();
                    }
                }
            }, null);
            tasksPending.incrementAndGet();
        }

        // Wait for threads
        while(tasksPending.get() > 0) {
            try {
                taskRunner.assistExecution();
            } catch(Throwable t) {
                synchronized (errors) {
                    errors.add(t);
                }
            }
        }

        // Check if there were some errors in the threads
        if(!errors.isEmpty()) {
            Throwable firstError = errors.get(0);
            if(firstError instanceof RuntimeException) {
                throw (RuntimeException)firstError;
            } else {
                throw new RuntimeException(firstError);
            }
        }

        LOGGER.debug("Returning " + results.size() + " rows");
        return results.values();
    }

    /**
     * Thread-safe transformation method that runs transformer for an 'inputRow'
     * and puts a list of output rows to the 'results' map (key is the row ID).
     */
    private void transform(InputRow inputRow, Map<Long, List<Object[]>> results) {
        ThreadLocalOutputListener outputListener = new ThreadLocalOutputListener();

        final Set<ProvidedPropertyDescriptor> outputRowCollectorProperties = descriptor
                .getProvidedPropertiesByType(OutputRowCollector.class);
        try {
            // register output values listener in the transformer row collectors.
            registerOutputListener(outputRowCollectorProperties, outputListener);

            Object[] values = ((Transformer) component).transform(inputRow);
            if(values != null) {
                outputListener.onValues(values);
            }
            synchronized (results) {
                results.put(inputRow.getId(), outputListener.outputRows);
            }
        } finally {
            // unregister output values listener
            unregisterOutputListener(outputRowCollectorProperties);
        }
    }

    private void unregisterOutputListener(Set<ProvidedPropertyDescriptor> outputRowCollectorProperties) {
        if(outputRowCollectorProperties != null && !outputRowCollectorProperties.isEmpty()) {
            for (ProvidedPropertyDescriptor descriptor : outputRowCollectorProperties) {
                OutputRowCollector outputRowCollector = (OutputRowCollector) descriptor.getValue(component);
                if (outputRowCollector instanceof ThreadLocalOutputRowCollector) {
                    ((ThreadLocalOutputRowCollector) outputRowCollector).removeListener();
                }
            }
        }
    }

    private void registerOutputListener(Set<ProvidedPropertyDescriptor> outputRowCollectorProperties, ThreadLocalOutputListener outputListener) {
        if (outputRowCollectorProperties != null && !outputRowCollectorProperties.isEmpty()) {
            for (ProvidedPropertyDescriptor descriptor : outputRowCollectorProperties) {
                OutputRowCollector outputRowCollector = (OutputRowCollector) descriptor.getValue(component);
                if (outputRowCollector instanceof ThreadLocalOutputRowCollector) {
                    ((ThreadLocalOutputRowCollector) outputRowCollector).setListener(outputListener);
                } else {
                    throw new UnsupportedOperationException("Unsupported output row collector type: " + outputRowCollector);
                }
            }
        }
    }

    private InputColumn<?> getOrCreateInputColumn(String columnName, String propertyName) {
        final MutableColumn column = columns.get(columnName);
        if (column == null) {
            throw new IllegalArgumentException("Column '" + columnName + "' specified in property '" + propertyName
                    + "' was not found in table columns specification");
        }
        InputColumn<?> inputColumn = inputColumns.get(columnName);
        if (inputColumn == null) {
            inputColumn = new MetaModelInputColumn(column);
            inputColumns.put(columnName, inputColumn);
        }
        return inputColumn;
    }

    private Object[] toRowValues(ArrayNode row) {
        ArrayList<Object> values = new ArrayList<>();
        int i = 0;
        for (JsonNode value : row) {
            if (i >= table.getColumnCount()) {
                LOGGER.debug("Data contain more columns than specified. Will be ignored.");
                break;
            }
            Column col = table.getColumn(i);
            values.add(convertTableValue(col.getType().getJavaEquivalentClass(), value));
            i++;
        }
        return values.toArray(new Object[values.size()]);
    }

    private Object convertPropertyValue(ConfiguredPropertyDescriptor propDesc, JsonNode value) {
        Class<?> type = propDesc.getType();
        try {
            return convertValue(type, value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot convert property '" + propDesc.getName() + " value ' of type '" + type
                    + "': " + value.toString(), e);
        }
    }

    private Object convertTableValue(Class<?> type, JsonNode value) {
        try {
            return convertValue(type, value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot convert table value of type '" + type + "': " + value.toString(), e);
        }
    }

    private Object convertValue(Class<?> type, JsonNode value) throws IOException {
        if(type == File.class) {
            return getStringConverter().deserialize(value.asText(), type);
        }
        return mapper.readValue(value.traverse(), type);
    }

    private String toString(JsonNode item) {
        if (item.getNodeType() == JsonNodeType.STRING) {
            return ((TextNode) item).textValue();
        } else if (item.isArray() && ((ArrayNode) item).size() < 1) {
            if (((ArrayNode) item).size() == 0) {
                return "";
            } else {
                return toString(((ArrayNode) item).get(0));
            }
        }
        return item.toString();
    }

    private List<String> convertToStringArray(JsonNode json) {
        List<String> result = new ArrayList<>();
        if (json.isArray()) {
            for (JsonNode item : ((ArrayNode) json)) {
                result.add(toString(item));
            }
        } else {
            result.add(toString(json));
        }
        return result;
    }

    protected StringConverter getStringConverter() {
        if(_stringConverter == null) {
            _stringConverter = new StringConverter(_dcConfiguration);
        }
        return _stringConverter;
    }

    private static class ThreadLocalOutputListener implements ThreadLocalOutputRowCollector.Listener {
        private final List<Object[]> outputRows = new ArrayList<>();
        @Override
        public void onValues(Object[] values) {
            outputRows.add(values);
        }
    }

    private class ComponentHandlerAnalysisJob implements AnalysisJob {

        @Override
        public AnalysisJobMetadata getMetadata() {
            return null;
        }

        @Override
        public Datastore getDatastore() {
            return null;
        }

        @Override
        public List<InputColumn<?>> getSourceColumns() {
            return inputColumnsList;
        }

        @Override
        public synchronized List<TransformerJob> getTransformerJobs() {
            return Collections.singletonList((TransformerJob)new ComponentHandlerTransformerJob());
        }

        @Override
        public List<FilterJob> getFilterJobs() {
            return Collections.emptyList();
        }

        @Override
        public List<AnalyzerJob> getAnalyzerJobs() {
            return Collections.emptyList();
        }
    }

    class ComponentHandlerTransformerJob implements TransformerJob {

        private static final long serialVersionUID = 1L;

        @Override
        public TransformerDescriptor<?> getDescriptor() {
            return (TransformerDescriptor<?>)descriptor;
        }

        @Override
        public String getName() {
            return descriptor.getDisplayName();
        }

        @Override
        public Map<String, String> getMetadataProperties() {
            return Collections.emptyMap();
        }

        @Override
        public org.datacleaner.job.ComponentConfiguration getConfiguration() {
            return config;
        }

        @Override
        public ComponentRequirement getComponentRequirement() {
            return null;
        }

        @Override
        public InputColumn<?>[] getInput() {
            return configuredInputColumns;
        }

        @Override
        public InputColumn<?>[] getOutput() {
            // TODO
            return new InputColumn<?>[0];
        }

        @Override
        public OutputDataStreamJob[] getOutputDataStreamJobs() {
            return new OutputDataStreamJob[0];
        }
    }

    private class ComponentHandlerInjectionManager implements InjectionManager {
        InjectionManager delegate;

        ComponentHandlerInjectionManager(InjectionManager delegate) {
            this.delegate = delegate;
        }

        @SuppressWarnings("unchecked")
        public <E> E getInstance(InjectionPoint<E> injectionPoint) {
            E obj;
            final Class<E> baseType = injectionPoint.getBaseType();
            if (baseType == OutputRowCollector.class) {
                obj = (E) new ThreadLocalOutputRowCollector();
            } else if(baseType == ComponentContext.class) {
                obj = (E) componentContext;
            } else {
                obj = delegate.getInstance(injectionPoint);
            }
            return obj;
        }
    }
}
