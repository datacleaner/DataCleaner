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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.datacleaner.api.InputRow;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.FilterJob;
import org.datacleaner.job.TransformerJob;
import org.datacleaner.job.concurrent.PreviousErrorsExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AnalysisListener that will register errors
 * 
 * 
 */
final class ErrorAwareAnalysisListener extends AnalysisListenerAdaptor implements ErrorAware {

    private static final Logger logger = LoggerFactory.getLogger(ErrorAwareAnalysisListener.class);

    private final List<Throwable> _errors = new LinkedList<Throwable>();
    private final AtomicBoolean _cancelled = new AtomicBoolean(false);

    protected void handleError(AnalysisJob job, Throwable throwable) {
        if (throwable instanceof AnalysisJobCancellation) {
            _cancelled.set(true);
        }

        if (!(throwable instanceof PreviousErrorsExistException)) {
            logger.warn("Exception stack trace:", throwable);
        }

        synchronized (_errors) {
            if (!_errors.contains(throwable)) {
                _errors.add(throwable);
            }
        }

        // check for SQLException.getNextException() which is particularly
        // important and unfortunately NOT included by default in stack traces.
        Throwable t = throwable;
        while (t != null) {
            if (t instanceof SQLException) {
                SQLException nextException = ((SQLException) t).getNextException();
                if (nextException != null) {
                    logger.warn("SQLException.getNextException() stack trace:", nextException);
                }
            }
            t = t.getCause();
        }
    }

    @Override
    public List<Throwable> getErrors() {
        // create a copy to avoid mutations or concurrent modifications
        final List<Throwable> result;
        synchronized (_errors) {
            result = new ArrayList<Throwable>(_errors);
        }
        return result;
    }

    @Override
    public boolean isErrornous() {
        synchronized (_errors) {
            return !_errors.isEmpty();
        }
    }

    @Override
    public void errorInFilter(AnalysisJob job, FilterJob filterJob, InputRow row, Throwable throwable) {
        logger.warn("errorInFilter({},{},{},{})", new Object[] { job, filterJob, row, throwable });
        handleError(job, throwable);
    }

    @Override
    public void errorInTransformer(AnalysisJob job, TransformerJob transformerJob, InputRow row, Throwable throwable) {
        logger.warn("errorInTransformer({},{},{},{})", new Object[] { job, transformerJob, row, throwable });
        handleError(job, throwable);
    }

    @Override
    public void errorInAnalyzer(AnalysisJob job, AnalyzerJob analyzerJob, InputRow row, Throwable throwable) {
        logger.warn("errorInAnalyzer({},{},{},{})", new Object[] { job, analyzerJob, row, throwable });
        handleError(job, throwable);
    }

    @Override
    public void errorUknown(AnalysisJob job, Throwable throwable) {
        logger.warn("errorUnknown({},{})", new Object[] { job, throwable });
        handleError(job, throwable);
    }

    @Override
    public boolean isCancelled() {
        return _cancelled.get();
    }
}
