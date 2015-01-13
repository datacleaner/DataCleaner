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

import java.text.DecimalFormatSymbols;
import java.util.List;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.result.ResultProducer;
import org.datacleaner.result.html.DrillToDetailsBodyElement;
import org.datacleaner.result.html.HtmlFragment;
import org.datacleaner.result.html.HtmlRenderingContext;
import org.datacleaner.result.html.SimpleHtmlFragment;
import org.datacleaner.util.LabelUtils;

public class HtmlCrosstabRendererCallback implements CrosstabRendererCallback<HtmlFragment> {

    private final StringBuilder sb;
    private final SimpleHtmlFragment htmlFragtment;
    private final RendererFactory rendererFactory;
    private final HtmlRenderingContext htmlRenderingContext;

    private int rowNumber;

    public HtmlCrosstabRendererCallback(RendererFactory rendererFactory, HtmlRenderingContext htmlRenderingContext) {
        this.rendererFactory = rendererFactory;
        this.htmlRenderingContext = htmlRenderingContext;
        this.sb = new StringBuilder();
        this.rowNumber = 0;
        this.htmlFragtment = new SimpleHtmlFragment();
    }

    @Override
    public void beginTable(Crosstab<?> crosstab, List<CrosstabDimension> horizontalDimensions,
            List<CrosstabDimension> verticalDimensions) {
        sb.append("<table class=\"crosstabTable\">");
    }

    @Override
    public void endTable() {
        sb.append("</table>");
    }

    @Override
    public void beginRow() {
        rowNumber++;
        if (rowNumber % 2 == 0) {
            sb.append("<tr class=\"even\">");
        } else {
            sb.append("<tr class=\"odd\">");
        }
    }

    @Override
    public void endRow() {
        sb.append("</tr>");
    }

    @Override
    public void horizontalHeaderCell(String category, CrosstabDimension dimension, int width) {
        if (width <= 0) {
            return;
        }
        if (width > 1) {
            sb.append("<td class=\"crosstabHorizontalHeader\" colspan=\"");
            sb.append(width);
            sb.append("\">");
        } else if (width == 1) {
            sb.append("<td class=\"crosstabHorizontalHeader\">");
        }
        sb.append(toHtml(category));
        sb.append("</td>");
    }

    @Override
    public void verticalHeaderCell(String category, CrosstabDimension dimension, int height) {
        if (height <= 0) {
            return;
        }
        if (height > 1) {
            sb.append("<td class=\"crosstabVerticalHeader\" rowspan=\"");
            sb.append(height);
            sb.append("\">");
        } else if (height == 1) {
            sb.append("<td class=\"crosstabVerticalHeader\">");
        }
        
        sb.append(toHtml(category));
        sb.append("</td>");
    }

    @Override
    public void valueCell(Object value, ResultProducer drillToDetailResultProducer) {
        if (drillToDetailResultProducer == null) {
            simpleValueCell(value);
            return;
        }

        final AnalyzerResult drillResult = drillToDetailResultProducer.getResult();
        if (drillResult == null) {
            simpleValueCell(value);
            return;
        }

        if (drillResult instanceof AnnotatedRowsResult
                && ((AnnotatedRowsResult) drillResult).getAnnotatedRowCount() == 0) {
            simpleValueCell(value);
            return;
        }

        final String drillElementId = htmlRenderingContext.createElementId();

        final DrillToDetailsBodyElement drillBodyElement = new DrillToDetailsBodyElement(drillElementId,
                rendererFactory, drillResult);
        htmlFragtment.addBodyElement(drillBodyElement);

        final String invocation = drillBodyElement.toJavaScriptInvocation();

        sb.append("<td class=\"value\">");
        sb.append("<a class=\"drillToDetailsLink\" href=\"#\" onclick=\"" + invocation + "\">");
        sb.append(toHtml(LabelUtils.getValueLabel(value)));
        sb.append("</a>");
        sb.append("</td>");
    }

    private void simpleValueCell(Object value) {
        sb.append("<td class=\"value\">");
        sb.append(toHtml(value));
        sb.append("</td>");
    }

    public String toHtml(Object value) {
        String valueLabel = LabelUtils.getValueLabel(value);
        valueLabel = htmlRenderingContext.escapeHtml(valueLabel);
        if (value instanceof Number) {
            // mark the decimal point
            DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
            char decimalSeparator = decimalFormatSymbols.getDecimalSeparator();
            int indexOfDecimalSeparator = valueLabel.lastIndexOf(decimalSeparator);
            if (indexOfDecimalSeparator != -1) {
                // add a <span class="decimal"></span> around the decimal part.
                valueLabel = valueLabel.substring(0, indexOfDecimalSeparator) + "<span class=\"decimal\">"
                        + valueLabel.substring(indexOfDecimalSeparator) + "</span>";
            }
        }
        return valueLabel;
    }

    @Override
    public void emptyHeader(CrosstabDimension verticalDimension, CrosstabDimension horizontalDimension) {
        sb.append("<td class=\"empty\"></td>");
    }

    @Override
    public HtmlFragment getResult() {
        htmlFragtment.addBodyElement(sb.toString());
        return htmlFragtment;
    }
}
