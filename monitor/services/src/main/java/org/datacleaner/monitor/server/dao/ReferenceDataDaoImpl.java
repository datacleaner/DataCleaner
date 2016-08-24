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
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.datacleaner.configuration.JaxbConfigurationReader;
import org.datacleaner.configuration.jaxb.Configuration;
import org.datacleaner.configuration.jaxb.CustomElementType;
import org.datacleaner.configuration.jaxb.DatastoreDictionaryType;
import org.datacleaner.configuration.jaxb.DatastoreSynonymCatalogType;
import org.datacleaner.configuration.jaxb.RegexPatternType;
import org.datacleaner.configuration.jaxb.SimplePatternType;
import org.datacleaner.configuration.jaxb.TextFileDictionaryType;
import org.datacleaner.configuration.jaxb.TextFileSynonymCatalogType;
import org.datacleaner.configuration.jaxb.ValueListDictionaryType;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.util.xml.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ReferenceDataDaoImpl.class);

    @Override
    public String updateReferenceDataSubSection(TenantContext tenantContext, Element updatedReferenceDataSubSection) {
        final Document configurationFileDocument = getConfigurationFileDocument(tenantContext);
        final Element referenceDataCatalogElement = getReferenceDataElement(configurationFileDocument);
        removeOldElementIfExists(referenceDataCatalogElement.getChildNodes(), updatedReferenceDataSubSection);
        final Node importedNode = configurationFileDocument.importNode(updatedReferenceDataSubSection, true);
        referenceDataCatalogElement.appendChild(importedNode);
        writeConfiguration(tenantContext, configurationFileDocument);

        return "";
    }
    
    
    private void removeOldElementIfExists(NodeList referenceDataNodes, Element updatedReferenceDataSubSection) {
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
    
    private void writeConfiguration(TenantContext tenantContext, Document configurationFileDocument) {
        final Transformer transformer = getTransformer();
        final Source source = new DOMSource(configurationFileDocument);

        tenantContext.getConfigurationFile().writeFile(out -> {
            final Result outputTarget = new StreamResult(out);
            transformer.transform(source, outputTarget);
            out.flush();
        });
        tenantContext.onConfigurationChanged();
    }

    private Element getReferenceDataElement(Document configurationFileDocument) {
        final Element configurationFileDocumentElement = configurationFileDocument.getDocumentElement();
        final Element referenceDataCatalogElement = DomUtils.getChildElementByTagName(configurationFileDocumentElement,
                "reference-data-catalog");

        if (referenceDataCatalogElement == null) {
            throw new IllegalStateException("Could not find <reference-data-catalog> element in configuration file");
        }

        return referenceDataCatalogElement;
    }

    private Document getConfigurationFileDocument(TenantContext tenantContext) {
        final Document configurationFileDocument = tenantContext.getConfigurationFile().readFile(in -> {
            try {
                return getDocumentBuilder().parse(in);
            } catch (Exception e) {
                throw new IllegalStateException("Could not parse configuration file", e);
            }
        });

        return configurationFileDocument;
    }

    protected DocumentBuilder getDocumentBuilder() {
        return XmlUtils.createDocumentBuilder();
    }

    protected Transformer getTransformer() {
        return XmlUtils.createTransformer();
    }
// mytodo: refactor remove*
    @Override
    public Element parseReferenceDataElement(Reader reader) {
        final DocumentBuilder documentBuilder = getDocumentBuilder();
        final InputSource inputSource = new InputSource(reader);

        try {
            final Document referenceDataDocument = documentBuilder.parse(inputSource);
            return referenceDataDocument.getDocumentElement();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }

            throw new IllegalStateException("Failed to parse referenceData element: " + e.getMessage(), e);
        }
    }

    @Override
    public void removeDictionary(final TenantContext tenantContext, final Dictionary dictionary)
            throws IllegalArgumentException {
        if (dictionary == null) {
            throw new IllegalArgumentException("String pattern name can not be null");
        }

        final JaxbConfigurationReader jaxbConfigurationAdaptor = new JaxbConfigurationReader();
        final RepositoryFile confFile = tenantContext.getConfigurationFile();
        final Configuration configuration = confFile.readFile(in -> {
            return jaxbConfigurationAdaptor.unmarshall(in);
        });

        boolean found = false;
        final List<Object> dictionaries = configuration.getReferenceDataCatalog()
                .getDictionaries().getTextFileDictionaryOrValueListDictionaryOrDatastoreDictionary();

        for (Iterator<Object> it = dictionaries.iterator(); it.hasNext(); ) {
            final String candidateName = getComparableName(it.next());

            if (dictionary.equals(candidateName)) {
                it.remove();
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Could not find string pattern with name '" + dictionary + "'");
        }

        confFile.writeFile(out -> {
            jaxbConfigurationAdaptor.marshall(configuration, out);
        });
    }

    @Override
    public void removeSynonymCatalog(final TenantContext tenantContext, final SynonymCatalog synonymCatalog)
            throws IllegalArgumentException {
        if (synonymCatalog == null) {
            throw new IllegalArgumentException("String pattern name can not be null");
        }

        final JaxbConfigurationReader jaxbConfigurationAdaptor = new JaxbConfigurationReader();
        final RepositoryFile confFile = tenantContext.getConfigurationFile();
        final Configuration configuration = confFile.readFile(in -> {
            return jaxbConfigurationAdaptor.unmarshall(in);
        });

        boolean found = false;
        final List<Object> synonymCatalogs = configuration.getReferenceDataCatalog()
                .getSynonymCatalogs().getTextFileSynonymCatalogOrDatastoreSynonymCatalogOrCustomSynonymCatalog();

        for (Iterator<Object> it = synonymCatalogs.iterator(); it.hasNext(); ) {
            final String candidateName = getComparableName(it.next());

            if (synonymCatalog.equals(candidateName)) {
                it.remove();
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Could not find string pattern with name '" + synonymCatalog + "'");
        }

        confFile.writeFile(out -> {
            jaxbConfigurationAdaptor.marshall(configuration, out);
        });
    }

    @Override
    public void removeStringPattern(final TenantContext tenantContext, final StringPattern stringPattern)
            throws IllegalArgumentException {
        if (stringPattern == null) {
            throw new IllegalArgumentException("String pattern name can not be null");
        }

        final JaxbConfigurationReader jaxbConfigurationAdaptor = new JaxbConfigurationReader();
        final RepositoryFile confFile = tenantContext.getConfigurationFile();
        final Configuration configuration = confFile.readFile(in -> {
            return jaxbConfigurationAdaptor.unmarshall(in);
        });

        boolean found = false;
        final List<Object> stringPatterns = configuration.getReferenceDataCatalog().getStringPatterns()
                .getRegexPatternOrSimplePattern();

        for (Iterator<Object> it = stringPatterns.iterator(); it.hasNext(); ) {
            final String candidateName = getComparableName(it.next());

            if (stringPattern.equals(candidateName)) {
                it.remove();
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Could not find string pattern with name '" + stringPattern + "'");
        }

        confFile.writeFile(out -> {
            jaxbConfigurationAdaptor.marshall(configuration, out);
        });
    }

    private String getComparableName(Object referenceDataObject) {
        if (referenceDataObject instanceof SimplePatternType) {
            return ((SimplePatternType) referenceDataObject).getName();
        } else if (referenceDataObject instanceof RegexPatternType) {
            return ((RegexPatternType) referenceDataObject).getName();
        } else if (referenceDataObject instanceof TextFileDictionaryType) {
            return ((TextFileDictionaryType) referenceDataObject).getName();
        } else if (referenceDataObject instanceof ValueListDictionaryType) {
            return ((ValueListDictionaryType) referenceDataObject).getName();
        } else if (referenceDataObject instanceof DatastoreDictionaryType) {
            return ((DatastoreDictionaryType) referenceDataObject).getName();
        } else if (referenceDataObject instanceof CustomElementType) {
            return ((CustomElementType) referenceDataObject).getClassName();
        } else if (referenceDataObject instanceof TextFileSynonymCatalogType) {
            return ((TextFileSynonymCatalogType) referenceDataObject).getName();
        } else if (referenceDataObject instanceof DatastoreSynonymCatalogType) {
            return ((DatastoreSynonymCatalogType) referenceDataObject).getName();
        }

        return "";
    }
}
