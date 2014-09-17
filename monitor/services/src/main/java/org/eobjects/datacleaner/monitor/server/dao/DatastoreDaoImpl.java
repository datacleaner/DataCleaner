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
package org.eobjects.datacleaner.monitor.server.dao;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eobjects.analyzer.configuration.DatastoreXmlExternalizer;
import org.eobjects.analyzer.configuration.JaxbConfigurationReader;
import org.eobjects.analyzer.configuration.jaxb.AbstractDatastoreType;
import org.eobjects.analyzer.configuration.jaxb.Configuration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.Func;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Default implementation of {@link DatastoreDao}
 */
@Component
public class DatastoreDaoImpl implements DatastoreDao {

    private static final Logger logger = LoggerFactory.getLogger(DatastoreDaoImpl.class);

    @Override
    public void removeDatastore(TenantContext tenantContext, String datastoreName) throws IllegalArgumentException {
        if (datastoreName == null) {
            throw new IllegalArgumentException("Datastore name cannot be null");
        }

        final JaxbConfigurationReader jaxbConfigurationAdaptor = new JaxbConfigurationReader();

        final RepositoryFile confFile = tenantContext.getConfigurationFile();
        final Configuration configuration = confFile.readFile(new Func<InputStream, Configuration>() {
            @Override
            public Configuration eval(InputStream in) {
                Configuration configuration = jaxbConfigurationAdaptor.unmarshall(in);
                return configuration;
            }
        });

        boolean found = false;

        final List<AbstractDatastoreType> datastores = configuration.getDatastoreCatalog()
                .getJdbcDatastoreOrAccessDatastoreOrCsvDatastore();
        for (Iterator<AbstractDatastoreType> it = datastores.iterator(); it.hasNext();) {
            final AbstractDatastoreType abstractDatastoreType = it.next();
            final String candidateName = abstractDatastoreType.getName();
            if (datastoreName.equals(candidateName)) {
                // found it!
                it.remove();
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Could not find datastore with name '" + datastoreName + "'");
        }

        confFile.writeFile(new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {
                jaxbConfigurationAdaptor.marshall(configuration, out);
            }
        });
    }

    @Override
    public Element parseDatastoreElement(Reader reader) {
        final DocumentBuilder documentBuilder = getDocumentBuilder();

        // parse the incoming datastore definition
        final InputSource inputSource = new InputSource(reader);
        try {
            final Document datastoreDocument = documentBuilder.parse(inputSource);
            return datastoreDocument.getDocumentElement();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException("Failed to parse datastore element: " + e.getMessage(), e);
        }
    }

    @Override
    public String addDatastore(TenantContext tenantContext, Datastore datastore) throws UnsupportedOperationException {
        final DatastoreXmlExternalizer externalizer = new DatastoreXmlExternalizer();
        final Element element = externalizer.externalize(datastore);
        final String result = addDatastore(tenantContext, element);
        return result;
    }

    @Override
    public String addDatastore(TenantContext tenantContext, Element datastoreElement) {

        final RepositoryFile confFile = tenantContext.getConfigurationFile();
        // parse the configuration file
        final Document configurationFileDocument = confFile.readFile(new Func<InputStream, Document>() {
            @Override
            public Document eval(InputStream in) {
                try {
                    return getDocumentBuilder().parse(in);
                } catch (Exception e) {
                    throw new IllegalStateException("Could not parse configuration file", e);
                }
            }
        });

        // add the new datastore to the <datastore-catalog> element of the
        // configuration file
        final Element configurationFileDocumentElement = configurationFileDocument.getDocumentElement();

        final Element datastoreCatalogElement = DomUtils.getChildElementByTagName(configurationFileDocumentElement,
                "datastore-catalog");
        if (datastoreCatalogElement == null) {
            throw new IllegalStateException("Could not find <datastore-catalog> element in configuration file");
        }

        final Node importedNode = configurationFileDocument.importNode(datastoreElement, true);
        datastoreCatalogElement.appendChild(importedNode);

        final int datastoreIndex = DomUtils.getChildElements(datastoreCatalogElement).size() - 1;

        // write the updated configuration file
        final Transformer transformer = getTransformer();
        final Source source = new DOMSource(configurationFileDocument);

        confFile.writeFile(new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {
                final Result outputTarget = new StreamResult(out);
                transformer.transform(source, outputTarget);
                out.flush();
            }
        });

        tenantContext.onConfigurationChanged();

        String datastoreName = datastoreElement.getAttribute("name");
        if (datastoreName == null) {
            // slightly more intricate way of getting datastore name by index
            DatastoreCatalog datastoreCatalog = tenantContext.getConfiguration().getDatastoreCatalog();
            String[] datastoreNames = datastoreCatalog.getDatastoreNames();
            try {
                datastoreName = datastoreNames[datastoreIndex];
            } catch (IndexOutOfBoundsException e) {
                logger.warn("Failed to get index {} of datastore names: {}", datastoreCatalog,
                        Arrays.toString(datastoreNames));
            }
        }
        return datastoreName;
    }

    protected DocumentBuilder getDocumentBuilder() {
        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            return documentBuilder;
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    protected Transformer getTransformer() {
        try {
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            return transformer;
        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }
}
