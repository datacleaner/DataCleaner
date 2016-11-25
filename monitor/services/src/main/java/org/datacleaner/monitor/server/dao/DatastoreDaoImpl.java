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
package org.datacleaner.monitor.server.dao;

import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.datacleaner.configuration.DomConfigurationWriter;
import org.datacleaner.configuration.JaxbConfigurationReader;
import org.datacleaner.configuration.jaxb.AbstractDatastoreType;
import org.datacleaner.configuration.jaxb.Configuration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.util.xml.XmlUtils;
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
    public void removeDatastore(final TenantContext tenantContext, final String datastoreName)
            throws IllegalArgumentException {
        if (datastoreName == null) {
            throw new IllegalArgumentException("Datastore name cannot be null");
        }

        final JaxbConfigurationReader jaxbConfigurationAdaptor = new JaxbConfigurationReader();

        final RepositoryFile confFile = tenantContext.getConfigurationFile();
        final Configuration configuration = confFile.readFile(jaxbConfigurationAdaptor::unmarshall);

        boolean found = false;

        final List<AbstractDatastoreType> datastores =
                configuration.getDatastoreCatalog().getJdbcDatastoreOrAccessDatastoreOrCsvDatastore();
        for (final Iterator<AbstractDatastoreType> it = datastores.iterator(); it.hasNext(); ) {
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

        confFile.writeFile(out -> jaxbConfigurationAdaptor.marshall(configuration, out));
    }

    @Override
    public Element parseDatastoreElement(final Reader reader) {
        final DocumentBuilder documentBuilder = getDocumentBuilder();

        // parse the incoming datastore definition
        final InputSource inputSource = new InputSource(reader);
        try {
            final Document datastoreDocument = documentBuilder.parse(inputSource);
            return datastoreDocument.getDocumentElement();
        } catch (final Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException("Failed to parse datastore element: " + e.getMessage(), e);
        }
    }

    @Override
    public String addDatastore(final TenantContext tenantContext, final Datastore datastore)
            throws UnsupportedOperationException {
        final DomConfigurationWriter externalizer = new DomConfigurationWriter();
        final Element element = externalizer.externalize(datastore);
        return addDatastore(tenantContext, element);
    }

    @Override
    public String addDatastore(final TenantContext tenantContext, final Element datastoreElement) {

        final RepositoryFile confFile = tenantContext.getConfigurationFile();
        // parse the configuration file
        final Document configurationFileDocument = confFile.readFile(in -> {
            try {
                return getDocumentBuilder().parse(in);
            } catch (final Exception e) {
                throw new IllegalStateException("Could not parse configuration file", e);
            }
        });

        // add the new datastore to the <datastore-catalog> element of the
        // configuration file
        final Element configurationFileDocumentElement = configurationFileDocument.getDocumentElement();

        final Element datastoreCatalogElement =
                DomUtils.getChildElementByTagName(configurationFileDocumentElement, "datastore-catalog");
        if (datastoreCatalogElement == null) {
            throw new IllegalStateException("Could not find <datastore-catalog> element in configuration file");
        }

        final Node importedNode = configurationFileDocument.importNode(datastoreElement, true);
        datastoreCatalogElement.appendChild(importedNode);

        final int datastoreIndex = DomUtils.getChildElements(datastoreCatalogElement).size() - 1;

        // write the updated configuration file
        final Transformer transformer = getTransformer();
        final Source source = new DOMSource(configurationFileDocument);

        confFile.writeFile(out -> {
            final Result outputTarget = new StreamResult(out);
            transformer.transform(source, outputTarget);
            out.flush();
        });

        tenantContext.onConfigurationChanged();

        String datastoreName = datastoreElement.getAttribute("name");
        if (datastoreName == null) {
            // slightly more intricate way of getting datastore name by index
            final DatastoreCatalog datastoreCatalog = tenantContext.getConfiguration().getDatastoreCatalog();
            final String[] datastoreNames = datastoreCatalog.getDatastoreNames();
            try {
                datastoreName = datastoreNames[datastoreIndex];
            } catch (final IndexOutOfBoundsException e) {
                logger.warn("Failed to get index {} of datastore names: {}", datastoreCatalog,
                        Arrays.toString(datastoreNames));
            }
        }
        return datastoreName;
    }

    protected DocumentBuilder getDocumentBuilder() {
        return XmlUtils.createDocumentBuilder();
    }

    protected Transformer getTransformer() {
        return XmlUtils.createTransformer();
    }
}
