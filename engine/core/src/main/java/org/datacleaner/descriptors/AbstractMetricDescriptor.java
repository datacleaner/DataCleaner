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
package org.datacleaner.descriptors;

import org.datacleaner.api.Description;
import org.datacleaner.api.Metric;

public abstract class AbstractMetricDescriptor implements MetricDescriptor {

    private static final long serialVersionUID = 1L;

    @Override
    public final String getDescription() {
        Description desc = getAnnotation(Description.class);
        if (desc == null) {
            return null;
        }
        return desc.value();
    }

    @Override
    public final int compareTo(MetricDescriptor o) {
        Metric metric1 = getAnnotation(Metric.class);
        final int order1 = metric1.order();
        Metric metric2 = o.getAnnotation(Metric.class);
        final int order2;
        if (metric2 == null) {
            order2 = Integer.MAX_VALUE;
        } else {
            order2 = metric2.order();
        }
        int diff = order1 - order2;
        if (diff == 0) {
            return getName().compareTo(o.getName());
        }
        return diff;
    }

    @Override
    public final  String toString() {
        return getClass().getSimpleName() + "[name=" + getName() + "]";
    }
}
