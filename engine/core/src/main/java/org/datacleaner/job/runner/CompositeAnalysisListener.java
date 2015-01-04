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
package org.datacleaner.job.runner;

import java.util.ArrayList;
import java.util.List;

import org.datacleaner.beans.api.ComponentMessage;
import org.datacleaner.data.InputRow;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.result.AnalyzerResult;

/**
 * {@link AnalysisListener} that wraps a list of inner listeners. Makes life
 * easier for the invokers of the listeners.
 */
public final class CompositeAnalysisListener implements AnalysisListener {

    private final List<AnalysisListener> _delegates;

    public CompositeAnalysisListener(AnalysisListener[] delegates) {
        _delegates = new ArrayList<AnalysisListener>(delegates.length);
        for (AnalysisListener analysisListener : delegates) {
            addDelegate(analysisListener);
        }
    }

    public CompositeAnalysisListener(AnalysisListener firstDelegate, AnalysisListener... delegates) {
        _delegates = new ArrayList<AnalysisListener>(1 + delegates.length);
        addDelegate(firstDelegate);
        for (AnalysisListener analysisListener : delegates) {
            addDelegate(analysisListener);
        }
    }

    /**
     * Adds a delegate to this {@link CompositeAnalysisListener}.
     * 
     * @param analysisListener
     */
    public void addDelegate(AnalysisListener analysisListener) {
        if (analysisListener == null) {
            return;
        }
        _delegates.add(analysisListener);
    }

    /**
     * Determines if this {@link CompositeAnalysisListener} is empty (i.e. has
     * no delegates)
     * 
     * @return
     */
    public boolean isEmpty() {
        return _delegates.isEmpty();
    }

    /**
     * Gets the number of delegates
     * 
     * @return
     */
    public int size() {
        return _delegates.size();
    }

    @Override
    public void jobBegin(AnalysisJob job, AnalysisJobMetrics metrics) {
        for (AnalysisListener delegate : _delegates) {
            delegate.jobBegin(job, metrics);
        }
    }

    @Override
    public void onComponentMessage(AnalysisJob job, ComponentJob componentJob, ComponentMessage message) {
        for (AnalysisListener delegate : _delegates) {
            delegate.onComponentMessage(job, componentJob, message);
        }
    }

    @Override
    public void jobSuccess(AnalysisJob job, AnalysisJobMetrics metrics) {
        for (AnalysisListener delegate : _delegates) {
            delegate.jobSuccess(job, metrics);
        }
    }

    @Override
    public void rowProcessingBegin(AnalysisJob job, RowProcessingMetrics metrics) {
        for (AnalysisListener delegate : _delegates) {
            delegate.rowProcessingBegin(job, metrics);
        }
    }

    @Override
    public void rowProcessingProgress(AnalysisJob job, RowProcessingMetrics metrics, InputRow row, int currentRow) {
        for (AnalysisListener delegate : _delegates) {
            delegate.rowProcessingProgress(job, metrics, row, currentRow);
        }
    }

    @Override
    public void rowProcessingSuccess(AnalysisJob job, RowProcessingMetrics metrics) {
        for (AnalysisListener delegate : _delegates) {
            delegate.rowProcessingSuccess(job, metrics);
        }
    }

    @Override
    public void analyzerBegin(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerMetrics metrics) {
        for (AnalysisListener delegate : _delegates) {
            delegate.analyzerBegin(job, analyzerJob, metrics);
        }
    }

    @Override
    public void analyzerSuccess(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerResult result) {
        for (AnalysisListener delegate : _delegates) {
            delegate.analyzerSuccess(job, analyzerJob, result);
        }
    }

    @Override
    public void errorInComponent(AnalysisJob job, ComponentJob componentJob, InputRow row, Throwable throwable) {
        for (AnalysisListener delegate : _delegates) {
            delegate.errorInComponent(job, componentJob, row, throwable);
        }
    }

    @Override
    public void errorUknown(AnalysisJob job, Throwable throwable) {
        for (AnalysisListener delegate : _delegates) {
            delegate.errorUknown(job, throwable);
        }
    }
}
