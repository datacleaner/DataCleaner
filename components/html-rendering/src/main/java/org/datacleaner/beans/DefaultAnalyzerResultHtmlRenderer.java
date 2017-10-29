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
package org.datacleaner.beans;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererBean;
import org.datacleaner.api.RendererPrecedence;
import org.datacleaner.result.html.HtmlFragment;
import org.datacleaner.result.html.SimpleHtmlFragment;
import org.datacleaner.result.renderer.HtmlRenderingFormat;

/**
 * The default HTML renderer for any AnalyzerResult.
 */
@RendererBean(HtmlRenderingFormat.class)
public class DefaultAnalyzerResultHtmlRenderer implements Renderer<AnalyzerResult, HtmlFragment> {

    @Override
    public RendererPrecedence getPrecedence(AnalyzerResult renderable) {
        return RendererPrecedence.LOWEST;
    }

    @Override
    public HtmlFragment render(AnalyzerResult result) {
        final SimpleHtmlFragment frag = new SimpleHtmlFragment();
        frag.addBodyElement(new MetricListBodyElement(result));
        return frag;
    }
}
