/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.server.jaxb;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eobjects.datacleaner.monitor.jaxb.MetricType;
import org.eobjects.datacleaner.monitor.jaxb.MetricType.Children;
import org.eobjects.datacleaner.monitor.jaxb.MetricsType;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;

/**
 * Utility class which has the responsibility of converting from and to
 * {@link MetricType} and {@link MetricIdentifier}.
 */
public class JaxbMetricAdaptor extends AbstractJaxbAdaptor<MetricsType> {

    public JaxbMetricAdaptor() {
        super(MetricsType.class);
    }
    
    public MetricsType read(InputStream in) {
        final MetricsType result = super.unmarshal(in);
        return result;
    }
    
    @Override
    public XMLGregorianCalendar createDate(Date date) {
        return super.createDate(date);
    }

    public MetricType serialize(MetricIdentifier metricIdentifier) {
        final MetricType metric = new MetricType();
        if (metricIdentifier.isDisplayNameSet()) {
            metric.setMetricDisplayName(metricIdentifier.getDisplayName());
        }
        metric.setMetricColor(metricIdentifier.getMetricColor());
        if (metricIdentifier.isFormulaBased()) {
            metric.setFormula(metricIdentifier.getFormula());
            final Children children = new Children();
            final List<MetricIdentifier> childMetricIdentifiers = metricIdentifier.getChildren();
            for (final MetricIdentifier childMetricIdentifier : childMetricIdentifiers) {
                final MetricType child = serialize(childMetricIdentifier);
                children.getMetric().add(child);
            }
            metric.setChildren(children);
        } else {
            metric.setAnalyzerDescriptorName(metricIdentifier.getAnalyzerDescriptorName());
            metric.setAnalyzerInput(metricIdentifier.getAnalyzerInputName());
            metric.setAnalyzerName(metricIdentifier.getAnalyzerName());
            metric.setMetricDescriptorName(metricIdentifier.getMetricDescriptorName());
            metric.setMetricParamColumnName(metricIdentifier.getParamColumnName());
            metric.setMetricParamQueryString(metricIdentifier.getParamQueryString());
        }
        return metric;
    }

    public MetricIdentifier deserialize(MetricType metricType) {
        final String metricDisplayName = metricType.getMetricDisplayName();
        final String metricColor = metricType.getMetricColor();
        final String formula = metricType.getFormula();
        final Children childrenTypes = metricType.getChildren();
        if (formula == null || childrenTypes == null) {
            final MetricIdentifier metricIdentifier = new MetricIdentifier();
            metricIdentifier.setAnalyzerDescriptorName(metricType.getAnalyzerDescriptorName());
            metricIdentifier.setAnalyzerName(metricType.getAnalyzerName());
            metricIdentifier.setAnalyzerInputName(metricType.getAnalyzerInput());
            metricIdentifier.setMetricDescriptorName(metricType.getMetricDescriptorName());
            metricIdentifier.setMetricDisplayName(metricDisplayName);
            metricIdentifier.setMetricColor(metricColor);
            metricIdentifier.setParamColumnName(metricType.getMetricParamColumnName());
            metricIdentifier.setParamQueryString(metricType.getMetricParamQueryString());
            metricIdentifier.setParameterizedByColumnName(metricType.getMetricParamColumnName() != null);
            metricIdentifier.setParameterizedByQueryString(metricType.getMetricParamQueryString() != null);
            return metricIdentifier;
        } else {
            final List<MetricType> childMetrics = childrenTypes.getMetric();
            final List<MetricIdentifier> children = new ArrayList<MetricIdentifier>(childMetrics.size());
            for (MetricType childMetricType : childMetrics) {
                MetricIdentifier childMetric = deserialize(childMetricType);
                children.add(childMetric);
            }

            final MetricIdentifier metricIdentifier = new MetricIdentifier();
            metricIdentifier.setFormula(formula);
            metricIdentifier.setChildren(children);
            metricIdentifier.setMetricDisplayName(metricDisplayName);
            metricIdentifier.setMetricColor(metricColor);

            return metricIdentifier;
        }
    }
}
