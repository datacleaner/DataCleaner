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
package org.datacleaner.beans;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.metamodel.util.FileResource;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.OutputDataStreamJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.concurrent.MultiThreadedTaskRunner;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.result.CharacterSetDistributionResult;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabNavigator;
import org.datacleaner.result.ListResult;
import org.datacleaner.result.renderer.CrosstabTextRenderer;
import org.datacleaner.storage.InMemoryRowAnnotationFactory;
import org.apache.metamodel.util.EqualsBuilder;
import org.datacleaner.test.MockAnalyzer;

import com.ibm.icu.text.UnicodeSet;

public class CharacterSetDistributionAnalyzerTest extends TestCase {

    private static final String CHARSET_NAMES = "[Arabic, Armenian, Bengali, Cyrillic, Devanagari, Georgian, Greek, Gujarati, Gurmukhi, Han, Hangul, Hebrew, Hiragana, Kannada, Katakana, Latin, ASCII, Latin, non-ASCII, Malayalam, Oriya, Syriac, Tamil, Telugu, Thaana, Thai]";

    public void testCreateFilters() throws Exception {
        Map<String, UnicodeSet> unicodeSets = CharacterSetDistributionAnalyzer.createUnicodeSets();
        Set<String> keys = unicodeSets.keySet();
        assertEquals(CHARSET_NAMES, keys.toString());

        UnicodeSet set = unicodeSets.get("Arabic");
        assertFalse(set.contains('a'));
        assertTrue(set.containsAll("البيانات"));

        set = unicodeSets.get("Latin, ASCII");
        assertTrue(set.contains('a'));
        assertTrue(set.contains('z'));
        assertFalse(set.contains('ä'));
        assertFalse(set.contains('æ'));

        set = unicodeSets.get("Latin, non-ASCII");
        assertFalse(set.contains('a'));
        assertFalse(set.contains('z'));
        assertTrue(set.contains('ä'));
        assertTrue(set.contains('æ'));
    }

    public void testSimpleScenario() throws Exception {
        CharacterSetDistributionAnalyzer analyzer = new CharacterSetDistributionAnalyzer();
        InputColumn<String> col1 = new MockInputColumn<String>("foo", String.class);
        InputColumn<String> col2 = new MockInputColumn<String>("bar", String.class);

        @SuppressWarnings("unchecked")
        InputColumn<String>[] cols = new InputColumn[] { col1, col2 };
        analyzer._columns = cols;
        analyzer._annotationFactory = new InMemoryRowAnnotationFactory();
        analyzer.init();

        analyzer.run(new MockInputRow().put(col1, "foobar").put(col2, "foobar"), 10);
        analyzer.run(new MockInputRow().put(col1, "DåtåClænør"), 1);
        analyzer.run(new MockInputRow().put(col1, "Данныечистого"), 1);
        analyzer.run(new MockInputRow().put(col1, "數據清潔"), 1);
        analyzer.run(new MockInputRow().put(col1, "بيانات الأنظف"), 1);
        analyzer.run(new MockInputRow().put(col1, "dữ liệu sạch hơn"), 1);

        CharacterSetDistributionResult result = analyzer.getResult();
        assertTrue(EqualsBuilder.equals(analyzer._columns, result.getColumns()));
        assertEquals(CHARSET_NAMES, Arrays.toString(result.getUnicodeSetNames()));

        Crosstab<?> crosstab = result.getCrosstab();
        assertEquals("[Column, Measures]", Arrays.toString(crosstab.getDimensionNames()));

        assertEquals(CHARSET_NAMES, crosstab.getDimension("Measures").getCategories().toString());

        CrosstabNavigator<?> cyrillicNavigation = crosstab.navigate().where("Column", "foo").where("Measures", "Cyrillic");
        assertEquals("1", cyrillicNavigation.get().toString());
        AnnotatedRowsResult cyrillicAnnotatedRowsResult = (AnnotatedRowsResult) cyrillicNavigation.explore().getResult();
        InputRow[] annotatedRows = cyrillicAnnotatedRowsResult.getRows();
        assertEquals(1, annotatedRows.length);
        assertEquals("Данныечистого", annotatedRows[0].getValue(col1));
        assertEquals("12", crosstab.navigate().where("Column", "foo").where("Measures", "Latin, ASCII").get().toString());
        assertEquals("2", crosstab.navigate().where("Column", "foo").where("Measures", "Latin, non-ASCII").get().toString());

        String resultString = new CrosstabTextRenderer().render(result);
        String[] resultLines = resultString.split("\n");
        assertEquals(25, resultLines.length);
        assertEquals("                    foo    bar ", resultLines[0]);
        assertEquals("Arabic                1      0 ", resultLines[1]);
        assertEquals("Armenian              0      0 ", resultLines[2]);
        assertEquals("Bengali               0      0 ", resultLines[3]);
        assertEquals("Cyrillic              1      0 ", resultLines[4]);
        assertEquals("Devanagari            0      0 ", resultLines[5]);
        assertEquals("Georgian              0      0 ", resultLines[6]);
        assertEquals("Greek                 0      0 ", resultLines[7]);
        assertEquals("Gujarati              0      0 ", resultLines[8]);
        assertEquals("Gurmukhi              0      0 ", resultLines[9]);
        assertEquals("Han                   1      0 ", resultLines[10]);
        assertEquals("Hangul                0      0 ", resultLines[11]);
        assertEquals("Hebrew                0      0 ", resultLines[12]);
        assertEquals("Hiragana              0      0 ", resultLines[13]);
        assertEquals("Kannada               0      0 ", resultLines[14]);
        assertEquals("Katakana              0      0 ", resultLines[15]);
        assertEquals("Latin, ASCII         12     10 ", resultLines[16]);
        assertEquals("Latin, non-ASCII      2      0 ", resultLines[17]);
        assertEquals("Malayalam             0      0 ", resultLines[18]);
        assertEquals("Oriya                 0      0 ", resultLines[19]);
        assertEquals("Syriac                0      0 ", resultLines[20]);
        assertEquals("Tamil                 0      0 ", resultLines[21]);
        assertEquals("Telugu                0      0 ", resultLines[22]);
        assertEquals("Thaana                0      0 ", resultLines[23]);
        assertEquals("Thai                  0      0 ", resultLines[24]);
    }

    public void testOutputDataStream() throws Throwable {
        final MultiThreadedTaskRunner taskRunner = new MultiThreadedTaskRunner(16);
        final DataCleanerEnvironment environment = new DataCleanerEnvironmentImpl()
                .withTaskRunner(taskRunner);
        final FileResource file = new FileResource("src/test/resources/strings.txt");
        final Datastore datastore = new CsvDatastore("strings", file);
        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(datastore)
                .withEnvironment(environment);

        final AnalysisJob job;
        try (final AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {
            ajb.setDatastore(datastore);
            ajb.addSourceColumns("given_name");
            ajb.addSourceColumns("family_name");

            final AnalyzerComponentBuilder<CharacterSetDistributionAnalyzer> analyzer1 = ajb
                    .addAnalyzer(CharacterSetDistributionAnalyzer.class);

            // now configure it
            final List<MetaModelInputColumn> sourceColumns = ajb.getSourceColumns();
            analyzer1.setName("analyzer1");
            analyzer1.addInputColumns(sourceColumns);

            assertTrue(analyzer1.isConfigured());
            final OutputDataStream dataStream = analyzer1.getOutputDataStream("character sets");
            assertNotNull(dataStream);

            final AnalysisJobBuilder outputDataStreamJobBuilder = analyzer1.getOutputDataStreamJobBuilder(dataStream);
            final List<MetaModelInputColumn> outputDataStreamColumns = outputDataStreamJobBuilder.getSourceColumns();
            assertEquals(3, outputDataStreamColumns.size());
            assertEquals("MetaModelInputColumn[character sets.Character set]", outputDataStreamColumns.get(0).toString());
            assertEquals("MetaModelInputColumn[character sets.given_name]", outputDataStreamColumns.get(1).toString());
            assertEquals("MetaModelInputColumn[character sets.family_name]", outputDataStreamColumns.get(2).toString());

            final AnalyzerComponentBuilder<MockAnalyzer> analyzer2 = outputDataStreamJobBuilder
                    .addAnalyzer(MockAnalyzer.class);
            analyzer2.addInputColumns(outputDataStreamColumns);
            analyzer2.setName("analyzer2");
            assertTrue(analyzer2.isConfigured());

            assertTrue(analyzer1.isOutputDataStreamConsumed(dataStream));

            job = ajb.toAnalysisJob();
        }

        final AnalyzerJob analyzerJob1 = job.getAnalyzerJobs().get(0);
        final OutputDataStreamJob[] outputDataStreamJobs = analyzerJob1.getOutputDataStreamJobs();
        final OutputDataStreamJob outputDataStreamJob = outputDataStreamJobs[0];
        final AnalyzerJob analyzerJob2 = outputDataStreamJob.getJob().getAnalyzerJobs().get(0);

        // now run the job(s)
        final AnalysisRunnerImpl runner = new AnalysisRunnerImpl(configuration);
        final AnalysisResultFuture resultFuture = runner.run(job);
        resultFuture.await();

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        assertEquals(2, resultFuture.getResults().size());

        final CharacterSetDistributionResult result1 = (CharacterSetDistributionResult) resultFuture.getResult(analyzerJob1);
        assertNotNull(result1);
        assertEquals(24, result1.getCrosstab().getDimension("Measures").getCategoryCount());

        final ListResult<?> result2 = (ListResult<?>) resultFuture.getResult(analyzerJob2);
        assertNotNull(result2);
        assertEquals(24, result2.getValues().size());
        assertEquals("MetaModelInputRow[Row[values=[Greek, 3, 3]]]", result2.getValues().get(6).toString());
        assertEquals("MetaModelInputRow[Row[values=[Han, 4, 4]]]", result2.getValues().get(9).toString());
    }
}
