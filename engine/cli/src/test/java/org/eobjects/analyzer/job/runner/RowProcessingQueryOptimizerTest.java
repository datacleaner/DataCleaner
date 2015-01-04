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
package org.eobjects.analyzer.job.runner;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.filter.MaxRowsFilter;
import org.eobjects.analyzer.beans.filter.NullCheckFilter;
import org.eobjects.analyzer.beans.filter.NullCheckFilter.NullCheckCategory;
import org.eobjects.analyzer.beans.standardize.EmailStandardizerTransformer;
import org.eobjects.analyzer.beans.stringpattern.PatternFinderAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.analyzer.test.TestHelper;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;

public class RowProcessingQueryOptimizerTest extends TestCase {

    private final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(null, null, true);

    private Datastore datastore;
    private AnalyzerBeansConfiguration conf;
    private AnalysisJobBuilder ajb;
    private FilterJobBuilder<MaxRowsFilter, MaxRowsFilter.Category> maxRowsBuilder;
    private AnalyzerJobBuilder<StringAnalyzer> stringAnalyzerBuilder;
    private DatastoreConnection con;
    private Column lastnameColumn;
    private InputColumn<?> lastNameInputColumn;
    private ArrayList<RowProcessingConsumer> consumers;
    private Query baseQuery;
    private SourceColumnFinder sourceColumnFinder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // set up a common fixture with a simple Max rows filter and a String
        // analyzer on the LASTNAME
        // column
        datastore = TestHelper.createSampleDatabaseDatastore("mydb");
        conf = new AnalyzerBeansConfigurationImpl().replace(new DatastoreCatalogImpl(datastore));
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
        consumers.add(createConsumer(maxRowsBuilder));
        consumers.add(createConsumer(stringAnalyzerBuilder));

        baseQuery = con.getDataContext().query().from("EMPLOYEES").select("LASTNAME").toQuery();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        con.close();
    }

    public void testSimpleOptimization() throws Exception {
        RowProcessingQueryOptimizer optimizer = new RowProcessingQueryOptimizer(datastore, consumers, baseQuery);

        assertTrue(optimizer.isOptimizable());

        Query optimizedQuery = optimizer.getOptimizedQuery();
        Integer maxRows = optimizedQuery.getMaxRows();
        assertNotNull("No max rows specified!", maxRows);
        assertEquals(1000, maxRows.intValue());
    }

    public void testAlwaysOptimizableFilter() throws Exception {
        Datastore datastore = new CsvDatastore("foo", "src/test/resources/projects.csv");
        RowProcessingQueryOptimizer optimizer = new RowProcessingQueryOptimizer(datastore, consumers, baseQuery);

        assertTrue(optimizer.isOptimizable());

        FilterJobBuilder<?, ?> fjb = ajb.addFilter(NullCheckFilter.class).addInputColumn(lastNameInputColumn);
        maxRowsBuilder.setRequirement(fjb, NullCheckCategory.NOT_NULL);
        consumers.add(0, createConsumer(fjb));

        optimizer = new RowProcessingQueryOptimizer(datastore, consumers, baseQuery);
        assertFalse(optimizer.isOptimizable());
    }

    public void testOptimizedChainedTransformer() throws Exception {
        TransformerJobBuilder<EmailStandardizerTransformer> emailStdBuilder = ajb
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
        consumers.remove(1);
        consumers.add(createConsumer(emailStdBuilder));
        consumers.add(createConsumer(stringAnalyzerBuilder));

        RowProcessingQueryOptimizer optimizer = new RowProcessingQueryOptimizer(datastore, consumers, baseQuery);

        // not optimizable because the transformer doesn't have the requirement
        assertFalse(optimizer.isOptimizable());

        consumers.remove(2);
        consumers.remove(1);
        emailStdBuilder.setRequirement(maxRowsBuilder, MaxRowsFilter.Category.VALID);
        consumers.add(createConsumer(emailStdBuilder));
        consumers.add(createConsumer(stringAnalyzerBuilder));

        optimizer = new RowProcessingQueryOptimizer(datastore, consumers, baseQuery);
        assertTrue(optimizer.isOptimizable());

        // even without the requirement, the string analyzer should still be
        // optimizable, because of it's dependency to the email standardizer
        stringAnalyzerBuilder.setRequirement(null);
        consumers.remove(2);
        consumers.add(createConsumer(stringAnalyzerBuilder));

        optimizer = new RowProcessingQueryOptimizer(datastore, consumers, baseQuery);
        assertTrue(optimizer.isOptimizable());
    }

    public void testDontOptimizeWhenComponentsHaveNoRequirements() throws Exception {
        AnalyzerJobBuilder<PatternFinderAnalyzer> patternFinderBuilder = ajb.addAnalyzer(PatternFinderAnalyzer.class);
        patternFinderBuilder.addInputColumn(lastNameInputColumn);
        consumers.add(createConsumer(patternFinderBuilder));

        RowProcessingQueryOptimizer optimizer = new RowProcessingQueryOptimizer(datastore, consumers, baseQuery);
        assertFalse(optimizer.isOptimizable());
    }

    public void testMultipleOptimizations() throws Exception {
        FilterJobBuilder<NullCheckFilter, NullCheckFilter.NullCheckCategory> notNullBuilder = ajb
                .addFilter(NullCheckFilter.class);
        Column emailColumn = con.getSchemaNavigator().convertToColumn("EMPLOYEES.EMAIL");
        ajb.addSourceColumn(emailColumn);
        InputColumn<?> emailInputColumn = ajb.getSourceColumnByName("email");
        notNullBuilder.addInputColumn(emailInputColumn);
        notNullBuilder.setRequirement(maxRowsBuilder, MaxRowsFilter.Category.VALID);
        stringAnalyzerBuilder.setRequirement(notNullBuilder, NullCheckCategory.NOT_NULL);

        consumers.remove(1);
        consumers.add(createConsumer(notNullBuilder));
        consumers.add(createConsumer(stringAnalyzerBuilder));

        RowProcessingQueryOptimizer optimizer = new RowProcessingQueryOptimizer(datastore, consumers, baseQuery);
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
        AnalyzerJobBuilder<PatternFinderAnalyzer> patternFinderBuilder = ajb.addAnalyzer(PatternFinderAnalyzer.class);
        patternFinderBuilder.addInputColumn(lastNameInputColumn);
        patternFinderBuilder.setRequirement(maxRowsBuilder, MaxRowsFilter.Category.INVALID);
        consumers.add(createConsumer(patternFinderBuilder));

        RowProcessingQueryOptimizer optimizer = new RowProcessingQueryOptimizer(datastore, consumers, baseQuery);
        assertFalse(optimizer.isOptimizable());
    }

    private FilterConsumer createConsumer(FilterJobBuilder<?, ?> filterJobBuilder) {
        FilterJob filterJob = filterJobBuilder.toFilterJob();
        FilterBeanDescriptor<?, ?> descriptor = filterJob.getDescriptor();
        Filter<?> filter = descriptor.newInstance();

        lifeCycleHelper.assignConfiguredProperties(descriptor, filter, filterJob.getConfiguration());

        FilterConsumer consumer = new FilterConsumer(filter, filterJob, filterJobBuilder.getInput(), sourceColumnFinder);
        return consumer;
    }

    private TransformerConsumer createConsumer(TransformerJobBuilder<?> transformerJobBuilder) {
        TransformerJob transformerJob = transformerJobBuilder.toTransformerJob();
        TransformerBeanDescriptor<?> descriptor = transformerJob.getDescriptor();
        Transformer<?> transformer = descriptor.newInstance();

        lifeCycleHelper.assignConfiguredProperties(descriptor, transformer, transformerJob.getConfiguration());

        TransformerConsumer consumer = new TransformerConsumer(transformer, transformerJob,
                transformerJobBuilder.getInput(), sourceColumnFinder);
        return consumer;
    }

    private AnalyzerConsumer createConsumer(AnalyzerJobBuilder<?> analyzerBuilder) {
        AnalyzerJob analyzerJob = analyzerBuilder.toAnalyzerJob();
        AnalyzerBeanDescriptor<?> descriptor = analyzerJob.getDescriptor();
        Analyzer<?> analyzer = descriptor.newInstance();

        lifeCycleHelper.assignConfiguredProperties(descriptor, analyzer, analyzerJob.getConfiguration());

        AnalyzerConsumer consumer = new AnalyzerConsumer(analyzer, analyzerJob, analyzerBuilder.getInput(),
                sourceColumnFinder);
        return consumer;
    }
}
