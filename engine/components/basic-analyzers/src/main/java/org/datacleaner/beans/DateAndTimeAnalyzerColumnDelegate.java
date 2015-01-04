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

import java.util.Date;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.datacleaner.data.InputRow;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 * Helper class for the Date/time Analyzer. This class collects all the
 * statistics for a single column. The Date/time Analyzer then consists of a
 * number of these delegates.
 * 
 * 
 */
final class DateAndTimeAnalyzerColumnDelegate {

    private final RowAnnotationFactory _annotationFactory;
    private final RowAnnotation _nullAnnotation;
    private final RowAnnotation _maxDateAnnotation;
    private final RowAnnotation _minDateAnnotation;
    private final RowAnnotation _maxTimeAnnotation;
    private final RowAnnotation _minTimeAnnotation;
    private final StatisticalSummary _statistics;
    private volatile int _numRows;
    private volatile LocalDate _minDate;
    private volatile LocalDate _maxDate;
    private volatile LocalTime _minTime;
    private volatile LocalTime _maxTime;

    public DateAndTimeAnalyzerColumnDelegate(boolean descriptiveStatistics, RowAnnotationFactory annotationFactory) {
        _annotationFactory = annotationFactory;
        _nullAnnotation = _annotationFactory.createAnnotation();
        _maxDateAnnotation = _annotationFactory.createAnnotation();
        _minDateAnnotation = _annotationFactory.createAnnotation();
        _maxTimeAnnotation = _annotationFactory.createAnnotation();
        _minTimeAnnotation = _annotationFactory.createAnnotation();
        _numRows = 0;
        if (descriptiveStatistics) {
            _statistics = new DescriptiveStatistics();
        } else {
            _statistics = new SummaryStatistics();
        }
    }

    public synchronized void run(final Date value, final InputRow row, final int distinctCount) {
        _numRows += distinctCount;
        if (value == null) {
            _annotationFactory.annotate(row, distinctCount, _nullAnnotation);
        } else {
            final long timestamp = value.getTime();

            for (int i = 0; i < distinctCount; i++) {
                if (_statistics instanceof DescriptiveStatistics) {
                    ((DescriptiveStatistics) _statistics).addValue(timestamp);
                } else {
                    ((SummaryStatistics) _statistics).addValue(timestamp);
                }
            }

            LocalDate localDate = new LocalDate(value);
            LocalTime localTime = new LocalTime(value);
            if (_minDate == null) {
                // first non-null value
                _minDate = localDate;
                _maxDate = localDate;
                _minTime = localTime;
                _maxTime = localTime;
            } else {
                if (localDate.isAfter(_maxDate)) {
                    _maxDate = localDate;
                    _annotationFactory.reset(_maxDateAnnotation);
                } else if (localDate.isBefore(_minDate)) {
                    _minDate = localDate;
                    _annotationFactory.reset(_minDateAnnotation);
                }

                if (localTime.isAfter(_maxTime)) {
                    _maxTime = localTime;
                    _annotationFactory.reset(_maxTimeAnnotation);
                } else if (localTime.isBefore(_minTime)) {
                    _minTime = localTime;
                    _annotationFactory.reset(_minTimeAnnotation);
                }
            }

            if (localDate.isEqual(_maxDate)) {
                _annotationFactory.annotate(row, distinctCount, _maxDateAnnotation);
            }
            if (localDate.isEqual(_minDate)) {
                _annotationFactory.annotate(row, distinctCount, _minDateAnnotation);
            }

            if (localTime.isEqual(_maxTime)) {
                _annotationFactory.annotate(row, distinctCount, _maxTimeAnnotation);
            }
            if (localTime.isEqual(_minTime)) {
                _annotationFactory.annotate(row, distinctCount, _minTimeAnnotation);
            }
        }
    }

    public Date getMean() {
        double meanTimestamp = _statistics.getMean();
        if (Double.isNaN(meanTimestamp)) {
            return null;
        }
        return new Date(Double.valueOf(meanTimestamp).longValue());
    }

    public Date getMedian() {
        if (_statistics instanceof DescriptiveStatistics) {
            double medianTimestamp = ((DescriptiveStatistics) _statistics).getPercentile(50.0);
            if (Double.isNaN(medianTimestamp)) {
                return null;
            }
            return new Date(Double.valueOf(medianTimestamp).longValue());
        }
        return null;
    }

    public Date getPercentile25() {
        if (_statistics instanceof DescriptiveStatistics) {
            double percentileTimestamp = ((DescriptiveStatistics) _statistics).getPercentile(25.0);
            if (Double.isNaN(percentileTimestamp)) {
                return null;
            }
            return new Date(Double.valueOf(percentileTimestamp).longValue());
        }
        return null;
    }

    public Date getPercentile75() {
        if (_statistics instanceof DescriptiveStatistics) {
            double percentileTimestamp = ((DescriptiveStatistics) _statistics).getPercentile(75.0);
            if (Double.isNaN(percentileTimestamp)) {
                return null;
            }
            return new Date(Double.valueOf(percentileTimestamp).longValue());
        }
        return null;
    }

    public Number getKurtosis() {
        if (_statistics instanceof DescriptiveStatistics) {
            double result = ((DescriptiveStatistics) _statistics).getKurtosis();
            if (Double.isNaN(result)) {
                return null;
            }
            return result;
        }
        return null;
    }

    public Number getSkewness() {
        if (_statistics instanceof DescriptiveStatistics) {
            double result = ((DescriptiveStatistics) _statistics).getSkewness();
            if (Double.isNaN(result)) {
                return null;
            }
            return result;
        }
        return null;
    }

    public LocalDate getMaxDate() {
        return _maxDate;
    }

    public LocalTime getMaxTime() {
        return _maxTime;
    }

    public LocalDate getMinDate() {
        return _minDate;
    }

    public LocalTime getMinTime() {
        return _minTime;
    }

    public int getNumRows() {
        return _numRows;
    }

    public RowAnnotation getNullAnnotation() {
        return _nullAnnotation;
    }

    public RowAnnotation getMaxDateAnnotation() {
        return _maxDateAnnotation;
    }

    public RowAnnotation getMinDateAnnotation() {
        return _minDateAnnotation;
    }

    public RowAnnotation getMaxTimeAnnotation() {
        return _maxTimeAnnotation;
    }

    public RowAnnotation getMinTimeAnnotation() {
        return _minTimeAnnotation;
    }

    public int getNumNull() {
        return _nullAnnotation.getRowCount();
    }

}
