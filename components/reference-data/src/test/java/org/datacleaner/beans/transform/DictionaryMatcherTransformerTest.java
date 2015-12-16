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
package org.datacleaner.beans.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.beans.valuedist.ValueDistributionAnalyzer;
import org.datacleaner.beans.valuedist.ValueDistributionAnalyzerResult;
import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.ReferenceDataCatalogImpl;
import org.datacleaner.reference.SimpleDictionary;
import org.datacleaner.reference.SimpleSynonym;
import org.datacleaner.reference.SimpleSynonymCatalog;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.SynonymCatalog;

public class DictionaryMatcherTransformerTest extends TestCase {

	public void testParseAndAssignDictionaries() throws Throwable {
		Collection<Dictionary> dictionaries = new ArrayList<Dictionary>();
		dictionaries.add(new SimpleDictionary("eobjects.org products", "MetaModel", "DataCleaner", "AnalyzerBeans"));
		dictionaries.add(new SimpleDictionary("apache products", "commons-lang", "commons-math", "commons-codec",
				"commons-logging"));
		dictionaries.add(new SimpleDictionary("logging products", "commons-logging", "log4j", "slf4j", "java.util.Logging"));

		Collection<SynonymCatalog> synonymCatalogs = new ArrayList<SynonymCatalog>();
		synonymCatalogs.add(new SimpleSynonymCatalog("translated terms", new SimpleSynonym("hello", "howdy", "hi", "yo",
				"hey"), new SimpleSynonym("goodbye", "bye", "see you", "hey")));

		Collection<StringPattern> stringPatterns = new ArrayList<StringPattern>();

		ReferenceDataCatalogImpl ref = new ReferenceDataCatalogImpl(dictionaries, synonymCatalogs, stringPatterns);

		Datastore datastore = new CsvDatastore("my database", "src/test/resources/projects.csv");
		DataCleanerConfigurationImpl conf = new DataCleanerConfigurationImpl();
		AnalysisJobBuilder job = new AnalysisJobBuilder(conf);
		job.setDatastore(datastore);
		job.addSourceColumns("product", "version");
		TransformerComponentBuilder<DictionaryMatcherTransformer> tjb1 = job.addTransformer(DictionaryMatcherTransformer.class);
		tjb1.setConfiguredProperty(
				"Dictionaries",
				new Dictionary[] { ref.getDictionary("eobjects.org products"), ref.getDictionary("apache products"),
						ref.getDictionary("logging products") });
		tjb1.addInputColumn(job.getSourceColumnByName("product"));
		List<MutableInputColumn<?>> outputColumns = tjb1.getOutputColumns();
		assertEquals(3, outputColumns.size());
		outputColumns.get(0).setName("eobjects match");
		outputColumns.get(1).setName("apache match");
		outputColumns.get(2).setName("logging match");

		TransformerComponentBuilder<ConvertToNumberTransformer> tjb2 = job.addTransformer(ConvertToNumberTransformer.class);
		tjb2.addInputColumn(outputColumns.get(2));
		tjb2.getOutputColumns().get(0).setName("logging match -> number");

		AnalyzerComponentBuilder<ValueDistributionAnalyzer> ajb = job
				.addAnalyzer(ValueDistributionAnalyzer.class);
		ajb.addInputColumns(tjb1.getOutputColumns());
		ajb.addInputColumns(tjb2.getOutputColumns());

		assertTrue(job.isConfigured());

		AnalysisJob analysisJob = job.toAnalysisJob();
		AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(conf).run(analysisJob);
		
		if (!resultFuture.isSuccessful()) {
		    job.close();
			throw resultFuture.getErrors().get(0);
		}
		
		List<AnalyzerResult> results = resultFuture.getResults();

		assertEquals(4, results.size());
		ValueDistributionAnalyzerResult res = (ValueDistributionAnalyzerResult) results.get(0);
		assertEquals("eobjects match", res.getName());
		assertEquals(8, res.getCount("true").intValue());
		assertEquals(4, res.getCount("false").intValue());

		res = (ValueDistributionAnalyzerResult) results.get(1);
		assertEquals("apache match", res.getName());
		assertEquals(2, res.getCount("true").intValue());
		assertEquals(10, res.getCount("false").intValue());

		res = (ValueDistributionAnalyzerResult) results.get(2);
		assertEquals("logging match", res.getName());
		assertEquals(3, res.getCount("true").intValue());
		assertEquals(9, res.getCount("false").intValue());

		res = (ValueDistributionAnalyzerResult) results.get(3);
		assertEquals("logging match -> number", res.getName());
		assertEquals(3, res.getCount("1").intValue());
		assertEquals(9, res.getCount("0").intValue());
		
		job.close();
	}

	public void testTransform() throws Exception {
		Dictionary[] dictionaries = new Dictionary[] {
				new SimpleDictionary("danish male names", "kasper", "kim", "asbjørn"),
				new SimpleDictionary("danish female names", "trine", "kim", "lene") };
		DictionaryMatcherTransformer transformer = new DictionaryMatcherTransformer(null, dictionaries, new DataCleanerConfigurationImpl());
		transformer.init();
		assertEquals("[true, false]", Arrays.toString(transformer.transform("kasper")));
		assertEquals("[false, false]", Arrays.toString(transformer.transform("foobar")));
		assertEquals("[false, true]", Arrays.toString(transformer.transform("trine")));
		assertEquals("[true, true]", Arrays.toString(transformer.transform("kim")));
		
		transformer._outputType = MatchOutputType.INPUT_OR_NULL;
		assertEquals("[kim, kim]", Arrays.toString(transformer.transform("kim")));
		assertEquals("[null, trine]", Arrays.toString(transformer.transform("trine")));
		
		transformer.close();
	}
}
