/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.dashboard.DashboardService;
import org.eobjects.datacleaner.monitor.dashboard.model.ChartOptions.HorizontalAxisOption;
import org.eobjects.datacleaner.monitor.dashboard.model.DashboardGroup;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineData;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineDataRow;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineIdentifier;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.monitor.job.MetricJobContext;
import org.eobjects.datacleaner.monitor.job.MetricValues;
import org.eobjects.datacleaner.monitor.server.dao.ResultDao;
import org.eobjects.datacleaner.monitor.server.dao.TimelineDao;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFile.Type;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.Predicate;
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
    public DashboardServiceImpl(final TenantContextFactory tenantContextFactory, final MetricValueProducer metricValueProducer,
            ResultDao resultDao, TimelineDao timelineDao) {
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
            final MetricValues metricValues = _metricValueProducer.getMetricValues(metricIdentifiers, resultFile, tenant,
                    jobIdentifier);
            final Date date = metricValues.getMetricDate();
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
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        List<JobIdentifier> jobs = tenantContext.getJobs();
        jobs = CollectionUtils.filter(jobs, new Predicate<JobIdentifier>() {
            @Override
            public Boolean eval(JobIdentifier job) {
                final boolean analysisJob = JobIdentifier.JOB_TYPE_ANALYSIS_JOB.equals(job.getType());
                if (analysisJob) {
                    // in most cases we have DC jobs, and this evaluation is
                    // faster
                    return true;
                }
                final JobContext jobContext = tenantContext.getJob(job);
                if (jobContext instanceof MetricJobContext) {
                    return true;
                }
                return false;
            }
        });
        Collections.sort(jobs);
        return jobs;
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
}
