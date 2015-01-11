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
package org.datacleaner.job.builder;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.NumberAnalyzer;
import org.datacleaner.beans.StringAnalyzer;
import org.datacleaner.beans.convert.ConvertToStringTransformer;
import org.datacleaner.beans.filter.MaxRowsFilter;
import org.datacleaner.beans.standardize.EmailStandardizerTransformer;
import org.datacleaner.beans.stringpattern.PatternFinderAnalyzer;
import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.TransformerBeanDescriptor;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.TransformerJob;
import org.datacleaner.test.TestHelper;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;

public class AnalysisJobBuilderTest extends TestCase {

    private AnalysisJobBuilder analysisJobBuilder;
    private AnalyzerBeansConfigurationImpl configuration;
    private Datastore datastore;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Collection<Datastore> datastores = new LinkedList<Datastore>();

        datastore = TestHelper.createSampleDatabaseDatastore("my db");
        datastores.add(datastore);

        configuration = new AnalyzerBeansConfigurationImpl().replace(new DatastoreCatalogImpl(datastores));

        analysisJobBuilder = new AnalysisJobBuilder(configuration);
        analysisJobBuilder.setDatastore("my db");
    }

    public void testValidate() throws Exception {
        analysisJobBuilder.addSourceColumns("PUBLIC.EMPLOYEES.REPORTSTO");
        MetaModelInputColumn reportsToColumn = analysisJobBuilder.getSourceColumns().get(0);

        FilterJobBuilder<MaxRowsFilter, MaxRowsFilter.Category> filter = analysisJobBuilder
                .addFilter(MaxRowsFilter.class);
        filter.setConfiguredProperty("Max rows", -1);
        analysisJobBuilder.addAnalyzer(NumberAnalyzer.class).addInputColumn(reportsToColumn);

        try {
            analysisJobBuilder.isConfigured(true);
            fail("Exception expected");
        } catch (Exception e) {
            assertEquals("Max rows value must be a positive integer", e.getMessage());
        }
    }

    public void testPreventCyclicFilterDependencies() throws Exception {
        analysisJobBuilder.addSourceColumns("PUBLIC.EMPLOYEES.REPORTSTO");
        FilterJobBuilder<MaxRowsFilter, MaxRowsFilter.Category> filter1 = analysisJobBuilder
                .addFilter(MaxRowsFilter.class);
        FilterJobBuilder<MaxRowsFilter, MaxRowsFilter.Category> filter2 = analysisJobBuilder
                .addFilter(MaxRowsFilter.class);
        filter1.setRequirement(filter2.getFilterOutcome(MaxRowsFilter.Category.INVALID));

        try {
            filter2.setRequirement(filter1.getFilterOutcome(MaxRowsFilter.Category.VALID));
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            assertEquals("Cyclic dependency detected when setting requirement: FilterOutcome[category=VALID]",
                    e.getMessage());
        }
    }

    public void testGetDatastore() throws Exception {
        assertNotNull(analysisJobBuilder.getDatastore());
        assertEquals("my db", analysisJobBuilder.getDatastore().getName());
    }

    public void testToString() throws Exception {
        AnalyzerJobBuilder<StringAnalyzer> ajb = analysisJobBuilder.addAnalyzer(StringAnalyzer.class);
        TransformerJobBuilder<ConvertToStringTransformer> tjb = analysisJobBuilder
                .addTransformer(ConvertToStringTransformer.class);

        assertEquals("AnalyzerJobBuilder[analyzer=String analyzer,inputColumns=[]]", ajb.toString());
        assertEquals("TransformerJobBuilder[transformer=Convert to string,inputColumns=[]]", tjb.toString());
    }

    public void testToAnalysisJob() throws Exception {
        Table employeeTable = datastore.openConnection().getDataContext().getDefaultSchema()
                .getTableByName("EMPLOYEES");
        assertNotNull(employeeTable);

        Column emailColumn = employeeTable.getColumnByName("EMAIL");
        analysisJobBuilder.addSourceColumns(employeeTable.getColumnByName("EMPLOYEENUMBER"),
                employeeTable.getColumnByName("FIRSTNAME"), emailColumn);

        assertTrue(analysisJobBuilder.containsSourceColumn(emailColumn));
        assertFalse(analysisJobBuilder.containsSourceColumn(null));
        assertFalse(analysisJobBuilder.containsSourceColumn(employeeTable.getColumnByName("LASTNAME")));

        TransformerJobBuilder<ConvertToStringTransformer> transformerJobBuilder = analysisJobBuilder
                .addTransformer(ConvertToStringTransformer.class);

        Collection<InputColumn<?>> numberColumns = analysisJobBuilder.getAvailableInputColumns(Number.class);
        assertEquals(1, numberColumns.size());
        assertEquals("[MetaModelInputColumn[PUBLIC.EMPLOYEES.EMPLOYEENUMBER]]",
                Arrays.toString(numberColumns.toArray()));

        transformerJobBuilder.addInputColumn(numberColumns.iterator().next());
        assertTrue(transformerJobBuilder.isConfigured());

        // the AnalyzerJob has no Analyzers yet, so it is not "configured".
        assertFalse(analysisJobBuilder.isConfigured());

        AnalyzerJobBuilder<StringAnalyzer> analyzerJobBuilder = analysisJobBuilder.addAnalyzer(StringAnalyzer.class);

        List<InputColumn<?>> stringInputColumns = analysisJobBuilder.getAvailableInputColumns(String.class);
        Set<String> columnNames = new TreeSet<String>();
        for (InputColumn<?> inputColumn : stringInputColumns) {
            columnNames.add(inputColumn.getName());
        }
        assertEquals("[EMAIL, EMPLOYEENUMBER (as string), FIRSTNAME]", columnNames.toString());

        analyzerJobBuilder.addInputColumns(stringInputColumns);
        assertTrue(analyzerJobBuilder.isConfigured());

        // now there is: source columns, configured analyzers and configured
        // transformers.
        assertTrue(analysisJobBuilder.isConfigured());

        AnalysisJob analysisJob = analysisJobBuilder.toAnalysisJob();

        assertEquals("ImmutableAnalysisJob[sourceColumns=3,filterJobs=0,transformerJobs=1,analyzerJobs=1]",
                analysisJob.toString());

        // test hashcode and equals
        assertNotSame(analysisJobBuilder.toAnalysisJob(), analysisJob);
        assertEquals(analysisJobBuilder.toAnalysisJob(), analysisJob);
        assertEquals(analysisJobBuilder.toAnalysisJob().hashCode(), analysisJob.hashCode());

        Collection<InputColumn<?>> sourceColumns = analysisJob.getSourceColumns();
        assertEquals(3, sourceColumns.size());

        try {
            sourceColumns.add(new MockInputColumn<Boolean>("bla", Boolean.class));
            fail("Exception expected");
        } catch (UnsupportedOperationException e) {
            // do nothing
        }

        Collection<TransformerJob> transformerJobs = analysisJob.getTransformerJobs();
        assertEquals(1, transformerJobs.size());

        TransformerJob transformerJob = transformerJobs.iterator().next();
        assertEquals("ImmutableTransformerJob[name=null,transformer=Convert to string]", transformerJob.toString());

        assertEquals("[MetaModelInputColumn[PUBLIC.EMPLOYEES.EMPLOYEENUMBER]]",
                Arrays.toString(transformerJob.getInput()));

        Collection<AnalyzerJob> analyzerJobs = analysisJob.getAnalyzerJobs();
        assertEquals(1, analyzerJobs.size());

        AnalyzerJob analyzerJob = analyzerJobs.iterator().next();
        assertEquals("ImmutableAnalyzerJob[name=null,analyzer=String analyzer]", analyzerJob.toString());
    }

    public void testGetAvailableUnfilteredBeans() throws Exception {
        Table customersTable = datastore.openConnection().getDataContext().getDefaultSchema()
                .getTableByName("CUSTOMERS");
        assertNotNull(customersTable);

        analysisJobBuilder.addSourceColumns(customersTable.getColumnByName("ADDRESSLINE1"),
                customersTable.getColumnByName("ADDRESSLINE2"));

        AnalyzerJobBuilder<StringAnalyzer> saAjb = analysisJobBuilder.addAnalyzer(StringAnalyzer.class);
        saAjb.addInputColumns(analysisJobBuilder.getSourceColumns());

        FilterJobBuilder<MaxRowsFilter, MaxRowsFilter.Category> fjb = analysisJobBuilder.addFilter(MaxRowsFilter.class);

        List<AbstractBeanWithInputColumnsBuilder<?, ?, ?>> result = analysisJobBuilder.getAvailableUnfilteredBeans(fjb);
        assertEquals(1, result.size());
        assertEquals(result.get(0), saAjb);

        AnalyzerJobBuilder<PatternFinderAnalyzer> pfAjb = analysisJobBuilder.addAnalyzer(PatternFinderAnalyzer.class);
        pfAjb.addInputColumns(analysisJobBuilder.getSourceColumns());

        result = analysisJobBuilder.getAvailableUnfilteredBeans(fjb);
        assertEquals(2, result.size());
        assertEquals(result.get(0), saAjb);
        assertEquals(result.get(1), pfAjb);

        pfAjb.setRequirement(fjb, MaxRowsFilter.Category.VALID);

        result = analysisJobBuilder.getAvailableUnfilteredBeans(fjb);
        assertEquals(1, result.size());
        assertEquals(result.get(0), saAjb);
    }

    public void testRemoveFilter() throws Exception {
        try (DatastoreConnection con = datastore.openConnection()) {
            FilterJobBuilder<MaxRowsFilter, MaxRowsFilter.Category> filter1 = analysisJobBuilder
                    .addFilter(MaxRowsFilter.class);
            analysisJobBuilder.setDefaultRequirement(filter1, MaxRowsFilter.Category.VALID);

            TransformerJobBuilder<EmailStandardizerTransformer> emailStdTransformer = analysisJobBuilder
                    .addTransformer(EmailStandardizerTransformer.class);
            ComponentRequirement componentRequirement = emailStdTransformer.getComponentRequirement();
            assertSame(filter1.getFilterOutcome(MaxRowsFilter.Category.VALID), componentRequirement
                    .getProcessingDependencies().iterator().next());

            FilterJobBuilder<MaxRowsFilter, MaxRowsFilter.Category> filter2 = analysisJobBuilder
                    .addFilter(MaxRowsFilter.class);
            filter2.setRequirement(null);
            filter1.setRequirement(filter2.getFilterOutcome(MaxRowsFilter.Category.VALID));

            assertNull(filter2.getComponentRequirement());

            analysisJobBuilder.addSourceColumn(con.getSchemaNavigator().convertToColumn("EMPLOYEES.EMAIL"));
            emailStdTransformer.addInputColumn(analysisJobBuilder.getSourceColumnByName("email"));

            AnalyzerJobBuilder<StringAnalyzer> stringAnalyzer = analysisJobBuilder.addAnalyzer(StringAnalyzer.class);
            stringAnalyzer.addInputColumns(emailStdTransformer.getOutputColumns());

            assertSame(filter1.getFilterOutcome(MaxRowsFilter.Category.VALID), stringAnalyzer.getComponentRequirement()
                    .getProcessingDependencies().iterator().next());

            analysisJobBuilder.removeFilter(filter1);

            assertNull(analysisJobBuilder.getDefaultRequirement());
            assertSame(filter2.getFilterOutcome(MaxRowsFilter.Category.VALID), stringAnalyzer.getComponentRequirement()
                    .getProcessingDependencies().iterator().next());
            assertSame(filter2.getFilterOutcome(MaxRowsFilter.Category.VALID), emailStdTransformer.getComponentRequirement()
                    .getProcessingDependencies().iterator().next());
        }
    }

    public void testSourceColumnListeners() throws Exception {
        Datastore datastore = TestHelper.createSampleDatabaseDatastore("mydb");
        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(new AnalyzerBeansConfigurationImpl())) {

            ajb.setDatastore(datastore);

            SourceColumnChangeListener listener1 = EasyMock.createMock(SourceColumnChangeListener.class);
            ajb.getSourceColumnListeners().add(listener1);

            Column column = ajb.getDatastoreConnection().getSchemaNavigator().convertToColumn("EMPLOYEES.EMAIL");
            MetaModelInputColumn inputColumn = new MetaModelInputColumn(column);

            // scene 1: add source column
            listener1.onAdd(inputColumn);
            listener1.onRemove(inputColumn);
            listener1.onAdd(inputColumn);

            EasyMock.replay(listener1);

            ajb.addSourceColumns(inputColumn);
            ajb.removeSourceColumn(column);
            ajb.addSourceColumn(inputColumn);

            EasyMock.verify(listener1);
            EasyMock.reset(listener1);

            // scene 2: add transformer
            TransformerChangeListener listener2 = EasyMock.createMock(TransformerChangeListener.class);
            ajb.getTransformerChangeListeners().add(listener2);

            final TransformerBeanDescriptor<EmailStandardizerTransformer> descriptor = Descriptors
                    .ofTransformer(EmailStandardizerTransformer.class);
            IArgumentMatcher tjbMatcher = new IArgumentMatcher() {
                @Override
                public boolean matches(Object argument) {
                    TransformerJobBuilder<?> tjb = (TransformerJobBuilder<?>) argument;
                    return tjb.getDescriptor() == descriptor;
                }

                @Override
                public void appendTo(StringBuffer buffer) {
                    buffer.append("transformer job builder");
                }
            };
            EasyMock.reportMatcher(tjbMatcher);
            listener2.onAdd(null);

            // output updated
            EasyMock.reportMatcher(tjbMatcher);
            EasyMock.reportMatcher(new IArgumentMatcher() {

                @Override
                public boolean matches(Object argument) {
                    @SuppressWarnings("unchecked")
                    List<MutableInputColumn<?>> list = (List<MutableInputColumn<?>>) argument;
                    if (list.size() == 2) {
                        if (list.get(0).getName().equals("Username")) {
                            if (list.get(1).getName().equals("Domain")) {
                                return true;
                            }
                        }
                    }
                    return false;
                }

                @Override
                public void appendTo(StringBuffer buffer) {
                    buffer.append("list of output columns");
                }
            });
            listener2.onOutputChanged(null, null);

            // configuration updated
            EasyMock.reportMatcher(tjbMatcher);
            listener2.onConfigurationChanged(null);

            // remove transformer
            EasyMock.reportMatcher(tjbMatcher);
            EasyMock.reportMatcher(new IArgumentMatcher() {

                @Override
                public boolean matches(Object argument) {
                    @SuppressWarnings("unchecked")
                    List<MutableInputColumn<?>> list = (List<MutableInputColumn<?>>) argument;
                    return list.isEmpty();
                }

                @Override
                public void appendTo(StringBuffer buffer) {
                    buffer.append("empty list of output columns");
                }
            });
            listener2.onOutputChanged(null, null);

            EasyMock.reportMatcher(tjbMatcher);
            listener2.onRemove(null);

            listener1.onRemove(inputColumn);

            EasyMock.replay(listener1, listener2);

            ajb.addTransformer(descriptor).addInputColumn(inputColumn);
            ajb.reset();

            EasyMock.verify(listener1, listener2);
        }
    }
}
