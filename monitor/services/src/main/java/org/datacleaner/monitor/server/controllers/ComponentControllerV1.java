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
package org.datacleaner.monitor.server.controllers;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.datacleaner.api.HiddenProperty;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.descriptors.AbstractPropertyDescriptor;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.monitor.configuration.ComponentStoreHolder;
import org.datacleaner.monitor.configuration.RemoteComponentsConfiguration;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.server.components.ComponentCache;
import org.datacleaner.monitor.server.components.ComponentCacheConfigWrapper;
import org.datacleaner.monitor.server.components.ComponentHandler;
import org.datacleaner.monitor.server.components.ComponentHandlerFactory;
import org.datacleaner.monitor.server.components.InputRewriterController;
import org.datacleaner.monitor.shared.ComponentNotAllowed;
import org.datacleaner.monitor.shared.ComponentNotFoundException;
import org.datacleaner.restclient.ComponentList;
import org.datacleaner.restclient.ComponentsRestClientUtils;
import org.datacleaner.restclient.CreateInput;
import org.datacleaner.restclient.OutputColumns;
import org.datacleaner.restclient.ProcessInput;
import org.datacleaner.restclient.ProcessOutput;
import org.datacleaner.restclient.ProcessResult;
import org.datacleaner.restclient.ProcessStatelessInput;
import org.datacleaner.restclient.ProcessStatelessOutput;
import org.datacleaner.restclient.Serializator;
import org.datacleaner.util.IconUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;

/**
 * Controller for DataCleaner components (transformers and analyzers). It
 * enables to use a particular component and provide the input data separately
 * without any need of the whole job or datastore configuration.
 * 
 * @since 8. 7. 2015
 */
@Controller
@RequestMapping("/{tenant}/components")
public class ComponentControllerV1 {
    private static final String REMOTE_MARK = "remote-icon-overlay.png";
    private static final Logger logger = LoggerFactory.getLogger(ComponentControllerV1.class);

    private static final String PARAMETER_NAME_TENANT = "tenant";
    private static final String PARAMETER_NAME_ICON_DATA = "iconData";
    private static final String PARAMETER_NAME_OUTPUT_STYLE = "outputStyle";
    private static final String PARAMETER_NAME_ID = "id";
    private static final String PARAMETER_NAME_NAME = "name";
    
    private static final String PARAMETER_VALUE_OUTPUT_STYLE_TABULAR = "tabular";
    private static final String PARAMETER_VALUE_OUTPUT_STYLE_MAP = "map";
    private static final String PARAMETER_VALUE_OUTPUT_STYLE_DOCUMENT = "document";
    
    private static ObjectMapper objectMapper = Serializator.getJacksonObjectMapper();
    private int _maxBatchSize = Integer.MAX_VALUE;

    private InputRewriterController inputRewriterController = new InputRewriterController();

    @Autowired
    TenantContextFactory _tenantContextFactory;
    
    @Autowired
    RemoteComponentsConfiguration _remoteComponentsConfiguration;

    @Autowired
    ComponentHandlerFactory componentHandlerFactory;

    @Autowired
    ComponentCache _componentCache;

    /**
     * It returns a list of all components and their configurations.
     * 
     * @param tenant
     * @param iconData
     * @return
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ComponentList getAllComponents(@PathVariable(PARAMETER_NAME_TENANT) final String tenant,
            @RequestParam(value = PARAMETER_NAME_ICON_DATA, required = false, defaultValue = "false") boolean iconData) {
        DataCleanerConfiguration configuration = _tenantContextFactory.getContext(tenant).getConfiguration();
        Collection<TransformerDescriptor<?>> transformerDescriptors = configuration.getEnvironment()
                .getDescriptorProvider().getTransformerDescriptors();
        ComponentList componentList = new ComponentList();

        for (TransformerDescriptor<?> descriptor : transformerDescriptors) {
            if (_remoteComponentsConfiguration.isAllowed(descriptor)) {
                try {
                    componentList.add(createComponentInfo(tenant, descriptor, iconData));
                } catch(Exception e) {
                    logger.error("Cannot create info about component {}", descriptor, e);
                }
            }
        }

        logger.debug("Informing about {} components", componentList.getComponents().size());
        return componentList;
    }

    @ResponseBody
    @RequestMapping(value = "/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ComponentList.ComponentInfo getComponentInfo(@PathVariable(PARAMETER_NAME_TENANT) final String tenant,
            @PathVariable(PARAMETER_NAME_NAME) String name,
            @RequestParam(value = PARAMETER_NAME_ICON_DATA, required = false, defaultValue = "false") boolean iconData) {
        name = ComponentsRestClientUtils.unescapeComponentName(name);
        logger.debug("Informing about '" + name + "'");
        DataCleanerConfiguration dcConfig = _tenantContextFactory.getContext(tenant).getConfiguration();
        ComponentDescriptor<?> descriptor = dcConfig.getEnvironment().getDescriptorProvider().getTransformerDescriptorByDisplayName(name);
        if (!_remoteComponentsConfiguration.isAllowed(descriptor)) {
            logger.info("Component {} is not allowed.", name);
            throw ComponentNotAllowed.createInstanceNotAllowed(name);
        }
        return createComponentInfo(tenant, descriptor, iconData);
    }

    /**
     * Returns output columns specification, based on the provided configuration
     */
    @ResponseBody
    @RequestMapping(value = "/{name}/_outputColumns", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OutputColumns getOutputColumns(@PathVariable(PARAMETER_NAME_TENANT) final String tenant,
            @PathVariable(PARAMETER_NAME_NAME) final String name, @RequestBody final CreateInput createInput) {
        String decodedName = ComponentsRestClientUtils.unescapeComponentName(name);
        logger.debug("Informing about output columns of '{}'", decodedName);
        TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        ComponentHandler handler = componentHandlerFactory.createComponent(tenantContext, decodedName, createInput.configuration);
        try {
            org.datacleaner.api.OutputColumns outCols = handler.getOutputColumns();
            org.datacleaner.restclient.OutputColumns result = new org.datacleaner.restclient.OutputColumns();

            for (int i = 0; i < outCols.getColumnCount(); i++) {
                SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
                try {
                    ComponentHandler.mapper.acceptJsonFormatVisitor(outCols.getColumnType(i), visitor);
                } catch (JsonMappingException e) {
                    throw new RuntimeException(e);
                }
                result.add(outCols.getColumnName(i), outCols.getColumnType(i), visitor.finalSchema());
            }
            return result;
        } finally {
            handler.closeComponent();
        }
    }

    /**
     * It creates a new component with the provided configuration, runs it and
     * returns the result.
     * 
     * @param tenant
     * @param name
     * @param processStatelessInput
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/{name}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProcessStatelessOutput processStateless(@PathVariable(PARAMETER_NAME_TENANT) final String tenant,
            @PathVariable(PARAMETER_NAME_NAME) final String name,
            @RequestParam(value = PARAMETER_NAME_OUTPUT_STYLE, required = false, defaultValue = PARAMETER_VALUE_OUTPUT_STYLE_TABULAR) String outputStyle,
            @RequestBody final ProcessStatelessInput processStatelessInput) {
        String decodedName = ComponentsRestClientUtils.unescapeComponentName(name);
        logger.debug("One-shot processing '{}'", decodedName);
        TenantContext tenantContext = _tenantContextFactory.getContext(tenant);

        // try to enhance the input in case the client uses simplified input format\
        ComponentDescriptor<?> compDesc = componentHandlerFactory.resolveDescriptor(tenantContext.getConfiguration().getEnvironment(), decodedName);
        inputRewriterController.rewriteStatelessInput(compDesc, processStatelessInput);

        ComponentHandler handler = componentHandlerFactory.createComponent(tenantContext, decodedName, processStatelessInput.configuration);
        ProcessStatelessOutput output = new ProcessStatelessOutput();
        OutputStyle outputStyleEnum = OutputStyle.forString(outputStyle);
        output.rows = getOutputJsonNode(handler, handler.runComponent(processStatelessInput.data, _maxBatchSize), outputStyleEnum);
        output.result = getJsonNode(handler.closeComponent());

        return output;
    }

    private JsonNode getOutputJsonNode(ComponentHandler handler, Collection<List<Object[]>> data, OutputStyle outputFormat) {
        if(outputFormat == OutputStyle.MAP) {
            org.datacleaner.api.OutputColumns columns = handler.getOutputColumns();
            int columnCount = columns.getColumnCount();
            List<List<Map<String, Object>>> mapStyleOutput = new ArrayList<>(data.size());
            for(List<Object[]> rowGroup: data) {
                List<Map<String, Object>> columnMapRowGroup = new ArrayList<>(rowGroup.size());
                for(Object[] row: rowGroup) {
                    Map<String, Object> columMapRow = new HashMap<>(columnCount);
                    for(int i = 0; i < columnCount; i++) {
                        columMapRow.put(columns.getColumnName(i), row[i]);
                    }
                    columnMapRowGroup.add(columMapRow);
                }
                mapStyleOutput.add(columnMapRowGroup);
            }
            return getJsonNode(mapStyleOutput);
        } else {
            return getJsonNode(data);
        }
    }

    private static JsonNode getJsonNode(Object value) {
        if (value == null) {
            return null;
        }
        return objectMapper.valueToTree(value);
    }

    /**
     * It runs the component and returns the results.
     */
    @ResponseBody
    @RequestMapping(value = "/{name}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public String createComponent(@PathVariable(PARAMETER_NAME_TENANT) final String tenant,
            @PathVariable(PARAMETER_NAME_NAME) final String name, // 1 day
            @RequestParam(value = "timeout", required = false, defaultValue = "86400000") final String timeout,
            @RequestBody final CreateInput createInput) {
        String decodedName = ComponentsRestClientUtils.unescapeComponentName(name);
        TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        String id = UUID.randomUUID().toString();
        long longTimeout = Long.parseLong(timeout);
        _componentCache.put(tenant, tenantContext, new ComponentStoreHolder(longTimeout, createInput, id, decodedName));
        return id;
    }

    /**
     * It returns the continuous result of the component for the provided input
     * data.
     */
    @ResponseBody
    @RequestMapping(value = "/_instance/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProcessOutput processComponent(@PathVariable(PARAMETER_NAME_TENANT) final String tenant,
            @PathVariable(PARAMETER_NAME_ID) final String id, @RequestBody final ProcessInput processInput)
                    throws ComponentNotFoundException {
        TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        ComponentCacheConfigWrapper config = _componentCache.get(id, tenant, tenantContext);
        if (config == null) {
            logger.warn("Component with id {} does not exist.", id);
            throw ComponentNotFoundException.createInstanceNotFound(id);
        }
        ComponentHandler handler = config.getHandler();
        ProcessOutput out = new ProcessOutput();
        out.rows = handler.runComponent(processInput.data, _maxBatchSize);
        return out;
    }

    /**
     * It returns the component's final result.
     */
    @ResponseBody
    @RequestMapping(value = "/{id}/result", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProcessResult getFinalResult(@PathVariable(PARAMETER_NAME_TENANT) final String tenant,
            @PathVariable(PARAMETER_NAME_ID) final String id) throws ComponentNotFoundException {
        // TODO - only for analyzers, implement it later after the architecture
        // decisions regarding the load-balancing and failover.
        return null;
    }

    /**
     * It deletes the component.
     */
    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteComponent(@PathVariable(PARAMETER_NAME_TENANT) final String tenant,
            @PathVariable(PARAMETER_NAME_ID) final String id) throws ComponentNotFoundException {
        TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        boolean isHere = _componentCache.remove(id, tenantContext);
        if (!isHere) {
            logger.warn("Instance of component {} not found in the cache and in the store", id);
            throw ComponentNotFoundException.createInstanceNotFound(id);
        }
    }

    public void setMaxBatchSize(int maxBatchSize){
        _maxBatchSize = maxBatchSize;
    }

    public static ComponentList.ComponentInfo createComponentInfo(String tenant, ComponentDescriptor<?> descriptor,
            boolean iconData) {
        Object componentInstance = descriptor.newInstance();
        ComponentList.ComponentInfo componentInfo = new ComponentList.ComponentInfo()
                .setName(descriptor.getDisplayName())
                .setCreateURL(getURLForCreation(tenant, descriptor))
                .setProperties(createPropertiesInfo(descriptor, componentInstance));
        setComponentAnnotations(descriptor, componentInfo);
        if (iconData) {
            componentInfo.setIconData(getComponentIconData(descriptor));
        }

        return componentInfo;
    }

    private static byte[] getComponentIconData(ComponentDescriptor<?> descriptor) {
        try {
            String iconImagePath = IconUtils.getImagePathForClass(descriptor.getComponentClass());
            InputStream iconStream = descriptor.getComponentClass().getClassLoader().getResourceAsStream(iconImagePath);
            Image icon = ImageIO.read(iconStream);
            int maxSize = IconUtils.ICON_SIZE_LARGE;
            int iconW = icon.getWidth(null);
            int iconH = icon.getHeight(null);
            if(iconW > maxSize || iconW > maxSize) {
                double scaleX = ((double)maxSize)/(double)icon.getWidth(null);
                double scaleY = ((double)maxSize)/(double)icon.getWidth(null);
                double scale = Math.min(scaleX, scaleY);
                iconH = (int)((double)iconH * scale);
                iconW = (int)((double)iconW * scale);
                icon = icon.getScaledInstance(iconW, iconH, Image.SCALE_SMOOTH);
            }

            BufferedImage resultImg = new BufferedImage(maxSize, maxSize, BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = resultImg.getGraphics();
            graphics.drawImage(icon, (maxSize-iconW)/2, (maxSize-iconH)/2, null);
            graphics.dispose();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(resultImg, "png", outputStream);
            outputStream.flush();
            byte[] iconBytes = outputStream.toByteArray();
            outputStream.close();

            return iconBytes;
        } catch (NullPointerException | IOException e) {
            logger.warn("Component icon data can not be provided. " + e.getMessage());

            return new byte[0];
        }
    }


    static private String getURLForCreation(String tenant, ComponentDescriptor<?> descriptor) {
        try {
            return String.format("/repository/%s/components/%s", UriUtils.encodePathSegment(tenant, "UTF8"),
                    UriUtils.encodePathSegment(
                            ComponentsRestClientUtils.escapeComponentName(descriptor.getDisplayName()), "UTF8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, ComponentList.PropertyInfo> createPropertiesInfo(ComponentDescriptor<?> descriptor,
            Object componentInstance) {
        Map<String, ComponentList.PropertyInfo> result = new HashMap<>();
        for (ConfiguredPropertyDescriptor propertyDescriptor : (Set<ConfiguredPropertyDescriptor>) descriptor.getConfiguredProperties()) {
            final HiddenProperty hiddenProperty = propertyDescriptor.getAnnotation(HiddenProperty.class);
            if (hiddenProperty != null && hiddenProperty.hiddenForRemoteAccess()) {
                continue;
            }
            ComponentList.PropertyInfo propInfo = new ComponentList.PropertyInfo();
            propInfo.setName(propertyDescriptor.getName());
            propInfo.setDescription(propertyDescriptor.getDescription());
            propInfo.setRequired(propertyDescriptor.isRequired());
            propInfo.setIsInputColumn(propertyDescriptor.isInputColumn());
            setPropertyType(descriptor, propertyDescriptor, propInfo);
            setPropertyAnnotations(propertyDescriptor, propInfo);
            result.put(propInfo.getName(), propInfo);
            Object defaultValue = propertyDescriptor.getValue(componentInstance);
            if (defaultValue != null) {
                propInfo.setDefaultValue(objectMapper.valueToTree(defaultValue));
            }
        }
        return result;
    }

    private static Map<String, Map<String, Object>> getAnnotationMap(Set<Annotation> annotations, String objectName){
        Map<String, Map<String, Object>> annotationMap = new HashMap<>();
        for (Annotation an : annotations) {
            boolean addAnnotationToMap = true;
            Class<?> anClass = an.annotationType();
            Map<String, Object> anValues = new HashMap<>();
            for (Method anMethod : anClass.getDeclaredMethods()) {
                try {
                    if (anMethod.getParameterTypes().length == 0) {
                        Object anValue = anMethod.invoke(an, new Object[0]);
                        if(!isAllowedValue(anValue)){
                            addAnnotationToMap = false;
                            break;
                        }
                        if (anValue != null) {
                            anValues.put(anMethod.getName(), anValue);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Cannot provide property '{}' annotation", objectName, e);
                }
            }
            if(addAnnotationToMap) {
                annotationMap.put(anClass.getName(), anValues);
            }
        }
        return annotationMap;
    }

    private static boolean isAllowedValue(final Object anValue) {
        if (anValue == null) {
            return true;
        }
        try {
            Serializator.getJacksonObjectMapper().writeValueAsString(anValue);
        } catch (JsonProcessingException e) {
            return false;
        }
        return true;
    }

    private static void setPropertyAnnotations(ConfiguredPropertyDescriptor propertyDescriptor,
            ComponentList.PropertyInfo propInfo) {
        Set<Annotation> annotations = propertyDescriptor.getAnnotations();
        if (annotations == null) {
            return;
        }
        propInfo.setAnnotations(getJsonNode(getAnnotationMap(annotations, propertyDescriptor.getName())));
    }

    private static void setComponentAnnotations(ComponentDescriptor<?> componentDescriptor,
            ComponentList.ComponentInfo componentInfo) {
        Set<Annotation> annotations = componentDescriptor.getAnnotations();
        if (annotations == null) {
            return;
        }
        componentInfo.setAnnotations(getJsonNode(getAnnotationMap(annotations, componentDescriptor.getDisplayName())));
    }

    static void setPropertyType(ComponentDescriptor<?> descriptor, ConfiguredPropertyDescriptor propertyDescriptor,
            ComponentList.PropertyInfo propInfo) {
        // TODO: avoid instanceof by extending the basic ComponentDescriptor
        // interface (maybe add getter for property "Type" in addition to
        // "Class" ? )

        SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();

        if (propertyDescriptor instanceof AbstractPropertyDescriptor) {
            Field f = ((AbstractPropertyDescriptor) propertyDescriptor).getField();
            Type t = f.getGenericType();
            if (t instanceof Class) {
                propInfo.setClassDetails(((Class<?>) t).getCanonicalName());
            } else {
                propInfo.setClassDetails(f.getGenericType().toString());
            }
            if (!propertyDescriptor.isInputColumn()) {
                try {
                    ComponentHandler.mapper.acceptJsonFormatVisitor(
                            ComponentHandler.mapper.constructType(f.getGenericType()), visitor);
                } catch (JsonMappingException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            propInfo.setClassDetails(propertyDescriptor.getType().getCanonicalName());
            if (!propertyDescriptor.isInputColumn()) {
                try {
                    ComponentHandler.mapper.acceptJsonFormatVisitor(
                            ComponentHandler.mapper.constructType(propertyDescriptor.getType()), visitor);
                } catch (JsonMappingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        propInfo.setClassName(propertyDescriptor.getType().getName());
        if (!propertyDescriptor.isInputColumn()) {
            propInfo.setSchema(visitor.finalSchema());
        }
    }

    public enum OutputStyle {

        TABULAR,
        MAP;

        public static OutputStyle forString(String outputStyle) {
            if (outputStyle == null) {
                return TABULAR;
            }
            outputStyle = outputStyle.trim().toLowerCase();
            switch (outputStyle) {
            case "":
            case PARAMETER_VALUE_OUTPUT_STYLE_TABULAR:
                return TABULAR;
            case PARAMETER_VALUE_OUTPUT_STYLE_DOCUMENT:
            case PARAMETER_VALUE_OUTPUT_STYLE_MAP:
                return MAP;
            default:
                throw new IllegalArgumentException("Unknown outputStyle '" + outputStyle + "'");
            }
        }
    }
}
