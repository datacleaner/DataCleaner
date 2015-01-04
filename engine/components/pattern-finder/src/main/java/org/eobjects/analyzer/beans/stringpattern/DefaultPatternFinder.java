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
package org.eobjects.analyzer.beans.stringpattern;

import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.storage.InMemoryRowAnnotationFactory;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;

/**
 * Default PatternFinder implementation, used by the PatternFinderAnalyzer.
 * 
 * 
 */
public final class DefaultPatternFinder extends PatternFinder<InputRow> {

	private final Map<TokenPattern, RowAnnotation> _annotations;
	private final RowAnnotationFactory _annotationFactory;

	/**
	 * Default constructor, which requires a configuration and a row annotation
	 * factory for storage of rows.
	 * 
	 * @param configuration
	 * @param annotationFactory
	 */
	public DefaultPatternFinder(TokenizerConfiguration configuration, RowAnnotationFactory annotationFactory) {
		super(configuration);
		if (annotationFactory == null) {
			throw new IllegalArgumentException("RowAnnotationFactory cannot be null");
		}
		_annotations = new HashMap<TokenPattern, RowAnnotation>();
		_annotationFactory = annotationFactory;
	}

	/**
	 * Alternative constructor for more ad-hoc usage. Uses an in memory storage
	 * mechanism with a threshold on how many rows to store.
	 * 
	 * @param configuration
	 * @param inMemoryRowThreshold
	 */
	public DefaultPatternFinder(TokenizerConfiguration configuration, int inMemoryRowThreshold) {
		super(configuration);
		_annotations = new HashMap<TokenPattern, RowAnnotation>();
		_annotationFactory = new InMemoryRowAnnotationFactory(inMemoryRowThreshold);
	}

	@Override
	protected void storeNewPattern(TokenPattern pattern, InputRow row, String value, int distinctCount) {
		RowAnnotation annotation = _annotationFactory.createAnnotation();
		_annotations.put(pattern, annotation);
		_annotationFactory.annotate(row, distinctCount, annotation);
	}

	@Override
	protected void storeMatch(TokenPattern pattern, InputRow row, String value, int distinctCount) {
		RowAnnotation annotation = _annotations.get(pattern);
		if (annotation == null) {
			throw new IllegalStateException("No annotation available for pattern: " + pattern);
		}
		_annotationFactory.annotate(row, distinctCount, annotation);
	}

	public Map<TokenPattern, RowAnnotation> getAnnotations() {
		return _annotations;
	}
}
