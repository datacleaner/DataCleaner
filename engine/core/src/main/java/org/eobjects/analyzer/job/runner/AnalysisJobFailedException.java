/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.job.runner;

import java.util.Collections;
import java.util.List;

/**
 * Exception type that indicates that a job execution failed. This exception may
 * potentially hold multiple causes, which can be found using the
 * {@link #getErrors()} method.
 */
public class AnalysisJobFailedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final List<Throwable> _errors;

    public AnalysisJobFailedException(List<Throwable> errors) {
        if (errors == null) {
            _errors = Collections.emptyList();
        } else {
            _errors = errors;
        }
    }

    @Override
    public String getMessage() {
        if (_errors.isEmpty()) {
            return "The analysis ended with an error state, but no exceptions where reported. Please inspect the logs.";
        }

        final StringBuilder sb = new StringBuilder();

        for (Throwable throwable : _errors) {
            if (sb.length() == 0) {
                sb.append("The analysis ended with " + _errors.size() + " errors: [");
            } else {
                sb.append(",");
            }

            final String className = throwable.getClass().getSimpleName();
            sb.append(className);
            sb.append(": ");

            final String message = throwable.getMessage();
            sb.append(message);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Gets all the errors that this exception encapsulates.
     * 
     * @return
     */
    public List<Throwable> getErrors() {
        return _errors;
    }

    @Override
    public Throwable getCause() {
        if (_errors.isEmpty()) {
            return null;
        }
        return _errors.get(0);
    }
}
