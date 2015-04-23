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
package org.datacleaner.documentation;

import org.datacleaner.api.HasAnalyzerResult;
import org.datacleaner.descriptors.MetricDescriptor;
import org.datacleaner.util.StringUtils;

/**
 * A wrapper around a {@link HasAnalyzerResult}s result metrics to make it
 * easier for the documentation template to get to certain aspects that should
 * be presented in the documentation.
 */
public class MetricDocumentationWrapper {

    private final MetricDescriptor _metric;

    public MetricDocumentationWrapper(MetricDescriptor metric) {
        _metric = metric;
    }

    public String getName() {
        return _metric.getName();
    }

    public String getDescription() {
        String description = _metric.getDescription();
        if (description == null) {
            return "";
        }
        description = "<p>" + description + "</p>";
        description = StringUtils.replaceAll(description, "\n\n", "\n");
        description = StringUtils.replaceAll(description, "\n", "</p><p>");
        return description;
    }

    public boolean isParameterizedByString() {
        return _metric.isParameterizedByString();
    }

    public boolean isParameterizedByInputColumn() {
        return _metric.isParameterizedByInputColumn();
    }
    
    public boolean isNotParameterized() {
        return !isParameterizedByString() && !isParameterizedByInputColumn();
    }
}
