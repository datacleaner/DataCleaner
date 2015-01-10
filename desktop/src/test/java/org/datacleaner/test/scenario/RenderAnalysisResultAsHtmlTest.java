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
package org.datacleaner.test.scenario;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.util.List;

import junit.framework.TestCase;

import org.datacleaner.beans.api.Renderer;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.result.AnalyzerResult;
import org.datacleaner.result.html.HtmlAnalysisResultWriter;
import org.datacleaner.result.html.HtmlFragment;
import org.datacleaner.result.renderer.HtmlRenderingFormat;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.util.ChangeAwareObjectInputStream;
import org.datacleaner.guice.DCModule;
import org.apache.metamodel.util.ImmutableRef;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class RenderAnalysisResultAsHtmlTest extends TestCase {

    /**
     * A very broad integration test which opens an analysis result with (more
     * or less) all built-in analyzer results and verifies that there is a
     * renderer for each of them
     * 
     * @throws Exception
     */
    public void testOpenJobWithAllAnalyzers() throws Exception {
        DCModule module = new DCModule();
        Injector injector = Guice.createInjector(module);
        AnalyzerBeansConfiguration configuration = injector.getInstance(AnalyzerBeansConfiguration.class);

        File file = new File("src/test/resources/all_analyzers.analysis.result.dat");
        AnalysisResult analysisResult;
        try (ChangeAwareObjectInputStream is = new ChangeAwareObjectInputStream(new FileInputStream(file))) {
            is.addRenamedPackage("org.datacleaner.output.beans", "org.datacleaner.extension.output");
            analysisResult = (AnalysisResult) is.readObject();
        }

        RendererFactory rendererFactory = new RendererFactory(configuration);

        List<AnalyzerResult> results = analysisResult.getResults();
        for (AnalyzerResult analyzerResult : results) {
            Renderer<? super AnalyzerResult, ? extends HtmlFragment> renderer = rendererFactory.getRenderer(
                    analyzerResult, HtmlRenderingFormat.class);

            assertNotNull("Did not find a renderer for: " + analyzerResult, renderer);
        }

        try (Writer fileWriter = new FileWriter("target/testOpenJobWithAllAnalyzers.out.html")) {
            HtmlAnalysisResultWriter writer = new HtmlAnalysisResultWriter();
            writer.write(analysisResult, configuration, ImmutableRef.of(fileWriter), null);
        }
    }
}
