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
package org.datacleaner.util.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;

public class DefaultCasMonitorHttpClientTest {

    // A main method that can be used to manually test a CAS based HTTP request
    public static void main(String[] args) throws Exception {
        try (CASMonitorHttpClient client = new DefaultCasMonitorHttpClient(HttpClients.createSystem(),
                "https://localhost:8443/cas", "admin", "admin", "https://localhost:8443/DataCleaner-monitor")) {

            doRequest(client, new HttpGet("https://localhost:8443/DataCleaner-monitor/repository/DC/ping"));
            doRequest(
                    client,
                    new HttpGet(
                            "https://localhost:8443/DataCleaner-monitor/repository/DC/launch-resources/conf.xml?job=Customer+completeness"));
            client.close();
        }

        try (CASMonitorHttpClient client = new DefaultCasMonitorHttpClient(HttpClients.createSystem(),
                "https://localhost:8443/cas", "admin", "admin", "https://localhost:8443/DataCleaner-monitor")) {
            doRequest(client, new HttpGet(
                    "https://localhost:8443/DataCleaner-monitor/repository/DC/jobs/Customer+completeness.analysis.xml"));
        }
    }

    private static void doRequest(CASMonitorHttpClient client, HttpUriRequest req) throws Exception {
        System.out.println("REQUESTING: " + req.getURI());

        final HttpResponse response = client.execute(req);

        final StatusLine statusLine = response.getStatusLine();
        System.out.println("\tStatus: " + statusLine.getStatusCode() + " - " + statusLine.getReasonPhrase());

        final HttpEntity entity = response.getEntity();
        final InputStream in = entity.getContent();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = reader.readLine();
        while (line != null) {
            System.out.println("\t" + line);
            line = reader.readLine();
        }
    }

}
