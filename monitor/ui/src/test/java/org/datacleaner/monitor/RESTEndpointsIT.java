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

import static io.restassured.RestAssured.basic;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpStatus;
import org.datacleaner.test.MonitorRestEndpoint;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import io.restassured.RestAssured;

public class RESTEndpointsIT {

    private static final String JOBS_PATH = "/jobs/";
    private static final String REFERENCEDATA_PATH = "/referencedata/dictionary/";

    private static final int ONE_MINUTE = 60000;

    private static final String USER_NAME = "admin";
    private static final String USER_PASSWORD = "admin";

    @Rule
    public ExternalResource monitorRestEndpoint = new MonitorRestEndpoint();

    @Before
    public void setup() throws IOException {
        RestAssured.authentication = basic(USER_NAME, USER_PASSWORD);
    }

    @Test(timeout = 10 * ONE_MINUTE)
    public void test() throws Exception {
        // upload test dictionary
        final String dictionaryJson = "{ \"name\" :\"DictionaryTest\", \"entries\" : [\"1\", \"5\"],"
                + " \"caseSensitive\": \"false\"}";

        final String header = given().contentType("application/json").body(dictionaryJson).when().put(REFERENCEDATA_PATH
                + "DictionaryTest").then().statusCode(HttpStatus.SC_CREATED).extract().header("Location").toString();
        assertEquals(
                "http://192.168.99.100:8080/DataCleaner-monitor-ui-5.1.6-SNAPSHOT/repository/demo/referencedata/dictionary/DictionaryTest",
                header);

        // upload job
        final String jobName = "ReferenceData.analysis.xml";
        final File file = new File("src/test/resources/" + jobName);
        assertTrue(file.exists());
        final String command = "docker exec " + HotFolderHelper.getContainerId()
                + " /bin/sh /tmp/generate-hot-folder-input.sh";
        HotFolderHelper.getCommandOutput(command);
        given().multiPart(file).param("hotfolder", "/tmp/hot_folder").when().post(JOBS_PATH).then().statusCode(
                HttpStatus.SC_OK);

        final String result3 = given().contentType("application/json").when().get("/schedules/ReferenceData")
                .then().statusCode(HttpStatus.SC_OK).extract().body().asString();

        //assertEquals("[{\"hotFolder\":\"/tmp/hot_folder\"}]", result3);

        try {
            // wait for the hot folder trigger and job execution
            Thread.sleep(20 * 1000);
        } catch (final InterruptedException e) {
            fail("Waiting for the job execution was interrupted. " + e.getMessage());
        }

        final String results2 = given().contentType("application/json").when().get("/results/").then().statusCode(
                HttpStatus.SC_OK).extract().body().jsonPath().getString("filename");
        assertEquals("", results2);

    }
}
