/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

import java.util.concurrent.ExecutionException;

import org.eobjects.analyzer.util.StringUtils;

/**
 * Utility/convenience methods for handling errors
 */
public class ErrorUtils {

    /**
     * Unwraps an exception for presentation to the user. This method will
     * remove unnecesary/irrelevant wrapping exceptions if possible.
     * 
     * @param throwable
     * @return
     */
    public static Throwable unwrapForPresentation(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        if (throwable instanceof ExecutionException || throwable instanceof IllegalStateException
                || throwable instanceof RuntimeException) {
            // unwrap causes from concurrent execution exceptions
            final Throwable cause = throwable.getCause();
            final String message = throwable.getMessage();
            if (cause != null && (StringUtils.isNullOrEmpty(message) || cause.toString().equals(message))) {
                throwable = cause;
            }
        }
        return throwable;
    }
}
