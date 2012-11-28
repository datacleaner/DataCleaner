/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.user;

import junit.framework.TestCase;

public class MonitorConnectionTest extends TestCase {

    private final UserPreferences userPreferences = new UserPreferencesImpl(null);

    public void testGetBaseUrl() throws Exception {
        MonitorConnection con1 = new MonitorConnection(userPreferences, "localhost", 8080, "DataCleaner-monitor",
                false, "DC", null, "");
        assertEquals("http://localhost:8080/DataCleaner-monitor", con1.getBaseUrl());

        MonitorConnection con2 = new MonitorConnection(userPreferences, "localhost", 8080, null, true, "DC", null, "");
        assertEquals("https://localhost:8080", con2.getBaseUrl());

        MonitorConnection con3 = new MonitorConnection(userPreferences, "localhost", 8080, "/DC", true, "DC", null, "");
        assertEquals("https://localhost:8080/DC", con3.getBaseUrl());
    }

    public void testGetRepositoryUrl() throws Exception {
        MonitorConnection con1 = new MonitorConnection(userPreferences, "localhost", 8080, "DataCleaner-monitor",
                false, "DC", null, "");
        assertEquals("http://localhost:8080/DataCleaner-monitor/repository/DC", con1.getRepositoryUrl());

        MonitorConnection con2 = new MonitorConnection(userPreferences, "localhost", 8080, null, true, null, null, "");
        assertEquals("https://localhost:8080/repository", con2.getRepositoryUrl());
    }
}
