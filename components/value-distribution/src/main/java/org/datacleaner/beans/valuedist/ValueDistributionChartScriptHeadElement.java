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

import org.datacleaner.result.ValueFrequency;
import org.datacleaner.result.html.HeadElement;
import org.datacleaner.result.html.HtmlRenderingContext;
import org.datacleaner.util.LabelUtils;

public class ValueDistributionChartScriptHeadElement implements HeadElement {

    private final String _chartElementId;
    private final Iterable<ValueFrequency> _valueCounts;

    public ValueDistributionChartScriptHeadElement(Iterable<ValueFrequency> valueCounts, String chartElementId) {
        _valueCounts = valueCounts;
        _chartElementId = chartElementId;
    }

    @Override
    public String toHtml(HtmlRenderingContext context) {
        // will be used to plot the y-axis value. Descending/negative because we want them to go from top to bottom.
        int negativeIndex = 0;
        String dataId = "data" + _chartElementId;

        final StringBuilder sb = new StringBuilder();
        sb.append("<script type=\"text/javascript\">");
        sb.append("//<![CDATA[");
        sb.append("var " + dataId + " = [");
        for (ValueFrequency vc : _valueCounts) {
            if (negativeIndex != 0) {
                sb.append(',');
            }
            final String color = getColor(vc);
            negativeIndex = negativeIndex - 1;
            sb.append("{");
            sb.append("label:\"" + escapeLabel(context, vc.getName()) + "\"");
            sb.append(",data:[[" + vc.getCount() + "," + negativeIndex + "]]");
            if (color != null) {
                sb.append(",color:\"" + color + "\"");
            }
            sb.append("}");
        }
        sb.append("];\n");
        sb.append("require(['jquery'], function ($) {");
        sb.append("$(function() {");
        sb.append("draw_value_distribution_bar('" + _chartElementId + "', " + dataId + ", 2);");
        sb.append("});");
        sb.append("});");
        sb.append("// ]]>");
        sb.append("</script>");
        return sb.toString();
    }

    private String getColor(ValueFrequency vc) {
        final String name = vc.getName();
        switch (name) {
        case LabelUtils.UNIQUE_LABEL:
            return "#ccc";
        case LabelUtils.BLANK_LABEL:
            return "#eee";
        case LabelUtils.UNEXPECTED_LABEL:
            return "#333";
        case LabelUtils.NULL_LABEL:
            return "#111";
        default:
            switch (name.toLowerCase()) {
            case "red":
            case "blue":
            case "green":
            case "yellow":
            case "orange":
            case "black":
                return name.toLowerCase();
            case "not_processed":
                return "#333";
            case "failure":
                return "#000";
            default:
                return null;
            }
        }
    }

    private String escapeLabel(HtmlRenderingContext context, String name) {
        final String escaped = context.escapeJson(name);
        return escaped.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }
}
