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

import java.io.File;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */
public class DataCleanerConfigurationUpdater {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(DataCleanerConfigurationUpdater.class);
    private String configurationFilePath = null;
    private Document document = null;

    public DataCleanerConfigurationUpdater(URL configurationFileURL) {
        this.configurationFilePath = configurationFileURL.getPath();
    }

    public void update(String[] nodePath, String newValue) {
        if (nodePath.length <= 0) {
            return;
        }

        if (document == null) {
            load();
        }

        Node node = findElementToUpdate(nodePath);

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
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(configurationFilePath);
        } catch (Exception e) {
            logger.warn("XML configuration was not loaded: " + e.getMessage());
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

    private void write() {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(configurationFilePath));
            transformer.transform(source, result);
        }
        catch (TransformerException e) {
            logger.warn("XML configuration was not stored: " + e.getMessage());
        }
    }
}
