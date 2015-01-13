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
package org.datacleaner.monitor.server.job;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.datacleaner.monitor.job.JobEngine;
import org.datacleaner.monitor.job.JobEngineManager;
import org.datacleaner.monitor.job.MetricJobContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public class DefaultJobEngineManagerTest extends TestCase {

    private ApplicationContext applicationContext;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final File targetDir = new File("target/example_repo");
        FileUtils.deleteDirectory(targetDir);
        FileUtils.copyDirectory(new File("src/test/resources/example_repo"), targetDir);
        
        if (applicationContext == null) {
            applicationContext  = new ClassPathXmlApplicationContext("context/application-context.xml");
        }
    }

    public void testGetJobEngineVanilla() throws Exception {
        JobEngineManager manager = applicationContext.getBean(JobEngineManager.class);

        // check that we're actually testing this implementation
        assertTrue(manager instanceof DefaultJobEngineManager);

        JobEngine<?> engine;
        
        engine = manager.getJobEngine(DataCleanerJobContext.class);
        assertEquals(DataCleanerJobEngine.class, engine.getClass());
        
        engine = manager.getJobEngine(CustomJobContext.class);
        assertEquals(CustomJobEngine.class, engine.getClass());
    }
    
    public void testGetJobEngineHierarchy() throws Exception {
        JobEngineManager manager = applicationContext.getBean(JobEngineManager.class);

        // check that we're actually testing this implementation
        assertTrue(manager instanceof DefaultJobEngineManager);

        JobEngine<?> engine;
        
        engine = manager.getJobEngine(DataCleanerJobContextImpl.class);
        assertNotNull(engine);
        assertEquals(DataCleanerJobEngine.class, engine.getClass());

        engine = manager.getJobEngine(MetricJobContext.class);
        assertNull(engine);
    }
}
