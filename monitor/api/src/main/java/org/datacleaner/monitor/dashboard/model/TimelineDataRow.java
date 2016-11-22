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
package org.datacleaner.monitor.dashboard.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * A row of metric values in a {@link TimelineData} object.
 */
public class TimelineDataRow implements Comparable<TimelineDataRow>, Serializable {

    private static final long serialVersionUID = 1L;

    private Date _date;
    private List<Number> _metricValues;
    private String _resultFilePath;

    public TimelineDataRow(final Date date, final String resultFilePath) {
        _date = date;
        _resultFilePath = resultFilePath;
    }

    public TimelineDataRow() {
        this(null, null);
    }

    public Date getDate() {
        return _date;
    }

    public void setDate(final Date date) {
        _date = date;
    }

    public String getResultFilePath() {
        return _resultFilePath;
    }

    public void setResultFilePath(final String resultFilePath) {
        _resultFilePath = resultFilePath;
    }

    public List<Number> getMetricValues() {
        return _metricValues;
    }

    public void setMetricValues(final List<Number> metricValues) {
        _metricValues = metricValues;
    }

    @Override
    public String toString() {
        return "TimelineDataRow[date=" + format(_date) + ",metricValues=" + _metricValues + "]";
    }

    @SuppressWarnings("deprecation")
    private String format(final Date d) {
        return (d.getYear() + 1900) + "-" + (d.getMonth() + 1) + "-" + d.getDate() + " " + d.getHours() + ":"
                + d.getMinutes();
    }

    @Override
    public int compareTo(final TimelineDataRow other) {
        int diff = _date.compareTo(other.getDate());
        if (diff != 0) {
            return diff;
        }

        final List<Number> otherMetricValues = other.getMetricValues();
        for (int i = 0; i < _metricValues.size(); i++) {
            final Number metricValue = _metricValues.get(i);
            final Number otherMetricValue = otherMetricValues.get(i);
            diff = metricValue.intValue() - otherMetricValue.intValue();
            if (diff != 0) {
                return diff;
            }
        }

        return -1;
    }
}
