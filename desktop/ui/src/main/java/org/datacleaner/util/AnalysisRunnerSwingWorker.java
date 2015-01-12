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
package org.datacleaner.util;

import javax.swing.SwingWorker;

import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.runner.AnalysisListener;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.windows.ResultWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link SwingWorker} that fires the execution of an {@link AnalysisJob} and
 * publishes updated to a {@link ResultWindow}.
 */
public final class AnalysisRunnerSwingWorker extends SwingWorker<AnalysisResultFuture, Void> {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisRunnerSwingWorker.class);
    private final AnalysisRunner _analysisRunner;
    private final AnalysisJob _job;
    private final ResultWindow _resultWindow;
    private AnalysisResultFuture _resultFuture;

    public AnalysisRunnerSwingWorker(AnalyzerBeansConfiguration configuration, AnalysisJob job,
            ResultWindow resultWindow) {
        final AnalysisListener analysisListener = resultWindow.createAnalysisListener();
        _analysisRunner = new AnalysisRunnerImpl(configuration, analysisListener);
        _job = job;
        _resultWindow = resultWindow;
    }

    @Override
    protected AnalysisResultFuture doInBackground() throws Exception {
        try {
            _resultFuture = _analysisRunner.run(_job);
            return _resultFuture;
        } catch (final Exception e) {
            logger.error("Unexpected error occurred when invoking run(...) on AnalysisRunner", e);
            _resultWindow.onUnexpectedError(_job, e);
            throw e;
        }
    }

    public void cancelIfRunning() {
        if (_resultFuture != null) {
            if (!_resultFuture.isDone()) {
                _resultFuture.cancel();
            }
        }
    }
}
