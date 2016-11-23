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

    public CompositeAnalysisListener(final AnalysisListener[] delegates) {
        _delegates = new ArrayList<>(delegates.length);
        for (final AnalysisListener analysisListener : delegates) {
            addDelegate(analysisListener);
        }
    }

    public CompositeAnalysisListener(final AnalysisListener firstDelegate, final AnalysisListener... delegates) {
        _delegates = new ArrayList<>(1 + delegates.length);
        addDelegate(firstDelegate);
        for (final AnalysisListener analysisListener : delegates) {
            addDelegate(analysisListener);
        }
    }

    /**
     * Adds a delegate to this {@link CompositeAnalysisListener}.
     *
     * @param analysisListener
     */
    public void addDelegate(final AnalysisListener analysisListener) {
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
    public void jobBegin(final AnalysisJob job, final AnalysisJobMetrics metrics) {
        for (final AnalysisListener delegate : _delegates) {
            try {
                delegate.jobBegin(job, metrics);
            } catch (final Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getName(), e);
            }
        }
    }

    @Override
    public void onComponentMessage(final AnalysisJob job, final ComponentJob componentJob,
            final ComponentMessage message) {
        for (final AnalysisListener delegate : _delegates) {
            try {
                delegate.onComponentMessage(job, componentJob, message);
            } catch (final Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getName(), e);
            }
        }
    }

    @Override
    public void jobSuccess(final AnalysisJob job, final AnalysisJobMetrics metrics) {
        for (final AnalysisListener delegate : _delegates) {
            try {
                delegate.jobSuccess(job, metrics);
            } catch (final Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getName(), e);
            }
        }
    }

    @Override
    public void rowProcessingBegin(final AnalysisJob job, final RowProcessingMetrics metrics) {
        for (final AnalysisListener delegate : _delegates) {
            try {
                delegate.rowProcessingBegin(job, metrics);
            } catch (final Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getName(), e);
            }
        }
    }

    @Override
    public void rowProcessingProgress(final AnalysisJob job, final RowProcessingMetrics metrics, final InputRow row,
            final int currentRow) {
        for (final AnalysisListener delegate : _delegates) {
            try {
                delegate.rowProcessingProgress(job, metrics, row, currentRow);
            } catch (final Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getName(), e);
            }
        }
    }

    @Override
    public void rowProcessingSuccess(final AnalysisJob job, final RowProcessingMetrics metrics) {
        for (final AnalysisListener delegate : _delegates) {
            try {
                delegate.rowProcessingSuccess(job, metrics);
            } catch (final Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getName(), e);
            }
        }
    }

    @Override
    public void componentBegin(final AnalysisJob job, final ComponentJob componentJob, final ComponentMetrics metrics) {
        for (final AnalysisListener delegate : _delegates) {
            try {
                delegate.componentBegin(job, componentJob, metrics);
            } catch (final Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getName(), e);
            }
        }
    }

    @Override
    public void componentSuccess(final AnalysisJob job, final ComponentJob componentJob, final AnalyzerResult result) {
        for (final AnalysisListener delegate : _delegates) {
            try {
                delegate.componentSuccess(job, componentJob, result);
            } catch (final Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getName(), e);
            }
        }
    }

    @Override
    public void errorInComponent(final AnalysisJob job, final ComponentJob componentJob, final InputRow row,
            final Throwable throwable) {
        for (final AnalysisListener delegate : _delegates) {
            try {
                delegate.errorInComponent(job, componentJob, row, throwable);
            } catch (final Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getName(), e);
            }
        }
    }

    @Override
    public void errorUnknown(final AnalysisJob job, final Throwable throwable) {
        for (final AnalysisListener delegate : _delegates) {
            try {
                delegate.errorUnknown(job, throwable);
            } catch (final Exception e) {
                logger.warn("Listener {} failed", delegate.getClass().getName(), e);
            }
        }
    }
}
