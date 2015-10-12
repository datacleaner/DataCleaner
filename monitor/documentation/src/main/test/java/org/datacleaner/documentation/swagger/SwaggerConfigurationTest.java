package org.datacleaner.documentation.swagger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class SwaggerConfigurationTest {
    private final SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration();

    @Test
    public void testSetAndGetSwagger() throws Exception {
        Assert.assertNotNull(swaggerConfiguration.getSwagger());
        final String value = "2.0";
        swaggerConfiguration.setSwagger(value);
        Assert.assertEquals(value, swaggerConfiguration.getSwagger());
    }

    @Test
    public void testSetAndGetInfo() throws Exception {
        Assert.assertNotNull(swaggerConfiguration.getInfo());
        final SwaggerInfo info = new SwaggerInfo();
        final String title = "title";
        info.setTitle(title);
        swaggerConfiguration.setInfo(info);
        Assert.assertEquals(title, swaggerConfiguration.getInfo().getTitle());
    }

    @Test
    public void testSetAndGetHost() throws Exception {
        Assert.assertNotNull(swaggerConfiguration.getHost());
        final String value = "http://localhost";
        swaggerConfiguration.setHost(value);
        Assert.assertEquals(value, swaggerConfiguration.getHost());
    }

    @Test
    public void testSetAndGetBasePath() throws Exception {
        Assert.assertNotNull(swaggerConfiguration.getBasePath());
        final String value = "/repository";
        swaggerConfiguration.setBasePath(value);
        Assert.assertEquals(value, swaggerConfiguration.getBasePath());
    }

    @Test
    public void testSetAndGetSchemes() throws Exception {
        Assert.assertNotNull(swaggerConfiguration.getSchemes());
        final String[] values = new String[] { "http", "https" };
        swaggerConfiguration.setSchemes(values);
        Assert.assertEquals(values[0], swaggerConfiguration.getSchemes()[0]);
    }

    @Test
    public void testSetAndGetTags() throws Exception {
        Assert.assertNotNull(swaggerConfiguration.getTags());
        final SwaggerTag swaggerTag = new SwaggerTag();
        final String name = "name";
        swaggerTag.setName(name);
        final List<SwaggerTag> list = new ArrayList<>();
        list.add(swaggerTag);
        swaggerConfiguration.setTags(list);
        Assert.assertEquals(name, swaggerConfiguration.getTags().get(0).getName());
    }

    @Test
    public void testSetAndGetPaths() throws Exception {
        Assert.assertNotNull(swaggerConfiguration.getPaths());
        final Map<String, Map<String, SwaggerMethod>> paths = new HashMap<>();
        final String url = "/service";
        final Map<String, SwaggerMethod> methods = new HashMap<>();
        final String httpMethod = "GET";
        final SwaggerMethod swaggerMethod = new SwaggerMethod();
        final String description = "description of the service";
        swaggerMethod.setDescription(description);
        methods.put(httpMethod, swaggerMethod);
        paths.put(url, methods);
        swaggerConfiguration.setPaths(paths);
        Assert.assertEquals(description, swaggerConfiguration.getPaths().get(url).get(httpMethod).getDescription());
    }

    @Test
    public void testSetAndGetSecurityDefinitions() throws Exception {
        Assert.assertNotNull(swaggerConfiguration.getSecurityDefinitions());
        final SwaggerSecurityDefinitions swaggerSecurityDefinitions = new SwaggerSecurityDefinitions();
        final String[] list = new String[] { "securityDefinition1", "securityDefinition2" };
        swaggerSecurityDefinitions.setList(list);
        swaggerConfiguration.setSecurityDefinitions(swaggerSecurityDefinitions);
        Assert.assertEquals(list[0], swaggerConfiguration.getSecurityDefinitions().getList()[0]);
    }

    @Test
    public void testSetAndGetDefinitions() throws Exception {
        Assert.assertNotNull(swaggerConfiguration.getDefinitions());
        final SwaggerDefinitions swaggerDefinitions = new SwaggerDefinitions();
        final String[] list = new String[] { "definition1", "definition2" };
        swaggerDefinitions.setList(list);
        swaggerConfiguration.setDefinitions(swaggerDefinitions);
        Assert.assertEquals(list[0], swaggerConfiguration.getDefinitions().getList()[0]);
    }

    @Test
    public void testSetAndGetExternalDocs() throws Exception {
        Assert.assertNotNull(swaggerConfiguration.getExternalDocs());
        final SwaggerExternalDocs swaggerExternalDocs = new SwaggerExternalDocs();
        final String url = "http://localhost/documentation";
        swaggerExternalDocs.setUrl(url);
        swaggerConfiguration.setExternalDocs(swaggerExternalDocs);
        Assert.assertEquals(url, swaggerConfiguration.getExternalDocs().getUrl());
    }
}