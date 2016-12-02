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
package org.datacleaner.result.html;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.metamodel.util.Predicate;
import org.apache.metamodel.util.Ref;
import org.apache.metamodel.util.TruePredicate;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Renderer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.documentation.ComponentDocumentationWrapper;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.result.AnalysisResultWriter;
import org.datacleaner.result.renderer.HtmlRenderingFormat;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.util.ComponentJobComparator;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AnalysisResultWriter} which writes an analysis result as a HTML page.
 */
public class HtmlAnalysisResultWriter implements AnalysisResultWriter {

    private static final Logger logger = LoggerFactory.getLogger(HtmlAnalysisResultWriter.class);

    private final boolean _tabs;
    private final boolean _headers;
    private final Predicate<Entry<ComponentJob, AnalyzerResult>> _jobInclusionPredicate;

    public HtmlAnalysisResultWriter() {
        this(true);
    }

    public HtmlAnalysisResultWriter(final boolean tabs) {
        this(tabs, new TruePredicate<>());
    }

    public HtmlAnalysisResultWriter(final boolean tabs,
            final Predicate<Entry<ComponentJob, AnalyzerResult>> jobInclusionPredicate) {
        this(tabs, jobInclusionPredicate, true);
    }

    public HtmlAnalysisResultWriter(final boolean tabs,
            final Predicate<Entry<ComponentJob, AnalyzerResult>> jobInclusionPredicate, final boolean headers) {
        _tabs = tabs;
        _jobInclusionPredicate = jobInclusionPredicate;
        _headers = headers;
    }

    @Override
    public void write(final AnalysisResult result, final DataCleanerConfiguration configuration,
            final Ref<Writer> writerRef, final Ref<OutputStream> outputStreamRef) throws IOException {
        final Writer writer = writerRef.get();
        write(result, configuration, writer);
    }

    public void write(final AnalysisResult result, final DataCleanerConfiguration configuration, final Writer writer)
            throws IOException {
        final HtmlRenderingContext context = new DefaultHtmlRenderingContext();

        final RendererFactory rendererFactory = new RendererFactory(configuration);
        final Map<ComponentJob, HtmlFragment> htmlFragments = new LinkedHashMap<>();
        final Map<ComponentJob, AnalyzerResult> resultMap = new TreeMap<>(new ComponentJobComparator());
        resultMap.putAll(result.getResultMap());

        for (final Entry<ComponentJob, AnalyzerResult> entry : resultMap.entrySet()) {
            final ComponentJob componentJob = entry.getKey();
            final AnalyzerResult analyzerResult = entry.getValue();

            if (_jobInclusionPredicate.eval(entry)) {
                final Renderer<? super AnalyzerResult, ? extends HtmlFragment> renderer =
                        rendererFactory.getRenderer(analyzerResult, HtmlRenderingFormat.class);
                if (renderer == null) {
                    throw new IllegalStateException("No HTML renderer found for result: " + analyzerResult);
                }

                final HtmlRenderingContext localContext = new ComponentHtmlRenderingContext(context, componentJob);

                try {
                    final HtmlFragment htmlFragment = renderer.render(analyzerResult);
                    htmlFragment.initialize(localContext);
                    htmlFragments.put(componentJob, htmlFragment);
                } catch (final Exception e) {
                    logger.error("Error while rendering analyzer result: " + analyzerResult, e);
                    writeRenderingError(writer, componentJob, analyzerResult, e);
                }
            } else {
                logger.debug("Skipping job {} / result {} because predicate evaluated false", componentJob,
                        analyzerResult);
            }
        }

        writeHtmlBegin(writer, context);
        writeHead(writer, htmlFragments, context);
        writeBody(writer, htmlFragments, context);
        writeHtmlEnd(writer, context);
    }

    private void writeMaterializationError(final Writer writer, final ComponentJob componentJob, final Exception e)
            throws IOException {
        writeGenericError(writer, componentJob, null, e);
    }

    private void writeRenderingError(final Writer writer, final ComponentJob componentJob,
            final AnalyzerResult analyzerResult, final Exception e) throws IOException {
        writeGenericError(writer, componentJob, analyzerResult, e);
    }

    private void writeGenericError(final Writer writer, final ComponentJob componentJob,
            final AnalyzerResult analyzerResult, final Exception e) throws IOException {
        writer.write("<div class=\"error\">");
        writer.write("<p>Component job: " + LabelUtils.getLabel(componentJob) + "</p>");
        if (analyzerResult != null) {
            writer.write("<p>Analyzer result: " + analyzerResult + "</p>");
        }
        writer.write("<pre>");
        final PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        writer.write("</pre>");
        writer.write("</div>");
    }

    protected void writeHtmlBegin(final Writer writer, final HtmlRenderingContext context) throws IOException {
        writer.write("<!DOCTYPE html>\n");
        writer.write("<html>\n");
    }

    protected void writeHtmlEnd(final Writer writer, final HtmlRenderingContext context) throws IOException {
        writer.write("</html>");
    }

    protected void writeHead(final Writer writer, final Map<ComponentJob, HtmlFragment> htmlFragments,
            final HtmlRenderingContext context) throws IOException {
        final Set<HeadElement> allHeadElements = new HashSet<>();

        writeHeadBegin(writer);

        // add base element no matter what
        {
            final HeadElement baseHeadElement = createBaseHeadElement();
            writeHeadElement(writer, null, baseHeadElement, context);
            allHeadElements.add(baseHeadElement);
        }

        for (final Entry<ComponentJob, HtmlFragment> entry : htmlFragments.entrySet()) {
            final HtmlFragment htmlFragment = entry.getValue();
            final List<HeadElement> headElements = htmlFragment.getHeadElements();
            for (final HeadElement headElement : headElements) {
                if (!allHeadElements.contains(headElement)) {
                    final ComponentJob componentJob = entry.getKey();
                    writeHeadElement(writer, componentJob, headElement, context);
                    allHeadElements.add(headElement);
                }
            }
        }

        writeHeadEnd(writer);
    }

    protected HeadElement createBaseHeadElement() {
        return new BaseHeadElement();
    }

    protected void writeHeadBegin(final Writer writer) throws IOException {
        writer.write("<head>\n");
        writer.write("  <title>Analysis result</title>");
    }

    protected void writeHeadEnd(final Writer writer) throws IOException {
        writer.write("</head>");
    }

    protected void writeHeadElement(final Writer writer, final ComponentJob componentJob, final HeadElement headElement,
            final HtmlRenderingContext context) throws IOException {
        final HtmlRenderingContext localContext = new ComponentHtmlRenderingContext(context, componentJob);

        writer.write("  ");
        try {
            final String html = headElement.toHtml(localContext);
            writer.write(html);
        } catch (final Exception e) {
            writeMaterializationError(writer, componentJob, e);
        }
        writer.write('\n');
    }

    protected void writeBody(final Writer writer, final Map<ComponentJob, HtmlFragment> htmlFragments,
            final HtmlRenderingContext context) throws IOException {
        final Set<Entry<ComponentJob, HtmlFragment>> htmlFragmentSet = htmlFragments.entrySet();

        writeBodyBegin(writer, context);

        writer.write("<div class=\"analysisResultHeader\">");

        if (_tabs) {
            // write a <ul> with all descriptors in it (a TOC)
            {
                writer.write("<ul class=\"analysisResultToc\">");
                ComponentDescriptor<?> lastDescriptor = null;
                for (final Entry<ComponentJob, HtmlFragment> entry : htmlFragmentSet) {
                    final ComponentJob componentJob = entry.getKey();
                    final ComponentDescriptor<?> descriptor = componentJob.getDescriptor();
                    if (!descriptor.equals(lastDescriptor)) {
                        final ComponentDocumentationWrapper wrapper = new ComponentDocumentationWrapper(descriptor);
                        final String iconSrc = wrapper.getIconSrc(IconUtils.ICON_SIZE_MEDIUM);
                        final String styleName = toStyleName(descriptor.getDisplayName());
                        writer.write("<li style=\"background-image: url(" + iconSrc
                                + ")\"><a href=\"#analysisResultDescriptorGroup_" + styleName + "\">");
                        writer.write(context.escapeHtml(descriptor.getDisplayName()));
                        writer.write("</a></li>");

                        lastDescriptor = descriptor;
                    }
                }
                writer.write("</ul>");
            }
        }

        writer.write("</div>");

        // write all descriptor groups
        {
            boolean descriptorGroupBegin = false;
            ComponentDescriptor<?> lastDescriptor = null;
            for (final Entry<ComponentJob, HtmlFragment> entry : htmlFragmentSet) {
                final ComponentJob componentJob = entry.getKey();
                final ComponentDescriptor<?> descriptor = componentJob.getDescriptor();
                final HtmlFragment htmlFragment = entry.getValue();

                if (!descriptor.equals(lastDescriptor)) {
                    if (descriptorGroupBegin) {
                        writer.write("</div>\n");
                    }

                    final String styleName = toStyleName(descriptor.getDisplayName());
                    writer.write("<div id=\"analysisResultDescriptorGroup_" + styleName
                            + "\" class=\"analysisResultDescriptorGroup " + toStyleName(descriptor.getDisplayName())
                            + "\">");

                    lastDescriptor = descriptor;
                    descriptorGroupBegin = true;
                }

                writeBodyHtmlFragment(writer, componentJob, htmlFragment, context);
            }

            if (descriptorGroupBegin) {
                writer.write("</div>\n");
            }
        }

        writeBodyEnd(writer, context);
    }

    protected void writeBodyBegin(final Writer writer, final HtmlRenderingContext context) throws IOException {
        writer.write("<body>\n");
        writer.write("<div class=\"analysisResultContainer\">\n");
    }

    protected void writeBodyEnd(final Writer writer, final HtmlRenderingContext context) throws IOException {
        writer.write("</div>\n");
        writer.write("</body>");
    }

    protected void writeBodyHtmlFragment(final Writer writer, final ComponentJob componentJob,
            final HtmlFragment htmlFragment, final HtmlRenderingContext context) throws IOException {
        final String displayName = componentJob.getDescriptor().getDisplayName();
        final String styleName = toStyleName(displayName);

        writer.write("<div class=\"analyzerResult " + styleName + "\">");
        if (_headers) {
            writeHeader(writer, componentJob, context, htmlFragment);
        }
        writer.write("<div class=\"analyzerResultContent\">\n");

        final List<BodyElement> bodyElements = htmlFragment.getBodyElements();
        for (final BodyElement bodyElement : bodyElements) {
            writeBodyElement(writer, componentJob, htmlFragment, bodyElement, context);
        }

        writer.write("</div>");
        writer.write("<div class=\"analyzerResultFooter\"></div>");
        writer.write("</div>\n");
    }

    protected void writeHeader(final Writer writer, final ComponentJob componentJob, final HtmlRenderingContext context,
            final HtmlFragment htmlFragment) throws IOException {
        final String label = LabelUtils.getLabel(componentJob);
        writer.write("<div class=\"analyzerResultHeader\">");
        writer.write("<h2>" + context.escapeHtml(label) + "</h2>");
        writer.write("</div>");
    }

    protected String toStyleName(final String displayName) {
        final String camelCase = StringUtils.toCamelCase(displayName);
        return camelCase.replaceAll("/", "_").replaceAll("&", "_");
    }

    protected void writeBodyElement(final Writer writer, final ComponentJob componentJob,
            final HtmlFragment htmlFragment, final BodyElement bodyElement, final HtmlRenderingContext context)
            throws IOException {
        final HtmlRenderingContext localContext = new ComponentHtmlRenderingContext(context, componentJob);

        writer.write("  ");
        try {
            final String html = bodyElement.toHtml(localContext);
            writer.write(html);
        } catch (final Exception e) {
            writeMaterializationError(writer, componentJob, e);
        }
        writer.write('\n');
    }
}
