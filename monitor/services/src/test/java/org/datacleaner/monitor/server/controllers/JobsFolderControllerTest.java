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

import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.server.job.MockJobEngineManager;
import org.datacleaner.repository.file.FileRepository;
import org.junit.Test;

public class JobsFolderControllerTest {

    @Test
    public void testResultsFolderJsonMetadata() throws Exception {

        final FileRepository repository = new FileRepository("src/test/resources/example_repo/");
        final TenantContextFactory contextFactory = new TenantContextFactoryImpl(repository,
                new DataCleanerEnvironmentImpl(), new MockJobEngineManager());

        final JobsFolderController controller = new JobsFolderController();
        controller._contextFactory = contextFactory;
        final String resultsMetadata1 = controller.getFolderJobsByMetadataProperty("tenant6", "Category", "Enrichment").toString();
        assertEquals("[{descriptors=[{name=null, metadataProperties={CoordinatesY=244, CoordinatesX=294}, descriptor=Concatenator, type=transformer}], filename=concat_job_no_analyzers.analysis.xml, repository_path=/tenant6/jobs/concat_job_no_analyzers.analysis.xml, name=concat_job_no_analyzers, metadataProperties={Group=Person Enrichment, Category=Enrichment, CoordinatesX.GoldenRecords.person=154, CoordinatesY.GoldenRecords.person=77}}, {descriptors=[{name=null, metadataProperties={CoordinatesY=244, CoordinatesX=294}, descriptor=Concatenator, type=transformer}], filename=concat_job_no_datastore.analysis.xml, repository_path=/tenant6/jobs/concat_job_no_datastore.analysis.xml, name=concat_job_no_datastore, metadataProperties={Group=Person Enrichment, Category=Enrichment, CoordinatesX.GoldenRecords.person=154, CoordinatesY.GoldenRecords.person=77}}]", resultsMetadata1);

        final String resultsMetadata2 = controller.getFolderJobsByMetadataProperty("tenant6", "Group", "Person").toString();
        assertEquals(
                "[{descriptors=[{name=null, metadataProperties={CoordinatesY=289, CoordinatesX=487}, descriptor=String analyzer, type=analyzer}, {name=null, metadataProperties={CoordinatesY=244, CoordinatesX=294}, descriptor=Concatenator, type=transformer}], filename=concat_job_short_column_paths.analysis.xml, repository_path=/tenant6/jobs/concat_job_short_column_paths.analysis.xml, name=concat_job_short_column_paths, metadataProperties={Group=Person}}]",
                resultsMetadata2);

        final String resultsMetadata3 = controller.getFolderJobsByMetadataProperty("tenant6", "test", "Person").toString();
        assertEquals("[]", resultsMetadata3);

        final String resultsMetadata4 = controller.getFolderJobsByMetadataProperty("tenant6", "", "").toString();
        assertEquals("[]", resultsMetadata4);

    }
}
