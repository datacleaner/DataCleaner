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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 23. 09. 2015
 */
public class SwaggerConfiguration {
    private String swagger = "2.0";
    private SwaggerInfo info = new SwaggerInfo();
    private String host = "";
    private String basePath = "";
    private String[] schemes = new String[] { "http" };
    private List<SwaggerTag> tags = new ArrayList<>();
    private Map<String, Map<String, SwaggerMethod>> paths = new HashMap<>();
    private Map<String, Map<String, Object>> securityDefinitions = new HashMap<>();
    private Map<String, SwaggerSchema> definitions = new HashMap<>();
    private SwaggerExternalDocs externalDocs = new SwaggerExternalDocs();
    private List<Map<String, String[]>> security = new ArrayList<>();

    public List<Map<String, String[]>> getSecurity() {
        return security;
    }

    public String getSwagger() {
        return swagger;
    }

    public void setSwagger(final String swagger) {
        this.swagger = swagger;
    }

    public SwaggerInfo getInfo() {
        return info;
    }

    public void setInfo(final SwaggerInfo info) {
        this.info = info;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(final String basePath) {
        this.basePath = basePath;
    }

    public String[] getSchemes() {
        return schemes;
    }

    public void setSchemes(final String[] schemes) {
        this.schemes = schemes;
    }

    public List<SwaggerTag> getTags() {
        return tags;
    }

    public void setTags(final List<SwaggerTag> tags) {
        this.tags = tags;
    }

    public Map<String, Map<String, SwaggerMethod>> getPaths() {
        return paths;
    }

    public void setPaths(final Map<String, Map<String, SwaggerMethod>> paths) {
        this.paths = paths;
    }

    public Map<String, Map<String, Object>> getSecurityDefinitions() {
        return securityDefinitions;
    }

    public void addSecurityDefinition(final String key, final Map<String, Object> securityDef) {
        securityDefinitions.put(key, securityDef);
    }

    public Map<String, SwaggerSchema> getDefinitions() {
        return definitions;
    }

    public void addDefinition(final String typeName, final SwaggerSchema schema) {
        definitions.put(typeName, schema);
    }

    public SwaggerExternalDocs getExternalDocs() {
        return externalDocs;
    }

    public void setExternalDocs(final SwaggerExternalDocs externalDocs) {
        this.externalDocs = externalDocs;
    }
}
