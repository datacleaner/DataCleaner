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
package org.datacleaner.beans.stringpattern;

import java.io.File;

import junit.framework.TestCase;

import org.datacleaner.api.InputColumn;
import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.result.html.DefaultHtmlRenderingContext;
import org.datacleaner.result.html.HtmlFragment;
import org.datacleaner.result.html.HtmlRenderingContext;
import org.datacleaner.result.renderer.AnnotatedRowsHtmlRenderer;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.storage.InMemoryRowAnnotationFactory;
import org.apache.metamodel.util.FileHelper;

public class PatternFinderResultHtmlRendererTest extends TestCase {

    private final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
    private final AnalyzerBeansConfigurationImpl conf = new AnalyzerBeansConfigurationImpl()
            .replace(descriptorProvider);
    private final RendererFactory rendererFactory = new RendererFactory(conf);
    private HtmlRenderingContext context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        descriptorProvider.addRendererBeanDescriptor(Descriptors.ofRenderer(AnnotatedRowsHtmlRenderer.class));
        context = new DefaultHtmlRenderingContext();
    }
    
    public void testNoPatterns() throws Exception {
        InputColumn<String> col1 = new MockInputColumn<String>("email username", String.class);

        PatternFinderAnalyzer analyzer = new PatternFinderAnalyzer();
        analyzer.setColumn(col1);
        analyzer.setRowAnnotationFactory(new InMemoryRowAnnotationFactory());
        analyzer.init();
        
        PatternFinderResult result = analyzer.getResult();

        HtmlFragment htmlFragment = new PatternFinderResultHtmlRenderer(rendererFactory).render(result);
        htmlFragment.initialize(context);
        assertEquals(0, htmlFragment.getHeadElements().size());
        assertEquals(1, htmlFragment.getBodyElements().size());

        String html = htmlFragment.getBodyElements().get(0).toHtml(context);
        assertEquals(FileHelper.readFileAsString(new File(
                "src/test/resources/pattern_finder_result_html_renderer_empty.html")), html);
    }

    public void testSinglePatterns() throws Exception {
        InputColumn<String> col1 = new MockInputColumn<String>("email username", String.class);

        PatternFinderAnalyzer analyzer = new PatternFinderAnalyzer();
        analyzer.setColumn(col1);
        analyzer.setRowAnnotationFactory(new InMemoryRowAnnotationFactory());
        analyzer.init();

        analyzer.run(new MockInputRow().put(col1, "kasper"), 1);
        analyzer.run(new MockInputRow().put(col1, "kasper.sorensen"), 1);
        analyzer.run(new MockInputRow().put(col1, "info"), 1);
        analyzer.run(new MockInputRow().put(col1, "kasper.sorensen"), 1);
        analyzer.run(new MockInputRow().put(col1, "winfried.vanholland"), 1);
        analyzer.run(new MockInputRow().put(col1, "kaspers"), 1);

        PatternFinderResult result = analyzer.getResult();

        HtmlFragment htmlFragment = new PatternFinderResultHtmlRenderer(rendererFactory).render(result);
        htmlFragment.initialize(context);
        assertEquals(0, htmlFragment.getHeadElements().size());
        assertEquals(1, htmlFragment.getBodyElements().size());

        String html = htmlFragment.getBodyElements().get(0).toHtml(context);
        assertEquals(FileHelper.readFileAsString(new File(
                "src/test/resources/pattern_finder_result_html_renderer_single.html")), html);
    }

    public void testMultiplePatterns() throws Exception {
        InputColumn<String> col1 = new MockInputColumn<String>("email username", String.class);
        InputColumn<String> col2 = new MockInputColumn<String>("email domain", String.class);

        PatternFinderAnalyzer analyzer = new PatternFinderAnalyzer();
        analyzer.setColumn(col1);
        analyzer.setGroupColumn(col2);
        analyzer.setRowAnnotationFactory(new InMemoryRowAnnotationFactory());
        analyzer.init();

        analyzer.run(new MockInputRow().put(col1, "kasper").put(col2, "eobjects.dk"), 1);
        analyzer.run(new MockInputRow().put(col1, "kasper.sorensen").put(col2, "eobjects.dk"), 1);
        analyzer.run(new MockInputRow().put(col1, "info").put(col2, "eobjects.dk"), 1);
        analyzer.run(new MockInputRow().put(col1, "kasper.sorensen").put(col2, "humaninference.com"), 1);
        analyzer.run(new MockInputRow().put(col1, "winfried.vanholland").put(col2, "humaninference.com"), 1);
        analyzer.run(new MockInputRow().put(col1, "kaspers").put(col2, "humaninference.com"), 1);

        PatternFinderResult result = analyzer.getResult();

        HtmlFragment htmlFragment = new PatternFinderResultHtmlRenderer(rendererFactory).render(result);
        htmlFragment.initialize(context);
        assertEquals(0, htmlFragment.getHeadElements().size());
        assertEquals(1, htmlFragment.getBodyElements().size());

        String html = htmlFragment.getBodyElements().get(0).toHtml(context);
        assertEquals(FileHelper.readFileAsString(new File(
                "src/test/resources/pattern_finder_result_html_renderer_multiple.html")), html);
    }
}
