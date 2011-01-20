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
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.eobjects.datacleaner.panels.ProgressInformationPanel;
import org.eobjects.datacleaner.windows.ResultWindow;
import org.eobjects.metamodel.schema.Table;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AnalysisRunnerSwingWorker extends SwingWorker<AnalysisResultFuture, Void> implements AnalysisListener {

	private static final Logger logger = LoggerFactory.getLogger(AnalysisRunnerSwingWorker.class);
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
		try {
			return _analysisRunner.run(_job);
		} catch (final Exception e) {
			logger.error("Unexpected error occurred when invoking run(...) on AnalysisRunner", e);
			errorUknown(_job, e);
			throw e;
		}
	}

	@Override
	public void jobBegin(AnalysisJob job) {
		String now = new DateTime().toString(DateTimeFormat.fullTime());
		_progressInformationPanel.addUserLog("Job begin (" + now + ")");
	}

	@Override
	public void jobSuccess(AnalysisJob job) {
		String now = new DateTime().toString(DateTimeFormat.fullTime());
		_progressInformationPanel.addUserLog("Job success (" + now + ")");
	}

	@Override
	public void rowProcessingBegin(final AnalysisJob job, final Table table, final int expectedRows) {
		if (expectedRows == -1) {
			_progressInformationPanel.addUserLog("Starting row processing for " + table.getQualifiedLabel());
		} else {
			_progressInformationPanel.addUserLog("Starting row processing for " + table.getQualifiedLabel() + " (approx. "
					+ expectedRows + " rows)");
			_progressInformationPanel.setExpectedRows(table, expectedRows);
		}
	}

	@Override
	public void rowProcessingProgress(AnalysisJob job, final Table table, final int currentRow) {
		_progressInformationPanel.updateProgress(table, currentRow);
	}

	@Override
	public void rowProcessingSuccess(AnalysisJob job, final Table table) {
		_progressInformationPanel.addUserLog("Row processing for " + table.getQualifiedLabel() + " finished");
	}

	@Override
	public void analyzerBegin(AnalysisJob job, final AnalyzerJob analyzerJob) {
		_progressInformationPanel.addUserLog("Starting analyzer '" + analyzerJob.getDescriptor().getDisplayName() + "'");
	}

	@Override
	public void analyzerSuccess(AnalysisJob job, final AnalyzerJob analyzerJob, final AnalyzerResult result) {
		SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
		sourceColumnFinder.addSources(job);
		final Table table = sourceColumnFinder.findOriginatingTable(analyzerJob.getInput()[0]);

		_progressInformationPanel.addUserLog("Analyzer '" + analyzerJob.getDescriptor().getDisplayName() + "' finished");
		_progressInformationPanel.addUserLog("Adding result to tab of " + table.getName());
		_resultWindow.addResult(table, analyzerJob, result);
	}

	@Override
	public void errorInFilter(AnalysisJob job, final FilterJob filterJob, final Throwable throwable) {
		_progressInformationPanel.addUserLog("An error occurred in the filter: "
				+ filterJob.getDescriptor().getDisplayName(), throwable);
	}

	@Override
	public void errorInTransformer(AnalysisJob job, final TransformerJob transformerJob, final Throwable throwable) {
		_progressInformationPanel.addUserLog("An error occurred in the transformer: "
				+ transformerJob.getDescriptor().getDisplayName(), throwable);
	}

	@Override
	public void errorInAnalyzer(AnalysisJob job, final AnalyzerJob analyzerJob, final Throwable throwable) {
		_progressInformationPanel.addUserLog("An error occurred in the analyzer: "
				+ analyzerJob.getDescriptor().getDisplayName(), throwable);
	}

	@Override
	public void errorUknown(AnalysisJob job, final Throwable throwable) {
		_progressInformationPanel.addUserLog("An error occurred in the analysis job!", throwable);
	}

}
