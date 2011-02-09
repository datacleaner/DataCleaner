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

import java.util.List;

import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;

public class DisplayOutputWritersForTransformedDataActionListener extends AbstractDisplayOutputWritersActionListener {

	private final TransformerJobBuilder<?> _transformerJobBuilder;

	public DisplayOutputWritersForTransformedDataActionListener(AnalyzerBeansConfiguration configuration,
			AnalysisJobBuilder analysisJobBuilder, TransformerJobBuilder<?> transformerJobBuilder) {
		super(configuration, analysisJobBuilder);
		_transformerJobBuilder = transformerJobBuilder;
	}

	@Override
	protected void configure(AnalysisJobBuilder analysisJobBuilder,
			RowProcessingAnalyzerJobBuilder<? extends RowProcessingAnalyzer<?>> analyzerJobBuilder) {
		List<InputColumn<?>> inputColumns = _transformerJobBuilder.getInputColumns();
		List<MutableInputColumn<?>> outputColumns = _transformerJobBuilder.getOutputColumns();
		analyzerJobBuilder.clearInputColumns();
		analyzerJobBuilder.addInputColumns(inputColumns);
		analyzerJobBuilder.addInputColumns(outputColumns);
	}

}
