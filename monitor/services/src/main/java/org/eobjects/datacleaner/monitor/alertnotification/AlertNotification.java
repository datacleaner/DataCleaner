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
package org.eobjects.datacleaner.monitor.alertnotification;

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.runner.AnalysisJobMetrics;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;

public abstract class AlertNotification implements Runnable {

    private ExecutionLog _execution;
    private AnalysisJobMetrics _metrics;
    private AnalysisJob _job;

    public void execute(ExecutionLog execution, AnalysisJobMetrics metrics, AnalysisJob job) {
        _execution = execution;
        _metrics = metrics;
        _job = job;
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        process(_execution, _metrics, _job);
    }

    public abstract void process(ExecutionLog execution, AnalysisJobMetrics metrics, AnalysisJob job);
}
