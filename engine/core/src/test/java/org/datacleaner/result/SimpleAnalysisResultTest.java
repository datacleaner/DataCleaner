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
package org.datacleaner.result;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.datacleaner.job.ComponentJob;

public class SimpleAnalysisResultTest extends TestCase {

    public void testConstructAndGet() throws Exception {
        ComponentJob mock = EasyMock.createMock(ComponentJob.class);
        AnalyzerResult result = new NumberResult(42);
        Map<ComponentJob, AnalyzerResult> results = new HashMap<ComponentJob, AnalyzerResult>();
        results.put(mock, result);

        SimpleAnalysisResult analysisResult = new SimpleAnalysisResult(results);

        assertNotNull(analysisResult.getCreationDate());
        assertSame(result, analysisResult.getResult(mock));

        assertEquals(1, analysisResult.getResults().size());
        assertEquals(1, analysisResult.getResultMap().size());
    }
}
