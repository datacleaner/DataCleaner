/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.result;

import java.io.FileInputStream;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.util.ChangeAwareObjectInputStream;

public class SimpleAnalysisResultTest extends TestCase {

    /**
     * Deserializes 4 analysis result objects, created by running the same job 4
     * times. The test asserts that the job definition saved in one of the
     * result files can be used to retrieve information from the other analysis
     * results as well.
     * 
     * @throws Exception
     */
    public void testDeserializeAndCompare() throws Exception {
        AnalysisResult[] analysisResults = new AnalysisResult[4];

        for (int i = 0; i < analysisResults.length; i++) {
            String filename = "src/test/resources/resultfiles/out" + (i + 1) + ".analysis.result.dat";
            
            try {
                ChangeAwareObjectInputStream in = new ChangeAwareObjectInputStream(new FileInputStream(filename));
                analysisResults[i] = (AnalysisResult) in.readObject();
                in.close();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to deserialize file: " + filename, e);
            }
            
            assertNotNull(analysisResults[i]);
            assertTrue(analysisResults[i] instanceof SimpleAnalysisResult);
            
        }

        Map<ComponentJob, AnalyzerResult> resultMap = analysisResults[0].getResultMap();
        Set<ComponentJob> componentJobs = resultMap.keySet();
        assertEquals(8, componentJobs.size());

        for (int i = 0; i < analysisResults.length; i++) {
            AnalysisResult analysisResult = analysisResults[i];
            for (ComponentJob componentJob : componentJobs) {
                AnalyzerResult analyzerResult = analysisResult.getResult(componentJob);
                assertNotNull(analyzerResult);
            }
        }
    }
}
