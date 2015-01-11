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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.datacleaner.result.Crosstab;
import org.datacleaner.result.html.BodyElement;
import org.datacleaner.result.html.HeadElement;
import org.datacleaner.result.html.HtmlFragment;
import org.datacleaner.result.html.HtmlRenderingContext;
import org.datacleaner.result.html.SimpleHtmlFragment;
import org.datacleaner.result.renderer.CrosstabHtmlRenderer;
import org.datacleaner.result.renderer.RendererFactory;

/**
 * The {@link HtmlFragment} created by a HTML rendering of a
 * {@link PatternFinderResult}
 */
class PatternFinderHtmlFragment implements HtmlFragment {

    private final PatternFinderResult _result;
    private final RendererFactory _rendererFactory;
    private final SimpleHtmlFragment _htmlFragment;

    public PatternFinderHtmlFragment(PatternFinderResult result, RendererFactory rendererFactory) {
        _result = result;
        _rendererFactory = rendererFactory;
        _htmlFragment = new SimpleHtmlFragment();
    }

    @Override
    public void initialize(HtmlRenderingContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"patternFinderResultContainer\">");

        if (_result.isGroupingEnabled()) {
            Map<String, Crosstab<?>> crosstabs = _result.getGroupedCrosstabs();
            if (crosstabs.isEmpty()) {
                _htmlFragment.addBodyElement("<p>No patterns found</p>");
                return;
            }
            Set<Entry<String, Crosstab<?>>> crosstabEntries = crosstabs.entrySet();
            for (Entry<String, Crosstab<?>> entry : crosstabEntries) {
                String group = entry.getKey();
                Crosstab<?> crosstab = entry.getValue();
                if (sb.length() != 0) {
                    sb.append("\n");
                }

                sb.append("<h3>Patterns for group: ");
                sb.append(context.escapeHtml(group));
                sb.append("</h3>");
                sb.append("<div class=\"patternFinderResultPanel\">");
                append(sb, crosstab, context);
                sb.append("</div>");
            }
        } else {
            Crosstab<?> crosstab = _result.getSingleCrosstab();
            sb.append("<div class=\"patternFinderResultPanel\">");
            append(sb, crosstab, context);
            sb.append("</div>");
        }
        sb.append("</div>");
        _htmlFragment.addBodyElement(sb.toString());
    }

    private void append(StringBuilder sb, Crosstab<?> crosstab, HtmlRenderingContext context) {
        final CrosstabHtmlRenderer crosstabHtmlRenderer = new CrosstabHtmlRenderer(_rendererFactory);

        final HtmlFragment renderedResult = crosstabHtmlRenderer.render(crosstab);
        renderedResult.initialize(context);

        final List<BodyElement> bodyElements = renderedResult.getBodyElements();
        for (BodyElement bodyElement : bodyElements) {
            sb.append(bodyElement.toHtml(context));
        }

        final List<HeadElement> headElements = renderedResult.getHeadElements();
        for (HeadElement headElement : headElements) {
            _htmlFragment.addHeadElement(headElement);
        }
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
