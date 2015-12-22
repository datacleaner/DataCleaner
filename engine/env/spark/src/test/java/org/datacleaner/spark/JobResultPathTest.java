package org.datacleaner.spark;

import org.apache.metamodel.util.Resource;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.spark.utils.ResultFilePathUtils;
import org.junit.Test;

import junit.framework.TestCase;

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

            final SparkJobContext sparkJobContext = new SparkJobContext("src/test/resources/conf_local.xml",
                    "src/test/resources/vanilla-job.analysis.xml",
                    "src/test/resources/jobProperties/jobAbsolutePath.properties", sparkContext);
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertEquals("file:///target/results/myresult.analysis.result.dat", sparkJobContext.getResultPath());
            assertEquals("vanilla-job", sparkJobContext.getJobName());
            assertTrue(ResultFilePathUtils.getResultResource(sparkContext, sparkJobContext).getQualifiedPath()
                    .endsWith("results/myresult.analysis.result.dat"));
        } finally {
            sparkContext.close();
        }
    }

    @Test
    public void testResultPathSpecifiedEmptyPath() throws Exception {
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + getName());
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext("src/test/resources/conf_local.xml",
                    "src/test/resources/vanilla-job.analysis.xml",
                    "src/test/resources/jobProperties/jobEmptyPath.properties", sparkContext);
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertEquals("", sparkJobContext.getResultPath());
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
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + getName());
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext("src/test/resources/conf_local.xml",
                    "src/test/resources/vanilla-job.analysis.xml",
                    "src/test/resources/jobProperties/jobRelativePath.properties", sparkContext);
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertEquals("target", sparkJobContext.getResultPath());
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
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + getName());
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext("src/test/resources/conf_local.xml",
                    "src/test/resources/vanilla-job.analysis.xml", null, sparkContext);
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertNull(sparkJobContext.getResultPath());
            final String analysisJobName = sparkJobContext.getJobName();
            assertEquals("vanilla-job", analysisJobName);
            final Resource resultResource = ResultFilePathUtils.getResultResource(sparkContext, sparkJobContext);
            final int lastIndexOfDash = resultResource.getQualifiedPath().lastIndexOf("-");
            assertTrue(resultResource.getQualifiedPath().contains(analysisJobName));
            assertEquals("/datacleaner/results/vanilla-job",
                    resultResource.getQualifiedPath().substring(0, lastIndexOfDash));

        } finally {
            sparkContext.close();
        }
    }
}
