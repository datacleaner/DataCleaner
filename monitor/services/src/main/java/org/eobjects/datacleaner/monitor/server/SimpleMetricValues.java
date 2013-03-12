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
package org.eobjects.datacleaner.monitor.server;

import java.util.Date;
import java.util.List;

/**
 * Simple POJO style implementation of {@link MetricValues}. Requires an eagerly
 * calculation of metric values.
 */
public class SimpleMetricValues implements MetricValues {

    private final Date _date;
    private final List<Number> _values;

    public SimpleMetricValues(Date date, List<Number> values) {
        _date = date;
        _values = values;
    }

    @Override
    public Date getMetricDate() {
        return _date;
    }

    @Override
    public List<Number> getValues() {
        return _values;
    }

}
