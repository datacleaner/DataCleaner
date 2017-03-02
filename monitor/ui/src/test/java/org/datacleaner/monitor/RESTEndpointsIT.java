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

import java.io.File;
import java.io.IOException;
import java.util.Date;

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
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test(timeout = 10 * ONE_MINUTE)
    public void test() throws Exception {
        // upload test dictionary
        final String dictionaryJson = "{ \"name\" :\"DictionaryTest\", \"entries\" : [\"1\", \"5\"],"
                + " \"caseSensitive\": \"false\"}";

        final String referenceDataLocation = given().contentType("application/json").body(dictionaryJson).when().put(
                REFERENCEDATA_PATH + "DictionaryTest").then().statusCode(HttpStatus.SC_CREATED).extract().header(
                        "Location").toString();
        assertEquals(RestAssured.baseURI + RestAssured.basePath + "/referencedata/dictionary/DictionaryTest",
                referenceDataLocation);

        // upload job and set the schedule to be a hot folder()
        final String jobName = "ReferenceData";
        final File file = new File("src/test/resources/" + jobName + ".analysis.xml");
        assertTrue(file.exists());
        final String command = "docker exec " + HotFolderHelper.getContainerId()
                + " /bin/sh /tmp/generate-hot-folder-input.sh";
        HotFolderHelper.getCommandOutput(command);

        final String hotFolder = "/tmp/hot_folder";
        given().multiPart(file).param("hotfolder", hotFolder).when().post(JOBS_PATH).then().statusCode(
                HttpStatus.SC_OK);

        final Date date = new Date();
        final long time = date.getTime();
        // check that the job is scheduled with a hot folder
        final boolean scheduleCheck = given().contentType("application/json").when().get("/schedules/" + jobName).then()
                .statusCode(HttpStatus.SC_OK).extract().body().asString().contains(hotFolder);

        assertTrue(scheduleCheck);

        // check the result
        boolean findJob;
        do {
            Thread.sleep(1000);
            findJob = given().contentType("application/json").when().get("/results?time=" + time).then().statusCode(
                    HttpStatus.SC_OK).extract().body().jsonPath().getString("filename").contains(jobName);
        } while (findJob == false);
        assertTrue(findJob);

        // remove the hot folder
        final String removeHotFolderCommand = "docker exec " + HotFolderHelper.getContainerId()
                + " /bin/sh /tmp/remove-hot-folder.sh";
        HotFolderHelper.getCommandOutput(removeHotFolderCommand);

    }
}
