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

import org.datacleaner.beans.api.Provided;
import org.datacleaner.beans.api.RendererBean;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabResult;
import org.datacleaner.result.html.HtmlFragment;

@RendererBean(HtmlRenderingFormat.class)
public class CrosstabHtmlRenderer extends AbstractRenderer<CrosstabResult, HtmlFragment> {

    @Inject
    @Provided
    RendererFactory rendererFactory;

    public CrosstabHtmlRenderer() {
        this(null);
    }

    public CrosstabHtmlRenderer(RendererFactory rendererFactory) {
        this.rendererFactory = rendererFactory;
    }

    @Override
    public HtmlFragment render(CrosstabResult result) {
        return render(result.getCrosstab());
    }

    public HtmlFragment render(Crosstab<?> crosstab) {
        return new CrosstabHtmlFragment(crosstab, rendererFactory);

    }
}
