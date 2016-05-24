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

import java.util.Arrays;
import java.util.Set;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.schema.TableType;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.HdfsResource;
import org.apache.metamodel.util.Resource;
import org.apache.metamodel.util.SimpleTableDef;
import org.apache.metamodel.xml.XmlDomDataContext;
import org.datacleaner.connection.CouchDbDatastore;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.DataHubDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.ElasticSearchDatastore;
import org.datacleaner.connection.ExcelDatastore;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.connection.JsonDatastore;
import org.datacleaner.connection.MongoDbDatastore;
import org.datacleaner.connection.SalesforceDatastore;
import org.datacleaner.reference.DatastoreDictionary;
import org.datacleaner.reference.DatastoreSynonymCatalog;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.RegexStringPattern;
import org.datacleaner.reference.SimpleDictionary;
import org.datacleaner.reference.SimpleStringPattern;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.reference.TextFileDictionary;
import org.datacleaner.reference.TextFileSynonymCatalog;
import org.datacleaner.server.DirectConnectionHadoopClusterInformation;
import org.datacleaner.server.DirectoryBasedHadoopClusterInformation;
import org.datacleaner.server.EnvironmentBasedHadoopClusterInformation;
import org.datacleaner.server.HadoopClusterInformation;
import org.datacleaner.util.HadoopResource;
import org.datacleaner.util.SecurityUtils;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.xml.XmlUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Strings;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Utility class for writing configuration elements to the XML format of
 * conf.xml.
 * 
 * Generally speaking, XML elements created by this class, and placed in a the
 * <datastore-catalog> and <reference-data-catalog> elements of conf.xml, will
 * be readable by {@link JaxbConfigurationReader}.
 */
public class DomConfigurationWriter {

    private final Document _document;

    public DomConfigurationWriter() {
        _document = XmlUtils.createDocument();
    }

    public DomConfigurationWriter(Resource resource) {
        _document = resource.read(XmlUtils::parseDocument);

    }

    public DomConfigurationWriter(Document document) {
        _document = document;
    }

    /**
     * Determines if the given datastore is externalizable by this object.
     *
     * @param serverInformation
     * @return
     */
    public boolean isExternalizable(final ServerInformation serverInformation) {
        if(serverInformation == null) {
            return false;
        }

        if (serverInformation instanceof HadoopClusterInformation) {
            return true;
        }

        return false;
    }

    /**
     * Determines if the given datastore is externalizable by this object.
     *
     * @param datastore
     * @return
     */
    public boolean isExternalizable(final Datastore datastore) {
        if (datastore == null) {
            return false;
        }

        if (datastore instanceof JdbcDatastore) {
            return true;
        }

        if (datastore instanceof CsvDatastore) {
            final Resource resource = ((CsvDatastore) datastore).getResource();
            if (resource instanceof FileResource) {
                return true;
            }
            if (resource instanceof HdfsResource) {
                return true;
            }
        }

        if (datastore instanceof ExcelDatastore) {
            final Resource resource = ((ExcelDatastore) datastore).getResource();
            if (resource instanceof FileResource) {
                return true;
            }
        }

        if (datastore instanceof ElasticSearchDatastore) {
            final SimpleTableDef[] tableDefs = ((ElasticSearchDatastore) datastore).getTableDefs();
            if (tableDefs == null) {
                return true;
            }
        }

        if (datastore instanceof MongoDbDatastore) {
            final SimpleTableDef[] tableDefs = ((MongoDbDatastore) datastore).getTableDefs();
            if (tableDefs == null) {
                return true;
            }
        }

        if (datastore instanceof CouchDbDatastore) {
            final SimpleTableDef[] tableDefs = ((CouchDbDatastore) datastore).getTableDefs();
            if (tableDefs == null) {
                return true;
            }
        }

        if (datastore instanceof SalesforceDatastore) {
            return true;
        }

        if (datastore instanceof DataHubDatastore) {
            return true;
        }
        
        if (datastore instanceof JsonDatastore){
            return true; 
        }

        return false;
    }

    public boolean isExternalizable(Dictionary dict) {
        return dict instanceof SimpleDictionary || dict instanceof TextFileDictionary
                || dict instanceof DatastoreDictionary;
    }

    public boolean isExternalizable(SynonymCatalog sc) {
        return sc instanceof TextFileSynonymCatalog || sc instanceof DatastoreSynonymCatalog;
    }

    public boolean isExternalizable(StringPattern sp) {
        return sp instanceof SimpleStringPattern || sp instanceof RegexStringPattern;
    }

    /**
     * Removes a Hadoop cluster by its name, if it exists and is recognizeable by the
     * externalizer.
     *
     * @param serverName
     * @return true if a server information element was removed from the XML document.
     */
    public boolean removeHadoopClusterServerInformation(final String serverName) {
        final Element serverInformationCatalogElement = getServerInformationCatalogElement();
        final Element hadoopClustersElement = getOrCreateChildElementByTagName(serverInformationCatalogElement,
                "hadoop-clusters");
        return removeChildElementByNameAttribute(serverName, hadoopClustersElement);
    }

    /**
     * Removes a datastore by its name, if it exists and is recognizeable by the
     * externalizer.
     * 
     * @param datastoreName
     * @return true if a datastore element was removed from the XML document.
     */
    public boolean removeDatastore(final String datastoreName) {
        final Element datastoreCatalogElement = getDatastoreCatalogElement();
        return removeChildElementByNameAttribute(datastoreName, datastoreCatalogElement);
    }

    /**
     * Removes a dictionary by its name, if it exists and is recognizable by the
     * externalizer.
     * 
     * @param dictionaryName
     * @return true if dictionary element was removed from the XML document
     */
    public boolean removeDictionary(final String dictionaryName) {
        final Element dictionariesElement = getDictionariesElement();
        return removeChildElementByNameAttribute(dictionaryName, dictionariesElement);
    }

    /**
     * Removes a synonym catalog by its name, if it exists and is recognizable
     * by the externalizer.
     * 
     * @param synonymCatalogName
     * @return true if dictionary element was removed from the XML document
     */
    public boolean removeSynonymCatalog(final String synonymCatalogName) {
        final Element synonymCatalogsElement = getSynonymCatalogsElement();
        return removeChildElementByNameAttribute(synonymCatalogName, synonymCatalogsElement);
    }

    /**
     * Removes a string pattern by its name, if it exists and is recognizable by
     * the externalizer.
     * 
     * @param stringPatternName
     * @return true if string pattern element was removed from the XML document
     */
    public boolean removeStringPattern(final String stringPatternName) {
        final Element stringPatternsElement = getStringPatternsElement();
        return removeChildElementByNameAttribute(stringPatternName, stringPatternsElement);
    }

    private boolean removeChildElementByNameAttribute(final String dictionaryName, final Element dictionariesElement) {
        final NodeList childNodes = dictionariesElement.getChildNodes();
        final int length = childNodes.getLength();
        for (int i = 0; i < length; i++) {
            final Node node = childNodes.item(i);
            if (node instanceof Element) {
                final Element element = (Element) node;
                final Attr[] attributes = XmlDomDataContext.getAttributes(element);
                for (Attr attr : attributes) {
                    if ("name".equals(attr.getName())) {
                        final String value = attr.getValue();
                        if (dictionaryName.equals(value)) {
                            // we have a match
                            dictionariesElement.removeChild(element);

                            onDocumentChanged(getDocument());

                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public Element externalize(ServerInformation serverInformation) throws UnsupportedOperationException {
        if (serverInformation == null) {
            throw new IllegalArgumentException("ServerInformation cannot be null");
        }

        final Element elem;

        if (serverInformation instanceof HadoopClusterInformation) {
            elem = toElement((HadoopClusterInformation) serverInformation);
        } else {
            throw new UnsupportedOperationException("Non-supported serverInformation: " + serverInformation);
        }

        final Element serverInformationCatalogElement = getHadoopClustersElement();
        serverInformationCatalogElement.appendChild(elem);

        onDocumentChanged(getDocument());

        return elem;
    }

    /**
     * Externalizes the given datastore
     *
     * @param datastore
     * @return
     * @throws UnsupportedOperationException
     */
    public Element externalize(Datastore datastore) throws UnsupportedOperationException {
        if (datastore == null) {
            throw new IllegalArgumentException("Datastore cannot be null");
        }

        final Element elem;

        if (datastore instanceof CsvDatastore) {
            final Resource resource = ((CsvDatastore) datastore).getResource();
            final String filename = toFilename(resource);
            elem = toElement((CsvDatastore) datastore, filename);
        } else if (datastore instanceof ExcelDatastore) {
            final Resource resource = ((ExcelDatastore) datastore).getResource();
            final String filename = toFilename(resource);
            elem = toElement((ExcelDatastore) datastore, filename);
        } else if (datastore instanceof JdbcDatastore) {
            elem = toElement((JdbcDatastore) datastore);
        } else if (datastore instanceof ElasticSearchDatastore) {
            elem = toElement((ElasticSearchDatastore) datastore);
        } else if (datastore instanceof MongoDbDatastore) {
            elem = toElement((MongoDbDatastore) datastore);
        } else if (datastore instanceof CouchDbDatastore) {
            elem = toElement((CouchDbDatastore) datastore);
        } else if (datastore instanceof SalesforceDatastore) {
            elem = toElement((SalesforceDatastore) datastore);
        } else if (datastore instanceof DataHubDatastore) {
            elem = toElement((DataHubDatastore) datastore);
        } else if (datastore instanceof JsonDatastore) {
            final Resource resource = ((JsonDatastore) datastore).getResource();
            final String filename = toFilename(resource);
            elem = toElement((JsonDatastore) datastore, filename);
        } else {
            throw new UnsupportedOperationException("Non-supported datastore: " + datastore);
        }

        final Element datastoreCatalogElement = getDatastoreCatalogElement();
        datastoreCatalogElement.appendChild(elem);

        onDocumentChanged(getDocument());

        return elem;
    }

    public Element externalize(Dictionary dictionary) throws UnsupportedOperationException {
        if (dictionary == null) {
            throw new IllegalArgumentException("Dictionary cannot be null");
        }

        final Element elem;

        if (dictionary instanceof SimpleDictionary) {
            elem = toElement((SimpleDictionary) dictionary);
        } else if (dictionary instanceof TextFileDictionary) {
            elem = toElement((TextFileDictionary) dictionary);
        } else if (dictionary instanceof DatastoreDictionary) {
            elem = toElement((DatastoreDictionary) dictionary);
        } else {
            throw new UnsupportedOperationException("Non-supported dictionary: " + dictionary);
        }

        final Element dictionariesElement = getDictionariesElement();
        dictionariesElement.appendChild(elem);

        onDocumentChanged(getDocument());

        return elem;
    }

    public Element externalize(SynonymCatalog sc) throws UnsupportedOperationException {
        if (sc == null) {
            throw new IllegalArgumentException("SynonymCatalog cannot be null");
        }

        final Element elem;

        if (sc instanceof TextFileSynonymCatalog) {
            elem = toElement((TextFileSynonymCatalog) sc);
        } else if (sc instanceof DatastoreSynonymCatalog) {
            elem = toElement((DatastoreSynonymCatalog) sc);
        } else {
            throw new UnsupportedOperationException("Non-supported synonym catalog: " + sc);
        }

        final Element synonymCatalogsElement = getSynonymCatalogsElement();
        synonymCatalogsElement.appendChild(elem);

        onDocumentChanged(getDocument());

        return elem;
    }

    public Element externalize(StringPattern sp) throws UnsupportedOperationException {
        if (sp == null) {
            throw new IllegalArgumentException("StringPattern cannot be null");
        }

        final Element elem;

        if (sp instanceof SimpleStringPattern) {
            elem = toElement((SimpleStringPattern) sp);
        } else if (sp instanceof RegexStringPattern) {
            elem = toElement((RegexStringPattern) sp);
        } else {
            throw new UnsupportedOperationException("Non-supported string pattern: " + sp);
        }

        final Element stringPatternsElement = getStringPatternsElement();
        stringPatternsElement.appendChild(elem);

        onDocumentChanged(getDocument());

        return elem;
    }

    private Element toElement(RegexStringPattern sp) {
        final Element elem = getDocument().createElement("regex-pattern");
        elem.setAttribute("name", sp.getName());
        if (!Strings.isNullOrEmpty(sp.getDescription())) {
            elem.setAttribute("description", sp.getDescription());
        }

        appendElement(elem, "expression", sp.getExpression());
        appendElement(elem, "match-entire-string", sp.isMatchEntireString());

        return elem;
    }

    private Element toElement(SimpleStringPattern sp) {
        final Element elem = getDocument().createElement("simple-pattern");
        elem.setAttribute("name", sp.getName());
        if (!Strings.isNullOrEmpty(sp.getDescription())) {
            elem.setAttribute("description", sp.getDescription());
        }

        appendElement(elem, "expression", sp.getExpression());

        return elem;
    }

    private Element toElement(DatastoreSynonymCatalog sc) {
        final Element elem = getDocument().createElement("datastore-synonym-catalog");
        elem.setAttribute("name", sc.getName());
        if (!Strings.isNullOrEmpty(sc.getDescription())) {
            elem.setAttribute("description", sc.getDescription());
        }

        appendElement(elem, "datastore-name", sc.getDatastoreName());
        appendElement(elem, "master-term-column-path", sc.getMasterTermColumnPath());

        final String[] synonymColumnPaths = sc.getSynonymColumnPaths();
        for (String path : synonymColumnPaths) {
            appendElement(elem, "synonym-column-path", path);
        }

        appendElement(elem, "load-into-memory", sc.isLoadIntoMemory());

        return elem;
    }

    private Element toElement(TextFileSynonymCatalog sc) {
        final Element elem = getDocument().createElement("text-file-synonym-catalog");
        elem.setAttribute("name", sc.getName());
        if (!Strings.isNullOrEmpty(sc.getDescription())) {
            elem.setAttribute("description", sc.getDescription());
        }

        appendElement(elem, "filename", sc.getFilename());
        appendElement(elem, "encoding", sc.getEncoding());
        appendElement(elem, "case-sensitive", sc.isCaseSensitive());

        return elem;
    }

    private Element toElement(SimpleDictionary dictionary) {
        final Element elem = getDocument().createElement("value-list-dictionary");
        elem.setAttribute("name", dictionary.getName());
        if (!Strings.isNullOrEmpty(dictionary.getDescription())) {
            elem.setAttribute("description", dictionary.getDescription());
        }

        final Set<String> values = dictionary.getValueSet();
        for (String value : values) {
            appendElement(elem, "value", value);
        }

        appendElement(elem, "case-sensitive", dictionary.isCaseSensitive());

        return elem;
    }

    private Element toElement(TextFileDictionary dictionary) {
        final Element elem = getDocument().createElement("text-file-dictionary");
        elem.setAttribute("name", dictionary.getName());
        if (!Strings.isNullOrEmpty(dictionary.getDescription())) {
            elem.setAttribute("description", dictionary.getDescription());
        }

        appendElement(elem, "filename", dictionary.getFilename());
        appendElement(elem, "encoding", dictionary.getEncoding());
        appendElement(elem, "case-sensitive", dictionary.isCaseSensitive());

        return elem;
    }

    private Element toElement(DatastoreDictionary dictionary) {
        final Element elem = getDocument().createElement("datastore-dictionary");
        elem.setAttribute("name", dictionary.getName());
        if (!Strings.isNullOrEmpty(dictionary.getDescription())) {
            elem.setAttribute("description", dictionary.getDescription());
        }

        appendElement(elem, "datastore-name", dictionary.getDatastoreName());
        appendElement(elem, "column-path", dictionary.getQualifiedColumnName());
        appendElement(elem, "load-into-memory", dictionary.isLoadIntoMemory());

        return elem;
    }

    /**
     * Overrideable method, invoked whenever the document has changed
     * 
     * @param document
     */
    protected void onDocumentChanged(Document document) {
    }

    /**
     * Creates a filename string to externalize, based on a given
     * {@link Resource}.
     * 
     * @param resource
     * @return
     * @throws UnsupportedOperationException
     */
    protected String toFilename(final Resource resource) throws UnsupportedOperationException {
        if (resource instanceof FileResource) {
            return ((FileResource) resource).getFile().getPath();
        }
        if(resource instanceof HadoopResource) {
            return ((HadoopResource) resource).getTemplatedPath();
        }
        if (resource instanceof HdfsResource) {
            return resource.getQualifiedPath();
        }

        throw new UnsupportedOperationException("Unsupported resource type: " + resource);
    }

    /**
     * Externalizes a {@link JdbcDatastore} to a XML element.
     * 
     * @param datastore
     * @return
     */
    public Element toElement(JdbcDatastore datastore) {
        final Element ds = getDocument().createElement("jdbc-datastore");
        ds.setAttribute("name", datastore.getName());
        if (!Strings.isNullOrEmpty(datastore.getDescription())) {
            ds.setAttribute("description", datastore.getDescription());
        }

        String jndiUrl = datastore.getDatasourceJndiUrl();
        if (Strings.isNullOrEmpty(jndiUrl)) {
            appendElement(ds, "url", datastore.getJdbcUrl());
            appendElement(ds, "driver", datastore.getDriverClass());
            appendElement(ds, "username", datastore.getUsername());
            appendElement(ds, "password", encodePassword(datastore.getPassword()));
            appendElement(ds, "multiple-connections", datastore.isMultipleConnections() + "");
        } else {
            appendElement(ds, "datasource-jndi-url", jndiUrl);
        }

        final TableType[] tableTypes = datastore.getTableTypes();
        if (tableTypes != null && tableTypes.length != 0 && !Arrays.equals(TableType.DEFAULT_TABLE_TYPES, tableTypes)) {
            final Element tableTypesElement = getDocument().createElement("table-types");
            ds.appendChild(tableTypesElement);

            for (final TableType tableType : tableTypes) {
                appendElement(tableTypesElement, "table-type", tableType.name());
            }
        }

        final String catalogName = datastore.getCatalogName();
        if (!Strings.isNullOrEmpty(catalogName)) {
            appendElement(ds, "catalog-name", catalogName);
        }

        return ds;
    }

    private String encodePassword(String password) {
        if (password == null) {
            return null;
        }

        return SecurityUtils.encodePasswordWithPrefix(password);
    }

    private String encodePassword(char[] password) {
        return encodePassword(new String(password));
    }

    /**
     * Externalizes a {@link HadoopClusterInformation} to a XML element.
     *
     * @param hadoopClusterInformation the hadoopClusterInformation to externalize
     * @return a XML element representing the datastore.
     */
    public Element toElement(HadoopClusterInformation hadoopClusterInformation) {
        final Element hadoopClusterElement = getDocument().createElement("hadoop-cluster");

        hadoopClusterElement.setAttribute("name", hadoopClusterInformation.getName());

        final String description = hadoopClusterInformation.getDescription();
        if (!Strings.isNullOrEmpty(description)) {
            hadoopClusterElement.setAttribute("description", description);
        }

        // These inherit each other, so order is important
        if (hadoopClusterInformation instanceof DirectConnectionHadoopClusterInformation) {
            appendElement(hadoopClusterElement, "namenode-url",
                    ((DirectConnectionHadoopClusterInformation) hadoopClusterInformation).getNameNodeUri().toString());
        } else if (hadoopClusterInformation instanceof EnvironmentBasedHadoopClusterInformation) {
            appendElement(hadoopClusterElement, "environment-configured", "");
        } else if (hadoopClusterInformation instanceof DirectoryBasedHadoopClusterInformation) {
            DirectoryBasedHadoopClusterInformation directoryBasedHadoopClusterInformation =
                    (DirectoryBasedHadoopClusterInformation) hadoopClusterInformation;
            Element directoriesElement = getDocument().createElement("directories");
            hadoopClusterElement.appendChild(directoriesElement);
            for (String directory : directoryBasedHadoopClusterInformation.getDirectories()) {
                appendElement(directoriesElement, "directory", directory);
            }
        } else {
            throw new UnsupportedOperationException("Unknown Hadoop cluster configuration");
        }

        return hadoopClusterElement;
    }

    /**
     * Externalizes a {@link ElasticSearchDatastore} to a XML element
     * 
     * @param datastore
     * @return
     */
    public Element toElement(ElasticSearchDatastore datastore) {
        final Element ds = getDocument().createElement("elasticsearch-datastore");
        ds.setAttribute("name", datastore.getName());
        if (!Strings.isNullOrEmpty(datastore.getDescription())) {
            ds.setAttribute("description", datastore.getDescription());
        }

        appendElement(ds, "hostname", datastore.getHostname());
        appendElement(ds, "port", datastore.getPort());
        appendElement(ds, "cluster-name", datastore.getClusterName());
        appendElement(ds, "index-name", datastore.getIndexName());
        appendElement(ds, "client-type", datastore.getClientType().name());
        appendElement(ds, "username", datastore.getUsername());
        appendElement(ds, "password", encodePassword(datastore.getPassword()));
        appendElement(ds, "ssl", datastore.getSsl());

        if (datastore.getSsl()) {
            appendElement(ds, "keystore-path", datastore.getKeystorePath());
            appendElement(ds, "keystore-password", encodePassword(datastore.getKeystorePassword()));
        }

        return ds;
    }

    /**
     * Externalizes a {@link MongoDbDatastore} to a XML element
     * 
     * @param datastore
     * @return
     */
    public Element toElement(MongoDbDatastore datastore) {
        final Element ds = getDocument().createElement("mongodb-datastore");
        ds.setAttribute("name", datastore.getName());
        if (!Strings.isNullOrEmpty(datastore.getDescription())) {
            ds.setAttribute("description", datastore.getDescription());
        }

        appendElement(ds, "hostname", datastore.getHostname());
        appendElement(ds, "port", datastore.getPort());
        appendElement(ds, "database-name", datastore.getDatabaseName());
        appendElement(ds, "username", datastore.getUsername());
        appendElement(ds, "password", encodePassword(datastore.getPassword()));

        return ds;
    }

    /**
     * Externalizes a {@link CouchDbDatastore} to a XML element
     * 
     * @param datastore
     * @return
     */
    public Element toElement(CouchDbDatastore datastore) {
        final Element ds = getDocument().createElement("couchdb-datastore");
        ds.setAttribute("name", datastore.getName());
        if (!Strings.isNullOrEmpty(datastore.getDescription())) {
            ds.setAttribute("description", datastore.getDescription());
        }

        appendElement(ds, "hostname", datastore.getHostname());
        appendElement(ds, "port", datastore.getPort());
        appendElement(ds, "username", datastore.getUsername());
        appendElement(ds, "password", encodePassword(datastore.getPassword()));
        appendElement(ds, "ssl", datastore.isSslEnabled());

        return ds;
    }

    /**
     * Externalizes a {@link CouchDbDatastore} to an XML element
     * 
     * @param datastore
     * @return
     */
    public Element toElement(SalesforceDatastore datastore) {
        final Element ds = getDocument().createElement("salesforce-datastore");
        ds.setAttribute("name", datastore.getName());
        if (!Strings.isNullOrEmpty(datastore.getDescription())) {
            ds.setAttribute("description", datastore.getDescription());
        }

        appendElement(ds, "username", datastore.getUsername());
        appendElement(ds, "password", encodePassword(datastore.getPassword()));
        appendElement(ds, "security-token", datastore.getSecurityToken());

        final String endpointUrl = datastore.getEndpointUrl();
        if (!Strings.isNullOrEmpty(endpointUrl)) {
            appendElement(ds, "endpoint-url", endpointUrl);
        }

        return ds;
    }

    /**
     * Externalizes a {@link DataHubDatastore} to an XML element
     * 
     * @param datastore
     * @return
     */
    private Element toElement(DataHubDatastore datastore) {
        final Element ds = getDocument().createElement("datahub-datastore");
        ds.setAttribute("name", datastore.getName());
        if (!isNullOrEmpty(datastore.getDescription())) {
            ds.setAttribute("description", datastore.getDescription());
        }

        appendElement(ds, "host", datastore.getHost());
        appendElement(ds, "port", datastore.getPort());
        appendElement(ds, "username", datastore.getUsername());
        appendElement(ds, "password", encodePassword(datastore.getPassword()));
        appendElement(ds, "https", datastore.isHttps());
        appendElement(ds, "acceptunverifiedsslpeers", datastore.isAcceptUnverifiedSslPeers());
        appendElement(ds, "datahubsecuritymode", datastore.getSecurityMode());

        return ds;
    }

    /**
     * Externalizes a {@link ExcelDatastore} to a XML element.
     * 
     * @param datastore
     * @param filename
     *            the filename/path to use in the XML element. Since the
     *            appropriate path will depend on the reading application's
     *            environment (supported {@link Resource} types), this specific
     *            property of the datastore is provided separately.
     * @return
     */
    public Element toElement(ExcelDatastore datastore, String filename) {
        final Element ds = getDocument().createElement("excel-datastore");

        ds.setAttribute("name", datastore.getName());
        if (!Strings.isNullOrEmpty(datastore.getDescription())) {
            ds.setAttribute("description", datastore.getDescription());
        }

        appendElement(ds, "filename", filename);

        return ds;
    }

    /**
     * Externalizes a {@link CsvDatastore} to a XML element.
     * 
     * @param datastore
     *            the datastore to externalize
     * @param filename
     *            the filename/path to use in the XML element. Since the
     *            appropriate path will depend on the reading application's
     *            environment (supported {@link Resource} types), this specific
     *            property of the datastore is provided separately.
     * @return a XML element representing the datastore.
     */
    public Element toElement(CsvDatastore datastore, String filename) {
        final Element datastoreElement = getDocument().createElement("csv-datastore");
        datastoreElement.setAttribute("name", datastore.getName());

        final String description = datastore.getDescription();
        if (!Strings.isNullOrEmpty(description)) {
            datastoreElement.setAttribute("description", description);
        }

        appendElement(datastoreElement, "filename", filename);
        appendElement(datastoreElement, "quote-char", datastore.getQuoteChar());
        appendElement(datastoreElement, "separator-char", datastore.getSeparatorChar());
        appendElement(datastoreElement, "escape-char", datastore.getEscapeChar());
        appendElement(datastoreElement, "encoding", datastore.getEncoding());
        appendElement(datastoreElement, "fail-on-inconsistencies", datastore.isFailOnInconsistencies());
        appendElement(datastoreElement, "multiline-values", datastore.isMultilineValues());
        appendElement(datastoreElement, "header-line-number", datastore.getHeaderLineNumber());

        return datastoreElement;
    }
    /**
     * Extrnalizes a Json datastore
     * @param datastore
     * @param filename
     * @return
     */
    public Element toElement(JsonDatastore datastore, String filename){
        final Element datastoreElement = getDocument().createElement("json-datastore"); 
        datastoreElement.setAttribute("name", datastore.getName());
        final String description = datastore.getDescription();
        if (!Strings.isNullOrEmpty(description)) {
            datastoreElement.setAttribute("description", description);
        }
        appendElement(datastoreElement, "filename", filename);
        
        return datastoreElement; 
    }

    /**
     * Gets the XML document that has been built.
     * 
     * @return
     */
    public final Document getDocument() {
        return _document;
    }

    /**
     * Gets the XML element that represents the {@link DatastoreCatalog}.
     * 
     * @return
     */
    public Element getDatastoreCatalogElement() {
        final Element configurationFileDocumentElement = getDocumentElement();

        final Element datastoreCatalogElement = getOrCreateChildElementByTagName(configurationFileDocumentElement,
                "datastore-catalog");
        if (datastoreCatalogElement == null) {
            throw new IllegalStateException("Could not find <datastore-catalog> element in configuration file");
        }
        return datastoreCatalogElement;
    }

    /**
     * Get the XML element that represents the {@link ServerInformationCatalog}.
     */
    public Element getServerInformationCatalogElement() {
        final Element configurationFileDocumentElement = getDocumentElement();
        return getOrCreateChildElementByTagName(configurationFileDocumentElement, "servers");
    }

    /**
     * Get the XML element that represents the hadoop cluster subset of {@link ServerInformationCatalog}.
     */
    public Element getHadoopClustersElement() {
        final Element hadoopClustersElement = getServerInformationCatalogElement();

        return getOrCreateChildElementByTagName(hadoopClustersElement, "hadoop-clusters");
    }


    /**
     * Gets the XML element that represents the dictionaries
     * 
     * @return
     */
    public Element getDictionariesElement() {
        final Element referenceDataCatalogElement = getReferenceDataCatalogElement();

        final Element dictionariesElement = getOrCreateChildElementByTagName(referenceDataCatalogElement,
                "dictionaries");
        if (dictionariesElement == null) {
            throw new IllegalStateException("Could not find <dictionaries> element in configuration file");
        }
        return dictionariesElement;
    }

    /**
     * Gets the XML element that represents the synonym catalogs
     * 
     * @return
     */
    public Element getSynonymCatalogsElement() {
        final Element referenceDataCatalogElement = getReferenceDataCatalogElement();

        final Element synonymCatalogsElement = getOrCreateChildElementByTagName(referenceDataCatalogElement,
                "synonym-catalogs");
        if (synonymCatalogsElement == null) {
            throw new IllegalStateException("Could not find <synonym-catalogs> element in configuration file");
        }
        return synonymCatalogsElement;
    }

    /**
     * Gets the XML element that represents the string patterns
     * 
     * @return
     */
    public Element getStringPatternsElement() {
        final Element referenceDataCatalogElement = getReferenceDataCatalogElement();

        final Element stringPatternsElement = getOrCreateChildElementByTagName(referenceDataCatalogElement,
                "string-patterns");
        if (stringPatternsElement == null) {
            throw new IllegalStateException("Could not find <string-patterns> element in configuration file");
        }
        return stringPatternsElement;
    }

    private Element getReferenceDataCatalogElement() {
        final Element configurationFileDocumentElement = getDocumentElement();

        final Element referenceDataCatalogElement = getOrCreateChildElementByTagName(configurationFileDocumentElement,
                "reference-data-catalog");
        if (referenceDataCatalogElement == null) {
            throw new IllegalStateException("Could not find <reference-data-catalog> element in configuration file");
        }
        return referenceDataCatalogElement;
    }

    private Element getDocumentElement() {
        final Document document = getDocument();
        Element documentElement = document.getDocumentElement();
        if (documentElement == null) {
            documentElement = document.createElement("configuration");
            documentElement.setAttribute("xmlns", "http://eobjects.org/analyzerbeans/configuration/1.0");
            document.appendChild(documentElement);
        }
        return documentElement;
    }

    private Element getOrCreateChildElementByTagName(Element element, String tagName) {
        Element elem = getChildElementByTagName(element, tagName);
        if (elem == null) {
            elem = getDocument().createElement(tagName);
            element.appendChild(elem);
        }
        return elem;
    }

    private Element getChildElementByTagName(Element element, String tagName) {
        final NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList == null) {
            return null;
        }
        final int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            final Node node = nodeList.item(i);
            if (node instanceof Element) {
                return (Element) node;
            }
        }
        return null;
    }

    private void appendElement(Element parent, String elementName, Object value) {
        if (value == null) {
            return;
        }

        if (value instanceof char[]) {
            value = new String((char[]) value);
        }

        String stringValue = value.toString();

        if (value instanceof Character) {
            final char c = ((Character) value).charValue();
            if (c == CsvConfiguration.NOT_A_CHAR) {
                stringValue = "NOT_A_CHAR";
            } else if (c == '\t') {
                stringValue = "\\t";
            } else if (c == '\n') {
                stringValue = "\\n";
            } else if (c == '\r') {
                stringValue = "\\r";
            }
        }

        final Element element = getDocument().createElement(elementName);
        element.setTextContent(stringValue);
        parent.appendChild(element);
    }


    public void addRemoteServer(String serverName, String url, String username, String password){
        Element descriptorProviderElement =
                getOrCreateChildElementByTagName(getDocumentElement(), "descriptor-providers");
        Element remoteComponentsElement =
                getOrCreateChildElementByTagName(descriptorProviderElement, "remote-components");

        Element serverElement = getDocument().createElement("server");
        remoteComponentsElement.appendChild(serverElement);

        if(!StringUtils.isNullOrEmpty(serverName)){
            appendElement(serverElement, "name", serverName);
        }

        if(!StringUtils.isNullOrEmpty(url)){
            appendElement(serverElement, "url", url);
        }
        appendElement(serverElement, "username", username);
        appendElement(serverElement, "password", SecurityUtils.encodePasswordWithPrefix(password));
        onDocumentChanged(getDocument());
    }

    public void updateRemoteServerCredentials(String serverName, String username, String password) {
        Element remoteComponents = getChildElementByTagName(getDocumentElement(), "remote-components");
        NodeList servers = remoteComponents.getElementsByTagName("server");
        for (int i = 0; i < servers.getLength(); i++) {
            if(servers.item(i) instanceof Element){
                Element server = (Element) servers.item(i);
                Element name = getChildElementByTagName(server, "name");
                if(name!= null && serverName.equals(name.getTextContent())){
                    Element usernameElemet = getOrCreateChildElementByTagName(server, "username");
                    usernameElemet.setTextContent(username);
                    Element passwordElement = getOrCreateChildElementByTagName(server, "password");
                    passwordElement.setTextContent(SecurityUtils.encodePasswordWithPrefix(password));
                    break;
                }
            }
        }
        onDocumentChanged(getDocument());
    }
}
