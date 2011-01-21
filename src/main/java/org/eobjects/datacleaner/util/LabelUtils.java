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
package org.eobjects.datacleaner.util;

import java.util.List;

import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.ConfigurableBeanJob;
import org.eobjects.analyzer.job.MergeInput;
import org.eobjects.analyzer.job.MergedOutcomeJob;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.MergeInputBuilder;
import org.eobjects.analyzer.job.builder.MergedOutcomeJobBuilder;
import org.eobjects.analyzer.util.StringUtils;

/**
 * Utility class for reusable methods and constants that represent user readable
 * labels
 * 
 * @author Kasper Sørensen
 */
public final class LabelUtils {

	public static final String NULL_LABEL = "<null>";

	public static final String UNIQUE_LABEL = "<unique>";

	public static final String COUNT_LABEL = "COUNT(*)";

	private LabelUtils() {
		// prevent instantiation
	}

	public static String getLabel(AbstractBeanJobBuilder<?, ?, ?> builder) {
		String label = builder.getName();
		if (StringUtils.isNullOrEmpty(label)) {
			label = builder.getDescriptor().getDisplayName();
		}
		return label;
	}

	public static String getLabel(ComponentJob job) {
		String label = job.getName();
		if (StringUtils.isNullOrEmpty(label)) {
			if (job instanceof ConfigurableBeanJob) {
				BeanDescriptor<?> descriptor = ((ConfigurableBeanJob<?>) job).getDescriptor();
				label = descriptor.getDisplayName();
			} else if (job instanceof MergedOutcomeJob) {
				MergeInput[] inputs = ((MergedOutcomeJob) job).getMergeInputs();
				StringBuilder sb = new StringBuilder();
				sb.append("MergedOutcome[");
				sb.append(inputs.length);
				sb.append(']');
				label = sb.toString();
			} else {
				label = job.toString();
			}
		}
		return label;
	}

	public static String getLabel(MergedOutcomeJobBuilder builder) {
		String label = builder.getName();
		if (StringUtils.isNullOrEmpty(label)) {
			List<MergeInputBuilder> inputs = builder.getMergeInputs();
			StringBuilder sb = new StringBuilder();
			sb.append("MergedOutcome[");
			sb.append(inputs.size());
			sb.append(']');
		}
		return label;
	}
}
