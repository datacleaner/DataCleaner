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

import org.datacleaner.beans.api.Renderer;
import org.datacleaner.beans.api.RendererBean;
import org.datacleaner.beans.api.RendererPrecedence;
import org.datacleaner.beans.api.RenderingFormat;
import org.datacleaner.result.AnalyzerResult;
import org.datacleaner.result.renderer.Renderable;
import org.datacleaner.result.renderer.TextRenderingFormat;

public class MockRenderers {

	public static class RenderableString implements Renderable {
		private final String str;

		public RenderableString(String str) {
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
		public RendererPrecedence getPrecedence(RenderableString renderable) {
			if (renderable.toString().equals("foo")) {
				return RendererPrecedence.HIGHEST;
			}
			return RendererPrecedence.HIGH;
		}

		@Override
		public String render(RenderableString renderable) {
			return "high";
		}
	}

	@RendererBean(TextRenderingFormat.class)
	public static class BarPrecedenceRenderer implements Renderer<RenderableString, String> {
		@Override
		public RendererPrecedence getPrecedence(RenderableString renderable) {
			if (renderable.toString().equals("bar")) {
				return RendererPrecedence.HIGHEST;
			}
			return RendererPrecedence.LOW;
		}

		@Override
		public String render(RenderableString renderable) {
			return "low";
		}
	}

	@RendererBean(TextRenderingFormat.class)
	public static class ConditionalPrecedenceRenderer implements Renderer<RenderableString, String> {

		@Override
		public RendererPrecedence getPrecedence(RenderableString renderable) {
			// renderable's toString() method should return the name of the
			// precedence (can also be used for testing exceptions in
			// resolving).
			return RendererPrecedence.valueOf(renderable.toString());
		}

		@Override
		public String render(RenderableString renderable) {
			return "low";
		}
	}

	@RendererBean(TextRenderingFormat.class)
	public static class InvalidRenderer1 implements Renderer<AnalyzerResult, Object> {

		@Override
		public RendererPrecedence getPrecedence(AnalyzerResult renderable) {
			return RendererPrecedence.MEDIUM;
		}

		@Override
		public Object render(AnalyzerResult result) {
			return null;
		}
	}

	@RendererBean(TextRenderingFormat.class)
	public static interface InvalidRenderer3 extends Renderer<AnalyzerResult, Integer> {
	}

	@RendererBean(InvalidRenderingFormat.class)
	public static class InvalidRenderer4 implements Renderer<AnalyzerResult, Integer> {

		@Override
		public RendererPrecedence getPrecedence(AnalyzerResult renderable) {
			return RendererPrecedence.MEDIUM;
		}

		@Override
		public Integer render(AnalyzerResult result) {
			return null;
		}
	}

	public static class InvalidRenderer2 implements Renderer<AnalyzerResult, String> {
		@Override
		public RendererPrecedence getPrecedence(AnalyzerResult renderable) {
			return RendererPrecedence.MEDIUM;
		}

		@Override
		public String render(AnalyzerResult result) {
			return null;
		}
	}

	public static abstract class InvalidRenderingFormat implements RenderingFormat<Number> {

		@Override
		public Class<Number> getOutputClass() {
			return Number.class;
		}
	}
}
