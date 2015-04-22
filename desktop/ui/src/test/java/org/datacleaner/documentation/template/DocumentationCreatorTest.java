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
package org.datacleaner.documentation.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.metamodel.util.FileHelper;
import org.datacleaner.beans.stringpattern.PatternFinderAnalyzer;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.descriptors.Descriptors;
import org.junit.Test;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;

public class DocumentationCreatorTest {

    @Test
    public void test() throws MalformedTemplateNameException, ParseException, IOException {

        final String benchmarkFilename = "src/test/resources/documentCreatorBenchmarkTest.html";
        final DocumentationCreator documentationCreator = new DocumentationCreator();
        final AnalyzerDescriptor<PatternFinderAnalyzer> patternFinderDescriptor = Descriptors
                .ofAnalyzer(PatternFinderAnalyzer.class);

        final File outputFile = new File("src/test/resources/documentationReferenceOutputFile.html");
        documentationCreator.createDocumentation(patternFinderDescriptor, new FileOutputStream(outputFile));
        final File benchmarkFile = new File(benchmarkFilename);
        final String benchmark = FileHelper.readFileAsString(benchmarkFile);
        final String output = FileHelper.readFileAsString(outputFile);
  
        assertEquals(benchmark, output);

        outputFile.delete();
        assertFalse(outputFile.exists()); 
    }
}
