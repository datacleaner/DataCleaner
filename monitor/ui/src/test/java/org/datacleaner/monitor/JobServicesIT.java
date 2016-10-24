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

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import io.restassured.RestAssured;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;

public class JobServicesIT {
    private static final String JOBS_PATH = "/jobs/";

    private static final int ONE_MINUTE = 60000;
    private static final int ONE_SECOND = 1000;

    private static final String USER_NAME = "admin";
    private static final String USER_PASSWORD = "admin";

    @Rule
    public ExternalResource monitorRestEndpoint = new MonitorRestEndpoint();

    @Before
    public void setup() throws IOException {
        RestAssured.authentication = basic(USER_NAME, USER_PASSWORD);
    }

    @Test(timeout = 5 * ONE_MINUTE)
    public void testJobs() throws URISyntaxException, InterruptedException {
        final String[] jobNames = {
                "Sample custom job",
                "Copy employees to customer table",
                "Customer completeness",
                "Customer profiling",
                "product_profiling",
        };
        
        for (String name : jobNames) {
            testJob(name);
        }
    }

    public void testJob(String jobName) throws URISyntaxException, InterruptedException {
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
