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
package org.datacleaner.phonetic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.datacleaner.result.AnalyzerResult;


public class SimilarityResult implements AnalyzerResult {

	private static final long serialVersionUID = 1L;

	private final List<SimilarityGroup> _similarityGroups;

	public SimilarityResult(List<SimilarityGroup> similarityGroups) {
		_similarityGroups = similarityGroups;
	}

	public List<SimilarityGroup> getSimilarityGroups() {
		return _similarityGroups;
	}

	public Set<String> getValues() {
		Set<String> result = new HashSet<String>();
		for (SimilarityGroup sv : _similarityGroups) {
			String[] values = sv.getValues();
			result.add(values[0]);
			result.add(values[1]);
		}
		return result;
	}

	public List<String> getSimilarValues(String string) {
		ArrayList<String> result = new ArrayList<String>();
		for (SimilarityGroup similarityGroup : _similarityGroups) {
			if (similarityGroup.contains(string)) {
				String[] values = similarityGroup.getValues();
				for (String value : values) {
					if (!value.equals(string)) {
						result.add(value);
					}
				}
				break;
			}
		}
		return result;
	}

}
