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
import org.datacleaner.util.VFSUtils;
import org.datacleaner.actions.OpenAnalysisJobActionListener;
import org.datacleaner.guice.DCModule;
import org.datacleaner.guice.DCModuleImpl;
import org.datacleaner.user.UserPreferencesImpl;
import org.datacleaner.windows.AbstractWindow;
import org.datacleaner.windows.ResultWindow;

public class OpenAnalysisResultTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty(AbstractWindow.SYSTEM_PROPERTY_HIDE_WINDOWS, "true");
    }

    /**
     * A very broad integration test which opens a result with (more or less)
     * all built-in analyzer results.
     * 
     * @throws Exception
     */
    public void testOpenJobWithAllAnalyzers() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("!!! Skipping test because environment is headless: " + getName());
            return;
        }

        DCModule module = new DCModuleImpl();

        FileObject file = VFSUtils.getFileSystemManager().resolveFile(
                "src/test/resources/all_analyzers.analysis.result.dat");

        OpenAnalysisJobActionListener listener = new OpenAnalysisJobActionListener(null, null, null, null, new UserPreferencesImpl(null));
        ResultWindow window = listener.openAnalysisResult(file, module);
        assertNotNull(window);

        assertEquals("all_analyzers.analysis.result.dat | Analysis results", window.getWindowTitle());
    }

    public void testDensityPlotVisualizationComponentLoading() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("!!! Skipping test because environment is headless: " + getName());
            return;
        }

        DCModule module = new DCModuleImpl();

        FileObject file = VFSUtils.getFileSystemManager().resolveFile(
                "src/test/resources/densityplot.analysis.result.dat");

        OpenAnalysisJobActionListener listener = new OpenAnalysisJobActionListener(null, null, null, null, new UserPreferencesImpl(null));
        ResultWindow window = listener.openAnalysisResult(file, module);
        assertNotNull(window);

        assertEquals("densityplot.analysis.result.dat | Analysis results", window.getWindowTitle());
    }

    public void testStackedAreaPlotVisualizationComponentLoading() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("!!! Skipping test because environment is headless: " + getName());
            return;
        }

        DCModule module = new DCModuleImpl();

        FileObject file = VFSUtils.getFileSystemManager().resolveFile(
                "src/test/resources/stackedarea.analysis.result.dat");

        OpenAnalysisJobActionListener listener = new OpenAnalysisJobActionListener(null, null, null, null, new UserPreferencesImpl(null));
        ResultWindow window = listener.openAnalysisResult(file, module);
        assertNotNull(window);

        assertEquals("stackedarea.analysis.result.dat | Analysis results", window.getWindowTitle());
    }

    public void testScatterPlotVisualizationComponentLoading() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("!!! Skipping test because environment is headless: " + getName());
            return;
        }

        DCModule module = new DCModuleImpl();

        FileObject file = VFSUtils.getFileSystemManager().resolveFile(
                "src/test/resources/scatterplot.analysis.result.dat");

        OpenAnalysisJobActionListener listener = new OpenAnalysisJobActionListener(null, null, null, null, new UserPreferencesImpl(null));
        ResultWindow window = listener.openAnalysisResult(file, module);
        assertNotNull(window);

        assertEquals("scatterplot.analysis.result.dat | Analysis results", window.getWindowTitle());
    }

}
