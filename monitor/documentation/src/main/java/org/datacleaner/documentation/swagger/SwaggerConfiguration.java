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
    private SwaggerSecurityDefinitions securityDefinitions = new SwaggerSecurityDefinitions();
    private SwaggerDefinitions definitions = new SwaggerDefinitions();
    private SwaggerExternalDocs externalDocs = new SwaggerExternalDocs();

    public String getSwagger() {
        return swagger;
    }

    public void setSwagger(String swagger) {
        this.swagger = swagger;
    }

    public SwaggerInfo getInfo() {
        return info;
    }

    public void setInfo(SwaggerInfo info) {
        this.info = info;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String[] getSchemes() {
        return schemes;
    }

    public void setSchemes(String[] schemes) {
        this.schemes = schemes;
    }

    public List<SwaggerTag> getTags() {
        return tags;
    }

    public void setTags(List<SwaggerTag> tags) {
        this.tags = tags;
    }

    public Map<String, Map<String, SwaggerMethod>> getPaths() {
        return paths;
    }

    public void setPaths(Map<String, Map<String, SwaggerMethod>> paths) {
        this.paths = paths;
    }

    public SwaggerSecurityDefinitions getSecurityDefinitions() {
        return securityDefinitions;
    }

    public void setSecurityDefinitions(SwaggerSecurityDefinitions securityDefinitions) {
        this.securityDefinitions = securityDefinitions;
    }

    public SwaggerDefinitions getDefinitions() {
        return definitions;
    }

    public void setDefinitions(SwaggerDefinitions definitions) {
        this.definitions = definitions;
    }

    public SwaggerExternalDocs getExternalDocs() {
        return externalDocs;
    }

    public void setExternalDocs(SwaggerExternalDocs externalDocs) {
        this.externalDocs = externalDocs;
    }
}
