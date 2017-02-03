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
package org.datacleaner.monitor.server.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.ws.rs.core.MediaType;

import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.scheduling.SchedulingService;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.datacleaner.monitor.server.job.MockJobEngineManager;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.repository.file.FileRepository;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class JobsFolderControllerTest {

    @Test
    public void testResultsFolderJsonMetadata() throws Exception {

        final FileRepository repository = new FileRepository("src/test/resources/example_repo/");
        final TenantContextFactory contextFactory =
                new TenantContextFactoryImpl(repository, new DataCleanerEnvironmentImpl(), new MockJobEngineManager());

        final JobsFolderController controller = new JobsFolderController();
        controller._contextFactory = contextFactory;
        final String resultsMetadata1 =
                controller.getFolderJobsByMetadataProperty("tenant6", "Category", "Enrichment").toString();
        assertEquals("[{descriptors=[{name=null, metadataProperties={CoordinatesY=244, CoordinatesX=294}, "
                        + "descriptor=Concatenator, type=transformer}], filename=concat_job_no_analyzers.analysis.xml, "
                        + "repository_path=/tenant6/jobs/concat_job_no_analyzers.analysis.xml, "
                        + "name=concat_job_no_analyzers, metadataProperties={Group=Person Enrichment, "
                        + "Category=Enrichment, CoordinatesX.GoldenRecords.person=154, "
                        + "CoordinatesY.GoldenRecords.person=77}}, {descriptors=[{name=null, "
                        + "metadataProperties={CoordinatesY=244, CoordinatesX=294}, descriptor=Concatenator, "
                        + "type=transformer}], filename=concat_job_no_datastore.analysis.xml, "
                        + "repository_path=/tenant6/jobs/concat_job_no_datastore.analysis.xml, "
                        + "name=concat_job_no_datastore, metadataProperties={Group=Person Enrichment, "
                        + "Category=Enrichment, CoordinatesX.GoldenRecords.person=154, "
                        + "CoordinatesY.GoldenRecords.person=77}}]", resultsMetadata1);

        final String resultsMetadata2 =
                controller.getFolderJobsByMetadataProperty("tenant6", "Group", "Person").toString();
        assertEquals("[{descriptors=[{name=null, metadataProperties={CoordinatesY=289, CoordinatesX=487}, "
                        + "descriptor=String analyzer, type=analyzer}, {name=null, "
                        + "metadataProperties={CoordinatesY=244, CoordinatesX=294}, descriptor=Concatenator, "
                        + "type=transformer}], filename=concat_job_short_column_paths.analysis.xml, "
                        + "repository_path=/tenant6/jobs/concat_job_short_column_paths.analysis.xml, "
                        + "name=concat_job_short_column_paths, metadataProperties={Group=Person}}]",
                resultsMetadata2);

        final String resultsMetadata3 =
                controller.getFolderJobsByMetadataProperty("tenant6", "test", "Person").toString();
        assertEquals("[]", resultsMetadata3);

        final String resultsMetadata4 = controller.getFolderJobsByMetadataProperty("tenant6", "", "").toString();
        assertEquals("[]", resultsMetadata4);

    }

    @Test
    public void testUploadAnalysisJobToFolderJson() throws Exception {
        final String jobFileName = "myjob.analysis.xml";

        final TenantContext context = mock(TenantContext.class);
        final TenantContextFactory contextFactory = mock(TenantContextFactory.class);
        final RepositoryFolder jobFolder = mock(RepositoryFolder.class);
        final RepositoryFile jobFile = mock(RepositoryFile.class);
        final SchedulingService schedulingService = mock(SchedulingService.class);
        final ScheduleDefinition scheduleDefinition = mock(ScheduleDefinition.class);

        when(contextFactory.getContext("test")).thenReturn(context);
        when(context.getJobFolder()).thenReturn(jobFolder);
        when(jobFolder.createFile(eq(jobFileName), any())).thenReturn(jobFile);
        when(schedulingService.getSchedule(argThat(tenant -> tenant.getId().equals("test")),
                argThat(job -> job.getName().equals("myjob")))).thenReturn(scheduleDefinition);
        when(jobFile.getType()).thenReturn(RepositoryFile.Type.ANALYSIS_JOB);
        when(jobFile.getName()).thenReturn(jobFileName);
        when(jobFile.getQualifiedPath()).thenReturn("/");

        final JobsFolderController jobsFolderController = new JobsFolderController();
        jobsFolderController._contextFactory = contextFactory;
        jobsFolderController._schedulingService = schedulingService;

        final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(jobsFolderController).build();

        final MockMultipartFile multipartFile =
                new MockMultipartFile("file", jobFileName, MediaType.TEXT_PLAIN, "dummy".getBytes());

        mockMvc.perform(fileUpload("/test/jobs").file(multipartFile).param("hotfolder", "/test"))
                .andExpect(status().isOk());

        verify(schedulingService, times(1)).updateSchedule(argThat(tenant -> tenant.getId().equals("test")),
                eq(scheduleDefinition));
        verify(scheduleDefinition, times(1)).setHotFolder("/test");
    }
}
