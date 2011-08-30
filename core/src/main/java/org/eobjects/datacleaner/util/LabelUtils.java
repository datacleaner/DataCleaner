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

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.ConfigurableBeanJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.job.MergeInput;
import org.eobjects.analyzer.job.MergedOutcome;
import org.eobjects.analyzer.job.MergedOutcomeJob;
import org.eobjects.analyzer.job.Outcome;
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
	public static final String BLANK_LABEL = "<blank>";
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
		String jobName = job.getName();
		StringBuilder label = new StringBuilder();
		if (StringUtils.isNullOrEmpty(jobName)) {
			if (job instanceof ConfigurableBeanJob) {
				BeanDescriptor<?> descriptor = ((ConfigurableBeanJob<?>) job).getDescriptor();
				label.append(descriptor.getDisplayName());
			} else if (job instanceof MergedOutcomeJob) {
				MergeInput[] inputs = ((MergedOutcomeJob) job).getMergeInputs();
				label.append("MergedOutcome[");
				label.append(inputs.length);
				label.append(']');
			} else {
				label.append(job.toString());
			}
		} else {
			label.append(jobName);
		}

		if (job instanceof AnalyzerJob) {
			AnalyzerJob analyzerJob = (AnalyzerJob) job;
			if (!StringUtils.isNullOrEmpty(jobName)) {
				label.append(" (");
				label.append(analyzerJob.getDescriptor().getDisplayName());
				label.append(')');
			}

			final InputColumn<?>[] input = analyzerJob.getInput();
			if (input.length > 0) {
				label.append(" (");
				if (input.length < 5) {
					for (int i = 0; i < input.length; i++) {
						if (i != 0) {
							label.append(',');
						}
						label.append(input[i].getName());
					}
				} else {
					label.append(input.length);
					label.append(" columns");
				}
				label.append(")");
			}

			final Outcome[] requirements = analyzerJob.getRequirements();
			if (requirements != null && requirements.length != 0) {
				label.append(" (");
				for (int i = 0; i < requirements.length; i++) {
					if (i != 0) {
						label.append(" ,");
					}
					appendRequirement(label, requirements[i]);
				}
				label.append(")");
			}
		}

		return label.toString();
	}

	private static void appendRequirement(StringBuilder sb, Outcome req) {
		if (req instanceof FilterOutcome) {
			FilterJob filterJob = ((FilterOutcome) req).getFilterJob();
			Enum<?> category = ((FilterOutcome) req).getCategory();

			String filterLabel = LabelUtils.getLabel(filterJob);

			sb.append(filterLabel);
			sb.append("=");
			sb.append(category);
		} else if (req instanceof MergedOutcome) {
			sb.append('[');
			MergedOutcomeJob mergedOutcomeJob = ((MergedOutcome) req).getMergedOutcomeJob();

			MergeInput[] mergeInputs = mergedOutcomeJob.getMergeInputs();
			for (int i = 0; i < mergeInputs.length; i++) {
				if (i != 0) {
					sb.append(',');
				}
				MergeInput mergeInput = mergeInputs[i];
				Outcome outcome = mergeInput.getOutcome();
				appendRequirement(sb, outcome);
			}
			sb.append(']');
		} else {
			// should not happen
			sb.append(req.toString());
		}
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

	public static String getLabel(String text) {
		if (text == null) {
			return NULL_LABEL;
		}
		if ("".equals(text)) {
			return BLANK_LABEL;
		}
		return text;
	}
}
