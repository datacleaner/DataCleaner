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
package org.datacleaner.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
import org.datacleaner.util.SecurityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.NodeList;

public class DataCleanerConfigurationUpdaterTest {

    private String tagName = "password";
    private String nodePathString = "configuration/descriptor-providers/remote-components/server/" + tagName;

    private final Resource originalConfigurationResource = new FileResource("src/test/resources/conf.xml");
    private final Resource configurationResource = new FileResource(
            "target/DataCleanerConfigurationUpdaterTest-conf.xml");

    @Test
    public void testUpdate() throws Exception {
        String originalPassword = encode("admin");
        String newPassword = encode("newPassword");
        FileHelper.copy(originalConfigurationResource, configurationResource);

        Assert.assertTrue(isValuePresent(originalPassword));
        Assert.assertFalse(isValuePresent(newPassword));

        changePassword(newPassword);

        Assert.assertFalse(isValuePresent(originalPassword));
        Assert.assertTrue(isValuePresent(newPassword));

        changePassword(originalPassword);

        Assert.assertTrue(isValuePresent(originalPassword));
        Assert.assertFalse(isValuePresent(newPassword));
    }

    @Test
    public void testWriteList() throws Exception {
        String nodePathStringRemote = "configuration/descriptor-providers/remote-components";
        FileHelper.copy(originalConfigurationResource, configurationResource);
        DataCleanerConfigurationUpdater configurationUpdater = new DataCleanerConfigurationUpdater(
                configurationResource);
        String xpathToServer2 = configurationUpdater.createChild(nodePathStringRemote, "server");
        Assert.assertEquals("configuration/descriptor-providers/remote-components/server[2]", xpathToServer2);

        String xpathToServer3 = configurationUpdater.createChild(nodePathStringRemote, "server");
        Assert.assertEquals("configuration/descriptor-providers/remote-components/server[3]", xpathToServer3);


        String serverName2 = configurationUpdater.createChild(xpathToServer2, "name");
        configurationUpdater.update(serverName2, "datacloud");
        configurationUpdater.write();

        NodeList serverNodes = configurationUpdater.getDocument().getElementsByTagName("server");
        Assert.assertEquals(3, serverNodes.getLength());
        NodeList childNodes = serverNodes.item(1).getChildNodes();
        Assert.assertEquals(1, childNodes.getLength());
        Assert.assertEquals("name", childNodes.item(0).getNodeName());
        Assert.assertEquals("datacloud", childNodes.item(0).getTextContent());
    }


    private static String encode(String value) {
        return SecurityUtils.encodePassword(value);
    }

    private boolean isValuePresent(String value) {
        try {
            final InputStream in = configurationResource.read();
            final BufferedReader inReader = FileHelper.getBufferedReader(in, FileHelper.DEFAULT_ENCODING);
            try {
                String line;

                while ((line = inReader.readLine()) != null) {
                    if (line.contains(value)) {
                        return true;
                    }
                }
            } finally {
                FileHelper.safeClose(inReader, in);
            }
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        return false;
    }

    private void changePassword(String newValue) {
        DataCleanerConfigurationUpdater configurationUpdater = new DataCleanerConfigurationUpdater(
                configurationResource);
        configurationUpdater.update(nodePathString, newValue);
    }
}