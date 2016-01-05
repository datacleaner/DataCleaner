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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;

import org.apache.metamodel.util.Resource;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.spark.utils.ResultFilePathUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * This class tests the path of the result specified in the job properties file
 * or otherwise.
 */
public class JobResultPathTest {

    @Rule
    public TestName name = new TestName();

    @Test
    public void testResultPathAbolutePathSpecified() throws Exception {
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + name.getMethodName());
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(URI.create("src/test/resources/conf_local.xml"),
                    URI.create("src/test/resources/vanilla-job.analysis.xml"), URI.create(
                            "src/test/resources/jobProperties/jobAbsolutePath.properties"), sparkContext);
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertEquals("file:///target/results/myresult.analysis.result.dat", sparkJobContext.getResultPath()
                    .toString());
            assertEquals("vanilla-job", sparkJobContext.getJobName());
            assertTrue(ResultFilePathUtils.getResultResource(sparkContext, sparkJobContext).getQualifiedPath()
                    .endsWith("results" + File.separator + "myresult.analysis.result.dat"));
        } finally {
            sparkContext.close();
        }
    }

    @Test
    public void testResultPathSpecifiedEmptyPath() throws Exception {
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + name.getMethodName());
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(URI.create("src/test/resources/conf_local.xml"),
                    URI.create("src/test/resources/vanilla-job.analysis.xml"), URI.create(
                            "src/test/resources/jobProperties/jobEmptyPath.properties"), sparkContext);
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertEquals(null, sparkJobContext.getResultPath());
            final String analysisJobName = sparkJobContext.getJobName();
            assertEquals("vanilla-job", analysisJobName);

            final Resource resultResource = ResultFilePathUtils.getResultResource(sparkContext, sparkJobContext);
            assertTrue(resultResource.getQualifiedPath().contains(analysisJobName));

        } finally {
            sparkContext.close();
        }
    }

    @Test
    public void testResultPathNameJustFolderSpecified() throws Exception {
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + name.getMethodName());
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(URI.create("src/test/resources/conf_local.xml"),
                    URI.create("src/test/resources/vanilla-job.analysis.xml"), URI.create(
                            "src/test/resources/jobProperties/jobRelativePath.properties"), sparkContext);
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertEquals("target", sparkJobContext.getResultPath().toString());
            final String analysisJobName = sparkJobContext.getJobName();
            assertEquals("vanilla-job", analysisJobName);

            final Resource resultResource = ResultFilePathUtils.getResultResource(sparkContext, sparkJobContext);
            assertTrue(resultResource.getQualifiedPath().contains(analysisJobName));
        } finally {
            sparkContext.close();
        }
    }

    @Test
    public void testResultPathNameNoVariableSpecified() throws Exception {
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + name.getMethodName());
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(URI.create("src/test/resources/conf_local.xml"),
                    URI.create("src/test/resources/vanilla-job.analysis.xml"), null, sparkContext);
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertNull(sparkJobContext.getResultPath());
            final String analysisJobName = sparkJobContext.getJobName();
            assertEquals("vanilla-job", analysisJobName);
            final Resource resultResource = ResultFilePathUtils.getResultResource(sparkContext, sparkJobContext);
            final int lastIndexOfDash = resultResource.getQualifiedPath().lastIndexOf("-");
            assertTrue(resultResource.getQualifiedPath().contains(analysisJobName));
            assertTrue(resultResource.getQualifiedPath().substring(0, lastIndexOfDash)
                    .endsWith("datacleaner" + File.separator + "results" + File.separator + "vanilla-job"));
        } finally {
            sparkContext.close();
        }
    }
}
