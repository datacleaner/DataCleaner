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
package org.datacleaner.restclient;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @since 02. 09. 2015
 */
public class ComponentRESTClient {
    
    private final RESTClient restClient;
    private String tenantName;
    private String url;

    public ComponentRESTClient(String url, String username, String password) {
        this.url = url;
        restClient = new RESTClientImpl(username, password);
        getUserTenantName();
    }

    /** Mainly for tests, tenant can be recognized automatically when other constructor is used */
    public ComponentRESTClient(String url, String username, String password, String tenantName) {
        this.url = url;
        restClient = new RESTClientImpl(username, password);
        this.tenantName = tenantName;
    }

    public ComponentList getAllComponents(final boolean iconData) {
        String response = call(RESTClient.HttpMethod.GET, getURL("?iconData=" + iconData), "");

        return Serializator.componentList(response);
    }

    public ComponentList.ComponentInfo getComponentInfo( String componentName, boolean iconData) {
        componentName = urlify(componentName);
        String response = call(RESTClient.HttpMethod.GET, getURL(componentName + "&iconData=" + iconData), "");

        return Serializator.componentInfo(response);
    }

    public OutputColumns getOutputColumns(String componentName, CreateInput config) {
        componentName = urlify(componentName);
        String configuration = Serializator.stringCreateInput(config);
        String response = call(RESTClient.HttpMethod.POST, getURL(componentName + "/_outputColumns"), configuration);

        return Serializator.outputColumnsOutput(response);
    }

    public ProcessStatelessOutput processStateless(String componentName, ProcessStatelessInput processStatelessInput) {
        componentName = urlify(componentName);
        String configurationAndData = Serializator.stringProcessStatelessInput(processStatelessInput);
        String response = call(RESTClient.HttpMethod.PUT, getURL(componentName), configurationAndData);

        return Serializator.processStatelessOutput(response);
    }

    public String createComponent(String componentName, final String timeout, final CreateInput config) {
        componentName = urlify(componentName);
        String configuration = Serializator.stringCreateInput(config);

        return call(RESTClient.HttpMethod.POST, getURL(componentName + "?timeout=" + timeout), configuration);
    }

    public ProcessOutput processComponent(final String instanceId, final ProcessInput processInput)
            throws ComponentNotFoundException {
        String inputData = Serializator.stringProcessInput(processInput);
        String response = call(RESTClient.HttpMethod.PUT, getURL("/_instance/" + instanceId), inputData);

        return Serializator.processOutput(response);
    }

    public ProcessResult getFinalResult(final String instanceId) throws ComponentNotFoundException {
        String response = call(RESTClient.HttpMethod.GET, getURL(instanceId + "/result"), "");

        return Serializator.processResult(response);
    }

    public void deleteComponent(final String instanceId) throws ComponentNotFoundException {
        call(RESTClient.HttpMethod.DELETE, getURL(instanceId), "");
    }

    public String getUserTenantName() {
        String url = String.format("%s%s", this.url, "/repository/_user");
        String response = call(RESTClient.HttpMethod.GET, url, "");
        JsonNode userInfo = null;
        try {
            userInfo = Serializator.getJacksonObjectMapper().readTree(response);
            JsonNode tenantN = userInfo.get("tenant");
            if(tenantN == null) {
                return tenantName = "unknown";
            } else {
                return tenantName = tenantN.asText();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String urlify(String string) {
        return ComponentsRestClientUtils.encodeUrlPathSegment(ComponentsRestClientUtils.escapeComponentName(string));
    }

    private String getURL(String suffix) {
        if (suffix != null && ! suffix.isEmpty()) {
            suffix = "/" + suffix;
        }

        return String.format("%s/repository/%s/components%s", url, urlify(tenantName), suffix);
    }

    private String call(RESTClient.HttpMethod httpMethod, String url, String requestBody) {
        String response = restClient.getResponse(httpMethod, url, requestBody);

        return response;
    }
}
