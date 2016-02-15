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
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

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

    /**
     * Short-hand version of {@link #update(String[], String)} which allows to
     * pass a colon-separated string with the node path elements in it, for
     * instance: "descriptor-providers:remote-components:server:username" to
     * update the username element in that path.
     * 
     * @param nodePath
     *            a colon-separated path to an element
     * @param newValue
     *            the new value to set
     */
    public void update(String nodePath, String newValue) {
        update(nodePath.split(":"), newValue);
    }

    /**
     * Updates an element value at the location of the nodePathElements.
     * 
     * @param nodePathElements
     *            a String array describing the path to an element
     * @param newValue
     *            the new value to set
     */
    public void update(String[] nodePathElements, String newValue) {
        if (nodePathElements.length <= 0) {
            return;
        }

        if (document == null) {
            load();
        }

        final Node node = findElementToUpdate(nodePathElements);

        if (node != null) {
            node.setTextContent(newValue);
            write();
        }
    }

    public void createElement(String nodePathElements, String text) {
        createElement(nodePathElements.split(":"), text);
    }

    public void createElement(String[] nodePathElements, String text) {
        if (document == null) {
            load();
        }
        int i = 0;
        NodeList nodeList = document.getElementsByTagName(nodePathElements[i]);
        i++;
        Node node = (nodeList.getLength() > 0) ? nodeList.item(0) : null;
        if (node == null) {
            return;
        }
        Node prevNode;
        while (true){
            prevNode = node;
            node = getNodeChild(node, nodePathElements[i]);
            i++;
            if(node == null){
                node = prevNode;
                i--;
                break;
            }

            if(i >= nodePathElements.length){
                return; // element is there
            }
        }

        for (int j = i; j < nodePathElements.length; j++) {
            Element element = document.createElement(nodePathElements[j]);
            node.appendChild(element);
            node = element;
            if(j == nodePathElements.length -1){
                if(text != null){
                    Text textDate = document.createTextNode(text);
                    node.appendChild(textDate);
                }
            }
        }
        write();
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
