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

import org.datacleaner.api.InputRow;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;

final class BooleanAnalyzerColumnDelegate {

	private final RowAnnotationFactory _annotationFactory;
	private final RowAnnotation _nullAnnotation;
	private final RowAnnotation _trueAnnotation;
	private final RowAnnotation _falseAnnotation;
	private volatile int _rowCount;

	public BooleanAnalyzerColumnDelegate(RowAnnotationFactory annotationFactory) {
		_annotationFactory = annotationFactory;
		_nullAnnotation = _annotationFactory.createAnnotation();
		_trueAnnotation = _annotationFactory.createAnnotation();
		_falseAnnotation = _annotationFactory.createAnnotation();
	}

	public void run(Boolean value, InputRow row, int distinctCount) {
		_rowCount += distinctCount;
		if (value == null) {
			_annotationFactory.annotate(row, distinctCount, _nullAnnotation);
		} else {
			if (value.booleanValue()) {
				_annotationFactory.annotate(row, distinctCount, _trueAnnotation);
			} else {
				_annotationFactory.annotate(row, distinctCount, _falseAnnotation);
			}
		}
	}

	public int getRowCount() {
		return _rowCount;
	}

	public int getNullCount() {
		return _nullAnnotation.getRowCount();
	}

	public RowAnnotation getFalseAnnotation() {
		return _falseAnnotation;
	}

	public RowAnnotation getTrueAnnotation() {
		return _trueAnnotation;
	}

	public RowAnnotation getNullAnnotation() {
		return _nullAnnotation;
	}
}
