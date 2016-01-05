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
package org.datacleaner.util.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XmlUtils {

    private XmlUtils() {
    }
    
    public static void writeDocument(Node docOrNode, OutputStream out) {
        writeDocument(docOrNode, out, true);
    }

    public static void writeDocument(Node docOrNode, OutputStream out, boolean includeXmlDeclaration) {
        final Transformer transformer = createTransformer(includeXmlDeclaration);
        try {
            final Source source = new DOMSource(docOrNode);
            final Result outputTarget = new StreamResult(out);
            transformer.transform(source, outputTarget);
        } catch (TransformerException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Transformer createTransformer() {
        return createTransformer(true);
    }

    public static Transformer createTransformer(boolean includeXmlDeclaration) {
        try {
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            if (!includeXmlDeclaration) {
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            }
            return transformer;
        } catch (TransformerConfigurationException e) {
            // should never happen
            throw new IllegalStateException(e);
        }
    }

    public static Document parseDocument(InputStream in) {
        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.parse(in);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    public static Document createDocument() {
        return createDocumentBuilder().newDocument();
    }

    public static DocumentBuilder createDocumentBuilder() {
        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            return documentBuilderFactory.newDocumentBuilder();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static String writeDocumentToString(Node node, boolean includeXmlDeclaration) {
        final Transformer transformer = createTransformer(includeXmlDeclaration);

        final StringWriter sw = new StringWriter();
        final StreamResult result = new StreamResult(sw);
        final DOMSource source = new DOMSource(node);
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new IllegalStateException(e);
        }
        final String xmlString = sw.toString();
        return xmlString;
    }
}
