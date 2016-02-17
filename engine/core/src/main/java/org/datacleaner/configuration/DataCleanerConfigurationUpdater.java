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
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.hadoop.yarn.webapp.hamlet.HamletSpec;
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
     * Updates an element value at the location of the nodePathElements.
     * 
     * @param xPath
     *            a String xpath describing the path to an element
     * @param newValue
     *            the new value to set
     */
    public void update(String xPath, String newValue) {
        if (document == null) {
            load();
        }
        final NodeList  elementToUpdate = findElementToUpdate(xPath);
        if(elementToUpdate == null || elementToUpdate.getLength() ==0){
            return;
        }
        elementToUpdate.item(0).setTextContent(newValue);
        write();
    }

    /**
     * Creates the new child to element
     *
     * @param parentXpath
     *                      is Xpath to parent
     * @param elementName
     *                      name of new element
     * @return XPath of new child
     */
    public String createChild(String parentXpath, String elementName){
        if (document == null) {
            load();
        }
        final NodeList parentNodes = findElementToUpdate(parentXpath);
        if(parentNodes == null || parentNodes.getLength() ==0) {
            return null;
        }

        Element newElement = document.createElement(elementName);
        parentNodes.item(0).appendChild(newElement);

        final NodeList createdNodes = findElementToUpdate(parentXpath + "/" + elementName);

        return parentXpath + "/" + elementName + "[" +createdNodes.getLength()+"]";
    }

    public NodeList findElementToUpdate(String xPath) {
        if (document == null) {
            load();
        }
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPathObj = xPathFactory.newXPath();
        NodeList nodes = null;
        try {
            XPathExpression compile = xPathObj.compile(xPath);
            Object result = compile.evaluate(document, XPathConstants.NODESET);
            nodes = (NodeList ) result;
        } catch (XPathExpressionException e) {
            logger.error("Problem with xpath {}.", xPath, e);
        }
        return nodes;
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
