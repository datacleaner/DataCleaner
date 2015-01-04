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
package org.eobjects.analyzer.result.renderer;

import java.util.Collection;
import java.util.Set;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.api.RendererPrecedence;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.descriptors.MetricDescriptor;
import org.eobjects.analyzer.descriptors.MetricParameters;
import org.eobjects.analyzer.descriptors.ResultDescriptor;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.util.LabelUtils;

/**
 * The last-resort text renderer, which prints every metric available
 */
@RendererBean(TextRenderingFormat.class)
public class MetricBasedResultTextRenderer implements Renderer<AnalyzerResult, String> {

    @Override
    public RendererPrecedence getPrecedence(AnalyzerResult renderable) {
        return RendererPrecedence.LOWEST;
    }

    @Override
    public String render(AnalyzerResult result) {
        final ResultDescriptor resultDescriptor = Descriptors.ofResult(result);
        final Set<MetricDescriptor> resultMetrics = resultDescriptor.getResultMetrics();

        final StringBuilder sb = new StringBuilder();
        sb.append(result.getClass().getSimpleName());
        sb.append(":");

        int count = 0;

        // add non-parameterized metrics
        for (final MetricDescriptor metricDescriptor : resultMetrics) {
            if (!metricDescriptor.isParameterizedByString() && !metricDescriptor.isParameterizedByInputColumn()) {
                final Number value = metricDescriptor.getValue(result, null);
                sb.append("\n - ");
                sb.append(metricDescriptor.getName());
                sb.append(": ");
                sb.append(LabelUtils.getValueLabel(value));
                count++;
            }
        }

        // add string-parameterized metrics
        for (final MetricDescriptor metricDescriptor : resultMetrics) {
            if (metricDescriptor.isParameterizedByString() && !metricDescriptor.isParameterizedByInputColumn()) {
                try {
                    Collection<String> suggestions = metricDescriptor.getMetricParameterSuggestions(result);
                    if (suggestions != null && !suggestions.isEmpty()) {
                        for (String suggestion : suggestions) {
                            final Number value = metricDescriptor.getValue(result, new MetricParameters(suggestion));

                            sb.append("\n - ");
                            sb.append(metricDescriptor.getName());
                            sb.append(" (");
                            sb.append(suggestion);
                            sb.append("): ");
                            sb.append(LabelUtils.getValueLabel(value));
                        }
                        count++;
                    }
                } catch (Exception e) {
                    // ignore that metric
                }
            }
        }

        if (count == 0) {
            sb.append("\n (no metrics)");
        }

        return sb.toString();
    }

}
