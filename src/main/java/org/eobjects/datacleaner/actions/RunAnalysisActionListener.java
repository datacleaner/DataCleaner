package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.datacleaner.windows.ProgressBarWindow;
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
		// TODO: Use swingworker in stead
		new Thread() {
			@Override
			public void run() {
				final ProgressBarWindow progressBarWindow = new ProgressBarWindow();
				progressBarWindow.setVisible(true);

				AnalysisJob job = _analysisJobBuilder.toAnalysisJob();

				AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(_configuration, progressBarWindow).run(job);

				resultFuture.await();
				if (resultFuture.isSuccessful()) {
					List<AnalyzerResult> results = resultFuture.getResults();
					new ResultWindow(results).setVisible(true);
				} else {
					List<Throwable> errors = resultFuture.getErrors();
					for (Throwable throwable : errors) {
						System.out.println("ERR:");
						throwable.printStackTrace();
					}
				}
			}
		}.start();
	}

}
