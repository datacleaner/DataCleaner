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

import junit.framework.TestCase;

import org.apache.commons.vfs2.FileObject;
import org.eobjects.analyzer.util.VFSUtils;
import org.eobjects.datacleaner.actions.OpenAnalysisJobActionListener;
import org.eobjects.datacleaner.guice.DCModule;
import org.eobjects.datacleaner.user.UserPreferencesImpl;
import org.eobjects.datacleaner.windows.AbstractWindow;
import org.eobjects.datacleaner.windows.ResultWindow;

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

        DCModule module = new DCModule();

        FileObject file = VFSUtils.getFileSystemManager().resolveFile(
                "src/test/resources/all_analyzers.analysis.result.dat");

        OpenAnalysisJobActionListener listener = new OpenAnalysisJobActionListener(null, null, null, null, new UserPreferencesImpl(null), null);
        ResultWindow window = listener.openAnalysisResult(file, module);
        assertNotNull(window);

        assertEquals("all_analyzers.analysis.result.dat | Analysis results", window.getWindowTitle());
    }
}
