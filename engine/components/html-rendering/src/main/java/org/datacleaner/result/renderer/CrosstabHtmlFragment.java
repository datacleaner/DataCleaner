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

import java.util.List;

import org.datacleaner.result.Crosstab;
import org.datacleaner.result.html.BodyElement;
import org.datacleaner.result.html.HeadElement;
import org.datacleaner.result.html.HtmlFragment;
import org.datacleaner.result.html.HtmlRenderingContext;

public class CrosstabHtmlFragment implements HtmlFragment {

    private final Crosstab<?> _crosstab;
    private final RendererFactory _rendererFactory;
    private HtmlFragment _htmlFragment;

    public CrosstabHtmlFragment(Crosstab<?> crosstab, RendererFactory rendererFactory) {
        _crosstab = crosstab;
        _rendererFactory = rendererFactory;
    }

    @Override
    public void initialize(HtmlRenderingContext context) {
        CrosstabRenderer crosstabRenderer = new CrosstabRenderer(_crosstab);
        HtmlFragment htmlFragment = crosstabRenderer.render(new HtmlCrosstabRendererCallback(_rendererFactory,
                context));
        _htmlFragment = htmlFragment;
    }

    @Override
    public List<HeadElement> getHeadElements() {
        return _htmlFragment.getHeadElements();
    }

    @Override
    public List<BodyElement> getBodyElements() {
        return _htmlFragment.getBodyElements();
    }

}
