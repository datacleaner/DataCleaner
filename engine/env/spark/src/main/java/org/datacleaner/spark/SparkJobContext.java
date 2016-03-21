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
package org.datacleaner.spark;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.HdfsResource;
import org.apache.spark.api.java.JavaSparkContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.JaxbConfigurationReader;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.JaxbJobReader;
import org.datacleaner.job.OutputDataStreamJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.spark.utils.HdfsHelper;
import org.datacleaner.util.InputStreamToPropertiesMapFunc;
import org.datacleaner.util.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * A container for for values that need to be passed between Spark workers. All
 * the values need to be {@link Serializable} or loadable from those
 * {@link Serializable} properties.
 */
public class SparkJobContext implements Serializable {
    private static final String METADATA_PROPERTY_COMPONENT_INDEX = "org.datacleaner.spark.component.index";
    private static final Logger logger = LoggerFactory.getLogger(SparkJobContext.class);
    private static final String PROPERTY_RESULT_PATH = "datacleaner.result.hdfs.path";
    private static final String PROPERTY_RESULT_ENABLED = "datacleaner.result.hdfs.enabled";

    private static final long serialVersionUID = 1L;

    private final String _jobName;
    private final String _configurationXml;
    private final String _analysisJobXml;
    private final Map<String, String> _customProperties;
    private final List<SparkJobLifeCycleListener> _sparkJobLifeCycleListeners = new ArrayList<>();

    // cached/transient state
    private transient DataCleanerConfiguration _dataCleanerConfiguration;
    private transient AnalysisJobBuilder _analysisJobBuilder;
    private AnalysisJobBuilder _analysisJobBuilder1;

    public SparkJobContext(final URI dataCleanerConfigurationPath, final URI analysisJobXmlPath,
            final URI customPropertiesPath, final JavaSparkContext sparkContext) {
        final HdfsHelper hdfsHelper = new HdfsHelper(sparkContext);
        _jobName = getAnalysisJobName(analysisJobXmlPath);
        logger.info("Loading SparkJobContext for {} - job name '{}'", analysisJobXmlPath, _jobName);

        _configurationXml = hdfsHelper.readFile(dataCleanerConfigurationPath, true);
        if (Strings.isNullOrEmpty(_configurationXml)) {
            throw new IllegalArgumentException("Failed to read content from configuration file: "
                    + dataCleanerConfigurationPath);
        }

        _analysisJobXml = hdfsHelper.readFile(analysisJobXmlPath, true);
        if (Strings.isNullOrEmpty(_analysisJobXml)) {
            throw new IllegalArgumentException("Failed to read content from job file: " + analysisJobXmlPath);
        }

        final String propertiesString = hdfsHelper.readFile(customPropertiesPath);
        if (propertiesString == null) {
            _customProperties = Collections.emptyMap();
        } else {
            // this is a pretty ugly way to go back to the bytes to read the
            // properties - but works and is quick
            _customProperties = new InputStreamToPropertiesMapFunc().eval(new ByteArrayInputStream(propertiesString
                    .getBytes()));
        }
        validateCustomProperties();
    }

    public SparkJobContext(final String jobName, final String dataCleanerConfiguration, final String analysisJobXml,
            final Map<String, String> customProperties) {
        _jobName = jobName;
        _customProperties = customProperties;
        _configurationXml = dataCleanerConfiguration;
        _analysisJobXml = analysisJobXml;
        validateCustomProperties();
    }

    private void validateCustomProperties() {
        if (isResultEnabled()) {
            // ensure parsability of result path
            getResultPath();
        }
    }

    public DataCleanerConfiguration getConfiguration() {
        if (_dataCleanerConfiguration == null) {
            final JaxbConfigurationReader confReader = new JaxbConfigurationReader(
                    new SparkConfigurationReaderInterceptor(_customProperties));
            _dataCleanerConfiguration = confReader.read(createInputStream(_configurationXml));
        }
        return _dataCleanerConfiguration;
    }

    public String getJobName() {
        return _jobName;
    }

    private InputStream createInputStream(String fileContents) {
        try {
            final byte[] bytes = fileContents.getBytes("UTF-8");
            return new ByteArrayInputStream(bytes);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public AnalysisJob getAnalysisJob() {
        return getAnalysisJobBuilder().toAnalysisJob();
    }

    public AnalysisJobBuilder getAnalysisJobBuilder() {
        if (_analysisJobBuilder == null) {
            // set HDFS as default scheme to avoid file resources
            SystemProperties.setIfNotSpecified(SystemProperties.DEFAULT_RESOURCE_SCHEME, HdfsResource.SCHEME_HDFS);

            final DataCleanerConfiguration configuration = getConfiguration();
            final JaxbJobReader jobReader = new JaxbJobReader(configuration);
            _analysisJobBuilder = jobReader.create(createInputStream(_analysisJobXml), _customProperties);
        }
        applyComponentIndexForKeyLookups(_analysisJobBuilder, new AtomicInteger(0));
        return _analysisJobBuilder;
    }

    /**
     * Appliesc compopnent indices via the component metadata to enable proper
     * functioning of the {@link #getComponentByKey(String)} and
     * {@link #getComponentKey(ComponentJob)} methods.
     *
     * @param analysisJobBuilder
     * @param currentComponentIndex
     */
    private void applyComponentIndexForKeyLookups(AnalysisJobBuilder analysisJobBuilder,
            AtomicInteger currentComponentIndex) {
        final Collection<ComponentBuilder> componentBuilders = analysisJobBuilder.getComponentBuilders();
        for (ComponentBuilder componentBuilder : componentBuilders) {
            componentBuilder.setMetadataProperty(METADATA_PROPERTY_COMPONENT_INDEX, Integer.toString(
                    currentComponentIndex.getAndIncrement()));
        }

        final List<AnalysisJobBuilder> childJobBuilders = analysisJobBuilder.getConsumedOutputDataStreamsJobBuilders();
        for (AnalysisJobBuilder childJobBuilder : childJobBuilders) {
            applyComponentIndexForKeyLookups(childJobBuilder, currentComponentIndex);
        }
    }

    public String getComponentKey(ComponentJob componentJob) {
        final String key = componentJob.getMetadataProperties().get(METADATA_PROPERTY_COMPONENT_INDEX);
        if (key == null) {
            throw new IllegalStateException("No key registered for component: " + componentJob);        }

        final String partitionKey = componentJob.getMetadataProperties().get(AnalyzerComponentBuilder.METADATA_PROPERTY_BUILDER_PARTITION_INDEX);
        if (partitionKey != null) {
            return key + "." + partitionKey;
        } else {
            return key;
        }
    }

    public ComponentJob getComponentByKey(final String key) {
        final AnalysisJob job = getAnalysisJob();
        final ComponentJob result = getComponentByKey(job, key);
        if (result == null) {
            throw new IllegalArgumentException("Cannot resolve component with key: " + key);
        }
        return result;
    }

    private ComponentJob getComponentByKey(final AnalysisJob job, final String queriedKey) {
        final List<ComponentJob> componentJobs = CollectionUtils.<ComponentJob> concat(false, job.getTransformerJobs(),
                job.getTransformerJobs(), job.getAnalyzerJobs());
        for (ComponentJob componentJob : componentJobs) {
            final String componentKey = getComponentKey(componentJob);
            if (queriedKey.equals(componentKey)) {
                return componentJob;
            }

            final OutputDataStreamJob[] outputDataStreamJobs = componentJob.getOutputDataStreamJobs();
            for (OutputDataStreamJob outputDataStreamJob : outputDataStreamJobs) {
                final AnalysisJob childJob = outputDataStreamJob.getJob();
                if (childJob != null) {
                    final ComponentJob result = getComponentByKey(childJob, queriedKey);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Gets the path defined in the job properties file
     *
     * @return
     */
    public URI getResultPath() {
        final String str = _customProperties.get(PROPERTY_RESULT_PATH);
        if (Strings.isNullOrEmpty(str)) {
            return null;
        }
        return URI.create(str);
    }

    public boolean isResultEnabled() {
        final String enabledString = _customProperties.get(PROPERTY_RESULT_ENABLED);
        return !"false".equalsIgnoreCase(enabledString);
    }

    /**
     * Gets the job name (removing the extension '.analysis.xml')
     *
     * @return
     */
    private static String getAnalysisJobName(URI uri) {
        final String filename = uri.getPath();
        final int lastIndexOfSlash = filename.lastIndexOf("/");
        final int lastIndexOfFileExtension = filename.lastIndexOf(".analysis.xml");
        final String jobName = filename.substring(lastIndexOfSlash + 1, lastIndexOfFileExtension);
        return jobName;
    }

    /**
     * Adds a listener for the job life cycle.
     * 
     * @param sparkJobLifeCycleListener
     *            The listener to add. Must be serializable.
     */
    public void addSparkJobLifeCycleListener(SparkJobLifeCycleListener sparkJobLifeCycleListener) {
        _sparkJobLifeCycleListeners.add(sparkJobLifeCycleListener);
    }

    /**
     * Removes a life cycle listener. Please note that this will _not_ work
     * globally after job start. If you remove it on a node, it will only be
     * removed on that node.
     */
    public void removeSparkJobLifeCycleListener(SparkJobLifeCycleListener sparkJobLifeCycleListener) {
        _sparkJobLifeCycleListeners.remove(sparkJobLifeCycleListener);
    }

    public void triggerOnPartitionProcessingEnd() {
        for (SparkJobLifeCycleListener listener : _sparkJobLifeCycleListeners) {
            try {
                listener.onPartitionProcessingEnd(this);
            } catch (Throwable e) {
                logger.warn("onPartitionProcessingEnd: Listener {} threw exception", listener, e);
            }
        }
    }

    public void triggerOnPartitionProcessingStart() {
        for (SparkJobLifeCycleListener listener : _sparkJobLifeCycleListeners) {
            try {
                listener.onPartitionProcessingStart(this);
            } catch (Throwable e) {
                logger.warn("onPartitionProcessingStart: Listener {} threw exception", listener, e);
            }
        }
    }

    public void triggerOnJobStart() {
        for (SparkJobLifeCycleListener listener : _sparkJobLifeCycleListeners) {
            try {
                listener.onJobStart(this);
            } catch (Throwable e) {
                logger.warn("onJobStart: Listener {} threw exception", listener, e);
            }
        }
    }

    public void triggerOnJobEnd() {
        for (SparkJobLifeCycleListener listener : _sparkJobLifeCycleListeners) {
            try {
                listener.onJobEnd(this);
            } catch (Throwable e) {
                logger.warn("onJobEnd: Listener {} threw exception", listener, e);
            }
        }
    }
}
