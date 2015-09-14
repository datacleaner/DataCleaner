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
package org.datacleaner.result;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.Func;
import org.datacleaner.api.Metric;
import org.datacleaner.util.LabelUtils;

/**
 * An abstract implementation of {@link ValueCountingAnalyzerResult} which
 * implements the most important metric: The value count.
 */
public abstract class AbstractValueCountingAnalyzerResult implements
		ValueCountingAnalyzerResult {

	private static final long serialVersionUID = 1L;

	@Metric(value = "Value count", supportsInClause = true)
	public final QueryParameterizableMetric getValueCount() {
		return new QueryParameterizableMetric() {

			@Override
			public Collection<String> getParameterSuggestions() {
				final Collection<ValueFrequency> valueCounts = AbstractValueCountingAnalyzerResult.this
						.getValueCounts();
				final List<String> result = CollectionUtils.map(valueCounts,
						new Func<ValueFrequency, String>() {
							@Override
							public String eval(ValueFrequency vc) {
								return vc.getName();
							}
						});
				result.remove(null);
				result.remove(LabelUtils.NULL_LABEL);
				result.remove(LabelUtils.UNEXPECTED_LABEL);
				return result;
			}

			@Override
			public int getTotalCount() {
				return AbstractValueCountingAnalyzerResult.this.getTotalCount();
			}

			@Override
			public int getInstanceCount(String instance) {
				Integer count = getCount(instance);
				if (count == null) {
					return 0;
				}
				return count;
			}
		};
	}

	@Override
	public Collection<ValueFrequency> getReducedValueFrequencies(
			final int preferredMaximum) {
		final Collection<ValueFrequency> original = getValueCounts();

		final Collection<ValueFrequency> result = new TreeSet<ValueFrequency>(
				original);

		if (original.size() <= preferredMaximum) {
			// check if any composite value freq's can be exploded
			for (ValueFrequency valueFrequency : original) {
				if (valueFrequency.isComposite()) {
					List<ValueFrequency> children = valueFrequency
							.getChildren();
					if (children != null) {
						if (result.size() - 1 + children.size() <= preferredMaximum) {
							// replace with children
							result.remove(valueFrequency);
							result.addAll(children);
						}
					}
				}
			}
			return result;
		} else {
			return original;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Value distribution for: ");
		sb.append(getName());
		appendToString(sb, this, 4);
		return sb.toString();
	}

	/**
	 * Appends a string representation with a maximum amount of entries
	 * 
	 * @param sb
	 *            the StringBuilder to append to
	 * 
	 * @param maxEntries
	 * @return
	 */
	protected void appendToString(StringBuilder sb,
			ValueCountingAnalyzerResult groupResult, int maxEntries) {
		if (maxEntries != 0) {
			Collection<ValueFrequency> valueCounts = groupResult
					.getValueCounts();
			for (ValueFrequency valueCount : valueCounts) {
				sb.append("\n - ");
				sb.append(valueCount.getName());
				sb.append(": ");
				sb.append(valueCount.getCount());

				maxEntries--;
				if (maxEntries == 0) {
					sb.append("\n ...");
					break;
				}
			}
		}
	}
}
