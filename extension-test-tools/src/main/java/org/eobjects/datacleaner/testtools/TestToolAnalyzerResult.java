/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.testtools;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;

public class TestToolAnalyzerResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;

	private final int successCount;
	private final AnnotatedRowsResult errorRowsResult;

	public TestToolAnalyzerResult(int successCount,
			RowAnnotationFactory annotationFactory,
			RowAnnotation errornousRowAnnotation,
			InputColumn<?>[] columnsOfInterest) {
		this.successCount = successCount;
		this.errorRowsResult = new AnnotatedRowsResult(errornousRowAnnotation,
				annotationFactory, columnsOfInterest);
	}

	public AnnotatedRowsResult getErrorRowsResult() {
		return errorRowsResult;
	}

	public int getSuccessCount() {
		return successCount;
	}
}
