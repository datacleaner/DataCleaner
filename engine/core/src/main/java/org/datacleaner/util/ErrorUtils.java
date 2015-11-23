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

import java.util.concurrent.ExecutionException;

import com.google.common.base.Strings;

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
        while (throwable instanceof ExecutionException || throwable instanceof IllegalStateException
                || throwable.getClass() == RuntimeException.class) {
            // unwrap causes from wrapping exceptions
            final Throwable cause = throwable.getCause();
            if (cause == null) {
                break;
            }
            final String message = throwable.getMessage();
            if (Strings.isNullOrEmpty(message) || cause.toString().equals(message)) {
                // run another iteration
                throwable = cause;
            } else {
                break;
            }
        }
        return throwable;
    }
}
