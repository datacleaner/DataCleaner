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
package org.eobjects.datacleaner.monitor.configuration;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.configuration.InjectionManagerFactory;
import org.eobjects.analyzer.configuration.InjectionManagerFactoryImpl;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.monitor.job.JobEngineManager;
import org.eobjects.datacleaner.monitor.server.job.MockJobEngineManager;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFile.Type;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.repository.file.FileRepository;

public class TenantContextImplTest extends TestCase {

    private Repository repository;
    private InjectionManagerFactory injectionManagerFactory;
    private JobEngineManager jobEngineManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        repository = new FileRepository("src/test/resources/example_repo");
        injectionManagerFactory = new InjectionManagerFactoryImpl();
        jobEngineManager = new MockJobEngineManager();
    }

    public void testJobNameWithSignificantSpace() throws Exception {

        TenantContext tenantContext = new TenantContextImpl("tenant4", repository, injectionManagerFactory,
                jobEngineManager);

        JobContext job1 = tenantContext.getJob("my job");
        JobContext job2 = tenantContext.getJob("my job ");
        JobContext job3 = tenantContext.getJob("my job");
        JobContext job4 = tenantContext.getJob("my job ");

        assertNotNull(job1);
        assertNotNull(job2);
        assertNotNull(job3);
        assertNotNull(job4);

        assertNotSame(job1, job2);

        assertSame(job1, job3);
        assertSame(job2, job4);

        assertEquals("my job.analysis.xml", job1.getJobFile().getName());
        assertEquals("my job .analysis.xml", job2.getJobFile().getName());
    }

    public void testGetMetadataFolder() {
        TenantContext tenantContext = new TenantContextImpl("tenant1", repository, injectionManagerFactory,
                jobEngineManager);

        RepositoryFolder metadataFolder = tenantContext.getMetadataFolder();
        List<RepositoryFile> files = metadataFolder.getFiles();
        assertEquals(10, files.size());
    }

    public void testGettingMetadataFiles() {
        TenantContext tenantContext = new TenantContextImpl("tenant1", repository, injectionManagerFactory,
                jobEngineManager);

        RepositoryFolder metadataFolder = tenantContext.getMetadataFolder();
        List<RepositoryFile> sourcerecordPersonXMLFiles = metadataFolder.getFiles("SBL.sourcerecord", "person.xml");
        assertEquals(1, sourcerecordPersonXMLFiles.size());

        List<RepositoryFile> goldenrecordPersonXMLFiles = metadataFolder.getFiles("goldenrecord", "organization.xml");
        assertEquals(1, goldenrecordPersonXMLFiles.size());

    }

    public void testMetadataFileTypes() {
        TenantContext tenantContext = new TenantContextImpl("tenant1", repository, injectionManagerFactory,
                jobEngineManager);

        RepositoryFolder metadataFolder = tenantContext.getMetadataFolder();
        List<RepositoryFile> files = metadataFolder.getFiles();

        for (Iterator<RepositoryFile> iterator = files.iterator(); iterator.hasNext();) {
            RepositoryFile repositoryFile = (RepositoryFile) iterator.next();
            if (!repositoryFile.getName().contains("analysis")) {
                Type type = repositoryFile.getType();
                assertEquals(Type.METADATA, type);
            }
        }

    }

}
