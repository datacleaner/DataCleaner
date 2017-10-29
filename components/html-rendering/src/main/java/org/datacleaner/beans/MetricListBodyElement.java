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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.HasAnalyzerResultComponentDescriptor;
import org.datacleaner.descriptors.MetricDescriptor;
import org.datacleaner.descriptors.MetricParameters;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.result.html.BodyElement;
import org.datacleaner.result.html.HtmlRenderingContext;

/**
 * Body element which simply produces a list of metrics as per the descriptor of the component job being rendered.
 */
public class MetricListBodyElement implements BodyElement {

    private final AnalyzerResult result;

    public MetricListBodyElement(AnalyzerResult result) {
        this.result = result;
    }

    @Override
    public String toHtml(HtmlRenderingContext context) {
        final ComponentJob componentJob = context.getComponentJob();
        if (componentJob == null) {
            return "";
        }
        return renderComponentJob(componentJob);
    }

    public String renderComponentJob(ComponentJob job) {
        final ComponentDescriptor<?> desc = job.getDescriptor();
        if (desc instanceof HasAnalyzerResultComponentDescriptor) {
            // if descriptor is an HasAnalyzerResultComponentDescriptor
            return renderMetrics(job, (HasAnalyzerResultComponentDescriptor<?>) desc);
        }
        // or else we cannot handle it
        return "";
    }

    public String renderMetrics(ComponentJob job, HasAnalyzerResultComponentDescriptor<?> descriptor) {
        final Set<ConfiguredPropertyDescriptor> primaryInputProperties =
                descriptor.getConfiguredPropertiesForInput(false);
        final List<InputColumn<?>> columns = primaryInputProperties.stream()
                .flatMap(property -> getInputColumns(job, property)).collect(Collectors.toList());
        final Set<MetricDescriptor> resultMetrics = descriptor.getResultMetrics();

        final StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"analyzerResultMetrics\">");

        resultMetrics.forEach(m -> {
            if (!m.isParameterizedByString()) {
                if (m.isParameterizedByInputColumn()) {
                    columns.forEach(col -> {
                        final Number metricValue = m.getValue(result, new MetricParameters(col));
                        sb.append("<div class=\"metric\">\n");
                        sb.append("              <span class=\"metricName\">" + m.getName() + " (" + col.getName()
                                + ")</span>\n");
                        sb.append("              <span class=\"metricValue\">" + metricValue + "</span>\n");
                        sb.append("            </div>");
                    });
                } else {
                    sb.append("<div class=\"metric\">\n");
                    sb.append("              <span class=\"metricName\">" + m.getName() + "</span>\n");
                    sb.append("              <span class=\"metricValue\">" + m.getValue(result, null) + "</span>\n");
                    sb.append("            </div>");
                }
            }
        });

        sb.append("</div>");
        return sb.toString();
    }

    private Stream<InputColumn<?>> getInputColumns(ComponentJob componentJob, ConfiguredPropertyDescriptor property) {
        final Object value = componentJob.getConfiguration().getProperty(property);
        if (value instanceof InputColumn) {
            return Arrays.stream(new InputColumn[] { (InputColumn<?>) value });
        }
        if (value instanceof InputColumn[]) {
            Arrays.stream((InputColumn[]) value);
        }
        if (value instanceof List) {
            @SuppressWarnings("unchecked") final List<InputColumn<?>> list = (List<InputColumn<?>>) value;
            return list.stream();
        }
        return Stream.empty();
    }
}
