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
import static javax.management.timer.Timer.ONE_MINUTE;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Date;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import io.restassured.RestAssured;

public class HotFolderIT {
    
    private static final String USER_NAME = "admin";
    private static final String USER_PASSWORD = "admin";
    
    @Before
    public void setup() throws IOException {
        RestAssured.authentication = basic(USER_NAME, USER_PASSWORD);
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test(timeout = 5 * ONE_MINUTE)
    public void testHotFolder() throws Exception {
        try {

            final Date date = new Date();
            final long time = date.getTime();
            final String command = "docker exec " + HotFolderHelper.getContainerId()
                    + " /bin/sh /tmp/generate-hot-folder-input.sh";
            HotFolderHelper.getCommandOutput(command);
            
            // check the result
            boolean findJob;
            do {
                Thread.sleep(1000);
                findJob = given().contentType("application/json").when().get("/results?not_before=" + time).then().statusCode(
                        HttpStatus.SC_OK).extract().body().jsonPath().getString("filename").contains(
                                "simple_numbers_distribution");
            } while (findJob == false);
            assertTrue(findJob);
            
            //remove the hot folder
            final String removeHotFolderCommand = "docker exec " + HotFolderHelper.getContainerId()
                    + " /bin/sh /tmp/remove-hot-folder.sh";
            HotFolderHelper.getCommandOutput(removeHotFolderCommand);
        } catch (final IOException e) {
            fail(e.getMessage());
        } 
    }
}
