/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.test.scenario;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.html.HtmlAnalysisResultWriter;
import org.eobjects.analyzer.result.html.HtmlFragment;
import org.eobjects.analyzer.result.renderer.HtmlRenderingFormat;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.util.ChangeAwareObjectInputStream;
import org.eobjects.datacleaner.guice.DCModule;
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
        ChangeAwareObjectInputStream is = new ChangeAwareObjectInputStream(new FileInputStream(file));
        AnalysisResult analysisResult = (AnalysisResult) is.readObject();
        is.close();

        RendererFactory rendererFactory = new RendererFactory(configuration);

        List<AnalyzerResult> results = analysisResult.getResults();
        for (AnalyzerResult analyzerResult : results) {
            Renderer<? super AnalyzerResult, ? extends HtmlFragment> renderer = rendererFactory.getRenderer(
                    analyzerResult, HtmlRenderingFormat.class);

            assertNotNull("Did not find a renderer for: " + analyzerResult, renderer);
        }

        Writer fileWriter = new FileWriter("target/testOpenJobWithAllAnalyzers.out.html");

        HtmlAnalysisResultWriter writer = new HtmlAnalysisResultWriter();
        writer.write(analysisResult, configuration, ImmutableRef.of(fileWriter), null);

        fileWriter.close();
    }
}
