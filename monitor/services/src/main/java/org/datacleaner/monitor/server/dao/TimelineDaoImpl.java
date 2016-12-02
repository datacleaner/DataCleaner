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
package org.datacleaner.monitor.server.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.dashboard.model.DashboardGroup;
import org.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.datacleaner.monitor.dashboard.model.TimelineIdentifier;
import org.datacleaner.monitor.server.TimelineReader;
import org.datacleaner.monitor.server.WriteTimelineAction;
import org.datacleaner.monitor.server.jaxb.JaxbTimelineReader;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.repository.RepositoryNode;
import org.datacleaner.util.FileFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TimelineDaoImpl implements TimelineDao {

    private static final Logger logger = LoggerFactory.getLogger(TimelineDaoImpl.class);

    private final TenantContextFactory _tenantContextFactory;
    private final Repository _repository;

    @Autowired
    public TimelineDaoImpl(final TenantContextFactory tenantContextFactory, final Repository repository) {
        _tenantContextFactory = tenantContextFactory;
        _repository = repository;
    }

    @Override
    public boolean removeTimeline(final TimelineIdentifier timeline) {
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
        } catch (final IllegalStateException e) {
            logger.warn("Attempt to delete node failed: " + node, e);
            return false;
        }

        return true;
    }

    @Override
    public Map<TimelineIdentifier, TimelineDefinition> getTimelinesForJob(final TenantIdentifier tenant,
            final JobIdentifier job) {
        final Map<TimelineIdentifier, TimelineDefinition> result = new HashMap<>();
        if (job == null) {
            return result;
        }

        final List<TimelineIdentifier> timelinesForTenant = getTimelinesForTenant(tenant);

        for (final TimelineIdentifier timelineIdentifier : timelinesForTenant) {
            final TimelineDefinition timelineDefinition = getTimelineDefinition(timelineIdentifier);
            if (job.equals(timelineDefinition.getJobIdentifier())) {
                result.put(timelineIdentifier, timelineDefinition);
            }
        }

        return result;
    }

    @Override
    public List<TimelineIdentifier> getTimelinesForTenant(final TenantIdentifier tenant) {
        final RepositoryFolder timelinesFolder = _tenantContextFactory.getContext(tenant).getTimelineFolder();
        final List<TimelineIdentifier> result = new ArrayList<>();

        final List<RepositoryFolder> folders = timelinesFolder.getFolders();
        for (final RepositoryFolder repositoryFolder : folders) {
            final DashboardGroup group = new DashboardGroup(repositoryFolder.getName());
            addTimelines(result, group, repositoryFolder);
        }
        addTimelines(result, null, timelinesFolder);

        return result;
    }

    private void addTimelines(final List<TimelineIdentifier> result, final DashboardGroup group,
            final RepositoryFolder repositoryFolder) {
        final String extension = FileFilters.ANALYSIS_TIMELINE_XML.getExtension();
        final List<RepositoryFile> files = repositoryFolder.getFiles(null, extension);
        for (final RepositoryFile file : files) {
            final String timelineName = file.getName().substring(0, file.getName().length() - extension.length());
            final TimelineIdentifier timelineIdentifier =
                    new TimelineIdentifier(timelineName, file.getQualifiedPath(), group);
            result.add(timelineIdentifier);
        }
    }

    @Override
    public TimelineIdentifier updateTimeline(final TimelineIdentifier identifier, final TimelineDefinition definition) {
        final RepositoryFile file = (RepositoryFile) _repository.getRepositoryNode(identifier.getPath());

        file.writeFile(new WriteTimelineAction(definition));
        logger.info("Updated timeline definition in file: {}", file);
        return new TimelineIdentifier(identifier.getName(), file.getQualifiedPath(), identifier.getGroup());
    }

    @Override
    public TimelineDefinition getTimelineDefinition(final TimelineIdentifier timeline) {
        final String path = timeline.getPath();

        logger.info("Reading timeline from file: {}", path);

        final RepositoryFile timelineNode = (RepositoryFile) _repository.getRepositoryNode(path);

        if (timelineNode == null) {
            throw new IllegalArgumentException(
                    "No such timeline: " + timeline.getName() + " (in group: " + timeline.getGroup() + ")");
        }

        return timelineNode.readFile(in -> {
            final TimelineReader reader = new JaxbTimelineReader();
            return reader.read(in);
        });
    }

}
