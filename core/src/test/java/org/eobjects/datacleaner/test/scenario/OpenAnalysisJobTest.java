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
package org.eobjects.datacleaner.test.scenario;

import java.awt.GraphicsEnvironment;
import java.io.File;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.datacleaner.actions.OpenAnalysisJobActionListener;
import org.eobjects.datacleaner.guice.DCModule;
import org.eobjects.datacleaner.windows.AbstractWindow;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;

import com.google.inject.Guice;
import com.google.inject.Injector;

import junit.framework.TestCase;

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
    public void testOpenJobWithAllAnalyzers() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("!!! Skipping test because environment is headless: " + getName());
            return;
        }
        
        DCModule module = new DCModule(new File("."));
        Injector injector = Guice.createInjector(module);
        AnalyzerBeansConfiguration configuration = injector.getInstance(AnalyzerBeansConfiguration.class);

        File file = new File("src/test/resources/all_analyzers.analysis.xml");

        AnalysisJobBuilderWindow window = OpenAnalysisJobActionListener.open(file, configuration, injector);
        assertNotNull(window);
        
        assertEquals("all_analyzers.analysis.xml", window.getJobFilename());
        assertEquals("Job is correctly configured", window.getStatusLabelText());
    }
}
