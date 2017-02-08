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
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.post;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.validation.constraints.AssertTrue;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.datacleaner.test.MonitorRestEndpoint;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import com.ibm.icu.util.Calendar;

import io.restassured.RestAssured;

public class RESTEndpointsIT {

    private static final String JOBS_PATH = "/jobs/";
    private static final String REFERENCEDATA_PATH = "/referencedata/dictionary/";

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

    @Test(timeout = 10 * ONE_MINUTE)
    public void test() throws Exception {
        // upload test dictionary
        final String dictionaryJson = "{ \"name\" :\"DictionaryTest\", \"entries\" : [\"cio\", \"dr\"],"
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
        final Path hotFolderLocation = Files.createTempDirectory("hotFolder");

        final String result = given().multiPart(file).param("hotfolder", hotFolderLocation.toAbsolutePath()).when()
                .post(JOBS_PATH).then().statusCode(HttpStatus.SC_OK).extract().body().asString();
        // assertEquals(
        // "[{\"filename\":\"ReferenceData.analysis.xml\",\"repository_path\":\"/demo/jobs/ReferenceData.analysis.xml\","
        // + "\"file_type\":\"ANALYSIS_JOB\",\"status\":\"Success\"}]", result);

        final File newFile = new File(hotFolderLocation.toFile(), "test" + Calendar.getInstance().getTimeInMillis());
        FileUtils.copyFile(new File("src/test/resources/emptyFile.txt"), newFile);
        assertTrue(newFile.exists());

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
