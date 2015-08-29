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
import java.util.List;

import junit.framework.TestCase;

import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Filter;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Transformer;
import org.datacleaner.beans.StringAnalyzer;
import org.datacleaner.beans.filter.NullCheckFilter;
import org.datacleaner.beans.filter.NullCheckFilter.NullCheckCategory;
import org.datacleaner.beans.standardize.EmailStandardizerTransformer;
import org.datacleaner.beans.stringpattern.PatternFinderAnalyzer;
import org.datacleaner.components.maxrows.MaxRowsFilter;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.descriptors.FilterDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.FilterJob;
import org.datacleaner.job.TransformerJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.concurrent.SingleThreadedTaskRunner;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.test.TestHelper;
import org.datacleaner.util.SourceColumnFinder;

public class RowProcessingQueryOptimizerTest extends TestCase {

    private final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(null, true);

    private Datastore datastore;
    private DataCleanerConfiguration conf;
    private AnalysisJobBuilder ajb;
    private FilterComponentBuilder<MaxRowsFilter, MaxRowsFilter.Category> maxRowsBuilder;
    private AnalyzerComponentBuilder<StringAnalyzer> stringAnalyzerBuilder;
    private DatastoreConnection con;
    private Column lastnameColumn;
    private InputColumn<?> lastNameInputColumn;
    private ArrayList<RowProcessingConsumer> consumers;
    private Query baseQuery;
    private SourceColumnFinder sourceColumnFinder;
    private RowProcessingPublisher publisher;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // set up a common fixture with a simple Max rows filter and a String
        // analyzer on the LASTNAME
        // column
        datastore = TestHelper.createSampleDatabaseDatastore("mydb");
        conf = new DataCleanerConfigurationImpl().withDatastores(datastore);
        ajb = new AnalysisJobBuilder(conf);
        ajb.setDatastore(datastore);
        maxRowsBuilder = ajb.addFilter(MaxRowsFilter.class);
        stringAnalyzerBuilder = ajb.addAnalyzer(StringAnalyzer.class);
        stringAnalyzerBuilder.setRequirement(maxRowsBuilder, MaxRowsFilter.Category.VALID);
        con = conf.getDatastoreCatalog().getDatastore("mydb").openConnection();
        lastnameColumn = con.getSchemaNavigator().convertToColumn("EMPLOYEES.LASTNAME");
        ajb.addSourceColumn(lastnameColumn);
        lastNameInputColumn = ajb.getSourceColumnByName("lastname");
        stringAnalyzerBuilder.addInputColumn(lastNameInputColumn);

        sourceColumnFinder = new SourceColumnFinder();

        consumers = new ArrayList<RowProcessingConsumer>();

        baseQuery = con.getDataContext().query().from("EMPLOYEES").select("LASTNAME").toQuery();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        con.close();
    }

    private RowProcessingPublisher createPublisher() {
        final AnalysisJob analysisJob = ajb.toAnalysisJob(false);
        final AnalysisListener analysisListener = new InfoLoggingAnalysisListener();
        final TaskRunner taskRunner = new SingleThreadedTaskRunner();
        final RowProcessingPublishers publishers = new RowProcessingPublishers(analysisJob, analysisListener, taskRunner,
                lifeCycleHelper, sourceColumnFinder);
        final Table table = ajb.getSourceColumns().get(0).getPhysicalColumn().getTable();
        return publishers.getRowProcessingPublisher(table);
    }

    public void testSimpleOptimization() throws Exception {
        publisher = createPublisher();
        consumers.add(createConsumer(maxRowsBuilder, publisher));
        consumers.add(createConsumer(stringAnalyzerBuilder, publisher));

        RowProcessingQueryOptimizer optimizer = new RowProcessingQueryOptimizerImpl(datastore, consumers, baseQuery);

        assertTrue(optimizer.isOptimizable());

        Query optimizedQuery = optimizer.getOptimizedQuery();
        Integer maxRows = optimizedQuery.getMaxRows();
        assertNotNull("No max rows specified!", maxRows);
        assertEquals(1000, maxRows.intValue());
    }

    public void testAlwaysOptimizableFilter() throws Exception {
        Datastore datastore = new CsvDatastore("foo", "src/test/resources/projects.csv");

        publisher = createPublisher();
        consumers.add(createConsumer(maxRowsBuilder, publisher));
        consumers.add(createConsumer(stringAnalyzerBuilder, publisher));
        RowProcessingQueryOptimizer optimizer = new RowProcessingQueryOptimizerImpl(datastore, consumers, baseQuery);

        assertTrue(optimizer.isOptimizable());

        FilterComponentBuilder<?, ?> fjb = ajb.addFilter(NullCheckFilter.class).addInputColumn(lastNameInputColumn);
        maxRowsBuilder.setRequirement(fjb, NullCheckCategory.NOT_NULL);
        publisher = createPublisher();
        consumers.add(0, createConsumer(fjb, publisher));

        optimizer = new RowProcessingQueryOptimizerImpl(datastore, consumers, baseQuery);
        assertFalse(optimizer.isOptimizable());
    }

    public void testOptimizedChainedTransformer() throws Exception {
        TransformerComponentBuilder<EmailStandardizerTransformer> emailStdBuilder = ajb
                .addTransformer(EmailStandardizerTransformer.class);
        Column emailColumn = con.getSchemaNavigator().convertToColumn("EMPLOYEES.EMAIL");
        ajb.addSourceColumn(emailColumn);
        InputColumn<?> emailInputColumn = ajb.getSourceColumnByName("email");
        emailStdBuilder.addInputColumn(emailInputColumn);

        // reconfigure the string analyzer to depend on transformed columns
        stringAnalyzerBuilder.clearInputColumns();
        List<MutableInputColumn<?>> outputColumns = emailStdBuilder.getOutputColumns();
        stringAnalyzerBuilder.addInputColumns(outputColumns);

        // remove the string analyzer and add the transformer in between
        publisher = createPublisher();
        consumers.add(createConsumer(maxRowsBuilder, publisher));
        consumers.add(createConsumer(emailStdBuilder, publisher));
        consumers.add(createConsumer(stringAnalyzerBuilder, publisher));

        RowProcessingQueryOptimizer optimizer = new RowProcessingQueryOptimizerImpl(datastore, consumers, baseQuery);

        // not optimizable because the transformer doesn't have the requirement
        assertFalse(optimizer.isOptimizable());

        consumers.remove(2);
        consumers.remove(1);
        emailStdBuilder.setRequirement(maxRowsBuilder, MaxRowsFilter.Category.VALID);
        publisher = createPublisher();
        consumers.add(createConsumer(emailStdBuilder, publisher));
        consumers.add(createConsumer(stringAnalyzerBuilder, publisher));

        optimizer = new RowProcessingQueryOptimizerImpl(datastore, consumers, baseQuery);
        assertTrue(optimizer.isOptimizable());

        // even without the requirement, the string analyzer should still be
        // optimizable, because of it's dependency to the email standardizer
        stringAnalyzerBuilder.setRequirement(null);
        consumers.remove(2);
        publisher = createPublisher();
        consumers.add(createConsumer(stringAnalyzerBuilder, publisher));

        optimizer = new RowProcessingQueryOptimizerImpl(datastore, consumers, baseQuery);
        assertTrue(optimizer.isOptimizable());
    }

    public void testDontOptimizeWhenComponentsHaveNoRequirements() throws Exception {
        AnalyzerComponentBuilder<PatternFinderAnalyzer> patternFinderBuilder = ajb
                .addAnalyzer(PatternFinderAnalyzer.class);
        patternFinderBuilder.addInputColumn(lastNameInputColumn);
        publisher = createPublisher();
        consumers.add(createConsumer(maxRowsBuilder, publisher));
        consumers.add(createConsumer(stringAnalyzerBuilder, publisher));
        consumers.add(createConsumer(patternFinderBuilder, publisher));

        RowProcessingQueryOptimizer optimizer = new RowProcessingQueryOptimizerImpl(datastore, consumers, baseQuery);
        assertFalse(optimizer.isOptimizable());
    }

    public void testMultipleOptimizations() throws Exception {
        FilterComponentBuilder<NullCheckFilter, NullCheckFilter.NullCheckCategory> notNullBuilder = ajb
                .addFilter(NullCheckFilter.class);
        Column emailColumn = con.getSchemaNavigator().convertToColumn("EMPLOYEES.EMAIL");
        ajb.addSourceColumn(emailColumn);
        InputColumn<?> emailInputColumn = ajb.getSourceColumnByName("email");
        notNullBuilder.addInputColumn(emailInputColumn);
        notNullBuilder.setRequirement(maxRowsBuilder, MaxRowsFilter.Category.VALID);
        stringAnalyzerBuilder.setRequirement(notNullBuilder, NullCheckCategory.NOT_NULL);

        publisher = createPublisher();
        consumers.add(createConsumer(maxRowsBuilder, publisher));
        consumers.add(createConsumer(notNullBuilder, publisher));
        consumers.add(createConsumer(stringAnalyzerBuilder, publisher));

        RowProcessingQueryOptimizer optimizer = new RowProcessingQueryOptimizerImpl(datastore, consumers, baseQuery);
        assertTrue(optimizer.isOptimizable());

        List<RowProcessingConsumer> optimizedConsumers = optimizer.getOptimizedConsumers();
        assertEquals(1, optimizedConsumers.size());

        Query q = optimizer.getOptimizedQuery();
        assertEquals(
                "SELECT \"EMPLOYEES\".\"LASTNAME\" FROM PUBLIC.\"EMPLOYEES\" WHERE \"EMPLOYEES\".\"EMAIL\" IS NOT NULL",
                q.toSql());
        assertEquals(1000, q.getMaxRows().intValue());
    }

    public void testMultipleOutcomesUsed() throws Exception {
        AnalyzerComponentBuilder<PatternFinderAnalyzer> patternFinderBuilder = ajb
                .addAnalyzer(PatternFinderAnalyzer.class);
        patternFinderBuilder.addInputColumn(lastNameInputColumn);
        patternFinderBuilder.setRequirement(maxRowsBuilder, MaxRowsFilter.Category.INVALID);
        publisher = createPublisher();
        consumers.add(createConsumer(maxRowsBuilder, publisher));
        consumers.add(createConsumer(stringAnalyzerBuilder, publisher));
        consumers.add(createConsumer(patternFinderBuilder, publisher));

        RowProcessingQueryOptimizer optimizer = new RowProcessingQueryOptimizerImpl(datastore, consumers, baseQuery);
        assertFalse(optimizer.isOptimizable());
    }

    private FilterConsumer createConsumer(FilterComponentBuilder<?, ?> filterJobBuilder,
            RowProcessingPublisher publisher) {
        FilterJob filterJob = filterJobBuilder.toFilterJob();
        FilterDescriptor<?, ?> descriptor = filterJob.getDescriptor();
        Filter<?> filter = descriptor.newInstance();

        lifeCycleHelper.assignConfiguredProperties(descriptor, filter, filterJob.getConfiguration());

        FilterConsumer consumer = new FilterConsumer(filter, filterJob, filterJobBuilder.getInput(), publisher);
        return consumer;
    }

    private TransformerConsumer createConsumer(TransformerComponentBuilder<?> transformerJobBuilder,
            RowProcessingPublisher publisher) {
        TransformerJob transformerJob = transformerJobBuilder.toTransformerJob();
        TransformerDescriptor<?> descriptor = transformerJob.getDescriptor();
        Transformer transformer = descriptor.newInstance();

        lifeCycleHelper.assignConfiguredProperties(descriptor, transformer, transformerJob.getConfiguration());

        TransformerConsumer consumer = new TransformerConsumer(transformer, transformerJob,
                transformerJobBuilder.getInput(), publisher);
        return consumer;
    }

    private AnalyzerConsumer createConsumer(AnalyzerComponentBuilder<?> analyzerBuilder,
            RowProcessingPublisher publisher) {
        AnalyzerJob analyzerJob = analyzerBuilder.toAnalyzerJob();
        AnalyzerDescriptor<?> descriptor = analyzerJob.getDescriptor();
        Analyzer<?> analyzer = descriptor.newInstance();

        lifeCycleHelper.assignConfiguredProperties(descriptor, analyzer, analyzerJob.getConfiguration());

        AnalyzerConsumer consumer = new AnalyzerConsumer(analyzer, analyzerJob, analyzerBuilder.getInput(), publisher);
        return consumer;
    }
}
