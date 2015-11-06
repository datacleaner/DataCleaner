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

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Func;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.beans.CompletenessAnalyzerResult;
import org.datacleaner.beans.StringAnalyzerResult;
import org.datacleaner.beans.uniqueness.UniqueKeyCheckAnalyzerResult;
import org.datacleaner.beans.valuedist.GroupedValueDistributionResult;
import org.datacleaner.beans.valuedist.ValueDistributionAnalyzerResult;
import org.datacleaner.beans.valuematch.ValueMatchAnalyzerResult;
import org.datacleaner.beans.writers.WriteDataResult;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.result.ReducedSingleValueDistributionResult;
import org.datacleaner.result.ValueCountingAnalyzerResult;
import org.junit.Test;

/**
 * Ignored until Jackson, Guava etc. dependency conflict is resolved.
 *
 */
public class SparkAnalysisRunnerTest extends TestCase {

    private static final int MIN_PARTITIONS_MULTIPLE = 4;

    @Test
    public void testVanillaScenario() throws Exception {
        final AnalysisResultFuture result;

        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + getName());
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(sparkContext,
                    "src/test/resources/conf_local.xml", "src/test/resources/vanilla-job.analysis.xml");
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);

            final SparkAnalysisRunner sparkAnalysisRunner = new SparkAnalysisRunner(sparkContext, sparkJobContext);

            result = sparkAnalysisRunner.run(job);
        } finally {
            sparkContext.close();
        }

        if (result.isErrornous()) {
            throw (Exception) result.getErrors().get(0);
        }

        final List<AnalyzerResult> results = result.getResults();
        assertEquals(2, results.size());

        final StringAnalyzerResult stringAnalyzerResult = result.getResults(StringAnalyzerResult.class).get(0);
        assertEquals("[MetaModelInputColumn[resources.person_names.txt.company]]",
                Arrays.toString(stringAnalyzerResult.getColumns()));

        final int rowCount = stringAnalyzerResult.getRowCount(stringAnalyzerResult.getColumns()[0]);
        assertEquals(7, rowCount);

        final int upperCaseChars = stringAnalyzerResult.getEntirelyUpperCaseCount(stringAnalyzerResult.getColumns()[0]);
        assertEquals(7, upperCaseChars);
    }
    
    @Test
    public void testWriteDataScenario() throws Exception {
        final String outputPath = "target/write-job.csv";
        final File outputFile = new File(outputPath);
        if (outputFile.exists() && outputFile.isDirectory()) {
            FileUtils.deleteDirectory(outputFile);
        }
        
        final AnalysisResultFuture result;

        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + getName());
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(sparkContext,
                    "src/test/resources/conf_local.xml", "src/test/resources/write-job.analysis.xml");
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);

            final SparkAnalysisRunner sparkAnalysisRunner = new SparkAnalysisRunner(sparkContext, sparkJobContext, MIN_PARTITIONS_MULTIPLE);

            result = sparkAnalysisRunner.run(job);
        } finally {
            sparkContext.close();
        }

        if (result.isErrornous()) {
            throw (Exception) result.getErrors().get(0);
        }

        final List<AnalyzerResult> results = result.getResults();
        assertEquals(1, results.size());

        final WriteDataResult writeDataResult = result.getResults(WriteDataResult.class).get(0);
        assertEquals(7, writeDataResult.getWrittenRowCount());
        
        assertTrue(outputFile.isDirectory());
        
        // file resource is capable of viewing the directory like it is a single file
        final FileResource fileResource = new FileResource(outputFile);
        final String str = fileResource.read(new Func<InputStream, String>() {
            @Override
            public String eval(InputStream in) {
                return FileHelper.readInputStreamAsString(in, "UTF8");
            }
        });
        
        final String[] lines = str.replaceAll("\r", "").split("\n");
        assertEquals("\"COUNTRY\",\"CUSTOMERNUMBER\"", lines[0]);
        assertEquals("\"Denmark\",\"HI\"", lines[1]);
        
        // asserting 8 lines is important - 7 data lines and 1 header line
        assertEquals(8, lines.length);
    }

    @Test
    public void testOutputDataStreamsScenario() throws Exception {
        final AnalysisResultFuture result;

        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName(
                "DCTest - testOutputDataStreamsScenario");
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(sparkContext,
                    "src/test/resources/conf_local.xml", "src/test/resources/melon-job.analysis.xml");
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);

            final SparkAnalysisRunner sparkAnalysisRunner = new SparkAnalysisRunner(sparkContext, sparkJobContext);

            result = sparkAnalysisRunner.run(job);
        } finally {
            sparkContext.close();
        }

        if (result.isErrornous()) {
            throw (Exception) result.getErrors().get(0);
        }

        final List<AnalyzerResult> results = result.getResults();
        assertEquals(3, results.size());

        final CompletenessAnalyzerResult completenessAnalyzerResult = result.getResults(
                CompletenessAnalyzerResult.class).get(0);
        assertEquals(7, completenessAnalyzerResult.getTotalRowCount());
        assertEquals(7, completenessAnalyzerResult.getValidRowCount());
        assertEquals(0, completenessAnalyzerResult.getInvalidRowCount());

        final ValueMatchAnalyzerResult incompleteValueMatcherAnalyzerResult = result.getResults(
                ValueMatchAnalyzerResult.class).get(0);
        assertEquals(0, incompleteValueMatcherAnalyzerResult.getTotalCount());
        assertEquals(Integer.valueOf(0), incompleteValueMatcherAnalyzerResult.getCount("Kasper"));

        final ValueMatchAnalyzerResult completeValueMatcherAnalyzerResult = result.getResults(
                ValueMatchAnalyzerResult.class).get(1);
        assertEquals(7, completeValueMatcherAnalyzerResult.getTotalCount());
        assertEquals(Integer.valueOf(1), completeValueMatcherAnalyzerResult.getCount("Tomasz"));
        assertEquals(Integer.valueOf(6), completeValueMatcherAnalyzerResult.getUnexpectedValueCount());
    }

    @Test
    public void testOutputDataStreamsNonDistributableScenario() throws Exception {
        final AnalysisResultFuture result;

        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName(
                "DCTest - testOutputDataStreamsNonDistributableScenario");
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(sparkContext,
                    "src/test/resources/conf_local.xml", "src/test/resources/non-dist-melon-job.analysis.xml");
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);

            final SparkAnalysisRunner sparkAnalysisRunner = new SparkAnalysisRunner(sparkContext, sparkJobContext, MIN_PARTITIONS_MULTIPLE);

            result = sparkAnalysisRunner.run(job);
        } finally {
            sparkContext.close();
        }

        if (result.isErrornous()) {
            throw (Exception) result.getErrors().get(0);
        }

        final List<AnalyzerResult> results = result.getResults();
        assertEquals(3, results.size());

        final CompletenessAnalyzerResult completenessAnalyzerResult = result.getResults(
                CompletenessAnalyzerResult.class).get(0);
        assertEquals(7, completenessAnalyzerResult.getTotalRowCount());
        assertEquals(7, completenessAnalyzerResult.getValidRowCount());
        assertEquals(0, completenessAnalyzerResult.getInvalidRowCount());

        final ValueMatchAnalyzerResult incompleteValueMatcherAnalyzerResult = result.getResults(
                ValueMatchAnalyzerResult.class).get(0);
        assertEquals(0, incompleteValueMatcherAnalyzerResult.getTotalCount());
        assertEquals(Integer.valueOf(0), incompleteValueMatcherAnalyzerResult.getCount("Kasper"));

        final UniqueKeyCheckAnalyzerResult uniqueKeyCheckAnalyzerResult = result.getResults(
                UniqueKeyCheckAnalyzerResult.class).get(0);
        assertEquals(7, uniqueKeyCheckAnalyzerResult.getRowCount());
        assertEquals(7, uniqueKeyCheckAnalyzerResult.getUniqueCount());
        assertEquals(0, uniqueKeyCheckAnalyzerResult.getNonUniqueCount());
        assertEquals(0, uniqueKeyCheckAnalyzerResult.getNullCount());
        // TODO: It would also be nice to have a flag indicating
        // distributable/non-distributable job that we could assert.
    }

    @Test
    public void testValueDistributionReducer() throws Exception {
        final AnalysisResultFuture result;

        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName(
                "DCTest - testValueDistributionReducer");
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(sparkContext,
                    "src/test/resources/conf_local.xml", "src/test/resources/distributable-value-dist.analysis.xml");
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);

            final SparkAnalysisRunner sparkAnalysisRunner = new SparkAnalysisRunner(sparkContext, sparkJobContext, MIN_PARTITIONS_MULTIPLE);

            result = sparkAnalysisRunner.run(job);
        } finally {
            sparkContext.close();
        }

        if (result.isErrornous()) {
            throw (Exception) result.getErrors().get(0);
        }

        final List<AnalyzerResult> results = result.getResults();
        assertEquals(1, results.size());

        final ValueDistributionAnalyzerResult completeValueDistributionAnalyzerResult = result.getResults(
                ValueDistributionAnalyzerResult.class).get(0);
        assertEquals(7, completeValueDistributionAnalyzerResult.getTotalCount());
        assertEquals(Integer.valueOf(7), completeValueDistributionAnalyzerResult.getUniqueCount());
        assertEquals(Integer.valueOf(7), completeValueDistributionAnalyzerResult.getDistinctCount());
        assertEquals(0, completeValueDistributionAnalyzerResult.getNullCount());
    }

    @Test
    public void testGroupedValueDistributionReducer() throws Exception {
        final AnalysisResultFuture result;

        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName(
                "DCTest - testGroupedValueDistributionReducer");
        final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        try {

            final SparkJobContext sparkJobContext = new SparkJobContext(sparkContext,
                    "src/test/resources/conf_local.xml",
                    "src/test/resources/distributable-grouped-value-dist.analysis.xml");
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);

            final SparkAnalysisRunner sparkAnalysisRunner = new SparkAnalysisRunner(sparkContext, sparkJobContext, MIN_PARTITIONS_MULTIPLE);

            result = sparkAnalysisRunner.run(job);
        } finally {
            sparkContext.close();
        }

        if (result.isErrornous()) {
            throw (Exception) result.getErrors().get(0);
        }

        final List<AnalyzerResult> results = result.getResults();
        assertEquals(1, results.size());

        final ValueDistributionAnalyzerResult completeValueDistributionAnalyzerResult = result.getResults(
                ValueDistributionAnalyzerResult.class).get(0);
        assertEquals(GroupedValueDistributionResult.class, completeValueDistributionAnalyzerResult.getClass());
        GroupedValueDistributionResult completeGroupedResult = (GroupedValueDistributionResult) completeValueDistributionAnalyzerResult;
        Iterator<? extends ValueCountingAnalyzerResult> iterator = completeGroupedResult.getGroupResults().iterator();
        ReducedSingleValueDistributionResult group1 = (ReducedSingleValueDistributionResult) iterator.next();
        ReducedSingleValueDistributionResult group2 = (ReducedSingleValueDistributionResult) iterator.next();

        if (group1.getName().equals("Denmark")) {
            assertEquals("Denmark", group1.getName());
            assertEquals(4, group1.getTotalCount());
            assertEquals(Integer.valueOf(4), group1.getUniqueCount());
            assertEquals(Integer.valueOf(4), group1.getDistinctCount());
            assertEquals(0, group1.getNullCount());

            assertEquals("Netherlands", group2.getName());
            assertEquals(3, group2.getTotalCount());
            assertEquals(Integer.valueOf(3), group2.getUniqueCount());
            assertEquals(Integer.valueOf(3), group2.getDistinctCount());
            assertEquals(0, group2.getNullCount());
        } else {
            assertEquals("Denmark", group2.getName());
            assertEquals(4, group2.getTotalCount());
            assertEquals(Integer.valueOf(4), group2.getUniqueCount());
            assertEquals(Integer.valueOf(4), group2.getDistinctCount());
            assertEquals(0, group2.getNullCount());

            assertEquals("Netherlands", group1.getName());
            assertEquals(3, group1.getTotalCount());
            assertEquals(Integer.valueOf(3), group1.getUniqueCount());
            assertEquals(Integer.valueOf(3), group1.getDistinctCount());
            assertEquals(0, group1.getNullCount());
        }
    }
}
