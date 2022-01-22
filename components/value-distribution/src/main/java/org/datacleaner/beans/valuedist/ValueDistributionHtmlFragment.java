/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
package org.datacleaner.beans.valuedist;

import java.util.Collection;
import java.util.List;

import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.result.GroupedValueCountingAnalyzerResult;
import org.datacleaner.result.ListResult;
import org.datacleaner.result.ValueCountingAnalyzerResult;
import org.datacleaner.result.ValueFrequency;
import org.datacleaner.result.html.BodyElement;
import org.datacleaner.result.html.DrillToDetailsBodyElement;
import org.datacleaner.result.html.HeadElement;
import org.datacleaner.result.html.HtmlFragment;
import org.datacleaner.result.html.HtmlRenderingContext;
import org.datacleaner.result.html.SimpleHtmlFragment;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.util.LabelUtils;

import com.google.common.escape.Escapers;
import com.google.common.html.HtmlEscapers;

public class ValueDistributionHtmlFragment implements HtmlFragment {

    private final ValueCountingAnalyzerResult _result;
    private final RendererFactory _rendererFactory;
    private final SimpleHtmlFragment _frag;

    public ValueDistributionHtmlFragment(ValueCountingAnalyzerResult result, RendererFactory rendererFactory) {
        _result = result;
        _rendererFactory = rendererFactory;
        _frag = new SimpleHtmlFragment();
    }

    @Override
    public List<BodyElement> getBodyElements() {
        return _frag.getBodyElements();
    }

    @Override
    public List<HeadElement> getHeadElements() {
        return _frag.getHeadElements();
    }

    @Override
    public void initialize(HtmlRenderingContext context) {
        _frag.addHeadElement(new ValueDistributionReusableScriptHeadElement());

        StringBuilder html = new StringBuilder();
        html.append("<div class=\"valueDistributionResultContainer\">");

        if (_result instanceof GroupedValueCountingAnalyzerResult) {
            final Collection<? extends ValueCountingAnalyzerResult> groupedResults =
                    ((GroupedValueCountingAnalyzerResult) _result).getGroupResults();
            for (ValueCountingAnalyzerResult r : groupedResults) {
                html.append(renderResult(r, context, true));
            }
        } else {
            html.append(renderResult(_result, context, false));
        }
        html.append("</div>");

        _frag.addBodyElement(html.toString());
    }

    private String renderResult(ValueCountingAnalyzerResult result, HtmlRenderingContext context, boolean group) {
        final String chartElementId = context.createElementId();

        final Collection<ValueFrequency> valueCounts = result.getReducedValueFrequencies(32);

        _frag.addHeadElement(new ValueDistributionChartScriptHeadElement(valueCounts, chartElementId));

        final int numBars = valueCounts.size();
        final int barHeight = numBars < 20 ? 40 : numBars < 30 ? 30 : 20;
        final int height = numBars * barHeight;
        final String style = "height: " + height + "px;";

        final StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"valueDistributionGroupPanel\">");
        if (group && result.getName() != null) {
            sb.append("<h3>Group: " + result.getName() + "</h3>");
        }

        sb.append("<div class=\"valueDistributionChart\" style=\"" + style + "\" id=\"" + chartElementId + "\">");
        sb.append("</div>");
        if (!valueCounts.isEmpty()) {
            sb.append("<table class=\"valueDistributionValueTable\">");
            for (ValueFrequency valueFreq : valueCounts) {
                sb.append("<tr><td>");
                sb.append(HtmlEscapers.htmlEscaper().escape(valueFreq.getName()));
                sb.append("</td><td>");
                sb.append(getCount(result, valueFreq, context));
                sb.append("</td></tr>");
            }
            sb.append("</table>");
        }
        sb.append("<table class=\"valueDistributionSummaryTable\">");
        sb.append("<tr><td>Total count</td><td>" + result.getTotalCount() + "</td></tr>");
        if (result.getDistinctCount() != null) {
            sb.append("<tr><td>Distinct count</td><td>" + result.getDistinctCount() + "</td></tr>");
        }
        sb.append("</table>");
        sb.append("</div>");
        return sb.toString();
    }

    private String getCount(ValueCountingAnalyzerResult result, ValueFrequency valueFreq,
            HtmlRenderingContext context) {
        final int count = valueFreq.getCount();
        if (count == 0) {
            return "<span>" + count + "</span>";
        }

        if (valueFreq.isComposite()) {
            if (LabelUtils.UNIQUE_LABEL.equals(valueFreq.getName())) {
                final Collection<String> uniqueValues = result.getUniqueValues();
                if (uniqueValues != null && !uniqueValues.isEmpty()) {
                    final String elementId = context.createElementId();
                    final ListResult<String> listResult = new ListResult<String>(uniqueValues);

                    final DrillToDetailsBodyElement bodyElement =
                            new DrillToDetailsBodyElement(elementId, _rendererFactory, listResult);
                    _frag.addBodyElement(bodyElement);

                    final String invocation = bodyElement.toJavaScriptInvocation();

                    return "<a class=\"drillToDetailsLink\" onclick=\"" + invocation + "\" href=\"#\">" + count
                            + "</a>";
                }
            }

            return "<span>" + count + "</span>";
        }

        final String value = valueFreq.getValue();

        final AnnotatedRowsResult annotatedRowsResult = result.getAnnotatedRowsForValue(value);
        if (annotatedRowsResult == null || annotatedRowsResult.getAnnotatedRowCount() == 0) {
            return "<span>" + count + "</span>";
        }

        final String elementId = context.createElementId();

        final DrillToDetailsBodyElement bodyElement =
                new DrillToDetailsBodyElement(elementId, _rendererFactory, annotatedRowsResult);
        _frag.addBodyElement(bodyElement);

        final String invocation = bodyElement.toJavaScriptInvocation();

        return "<a class=\"drillToDetailsLink\" onclick=\"" + invocation + "\" href=\"#\">" + count + "</a>";
    }
}
