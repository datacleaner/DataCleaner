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
package org.eobjects.datacleaner.phonetic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.inject.Inject;

import org.apache.commons.codec.language.Metaphone;
import org.apache.commons.codec.language.RefinedSoundex;
import org.apache.commons.codec.language.Soundex;
import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.SimilarityGroup;
import org.eobjects.analyzer.result.SimilarityResult;
import org.eobjects.analyzer.storage.InMemoryRowAnnotationFactory;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AnalyzerBean("Phonetic similarity finder")
@Description("Find similar sounding values using phonetic checking.\nThis analyzer uses the Soundex, Refined Soundex and Metaphone algorithms to determine the phonetic similarity of String values.")
public class PhoneticSimilarityFinder implements Analyzer<SimilarityResult> {

	public static enum MatchMode {
		STRICT, LOOSE
	}

	private final static Logger logger = LoggerFactory.getLogger(PhoneticSimilarityFinder.class);

	private final static double STRICT_SIMILARITY_THRESHOLD = 1.0;

	// everything that is ~80% similar will be included
	private final static double LOOSE_SIMILARITY_THRESHOLD = 8d / 10;

	private List<SimilarityGroup> _similarityGroups = new ArrayList<SimilarityGroup>();

	@Inject
	@Provided
	RowAnnotationFactory _rowAnnotationFactory;

	@Configured
	MatchMode matchMode = MatchMode.STRICT;

	@Inject
	@Configured
	InputColumn<String> _column;

	public PhoneticSimilarityFinder() {
	}

	// constructor for test purposes
	public PhoneticSimilarityFinder(InputColumn<String> column) {
		_column = column;
		_rowAnnotationFactory = new InMemoryRowAnnotationFactory();
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		String value = row.getValue(_column);

		if (value != null) {
			value = value.trim().toLowerCase();
			if (!"".equals(value)) {
				boolean foundMatch = false;

				for (ListIterator<SimilarityGroup> it = _similarityGroups.listIterator(); it.hasNext();) {
					SimilarityGroup similarityGroup = it.next();

					if (matches(value, similarityGroup)) {
						RowAnnotation annotation = similarityGroup.getAnnotation();
						it.set(new SimilarityGroup(annotation, _rowAnnotationFactory, _column, value, similarityGroup
								.getValues()));
						_rowAnnotationFactory.annotate(row, distinctCount, annotation);
						foundMatch = true;
					}
				}

				if (!foundMatch) {
					RowAnnotation annotation = _rowAnnotationFactory.createAnnotation();
					_rowAnnotationFactory.annotate(row, distinctCount, annotation);
					_similarityGroups.add(new SimilarityGroup(annotation, _rowAnnotationFactory, _column, value,
							new String[0]));
				}
			}
		}
	}

	public boolean matches(String value, SimilarityGroup similarityGroup) {
		// first do exact matching
		for (String similarityGroupValue : similarityGroup.getValues()) {
			if (value.equals(similarityGroupValue)) {
				return true;
			}
		}

		Soundex soundex = new Soundex();
		RefinedSoundex refinedSoundex = new RefinedSoundex();
		Metaphone metaphone = new Metaphone();

		double threshold;
		if (matchMode == MatchMode.STRICT) {
			threshold = STRICT_SIMILARITY_THRESHOLD;
		} else {
			threshold = LOOSE_SIMILARITY_THRESHOLD;
		}
		int soundexThreshold = (int) Math.round(threshold * 4);

		for (String similarityGroupValue : similarityGroup.getValues()) {
			boolean metaphoneEquals = metaphone.isMetaphoneEqual(value, similarityGroupValue);
			if (metaphoneEquals) {
				return true;
			}

			try {
				int soundexDiff = soundex.difference(value, similarityGroupValue);

				if (soundexDiff >= soundexThreshold) {
					return true;
				}
			} catch (Exception e) {
				logger.error("Could not determine soundex difference", e);
			}

			int refinedSoundexThreshold = (int) Math.round(threshold
					* Math.min(value.length(), similarityGroupValue.length()));

			try {
				int refinedSoundexDiff = refinedSoundex.difference(value, similarityGroupValue);

				if (refinedSoundexDiff >= refinedSoundexThreshold) {
					return true;
				}
			} catch (Exception e) {
				logger.error("Could not determine refined soundex difference", e);
			}
		}

		return false;
	}

	@Override
	public SimilarityResult getResult() {
		for (Iterator<SimilarityGroup> it = _similarityGroups.iterator(); it.hasNext();) {
			SimilarityGroup similarityGroup = it.next();
			if (similarityGroup.getValueCount() == 1) {
				it.remove();
			}
		}
		return new SimilarityResult(_similarityGroups);
	}
}