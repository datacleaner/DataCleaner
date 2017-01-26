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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.datacleaner.configuration.DomConfigurationWriter;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.repository.RepositoryFileResource;
import org.datacleaner.util.xml.XmlUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Default implementation of {@link ReferenceDataDao}.
 */
@Component
public class ReferenceDataDaoImpl implements ReferenceDataDao {

    @Override
    public String updateReferenceDataSubSection(final TenantContext tenantContext,
            final Element updatedReferenceDataSubSection) {
        final Document configurationFileDocument = getConfigurationFileDocument(tenantContext);
        final Element referenceDataCatalogElement = getReferenceDataElement(configurationFileDocument);
        removeOldElementIfExists(referenceDataCatalogElement.getChildNodes(), updatedReferenceDataSubSection);
        final Node importedNode = configurationFileDocument.importNode(updatedReferenceDataSubSection, true);
        referenceDataCatalogElement.appendChild(importedNode);
        writeConfiguration(tenantContext, configurationFileDocument);

        return "";
    }

    private void removeOldElementIfExists(final NodeList referenceDataNodes,
            final Element updatedReferenceDataSubSection) {
        Node oldNode = null;

        for (int i = 0; i < referenceDataNodes.getLength(); i++) {
            if (referenceDataNodes.item(i).getNodeName().equals(updatedReferenceDataSubSection.getNodeName())) {
                oldNode = referenceDataNodes.item(i);
            }
        }

        if (oldNode != null) {
            oldNode.getParentNode().removeChild(oldNode);
        }
    }

    private void writeConfiguration(final TenantContext tenantContext, final Document configurationFileDocument) {
        final Transformer transformer = getTransformer();
        final Source source = new DOMSource(configurationFileDocument);

        tenantContext.getConfigurationFile().writeFile(out -> {
            final Result outputTarget = new StreamResult(out);
            transformer.transform(source, outputTarget);
            out.flush();
        });
        tenantContext.onConfigurationChanged();
    }

    private Element getReferenceDataElement(final Document configurationFileDocument) {
        final Element configurationFileDocumentElement = configurationFileDocument.getDocumentElement();
        final Element referenceDataCatalogElement =
                DomUtils.getChildElementByTagName(configurationFileDocumentElement, "reference-data-catalog");

        if (referenceDataCatalogElement == null) {
            throw new IllegalStateException("Could not find <reference-data-catalog> element in configuration file");
        }

        return referenceDataCatalogElement;
    }

    private Document getConfigurationFileDocument(final TenantContext tenantContext) {
        return tenantContext.getConfigurationFile().readFile(in -> {
            try {
                return getDocumentBuilder().parse(in);
            } catch (final Exception e) {
                throw new IllegalStateException("Could not parse configuration file", e);
            }
        });
    }

    protected DocumentBuilder getDocumentBuilder() {
        return XmlUtils.createDocumentBuilder();
    }

    protected Transformer getTransformer() {
        return XmlUtils.createTransformer();
    }

    @Override
    public Element parseReferenceDataElement(final Reader reader) {
        final DocumentBuilder documentBuilder = getDocumentBuilder();
        final InputSource inputSource = new InputSource(reader);

        try {
            final Document referenceDataDocument = documentBuilder.parse(inputSource);
            return referenceDataDocument.getDocumentElement();
        } catch (final Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }

            throw new IllegalStateException("Failed to parse reference data element: " + e.getMessage(), e);
        }
    }

    private DomConfigurationWriter getConfigurationWriter(final TenantContext tenantContext) {
        final RepositoryFileResource resource = new RepositoryFileResource(tenantContext.getConfigurationFile());
        return new DomConfigurationWriter(resource) {
            @Override
            protected void onDocumentChanged(final Document document) {
                resource.write(out -> XmlUtils.writeDocument(document, out));
                tenantContext.onConfigurationChanged();
            }
        };
    }

    @Override
    public void addDictionary(final TenantContext tenantContext, final Dictionary dictionary) {
        getConfigurationWriter(tenantContext).externalize(dictionary);
    }

    @Override
    public void addSynonymCatalog(final TenantContext tenantContext, final SynonymCatalog synonymCatalog) {
        getConfigurationWriter(tenantContext).externalize(synonymCatalog);
    }

    @Override
    public void addStringPattern(final TenantContext tenantContext, final StringPattern stringPattern) {
        getConfigurationWriter(tenantContext).externalize(stringPattern);
    }

    @Override
    public void removeDictionary(final TenantContext tenantContext, final String name)
            throws IllegalArgumentException {
        getConfigurationWriter(tenantContext).removeDictionary(name);
    }

    @Override
    public void removeSynonymCatalog(final TenantContext tenantContext, final String name)
            throws IllegalArgumentException {
        getConfigurationWriter(tenantContext).removeSynonymCatalog(name);
    }

    @Override
    public void removeStringPattern(final TenantContext tenantContext, final String name)
            throws IllegalArgumentException {
        getConfigurationWriter(tenantContext).removeStringPattern(name);
    }
}
