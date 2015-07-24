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

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.ComponentMessage;
import org.datacleaner.api.InputRow;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AnalysisListener} that wraps a list of inner listeners. Makes life
 * easier for the invokers of the listeners.
 */
public final class CompositeAnalysisListener implements AnalysisListener {
    private static final Logger logger = LoggerFactory.getLogger(CompositeAnalysisListener.class);

    private final List<AnalysisListener> _delegates;

    public CompositeAnalysisListener(AnalysisListener[] delegates) {
        _delegates = new ArrayList<>(delegates.length);
        for (AnalysisListener analysisListener : delegates) {
            addDelegate(analysisListener);
        }
    }

    public CompositeAnalysisListener(AnalysisListener firstDelegate, AnalysisListener... delegates) {
        _delegates = new ArrayList<>(1 + delegates.length);
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
            try {
                delegate.jobBegin(job, metrics);
            } catch (Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public void onComponentMessage(AnalysisJob job, ComponentJob componentJob, ComponentMessage message) {
        for (AnalysisListener delegate : _delegates) {
            try {
                delegate.onComponentMessage(job, componentJob, message);
            } catch (Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public void jobSuccess(AnalysisJob job, AnalysisJobMetrics metrics) {
        for (AnalysisListener delegate : _delegates) {
            try {
                delegate.jobSuccess(job, metrics);
            } catch (Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public void rowProcessingBegin(AnalysisJob job, RowProcessingMetrics metrics) {
        for (AnalysisListener delegate : _delegates) {
            try {
                delegate.rowProcessingBegin(job, metrics);
            } catch (Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public void rowProcessingProgress(AnalysisJob job, RowProcessingMetrics metrics, InputRow row, int currentRow) {
        for (AnalysisListener delegate : _delegates) {
            try {
                delegate.rowProcessingProgress(job, metrics, row, currentRow);
            } catch (Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public void rowProcessingSuccess(AnalysisJob job, RowProcessingMetrics metrics) {
        for (AnalysisListener delegate : _delegates) {
            try {
                delegate.rowProcessingSuccess(job, metrics);
            } catch (Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public void componentBegin(AnalysisJob job, ComponentJob componentJob, ComponentMetrics metrics) {
        for (AnalysisListener delegate : _delegates) {
            try {
                delegate.componentBegin(job, componentJob, metrics);
            } catch (Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public void componentSuccess(AnalysisJob job, ComponentJob componentJob, AnalyzerResult result) {
        for (AnalysisListener delegate : _delegates) {
            try {
                delegate.componentSuccess(job, componentJob, result);
            } catch (Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public void errorInComponent(AnalysisJob job, ComponentJob componentJob, InputRow row, Throwable throwable) {
        for (AnalysisListener delegate : _delegates) {
            try {
                delegate.errorInComponent(job, componentJob, row, throwable);
            } catch (Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public void errorUnknown(AnalysisJob job, Throwable throwable) {
        for (AnalysisListener delegate : _delegates) {
            try {
                delegate.errorUnknown(job, throwable);
            } catch (Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getSimpleName(), e);
            }
        }
    }
}
