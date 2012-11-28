/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class CASMonitorHttpClientTest {

    // A main method that can be used to manually test a CAS based HTTP request
    public static void main(String[] args) throws Exception {
        final CASMonitorHttpClient client = new CASMonitorHttpClient(new DefaultHttpClient(),
                "http://localhost:8080/cas", "admin", "admin");

        final HttpResponse response = client.execute(new HttpGet("http://localhost:8080/test/test.jsp"));

        final StatusLine statusLine = response.getStatusLine();
        System.out.println("Status: " + statusLine.getStatusCode() + " - " + statusLine.getReasonPhrase());

        final HttpEntity entity = response.getEntity();
        final InputStream in = entity.getContent();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = reader.readLine();
        while (line != null) {
            System.out.println("Read: " + line);
            line = reader.readLine();
        }
    }

}
