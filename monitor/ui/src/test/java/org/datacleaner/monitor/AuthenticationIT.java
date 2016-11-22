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
package org.datacleaner.monitor;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.datacleaner.test.MonitorRestEndpoint;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import io.restassured.RestAssured;

public class AuthenticationIT {
    private static final String JOBS_PATH = "/jobs/";

    private static final String USER_NAME = "admin";
    private static final String USER_PASSWORD = "admin";

    @Rule
    public ExternalResource monitorRestEndpoint = new MonitorRestEndpoint();

    @Before
    public void setup() throws IOException {
        RestAssured.authentication = RestAssured.DEFAULT_AUTH;
    }

    @Test
    public void testUnauthorized() {
        get(JOBS_PATH)
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void testAuthorized() {
        given().auth().basic(USER_NAME, USER_PASSWORD)
                .when().get(JOBS_PATH)
                .then().statusCode(HttpStatus.SC_OK);
    }
}
