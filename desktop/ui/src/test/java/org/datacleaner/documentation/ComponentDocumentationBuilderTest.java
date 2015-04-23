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
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.metamodel.util.FileHelper;
import org.datacleaner.beans.stringpattern.PatternFinderAnalyzer;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.documentation.ComponentDocumentationBuilder;
import org.junit.Test;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;

public class ComponentDocumentationBuilderTest {

    @Test
    public void testBuildPatternFinderDocs() throws MalformedTemplateNameException, ParseException, IOException {
        final File benchmarkFile = new File("src/test/resources/documentation/pattern_finder.html");
        final File outputFile = new File("target/documentation_pattern_finder.html");

        final AnalyzerDescriptor<PatternFinderAnalyzer> patternFinderDescriptor = Descriptors
                .ofAnalyzer(PatternFinderAnalyzer.class);

        final ComponentDocumentationBuilder documentationCreator = new ComponentDocumentationBuilder();
        documentationCreator.createDocumentation(patternFinderDescriptor, new FileOutputStream(outputFile));

        final String benchmark = FileHelper.readFileAsString(benchmarkFile);
        final String output = FileHelper.readFileAsString(outputFile);

        assertEquals(benchmark, output);

        outputFile.delete();
        assertFalse(outputFile.exists());
    }
}
