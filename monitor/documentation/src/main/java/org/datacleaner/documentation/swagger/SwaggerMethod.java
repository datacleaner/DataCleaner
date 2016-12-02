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
public class SwaggerMethod {
    private List<String> tags = new ArrayList<>();
    private String summary = "";
    private String description = "";
    private String operationId = "";
    private String[] consumes = new String[] {};
    private String[] produces = new String[] {};
    private SwaggerParameter[] parameters = new SwaggerParameter[] {};
    private Map<String, SwaggerResponse> responses = new HashMap<>();

    public List<String> getTags() {
        return tags;
    }

    public void setTags(final List<String> tags) {
        this.tags = tags;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(final String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(final String operationId) {
        this.operationId = operationId;
    }

    public String[] getConsumes() {
        return consumes;
    }

    public void setConsumes(final String[] consumes) {
        this.consumes = consumes;
    }

    public String[] getProduces() {
        return produces;
    }

    public void setProduces(final String[] produces) {
        this.produces = produces;
    }

    public SwaggerParameter[] getParameters() {
        return parameters;
    }

    public void setParameters(final SwaggerParameter[] parameters) {
        this.parameters = parameters;
    }

    public Map<String, SwaggerResponse> getResponses() {
        return responses;
    }

    public void setResponses(final Map<String, SwaggerResponse> responses) {
        this.responses = responses;
    }
}
