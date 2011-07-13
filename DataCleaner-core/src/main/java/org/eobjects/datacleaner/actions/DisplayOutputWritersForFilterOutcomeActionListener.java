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
package org.eobjects.datacleaner.actions;

import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.datacleaner.output.beans.AbstractOutputWriterAnalyzer;

/**
 * Action that displays output writers for a filter's outcome.
 * 
 * @author Kasper Sørensen
 */
public class DisplayOutputWritersForFilterOutcomeActionListener extends AbstractDisplayOutputWritersActionListener {

	private final FilterJobBuilder<?, ?> _filterJobBuilder;
	private final String _categoryName;

	public DisplayOutputWritersForFilterOutcomeActionListener(AnalyzerBeansConfiguration configuration,
			AnalysisJobBuilder analysisJobBuilder, FilterJobBuilder<?, ?> filterJobBuilder, String categoryName) {
		super(configuration, analysisJobBuilder);
		_filterJobBuilder = filterJobBuilder;
		_categoryName = categoryName;
	}

	@Override
	protected void configure(AnalysisJobBuilder analysisJobBuilder,
			RowProcessingAnalyzerJobBuilder<? extends RowProcessingAnalyzer<?>> analyzerJobBuilder) {
		RowProcessingAnalyzer<?> analyzer = analyzerJobBuilder.getConfigurableBean();
		if (analyzer instanceof AbstractOutputWriterAnalyzer) {
			((AbstractOutputWriterAnalyzer) analyzer).configureForFilterOutcome(analysisJobBuilder,
					_filterJobBuilder.getDescriptor(), _categoryName);
		}
		analyzerJobBuilder.setRequirement(_filterJobBuilder, _categoryName);
	}

}
