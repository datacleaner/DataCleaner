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

import java.io.File;

import org.apache.metamodel.util.FileHelper;
import org.datacleaner.beans.stringpattern.PatternFinderAnalyzer;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.descriptors.Descriptors;
import org.junit.Test;

public class DocumentationLoaderTest {

    @Test
    public void test() {
        
        String benchmarkFilename = "src/test/resources/documentLoaderTest.txt";
        DocumentationLoader documentationLoader = new DocumentationLoader();

        final AnalyzerDescriptor<PatternFinderAnalyzer> patternFinderDescriptor = Descriptors
                .ofAnalyzer(PatternFinderAnalyzer.class);

        documentationLoader.createDocumentation(patternFinderDescriptor);
        
        final File benchmarkFile = new File(benchmarkFilename);
        final File outputFile= new File(DocumentationLoader.OUTPUT_FILENAME); 

        final String benchmark = FileHelper.readFileAsString(benchmarkFile);
        final String output = FileHelper.readFileAsString(outputFile);
        
        assertEquals(benchmark, output);

    }

}
