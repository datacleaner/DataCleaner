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
package org.datacleaner.test.full.scenarios;

import java.util.List;

import junit.framework.TestCase;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.StringAnalyzer;
import org.datacleaner.beans.StringAnalyzerResult;
import org.datacleaner.beans.script.JavaScriptFilter;
import org.datacleaner.beans.standardize.EmailStandardizerTransformer;
import org.datacleaner.beans.standardize.NameStandardizerTransformer;
import org.datacleaner.beans.valuedist.ValueDistributionAnalyzer;
import org.datacleaner.beans.valuedist.ValueDistributionAnalyzerResult;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;

@SuppressWarnings("deprecation")
public class NameAndEmailPartEqualityTest extends TestCase {

    public void testScenario() throws Throwable {
        DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl();

        AnalysisRunner runner = new AnalysisRunnerImpl(configuration);

        CsvDatastore ds = new CsvDatastore("data.csv", "src/test/resources/NameAndEmailPartEqualityTest-data.csv");

        AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(configuration);
        analysisJobBuilder.setDatastore(ds);

        DatastoreConnection con = ds.openConnection();
        Schema schema = con.getDataContext().getDefaultSchema();
        Table table = schema.getTables()[0];
        assertNotNull(table);

        Column nameColumn = table.getColumnByName("name");
        Column emailColumn = table.getColumnByName("email");

        analysisJobBuilder.addSourceColumns(nameColumn, emailColumn);

        TransformerComponentBuilder<NameStandardizerTransformer> nameTransformerComponentBuilder = analysisJobBuilder
                .addTransformer(NameStandardizerTransformer.class);
        nameTransformerComponentBuilder.addInputColumn(analysisJobBuilder.getSourceColumnByName("name"));
        nameTransformerComponentBuilder.setConfiguredProperty("Name patterns",
                NameStandardizerTransformer.DEFAULT_PATTERNS);

        assertTrue(nameTransformerComponentBuilder.isConfigured());

        final List<MutableInputColumn<?>> nameColumns = nameTransformerComponentBuilder.getOutputColumns();
        assertEquals(4, nameColumns.size());
        assertEquals("Firstname", nameColumns.get(0).getName());
        assertEquals("Lastname", nameColumns.get(1).getName());
        assertEquals("Middlename", nameColumns.get(2).getName());
        assertEquals("Titulation", nameColumns.get(3).getName());

        TransformerComponentBuilder<EmailStandardizerTransformer> emailTransformerComponentBuilder = analysisJobBuilder
                .addTransformer(EmailStandardizerTransformer.class);
        emailTransformerComponentBuilder.addInputColumn(analysisJobBuilder.getSourceColumnByName("email"));

        assertTrue(emailTransformerComponentBuilder.isConfigured());

        @SuppressWarnings("unchecked")
        final MutableInputColumn<String> usernameColumn = (MutableInputColumn<String>) emailTransformerComponentBuilder
                .getOutputColumnByName("Username");
        assertNotNull(usernameColumn);

        assertTrue(analysisJobBuilder.addAnalyzer(StringAnalyzer.class).addInputColumns(nameColumns)
                .addInputColumns(emailTransformerComponentBuilder.getOutputColumns()).isConfigured());

        for (InputColumn<?> inputColumn : nameColumns) {
            AnalyzerComponentBuilder<ValueDistributionAnalyzer> analyzerJobBuilder = analysisJobBuilder
                    .addAnalyzer(ValueDistributionAnalyzer.class);
            analyzerJobBuilder.addInputColumn(inputColumn);
            analyzerJobBuilder.setConfiguredProperty("Record unique values", false);
            analyzerJobBuilder.setConfiguredProperty("Top n most frequent values", 1000);
            analyzerJobBuilder.setConfiguredProperty("Bottom n most frequent values", 1000);
            assertTrue(analyzerJobBuilder.isConfigured());
        }

        FilterComponentBuilder<JavaScriptFilter, JavaScriptFilter.Category> fjb = analysisJobBuilder
                .addFilter(JavaScriptFilter.class);
        fjb.addInputColumn(nameTransformerComponentBuilder.getOutputColumnByName("Firstname"));
        fjb.addInputColumn(usernameColumn);
        fjb.setConfiguredProperty("Source code", "values[0] == values[1];");

        assertTrue(fjb.isConfigured());

        analysisJobBuilder.addAnalyzer(StringAnalyzer.class)
                .addInputColumn(analysisJobBuilder.getSourceColumnByName("email"))
                .setRequirement(fjb, JavaScriptFilter.Category.VALID);
        analysisJobBuilder.addAnalyzer(StringAnalyzer.class)
                .addInputColumn(analysisJobBuilder.getSourceColumnByName("email"))
                .setRequirement(fjb, JavaScriptFilter.Category.INVALID);

        AnalysisResultFuture resultFuture = runner.run(analysisJobBuilder.toAnalysisJob());
        analysisJobBuilder.close();

        con.close();

        if (!resultFuture.isSuccessful()) {
            List<Throwable> errors = resultFuture.getErrors();
            throw errors.get(0);
        }

        List<AnalyzerResult> results = resultFuture.getResults();

        assertEquals(7, results.size());

        ValueDistributionAnalyzerResult vdResult = (ValueDistributionAnalyzerResult) results.get(1);
        assertEquals("Firstname", vdResult.getName());
        assertEquals(0, vdResult.getNullCount());
        assertEquals(2, vdResult.getUniqueCount().intValue());
        assertEquals("[[barack->4], [<unique>->2]]", vdResult.getValueCounts().toString());

        vdResult = (ValueDistributionAnalyzerResult) results.get(2);
        assertEquals("Lastname", vdResult.getName());
        assertEquals(0, vdResult.getNullCount());
        assertEquals(0, vdResult.getUniqueCount().intValue());
        assertEquals("[[obama->4], [doe->2]]", vdResult.getValueCounts().toString());

        vdResult = (ValueDistributionAnalyzerResult) results.get(3);
        assertEquals("Middlename", vdResult.getName());
        assertEquals(4, vdResult.getNullCount());
        assertEquals(0, vdResult.getUniqueCount().intValue());
        assertEquals("[[<null>->4], [hussein->2]]", vdResult.getValueCounts().toString());

        vdResult = (ValueDistributionAnalyzerResult) results.get(4);
        assertEquals("Titulation", vdResult.getName());
        assertEquals(6, vdResult.getNullCount());
        assertEquals(0, vdResult.getUniqueCount().intValue());
        assertEquals("[[<null>->6]]", vdResult.getValueCounts().toString());

        StringAnalyzerResult stringAnalyzerResult = (StringAnalyzerResult) results.get(5);
        assertEquals(1, stringAnalyzerResult.getColumns().length);
        assertEquals("4", stringAnalyzerResult.getCrosstab().where("Column", "email").where("Measures", "Row count")
                .get().toString());

        stringAnalyzerResult = (StringAnalyzerResult) results.get(6);
        assertEquals(1, stringAnalyzerResult.getColumns().length);
        assertEquals("2", stringAnalyzerResult.getCrosstab().where("Column", "email").where("Measures", "Row count")
                .get().toString());
    }
}
