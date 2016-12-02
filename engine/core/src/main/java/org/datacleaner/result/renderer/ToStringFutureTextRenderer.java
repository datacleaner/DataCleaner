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
package org.datacleaner.result.renderer;

import javax.inject.Inject;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.AnalyzerResultFuture;
import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererBean;
import org.datacleaner.api.RendererPrecedence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A renderer for analyzers returning {@link AnalyzerFutureResult}. Waits for the Future to
 * finish processing and renders the wrapped {@link AnalyzerResult}.
 */
@RendererBean(TextRenderingFormat.class)
public class ToStringFutureTextRenderer implements Renderer<AnalyzerResultFuture<? extends AnalyzerResult>, String> {

    Logger logger = LoggerFactory.getLogger(ToStringFutureTextRenderer.class);

    @Inject
    RendererFactory _rendererFactory;

    @Override
    public RendererPrecedence getPrecedence(final AnalyzerResultFuture<? extends AnalyzerResult> renderable) {
        return RendererPrecedence.MEDIUM;
    }

    @Override
    public String render(final AnalyzerResultFuture<? extends AnalyzerResult> renderable) {
        final StringBuilder resultString = new StringBuilder();

        try {
            final AnalyzerResult result = renderable.get();

            final Renderer<? super AnalyzerResult, ? extends CharSequence> renderer =
                    _rendererFactory.getRenderer(result, TextRenderingFormat.class);
            if (renderer != null) {
                logger.debug("renderer.render({})", result);
                final CharSequence component = renderer.render(result);
                resultString.append(component);
                return resultString.toString();
            } else {
                final String message = "No renderer found for result type " + result.getClass().getName();
                logger.error(message);
                throw new IllegalStateException(message);
            }
        } catch (final RuntimeException error) {
            final String message = "Unable to fetch result";
            logger.error(message, error);
            throw new IllegalStateException(message, error);
        }
    }

}
