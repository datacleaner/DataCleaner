/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.connection.PojoDatastore;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.datacleaner.util.PreviewTransformedDataAnalyzer;
import org.eobjects.metamodel.pojo.MapTableDataProvider;
import org.eobjects.metamodel.pojo.TableDataProvider;
import org.eobjects.metamodel.util.SimpleTableDef;

public class LuceneIntegrationTest extends TestCase {

    private static final String[] AVAILABLE_FIELD_NAMES = new String[] { "name", "country", "phone" };
    private static final String[] SEARCHED_FIELD_NAMES = new String[] { "name", "country" };

    public void testScenario() throws Exception {
        final Datastore orderdb = new JdbcDatastore("orderdb", "jdbc:hsqldb:res:orderdb;readonly=true",
                "org.hsqldb.jdbcDriver");

        AnalyzerBeansConfigurationImpl conf = new AnalyzerBeansConfigurationImpl().replace(new DatastoreCatalogImpl(
                orderdb));

        final SearchIndex searchIndex = runWriteSearchIndexJob(conf);
        runSearchIndexAssertions(searchIndex);

        final SimpleTableDef tableDef = new SimpleTableDef("inputtable", SEARCHED_FIELD_NAMES);
        final Collection<Map<String, ?>> maps = new ArrayList<Map<String, ?>>();
        maps.add(createMap("Atelier graphick", "France"));
        maps.add(createMap("Foobar", "FOOO"));
        maps.add(createMap("La Rochelle Gifts", "France"));
        maps.add(createMap("Land of Toys Inc.", "USA"));
        maps.add(createMap("Signal Gift Stores", "USA"));

        final TableDataProvider<?> tableDataProvider = new MapTableDataProvider(tableDef, maps);
        final Datastore searches = new PojoDatastore("searches",
                Arrays.<TableDataProvider<?>> asList(tableDataProvider));
        conf = conf.replace(new DatastoreCatalogImpl(searches));

        runSearchJob(conf, searchIndex);
    }

    private void runSearchIndexAssertions(SearchIndex searchIndex) throws CorruptIndexException, IOException {
        final AbstractSearchIndex abstractSearchIndex = (AbstractSearchIndex) searchIndex;
        final IndexReader reader = abstractSearchIndex.getIndexReader();

        final Document document = reader.document(0);
        assertEquals("[Atelier graphique]", Arrays.toString(document.getValues("name")));
        assertEquals("{name=Atelier graphique, country=France, phone=40.32.2555}", SearchTransformer.toMap(document)
                .toString());
        assertEquals("{name=Signal Gift Stores, country=USA, phone=7025551838}",
                SearchTransformer.toMap(reader.document(1)).toString());
        assertEquals("{name=Australian Collectors, Co., country=Australia, phone=03 9520 4555}", SearchTransformer
                .toMap(reader.document(2)).toString());
        assertEquals("{name=La Rochelle Gifts, country=France, phone=40.67.8555}",
                SearchTransformer.toMap(reader.document(3)).toString());
    }

    private Map<String, ?> createMap(String name, String country) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", name);
        map.put("country", country);
        return map;
    }

    private void runSearchJob(AnalyzerBeansConfiguration conf, SearchIndex searchIndex) {
        final AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(conf);
        analysisJobBuilder.setDatastore("searches");
        analysisJobBuilder.addSourceColumns(SEARCHED_FIELD_NAMES);

        final TransformerJobBuilder<SearchTransformer> transformer = analysisJobBuilder
                .addTransformer(SearchTransformer.class);
        transformer.getConfigurableBean().searchIndex = searchIndex;
        transformer.getConfigurableBean().searchFields = SEARCHED_FIELD_NAMES;
        transformer.addInputColumns(analysisJobBuilder.getSourceColumns());
        transformer.getOutputColumns().get(0).setName("out1");
        transformer.getOutputColumns().get(1).setName("out2");

        final AnalyzerJobBuilder<PreviewTransformedDataAnalyzer> analyzer = analysisJobBuilder
                .addAnalyzer(PreviewTransformedDataAnalyzer.class);
        analyzer.addInputColumns(analysisJobBuilder.getSourceColumns());
        analyzer.addInputColumns(transformer.getOutputColumns());

        final AnalysisJob job = analysisJobBuilder.toAnalysisJob();

        final AnalysisResultFuture future = new AnalysisRunnerImpl(conf).run(job);
        if (future.isErrornous()) {
            Throwable error = future.getErrors().get(0);
            if (error instanceof RuntimeException) {
                throw (RuntimeException) error;
            }
            throw new IllegalStateException(error);
        }

        final List<AnalyzerResult> results = future.getResults();
        assertEquals(1, results.size());

        final PreviewTransformedDataAnalyzer result = (PreviewTransformedDataAnalyzer) results.get(0);
        final List<Object[]> list = result.getList();

        assertEquals(5, list.size());
        assertEquals(
                "[Atelier graphick, France, {name=Atelier graphique, country=France, phone=40.32.2555}, 1.2555174]",
                Arrays.toString(list.get(0)));
        assertEquals("[Foobar, FOOO, null, 0]", Arrays.toString(list.get(1)));
        assertEquals(
                "[La Rochelle Gifts, France, {name=Atelier graphique, country=France, phone=40.32.2555}, 0.6277587]",
                Arrays.toString(list.get(2)));
        assertEquals("[Land of Toys Inc., USA, null, 0]", Arrays.toString(list.get(3)));
        assertEquals("[Signal Gift Stores, USA, null, 0]", Arrays.toString(list.get(4)));
    }

    private SearchIndex runWriteSearchIndexJob(AnalyzerBeansConfiguration conf) {
        final AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(conf);
        analysisJobBuilder.setDatastore("orderdb");
        analysisJobBuilder.addSourceColumns("CUSTOMERS.CUSTOMERNAME", "CUSTOMERS.COUNTRY", "CUSTOMERS.PHONE");

        final AnalyzerJobBuilder<WriteSearchIndexAnalyzer> analyzer = analysisJobBuilder
                .addAnalyzer(WriteSearchIndexAnalyzer.class);

        final InMemorySearchIndex searchIndex = new InMemorySearchIndex("my index", AVAILABLE_FIELD_NAMES);

        analyzer.getConfigurableBean().searchFields = AVAILABLE_FIELD_NAMES;
        analyzer.getConfigurableBean().searchIndex = searchIndex;
        analyzer.addInputColumns(analysisJobBuilder.getSourceColumns());

        final AnalysisJob job = analysisJobBuilder.toAnalysisJob();

        final AnalysisResultFuture future = new AnalysisRunnerImpl(conf).run(job);
        if (future.isErrornous()) {
            Throwable error = future.getErrors().get(0);
            if (error instanceof RuntimeException) {
                throw (RuntimeException) error;
            }
            throw new IllegalStateException(error);
        }

        return searchIndex;
    }
}
