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
import org.junit.Before;
import org.junit.Test;

public class AuthenticationIT extends MonitorBaseIT {

    @Before
    public void setup() throws IOException {
        super.setup();
    }

    @Test
    public void testAuthentication() {
        get(JOBS_PATH)
            .then().statusCode(HttpStatus.SC_UNAUTHORIZED);

        given().auth().basic(USER_NAME, USER_PASSWORD)
            .when().get(JOBS_PATH)
            .then().statusCode(HttpStatus.SC_OK);
    }
}
