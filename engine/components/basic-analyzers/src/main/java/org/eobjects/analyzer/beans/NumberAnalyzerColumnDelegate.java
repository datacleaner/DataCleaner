/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.beans;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;

/**
 * Helper class for the number analyzer, which handles the processing of a
 * single column's values.
 */
final class NumberAnalyzerColumnDelegate {

	private final RowAnnotationFactory _annotationFactory;
	private final StatisticalSummary _statistics;
	private volatile int _numRows;
	private final RowAnnotation _nullAnnotation;
	private final RowAnnotation _maxAnnotation;
	private final RowAnnotation _minAnnotation;

	public NumberAnalyzerColumnDelegate(boolean descriptiveStatistics, RowAnnotationFactory annotationFactory) {
		_annotationFactory = annotationFactory;
		_nullAnnotation = _annotationFactory.createAnnotation();
		_maxAnnotation = _annotationFactory.createAnnotation();
		_minAnnotation = _annotationFactory.createAnnotation();
		if (descriptiveStatistics) {
		    _statistics = new DescriptiveStatistics();
		} else {
		    _statistics = new SummaryStatistics();
		}
	}

	public synchronized void run(InputRow row, Number value, int distinctCount) {
		_numRows += distinctCount;
		if (value != null) {
			double doubleValue = value.doubleValue();
			double max = _statistics.getMax();
			double min = _statistics.getMin();

			if (max < doubleValue) {
				_annotationFactory.reset(_maxAnnotation);
			}
			if (min > doubleValue) {
				_annotationFactory.reset(_minAnnotation);
			}

			for (int i = 0; i < distinctCount; i++) {
			    if (_statistics instanceof DescriptiveStatistics) {
			        ((DescriptiveStatistics)_statistics).addValue(doubleValue);
			    } else {
			        ((SummaryStatistics)_statistics).addValue(doubleValue);
			    }
			}

			max = _statistics.getMax();
			min = _statistics.getMin();

			if (max == doubleValue) {
				_annotationFactory.annotate(row, distinctCount, _maxAnnotation);
			}
			if (min == doubleValue) {
				_annotationFactory.annotate(row, distinctCount, _minAnnotation);
			}
		} else {
			_annotationFactory.annotate(row, distinctCount, _nullAnnotation);
		}
	}

	public RowAnnotation getNullAnnotation() {
		return _nullAnnotation;
	}

	public StatisticalSummary getStatistics() {
		return _statistics;
	}

	public int getNullCount() {
		return _nullAnnotation.getRowCount();
	}

	public RowAnnotation getMaxAnnotation() {
		return _maxAnnotation;
	}

	public RowAnnotation getMinAnnotation() {
		return _minAnnotation;
	}

	public int getNumRows() {
		return _numRows;
	}
}
