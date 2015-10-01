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
package org.datacleaner.documentation.swagger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Own implementation of Swagger JSON provider.
 */
@Controller
@RequestMapping("/swagger.json")
public class SwaggerJSONController {
    private static final String CONTROLLERS_PACKAGE = "org.datacleaner.monitor.server.controllers";
    private static final String BASE_PATH = "/repository";
    private static final Logger logger = LoggerFactory.getLogger(SwaggerJSONController.class);
    private Class serviceClass = null;
    private String serviceUrlPrefix = null;
    private SwaggerConfiguration swaggerConfiguration = null;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SwaggerConfiguration generateSwaggerJSON(HttpServletRequest httpServletRequest) {
        swaggerConfiguration = new SwaggerConfiguration();
        swaggerConfiguration.setHost(getCurrentHost(httpServletRequest));
        swaggerConfiguration.setBasePath(SwaggerJSONController.BASE_PATH);
        Set<Class<?>> controllerClasses = getControllerClasses();

        for (Class<?> clazz : controllerClasses) {
            serviceClass = clazz;
            addClassServices();
        }

        return swaggerConfiguration;
    }

    private Set<Class<?>> getControllerClasses() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));
        Set<Class<?>> controllerClasses = new HashSet<>();

        for (BeanDefinition bd : scanner.findCandidateComponents(SwaggerJSONController.CONTROLLERS_PACKAGE)) {
            try {
                String className = bd.getBeanClassName();
                controllerClasses.add(Class.forName(className));
            }
            catch (ClassNotFoundException e) {
                continue;
            }
        }

        return controllerClasses;
    }

    private String getCurrentHost(HttpServletRequest httpServletRequest) {
        String thisURL = httpServletRequest.getRequestURL().toString();
        String protocolSeparator = "://";
        String host = thisURL.substring(
                thisURL.indexOf(protocolSeparator) + protocolSeparator.length(),
                thisURL.indexOf(SwaggerJSONController.BASE_PATH));

        return host;
    }

    private void addClassServices() {
        RequestMapping classRequestMapping = (RequestMapping) serviceClass.getAnnotation(RequestMapping.class);

        if (classRequestMapping == null) {
            return;
        }

        serviceUrlPrefix = classRequestMapping.value()[0];

        for (Method method : serviceClass.getMethods()) {
            addMethod(method);
        }

        swaggerConfiguration.getTags().add(createSwaggerTag());
    }

    private SwaggerTag createSwaggerTag() {
        SwaggerTag swaggerTag = new SwaggerTag();
        swaggerTag.setName(serviceClass.getSimpleName());

        return swaggerTag;
    }

    private void addMethod(Method method) {
        RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);

        if (methodRequestMapping == null) {
            return;
        }

        String url = getMethodURL(methodRequestMapping.value());

        if (! swaggerConfiguration.getPaths().containsKey(url)) {
            Map<String, SwaggerMethod> swaggerMethods = new HashMap<>();
            swaggerConfiguration.getPaths().put(url, swaggerMethods);
        }

        addHTTPMethods(url, method);
    }

    private String getMethodURL(String[] methodRequestMappingValues) {
        String methodUrl = "";

        if (methodRequestMappingValues.length > 0) {
            methodUrl = methodRequestMappingValues[0];
        }

        return serviceUrlPrefix + methodUrl;
    }

    private void addHTTPMethods(String url, Method method) {
        RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
        Map<String, SwaggerMethod> swaggerMethods = swaggerConfiguration.getPaths().get(url);

        for (RequestMethod requestMethod : methodRequestMapping.method()) {
            SwaggerMethod swaggerMethod = createSwaggerMethod(methodRequestMapping, method);
            String httpMethodName = requestMethod.name().toLowerCase();

            if (httpMethodName.isEmpty()) {
                logger.warn("HTTP method of a service method is empty ({}). ", method.getName());
                continue;
            }

            swaggerMethods.put(httpMethodName, swaggerMethod);
        }
    }

    private SwaggerMethod createSwaggerMethod(RequestMapping methodRequestMapping, Method method) {
        SwaggerMethod swaggerMethod = new SwaggerMethod();

        swaggerMethod.setOperationId(method.getName());
        swaggerMethod.setDescription(methodRequestMapping.name());
        swaggerMethod.setConsumes(methodRequestMapping.consumes());
        swaggerMethod.setProduces(methodRequestMapping.produces());
        swaggerMethod.setParameters(createSwaggerParameterArray(method));
        swaggerMethod.getTags().add(serviceClass.getSimpleName());

        return swaggerMethod;
    }

    private SwaggerParameter[] createSwaggerParameterArray(Method method) {
        List<SwaggerParameter> swaggerParameterList = new ArrayList<>();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class[] parameterTypes = method.getParameterTypes();

        for (int i = 0; i < parameterTypes.length; i++) {
            SwaggerParameter swaggerParameter = getSwaggerParameter(parameterTypes[i], parameterAnnotations[i]);

            if (swaggerParameter != null) {
                swaggerParameterList.add(swaggerParameter);
            }
        }

        return swaggerParameterList.toArray(new SwaggerParameter[swaggerParameterList.size()]);
    }

    private SwaggerParameter getSwaggerParameter(Class parameterType, Annotation[] parameterAnnotations) {
        SwaggerParameter swaggerParameter = null;
        RequestBody requestBody = (RequestBody) findAnnotation(parameterAnnotations, RequestBody.class);
        RequestParam requestParam = (RequestParam) findAnnotation(parameterAnnotations, RequestParam.class);
        PathVariable pathVariable = (PathVariable) findAnnotation(parameterAnnotations, PathVariable.class);
        String parameterName = "";

        if (requestBody != null) {
            swaggerParameter = new SwaggerParameter();
            swaggerParameter.setRequired(requestBody.required());
            swaggerParameter.setIn(SwaggerParameter.In.BODY.getValue());
            parameterName = "body";
        }
        else if (requestParam != null) {
            swaggerParameter = new SwaggerParameter();
            swaggerParameter.setRequired(requestParam.required());
            swaggerParameter.setIn(SwaggerParameter.In.QUERY.getValue());
            parameterName = requestParam.value();
        }
        else if (pathVariable != null) {
            swaggerParameter = new SwaggerParameter();
            swaggerParameter.setRequired(true);
            swaggerParameter.setIn(SwaggerParameter.In.PATH.getValue());
            parameterName = pathVariable.value();
        }

        if (swaggerParameter != null) {
            swaggerParameter.setName(parameterName);
            swaggerParameter.setTypeByClass(parameterType);
        }

        return swaggerParameter;
    }

    private Annotation findAnnotation(Annotation[] allAnnotations, Class requiredType) {
        for (Annotation annotation : allAnnotations) {
            if (annotation.annotationType().equals(requiredType)) {
                return annotation;
            }
        }

        return null;
    }
}
