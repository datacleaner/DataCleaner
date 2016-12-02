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

import javax.inject.Inject;

import org.datacleaner.api.Provided;
import org.datacleaner.api.RendererBean;
import org.datacleaner.result.html.HtmlFragment;
import org.datacleaner.result.renderer.AbstractRenderer;
import org.datacleaner.result.renderer.HtmlRenderingFormat;
import org.datacleaner.result.renderer.RendererFactory;

@RendererBean(HtmlRenderingFormat.class)
public class PatternFinderResultHtmlRenderer extends AbstractRenderer<PatternFinderResult, HtmlFragment> {

    @Inject
    @Provided
    RendererFactory rendererFactory;

    public PatternFinderResultHtmlRenderer() {
        this(null);
    }

    public PatternFinderResultHtmlRenderer(final RendererFactory rendererFactory) {
        this.rendererFactory = rendererFactory;
    }

    @Override
    public HtmlFragment render(final PatternFinderResult result) {
        return new PatternFinderHtmlFragment(result, rendererFactory);
    }
}
