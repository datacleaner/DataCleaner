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
import org.junit.Test;

/**
 * This class tests the path of the result specified in the job properties file
 * or otherwise. The default setting for 'fs.defaultFS' is 'file:///'
 */
public class JobResultPathTest extends TestCase {

    @Test
    public void testResultJobNameCorrectSpecified() throws Exception {
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + getName());
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(sparkContext,
                    "src/test/resources/conf_local.xml", "src/test/resources/vanilla-job.analysis.xml",
                    "src/test/resources/jobProperties/job.properties");
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertEquals("hdfs://bigdatavm:9000/target/results/myresult.analysis.result.dat",
                    sparkJobContext.getResultPathUsercustomized());
            assertEquals("vanilla-job", sparkJobContext.getAnalysisJobName());
            assertEquals("hdfs://bigdatavm:9000/target/results/myresult.analysis.result.dat",
                    sparkJobContext.getResultJobFilePath());
        } finally {
            sparkContext.close();
        }
    }

    @Test
    public void testResultJobNameSpecifiedEmpty() throws Exception {
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + getName());
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(sparkContext,
                    "src/test/resources/conf_local.xml", "src/test/resources/vanilla-job.analysis.xml",
                    "src/test/resources/jobProperties/job1.properties");
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertEquals("", sparkJobContext.getResultPathUsercustomized());
            final String analysisJobName = sparkJobContext.getAnalysisJobName();
            assertEquals("vanilla-job", analysisJobName);

            final String resultJobFilePath = sparkJobContext.getResultJobFilePath();
            assertTrue(resultJobFilePath.contains(analysisJobName));

        } finally {
            sparkContext.close();
        }
    }

    @Test
    public void testResultJobNameJustFolderSpecified() throws Exception {
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + getName());
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(sparkContext,
                    "src/test/resources/conf_local.xml", "src/test/resources/vanilla-job.analysis.xml",
                    "src/test/resources/jobProperties/job2.properties");
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertEquals("target/results/", sparkJobContext.getResultPathUsercustomized());
            final String analysisJobName = sparkJobContext.getAnalysisJobName();
            assertEquals("vanilla-job", analysisJobName);

            final String resultJobFilePath = sparkJobContext.getResultJobFilePath();
            final int lastIndexOfDash = resultJobFilePath.lastIndexOf("-");
            assertTrue(resultJobFilePath.contains(analysisJobName));
            assertEquals("file:///target/results/vanilla-job", resultJobFilePath.substring(0, lastIndexOfDash));

        } finally {
            sparkContext.close();
        }
    }

    @Test
    public void testResultJobNameJustANameSpecified() throws Exception {
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + getName());
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(sparkContext,
                    "src/test/resources/conf_local.xml", "src/test/resources/vanilla-job.analysis.xml",
                    "src/test/resources/jobProperties/job3.properties");
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertEquals("myresult", sparkJobContext.getResultPathUsercustomized());
            final String analysisJobName = sparkJobContext.getAnalysisJobName();
            assertEquals("vanilla-job", analysisJobName);

            final String resultJobFilePath = sparkJobContext.getResultJobFilePath();
            final int lastIndexOfDash = resultJobFilePath.lastIndexOf("-");
            assertTrue(resultJobFilePath.contains(analysisJobName));
            assertEquals("file:///myresult/vanilla-job", resultJobFilePath.substring(0, lastIndexOfDash));

        } finally {
            sparkContext.close();
        }
    }

    @Test
    public void testResultJobNameNoVariableSpecified() throws Exception {
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + getName());
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(sparkContext,
                    "src/test/resources/conf_local.xml", "src/test/resources/vanilla-job.analysis.xml");
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertNull(sparkJobContext.getResultPathUsercustomized());
            final String analysisJobName = sparkJobContext.getAnalysisJobName();
            assertEquals("vanilla-job", analysisJobName);
            final String resultJobFilePath = sparkJobContext.getResultJobFilePath();
            final int lastIndexOfDash = resultJobFilePath.lastIndexOf("-");
            assertTrue(resultJobFilePath.contains(analysisJobName));
            assertEquals("file:////datacleaner/results/vanilla-job", resultJobFilePath.substring(0, lastIndexOfDash));

        } finally {
            sparkContext.close();
        }
    }

}
