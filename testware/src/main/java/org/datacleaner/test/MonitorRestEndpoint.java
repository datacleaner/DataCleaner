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
package org.datacleaner.test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.rules.ExternalResource;

import io.restassured.RestAssured;

public class MonitorRestEndpoint extends ExternalResource {
    @Override
    protected void before() throws Exception {
        final Properties dockerProperties = new Properties();
        dockerProperties.load(getClass().getClassLoader().getResourceAsStream("docker.properties"));

        RestAssured.baseURI =
                "http://" + determineHostName() + ":" + dockerProperties.getProperty("monitor.portnumber") + "/"
                        + dockerProperties.getProperty("monitor.contextpath");
        RestAssured.basePath = "/repository/demo";
    }

    private String determineHostName() throws URISyntaxException {
        final String dockerHost = System.getenv("DOCKER_HOST");

        if (dockerHost == null) {
            // If no value is returned for the DOCKER_HOST environment variable fall back to a default.
            return "localhost";
        } else {
            return (new URI(dockerHost)).getHost();
        }
    }
}
