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

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Func;
import org.apache.metamodel.util.HdfsResource;
import org.apache.metamodel.util.Resource;
import org.apache.spark.Accumulator;
import org.apache.spark.api.java.JavaSparkContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.JaxbConfigurationReader;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.FilterJob;
import org.datacleaner.job.JaxbJobReader;
import org.datacleaner.job.OutputDataStreamJob;
import org.datacleaner.job.TransformerJob;

/**
 * A container for for values that need to be passed between Spark workers. All
 * the values need to be {@link Serializable} or loadable from those
 * {@link Serializable} properties.
 */
public class SparkJobContext implements Serializable {

    public static final String ACCUMULATOR_CONFIGURATION_READS = "DataCleanerConfiguration reads";
    public static final String ACCUMULATOR_JOB_READS = "AnalysisJob reads";

    private static final long serialVersionUID = 1L;

    private final String _configurationPath;
    private final String _analysisJobPath;

    private final Map<String, Accumulator<Integer>> _accumulators;

    // cached/transient state
    private transient DataCleanerConfiguration _dataCleanerConfiguration;
    private transient AnalysisJob _analysisJob;
    private transient List<ComponentJob> _componentList;

    public SparkJobContext(JavaSparkContext sparkContext, final String dataCleanerConfigurationPath,
            final String analysisJobXmlPath) {
        _accumulators = new HashMap<>();
        _accumulators.put(ACCUMULATOR_JOB_READS, sparkContext.accumulator(0));
        _accumulators.put(ACCUMULATOR_CONFIGURATION_READS, sparkContext.accumulator(0));

        _configurationPath = dataCleanerConfigurationPath;
        _analysisJobPath = analysisJobXmlPath;
    }

    public String getConfigurationPath() {
        return _configurationPath;
    }

    public DataCleanerConfiguration getConfiguration() {
        if (_dataCleanerConfiguration == null) {
            _accumulators.get(ACCUMULATOR_CONFIGURATION_READS).add(1);
            final Resource configurationResource = createResource(_configurationPath);
            _dataCleanerConfiguration = configurationResource.read(new Func<InputStream, DataCleanerConfiguration>() {
                @Override
                public DataCleanerConfiguration eval(InputStream in) {
                    final JaxbConfigurationReader confReader = new JaxbConfigurationReader();
                    return confReader.read(in);
                }
            });
        }
        return _dataCleanerConfiguration;
    }

    private static Resource createResource(String path) {
        if (path.toLowerCase().startsWith("hdfs:")) {
            return new HdfsResource(path);
        }
        return new FileResource(path);
    }

    public AnalysisJob getAnalysisJob() {
        if (_analysisJob == null) {
            _accumulators.get(ACCUMULATOR_JOB_READS).add(1);
            final Resource analysisJobResource = createResource(_analysisJobPath);
            final DataCleanerConfiguration configuration = getConfiguration();
            _analysisJob = analysisJobResource.read(new Func<InputStream, AnalysisJob>() {
                @Override
                public AnalysisJob eval(InputStream in) {
                    final JaxbJobReader jobReader = new JaxbJobReader(configuration);
                    final AnalysisJob analysisJob = jobReader.read(in);
                    return analysisJob;
                }
            });
        }
        return _analysisJob;
    }

    public String getAnalysisJobPath() {
        return _analysisJobPath;
    }

    public Map<String, Accumulator<Integer>> getAccumulators() {
        return _accumulators;
    }

    public String getComponentKey(ComponentJob componentJob) {
        List<ComponentJob> componentJobList = getComponentList();
        for (int i = 0; i < componentJobList.size(); i++) {
            if (componentJob.equals(componentJobList.get(i))) {
                return String.valueOf(i);
            }
        }
        return null;
    }
    
    public ComponentJob getComponentByKey(String key) {
        List<ComponentJob> componentJobList = getComponentList();
        for (int i = 0; i < componentJobList.size(); i++) {
            final ComponentJob componentJob = componentJobList.get(i);
            if (key.equals(getComponentKey(componentJob))) {
                return componentJob;
            }
        }
        return null;
    }
    
    public List<ComponentJob> getComponentList() {
        if (_componentList == null) {
            _componentList = buildComponentList(getAnalysisJob());
        }
        return _componentList;
    }
    
    private List<ComponentJob> buildComponentList(AnalysisJob analysisJob) {
        List<ComponentJob> componentJobList = new ArrayList<>();
        List<TransformerJob> transformerJobs = analysisJob.getTransformerJobs();
        List<FilterJob> filterJobs = analysisJob.getFilterJobs();
        List<AnalyzerJob> analyzerJobs = analysisJob.getAnalyzerJobs();

        for (TransformerJob transformerJob : transformerJobs) {
            componentJobList.add(transformerJob);
        }

        for (FilterJob filterJob : filterJobs) {
            componentJobList.add(filterJob);
        }

        for (AnalyzerJob analyzerJob : analyzerJobs) {
            componentJobList.add(analyzerJob);
        }
        
        for (TransformerJob transformerJob : analysisJob.getTransformerJobs()) {
            for (OutputDataStreamJob outputDataStreamJob : transformerJob.getOutputDataStreamJobs()) {
                AnalysisJob outputDataStreamAnalysisJob = outputDataStreamJob.getJob();
                componentJobList.addAll(buildComponentList(outputDataStreamAnalysisJob));
            }
        }
        
        for (FilterJob filterJob : analysisJob.getFilterJobs()) {
            for (OutputDataStreamJob outputDataStreamJob : filterJob.getOutputDataStreamJobs()) {
                AnalysisJob outputDataStreamAnalysisJob = outputDataStreamJob.getJob();
                componentJobList.addAll(buildComponentList(outputDataStreamAnalysisJob));
            }
        }
        
        for (AnalyzerJob analyzerJob : analysisJob.getAnalyzerJobs()) {
            for (OutputDataStreamJob outputDataStreamJob : analyzerJob.getOutputDataStreamJobs()) {
                AnalysisJob outputDataStreamAnalysisJob = outputDataStreamJob.getJob();
                componentJobList.addAll(buildComponentList(outputDataStreamAnalysisJob));
            }
        }
        
        return componentJobList;
    }

}
