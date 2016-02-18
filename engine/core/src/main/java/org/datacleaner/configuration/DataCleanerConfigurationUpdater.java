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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Resource;
import org.datacleaner.util.xml.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Class for updating values (content of tags) in the XML configuration file
 * (conf.xml). It does not create new tags nor removes any.
 */
public class DataCleanerConfigurationUpdater {

    private static final Logger logger = LoggerFactory.getLogger(DataCleanerConfigurationUpdater.class);

    private final Resource configurationFileResource;
    private final Document document;

    public DataCleanerConfigurationUpdater(Resource configurationFileResource) {
        this.configurationFileResource = configurationFileResource;
        document = load();

    }

    /**
     * Updates an element value at the location of the nodePathElements.
     * 
     * @param xPath
     *            a String xpath describing the path to an element
     * @param newValue
     *            the new value to set
     */
    public void update(String xPath, String newValue) {
        final NodeList  elementToUpdate = find(xPath);
        for (int i = 0; i < elementToUpdate.getLength(); i++) {
            elementToUpdate.item(i).setTextContent(newValue);
        }
    }

    /**
     * Creates the new child to element
     *
     * @param parentXpath
     *                      is Xpath to parent
     * @param elementName
     *                      name of new element
     * @return XPath of new child or null if parent doesn't exist
     */
    public String createChild(String parentXpath, String elementName){
        final NodeList parentNodes = find(parentXpath);
        if(parentNodes.getLength() == 0) {
            return null;
        }

        Element newElement = document.createElement(elementName);
        parentNodes.item(0).appendChild(newElement);

        final NodeList createdNodes = find(parentXpath + "/" + elementName);

        return parentXpath + "/" + elementName + "[" +createdNodes.getLength() + "]";
    }

    public NodeList find(String xPath) {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPathObj = xPathFactory.newXPath();
        try {
            XPathExpression compile = xPathObj.compile(xPath);
            return (NodeList) compile.evaluate(document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
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
        return document;
    }

    private Document load() {
        final InputStream in = configurationFileResource.read();
        try {
            return XmlUtils.parseDocument(in);
        } catch (Exception e) {
            throw new RuntimeException("Configuration file " + configurationFileResource + " cannot be loaded: " + e.getMessage(), e);
        } finally {
            FileHelper.safeClose(in);
        }
    }
}
