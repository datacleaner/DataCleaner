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
