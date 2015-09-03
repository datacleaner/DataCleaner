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
package org.datacleaner.components.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @since 02. 09. 2015
 */
public class ComponentRESTClientImpl implements ComponentRESTClient {
    private RESTClient restClient = null;
    private String tenantName = "";
    private String host = "";

    public ComponentRESTClientImpl(String host, String username, String password) {
        this.tenantName = username;
        this.host = host;
        restClient = new RESTClientImpl(username, password);
    }

    public String getAllComponents() {
        return call(RESTClient.HttpMethod.GET, getURL(""), "");
    }

    public String getComponentInfo(String componentName) {
        componentName = urlify(componentName);
        return call(RESTClient.HttpMethod.GET, getURL(componentName), "");
    }

    public String processStateless(String componentName, final String configurationAndData) {
        componentName = urlify(componentName);
        return call(RESTClient.HttpMethod.PUT, getURL(componentName), configurationAndData);
    }

    public String createComponent(String componentName, final String timeout, final String configuration) {
        componentName = urlify(componentName);
        return call(RESTClient.HttpMethod.POST, getURL(componentName + "?timeout=" + timeout), configuration);
    }

    public String processComponent(final String instanceId, final String inputData) {
        return call(RESTClient.HttpMethod.PUT, getURL("/_instance/" + instanceId), inputData);
    }

    public String getFinalResult(final String instanceId) {
        return call(RESTClient.HttpMethod.GET, getURL(instanceId + "/result"), "");
    }

    public void deleteComponent(final String instanceId) {
        call(RESTClient.HttpMethod.DELETE, getURL(instanceId), "");
    }

    private String urlify(String string) {
        try {
            string = URLEncoder.encode(string, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
        }

        return string;
    }

    private String getURL(String suffix) {
        if (suffix != null && ! suffix.isEmpty()) {
            suffix = "/" + suffix;
        }

        return String.format("%s/repository/%s/components%s", host, tenantName, suffix);
    }

    private String call(RESTClient.HttpMethod httpMethod, String url, String requestBody) {
        restClient.setEndpoint(httpMethod, url);
        String response = restClient.getResponse(requestBody);

        return response;
    }
}
