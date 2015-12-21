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

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Resource;
import org.datacleaner.util.xml.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class for updating values (content of tags) in the XML configuration file
 * (conf.xml). It does not create new tags nor removes any.
 */
public class DataCleanerConfigurationUpdater {

    private static final Logger logger = LoggerFactory.getLogger(DataCleanerConfigurationUpdater.class);

    private final Resource configurationFileResource;
    private Document document = null;

    public DataCleanerConfigurationUpdater(Resource configurationFileResource) {
        this.configurationFileResource = configurationFileResource;
    }

    public void update(String nodePath, String newValue) {
        update(new String[] { nodePath }, newValue);
    }

    public void update(String[] nodePaths, String newValue) {
        if (nodePaths.length <= 0) {
            return;
        }

        if (document == null) {
            load();
        }

        final Node node = findElementToUpdate(nodePaths);

        if (node != null) {
            node.setTextContent(newValue);
            write();
        }
    }

    private Node findElementToUpdate(String[] nodePath) {
        int i = 0;
        NodeList nodeList = document.getElementsByTagName(nodePath[i]);
        i++;
        Node node = (nodeList.getLength() > 0) ? nodeList.item(0) : null;

        while (node != null && i < nodePath.length) {
            node = getNodeChild(node, nodePath[i]);
            i++;
        }

        return node;
    }

    private void load() {
        final InputStream in = configurationFileResource.read();
        try {
            document = XmlUtils.parseDocument(in);
        } catch (Exception e) {
            logger.warn("XML configuration was not loaded: " + e.getMessage(), e);
        } finally {
            FileHelper.safeClose(in);
        }
    }

    private static Node getNodeChild(Node parentNode, String childNodeName) {
        NodeList nodeList = parentNode.getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);

            if (childNode.getNodeName().equals(childNodeName)) {
                return childNode;
            }
        }

        return null;
    }

    public void write() {
        final OutputStream out = configurationFileResource.write();
        try {
            XmlUtils.writeDocument(document, out);
        } finally {
            FileHelper.safeClose(out);
        }
    }

    public Document getDocument() {
        if (document == null) {
            load();
        }
        return document;
    }
}
