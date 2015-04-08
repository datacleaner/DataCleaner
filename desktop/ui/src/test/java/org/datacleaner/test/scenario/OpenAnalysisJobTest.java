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
package org.datacleaner.test.scenario;

import java.awt.GraphicsEnvironment;

import junit.framework.TestCase;

import org.apache.commons.vfs2.FileObject;
import org.datacleaner.actions.OpenAnalysisJobActionListener;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.guice.DCModule;
import org.datacleaner.guice.DCModuleImpl;
import org.datacleaner.util.VFSUtils;
import org.datacleaner.windows.AbstractWindow;
import org.datacleaner.windows.AnalysisJobBuilderWindow;
import org.datacleaner.windows.AnalysisJobBuilderWindowImpl;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class OpenAnalysisJobTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty(AbstractWindow.SYSTEM_PROPERTY_HIDE_WINDOWS, "true");
    }

    /**
     * A very broad integration test which opens a job with (more or less) all
     * built-in analyzers.
     * 
     * @throws Exception
     */
    public void testOpenJobWithManyAnalyzers() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("!!! Skipping test because environment is headless: " + getName());
            return;
        }

        DCModule module = new DCModuleImpl();
        Injector injector = Guice.createInjector(module);
        DataCleanerConfiguration configuration = injector.getInstance(DataCleanerConfiguration.class);

        FileObject file = VFSUtils.getFileSystemManager().resolveFile("src/test/resources/all_analyzers.analysis.xml");
        assertTrue(file.exists());

        injector = OpenAnalysisJobActionListener.open(file, configuration, injector);
        AnalysisJobBuilderWindow window = injector.getInstance(AnalysisJobBuilderWindow.class);

        assertNotNull(window);

        assertEquals("all_analyzers.analysis.xml", window.getJobFile().getName().getBaseName());

        ((AnalysisJobBuilderWindowImpl) window).updateStatusLabel();

        assertEquals("Job is correctly configured", window.getStatusLabelText());
    }
}
