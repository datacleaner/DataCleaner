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
package org.datacleaner.job.runner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import junit.framework.TestCase;

import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableTable;
import org.apache.metamodel.schema.Table;
import org.datacleaner.beans.StringAnalyzer;
import org.datacleaner.components.convert.ConvertToStringTransformer;
import org.datacleaner.components.fuse.CoalesceMultipleFieldsTransformer;
import org.datacleaner.components.fuse.CoalesceUnit;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.FilterJob;
import org.datacleaner.job.TransformerJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;

public class RowProcessingConsumerSorterTest extends TestCase {

    private MutableColumn physicalColumn;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        physicalColumn = new MutableColumn("foo", ColumnType.VARCHAR);
        physicalColumn.setTable(new MutableTable("bar").addColumn(physicalColumn));
    }

    public void testCreateProcessOrderedConsumerListNoConsumers() throws Exception {
        List<RowProcessingConsumer> consumerList = new RowProcessingConsumerSorter(
                new ArrayList<RowProcessingConsumer>()).createProcessOrderedConsumerList();
        assertTrue(consumerList.isEmpty());
    }

    public void testCreateProcessOrderedConsumerListWithMergedOutcomes() throws Exception {
        AnalysisJobBuilder ajb = new AnalysisJobBuilder(new DataCleanerConfigurationImpl());
        ajb.setDatastoreConnection(new MockDatastoreConnection());
        ajb.addSourceColumn(physicalColumn);
        MetaModelInputColumn inputColumn = ajb.getSourceColumns().get(0);

        // 1: add a filter
        FilterComponentBuilder<MockFilter, MockFilter.Category> fjb1 = ajb.addFilter(MockFilter.class);
        fjb1.addInputColumn(inputColumn);
        fjb1.setName("fjb1");

        // 2: trim (depends on filter)
        TransformerComponentBuilder<TransformerMock> tjb1 = ajb.addTransformer(TransformerMock.class);
        tjb1.addInputColumn(inputColumn);
        tjb1.setRequirement(fjb1, MockFilter.Category.VALID);
        tjb1.setName("tjb1");

        // 3: merge either the null or the trimmed value
        TransformerComponentBuilder<CoalesceMultipleFieldsTransformer> coalesce = ajb
                .addTransformer(CoalesceMultipleFieldsTransformer.class);
        CoalesceUnit unit1 = new CoalesceUnit(tjb1.getOutputColumns().get(0));
        CoalesceUnit unit2 = new CoalesceUnit(inputColumn);
        coalesce.getComponentInstance().configureUsingCoalesceUnits(unit1, unit2);

        MutableInputColumn<?> mergedColumn1 = coalesce.getOutputColumns().get(0);

        // 4: add another filter (depends on merged output)
        FilterComponentBuilder<MockFilter, MockFilter.Category> fjb2 = ajb.addFilter(MockFilter.class);
        fjb2.addInputColumn(mergedColumn1);
        fjb2.setName("fjb2");

        // 5: add an analyzer
        ajb.addAnalyzer(StringAnalyzer.class).addInputColumn(mergedColumn1)
                .setRequirement(fjb2, MockFilter.Category.VALID);

        assertTrue(ajb.isConfigured());

        List<RowProcessingConsumer> consumers = getConsumers(ajb.toAnalysisJob());

        consumers = new RowProcessingConsumerSorter(consumers).createProcessOrderedConsumerList();

        assertEquals(5, consumers.size());

        assertEquals("ImmutableFilterJob[name=fjb1,filter=Mock filter]", consumers.get(0).getComponentJob().toString());
        assertEquals("ImmutableTransformerJob[name=tjb1,transformer=Transformer mock]", consumers.get(1)
                .getComponentJob().toString());
        assertEquals("ImmutableTransformerJob[name=null,transformer=Fuse / Coalesce fields]", consumers.get(2)
                .getComponentJob().toString());
        assertEquals("ImmutableFilterJob[name=fjb2,filter=Mock filter]", consumers.get(3).getComponentJob().toString());
        assertEquals("ImmutableAnalyzerJob[name=null,analyzer=String analyzer]", consumers.get(4).getComponentJob()
                .toString());

        ajb.close();
    }

    public void testCreateProcessOrderedConsumerListWithFilterDependencies() throws Exception {
        AnalysisJobBuilder ajb = new AnalysisJobBuilder(new DataCleanerConfigurationImpl());
        ajb.setDatastoreConnection(new MockDatastoreConnection());
        ajb.addSourceColumn(physicalColumn);
        MetaModelInputColumn inputColumn = ajb.getSourceColumns().get(0);

        // 1: add a filter
        FilterComponentBuilder<MockFilter, MockFilter.Category> fjb1 = ajb.addFilter(MockFilter.class);
        fjb1.addInputColumn(inputColumn);
        fjb1.setName("fjb1");

        // 2: trim (depends on filter)
        TransformerComponentBuilder<TransformerMock> tjb1 = ajb.addTransformer(TransformerMock.class);
        tjb1.addInputColumn(inputColumn);
        tjb1.setRequirement(fjb1, MockFilter.Category.VALID);
        tjb1.setName("tjb1");

        // 3: trim again, just to examplify (depends on first trim output)
        TransformerComponentBuilder<TransformerMock> tjb2 = ajb.addTransformer(TransformerMock.class);
        tjb2.addInputColumn(tjb1.getOutputColumns().get(0));
        tjb2.setName("tjb2");

        // 4: add a single word filter (depends on second trim)
        FilterComponentBuilder<MockFilter, MockFilter.Category> fjb2 = ajb.addFilter(MockFilter.class);
        fjb2.addInputColumn(tjb2.getOutputColumns().get(0));
        fjb2.setName("fjb2");

        // 5 and 6: Analyze VALID and INVALID output of single-word filter
        // separately (the order of these two are not deterministic because of
        // the shuffle)
        ajb.addAnalyzer(StringAnalyzer.class).addInputColumn(inputColumn)
                .setRequirement(fjb2, MockFilter.Category.VALID);
        ajb.addAnalyzer(StringAnalyzer.class).addInputColumn(inputColumn)
                .setRequirement(fjb2, MockFilter.Category.INVALID);

        assertTrue(ajb.isConfigured());

        List<RowProcessingConsumer> consumers = getConsumers(ajb.toAnalysisJob());

        assertEquals(6, consumers.size());

        consumers = new RowProcessingConsumerSorter(consumers).createProcessOrderedConsumerList();

        assertEquals("ImmutableFilterJob[name=fjb1,filter=Mock filter]", consumers.get(0).getComponentJob().toString());
        assertEquals("ImmutableTransformerJob[name=tjb1,transformer=Transformer mock]", consumers.get(1)
                .getComponentJob().toString());
        assertEquals("ImmutableTransformerJob[name=tjb2,transformer=Transformer mock]", consumers.get(2)
                .getComponentJob().toString());
        assertEquals("ImmutableFilterJob[name=fjb2,filter=Mock filter]", consumers.get(3).getComponentJob().toString());

        ajb.close();
    }

    public void testCreateProcessOrderedConsumerListChainedTransformers() throws Exception {
        AnalysisJobBuilder ajb = new AnalysisJobBuilder(new DataCleanerConfigurationImpl());
        ajb.addSourceColumn(physicalColumn);

        TransformerComponentBuilder<TransformerMock> tjb1 = ajb.addTransformer(TransformerMock.class).addInputColumn(
                ajb.getSourceColumns().get(0));
        TransformerComponentBuilder<TransformerMock> tjb2 = ajb.addTransformer(TransformerMock.class).addInputColumn(
                tjb1.getOutputColumns().get(0));
        TransformerComponentBuilder<ConvertToStringTransformer> tjb3 = ajb.addTransformer(
                ConvertToStringTransformer.class).addInputColumn(tjb2.getOutputColumns().get(0));

        ajb.addAnalyzer(StringAnalyzer.class).addInputColumn(ajb.getSourceColumns().get(0));
        ajb.addAnalyzer(StringAnalyzer.class).addInputColumn(tjb3.getOutputColumns().get(0));

        ajb.setDatastoreConnection(new MockDatastoreConnection());

        assertTrue(ajb.isConfigured());
        AnalysisJob analysisJob = ajb.toAnalysisJob();

        List<RowProcessingConsumer> consumers = getConsumers(analysisJob);

        consumers = new RowProcessingConsumerSorter(consumers).createProcessOrderedConsumerList();

        assertEquals(5, consumers.size());

        List<TransformerJob> transformerJobs = new ArrayList<TransformerJob>(analysisJob.getTransformerJobs());
        List<AnalyzerJob> analyzerJobs = new ArrayList<AnalyzerJob>(analysisJob.getAnalyzerJobs());

        // create a list that represents the expected dependent sequence
        Queue<ComponentJob> jobDependencies = new LinkedList<ComponentJob>();
        jobDependencies.add(transformerJobs.get(0));
        jobDependencies.add(transformerJobs.get(1));
        jobDependencies.add(transformerJobs.get(2));
        jobDependencies.add(analyzerJobs.get(1));

        int jobDependenciesFound = 0;
        boolean analyzerJob1found = false;

        ComponentJob nextJobDependency = jobDependencies.poll();
        for (RowProcessingConsumer rowProcessingConsumer : consumers) {
            ComponentJob job = rowProcessingConsumer.getComponentJob();
            if (job == nextJobDependency) {
                nextJobDependency = jobDependencies.poll();
                jobDependenciesFound++;
            } else if (job == analyzerJobs.get(0)) {
                assertFalse(analyzerJob1found);
                analyzerJob1found = true;
            } else {
                fail("The consumers sort order is wrong! Found: " + job + " but expected: " + nextJobDependency);
            }
        }

        assertTrue(analyzerJob1found);
        assertEquals(4, jobDependenciesFound);

        ajb.close();
    }

    private List<RowProcessingConsumer> getConsumers(AnalysisJob analysisJob) {
        List<RowProcessingConsumer> consumers = new ArrayList<RowProcessingConsumer>();
        RowProcessingPublishers publishers = new RowProcessingPublishers(analysisJob, null, null, null);
        Table table = analysisJob.getSourceColumns().get(0)
                .getPhysicalColumn().getTable();
        RowProcessingPublisher publisher = publishers.getRowProcessingPublisher(publishers.getStream(table));

        for (AnalyzerJob analyzerJob : analysisJob.getAnalyzerJobs()) {
            RowProcessingConsumer consumer = new AnalyzerConsumer(analyzerJob.getDescriptor().newInstance(),
                    analyzerJob, analyzerJob.getInput(), publisher);
            consumers.add(consumer);
        }
        for (TransformerJob transformerJob : analysisJob.getTransformerJobs()) {
            RowProcessingConsumer consumer = new TransformerConsumer(transformerJob.getDescriptor().newInstance(),
                    transformerJob, transformerJob.getInput(), publisher);
            consumers.add(consumer);
        }
        for (FilterJob filterJob : analysisJob.getFilterJobs()) {
            FilterConsumer consumer = new FilterConsumer(filterJob.getDescriptor().newInstance(), filterJob,
                    filterJob.getInput(), publisher);
            consumers.add(consumer);
        }

        // shuffle the list (it should work regardless of the initial sort
        // order)
        Collections.shuffle(consumers);

        return consumers;
    }
}
