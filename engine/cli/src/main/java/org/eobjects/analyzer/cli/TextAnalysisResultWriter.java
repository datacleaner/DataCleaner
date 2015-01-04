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
package org.eobjects.analyzer.cli;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Map.Entry;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.AnalysisResultWriter;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.result.renderer.TextRenderingFormat;
import org.eobjects.analyzer.util.LabelUtils;
import org.apache.metamodel.util.Ref;

public class TextAnalysisResultWriter implements AnalysisResultWriter {

    @Override
    public void write(AnalysisResult result, AnalyzerBeansConfiguration configuration, Ref<Writer> writerRef,
            Ref<OutputStream> outputStreamRef) throws Exception {
        final Writer writer = writerRef.get();
        writer.write("SUCCESS!\n");

        final RendererFactory rendererFactory = new RendererFactory(configuration);
        for (Entry<ComponentJob, AnalyzerResult> entry : result.getResultMap().entrySet()) {
            final ComponentJob componentJob = entry.getKey();
            final AnalyzerResult analyzerResult = entry.getValue();
            final String name = LabelUtils.getLabel(componentJob);

            writer.write("\nRESULT: ");
            writer.write(name);
            writer.write('\n');

            final Renderer<? super AnalyzerResult, ? extends CharSequence> renderer = rendererFactory.getRenderer(
                    analyzerResult, TextRenderingFormat.class);

            if (renderer == null) {
                throw new IllegalStateException("No text renderer found for result: " + analyzerResult);
            }

            CharSequence renderedResult = renderer.render(analyzerResult);
            writer.write(renderedResult.toString());
            writer.write('\n');
        }
    }

}
