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
package org.datacleaner.monitor.dashboard;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import org.junit.Test;

public class JobServicesIT {
    @Test
    public void testLogin() throws IOException {
        final Properties testProperties = new Properties();
        testProperties.load(getClass().getClassLoader().getResourceAsStream("docker.properties"));

        final URL testURL = new URL(testProperties.getProperty("monitor.url"));

        final HttpURLConnection connection = (HttpURLConnection) testURL.openConnection();
        connection.setRequestMethod("GET");

        assertEquals(200, connection.getResponseCode());
    }
}
