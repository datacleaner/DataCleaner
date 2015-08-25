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
package org.datacleaner.spark;

import junit.framework.TestCase;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.datacleaner.job.AnalysisJob;
import org.junit.Test;

/**
 * Ignored until Jackson, Guava etc. dependency conflict is resolved.
 *
 */
public class SparkAnalysisRunnerTest extends TestCase {

    @Test
    public void testVanillaScenario() throws Exception {
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest");
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(sparkContext, "src/test/resources/conf.xml",
                    "src/test/resources/vanilla-job.analysis.xml");
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);

            final SparkAnalysisRunner sparkAnalysisRunner = new SparkAnalysisRunner(sparkContext, sparkJobContext);
            sparkAnalysisRunner.run(job);
        } finally {
            sparkContext.close();
        }
    }
}
