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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Func;
import org.apache.metamodel.util.HdfsResource;
import org.apache.metamodel.util.Resource;
import org.apache.spark.api.java.JavaSparkContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DefaultConfigurationReaderInterceptor;
import org.datacleaner.configuration.JaxbConfigurationReader;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.JaxbJobReader;
import org.datacleaner.job.OutputDataStreamJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.util.InputStreamToPropertiesMapFunc;

import com.google.common.base.Strings;

/**
 * A container for for values that need to be passed between Spark workers. All
 * the values need to be {@link Serializable} or loadable from those
 * {@link Serializable} properties.
 */
public class SparkJobContext implements Serializable {

    private static String DATA_CLEANER_RESULT_PATH_PROPERTY = "datacleaner.result.hdfs.path";
    public static final String ACCUMULATOR_CONFIGURATION_READS = "DataCleanerConfiguration reads";
    public static final String ACCUMULATOR_JOB_READS = "AnalysisJob reads";

    private static final String METADATA_PROPERTY_COMPONENT_INDEX = "org.datacleaner.spark.component.index";

    private static final long serialVersionUID = 1L;

    private final String _configurationXml;
    private final String _analysisJobXml;
    private final String _analysisJobXmlPath;
    private final Map<String, String> _customProperties;

    // cached/transient state
    private transient DataCleanerConfiguration _dataCleanerConfiguration;
    private transient AnalysisJobBuilder _analysisJobBuilder;

    public SparkJobContext(JavaSparkContext sparkContext, final String dataCleanerConfigurationPath,
            final String analysisJobXmlPath) {
        this(sparkContext, dataCleanerConfigurationPath, analysisJobXmlPath, null);
    }

    public SparkJobContext(JavaSparkContext sparkContext, final String dataCleanerConfigurationPath,
            final String analysisJobXmlPath, final String propertiesPath) {
        _customProperties = readCustomProperties(propertiesPath);
        _configurationXml = readFile(dataCleanerConfigurationPath);
        _analysisJobXml = readFile(analysisJobXmlPath);
        _analysisJobXmlPath= analysisJobXmlPath;
    }

    private String readFile(String path) {
        final Resource resource = createResource(path);
        return resource.read(new Func<InputStream, String>() {
            @Override
            public String eval(InputStream in) {
                return FileHelper.readInputStreamAsString(in, "UTF-8");
            }
        });
    }

    public DataCleanerConfiguration getConfiguration() {
        if (_dataCleanerConfiguration == null) {
            final JaxbConfigurationReader confReader = new JaxbConfigurationReader(
                    new DefaultConfigurationReaderInterceptor(_customProperties));
            _dataCleanerConfiguration = confReader.read(createInputStream(_configurationXml));
        }
        return _dataCleanerConfiguration;
    }

    private InputStream createInputStream(String fileContents) {
        try {
            final byte[] bytes = fileContents.getBytes("UTF-8");
            return new ByteArrayInputStream(bytes);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private Map<String, String> readCustomProperties(String propertiesPath) {
        final Resource propertiesResource = createResource(propertiesPath);
        if (propertiesResource != null && propertiesResource.isExists()) {
            return propertiesResource.read(new InputStreamToPropertiesMapFunc());
        }
        return Collections.emptyMap();
    }

    private static Resource createResource(String path) {
        if (Strings.isNullOrEmpty(path)) {
            return null;
        }
        if (path.toLowerCase().startsWith("hdfs:")) {
            return new HdfsResource(path);
        }
        return new FileResource(path);
    }

    public AnalysisJob getAnalysisJob() {
        return getAnalysisJobBuilder().toAnalysisJob();
    }

    public AnalysisJobBuilder getAnalysisJobBuilder() {
        if (_analysisJobBuilder == null) {
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
            componentBuilder.setMetadataProperty(METADATA_PROPERTY_COMPONENT_INDEX,
                    Integer.toString(currentComponentIndex.getAndIncrement()));
        }

        final List<AnalysisJobBuilder> childJobBuilders = analysisJobBuilder.getConsumedOutputDataStreamsJobBuilders();
        for (AnalysisJobBuilder childJobBuilder : childJobBuilders) {
            applyComponentIndexForKeyLookups(childJobBuilder, currentComponentIndex);
        }
    }

    public String getComponentKey(ComponentJob componentJob) {
        final String key = componentJob.getMetadataProperties().get(METADATA_PROPERTY_COMPONENT_INDEX);
        if (key == null) {
            throw new IllegalArgumentException("Cannot find component in job: " + componentJob);
        }
        return key;
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
            final String componentKey = componentJob.getMetadataProperties().get(METADATA_PROPERTY_COMPONENT_INDEX);
            if (componentKey == null) {
                throw new IllegalStateException("No key registered for component: " + componentJob);
            }
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

    public String getResultPath() {
        if (_customProperties != null) {
            if (_customProperties.containsKey(DATA_CLEANER_RESULT_PATH_PROPERTY)) {
                return _customProperties.get(DATA_CLEANER_RESULT_PATH_PROPERTY);
            }
        }
        return null;
    }
    
    public String getAnalysisJobName() {
        final int lastIndexOfSlash = _analysisJobXmlPath.lastIndexOf("/");
        final int lastIndexOfFileExtension = _analysisJobXmlPath.lastIndexOf(".analysis.xml");
        final String jobName = _analysisJobXmlPath.substring(lastIndexOfSlash+1, lastIndexOfFileExtension);
        return  jobName;
    }
}
