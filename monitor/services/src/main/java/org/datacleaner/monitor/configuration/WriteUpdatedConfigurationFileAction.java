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
package org.datacleaner.monitor.configuration;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.metamodel.util.Action;
import org.datacleaner.configuration.jaxb.Configuration;
import org.datacleaner.configuration.jaxb.CustomElementType;
import org.datacleaner.configuration.jaxb.DatastoreDictionaryType;
import org.datacleaner.configuration.jaxb.DatastoreSynonymCatalogType;
import org.datacleaner.configuration.jaxb.RegexPatternType;
import org.datacleaner.configuration.jaxb.SimplePatternType;
import org.datacleaner.configuration.jaxb.TextFileDictionaryType;
import org.datacleaner.configuration.jaxb.TextFileSynonymCatalogType;
import org.datacleaner.configuration.jaxb.ValueListDictionaryType;
import org.datacleaner.monitor.server.jaxb.AbstractJaxbAdaptor;
import org.datacleaner.repository.RepositoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes an updated conf.xml file to the repository. This is used by taking the
 * <datastore-catalog> and <reference-data-catalog> elements of the updated
 * conf.xml file, and replacing them in the existing conf.xml file.
 */
public class WriteUpdatedConfigurationFileAction extends AbstractJaxbAdaptor<Configuration>
        implements Action<OutputStream> {

    private static final Logger logger = LoggerFactory.getLogger(WriteUpdatedConfigurationFileAction.class);

    private final InputStream _updatedConfigurationInputStream;
    private final Configuration _existingConfiguration;
    private final boolean _onlyReferenceData;

    public WriteUpdatedConfigurationFileAction(InputStream updatedConfigurationInputStream,
            RepositoryFile existingConfigurationFile) throws JAXBException {
        this(updatedConfigurationInputStream, existingConfigurationFile, false);
    }

    public WriteUpdatedConfigurationFileAction(InputStream updatedConfigurationInputStream,
            RepositoryFile existingConfigurationFile, boolean onlyReferenceData) throws JAXBException {
        super(Configuration.class);
        _updatedConfigurationInputStream = updatedConfigurationInputStream;
        _onlyReferenceData = onlyReferenceData;

        Configuration existingConfiguration;
        try {
            if (existingConfigurationFile == null) {
                existingConfiguration = null;
            } else {
                existingConfiguration = existingConfigurationFile.readFile(in -> {
                    return unmarshal(in);
                });
            }
        } catch (Exception e) {
            logger.warn("Failed to parse configuration file - treating it as invalid and will override.", e);
            existingConfiguration = null;
        }
        _existingConfiguration = existingConfiguration;
    }

    @Override
    public void run(OutputStream out) throws Exception {
        final Configuration updatedConfiguration = unmarshal(_updatedConfigurationInputStream);

        if (_existingConfiguration == null) {
            marshal(updatedConfiguration, out);
        } else {
            if (!_onlyReferenceData) {
                _existingConfiguration.setDatastoreCatalog(updatedConfiguration.getDatastoreCatalog());
            }

            addNewReferenceData(_existingConfiguration, updatedConfiguration);
            marshal(_existingConfiguration, out);
        }
    }

    private void addNewReferenceData(Configuration currentConfiguration, Configuration newConfiguration) {
        if (newConfiguration.getReferenceDataCatalog().getDictionaries() != null) { // dictionaries
            if (currentConfiguration.getReferenceDataCatalog().getDictionaries() == null) {
                currentConfiguration.getReferenceDataCatalog()
                        .setDictionaries(newConfiguration.getReferenceDataCatalog().getDictionaries());
            } else {
                addUniqueValues(currentConfiguration.getReferenceDataCatalog().getDictionaries()
                                .getTextFileDictionaryOrValueListDictionaryOrDatastoreDictionary(),
                        newConfiguration.getReferenceDataCatalog().getDictionaries()
                                .getTextFileDictionaryOrValueListDictionaryOrDatastoreDictionary());
            }
        }

        if (newConfiguration.getReferenceDataCatalog().getSynonymCatalogs() != null) { // synonym catalogs
            if (currentConfiguration.getReferenceDataCatalog().getSynonymCatalogs() == null) {
                currentConfiguration.getReferenceDataCatalog()
                        .setSynonymCatalogs(newConfiguration.getReferenceDataCatalog().getSynonymCatalogs());

            } else {
                addUniqueValues(currentConfiguration.getReferenceDataCatalog().getSynonymCatalogs()
                                .getTextFileSynonymCatalogOrDatastoreSynonymCatalogOrCustomSynonymCatalog(),
                        newConfiguration.getReferenceDataCatalog().getSynonymCatalogs()
                                .getTextFileSynonymCatalogOrDatastoreSynonymCatalogOrCustomSynonymCatalog());
            }
        }

        if (newConfiguration.getReferenceDataCatalog().getStringPatterns() != null) { // string patterns
            if (currentConfiguration.getReferenceDataCatalog().getStringPatterns() == null) {
                currentConfiguration.getReferenceDataCatalog()
                        .setStringPatterns(newConfiguration.getReferenceDataCatalog().getStringPatterns());
            } else {
                addUniqueValues(currentConfiguration.getReferenceDataCatalog().getStringPatterns()
                                .getRegexPatternOrRegexSwapPatternOrSimplePattern(),
                        newConfiguration.getReferenceDataCatalog()
                                .getStringPatterns().getRegexPatternOrRegexSwapPatternOrSimplePattern());
            }
        }
    }

    private void addUniqueValues(List<Object> target, List<Object> source) {
        for (Object sourceObject : source) {
            final String sourceName = getComparableName(sourceObject);
            boolean targetContainsSource = false;

            for (Object targetObject : target) {
                final String targetName = getComparableName(targetObject);

                if (targetName.equals(sourceName)) {
                    targetContainsSource = true;
                }
            }

            if (!targetContainsSource) {
                target.add(sourceObject);
            }
        }
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
