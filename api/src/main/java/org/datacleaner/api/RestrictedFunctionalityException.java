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
package org.datacleaner.api;

/**
 * An exception that {@link Component}s can throw when certain functionality is
 * restricted - typically because it's a paid for function or because an
 * approval process is pending or so.
 * 
 * Typically the user interface will have special handling available for this
 * exception type to guide him in the direction of unlocking the restricted
 * functionality.
 * 
 * This class and {@link RestrictedFunctionalityMessage} are two ways to
 * archieve something quite similar. The main difference is that a
 * {@link RestrictedFunctionalityMessage} can be used in scenarios where a job
 * is allowed to finish with just a partially functional result (for instance a
 * transformer may change it's behaviour after the message is sent) whereas the
 * {@link RestrictedFunctionalityException} will cancel the job because the
 * functionality is simply unavailable or a partial result is not granted
 * either.
 */
public class RestrictedFunctionalityException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final RestrictedFunctionalityCallToAction[] _callToActions;

    /**
     * Constructs a {@link RestrictedFunctionalityException}
     * 
     * @param message
     *            a message to the user about what and why he is being
     *            restricted in functionality.
     * @param callToActions
     *            an array of call to actions for the user to pick from.
     */
    public RestrictedFunctionalityException(String message, RestrictedFunctionalityCallToAction... callToActions) {
        super(message, null, true, false);
        _callToActions = callToActions;
    }

    /**
     * Constructs a {@link RestrictedFunctionalityException}
     * 
     * @param message
     *            a message to the user about what and why he is being
     *            restricted in functionality.
     * @param cause
     *            an underlying cause of the restriction
     * @param callToActions
     *            an array of call to actions for the user to pick from.
     */
    public RestrictedFunctionalityException(String message, Throwable cause,
            RestrictedFunctionalityCallToAction... callToActions) {
        super(message, cause, true, false);
        _callToActions = callToActions;
    }

    public RestrictedFunctionalityCallToAction[] getCallToActions() {
        return _callToActions;
    }
}
