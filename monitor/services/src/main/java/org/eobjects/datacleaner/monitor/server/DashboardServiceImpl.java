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
import java.io.InputStream;
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
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.util.LabelUtils;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.dashboard.DashboardService;
import org.eobjects.datacleaner.monitor.dashboard.model.ChartOptions.HorizontalAxisOption;
import org.eobjects.datacleaner.monitor.dashboard.model.DashboardGroup;
import org.eobjects.datacleaner.monitor.dashboard.model.JobMetrics;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineData;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineDataRow;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineIdentifier;
import org.eobjects.datacleaner.monitor.server.dao.ResultDao;
import org.eobjects.datacleaner.monitor.server.dao.TimelineDao;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.MetricGroup;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFile.Type;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Main implementation of the {@link DashboardService} interface.
 */
@Component
public class DashboardServiceImpl implements DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final TenantContextFactory _tenantContextFactory;
    private final MetricValueProducer _metricValueProducer;
    private final ResultDao _resultDao;
    private final TimelineDao _timelineDao;

    @Autowired
    public DashboardServiceImpl(final TenantContextFactory tenantContextFactory,
            final MetricValueProducer metricValueProducer, ResultDao resultDao, TimelineDao timelineDao) {
        _tenantContextFactory = tenantContextFactory;
        _metricValueProducer = metricValueProducer;
        _resultDao = resultDao;
        _timelineDao = timelineDao;
    }

    @Override
    public List<DashboardGroup> getDashboardGroups(final TenantIdentifier tenant) {
        final RepositoryFolder timelinesFolder = _tenantContextFactory.getContext(tenant).getTimelineFolder();
        final List<RepositoryFolder> folders = timelinesFolder.getFolders();
        final List<DashboardGroup> groups = new ArrayList<DashboardGroup>();
        for (RepositoryFolder folder : folders) {
            final DashboardGroup group = new DashboardGroup(folder.getName());

            final RepositoryFile descriptionFile = folder.getFile("description.txt");
            if (descriptionFile == null) {
                logger.debug("No description file for timeline group: {}", group);
            } else {
                descriptionFile.readFile(new Action<InputStream>() {
                    @Override
                    public void run(InputStream in) throws Exception {
                        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
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
                        }

                        reader.close();
                    }
                });
            }

            groups.add(group);
        }
        return groups;
    }

    @Override
    public List<TimelineIdentifier> getTimelines(final TenantIdentifier tenant, final DashboardGroup group) {
        final RepositoryFolder timelinesFolder = _tenantContextFactory.getContext(tenant).getTimelineFolder();
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
        final List<MetricIdentifier> metricIdentifiers = timeline.getMetrics();

        JobIdentifier jobIdentifier = timeline.getJobIdentifier();
        final List<RepositoryFile> resultFiles = _resultDao.getResultsForJob(tenant, jobIdentifier);
        final List<TimelineDataRow> rows = new ArrayList<TimelineDataRow>();

        final HorizontalAxisOption horizontalAxisOption = timeline.getChartOptions().getHorizontalAxisOption();

        for (RepositoryFile resultFile : resultFiles) {
            MetricValues metricValues = _metricValueProducer.getMetricValues(metricIdentifiers, resultFile, tenant,
                    jobIdentifier);
            Date date = metricValues.getMetricDate();
            if (isInRange(date, horizontalAxisOption)) {
                final TimelineDataRow row = new TimelineDataRow(date, resultFile.getQualifiedPath());
                final List<Number> metricValuesList = metricValues.getValues();
                row.setMetricValues(metricValuesList);
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

    @Override
    public TimelineDefinition getTimelineDefinition(final TenantIdentifier tenant, final TimelineIdentifier timeline) {
        return _timelineDao.getTimelineDefinition(timeline);
    }

    @Override
    public List<JobIdentifier> getJobs(final TenantIdentifier tenant) {
        final List<String> jobNames = _tenantContextFactory.getContext(tenant).getJobNames();
        final List<JobIdentifier> result = new ArrayList<JobIdentifier>(jobNames.size());

        for (String jobName : jobNames) {
            final JobIdentifier job = new JobIdentifier(jobName);
            result.add(job);
        }

        Collections.sort(result);

        return result;
    }

    @Override
    public JobMetrics getJobMetrics(final TenantIdentifier tenant, final JobIdentifier jobIdentifier) {
        final AnalysisJob job = _tenantContextFactory.getContext(tenant).getJob(jobIdentifier.getName())
                .getAnalysisJob();
        MetricValueUtils metricValueUtils = new MetricValueUtils();

        final Collection<AnalyzerJob> analyzerJobs = job.getAnalyzerJobs();

        final List<MetricGroup> metricGroups = new ArrayList<MetricGroup>();
        for (AnalyzerJob analyzerJob : analyzerJobs) {
            final Set<MetricDescriptor> metricDescriptors = analyzerJob.getDescriptor().getResultMetrics();
            if (!metricDescriptors.isEmpty()) {
                final String label = LabelUtils.getLabel(analyzerJob);
                final InputColumn<?> identifyingInputColumn = metricValueUtils.getIdentifyingInputColumn(analyzerJob);
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

    @Override
    public TimelineIdentifier updateTimelineDefinition(final TenantIdentifier tenant,
            final TimelineIdentifier timelineIdentifier, final TimelineDefinition timelineDefinition) {
        return _timelineDao.updateTimeline(timelineIdentifier, timelineDefinition);
    }

    @Override
    public TimelineIdentifier createTimelineDefinition(final TenantIdentifier tenant,
            final TimelineIdentifier timelineIdentifier, final TimelineDefinition timelineDefinition) {
        final String name = timelineIdentifier.getName();
        final DashboardGroup group = timelineIdentifier.getGroup();

        final RepositoryFolder timelinesFolder = _tenantContextFactory.getContext(tenant).getTimelineFolder();

        final RepositoryFolder folder;
        if (group == null) {
            folder = timelinesFolder;
        } else {
            folder = timelinesFolder.getFolder(group.getName());
        }
        final String fileName = name + FileFilters.ANALYSIS_TIMELINE_XML.getExtension();

        final RepositoryFile file = folder.createFile(fileName, new WriteTimelineAction(timelineDefinition));
        logger.info("Created timeline definition in file: {}", file);

        return new TimelineIdentifier(timelineIdentifier.getName(), file.getQualifiedPath(), group);
    }

    @Override
    public Collection<String> getMetricParameterSuggestions(TenantIdentifier tenant, JobIdentifier job,
            MetricIdentifier metric) {
        if (metric.isFormulaBased()) {
            return new ArrayList<String>(0);
        }

        final TenantContext context = _tenantContextFactory.getContext(tenant);
        final MetricValueUtils metricValueUtils = new MetricValueUtils();

        final AnalyzerBeansConfiguration configuration = context.getConfiguration();
        final AnalyzerBeanDescriptor<?> analyzerDescriptor = configuration.getDescriptorProvider()
                .getAnalyzerBeanDescriptorByDisplayName(metric.getAnalyzerDescriptorName());
        final MetricDescriptor metricDescriptor = analyzerDescriptor.getResultMetric(metric.getMetricDescriptorName());

        if (!metricDescriptor.isParameterizedByString()) {
            return null;
        }

        final RepositoryFolder resultsFolder = context.getResultFolder();
        final String jobName = job.getName();

        final RepositoryFile resultFile = resultsFolder.getLatestFile(jobName,
                FileFilters.ANALYSIS_RESULT_SER.getExtension());
        if (resultFile == null) {
            return new ArrayList<String>(0);
        }

        final AnalysisResult analysisResult = context.getResult(resultFile.getName()).getAnalysisResult();

        final AnalysisJob analysisJob = context.getJob(job.getName()).getAnalysisJob();
        final AnalyzerJob analyzerJob = metricValueUtils.getAnalyzerJob(metric, analysisJob);

        final AnalyzerResult result = metricValueUtils.getResult(analysisResult, analyzerJob, metric);

        final Collection<String> suggestions = metricDescriptor.getMetricParameterSuggestions(result);

        // make sure we can send it across the GWT-RPC wire.
        if (suggestions instanceof ArrayList) {
            return suggestions;
        }
        return new ArrayList<String>(suggestions);
    }

    @Override
    public Boolean removeTimeline(TenantIdentifier tenant, TimelineIdentifier timeline) {
        return _timelineDao.removeTimeline(timeline);
    }

    @Override
    public DashboardGroup addDashboardGroup(TenantIdentifier tenant, String name) {
        final DashboardGroup group = new DashboardGroup(name);

        final RepositoryFolder timelineFolder = _tenantContextFactory.getContext(tenant).getTimelineFolder();
        final RepositoryFolder groupFolder = timelineFolder.createFolder(name);

        assert groupFolder != null;

        return group;
    }

    @Override
    public Boolean removeDashboardGroup(TenantIdentifier tenant, DashboardGroup timelineGroup) {
        final RepositoryFolder timelineFolder = _tenantContextFactory.getContext(tenant).getTimelineFolder();
        final RepositoryFolder groupFolder = timelineFolder.getFolder(timelineGroup.getName());

        if (groupFolder == null) {
            throw new IllegalArgumentException("Timeline group '" + timelineGroup.getName() + "' does not exist.");
        }

        try {
            groupFolder.delete();
            return true;
        } catch (Exception e) {
            logger.warn("Failed to delete timeline group folder: " + timelineGroup.getName(), e);
            return false;
        }
    }

    @Override
    public boolean isDashboardEditor(TenantIdentifier tenant) {
        // this question cannot be answerred here. The wrapping SecureGwtServlet
        // will handle it.
        return true;
    }
}
