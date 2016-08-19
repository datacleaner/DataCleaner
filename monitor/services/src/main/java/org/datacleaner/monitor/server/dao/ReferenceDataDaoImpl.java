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
import org.datacleaner.reference.ReferenceDataCatalog;
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
 * Default implementation of {@link ReferenceDataDao}. 
 */
@Component
public class ReferenceDataDaoImpl implements ReferenceDataDao {

    private static final Logger logger = LoggerFactory.getLogger(ReferenceDataDaoImpl.class);

    @Override
    public void removeReferenceData(TenantContext tenantContext, String referenceDataName)
            throws IllegalArgumentException {
        if (referenceDataName == null) {
            throw new IllegalArgumentException("Reference data name can not be null");
        }

        final JaxbConfigurationReader jaxbConfigurationAdaptor = new JaxbConfigurationReader();
        final RepositoryFile confFile = tenantContext.getConfigurationFile();
        final Configuration configuration = confFile.readFile(in -> {
            return jaxbConfigurationAdaptor.unmarshall(in);
        });

// mytodo: only dictionaries for now
        boolean found = false;
        final List<Object> dictionaries = configuration.getReferenceDataCatalog()
                .getDictionaries().getTextFileDictionaryOrValueListDictionaryOrDatastoreDictionary();

        for (Iterator<Object> it = dictionaries.iterator(); it.hasNext(); ) {
            final String candidateName = getComparableName(it.next());
            
            if (referenceDataName.equals(candidateName)) {
                it.remove();
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Could not find reference data with name '" + referenceDataName + "'");
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
    public String addReferenceData(TenantContext tenantContext, Element referenceDataElement) {
        final RepositoryFile confFile = tenantContext.getConfigurationFile();
        final Document configurationFileDocument = confFile.readFile(in -> {
            try {
                return getDocumentBuilder().parse(in);
            } catch (Exception e) {
                throw new IllegalStateException("Could not parse configuration file", e);
            }
        });

        final Element configurationFileDocumentElement = configurationFileDocument.getDocumentElement();
        final Element referenceDataCatalogElement = DomUtils.getChildElementByTagName(configurationFileDocumentElement,
                "reference-data-catalog");
        
        if (referenceDataCatalogElement == null) {
            throw new IllegalStateException("Could not find <reference-data-catalog> element in configuration file");
        }

        final Node importedNode = configurationFileDocument.importNode(referenceDataElement, true);
        referenceDataCatalogElement.appendChild(importedNode);

        final int referenceDataIndex = DomUtils.getChildElements(referenceDataCatalogElement).size() - 1;

        final Transformer transformer = getTransformer();
        final Source source = new DOMSource(configurationFileDocument);

        confFile.writeFile(out -> {
            final Result outputTarget = new StreamResult(out);
            transformer.transform(source, outputTarget);
            out.flush();
        });

        tenantContext.onConfigurationChanged();
        String referenceDataName = referenceDataElement.getAttribute("name");
        
        if (referenceDataName == null) {
            ReferenceDataCatalog referenceDataCatalog = tenantContext.getConfiguration().getReferenceDataCatalog();
            String[] referenceDataNames = referenceDataCatalog.getDictionaryNames();
            
            try {
                referenceDataName = referenceDataNames[referenceDataIndex];
            } catch (IndexOutOfBoundsException e) {
                logger.warn("Failed to get index {} of reference data names: {}", referenceDataCatalog,
                        Arrays.toString(referenceDataNames));
            }
        }
        return referenceDataName;
    }

    protected DocumentBuilder getDocumentBuilder() {
        return XmlUtils.createDocumentBuilder();
    }

    protected Transformer getTransformer() {
        return XmlUtils.createTransformer();
    }
}
