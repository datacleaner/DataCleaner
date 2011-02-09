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
