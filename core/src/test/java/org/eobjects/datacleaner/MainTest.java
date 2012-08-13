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
        final String hostname = "127.0.0.1";
        final String port = "8888";
        final String context = "";
        final String confLocation = "http://" + hostname + ":" + port + context
                + "/repository/DC/launch-resources/conf.xml?job=random_number_generation";
        final String jobLocation = "http://" + hostname + ":" + port + context
                + "/repository/DC/jobs/random_number_generation.analysis.xml";
        final String[] args = ("-conf " + confLocation + " -job " + jobLocation + " -ds orderdb"
                + " -Ddatacleaner.ui.visible=true -Ddatacleaner.embed.client=dq-monitor -Ddatacleaner.sandbox=true"
                + " -Ddatacleaner.monitor.hostname=" + hostname + " -Ddatacleaner.monitor.port=" + port
                + " -Ddatacleaner.monitor.context=" + context + "/ -Ddatacleaner.monitor.https=false"
                + " -Ddatacleaner.monitor.tenant=DC" + " -Ddatacleaner.monitor.username=admin").split(" ");
        Main.main(args);
    }
}
