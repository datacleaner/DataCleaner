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
package org.datacleaner.util;


import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
import org.datacleaner.configuration.DataCleanerConfigurationUpdater;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RemoteServersConfigRWTest {
    private final Resource originalConfigurationResource = new FileResource("src/test/resources/conf.xml");
    private final Resource configurationResource = new FileResource(
            "target/DataCleanerConfigurationUpdaterTest-conf.xml");

    @Test
    public void testAddNewServer() throws Exception {
        FileHelper.copy(originalConfigurationResource, configurationResource);
        MyRemoteServersConfigRW remoteServersConfigRW = new MyRemoteServersConfigRW();
        remoteServersConfigRW.writeCredentialsToConfig("newServer", "URLserver", "user", "pass");
        remoteServersConfigRW.writeCredentialsToConfig("newServer2", "URLserver2", "user2", "pass2");

        //check
        DataCleanerConfigurationUpdater dataCleanerConfigurationUpdater = new DataCleanerConfigurationUpdater(configurationResource);
        NodeList server = dataCleanerConfigurationUpdater.getDocument().getElementsByTagName("server");
        Assert.assertEquals(3, server.getLength());
        checkServerNode(server.item(1), "newServer", "URLserver", "user", "pass");
        checkServerNode(server.item(2), "newServer2", "URLserver2", "user2", "pass2");
   }

    @Test
    public void testUpdate() throws Exception {
        testAddNewServer();
        MyRemoteServersConfigRW remoteServersConfigRW = new MyRemoteServersConfigRW();
        remoteServersConfigRW.updateCredentials("newServer", "newUser", "newPassword");

        //check
        DataCleanerConfigurationUpdater dataCleanerConfigurationUpdater = new DataCleanerConfigurationUpdater(configurationResource);
        NodeList server = dataCleanerConfigurationUpdater.getDocument().getElementsByTagName("server");
        Assert.assertEquals(3, server.getLength());
        checkServerNode(server.item(1), "newServer", "URLserver", "newUser", "newPassword");
        checkServerNode(server.item(2), "newServer2", "URLserver2", "user2", "pass2");
    }

    private void checkServerNode(Node serverNode, String serverName, String serverUrl, String userName, String password){
        String[] data = new String[] { serverName, serverUrl, userName, password};
        NodeList childNodes = serverNode.getChildNodes();
        int dataIndex = 0;
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if(child.getNodeName().contains("#")){
                continue;
            }
            Assert.assertEquals(data[dataIndex], child.getTextContent());
            dataIndex++;
        }
    }

    private class MyRemoteServersConfigRW extends RemoteServersConfigRW{

        public MyRemoteServersConfigRW() {
            super(null);
        }

        @Override
        Resource getDataCleanerConfigurationFileResource() {
            return configurationResource;
        }
    }
}