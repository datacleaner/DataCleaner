/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.monitor.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.MetricDescriptor;
import org.eobjects.analyzer.descriptors.MetricParameters;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.util.ChangeAwareObjectInputStream;
import org.eobjects.analyzer.util.CollectionUtils2;
import org.eobjects.analyzer.util.LabelUtils;
import org.eobjects.datacleaner.monitor.configuration.ConfigurationCache;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.MetricGroup;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.timeline.TimelineService;
import org.eobjects.datacleaner.monitor.timeline.model.ChartOptions.HorizontalAxisOption;
import org.eobjects.datacleaner.monitor.timeline.model.JobMetrics;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineData;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineDataRow;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineGroup;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineIdentifier;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFile.Type;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.repository.RepositoryNode;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.FileHelper;
import org.eobjects.metamodel.util.HasNameMapper;
import org.eobjects.metamodel.util.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main implementation of the {@link TimelineService} interface.
 */
public class TimelineServiceImpl implements TimelineService {

    public static final String PATH_JOBS = "jobs";
    public static final String PATH_RESULTS = "results";
    public static final String PATH_TIMELINES = "timelines";

    private static final Logger logger = LoggerFactory.getLogger(TimelineServiceImpl.class);

    private final ConfigurationCache _configurationCache;
    private final Repository _repository;

    protected TimelineServiceImpl(final Repository repository, final ConfigurationCache configurationCache) {
        _repository = repository;
        _configurationCache = configurationCache;
    }

    @Override
    public List<TimelineGroup> getTimelineGroups(final TenantIdentifier tenant) {
        final RepositoryFolder tenantFolder = _repository.getFolder(tenant.getId());
        final RepositoryFolder timelinesFolder = tenantFolder.getFolder(PATH_TIMELINES);
        final List<RepositoryFolder> folders = timelinesFolder.getFolders();
        final List<TimelineGroup> groups = new ArrayList<TimelineGroup>();
        for (RepositoryFolder folder : folders) {
            final TimelineGroup group = new TimelineGroup(folder.getName());

            final RepositoryFile descriptionFile = folder.getFile("description.txt");
            if (descriptionFile == null) {
                logger.debug("No description file for timeline group: {}", group);
            } else {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(descriptionFile.readFile()));
                final StringBuilder sb = new StringBuilder();
                try {
                    boolean first = false;
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append('\n');
                        }
                        sb.append(line);
                    }
                    group.setDescription(sb.toString());
                } catch (Exception e) {
                    logger.error("Error while reading timeline group description file: " + descriptionFile, e);
                } finally {
                    FileHelper.safeClose(reader);
                }
            }

            groups.add(group);
        }
        return groups;
    }

    @Override
    public List<TimelineIdentifier> getTimelines(final TenantIdentifier tenant, final TimelineGroup group) {
        final RepositoryFolder tenantFolder = _repository.getFolder(tenant.getId());
        final RepositoryFolder timelinesFolder = tenantFolder.getFolder(PATH_TIMELINES);
        final List<RepositoryFile> files;
        final String groupName = (group == null ? null : group.getName());
        if (group == null || groupName == null || "".equals(groupName)) {
            files = timelinesFolder.getFiles();
        } else {
            RepositoryFolder groupFolder = timelinesFolder.getFolder(groupName);
            files = groupFolder.getFiles();
        }

        final List<TimelineIdentifier> result = new ArrayList<TimelineIdentifier>();
        for (RepositoryFile file : files) {
            if (file.getType() == Type.TIMELINE_SPEC) {
                String timelineName = file.getName().substring(0,
                        file.getName().length() - FileFilters.ANALYSIS_TIMELINE_XML.getExtension().length());
                TimelineIdentifier timeline = new TimelineIdentifier(timelineName, file.getQualifiedPath(), group);
                result.add(timeline);
            }
        }

        return result;
    }

    @Override
    public TimelineData getTimelineData(TenantIdentifier tenant, TimelineDefinition timeline) {
        final AnalysisJob analysisJob = readAnalysisJob(tenant, timeline.getJobIdentifier());

        final List<MetricIdentifier> metricIdentifiers = timeline.getMetrics();
        final int metricCount = metricIdentifiers.size();

        final List<MetricDescriptor> metricDescriptors = new ArrayList<MetricDescriptor>(metricCount);
        final List<MetricParameters> metricParameters = new ArrayList<MetricParameters>(metricCount);
        final List<AnalyzerJob> analyzerJobs = new ArrayList<AnalyzerJob>(metricCount);

        for (MetricIdentifier metricIdentifier : metricIdentifiers) {
            final AnalyzerJob analyzerJob = getAnalyzerJob(metricIdentifier, analysisJob);
            analyzerJobs.add(analyzerJob);

            final MetricDescriptor metricDescriptor = getMetricDescriptor(metricIdentifier, analyzerJob);
            metricDescriptors.add(metricDescriptor);

            MetricParameters parameter = createMetricParameter(metricIdentifier, metricDescriptor, analyzerJob);
            metricParameters.add(parameter);
        }

        final List<RepositoryFile> resultFiles = getResultFilesForJob(tenant, timeline.getJobIdentifier());
        final List<TimelineDataRow> rows = new ArrayList<TimelineDataRow>();

        final HorizontalAxisOption horizontalAxisOption = timeline.getChartOptions().getHorizontalAxisOption();

        for (RepositoryFile resultFile : resultFiles) {

            final AnalysisResult analysisResult = readAnalysisResult(resultFile);

            final Date date = analysisResult.getCreationDate();

            if (isInRange(date, horizontalAxisOption)) {
                final TimelineDataRow row = new TimelineDataRow(date, resultFile.getQualifiedPath());

                final List<Number> metricValues = new ArrayList<Number>(metricCount);
                for (int i = 0; i < metricCount; i++) {
                    final MetricIdentifier metricIdentifier = metricIdentifiers.get(i);
                    final AnalyzerJob job = analyzerJobs.get(i);
                    final MetricDescriptor metric = metricDescriptors.get(i);
                    final MetricParameters parameters = metricParameters.get(i);

                    final AnalyzerResult analyzerResult = getResult(analysisResult, job, metricIdentifier);

                    final Number metricValue = metric.getValue(analyzerResult, parameters);
                    metricValues.add(metricValue);
                }
                row.setMetricValues(metricValues);

                rows.add(row);
            }
        }

        // sort rows to ensure correct date order
        Collections.sort(rows);

        final TimelineData timelineData = new TimelineData();
        timelineData.setRows(rows);

        return timelineData;
    }

    private boolean isInRange(Date date, HorizontalAxisOption horizontalAxisOption) {
        final Date beginDate = horizontalAxisOption.getBeginDate();
        final Date endDate = horizontalAxisOption.getEndDate();

        if (beginDate != null && date.before(beginDate)) {
            return false;
        }
        if (endDate != null && date.after(endDate)) {
            return false;
        }

        return true;
    }

    private AnalyzerResult getResult(final AnalysisResult analysisResult, final AnalyzerJob analyzerJob,
            final MetricIdentifier metricIdentifier) {
        AnalyzerResult result = analysisResult.getResult(analyzerJob);
        if (result == null) {
            logger.info("Could not resolve AnalyzerResult using key={}, reiterating using non-exact matching",
                    analyzerJob);

            Collection<ComponentJob> componentJobs = analysisResult.getResultMap().keySet();

            List<AnalyzerJob> candidates = CollectionUtils2.filterOnClass(componentJobs, AnalyzerJob.class);

            // filter analyzers of the corresponding type
            candidates = refineCandidates(candidates, new Predicate<AnalyzerJob>() {
                @Override
                public Boolean eval(AnalyzerJob o) {
                    final String actualDescriptorName = o.getDescriptor().getDisplayName();
                    final String metricDescriptorName = analyzerJob.getDescriptor().getDisplayName();
                    return metricDescriptorName.equals(actualDescriptorName);
                }
            });

            final String analyzerJobName = analyzerJob.getName();
            if (analyzerJobName != null) {
                // filter analyzers with a particular name
                candidates = refineCandidates(candidates, new Predicate<AnalyzerJob>() {
                    @Override
                    public Boolean eval(AnalyzerJob o) {
                        final String actualAnalyzerName = o.getName();
                        final String metricAnalyzerName = analyzerJobName;
                        return metricAnalyzerName.equals(actualAnalyzerName);
                    }
                });
            }

            // filter analyzer jobs with same input
            candidates = refineCandidates(candidates, new Predicate<AnalyzerJob>() {
                @Override
                public Boolean eval(AnalyzerJob o) {
                    final String actualAnalyzerInputNames = CollectionUtils.map(o.getInput(), new HasNameMapper())
                            .toString();
                    final String metricAnalyzerInputNames = CollectionUtils.map(analyzerJob.getInput(),
                            new HasNameMapper()).toString();
                    return metricAnalyzerInputNames.equals(actualAnalyzerInputNames);
                }
            });

            // filter analyzer jobs with input matching the metric
            final String analyzerInputName = metricIdentifier.getAnalyzerInputName();
            if (analyzerInputName != null) {
                candidates = refineCandidates(candidates, new Predicate<AnalyzerJob>() {
                    @Override
                    public Boolean eval(AnalyzerJob o) {
                        InputColumn<?> identifyingInputColumn = getIdentifyingInputColumn(o);
                        if (identifyingInputColumn == null) {
                            return false;
                        }
                        return analyzerInputName.equals(identifyingInputColumn.getName());
                    }
                });
            }

            if (candidates.isEmpty()) {
                throw new IllegalArgumentException("No matching AnalyzerJobs found");
            } else if (candidates.size() > 1) {
                logger.warn("Multiple matching AnalyzerJobs found, selecting the first: {}", candidates);
            }
            AnalyzerJob candidate = candidates.iterator().next();
            result = analysisResult.getResult(candidate);
        }
        return result;
    }

    private <E> List<E> refineCandidates(final List<E> candidates, final Predicate<? super E> predicate) {
        if (candidates.size() == 1) {
            return candidates;
        }
        List<E> newCandidates = CollectionUtils.filter(candidates, predicate);
        if (newCandidates.isEmpty()) {
            return candidates;
        }
        return newCandidates;
    }

    private MetricParameters createMetricParameter(final MetricIdentifier metricIdentifier,
            final MetricDescriptor metricDescriptor, AnalyzerJob analyzerJob) {
        final String queryString;
        final InputColumn<?> queryInputColumn;

        final String paramQueryString = metricIdentifier.getParamQueryString();
        if (paramQueryString == null) {
            queryString = null;
        } else {
            queryString = paramQueryString;
        }

        final String paramColumnName = metricIdentifier.getParamColumnName();
        if (paramColumnName == null) {
            queryInputColumn = null;
        } else {
            InputColumn<?>[] inputColumns = analyzerJob.getInput();
            InputColumn<?> candidate = null;
            for (InputColumn<?> inputColumn : inputColumns) {
                if (paramColumnName.equals(inputColumn.getName())) {
                    candidate = inputColumn;
                    break;
                }
            }
            if (candidate == null) {
                logger.warn("Could not find any input column with name '{}'", paramColumnName);
            }
            queryInputColumn = candidate;
        }

        return new MetricParameters(queryString, queryInputColumn);
    }

    public static AnalysisResult readAnalysisResult(final RepositoryFile resultFile) {
        try {
            ChangeAwareObjectInputStream inputStream = new ChangeAwareObjectInputStream(resultFile.readFile());
            AnalysisResult result = (AnalysisResult) inputStream.readObject();
            return result;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private List<RepositoryFile> getResultFilesForJob(final TenantIdentifier tenant, final JobIdentifier jobIdentifier) {
        final RepositoryFolder tenantFolder = _repository.getFolder(tenant.getId());
        final RepositoryFolder resultsFolder = tenantFolder.getFolder(PATH_RESULTS);

        final String jobName = jobIdentifier.getName();

        List<RepositoryFile> files = resultsFolder.getFiles(FileFilters.ANALYSIS_RESULT_SER.getExtension());
        files = CollectionUtils.filter(files, new Predicate<RepositoryFile>() {
            @Override
            public Boolean eval(RepositoryFile file) {
                return file.getName().startsWith(jobName);
            }
        });

        return files;
    }

    private AnalyzerJob getAnalyzerJob(final MetricIdentifier metricIdentifier, final AnalysisJob analysisJob) {
        List<AnalyzerJob> candidates = new ArrayList<AnalyzerJob>(analysisJob.getAnalyzerJobs());

        // filter analyzers of the corresponding type
        candidates = refineCandidates(candidates, new Predicate<AnalyzerJob>() {
            @Override
            public Boolean eval(AnalyzerJob o) {
                final String actualDescriptorName = o.getDescriptor().getDisplayName();
                final String metricDescriptorName = metricIdentifier.getAnalyzerDescriptorName();
                return metricDescriptorName.equals(actualDescriptorName);
            }
        });

        if (metricIdentifier.getAnalyzerName() != null) {
            // filter analyzers with a particular name
            candidates = refineCandidates(candidates, new Predicate<AnalyzerJob>() {
                @Override
                public Boolean eval(AnalyzerJob o) {
                    final String actualAnalyzerName = o.getName();
                    final String metricAnalyzerName = metricIdentifier.getAnalyzerName();
                    return metricAnalyzerName.equals(actualAnalyzerName);
                }
            });
        }

        if (metricIdentifier.getAnalyzerInputName() != null) {
            // filter analyzers with a particular input
            candidates = refineCandidates(candidates, new Predicate<AnalyzerJob>() {
                @Override
                public Boolean eval(AnalyzerJob o) {
                    final InputColumn<?> inputColumn = getIdentifyingInputColumn(o);
                    if (inputColumn == null) {
                        return false;
                    }

                    final String metricInputName = metricIdentifier.getAnalyzerInputName();
                    return metricInputName.equals(inputColumn.getName());
                }
            });
        }

        if (candidates.isEmpty()) {
            logger.error("No more AnalyzerJob candidates to choose from");
            return null;
        } else if (candidates.size() > 1) {
            logger.warn("Multiple ({}) AnalyzerJob candidates to choose from, picking first");
        }

        AnalyzerJob analyzerJob = candidates.iterator().next();
        return analyzerJob;
    }

    private InputColumn<?> getIdentifyingInputColumn(final AnalyzerJob o) {
        final Set<ConfiguredPropertyDescriptor> inputProperties = o.getDescriptor().getConfiguredPropertiesForInput(
                false);
        if (inputProperties.size() != 1) {
            return null;
        }

        final ConfiguredPropertyDescriptor inputProperty = inputProperties.iterator().next();
        final Object input = o.getConfiguration().getProperty(inputProperty);

        if (input instanceof InputColumn) {
            final InputColumn<?> inputColumn = (InputColumn<?>) input;
            return inputColumn;
        } else if (input instanceof InputColumn[]) {
            final InputColumn<?>[] inputColumns = (InputColumn[]) input;
            if (inputColumns.length != 1) {
                return null;
            }
            return inputColumns[0];
        }
        return null;
    }

    private MetricDescriptor getMetricDescriptor(final MetricIdentifier metricIdentifier, final AnalyzerJob analyzerJob) {
        AnalyzerBeanDescriptor<?> analyzerDescriptor = analyzerJob.getDescriptor();
        MetricDescriptor metric = analyzerDescriptor.getResultMetric(metricIdentifier.getMetricDescriptorName());

        if (metric == null) {
            logger.error("Did not find any metric descriptors with name '{}' in {}",
                    metricIdentifier.getMetricDescriptorName(), analyzerDescriptor.getResultClass());
        }
        return metric;
    }

    @Override
    public TimelineDefinition getTimelineDefinition(final TenantIdentifier tenant, final TimelineIdentifier timeline) {
        final String path = timeline.getPath();

        logger.info("Reading timeline from file: {}", path);

        final RepositoryFile timelineNode = (RepositoryFile) _repository.getRepositoryNode(path);
        final TimelineReader reader = new JaxbTimelineReader();

        final TimelineDefinition timelineDefinition = reader.read(timelineNode.readFile());
        return timelineDefinition;
    }

    @Override
    public List<JobIdentifier> getJobs(final TenantIdentifier tenant) {
        final List<JobIdentifier> result = new ArrayList<JobIdentifier>();

        final RepositoryFolder tenantFolder = _repository.getFolder(tenant.getId());
        final RepositoryFolder jobsFolder = tenantFolder.getFolder(PATH_JOBS);
        final List<RepositoryFile> files = jobsFolder.getFiles();

        for (RepositoryFile file : files) {
            if (file.getType() == Type.ANALYSIS_JOB) {
                final JobIdentifier job = createJobIdentifier(file);
                result.add(job);
            }
        }

        return result;
    }

    private JobIdentifier createJobIdentifier(final RepositoryFile file) {
        final JobIdentifier job = new JobIdentifier();
        job.setName(file.getName().substring(0,
                file.getName().length() - FileFilters.ANALYSIS_XML.getExtension().length()));
        return job;
    }

    @Override
    public JobMetrics getJobMetrics(final TenantIdentifier tenant, final JobIdentifier jobIdentifier) {
        final AnalysisJob job = readAnalysisJob(tenant, jobIdentifier);

        final Collection<AnalyzerJob> analyzerJobs = job.getAnalyzerJobs();

        final List<MetricGroup> metricGroups = new ArrayList<MetricGroup>();
        for (AnalyzerJob analyzerJob : analyzerJobs) {
            final Set<MetricDescriptor> metricDescriptors = analyzerJob.getDescriptor().getResultMetrics();
            if (!metricDescriptors.isEmpty()) {
                final String label = LabelUtils.getLabel(analyzerJob);
                final InputColumn<?> identifyingInputColumn = getIdentifyingInputColumn(analyzerJob);
                final List<MetricIdentifier> metricIdentifiers = new ArrayList<MetricIdentifier>();

                for (MetricDescriptor metricDescriptor : metricDescriptors) {
                    MetricIdentifier metricIdentifier = new MetricIdentifier();
                    metricIdentifier.setAnalyzerDescriptorName(analyzerJob.getDescriptor().getDisplayName());
                    metricIdentifier.setAnalyzerName(analyzerJob.getName());
                    if (identifyingInputColumn != null) {
                        metricIdentifier.setAnalyzerInputName(identifyingInputColumn.getName());
                    }
                    metricIdentifier.setMetricDescriptorName(metricDescriptor.getName());
                    metricIdentifier.setParameterizedByColumnName(metricDescriptor.isParameterizedByInputColumn());
                    metricIdentifier.setParameterizedByQueryString(metricDescriptor.isParameterizedByString());

                    metricIdentifiers.add(metricIdentifier);
                }

                final List<String> columnNames = new ArrayList<String>();
                final Set<ConfiguredPropertyDescriptor> inputProperties = analyzerJob.getDescriptor()
                        .getConfiguredPropertiesForInput(false);
                for (ConfiguredPropertyDescriptor inputProperty : inputProperties) {
                    final Object input = analyzerJob.getConfiguration().getProperty(inputProperty);
                    if (input instanceof InputColumn) {
                        String columnName = ((InputColumn<?>) input).getName();
                        columnNames.add(columnName);
                    } else if (input instanceof InputColumn[]) {
                        InputColumn<?>[] inputColumns = (InputColumn<?>[]) input;
                        for (InputColumn<?> inputColumn : inputColumns) {
                            String columnName = inputColumn.getName();
                            if (!columnNames.contains(columnName)) {
                                columnNames.add(columnName);
                            }
                        }
                    }
                }

                final MetricGroup metricGroup = new MetricGroup();
                metricGroup.setName(label);
                metricGroup.setMetrics(metricIdentifiers);
                metricGroup.setColumnNames(columnNames);
                metricGroups.add(metricGroup);
            }
        }

        final JobMetrics metrics = new JobMetrics();
        metrics.setMetricGroups(metricGroups);
        metrics.setJob(jobIdentifier);
        return metrics;
    }

    private AnalysisJob readAnalysisJob(final TenantIdentifier tenant, final JobIdentifier jobIdentifier) {
        final RepositoryFolder tenantFolder = _repository.getFolder(tenant.getId());
        final RepositoryFolder jobsFolder = tenantFolder.getFolder(PATH_JOBS);

        final RepositoryFile jobFile = jobsFolder.getFile(jobIdentifier.getName()
                + FileFilters.ANALYSIS_XML.getExtension());

        final AnalyzerBeansConfiguration configuration = _configurationCache.getAnalyzerBeansConfiguration(tenant);

        final MonitorJobReader reader = new MonitorJobReader(configuration, jobFile);

        final AnalysisJob job = reader.readJob();
        return job;
    }

    @Override
    public TimelineIdentifier updateTimelineDefinition(final TenantIdentifier tenant,
            final TimelineIdentifier timelineIdentifier, final TimelineDefinition timelineDefinition) {
        final RepositoryFile file = (RepositoryFile) _repository.getRepositoryNode(timelineIdentifier.getPath());

        file.writeFile(new WriteTimelineAction(timelineDefinition));
        logger.info("Updated timeline definition in file: {}", file);
        return new TimelineIdentifier(timelineIdentifier.getName(), file.getQualifiedPath(),
                timelineIdentifier.getGroup());
    }

    @Override
    public TimelineIdentifier createTimelineDefinition(final TenantIdentifier tenant,
            final TimelineIdentifier timelineIdentifier, final TimelineDefinition timelineDefinition) {
        final String name = timelineIdentifier.getName();
        final TimelineGroup group = timelineIdentifier.getGroup();

        final RepositoryFolder folder;
        if (group == null) {
            folder = _repository.getFolder(tenant.getId()).getFolder(PATH_TIMELINES);
        } else {
            folder = _repository.getFolder(tenant.getId()).getFolder(PATH_TIMELINES).getFolder(group.getName());
        }
        final String fileName = name + FileFilters.ANALYSIS_TIMELINE_XML.getExtension();

        final RepositoryFile file = folder.createFile(fileName, new WriteTimelineAction(timelineDefinition));
        logger.info("Created timeline definition in file: {}", file);

        return new TimelineIdentifier(timelineIdentifier.getName(), file.getQualifiedPath(), group);
    }

    @Override
    public Collection<String> getMetricParameterSuggestions(TenantIdentifier tenant, JobIdentifier job,
            MetricIdentifier metric) {
        final AnalyzerBeansConfiguration configuration = _configurationCache.getAnalyzerBeansConfiguration(tenant);
        final AnalyzerBeanDescriptor<?> analyzerDescriptor = configuration.getDescriptorProvider()
                .getAnalyzerBeanDescriptorByDisplayName(metric.getAnalyzerDescriptorName());
        final MetricDescriptor metricDescriptor = analyzerDescriptor.getResultMetric(metric.getMetricDescriptorName());

        if (!metricDescriptor.isParameterizedByString()) {
            return null;
        }

        final List<RepositoryFile> resultFiles = getResultFilesForJob(tenant, job);
        final RepositoryFile resultFile = resultFiles.get(resultFiles.size() - 1);
        final AnalysisResult analysisResult = readAnalysisResult(resultFile);

        final AnalysisJob analysisJob = readAnalysisJob(tenant, job);
        final AnalyzerJob analyzerJob = getAnalyzerJob(metric, analysisJob);

        final AnalyzerResult result = getResult(analysisResult, analyzerJob, metric);

        final Collection<String> suggestions = metricDescriptor.getMetricParameterSuggestions(result);

        // make sure we can send it across the GWT-RPC wire.
        if (suggestions instanceof ArrayList) {
            return suggestions;
        }
        return new ArrayList<String>(suggestions);
    }

    @Override
    public Boolean deleteTimeline(TenantIdentifier tenant, TimelineIdentifier timeline) {
        if (timeline == null) {
            return false;
        }

        final String path = timeline.getPath();
        final RepositoryNode node = _repository.getRepositoryNode(path);

        if (node == null) {
            return false;
        }

        try {
            node.delete();
        } catch (IllegalStateException e) {
            logger.warn("Attempt to delete node failed: " + node, e);
            return false;
        }

        return true;
    }
}
