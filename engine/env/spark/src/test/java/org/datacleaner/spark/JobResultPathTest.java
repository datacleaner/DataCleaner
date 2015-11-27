package org.datacleaner.spark;

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
import junit.framework.TestCase;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.spark.utils.ResultFilePathUtils;
import org.junit.Test;

/**
 * This class tests the path of the result specified in the job properties file
 * or otherwise. 
 */
public class JobResultPathTest extends TestCase {

    @Test
    public void testResultPathAbolutePathSpecified() throws Exception {
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + getName());
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(sparkContext,
                    "src/test/resources/conf_local.xml", "src/test/resources/vanilla-job.analysis.xml",
                    "src/test/resources/jobProperties/jobAbsolutePath.properties");
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertEquals("hdfs://bigdatavm:9000/target/results/myresult.analysis.result.dat",
                    sparkJobContext.getResultPath());
            assertEquals("vanilla-job", sparkJobContext.getAnalysisJobName());
            assertEquals("hdfs://bigdatavm:9000/target/results/myresult.analysis.result.dat",
                    ResultFilePathUtils.getResultFilePath(sparkContext, sparkJobContext));
        } finally {
            sparkContext.close();
        }
    }

    @Test
    public void testResultPathSpecifiedEmptyPath() throws Exception {
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + getName());
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(sparkContext,
                    "src/test/resources/conf_local.xml", "src/test/resources/vanilla-job.analysis.xml",
                    "src/test/resources/jobProperties/jobEmptyPath.properties");
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertEquals("", sparkJobContext.getResultPath());
            final String analysisJobName = sparkJobContext.getAnalysisJobName();
            assertEquals("vanilla-job", analysisJobName);

            final String resultJobFilePath = ResultFilePathUtils.getResultFilePath(sparkContext, sparkJobContext);
            assertTrue(resultJobFilePath.contains(analysisJobName));

        } finally {
            sparkContext.close();
        }
    }

    @Test
    public void testResultPathNameJustFolderSpecified() throws Exception {
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + getName());
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(sparkContext,
                    "src/test/resources/conf_local.xml", "src/test/resources/vanilla-job.analysis.xml",
                    "src/test/resources/jobProperties/jobRelativePath.properties");
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertEquals("/target/results", sparkJobContext.getResultPath());
            final String analysisJobName = sparkJobContext.getAnalysisJobName();
            assertEquals("vanilla-job", analysisJobName);

            final String resultJobFilePath = ResultFilePathUtils.getResultFilePath(sparkContext, sparkJobContext);
            final int lastIndexOfDash = resultJobFilePath.lastIndexOf("-");
            assertTrue(resultJobFilePath.contains(analysisJobName));
            assertEquals("hdfs://target/results/vanilla-job", resultJobFilePath.substring(0, lastIndexOfDash));

        } finally {
            sparkContext.close();
        }
    }

    @Test
    public void testResultPathNameJustANameSpecified() throws Exception {
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + getName());
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(sparkContext,
                    "src/test/resources/conf_local.xml", "src/test/resources/vanilla-job.analysis.xml",
                    "src/test/resources/jobProperties/jobSimpleNamePath.properties");
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertEquals("myresult", sparkJobContext.getResultPath());
            final String analysisJobName = sparkJobContext.getAnalysisJobName();
            assertEquals("vanilla-job", analysisJobName);

            final String resultJobFilePath = ResultFilePathUtils.getResultFilePath(sparkContext, sparkJobContext);
            final int lastIndexOfDash = resultJobFilePath.lastIndexOf("-");
            assertTrue(resultJobFilePath.contains(analysisJobName));
            assertEquals("hdfs://myresult/vanilla-job", resultJobFilePath.substring(0, lastIndexOfDash));

        } finally {
            sparkContext.close();
        }
    }

    @Test
    public void testResultPathNameNoVariableSpecified() throws Exception {
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + getName());
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {
  
            final SparkJobContext sparkJobContext = new SparkJobContext(sparkContext,
                    "src/test/resources/conf_local.xml", "src/test/resources/vanilla-job.analysis.xml");
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertNull(sparkJobContext.getResultPath());
            final String analysisJobName = sparkJobContext.getAnalysisJobName();
            assertEquals("vanilla-job", analysisJobName);
            final String resultJobFilePath = ResultFilePathUtils.getResultFilePath(sparkContext, sparkJobContext);
            final int lastIndexOfDash = resultJobFilePath.lastIndexOf("-");
            assertTrue(resultJobFilePath.contains(analysisJobName));
            assertEquals("hdfs://datacleaner/results/vanilla-job", resultJobFilePath.substring(0, lastIndexOfDash));

        } finally {
            sparkContext.close();
        }
    }

    @Test
    public void testCreatePath() {
        final String newPath1 = ResultFilePathUtils.createPath("hdfs://bigdatavm:9000", "mypath/myfile.analysis.result.dat");
        assertEquals("hdfs://bigdatavm:9000/mypath/myfile.analysis.result.dat", newPath1);
        
        final String newPath2 = ResultFilePathUtils.createPath("hdfs://bigdatavm:9000", "/mypath/myfile.analysis.result.dat");
        assertEquals("hdfs://bigdatavm:9000/mypath/myfile.analysis.result.dat", newPath2);
    }

}
