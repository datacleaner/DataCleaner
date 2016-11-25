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
package org.datacleaner.test.mock;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Renderable;
import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererBean;
import org.datacleaner.api.RendererPrecedence;
import org.datacleaner.api.RenderingFormat;
import org.datacleaner.result.renderer.TextRenderingFormat;

public class MockRenderers {

    @RendererBean(TextRenderingFormat.class)
    public interface InvalidRenderer3 extends Renderer<AnalyzerResult, Integer> {
    }

    public static class RenderableString implements Renderable {
        private final String str;

        public RenderableString(final String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @RendererBean(TextRenderingFormat.class)
    public static class FooPrecedenceRenderer implements Renderer<RenderableString, String> {
        @Override
        public RendererPrecedence getPrecedence(final RenderableString renderable) {
            if (renderable.toString().equals("foo")) {
                return RendererPrecedence.HIGHEST;
            }
            return RendererPrecedence.HIGH;
        }

        @Override
        public String render(final RenderableString renderable) {
            return "high";
        }
    }

    @RendererBean(TextRenderingFormat.class)
    public static class BarPrecedenceRenderer implements Renderer<RenderableString, String> {
        @Override
        public RendererPrecedence getPrecedence(final RenderableString renderable) {
            if (renderable.toString().equals("bar")) {
                return RendererPrecedence.HIGHEST;
            }
            return RendererPrecedence.LOW;
        }

        @Override
        public String render(final RenderableString renderable) {
            return "low";
        }
    }

    @RendererBean(TextRenderingFormat.class)
    public static class ConditionalPrecedenceRenderer implements Renderer<RenderableString, String> {

        @Override
        public RendererPrecedence getPrecedence(final RenderableString renderable) {
            // renderable's toString() method should return the name of the
            // precedence (can also be used for testing exceptions in
            // resolving).
            return RendererPrecedence.valueOf(renderable.toString());
        }

        @Override
        public String render(final RenderableString renderable) {
            return "low";
        }
    }

    @RendererBean(TextRenderingFormat.class)
    public static class InvalidRenderer1 implements Renderer<AnalyzerResult, Object> {

        @Override
        public RendererPrecedence getPrecedence(final AnalyzerResult renderable) {
            return RendererPrecedence.MEDIUM;
        }

        @Override
        public Object render(final AnalyzerResult result) {
            return null;
        }
    }

    @RendererBean(InvalidRenderingFormat.class)
    public static class InvalidRenderer4 implements Renderer<AnalyzerResult, Integer> {

        @Override
        public RendererPrecedence getPrecedence(final AnalyzerResult renderable) {
            return RendererPrecedence.MEDIUM;
        }

        @Override
        public Integer render(final AnalyzerResult result) {
            return null;
        }
    }

    public static class InvalidRenderer2 implements Renderer<AnalyzerResult, String> {
        @Override
        public RendererPrecedence getPrecedence(final AnalyzerResult renderable) {
            return RendererPrecedence.MEDIUM;
        }

        @Override
        public String render(final AnalyzerResult result) {
            return null;
        }
    }

    public abstract static class InvalidRenderingFormat implements RenderingFormat<Number> {

        @Override
        public Class<Number> getOutputClass() {
            return Number.class;
        }
    }
}
