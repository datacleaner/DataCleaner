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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.fixedwidth.FixedWidthConfiguration;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.ColumnTypeImpl;
import org.apache.metamodel.schema.TableType;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Resource;
import org.apache.metamodel.util.SimpleTableDef;
import org.apache.metamodel.xml.XmlSaxTableDef;
import org.datacleaner.api.RenderingFormat;
import org.datacleaner.configuration.jaxb.AbstractDatastoreType;
import org.datacleaner.configuration.jaxb.AccessDatastoreType;
import org.datacleaner.configuration.jaxb.BerkeleyDbStorageProviderType;
import org.datacleaner.configuration.jaxb.CassandraDatastoreType;
import org.datacleaner.configuration.jaxb.ClasspathScannerType;
import org.datacleaner.configuration.jaxb.ClasspathScannerType.Package;
import org.datacleaner.configuration.jaxb.CombinedStorageProviderType;
import org.datacleaner.configuration.jaxb.CompositeDatastoreType;
import org.datacleaner.configuration.jaxb.Configuration;
import org.datacleaner.configuration.jaxb.ConfigurationMetadataType;
import org.datacleaner.configuration.jaxb.CouchdbDatastoreType;
import org.datacleaner.configuration.jaxb.CsvDatastoreType;
import org.datacleaner.configuration.jaxb.CustomElementType;
import org.datacleaner.configuration.jaxb.CustomElementType.Property;
import org.datacleaner.configuration.jaxb.DatastoreCatalogType;
import org.datacleaner.configuration.jaxb.DatastoreDictionaryType;
import org.datacleaner.configuration.jaxb.DatastoreSynonymCatalogType;
import org.datacleaner.configuration.jaxb.DbaseDatastoreType;
import org.datacleaner.configuration.jaxb.ElasticSearchDatastoreType;
import org.datacleaner.configuration.jaxb.ElasticSearchDatastoreType.TableDef.Field;
import org.datacleaner.configuration.jaxb.ExcelDatastoreType;
import org.datacleaner.configuration.jaxb.FixedWidthDatastoreType;
import org.datacleaner.configuration.jaxb.FixedWidthDatastoreType.WidthSpecification;
import org.datacleaner.configuration.jaxb.HbaseDatastoreType;
import org.datacleaner.configuration.jaxb.HbaseDatastoreType.TableDef.Column;
import org.datacleaner.configuration.jaxb.InMemoryStorageProviderType;
import org.datacleaner.configuration.jaxb.JdbcDatastoreType;
import org.datacleaner.configuration.jaxb.JdbcDatastoreType.TableTypes;
import org.datacleaner.configuration.jaxb.JsonDatastoreType;
import org.datacleaner.configuration.jaxb.MongodbDatastoreType;
import org.datacleaner.configuration.jaxb.MultithreadedTaskrunnerType;
import org.datacleaner.configuration.jaxb.ObjectFactory;
import org.datacleaner.configuration.jaxb.OpenOfficeDatabaseDatastoreType;
import org.datacleaner.configuration.jaxb.PojoDatastoreType;
import org.datacleaner.configuration.jaxb.ReferenceDataCatalogType;
import org.datacleaner.configuration.jaxb.ReferenceDataCatalogType.Dictionaries;
import org.datacleaner.configuration.jaxb.ReferenceDataCatalogType.StringPatterns;
import org.datacleaner.configuration.jaxb.ReferenceDataCatalogType.SynonymCatalogs;
import org.datacleaner.configuration.jaxb.RegexPatternType;
import org.datacleaner.configuration.jaxb.SalesforceDatastoreType;
import org.datacleaner.configuration.jaxb.SasDatastoreType;
import org.datacleaner.configuration.jaxb.SimplePatternType;
import org.datacleaner.configuration.jaxb.SinglethreadedTaskrunnerType;
import org.datacleaner.configuration.jaxb.StorageProviderType;
import org.datacleaner.configuration.jaxb.SugarCrmDatastoreType;
import org.datacleaner.configuration.jaxb.TableTypeEnum;
import org.datacleaner.configuration.jaxb.TextFileDictionaryType;
import org.datacleaner.configuration.jaxb.TextFileSynonymCatalogType;
import org.datacleaner.configuration.jaxb.ValueListDictionaryType;
import org.datacleaner.configuration.jaxb.XmlDatastoreType;
import org.datacleaner.configuration.jaxb.XmlDatastoreType.TableDef;
import org.datacleaner.connection.AccessDatastore;
import org.datacleaner.connection.CassandraDatastore;
import org.datacleaner.connection.CompositeDatastore;
import org.datacleaner.connection.CouchDbDatastore;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.connection.DbaseDatastore;
import org.datacleaner.connection.ElasticSearchDatastore;
import org.datacleaner.connection.ElasticSearchDatastore.ClientType;
import org.datacleaner.connection.ExcelDatastore;
import org.datacleaner.connection.FixedWidthDatastore;
import org.datacleaner.connection.HBaseDatastore;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.connection.JsonDatastore;
import org.datacleaner.connection.MongoDbDatastore;
import org.datacleaner.connection.OdbDatastore;
import org.datacleaner.connection.PojoDatastore;
import org.datacleaner.connection.SalesforceDatastore;
import org.datacleaner.connection.SasDatastore;
import org.datacleaner.connection.SugarCrmDatastore;
import org.datacleaner.connection.XmlDatastore;
import org.datacleaner.descriptors.ClasspathScanDescriptorProvider;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.job.concurrent.MultiThreadedTaskRunner;
import org.datacleaner.job.concurrent.SingleThreadedTaskRunner;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.reference.DatastoreDictionary;
import org.datacleaner.reference.DatastoreSynonymCatalog;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.ReferenceData;
import org.datacleaner.reference.ReferenceDataCatalog;
import org.datacleaner.reference.ReferenceDataCatalogImpl;
import org.datacleaner.reference.RegexStringPattern;
import org.datacleaner.reference.SimpleDictionary;
import org.datacleaner.reference.SimpleStringPattern;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.reference.TextFileDictionary;
import org.datacleaner.reference.TextFileSynonymCatalog;
import org.datacleaner.storage.BerkeleyDbStorageProvider;
import org.datacleaner.storage.CombinedStorageProvider;
import org.datacleaner.storage.InMemoryStorageProvider;
import org.datacleaner.storage.StorageProvider;
import org.datacleaner.util.CollectionUtils2;
import org.datacleaner.util.JaxbValidationEventHandler;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.SecurityUtils;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.convert.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * Configuration reader that uses the JAXB model to read XML file based
 * configurations for DataCleaner.
 */
public final class JaxbConfigurationReader implements ConfigurationReader<InputStream> {

    private static final Logger logger = LoggerFactory.getLogger(JaxbConfigurationReader.class);

    public static final String ENCODED_PASSWORD_PREFIX = "enc:";

    private final JAXBContext _jaxbContext;
    private final ConfigurationReaderInterceptor _interceptor;
    private final Deque<String> _variablePathBuilder;

    public JaxbConfigurationReader() {
        this(null);
    }

    public JaxbConfigurationReader(ConfigurationReaderInterceptor configurationReaderCallback) {
        if (configurationReaderCallback == null) {
            configurationReaderCallback = new DefaultConfigurationReaderInterceptor();
        }
        _interceptor = configurationReaderCallback;
        _variablePathBuilder = new ArrayDeque<String>(4);
        try {
            _jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(),
                    ObjectFactory.class.getClassLoader());
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public DataCleanerConfiguration read(InputStream input) {
        return create(input);
    }

    public DataCleanerConfiguration create(FileObject file) {
        InputStream inputStream = null;
        try {
            inputStream = file.getContent().getInputStream();
            return create(inputStream);
        } catch (FileSystemException e) {
            throw new IllegalArgumentException(e);
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }

    public DataCleanerConfiguration create(File file) {
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            return create(inputStream);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }

    public DataCleanerConfiguration create(InputStream inputStream) {
        Configuration configuration = unmarshall(inputStream);
        return create(configuration);
    }

    /**
     * Convenience method to get the untouched JAXB configuration object from an
     * inputstream.
     * 
     * @param inputStream
     * @return
     */
    public Configuration unmarshall(InputStream inputStream) {
        try {
            final Unmarshaller unmarshaller = _jaxbContext.createUnmarshaller();
            unmarshaller.setEventHandler(new JaxbValidationEventHandler());

            final Configuration configuration = (Configuration) unmarshaller.unmarshal(inputStream);
            return configuration;
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Convenience method to marshal a JAXB configuration object into an output
     * stream.
     * 
     * @param configuration
     * @param outputStream
     */
    public void marshall(Configuration configuration, OutputStream outputStream) {
        try {
            final Marshaller marshaller = _jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setEventHandler(new JaxbValidationEventHandler());
            marshaller.marshal(configuration, outputStream);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    public DataCleanerConfiguration create(Configuration jaxbConfiguration) {
        final ConfigurationMetadataType metadata = jaxbConfiguration.getConfigurationMetadata();
        if (metadata != null) {
            logger.info("Configuration name: {}", metadata.getConfigurationName());
            logger.info("Configuration version: {}", metadata.getConfigurationVersion());
            logger.info("Configuration description: {}", metadata.getConfigurationDescription());
            logger.info("Author: {}", metadata.getAuthor());
            logger.info("Created date: {}", metadata.getCreatedDate());
            logger.info("Updated date: {}", metadata.getUpdatedDate());
        }

        final DataCleanerHomeFolder homeFolder = _interceptor.getHomeFolder();

        // create temporary environment and configuration objects - they will be
        // passed along during building of the final ones.
        final TemporaryMutableDataCleanerEnvironment temporaryEnvironment = new TemporaryMutableDataCleanerEnvironment(
                _interceptor.createBaseEnvironment());
        DataCleanerConfigurationImpl temporaryConfiguration = new DataCleanerConfigurationImpl(temporaryEnvironment,
                homeFolder);

        // update the temporary environment if overrides are specified in
        // configuration file.
        updateTaskRunnerIfSpecified(jaxbConfiguration, temporaryEnvironment, temporaryConfiguration);
        updateStorageProviderIfSpecified(jaxbConfiguration, temporaryEnvironment, temporaryConfiguration);
        updateDescriptorProviderIfSpecified(jaxbConfiguration, temporaryEnvironment, temporaryConfiguration);

        // add datastore catalog
        final DatastoreCatalog datastoreCatalog;
        {
            addVariablePath("datastoreCatalog");
            datastoreCatalog = createDatastoreCatalog(jaxbConfiguration.getDatastoreCatalog(), temporaryConfiguration,
                    temporaryEnvironment);
            removeVariablePath();

            temporaryConfiguration = temporaryConfiguration.withDatastoreCatalog(datastoreCatalog);
        }

        // add reference data catalog
        final ReferenceDataCatalog referenceDataCatalog;
        {
            addVariablePath("referenceDataCatalog");
            referenceDataCatalog = createReferenceDataCatalog(jaxbConfiguration.getReferenceDataCatalog(),
                    temporaryEnvironment, temporaryConfiguration);
            removeVariablePath();
        }

        final DataCleanerEnvironmentImpl finalEnvironment = new DataCleanerEnvironmentImpl(temporaryEnvironment);
        final DataCleanerConfigurationImpl dataCleanerConfiguration = new DataCleanerConfigurationImpl(
                finalEnvironment, homeFolder, datastoreCatalog, referenceDataCatalog);

        return dataCleanerConfiguration;
    }

    private void updateDescriptorProviderIfSpecified(Configuration jaxbConfiguration,
            TemporaryMutableDataCleanerEnvironment environment, DataCleanerConfiguration temporaryConfiguration) {
        final DescriptorProvider descriptorProvider = createDescriptorProvider(jaxbConfiguration, environment,
                temporaryConfiguration);
        if (descriptorProvider != null) {
            environment.setDescriptorProvider(descriptorProvider);
        }
    }

    private DescriptorProvider createDescriptorProvider(Configuration configuration,
            DataCleanerEnvironment environment, DataCleanerConfiguration temporaryConfiguration) {
        final DescriptorProvider descriptorProvider;
        final CustomElementType customDescriptorProviderElement = configuration.getCustomDescriptorProvider();
        final ClasspathScannerType classpathScannerElement = configuration.getClasspathScanner();
        if (customDescriptorProviderElement != null) {
            descriptorProvider = createCustomElement(customDescriptorProviderElement, DescriptorProvider.class,
                    temporaryConfiguration, true);
        } else {
            final Collection<Class<? extends RenderingFormat<?>>> excludedRenderingFormats = new HashSet<Class<? extends RenderingFormat<?>>>();
            if (classpathScannerElement != null) {
                final List<String> excludedRenderingFormatList = classpathScannerElement.getExcludedRenderingFormat();
                for (String excludedRenderingFormat : excludedRenderingFormatList) {
                    try {
                        @SuppressWarnings("unchecked")
                        Class<? extends RenderingFormat<?>> cls = (Class<? extends RenderingFormat<?>>) _interceptor
                                .loadClass(excludedRenderingFormat);
                        excludedRenderingFormats.add(cls);
                    } catch (ClassNotFoundException e) {
                        logger.error("Could not find excluded rendering format class: " + excludedRenderingFormat, e);
                    }
                }
            }

            final ClasspathScanDescriptorProvider classpathScanner = new ClasspathScanDescriptorProvider(
                    environment.getTaskRunner(), excludedRenderingFormats);
            if (classpathScannerElement != null) {
                final List<Package> packages = classpathScannerElement.getPackage();
                for (Package pkg : packages) {
                    String packageName = pkg.getValue();
                    if (packageName != null) {
                        packageName = packageName.trim();
                        Boolean recursive = pkg.isRecursive();
                        if (recursive == null) {
                            recursive = true;
                        }
                        classpathScanner.scanPackage(packageName, recursive);
                    }
                }
            }
            descriptorProvider = classpathScanner;
        }

        return descriptorProvider;
    }

    private void updateStorageProviderIfSpecified(Configuration configuration,
            TemporaryMutableDataCleanerEnvironment environment, DataCleanerConfiguration temporaryConfiguration) {
        final StorageProviderType storageProviderType = configuration.getStorageProvider();

        if (storageProviderType == null) {
            return;
        }

        final StorageProvider storageProvider = createStorageProvider(storageProviderType, environment,
                temporaryConfiguration);
        environment.setStorageProvider(storageProvider);
    }

    private StorageProvider createStorageProvider(StorageProviderType storageProviderType,
            DataCleanerEnvironment environment, DataCleanerConfiguration temporaryConfiguration) {

        final CombinedStorageProviderType combinedStorageProvider = storageProviderType.getCombined();
        if (combinedStorageProvider != null) {
            final StorageProviderType collectionsStorage = combinedStorageProvider.getCollectionsStorage();
            final StorageProviderType rowAnnotationStorage = combinedStorageProvider.getRowAnnotationStorage();

            final StorageProvider collectionsStorageProvider = createStorageProvider(collectionsStorage, environment,
                    temporaryConfiguration);
            final StorageProvider rowAnnotationStorageProvider = createStorageProvider(rowAnnotationStorage,
                    environment, temporaryConfiguration);

            return new CombinedStorageProvider(collectionsStorageProvider, rowAnnotationStorageProvider);
        }

        final InMemoryStorageProviderType inMemoryStorageProvider = storageProviderType.getInMemory();
        if (inMemoryStorageProvider != null) {
            final int maxRowsThreshold = inMemoryStorageProvider.getMaxRowsThreshold();
            final int maxSetsThreshold = inMemoryStorageProvider.getMaxSetsThreshold();
            return new InMemoryStorageProvider(maxSetsThreshold, maxRowsThreshold);
        }

        final CustomElementType customStorageProvider = storageProviderType.getCustomStorageProvider();
        if (customStorageProvider != null) {
            return createCustomElement(customStorageProvider, StorageProvider.class, temporaryConfiguration, true);
        }

        final BerkeleyDbStorageProviderType berkeleyDbStorageProvider = storageProviderType.getBerkeleyDb();
        if (berkeleyDbStorageProvider != null) {
            final File parentDirectory = new File(_interceptor.getTemporaryStorageDirectory());
            final BerkeleyDbStorageProvider storageProvider = new BerkeleyDbStorageProvider(parentDirectory);
            final Boolean cleanDirectoryOnStartup = berkeleyDbStorageProvider.isCleanDirectoryOnStartup();
            if (cleanDirectoryOnStartup != null && cleanDirectoryOnStartup.booleanValue()) {
                storageProvider.cleanDirectory();
            }
            return storageProvider;
        }

        return environment.getStorageProvider();
    }

    @SuppressWarnings("deprecation")
    private String createFilename(String filename) {
        return _interceptor.createFilename(filename);
    }

    private ReferenceDataCatalog createReferenceDataCatalog(ReferenceDataCatalogType referenceDataCatalog,
            DataCleanerEnvironment environment, DataCleanerConfiguration temporaryConfiguration) {
        final List<Dictionary> dictionaryList = new ArrayList<Dictionary>();
        final List<SynonymCatalog> synonymCatalogList = new ArrayList<SynonymCatalog>();

        final List<StringPattern> stringPatterns = new ArrayList<StringPattern>();

        if (referenceDataCatalog != null) {

            final Dictionaries dictionaries = referenceDataCatalog.getDictionaries();
            if (dictionaries != null) {
                for (Object dictionaryType : dictionaries
                        .getTextFileDictionaryOrValueListDictionaryOrDatastoreDictionary()) {
                    if (dictionaryType instanceof DatastoreDictionaryType) {
                        final DatastoreDictionaryType ddt = (DatastoreDictionaryType) dictionaryType;

                        final String name = ddt.getName();
                        checkName(name, Dictionary.class, dictionaryList);

                        addVariablePath(name);

                        final String dsName = getStringVariable("datastoreName", ddt.getDatastoreName());
                        final String columnPath = getStringVariable("columnPath", ddt.getColumnPath());

                        final DatastoreDictionary dict = new DatastoreDictionary(name, dsName, columnPath);
                        dict.setDescription(ddt.getDescription());

                        dictionaryList.add(dict);

                        removeVariablePath();

                    } else if (dictionaryType instanceof TextFileDictionaryType) {
                        final TextFileDictionaryType tfdt = (TextFileDictionaryType) dictionaryType;

                        final String name = tfdt.getName();
                        checkName(name, Dictionary.class, dictionaryList);

                        addVariablePath(name);

                        String filenamePath = getStringVariable("filename", tfdt.getFilename());
                        String filename = createFilename(filenamePath);
                        String encoding = getStringVariable("encoding", tfdt.getEncoding());
                        if (encoding == null) {
                            encoding = FileHelper.UTF_8_ENCODING;
                        }

                        final TextFileDictionary dict = new TextFileDictionary(name, filename, encoding);
                        dict.setDescription(tfdt.getDescription());
                        dictionaryList.add(dict);

                        removeVariablePath();
                    } else if (dictionaryType instanceof ValueListDictionaryType) {
                        final ValueListDictionaryType vldt = (ValueListDictionaryType) dictionaryType;

                        final String name = vldt.getName();
                        checkName(name, Dictionary.class, dictionaryList);

                        final List<String> values = vldt.getValue();
                        final SimpleDictionary dict = new SimpleDictionary(name, values);
                        dict.setDescription(vldt.getDescription());
                        dictionaryList.add(dict);
                    } else if (dictionaryType instanceof CustomElementType) {
                        final Dictionary customDictionary = createCustomElement((CustomElementType) dictionaryType,
                                Dictionary.class, temporaryConfiguration, false);
                        checkName(customDictionary.getName(), Dictionary.class, dictionaryList);
                        dictionaryList.add(customDictionary);
                    } else {
                        throw new IllegalStateException("Unsupported dictionary type: " + dictionaryType);
                    }
                }
            }

            final SynonymCatalogs synonymCatalogs = referenceDataCatalog.getSynonymCatalogs();
            if (synonymCatalogs != null) {
                for (Object synonymCatalogType : synonymCatalogs
                        .getTextFileSynonymCatalogOrDatastoreSynonymCatalogOrCustomSynonymCatalog()) {
                    if (synonymCatalogType instanceof TextFileSynonymCatalogType) {
                        final TextFileSynonymCatalogType tfsct = (TextFileSynonymCatalogType) synonymCatalogType;

                        final String name = tfsct.getName();
                        checkName(name, SynonymCatalog.class, synonymCatalogList);

                        addVariablePath(name);

                        final String filenamePath = getStringVariable("filename", tfsct.getFilename());
                        final String filename = createFilename(filenamePath);
                        String encoding = getStringVariable("encoding", tfsct.getEncoding());
                        if (encoding == null) {
                            encoding = FileHelper.UTF_8_ENCODING;
                        }
                        final boolean caseSensitive = getBooleanVariable("caseSensitive", tfsct.isCaseSensitive(), true);
                        final TextFileSynonymCatalog sc = new TextFileSynonymCatalog(name, filename, caseSensitive,
                                encoding);
                        sc.setDescription(tfsct.getDescription());
                        synonymCatalogList.add(sc);

                        removeVariablePath();

                    } else if (synonymCatalogType instanceof CustomElementType) {
                        final SynonymCatalog customSynonymCatalog = createCustomElement(
                                (CustomElementType) synonymCatalogType, SynonymCatalog.class, temporaryConfiguration,
                                false);
                        checkName(customSynonymCatalog.getName(), SynonymCatalog.class, synonymCatalogList);
                        synonymCatalogList.add(customSynonymCatalog);
                    } else if (synonymCatalogType instanceof DatastoreSynonymCatalogType) {
                        final DatastoreSynonymCatalogType datastoreSynonymCatalogType = (DatastoreSynonymCatalogType) synonymCatalogType;

                        final String name = datastoreSynonymCatalogType.getName();
                        checkName(name, SynonymCatalog.class, synonymCatalogList);

                        addVariablePath(name);

                        final String dataStoreName = getStringVariable("datastoreName",
                                datastoreSynonymCatalogType.getDatastoreName());
                        final String masterTermColumnPath = getStringVariable("masterTermColumnPath",
                                datastoreSynonymCatalogType.getMasterTermColumnPath());

                        final String[] synonymColumnPaths = datastoreSynonymCatalogType.getSynonymColumnPath().toArray(
                                new String[0]);
                        final DatastoreSynonymCatalog sc = new DatastoreSynonymCatalog(name, dataStoreName,
                                masterTermColumnPath, synonymColumnPaths);
                        sc.setDescription(datastoreSynonymCatalogType.getDescription());
                        synonymCatalogList.add(sc);

                        removeVariablePath();
                    } else {
                        throw new IllegalStateException("Unsupported synonym catalog type: " + synonymCatalogType);
                    }
                }
            }

            final StringPatterns stringPatternTypes = referenceDataCatalog.getStringPatterns();
            if (stringPatternTypes != null) {
                for (Object obj : stringPatternTypes.getRegexPatternOrSimplePattern()) {
                    if (obj instanceof RegexPatternType) {
                        final RegexPatternType regexPatternType = (RegexPatternType) obj;

                        final String name = regexPatternType.getName();
                        checkName(name, StringPattern.class, stringPatterns);

                        addVariablePath(name);

                        final String expression = getStringVariable("expression", regexPatternType.getExpression());
                        final boolean matchEntireString = getBooleanVariable("matchEntireString",
                                regexPatternType.isMatchEntireString(), true);
                        final RegexStringPattern sp = new RegexStringPattern(name, expression, matchEntireString);
                        sp.setDescription(regexPatternType.getDescription());
                        stringPatterns.add(sp);

                        removeVariablePath();
                    } else if (obj instanceof SimplePatternType) {
                        final SimplePatternType simplePatternType = (SimplePatternType) obj;

                        final String name = simplePatternType.getName();
                        checkName(name, StringPattern.class, stringPatterns);

                        addVariablePath(name);

                        final String expression = getStringVariable("expression", simplePatternType.getExpression());
                        final SimpleStringPattern sp = new SimpleStringPattern(name, expression);
                        sp.setDescription(simplePatternType.getDescription());
                        stringPatterns.add(sp);

                        removeVariablePath();
                    } else {
                        throw new IllegalStateException("Unsupported string pattern type: " + obj);
                    }
                }
            }
        }

        return new ReferenceDataCatalogImpl(dictionaryList, synonymCatalogList, stringPatterns);
    }

    private DatastoreCatalog createDatastoreCatalog(DatastoreCatalogType datastoreCatalogType,
            DataCleanerConfigurationImpl temporaryConfiguration, DataCleanerEnvironment environment) {
        final Map<String, Datastore> datastores = new HashMap<String, Datastore>();

        // read all single, non-custom datastores
        final List<AbstractDatastoreType> datastoreTypes = datastoreCatalogType
                .getJdbcDatastoreOrAccessDatastoreOrCsvDatastore();
        for (AbstractDatastoreType datastoreType : datastoreTypes) {
            final String name = datastoreType.getName();
            checkName(name, Datastore.class, datastores);
            addVariablePath(name);

            final Datastore ds;
            if (datastoreType instanceof CsvDatastoreType) {
                ds = createDatastore(name, (CsvDatastoreType) datastoreType);
            } else if (datastoreType instanceof JdbcDatastoreType) {
                ds = createDatastore(name, (JdbcDatastoreType) datastoreType);
            } else if (datastoreType instanceof FixedWidthDatastoreType) {
                ds = createDatastore(name, (FixedWidthDatastoreType) datastoreType);
            } else if (datastoreType instanceof SasDatastoreType) {
                ds = createDatastore(name, (SasDatastoreType) datastoreType);
            } else if (datastoreType instanceof AccessDatastoreType) {
                ds = createDatastore(name, (AccessDatastoreType) datastoreType);
            } else if (datastoreType instanceof XmlDatastoreType) {
                ds = createDatastore(name, (XmlDatastoreType) datastoreType);
            } else if (datastoreType instanceof ExcelDatastoreType) {
                ds = createDatastore(name, (ExcelDatastoreType) datastoreType);
            } else if (datastoreType instanceof JsonDatastoreType) {
                ds = createDatastore(name, (JsonDatastoreType) datastoreType);
            } else if (datastoreType instanceof DbaseDatastoreType) {
                ds = createDatastore(name, (DbaseDatastoreType) datastoreType);
            } else if (datastoreType instanceof OpenOfficeDatabaseDatastoreType) {
                ds = createDatastore(name, (OpenOfficeDatabaseDatastoreType) datastoreType);
            } else if (datastoreType instanceof PojoDatastoreType) {
                ds = createDatastore(name, (PojoDatastoreType) datastoreType, temporaryConfiguration);
            } else if (datastoreType instanceof CouchdbDatastoreType) {
                ds = createDatastore(name, (CouchdbDatastoreType) datastoreType);
            } else if (datastoreType instanceof MongodbDatastoreType) {
                ds = createDatastore(name, (MongodbDatastoreType) datastoreType);
            } else if (datastoreType instanceof ElasticSearchDatastoreType) {
                ds = createDatastore(name, (ElasticSearchDatastoreType) datastoreType);
            } else if (datastoreType instanceof CassandraDatastoreType) {
                ds = createDatastore(name, (CassandraDatastoreType) datastoreType);
            } else if (datastoreType instanceof HbaseDatastoreType) {
                ds = createDatastore(name, (HbaseDatastoreType) datastoreType);
            } else if (datastoreType instanceof SalesforceDatastoreType) {
                ds = createDatastore(name, (SalesforceDatastoreType) datastoreType);
            } else if (datastoreType instanceof SugarCrmDatastoreType) {
                ds = createDatastore(name, (SugarCrmDatastoreType) datastoreType);
            } else if (datastoreType instanceof CompositeDatastoreType) {
                // skip composite datastores at this point
                continue;
            } else {
                throw new UnsupportedOperationException("Unsupported datastore type: " + datastoreType);
            }

            final String datastoreDescription = datastoreType.getDescription();
            ds.setDescription(datastoreDescription);

            removeVariablePath();
            datastores.put(name, ds);
        }

        // create custom datastores
        final List<CustomElementType> customDatastores = datastoreCatalogType.getCustomDatastore();
        for (CustomElementType customElementType : customDatastores) {
            Datastore ds = createCustomElement(customElementType, Datastore.class, temporaryConfiguration, true);
            String name = ds.getName();
            checkName(name, Datastore.class, datastores);
            datastores.put(name, ds);
        }

        // create composite datastores as the last step
        final List<CompositeDatastoreType> compositeDatastores = CollectionUtils2.filterOnClass(datastoreTypes,
                CompositeDatastoreType.class);
        for (CompositeDatastoreType compositeDatastoreType : compositeDatastores) {
            String name = compositeDatastoreType.getName();
            checkName(name, Datastore.class, datastores);

            List<String> datastoreNames = compositeDatastoreType.getDatastoreName();
            List<Datastore> childDatastores = new ArrayList<Datastore>(datastoreNames.size());
            for (String datastoreName : datastoreNames) {
                Datastore datastore = datastores.get(datastoreName);
                if (datastore == null) {
                    throw new IllegalStateException("No such datastore: " + datastoreName
                            + " (found in composite datastore: " + name + ")");
                }
                childDatastores.add(datastore);
            }

            CompositeDatastore ds = new CompositeDatastore(name, childDatastores);
            ds.setDescription(compositeDatastoreType.getDescription());
            datastores.put(name, ds);
        }

        final DatastoreCatalogImpl result = new DatastoreCatalogImpl(datastores.values());
        return result;
    }

    private Datastore createDatastore(String name, CassandraDatastoreType datastoreType) {

        final String hostname = getStringVariable("hostname", datastoreType.getHostname());
        Integer port = getIntegerVariable("port", datastoreType.getPort());
        if (port == null) {
            port = CassandraDatastore.DEFAULT_PORT;
        }
        final String keySpace = getStringVariable("keyspace", datastoreType.getKeyspace());
        final String username = getStringVariable("username", datastoreType.getUsername());
        final String password = getPasswordVariable("password", datastoreType.getPassword());
        final boolean ssl = getBooleanVariable("ssl", datastoreType.isSsl(), false);

        final List<org.datacleaner.configuration.jaxb.CassandraDatastoreType.TableDef> tableDefList = datastoreType
                .getTableDef();
        final SimpleTableDef[] tableDefs;
        if (tableDefList == null || tableDefList.isEmpty()) {
            tableDefs = null;
        } else {
            tableDefs = new SimpleTableDef[tableDefList.size()];
            for (int i = 0; i < tableDefs.length; i++) {
                final org.datacleaner.configuration.jaxb.CassandraDatastoreType.TableDef tableDef = tableDefList.get(i);
                final String tableName = tableDef.getTableName();
                final List<org.datacleaner.configuration.jaxb.CassandraDatastoreType.TableDef.Column> columnList = tableDef
                        .getColumn();

                final String[] columnNames = new String[columnList.size()];
                final ColumnType[] columnTypes = new ColumnType[columnList.size()];

                for (int j = 0; j < columnTypes.length; j++) {
                    final String propertyName = columnList.get(j).getName();
                    final String propertyTypeName = columnList.get(j).getType();
                    final ColumnType propertyType;
                    if (StringUtils.isNullOrEmpty(propertyTypeName)) {
                        propertyType = ColumnType.STRING;
                    } else {
                        propertyType = ColumnTypeImpl.valueOf(propertyTypeName);
                    }
                    columnNames[j] = propertyName;
                    columnTypes[j] = propertyType;
                }

                tableDefs[i] = new SimpleTableDef(tableName, columnNames, columnTypes);
            }
        }

        return new CassandraDatastore(name, hostname, port, keySpace, username, password, ssl, tableDefs);
    }

    private Datastore createDatastore(String name, ElasticSearchDatastoreType datastoreType) {
        final String clusterName = getStringVariable("clusterName", datastoreType.getClusterName());
        final String hostname = getStringVariable("hostname", datastoreType.getHostname());
        final String username = getStringVariable("username", datastoreType.getUsername());
        final String password = getPasswordVariable("password", datastoreType.getPassword());
        final boolean ssl = getBooleanVariable("ssl", datastoreType.isSsl(), false);
        final String keystorePath = getStringVariable("keystorePath", datastoreType.getKeystorePath());
        final String keystorePassword = getPasswordVariable("keystorePassword", datastoreType.getKeystorePassword());

        Integer port = getIntegerVariable("port", datastoreType.getPort());
        if (port == null) {
            port = ElasticSearchDatastore.DEFAULT_PORT;
        }

        final String indexName = getStringVariable("indexName", datastoreType.getIndexName());

        final List<org.datacleaner.configuration.jaxb.ElasticSearchDatastoreType.TableDef> tableDefList = datastoreType
                .getTableDef();
        final SimpleTableDef[] tableDefs;
        if (tableDefList.isEmpty()) {
            tableDefs = null;
        } else {
            tableDefs = new SimpleTableDef[tableDefList.size()];
            for (int i = 0; i < tableDefs.length; i++) {
                final org.datacleaner.configuration.jaxb.ElasticSearchDatastoreType.TableDef tableDef = tableDefList
                        .get(i);

                final String docType = tableDef.getDocumentType();
                final List<Field> fieldList = tableDef.getField();

                final String[] columnNames = new String[fieldList.size()];
                final ColumnType[] columnTypes = new ColumnType[fieldList.size()];

                for (int j = 0; j < columnTypes.length; j++) {
                    final String propertyName = fieldList.get(j).getName();
                    final String propertyTypeName = fieldList.get(j).getType();
                    final ColumnType propertyType;
                    if (StringUtils.isNullOrEmpty(propertyTypeName)) {
                        propertyType = ColumnType.STRING;
                    } else {
                        propertyType = ColumnTypeImpl.valueOf(propertyTypeName);
                    }
                    columnNames[j] = propertyName;
                    columnTypes[j] = propertyType;
                }

                tableDefs[i] = new SimpleTableDef(docType, columnNames, columnTypes);
            }
        }

        return new ElasticSearchDatastore(name, ClientType.TRANSPORT, hostname, port, clusterName, indexName,
                tableDefs, username, password, ssl, keystorePath, keystorePassword);
    }

    private Datastore createDatastore(String name, JsonDatastoreType datastoreType) {
        final String filename = getStringVariable("filename", datastoreType.getFilename());
        final Resource resource = _interceptor.createResource(filename);
        return new JsonDatastore(name, resource);
    }

    private Datastore createDatastore(String name, HbaseDatastoreType datastoreType) {
        final String zookeeperHostname = getStringVariable("zookeeperHostname", datastoreType.getZookeeperHostname());
        final int zookeeperPort = getIntegerVariable("zookeeperPort", datastoreType.getZookeeperPort());
        final List<org.datacleaner.configuration.jaxb.HbaseDatastoreType.TableDef> tableDefList = datastoreType
                .getTableDef();

        final SimpleTableDef[] tableDefs;
        if (tableDefList.isEmpty()) {
            tableDefs = null;
        } else {
            tableDefs = new SimpleTableDef[tableDefList.size()];
            for (int i = 0; i < tableDefs.length; i++) {
                final org.datacleaner.configuration.jaxb.HbaseDatastoreType.TableDef tableDef = tableDefList.get(i);
                final String tableName = tableDef.getName();
                final List<org.datacleaner.configuration.jaxb.HbaseDatastoreType.TableDef.Column> columnList = tableDef
                        .getColumn();
                final String[] columnNames = new String[columnList.size()];
                final ColumnType[] columnTypes = new ColumnType[columnList.size()];
                for (int j = 0; j < columnTypes.length; j++) {
                    final Column column = columnList.get(j);
                    final String columnName;
                    final String family = column.getFamily();
                    if (Strings.isNullOrEmpty(family)) {
                        columnName = column.getName();
                    } else {
                        columnName = family + ":" + column.getName();
                    }
                    final String columnTypeName = column.getType();
                    final ColumnType columnType;
                    if (StringUtils.isNullOrEmpty(columnTypeName)) {
                        columnType = ColumnType.STRING;
                    } else {
                        columnType = ColumnTypeImpl.valueOf(columnTypeName);
                    }
                    columnNames[j] = columnName;
                    columnTypes[j] = columnType;
                }

                tableDefs[i] = new SimpleTableDef(tableName, columnNames, columnTypes);
            }
        }
        return new HBaseDatastore(name, zookeeperHostname, zookeeperPort, tableDefs);
    }

    private Datastore createDatastore(String name, SalesforceDatastoreType datastoreType) {
        String username = getStringVariable("username", datastoreType.getUsername());
        String password = getPasswordVariable("password", datastoreType.getPassword());
        String securityToken = getStringVariable("securityToken", datastoreType.getSecurityToken());
        String endpointUrl = getStringVariable("endpointUrl", datastoreType.getEndpointUrl());
        return new SalesforceDatastore(name, username, password, securityToken, endpointUrl);
    }

    private Datastore createDatastore(String name, SugarCrmDatastoreType datastoreType) {
        String baseUrl = getStringVariable("baseUrl", datastoreType.getBaseUrl());
        String username = getStringVariable("username", datastoreType.getUsername());
        String password = getPasswordVariable("password", datastoreType.getPassword());
        return new SugarCrmDatastore(name, baseUrl, username, password);
    }

    private Datastore createDatastore(String name, MongodbDatastoreType mongodbDatastoreType) {
        String hostname = getStringVariable("hostname", mongodbDatastoreType.getHostname());
        Integer port = getIntegerVariable("port", mongodbDatastoreType.getPort());
        String databaseName = getStringVariable("databaseName", mongodbDatastoreType.getDatabaseName());
        String username = getStringVariable("username", mongodbDatastoreType.getUsername());
        String password = getPasswordVariable("password", mongodbDatastoreType.getPassword());

        List<org.datacleaner.configuration.jaxb.MongodbDatastoreType.TableDef> tableDefList = mongodbDatastoreType
                .getTableDef();
        final SimpleTableDef[] tableDefs;
        if (tableDefList.isEmpty()) {
            tableDefs = null;
        } else {
            tableDefs = new SimpleTableDef[tableDefList.size()];
            for (int i = 0; i < tableDefs.length; i++) {
                final org.datacleaner.configuration.jaxb.MongodbDatastoreType.TableDef tableDef = tableDefList.get(i);
                final String collectionName = tableDef.getCollection();
                final List<org.datacleaner.configuration.jaxb.MongodbDatastoreType.TableDef.Property> propertyList = tableDef
                        .getProperty();
                final String[] propertyNames = new String[propertyList.size()];
                final ColumnType[] columnTypes = new ColumnType[propertyList.size()];
                for (int j = 0; j < columnTypes.length; j++) {
                    final String propertyName = propertyList.get(j).getName();
                    final String propertyTypeName = propertyList.get(j).getType();
                    final ColumnType propertyType;
                    if (StringUtils.isNullOrEmpty(propertyTypeName)) {
                        propertyType = ColumnType.STRING;
                    } else {
                        propertyType = ColumnTypeImpl.valueOf(propertyTypeName);
                    }
                    propertyNames[j] = propertyName;
                    columnTypes[j] = propertyType;
                }

                tableDefs[i] = new SimpleTableDef(collectionName, propertyNames, columnTypes);
            }
        }

        MongoDbDatastore ds = new MongoDbDatastore(name, hostname, port, databaseName, username, password, tableDefs);
        return ds;
    }

    private Datastore createDatastore(String name, CouchdbDatastoreType couchdbDatastoreType) {
        final String hostname = getStringVariable("hostname", couchdbDatastoreType.getHostname());
        final Integer port = getIntegerVariable("port", couchdbDatastoreType.getPort());
        final String username = getStringVariable("username", couchdbDatastoreType.getUsername());
        final String password = getPasswordVariable("password", couchdbDatastoreType.getPassword());
        final boolean sslEnabled = getBooleanVariable("ssl", couchdbDatastoreType.isSsl(), false);

        final List<org.datacleaner.configuration.jaxb.CouchdbDatastoreType.TableDef> tableDefList = couchdbDatastoreType
                .getTableDef();
        final SimpleTableDef[] tableDefs;
        if (tableDefList.isEmpty()) {
            tableDefs = null;
        } else {
            tableDefs = new SimpleTableDef[tableDefList.size()];
            for (int i = 0; i < tableDefs.length; i++) {
                org.datacleaner.configuration.jaxb.CouchdbDatastoreType.TableDef tableDef = tableDefList.get(i);
                String databaseName = tableDef.getDatabase();
                List<org.datacleaner.configuration.jaxb.CouchdbDatastoreType.TableDef.Field> fieldList = tableDef
                        .getField();
                String[] propertyNames = new String[fieldList.size()];
                ColumnType[] columnTypes = new ColumnType[fieldList.size()];
                for (int j = 0; j < columnTypes.length; j++) {
                    String propertyName = fieldList.get(j).getName();
                    String propertyTypeName = fieldList.get(j).getType();
                    final ColumnType propertyType;
                    if (StringUtils.isNullOrEmpty(propertyTypeName)) {
                        propertyType = ColumnType.STRING;
                    } else {
                        propertyType = ColumnTypeImpl.valueOf(propertyTypeName);
                    }
                    propertyNames[j] = propertyName;
                    columnTypes[j] = propertyType;
                }

                tableDefs[i] = new SimpleTableDef(databaseName, propertyNames, columnTypes);
            }
        }

        final CouchDbDatastore ds = new CouchDbDatastore(name, hostname, port, username, password, sslEnabled,
                tableDefs);
        return ds;
    }

    private Datastore createDatastore(String name, PojoDatastoreType pojoDatastore,
            DataCleanerConfigurationImpl temporaryConfiguration) {
        final JaxbPojoDatastoreAdaptor adaptor = new JaxbPojoDatastoreAdaptor(temporaryConfiguration);
        final PojoDatastore datastore = adaptor.read(pojoDatastore);
        return datastore;
    }

    private Datastore createDatastore(String name, OpenOfficeDatabaseDatastoreType odbDatastoreType) {
        final String filenamePath = getStringVariable("filename", odbDatastoreType.getFilename());
        final String filename = createFilename(filenamePath);
        final OdbDatastore ds = new OdbDatastore(name, filename);
        return ds;
    }

    private Datastore createDatastore(String name, DbaseDatastoreType dbaseDatastoreType) {
        final String filenamePath = getStringVariable("filename", dbaseDatastoreType.getFilename());
        final String filename = createFilename(filenamePath);
        final DbaseDatastore ds = new DbaseDatastore(name, filename);
        return ds;
    }

    private Datastore createDatastore(String name, ExcelDatastoreType excelDatastoreType) {
        final String filename = getStringVariable("filename", excelDatastoreType.getFilename());
        final Resource resource = _interceptor.createResource(filename);
        final ExcelDatastore ds = new ExcelDatastore(name, resource, filename);
        return ds;
    }

    private Datastore createDatastore(String name, XmlDatastoreType xmlDatastoreType) {
        final String filenamePath = getStringVariable("filename", xmlDatastoreType.getFilename());
        final String filename = createFilename(filenamePath);
        final List<TableDef> tableDefList = xmlDatastoreType.getTableDef();
        final XmlSaxTableDef[] tableDefs;
        if (tableDefList.isEmpty()) {
            tableDefs = null;
        } else {
            tableDefs = new XmlSaxTableDef[tableDefList.size()];
            for (int i = 0; i < tableDefs.length; i++) {
                final String rowXpath = tableDefList.get(i).getRowXpath();
                final String[] valueXpaths = tableDefList.get(i).getValueXpath().toArray(new String[0]);
                tableDefs[i] = new XmlSaxTableDef(rowXpath, valueXpaths);
            }
        }

        XmlDatastore ds = new XmlDatastore(name, filename, tableDefs);
        return ds;
    }

    private Datastore createDatastore(String name, AccessDatastoreType accessDatastoreType) {
        final String filenamePath = getStringVariable("filename", accessDatastoreType.getFilename());
        final String filename = createFilename(filenamePath);
        final AccessDatastore ds = new AccessDatastore(name, filename);
        return ds;
    }

    private Datastore createDatastore(String name, SasDatastoreType sasDatastoreType) {
        final String directoryPath = getStringVariable("directory", sasDatastoreType.getDirectory());
        final File directory = new File(directoryPath);
        final SasDatastore ds = new SasDatastore(name, directory);
        return ds;
    }

    private Datastore createDatastore(String name, FixedWidthDatastoreType fixedWidthDatastore) {
        @SuppressWarnings("deprecation")
        final String filename = _interceptor.createFilename(getStringVariable("filename",
                fixedWidthDatastore.getFilename()));
        String encoding = getStringVariable("encoding", fixedWidthDatastore.getEncoding());
        if (!StringUtils.isNullOrEmpty(encoding)) {
            encoding = FileHelper.UTF_8_ENCODING;
        }

        final boolean failOnInconsistencies = getBooleanVariable("failOnInconsistencies",
                fixedWidthDatastore.isFailOnInconsistencies(), true);

        Integer headerLineNumber = getIntegerVariable("headerLineNumber", fixedWidthDatastore.getHeaderLineNumber());
        if (headerLineNumber == null) {
            headerLineNumber = FixedWidthConfiguration.DEFAULT_COLUMN_NAME_LINE;
        }

        final WidthSpecification widthSpecification = fixedWidthDatastore.getWidthSpecification();

        final FixedWidthDatastore ds;
        final Integer fixedValueWidth = getIntegerVariable("fixedValueWidth", widthSpecification.getFixedValueWidth());
        if (fixedValueWidth == null) {
            final List<Integer> valueWidthsBoxed = widthSpecification.getValueWidth();
            int[] valueWidths = new int[valueWidthsBoxed.size()];
            for (int i = 0; i < valueWidths.length; i++) {
                valueWidths[i] = valueWidthsBoxed.get(i).intValue();
            }
            ds = new FixedWidthDatastore(name, filename, encoding, valueWidths, failOnInconsistencies,
                    headerLineNumber.intValue());
        } else {
            ds = new FixedWidthDatastore(name, filename, encoding, fixedValueWidth, failOnInconsistencies,
                    headerLineNumber.intValue());
        }
        return ds;
    }

    private Datastore createDatastore(String name, JdbcDatastoreType jdbcDatastoreType) {
        JdbcDatastore ds;

        final TableTypes jaxbTableTypes = jdbcDatastoreType.getTableTypes();
        final TableType[] tableTypes;
        if (jaxbTableTypes == null) {
            tableTypes = null;
        } else {
            final List<TableTypeEnum> jaxbTableTypeList = jaxbTableTypes.getTableType();
            tableTypes = new TableType[jaxbTableTypeList.size()];
            for (int i = 0; i < tableTypes.length; i++) {
                final TableTypeEnum tableTypeEnum = jaxbTableTypeList.get(i);
                tableTypes[i] = TableType.valueOf(tableTypeEnum.toString());
            }
        }

        final String catalogName = getStringVariable("catalogName", jdbcDatastoreType.getCatalogName());

        final String datasourceJndiUrl = getStringVariable("jndiUrl", jdbcDatastoreType.getDatasourceJndiUrl());
        if (datasourceJndiUrl == null) {
            final String url = getStringVariable("url", jdbcDatastoreType.getUrl());
            final String driver = getStringVariable("driver", jdbcDatastoreType.getDriver());
            final String username = getStringVariable("username", jdbcDatastoreType.getUsername());
            final String password = getPasswordVariable("password", jdbcDatastoreType.getPassword());
            boolean multipleConnections = getBooleanVariable("multipleConnections",
                    jdbcDatastoreType.isMultipleConnections(), true);

            ds = new JdbcDatastore(name, url, driver, username, password, multipleConnections, tableTypes, catalogName);
        } else {
            ds = new JdbcDatastore(name, datasourceJndiUrl, tableTypes, catalogName);
        }
        return ds;
    }

    private Datastore createDatastore(String name, CsvDatastoreType csvDatastoreType) {
        final String filename = getStringVariable("filename", csvDatastoreType.getFilename());
        final Resource resource = _interceptor.createResource(filename);

        final String quoteCharString = getStringVariable("quoteChar", csvDatastoreType.getQuoteChar());
        final char quoteChar = getChar(quoteCharString, CsvConfiguration.DEFAULT_QUOTE_CHAR,
                CsvConfiguration.NOT_A_CHAR);

        final String separatorCharString = getStringVariable("separatorChar", csvDatastoreType.getSeparatorChar());
        final char separatorChar = getChar(separatorCharString, CsvConfiguration.DEFAULT_SEPARATOR_CHAR,
                CsvConfiguration.NOT_A_CHAR);

        final String escapeCharString = getStringVariable("escapeChar", csvDatastoreType.getEscapeChar());
        final char escapeChar = getChar(escapeCharString, CsvConfiguration.DEFAULT_ESCAPE_CHAR,
                CsvConfiguration.NOT_A_CHAR);

        String encoding = getStringVariable("encoding", csvDatastoreType.getEncoding());
        if (StringUtils.isNullOrEmpty(encoding)) {
            encoding = FileHelper.UTF_8_ENCODING;
        }

        final boolean failOnInconsistencies = getBooleanVariable("failOnInconsistencies",
                csvDatastoreType.isFailOnInconsistencies(), true);

        final boolean multilineValues = getBooleanVariable("multilineValues", csvDatastoreType.isMultilineValues(),
                true);

        Integer headerLineNumber = getIntegerVariable("headerLineNumber", csvDatastoreType.getHeaderLineNumber());
        if (headerLineNumber == null) {
            headerLineNumber = CsvConfiguration.DEFAULT_COLUMN_NAME_LINE;
        }

        final CsvDatastore ds = new CsvDatastore(name, resource, filename, quoteChar, separatorChar, escapeChar,
                encoding, failOnInconsistencies, multilineValues, headerLineNumber);
        return ds;
    }

    private char getChar(String charString, char ifNull, char ifBlank) {
        if (charString == null) {
            return ifNull;
        }
        if ("".equals(charString)) {
            return ifBlank;
        }
        if (charString.length() == 1) {
            return charString.charAt(0);
        }
        if ("\\t".equals(charString)) {
            return '\t';
        }
        if ("\\\n".equals(charString)) {
            return '\n';
        }
        if ("\\r".equals(charString)) {
            return '\r';
        }
        if ("\\\\".equals(charString)) {
            return '\\';
        }
        if ("NOT_A_CHAR".equals(charString)) {
            return CsvConfiguration.NOT_A_CHAR;
        }
        logger.warn("Char string contained more than 1 character and was not identified as a special char: '{}'",
                charString);
        return charString.charAt(0);
    }

    private void addVariablePath(String name) {
        name = StringUtils.toCamelCase(name);
        _variablePathBuilder.add(name);
    }

    private void removeVariablePath() {
        _variablePathBuilder.pollLast();
    }

    private String getStringVariable(String key, String valueIfNull) {
        final StringBuilder sb = new StringBuilder();
        for (final String keyElement : _variablePathBuilder) {
            if (sb.length() > 0) {
                sb.append('.');
            }
            sb.append(keyElement);
        }
        sb.append('.');
        sb.append(key);

        final String variablePath = sb.toString();
        final String value = _interceptor.getPropertyOverride(variablePath);
        if (value == null) {
            return valueIfNull;
        }
        logger.info("Overriding variable '{}' with value: {}", variablePath, value);
        return value;
    }

    private String getPasswordVariable(String key, String valueIfNull) {
        final String possiblyEncodedPassword = getStringVariable(key, valueIfNull);
        if (possiblyEncodedPassword == null) {
            return null;
        }
        if (possiblyEncodedPassword.startsWith(ENCODED_PASSWORD_PREFIX)) {
            return SecurityUtils.decodePassword(possiblyEncodedPassword.substring(ENCODED_PASSWORD_PREFIX.length()));
        }
        return possiblyEncodedPassword;
    }

    public Integer getIntegerVariable(String key, Integer valueIfNull) {
        String value = getStringVariable(key, null);
        if (value == null) {
            return valueIfNull;
        }
        return Integer.parseInt(value);
    }

    private boolean getBooleanVariable(String key, Boolean valueIfNull, boolean valueIfNull2) {
        String value = getStringVariable(key, null);
        if (StringUtils.isNullOrEmpty(value)) {
            if (valueIfNull == null) {
                return valueIfNull2;
            }
            return valueIfNull;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Checks if a string is a valid name of a component.
     * 
     * @param name
     *            the name to be validated
     * @param type
     *            the type of component (used for error messages)
     * @param previousEntries
     *            the previous entries of that component type (for uniqueness
     *            check)
     * @throws IllegalStateException
     *             if the name is invalid
     */
    private static void checkName(final String name, Class<?> type, final Map<String, ?> previousEntries)
            throws IllegalStateException {
        if (StringUtils.isNullOrEmpty(name)) {
            throw new IllegalStateException(type.getSimpleName() + " name cannot be null");
        }
        if (previousEntries.containsKey(name)) {
            throw new IllegalStateException(type.getSimpleName() + " name is not unique: " + name);
        }
    }

    /**
     * Checks if a string is a valid name of a component.
     * 
     * @param name
     *            the name to be validated
     * @param type
     *            the type of component (used for error messages)
     * @param previousEntries
     *            the previous entries of that component type (for uniqueness
     *            check)
     * @throws IllegalStateException
     *             if the name is invalid
     */
    private static void checkName(String name, Class<?> type, List<? extends ReferenceData> previousEntries)
            throws IllegalStateException {
        if (StringUtils.isNullOrEmpty(name)) {
            throw new IllegalStateException(type.getSimpleName() + " name cannot be null");
        }
        for (ReferenceData referenceData : previousEntries) {
            if (name.equals(referenceData.getName())) {
                throw new IllegalStateException(type.getSimpleName() + " name is not unique: " + name);
            }
        }
    }

    private void updateTaskRunnerIfSpecified(Configuration configuration,
            TemporaryMutableDataCleanerEnvironment environment, DataCleanerConfiguration temporaryConfiguration) {
        final SinglethreadedTaskrunnerType singlethreadedTaskrunner = configuration.getSinglethreadedTaskrunner();
        final MultithreadedTaskrunnerType multithreadedTaskrunner = configuration.getMultithreadedTaskrunner();
        final CustomElementType customTaskrunner = configuration.getCustomTaskrunner();

        if (singlethreadedTaskrunner != null) {
            final TaskRunner taskRunner = new SingleThreadedTaskRunner();
            environment.setTaskRunner(taskRunner);
        } else if (multithreadedTaskrunner != null) {
            Short maxThreads = multithreadedTaskrunner.getMaxThreads();
            final TaskRunner taskRunner;
            if (maxThreads != null) {
                taskRunner = new MultiThreadedTaskRunner(maxThreads.intValue());
            } else {
                taskRunner = new MultiThreadedTaskRunner();
            }
            environment.setTaskRunner(taskRunner);
        } else if (customTaskrunner != null) {
            final TaskRunner taskRunner = createCustomElement(customTaskrunner, TaskRunner.class,
                    temporaryConfiguration, true);
            environment.setTaskRunner(taskRunner);
        }
    }

    /**
     * Creates a custom component based on an element which specified just a
     * class name and an optional set of properties.
     * 
     * @param <E>
     * @param customElementType
     *            the JAXB custom element type
     * @param expectedClazz
     *            an expected class or interface that the component should honor
     * @param configuration
     *            the DataCleaner configuration (may be temporary) in use
     * @param initialize
     *            whether or not to call any initialize methods on the component
     *            (reference data should not be initialized, while eg. custom
     *            task runners support this.
     * @return the custom component
     */
    private <E> E createCustomElement(CustomElementType customElementType, Class<E> expectedClazz,
            DataCleanerConfiguration configuration, boolean initialize) {
        final InjectionManager injectionManager = configuration.getEnvironment().getInjectionManagerFactory()
                .getInjectionManager(configuration);
        return createCustomElementInternal(customElementType, expectedClazz, injectionManager, initialize);
    }

    private <E> E createCustomElementInternal(CustomElementType customElementType, Class<E> expectedClazz,
            InjectionManager injectionManager, boolean initialize) {
        Class<?> foundClass;
        String className = customElementType.getClassName();

        assert className != null;
        try {
            foundClass = _interceptor.loadClass(className);
        } catch (Exception e) {
            logger.error("Failed to load class: {}", className);
            throw new IllegalStateException(e);
        }
        if (!ReflectionUtils.is(foundClass, expectedClazz)) {
            throw new IllegalStateException(className + " is not a valid " + expectedClazz);
        }

        @SuppressWarnings("unchecked")
        E result = (E) ReflectionUtils.newInstance(foundClass);

        ComponentDescriptor<?> descriptor = Descriptors.ofComponent(foundClass);

        StringConverter stringConverter = new StringConverter(injectionManager);

        List<Property> propertyTypes = customElementType.getProperty();
        if (propertyTypes != null) {
            for (Property property : propertyTypes) {
                String propertyName = property.getName();
                String propertyValue = property.getValue();

                ConfiguredPropertyDescriptor configuredProperty = descriptor.getConfiguredProperty(propertyName);
                if (configuredProperty == null) {
                    logger.warn("Missing configured property name: {}", propertyName);
                    if (logger.isInfoEnabled()) {
                        Set<ConfiguredPropertyDescriptor> configuredProperties = descriptor.getConfiguredProperties();
                        for (ConfiguredPropertyDescriptor configuredPropertyDescriptor : configuredProperties) {
                            logger.info("Available configured property name: {}, {}",
                                    configuredPropertyDescriptor.getName(), configuredPropertyDescriptor.getType());
                        }
                    }
                    throw new IllegalStateException("No such property in " + foundClass.getName() + ": " + propertyName);
                }

                Object configuredValue = stringConverter.deserialize(propertyValue, configuredProperty.getType(),
                        configuredProperty.getCustomConverter());

                configuredProperty.setValue(result, configuredValue);
            }
        }

        final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(injectionManager, true);
        lifeCycleHelper.assignProvidedProperties(descriptor, result);

        if (initialize) {
            lifeCycleHelper.initialize(descriptor, result);
        }

        return result;
    }
}
