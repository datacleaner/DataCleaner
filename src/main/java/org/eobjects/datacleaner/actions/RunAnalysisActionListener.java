package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.windows.ResultWindow;

public class RunAnalysisActionListener implements ActionListener {

	private final AnalysisJobBuilder _analysisJobBuilder;
	private final AnalyzerBeansConfiguration _configuration;

	public RunAnalysisActionListener(AnalysisJobBuilder analysisJobBuilder, AnalyzerBeansConfiguration configuration) {
		super();
		_analysisJobBuilder = analysisJobBuilder;
		_configuration = configuration;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		AnalysisJob job = _analysisJobBuilder.toAnalysisJob();
		ResultWindow window = new ResultWindow(_configuration, job);
		window.setVisible(true);
		window.startAnalysis();
	}

}
