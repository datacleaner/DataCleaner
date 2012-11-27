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
package org.eobjects.datacleaner;

import java.util.Map;

import org.h2.util.StringUtils;

import junit.framework.TestCase;

public class MainTest extends TestCase {

    public void testInitializeSystemProperties() throws Exception {
        Map<String, String> properties = Main
                .initializeSystemProperties("-job hey.xml -Dfoo=bar -Dfoo=bar -DdatastoreCatalog.orderdb.url=foobar -hello world"
                        .split(" "));
        assertEquals(2, properties.size());
        assertEquals("foobar", properties.get("datastoreCatalog.orderdb.url"));
        assertEquals("bar", properties.get("foo"));

        // clean up
        System.clearProperty("datastoreCatalog.orderdb.url");
        System.clearProperty("foo");
    }

    // A simple main method "integration test" which assumes that the
    // JettyRunner of the DC monitor is running. This will emulate how the JNLP
    // client of DC monitor starts up.
    public static void main(String[] foo) {
        final String hostname = "demo.datacleaner.org";
        final String port = "80";
        final String context = "/DataCleaner-monitor";
        final String tenant = "DC";
        final String username = "admin";
        final String datastore = "orderdb";
        final String jobName = "Customer completeness";

        final String confLocation;
        final String jobLocation;
        if (StringUtils.isNullOrEmpty(jobName)) {
            confLocation = "http://" + hostname + ":" + port + context + "/repository/" + tenant
                    + "/launch-resources/conf.xml";
            jobLocation = null;
        } else {
            confLocation = "http://" + hostname + ":" + port + context + "/repository/" + tenant
                    + "/launch-resources/conf.xml?job=" + jobName.replaceAll(" ", "\\+");
            jobLocation = "http://" + hostname + ":" + port + context + "/repository/" + tenant + "/jobs/"
                    + jobName.replaceAll(" ", "\\+") + ".analysis.xml";
        }
        final String[] args = ("-conf " + confLocation + (jobLocation != null ? " -job " + jobLocation : "")
                + (StringUtils.isNullOrEmpty(datastore) ? "" : " -ds " + datastore)
                + " -Ddatacleaner.ui.visible=true -Ddatacleaner.embed.client=dq-monitor -Ddatacleaner.sandbox=true"
                + " -Ddatacleaner.monitor.hostname=" + hostname + " -Ddatacleaner.monitor.port=" + port
                + " -Ddatacleaner.monitor.context=" + context + "/ -Ddatacleaner.monitor.https=false"
                + " -Ddatacleaner.monitor.tenant=" + tenant + " -Ddatacleaner.monitor.username=" + username).split(" ");
        Main.main(args);
    }
}
