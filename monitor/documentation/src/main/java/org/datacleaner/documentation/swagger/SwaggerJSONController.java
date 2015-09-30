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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private Reflections reflections = new Reflections(SwaggerJSONController.CONTROLLERS_PACKAGE);
    private Class serviceClass = null;
    private String serviceUrlPrefix = null;
    private SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration();

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SwaggerConfiguration generateSwaggerJSON(HttpServletRequest httpServletRequest) {
        swaggerConfiguration.setHost(getCurrentHost(httpServletRequest));
        swaggerConfiguration.setBasePath(SwaggerJSONController.BASE_PATH);
        Set<Class<?>> controllerClasses = reflections.getTypesAnnotatedWith(Controller.class);

        for (Class<?> clazz : controllerClasses) {
            serviceClass = clazz;
            addClassServices();
        }

        return swaggerConfiguration;
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
            addSwaggerMethod(method);
        }

        swaggerConfiguration.getTags().add(createSwaggerTag(classRequestMapping));
    }

    private SwaggerTag createSwaggerTag(RequestMapping classRequestMapping) {
        String[] values = classRequestMapping.value();
        String serviceName = "default";

        if (values.length > 0) {
            serviceName = values[0];
        }

        SwaggerTag swaggerTag = new SwaggerTag();
        swaggerTag.setName(serviceName);

        return swaggerTag;
    }

    private void addSwaggerMethod(Method method) {
        RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);

        if (methodRequestMapping == null) {
            return;
        }

        String methodUrl = "";
        String[] values = methodRequestMapping.value();

        if (values.length > 0) {
            methodUrl = values[0];
        }

        String url = serviceUrlPrefix + methodUrl;
        swaggerConfiguration.getPaths().put(url, new HashMap<String, SwaggerMethod>());

        for (RequestMethod requestMethod : methodRequestMapping.method()) {
            SwaggerMethod swaggerMethod = createSwaggerMethod(methodRequestMapping, method);
            String httpMethodName = requestMethod.name().toLowerCase();

            if (httpMethodName.isEmpty()) {
                logger.warn("HTTP method of a service method is empty ({}). ", method.getName());
                continue;
            }

            swaggerConfiguration.getPaths().get(url).put(httpMethodName, swaggerMethod);
        }
    }

    private SwaggerMethod createSwaggerMethod(RequestMapping methodRequestMapping, Method method) {
        SwaggerMethod swaggerMethod = new SwaggerMethod();

        swaggerMethod.setOperationId(method.getName());
        swaggerMethod.setDescription(methodRequestMapping.name());
        swaggerMethod.setConsumes(methodRequestMapping.consumes());
        swaggerMethod.setProduces(methodRequestMapping.produces());
        swaggerMethod.setParameters(createSwaggerParameterArray(method));

        return swaggerMethod;
    }

    private SwaggerParameter[] createSwaggerParameterArray(Method method) {
        List<SwaggerParameter> swaggerParameterList = new ArrayList<>();

        for (Parameter parameter : method.getParameters()) {
            SwaggerParameter swaggerParameter = getSwaggerParameter(parameter);

            if (swaggerParameter != null) {
                swaggerParameterList.add(swaggerParameter);
            }
        }

        return swaggerParameterList.toArray(new SwaggerParameter[swaggerParameterList.size()]);
    }

    private SwaggerParameter getSwaggerParameter(Parameter parameter) {
        SwaggerParameter swaggerParameter = null;
        RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
        RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
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
            swaggerParameter.setTypeByClass(parameter.getType());
        }

        return swaggerParameter;
    }
}
