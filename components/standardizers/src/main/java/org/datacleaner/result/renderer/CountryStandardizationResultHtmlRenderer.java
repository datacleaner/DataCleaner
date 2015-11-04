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

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.apache.commons.lang.StringEscapeUtils;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererBean;
import org.datacleaner.api.RendererPrecedence;
import org.datacleaner.beans.standardize.CountryStandardizationResult;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.result.html.DrillToDetailsBodyElement;
import org.datacleaner.result.html.HtmlFragment;
import org.datacleaner.result.html.SimpleHtmlFragment;

@RendererBean(HtmlRenderingFormat.class)
public class CountryStandardizationResultHtmlRenderer implements Renderer<CountryStandardizationResult, HtmlFragment> {

    @Inject
    @Provided
    RendererFactory _rendererFactory;

    @Override
    public RendererPrecedence getPrecedence(CountryStandardizationResult renderable) {
        return RendererPrecedence.HIGH;
    }

    @Override
    public HtmlFragment render(CountryStandardizationResult result) {
        final AtomicInteger _elementCounter = new AtomicInteger(0);
        final SimpleHtmlFragment htmlFragment = new SimpleHtmlFragment();
        final Collection<String> categoryNames = result.getCategoryNames();
        final StringBuilder sb = new StringBuilder();
        sb.append("<div><table class=\"countryStandardization\">");

        for (String category : categoryNames) {

            final Integer categoryCount = result.getCategoryCount(category);
            final AnnotatedRowsResult categoryRowSample = result.getCategoryRowSample(category);
            sb.append("<tr>");
            sb.append("<td>");
            sb.append(StringEscapeUtils.escapeHtml(category));
            sb.append("</td>");
            final DrillToDetailsBodyElement countryFrangment = new DrillToDetailsBodyElement(createElementId(_elementCounter),
                    _rendererFactory, categoryRowSample);
            final String javaScriptInvocation = countryFrangment.toJavaScriptInvocation();
            htmlFragment.addBodyElement(countryFrangment);
            final String link = "<a class=\"drillToDetailsLink\" onclick=\"" + javaScriptInvocation + " \" href=\"#\">"
                    + categoryCount + "</a>";
            sb.append("<td>");
            sb.append(link);
            sb.append("</td>");
            sb.append("</tr>");
        }

        sb.append("</table></div>");
        htmlFragment.addBodyElement(sb.toString());
        return htmlFragment;
    }

    private String createElementId(AtomicInteger elementCounter) {
        return "country_stand_" + elementCounter.incrementAndGet();
    }
}
