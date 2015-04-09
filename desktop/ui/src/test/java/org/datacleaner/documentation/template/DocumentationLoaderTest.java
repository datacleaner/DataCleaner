package org.datacleaner.documentation.template;

import org.datacleaner.api.Analyzer;
import org.datacleaner.beans.stringpattern.PatternFinder;
import org.datacleaner.beans.stringpattern.PatternFinderAnalyzer;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.Descriptors;
import org.easymock.EasyMock;
import org.junit.Test;

public class DocumentationLoaderTest {

    @Test
    public void test() {
        DocumentationLoader documentationLoader = new DocumentationLoader();

        final AnalyzerDescriptor<PatternFinderAnalyzer> patternFinderDescriptor = Descriptors
                .ofAnalyzer(PatternFinderAnalyzer.class);

        documentationLoader.createDocumentation(patternFinderDescriptor);
    }

}
