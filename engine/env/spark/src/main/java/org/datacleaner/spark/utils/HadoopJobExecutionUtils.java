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
package org.datacleaner.spark.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.HdfsResource;
import org.apache.metamodel.util.Resource;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DomConfigurationWriter;
import org.datacleaner.configuration.RemoteServerData;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.JsonDatastore;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.OutputDataStreamJob;
import org.datacleaner.reference.DatastoreDictionary;
import org.datacleaner.reference.DatastoreSynonymCatalog;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.xml.XmlUtils;

import com.google.common.collect.Iterators;

/**
 * Utility class for creating a job that runs on Hadoop with Spark.
 *
 */
public class HadoopJobExecutionUtils {

    public static boolean isValidSourceDatastore(Datastore datastore) {
        if (isHdfsResourcedDatastore(datastore)) {
            if (datastore instanceof CsvDatastore) {
                final CsvDatastore csvDatastore = (CsvDatastore) datastore;
                if (!isValidMultilines(csvDatastore) || !isValidEnconding(csvDatastore)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static boolean isHdfsResourcedDatastore(Datastore datastore) {
        if (datastore instanceof CsvDatastore) {
            final CsvDatastore csvDatastore = (CsvDatastore) datastore;
            final Resource resource = csvDatastore.getResource();
            if (!isHdfsResource(resource)) {
                return false;
            }
        } else if (datastore instanceof JsonDatastore) {
            final JsonDatastore jsonDatastore = (JsonDatastore) datastore;
            final Resource resource = jsonDatastore.getResource();
            if (!isHdfsResource(resource)) {
                return false;
            }
        } else {
            // other type of datastore
            return false;
        }
        return true;

    }

    public static boolean isValidEnconding(CsvDatastore datastore) {
        final CsvConfiguration csvConfiguration = datastore.getCsvConfiguration();
        final String encoding = csvConfiguration.getEncoding();
        if (!encoding.equals(FileHelper.UTF_8_ENCODING)) {
            return false;
        }
        return true;
    }

    public static boolean isValidMultilines(CsvDatastore datastore) {
        final CsvConfiguration csvConfiguration = datastore.getCsvConfiguration();
        if (csvConfiguration.isMultilineValues()) {
            return false;
        }
        return true;
    }

    private static boolean isHdfsResource(Resource resource) {
        if (resource instanceof HdfsResource) {
            return true;
        }
        return false;
    }

    public static boolean isSparkHomeSet() {
        final String property = System.getenv("SPARK_HOME");
        if (StringUtils.isNullOrEmpty(property)) {
            return false;
        }
        return true;
    }

    public static File createMinimalConfigurationFile(final DataCleanerConfiguration configuration,
            final AnalysisJob job) throws IOException {
        final File temporaryConfigurationFile = File.createTempFile("conf", "xml");

        // Use sets to be sure all entries are unique.
        final Set<Datastore> datastores = new HashSet<>();
        final Set<Dictionary> dictionaries = new HashSet<>();
        final Set<StringPattern> stringPatterns = new HashSet<>();
        final Set<SynonymCatalog> synonymCatalogs = new HashSet<>();

        addJobConfigurations(job, datastores, dictionaries, stringPatterns, synonymCatalogs);

        // Loop over all dictionaries to see if there are any dictionaries
        // stored in a data store, in which case the underlying data stores
        // should also be added to the data stores which are externalized.
        dictionaries.forEach(dictionary -> {
            if (dictionary instanceof DatastoreDictionary) {
                datastores.add(configuration.getDatastoreCatalog().getDatastore(((DatastoreDictionary) dictionary)
                        .getDatastoreName()));
            }
        });

        // Loop over all synonym catalogs to see if there are any synonym
        // catalogs stored in a data store, in which case the underlying data
        // stores should also be added to the data stores which are
        // externalized.
        synonymCatalogs.forEach(synonymCatalog -> {
            if (synonymCatalog instanceof DatastoreSynonymCatalog) {
                datastores.add(configuration.getDatastoreCatalog().getDatastore(
                        ((DatastoreSynonymCatalog) synonymCatalog).getDatastoreName()));
            }
        });

        final DomConfigurationWriter configurationWriter = new DomConfigurationWriter();

        datastores.stream().filter(configurationWriter::isExternalizable).forEach(configurationWriter::externalize);
        dictionaries.stream().filter(configurationWriter::isExternalizable).forEach(configurationWriter::externalize);
        stringPatterns.stream().filter(configurationWriter::isExternalizable).forEach(configurationWriter::externalize);
        synonymCatalogs.stream().filter(configurationWriter::isExternalizable).forEach(
                configurationWriter::externalize);

        addRemoteServersConfiguration(configuration, configurationWriter);

        XmlUtils.writeDocument(configurationWriter.getDocument(), new FileOutputStream(temporaryConfigurationFile));

        return temporaryConfigurationFile;
    }

    private static void addRemoteServersConfiguration(final DataCleanerConfiguration configuration,
            final DomConfigurationWriter configurationWriter) {
        final List<RemoteServerData> serverList = configuration.getEnvironment().getRemoteServerConfiguration()
                .getServerList();
        if (serverList != null) {
            for (RemoteServerData remoteServer : serverList) {
                configurationWriter.addRemoteServer(remoteServer.getServerName(), remoteServer.getUrl(), remoteServer
                        .getUsername(), remoteServer.getPassword());
            }
        }
    }

    private static void addJobConfigurations(final AnalysisJob job, final Set<Datastore> datastores,
            final Set<Dictionary> dictionaries, final Set<StringPattern> stringPatterns,
            final Set<SynonymCatalog> synonymCatalogs) {
        datastores.add(job.getDatastore());

        Iterators.concat(job.getAnalyzerJobs().iterator(), job.getFilterJobs().iterator(), job.getTransformerJobs()
                .iterator()).forEachRemaining(component -> {
                    component.getDescriptor().getConfiguredProperties().forEach(descriptor -> {
                        final Class<?> type = descriptor.getBaseType();

                        if (type == Datastore.class) {
                            datastores.addAll(getProperties(component, descriptor));
                        } else if (type == Dictionary.class) {
                            dictionaries.addAll(getProperties(component, descriptor));
                        } else if (type == StringPattern.class) {
                            stringPatterns.addAll(getProperties(component, descriptor));
                        } else if (type == SynonymCatalog.class) {
                            synonymCatalogs.addAll(getProperties(component, descriptor));
                        }
                    });

                    for (OutputDataStreamJob outputDataStreamJob : component.getOutputDataStreamJobs()) {
                        addJobConfigurations(outputDataStreamJob.getJob(), datastores, dictionaries, stringPatterns,
                                synonymCatalogs);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> getProperties(ComponentJob component, ConfiguredPropertyDescriptor descriptor) {
        if (descriptor.isArray()) {
            return Arrays.asList(((T[]) component.getConfiguration().getProperty(descriptor)));
        } else {
            return Collections.singletonList((T) component.getConfiguration().getProperty(descriptor));
        }
    }

    public static String getUrlReadyJobName(String jobName) {
        return jobName.replace(" ", "%20");
    }

}
