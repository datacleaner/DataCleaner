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
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
import org.datacleaner.util.SystemProperties;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.junit.Assert.*;

public class SparkAnalysisRunnerTest {
    private static class TestSparkJobLifeCycleListener implements SparkJobLifeCycleListener {
        private static final long serialVersionUID = 1L;
        final AtomicBoolean _jobStartCalled = new AtomicBoolean();
        final AtomicBoolean _jobEndCalled = new AtomicBoolean();

        @Override
        public void onPartitionProcessingStart(SparkJobContext sparkJobContext) {
            // Unfortunately, serialization only goes one way, so we can't assert on this.
            System.out.println("Node start");
        }

        @Override
        public void onPartitionProcessingEnd(SparkJobContext sparkJobContext) {
            // Unfortunately, serialization only goes one way, so we can't assert on this.
            System.out.println("Node end");
        }

        @Override
        public void onJobStart(SparkJobContext sparkJobContext) {
            _jobStartCalled.set(true);
        }

        @Override
        public void onJobEnd(SparkJobContext sparkJobContext) {
            _jobEndCalled.set(true);
        }
    }

    private static final int MIN_PARTITIONS_MULTIPLE = 4;

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void init() {
        // use local filesystem as default during tests
        System.setProperty(SystemProperties.DEFAULT_RESOURCE_SCHEME, "file");
    }

    @Test
    public void testVanillaScenario() throws Exception {
        final AnalysisResultFuture result = runAnalysisJob("DCTest - " + getName(), URI.create(
                "src/test/resources/vanilla-job.analysis.xml"), "vanilla-job", false);

        if (result.isErrornous()) {
            throw (Exception) result.getErrors().get(0);
        }

        assertEquals(2, result.getResultMap().size());
        final List<AnalyzerResult> results = result.getResults();
        assertEquals(2, results.size());

        final StringAnalyzerResult stringAnalyzerResult = result.getResults(StringAnalyzerResult.class).get(0);
        assertEquals("[MetaModelInputColumn[resources.person_names.txt.company]]", Arrays.toString(stringAnalyzerResult
                .getColumns()));

        final int rowCount = stringAnalyzerResult.getRowCount(stringAnalyzerResult.getColumns()[0]);
        assertEquals(7, rowCount);

        final int upperCaseChars = stringAnalyzerResult.getEntirelyUpperCaseCount(stringAnalyzerResult.getColumns()[0]);
        assertEquals(7, upperCaseChars);
    }

    @Test
    public void testEscalatedValueDistributionScenario() throws Exception {
        final AnalysisResultFuture result = runAnalysisJob("DCTest - " + getName(), URI.create(
                "src/test/resources/escalated-job.analysis.xml"), "escalated-job", false);

        if (result.isErrornous()) {
            throw (Exception) result.getErrors().get(0);
        }

        assertEquals(3, result.getResultMap().size());
        final List<AnalyzerResult> results = result.getResults();
        assertEquals(3, results.size());

        final List<? extends ValueDistributionAnalyzerResult> valueDistributionAnalyzerResults =
                result.getResults(ValueDistributionAnalyzerResult.class);
        assertEquals(2,valueDistributionAnalyzerResults.size());

        final ValueDistributionAnalyzerResult
                vdAnalyzerResult = valueDistributionAnalyzerResults.get(0);
        assertEquals(7, vdAnalyzerResult
                .getTotalCount());
    }


    @Test
    public void testWriteDataScenarioNoResult() throws Exception {
        final AnalysisResultFuture result = runWriteDataScenario(false);

        final List<AnalyzerResult> results = result.getResults();
        assertEquals(0, results.size());
    }

    @Test
    public void testWriteDataScenarioSaveResult() throws Exception {
        final AnalysisResultFuture result = runWriteDataScenario(true);

        final List<AnalyzerResult> results = result.getResults();
        assertEquals(1, results.size());

        final WriteDataResult writeDataResult = result.getResults(WriteDataResult.class).get(0);
        assertEquals(7, writeDataResult.getWrittenRowCount());
    }

    private AnalysisResultFuture runWriteDataScenario(boolean saveResult) throws Exception {
        final String outputPath = "target/write-job.csv";
        final File outputFile = new File(outputPath);
        if (outputFile.exists() && outputFile.isDirectory()) {
            FileUtils.deleteDirectory(outputFile);
        }

        final AnalysisResultFuture result;

        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("DCTest - " + getName());
        try (JavaSparkContext sparkContext = new JavaSparkContext(sparkConf)) {

            final SparkJobContext sparkJobContext;
            if (saveResult) {
                sparkJobContext = new SparkJobContext(URI.create("src/test/resources/conf_local.xml"), URI.create(
                        "src/test/resources/write-job.analysis.xml"), null, sparkContext);
            } else {
                sparkJobContext = new SparkJobContext(URI.create("src/test/resources/conf_local.xml"), URI.create(
                        "src/test/resources/write-job.analysis.xml"), URI.create(
                                "src/test/resources/jobProperties/noResult.properties"), sparkContext);
            }
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertEquals("write-job", sparkJobContext.getJobName());

            final SparkAnalysisRunner sparkAnalysisRunner = new SparkAnalysisRunner(sparkContext, sparkJobContext,
                    MIN_PARTITIONS_MULTIPLE);

            result = sparkAnalysisRunner.run(job);
        }

        if (result.isErrornous()) {
            throw (Exception) result.getErrors().get(0);
        }

        assertTrue(outputFile.isDirectory());

        // file resource is capable of viewing the directory like it is a single
        // file
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

        return result;
    }

    @Test
    public void testOutputDataStreamsScenario() throws Exception {
        final AnalysisResultFuture result = runAnalysisJob("DCTest - testOutputDataStreamsScenario", URI.create(
                "src/test/resources/melon-job.analysis.xml"), "melon-job", false);

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

        result = runAnalysisJob("DCTest - testOutputDataStreamsNonDistributableScenario", URI.create(
                "src/test/resources/non-dist-melon-job.analysis.xml"), "non-dist-melon-job", true);

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
        final AnalysisResultFuture result = runAnalysisJob("DCTest - testValueDistributionReducer", URI.create(
                "src/test/resources/distributable-value-dist.analysis.xml"), "distributable-value-dist", true);

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
        final AnalysisResultFuture result = runAnalysisJob("DCTest - testGroupedValueDistributionReducer", URI.create(
                "src/test/resources/distributable-grouped-value-dist.analysis.xml"), "distributable-grouped-value-dist",
                true);

        if (result.isErrornous()) {
            throw (Exception) result.getErrors().get(0);
        }

        final List<AnalyzerResult> results = result.getResults();
        assertEquals(1, results.size());

        final ValueDistributionAnalyzerResult completeValueDistributionAnalyzerResult = result.getResults(
                ValueDistributionAnalyzerResult.class).get(0);
        assertEquals(GroupedValueDistributionResult.class, completeValueDistributionAnalyzerResult.getClass());
        final GroupedValueDistributionResult completeGroupedResult = (GroupedValueDistributionResult) completeValueDistributionAnalyzerResult;
        final Iterator<? extends ValueCountingAnalyzerResult> iterator = completeGroupedResult.getGroupResults().iterator();
        final ReducedSingleValueDistributionResult group1 = (ReducedSingleValueDistributionResult) iterator.next();
        final ReducedSingleValueDistributionResult group2 = (ReducedSingleValueDistributionResult) iterator.next();

        if (group1.getName().equals("Denmark")) {
            checkGroup(group1, "Denmark", 4, 4, 4, 0);
            checkGroup(group2, "Netherlands", 3, 3, 3, 0);
        } else {
            checkGroup(group2, "Denmark", 4, 4, 4, 0);
            checkGroup(group1, "Netherlands", 3, 3, 3, 0);
        }
    }

    @Test
    public void testJsonDatastore() throws Exception {
        final String appName = "DCTest - " + getName();
        final AnalysisResultFuture result = runAnalysisJob(appName, URI.create(
                "src/test/resources/json-job.analysis.xml"), "json-job", false);

        final List<AnalyzerResult> results = result.getResults();
        assertNotNull(results);
        assertEquals(1, results.size());

        final ValueDistributionAnalyzerResult valueDistributionAnalyzerResult = result.getResults(
                ValueDistributionAnalyzerResult.class).get(0);
        assertEquals("[[blue->3], [green->2], [<unique>->1]]", valueDistributionAnalyzerResult.getValueCounts()
                .toString());

        assertEquals(1, valueDistributionAnalyzerResult.getUniqueCount().intValue());
        assertEquals("[brown]", valueDistributionAnalyzerResult.getUniqueValues().toString());
    }

    @Test
    public void testLifeCycleListener() throws Exception {
        final String appName = "DCTest - " + getName();
        final URI analysisJobXmlPath = URI.create("src/test/resources/json-job.analysis.xml");
        final String expectedAnalysisJobName = "json-job";
        final TestSparkJobLifeCycleListener sparkJobLifeCycleListener = new TestSparkJobLifeCycleListener();

        runAnalysisJob(appName, analysisJobXmlPath, expectedAnalysisJobName, false, sparkJobLifeCycleListener);
        assertTrue(sparkJobLifeCycleListener._jobStartCalled.get());
        assertTrue(sparkJobLifeCycleListener._jobEndCalled.get());
    }

    private void checkGroup(final ReducedSingleValueDistributionResult group, final String groupName,
            final int expectedTotalCount, final int expectedUniqueCount, final int expectedDistinctCount,
            final int expectedNullCount) {
        assertEquals(groupName, group.getName());
        assertEquals(expectedTotalCount, group.getTotalCount());
        assertEquals(Integer.valueOf(expectedUniqueCount), group.getUniqueCount());
        assertEquals(Integer.valueOf(expectedDistinctCount), group.getDistinctCount());
        assertEquals(expectedNullCount, group.getNullCount());
    }

    private String getName() {
        return testName.getMethodName();
    }

    private AnalysisResultFuture runAnalysisJob(final String appName, final URI analysisJobXmlPath,
            final String expectedAnalysisJobName, boolean useMinPartitions) throws Exception {
        return runAnalysisJob(appName, analysisJobXmlPath, expectedAnalysisJobName, useMinPartitions, null);
    }

    private AnalysisResultFuture runAnalysisJob(final String appName, final URI analysisJobXmlPath,
            final String expectedAnalysisJobName, boolean useMinPartitions,
            final SparkJobLifeCycleListener sparkJobLifeCycleListener) throws Exception {
        final AnalysisResultFuture result;
        final SparkConf sparkConf = new SparkConf().setMaster("local").setAppName(appName);
        try (JavaSparkContext sparkContext = new JavaSparkContext(sparkConf)) {
            final SparkJobContext sparkJobContext = new SparkJobContext(URI.create("src/test/resources/conf_local.xml"),
                    analysisJobXmlPath, null, sparkContext);
            if (sparkJobLifeCycleListener != null) {
                sparkJobContext.addSparkJobLifeCycleListener(sparkJobLifeCycleListener);
            }
            final AnalysisJob job = sparkJobContext.getAnalysisJob();
            assertNotNull(job);
            assertEquals(expectedAnalysisJobName, sparkJobContext.getJobName());

            final SparkAnalysisRunner sparkAnalysisRunner = new SparkAnalysisRunner(sparkContext, sparkJobContext,
                    useMinPartitions ? MIN_PARTITIONS_MULTIPLE : null);

            result = sparkAnalysisRunner.run(job);
        }

        if (result.isErrornous()) {
            throw (Exception) result.getErrors().get(0);
        }

        return result;
    }
}
