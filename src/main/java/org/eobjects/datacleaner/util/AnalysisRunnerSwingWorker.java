package org.eobjects.datacleaner.util;

import java.util.List;

import javax.swing.SwingWorker;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.job.tasks.Task;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.eobjects.datacleaner.panels.ProgressInformationPanel;
import org.eobjects.datacleaner.windows.ResultWindow;

import dk.eobjects.metamodel.schema.Table;

public class AnalysisRunnerSwingWorker extends SwingWorker<AnalysisResultFuture, Task> implements AnalysisListener {

	private final AnalysisRunner _analysisRunner;
	private final AnalysisJob _job;
	private final ResultWindow _resultWindow;
	private final ProgressInformationPanel _progressInformationPanel;

	public AnalysisRunnerSwingWorker(AnalyzerBeansConfiguration configuration, AnalysisJob job, ResultWindow resultWindow,
			ProgressInformationPanel progressInformationPanel) {
		_analysisRunner = new AnalysisRunnerImpl(configuration, this);
		_job = job;
		_resultWindow = resultWindow;
		_progressInformationPanel = progressInformationPanel;
	}

	@Override
	protected AnalysisResultFuture doInBackground() throws Exception {
		return _analysisRunner.run(_job);
	}

	@Override
	protected void process(List<Task> chunks) {
		for (Task task : chunks) {
			try {
				task.execute();
			} catch (Exception e) {
				_progressInformationPanel.addUserLog(
						"An unexpected error occurred while picking up UI tasks for result handling", e);
			}
		}
	}

	@Override
	public void jobBegin(AnalysisJob job) {
		publish(new Task() {
			@Override
			public void execute() throws Exception {
				_progressInformationPanel.addUserLog("Job begin");
			}
		});
	}

	@Override
	public void jobSuccess(AnalysisJob job) {
		publish(new Task() {
			@Override
			public void execute() throws Exception {
				_progressInformationPanel.addUserLog("Job success");
			}
		});
	}

	@Override
	public void rowProcessingBegin(final AnalysisJob job, final Table table, final int expectedRows) {
		publish(new Task() {
			@Override
			public void execute() throws Exception {
				if (expectedRows == -1) {
					_progressInformationPanel.addUserLog("Starting row processing for " + table.getQualifiedLabel());
				} else {
					_progressInformationPanel.addUserLog("Starting row processing for " + table.getQualifiedLabel()
							+ " (approx. " + expectedRows + " rows)");
				}
			}
		});
	}

	@Override
	public void rowProcessingProgress(AnalysisJob job, Table table, int currentRow) {
		// how much logging do we want here?
	}

	@Override
	public void rowProcessingSuccess(AnalysisJob job, final Table table) {
		publish(new Task() {
			@Override
			public void execute() throws Exception {
				_progressInformationPanel.addUserLog("Row processing for " + table.getQualifiedLabel() + " finished");
			}
		});
	}

	@Override
	public void analyzerBegin(AnalysisJob job, final AnalyzerJob analyzerJob) {
		publish(new Task() {
			@Override
			public void execute() throws Exception {
				_progressInformationPanel.addUserLog("Starting analyzer '" + analyzerJob.getDescriptor().getDisplayName()
						+ "'");
			}
		});
	}

	@Override
	public void analyzerSuccess(AnalysisJob job, final AnalyzerJob analyzerJob, final AnalyzerResult result) {
		SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
		sourceColumnFinder.addSources(job);
		final Table table = sourceColumnFinder.findOriginatingTable(analyzerJob.getInput()[0]);

		publish(new Task() {
			@Override
			public void execute() throws Exception {
				_progressInformationPanel.addUserLog("Analyzer '" + analyzerJob.getDescriptor().getDisplayName()
						+ "' finished");
				_progressInformationPanel.addUserLog("Adding result to tab of " + table.getName());
				_resultWindow.addResult(table, analyzerJob, result);
			}
		});
	}

	@Override
	public void errorInFilter(AnalysisJob job, final FilterJob filterJob, final Throwable throwable) {
		publish(new Task() {
			@Override
			public void execute() throws Exception {
				_progressInformationPanel.addUserLog("An error occurred in the filter: "
						+ filterJob.getDescriptor().getDisplayName(), throwable);
			}
		});
	}

	@Override
	public void errorInTransformer(AnalysisJob job, final TransformerJob transformerJob, final Throwable throwable) {
		publish(new Task() {
			@Override
			public void execute() throws Exception {
				_progressInformationPanel.addUserLog("An error occurred in the transformer: "
						+ transformerJob.getDescriptor().getDisplayName(), throwable);
			}
		});
	}

	@Override
	public void errorInAnalyzer(AnalysisJob job, final AnalyzerJob analyzerJob, final Throwable throwable) {
		publish(new Task() {
			@Override
			public void execute() throws Exception {
				_progressInformationPanel.addUserLog("An error occurred in the analyzer: "
						+ analyzerJob.getDescriptor().getDisplayName(), throwable);
			}
		});
	}

	@Override
	public void errorUknown(AnalysisJob job, final Throwable throwable) {
		publish(new Task() {
			@Override
			public void execute() throws Exception {
				_progressInformationPanel.addUserLog("An error occurred in the analysis job!", throwable);
			}
		});
	}

}
