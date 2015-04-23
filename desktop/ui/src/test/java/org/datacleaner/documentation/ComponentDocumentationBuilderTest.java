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
package org.datacleaner.documentation;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.metamodel.util.FileHelper;
import org.datacleaner.beans.CompletenessAnalyzer;
import org.datacleaner.beans.filter.EqualsFilter;
import org.datacleaner.beans.stringpattern.PatternFinderAnalyzer;
import org.datacleaner.beans.transform.ConcatenatorTransformer;
import org.datacleaner.components.tablelookup.TableLookupTransformer;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.util.StringUtils;
import org.junit.Test;

public class ComponentDocumentationBuilderTest {

    private final ComponentDocumentationBuilder documentationCreator = new ComponentDocumentationBuilder();

    /**
     * Test of an analyzer
     * 
     * @throws Exception
     */
    @Test
    public void testBuildPatternFinderDocs() throws Exception {
        final File benchmarkFile = new File("src/test/resources/documentation/pattern_finder.html");
        final File outputFile = new File("target/documentation_pattern_finder.html");
        final ComponentDescriptor<?> descriptor = Descriptors.ofAnalyzer(PatternFinderAnalyzer.class);

        runBenchmarkTest(descriptor, benchmarkFile, outputFile);
    }

    /**
     * Test of a pretty simple filter
     * 
     * @throws Exception
     */
    @Test
    public void testBuildEqualsFilterDocs() throws Exception {
        final File benchmarkFile = new File("src/test/resources/documentation/equals_filter.html");
        final File outputFile = new File("target/documentation_equals_filter.html");
        final ComponentDescriptor<?> descriptor = Descriptors.ofFilter(EqualsFilter.class);

        runBenchmarkTest(descriptor, benchmarkFile, outputFile);
    }

    /**
     * Test of a pretty simple transformer
     * 
     * @throws Exception
     */
    @Test
    public void testBuildConcatenatorTransformerDocs() throws Exception {
        final File benchmarkFile = new File("src/test/resources/documentation/concatenator.html");
        final File outputFile = new File("target/documentation_concatenator.html");
        final ComponentDescriptor<?> descriptor = Descriptors.ofTransformer(ConcatenatorTransformer.class);

        runBenchmarkTest(descriptor, benchmarkFile, outputFile);
    }

    /**
     * Test of an advanced transformer
     * 
     * @throws Exception
     */
    @Test
    public void testBuildTableLookupTransformerDocs() throws Exception {
        final File benchmarkFile = new File("src/test/resources/documentation/table_lookup.html");
        final File outputFile = new File("target/documentation_table_lookup.html");
        final ComponentDescriptor<?> descriptor = Descriptors.ofTransformer(TableLookupTransformer.class);

        runBenchmarkTest(descriptor, benchmarkFile, outputFile);
    }

    /**
     * Test of an analyzer with mapped properties and enums
     * 
     * @throws Exception
     */
    @Test
    public void testBuildCompletenessAnalyzerDocs() throws Exception {
        final File benchmarkFile = new File("src/test/resources/documentation/completeness_analyzer.html");
        final File outputFile = new File("target/documentation_completeness_analyzer.html");
        final ComponentDescriptor<?> descriptor = Descriptors.ofAnalyzer(CompletenessAnalyzer.class);

        runBenchmarkTest(descriptor, benchmarkFile, outputFile);
    }

    private void runBenchmarkTest(ComponentDescriptor<?> descriptor, File benchmarkFile, File outputFile)
            throws Exception {
        documentationCreator.createDocumentation(descriptor, new FileOutputStream(outputFile));
        final String output = normalize(FileHelper.readFileAsString(outputFile));

        final String benchmark;
        if (benchmarkFile.exists()) {
            benchmark = normalize(FileHelper.readFileAsString(benchmarkFile));
        } else {
            // use path to make it simple to add the benchmark file when it
            // breaks
            benchmark = "No such file: " + benchmarkFile.getPath();
        }

        assertEquals(benchmark, output);
    }

    private String normalize(String str) {
        return StringUtils.replaceAll(str, "\r\n", "\n").trim();
    }
}
