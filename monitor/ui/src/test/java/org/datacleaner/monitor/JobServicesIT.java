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

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import io.restassured.RestAssured;

public class JobServicesIT extends MonitorBaseIT {
    private static final int ONE_MINUTE = 60000;
    private static final int ONE_SECOND = 1000;

    @Before
    public void setup() throws IOException {
        super.setup();

        RestAssured.authentication = basic(USER_NAME, USER_PASSWORD);
    }

    @Test(timeout = ONE_MINUTE)
    public void testJobCycle() throws URISyntaxException, InterruptedException {
        final String jobName = "customer_completeness";
        final String jobFileName = jobName + ".analysis.xml";

        given().multiPart(new File(getClass().getClassLoader().getResource(jobFileName).toURI()))
            .expect().body("file_type", equalTo("ANALYSIS_JOB")).and()
            .expect().body("status", equalTo("Success")).when().post(JOBS_PATH + jobFileName);

        final String resultPath = "/logs/" + post(JOBS_PATH + jobName + ".trigger").then().extract().path("resultId");

        while (get(resultPath).then().extract().path("execution-log.execution-status").equals("PENDING")) {
            Thread.sleep(ONE_SECOND);
        }

        while (get(resultPath).then().extract().path("execution-log.execution-status").equals("RUNNING")) {
            Thread.sleep(ONE_SECOND);
        }

        get(resultPath).then().body("execution-log.execution-status", equalTo("SUCCESS"));

        post(JOBS_PATH + jobName + ".delete").then().statusCode(HttpStatus.SC_OK);
    }
}
