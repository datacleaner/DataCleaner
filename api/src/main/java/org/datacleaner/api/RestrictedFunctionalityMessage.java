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
 * {@link ComponentMessage} that can be published (via {@link ComponentContext}
 * by components if certain functionality is restricted - typically because it's
 * a paid for function or because an approval process is pending or so.
 * 
 * Typically the user interface will have special handling available for this
 * message type to guide him in the direction of unlocking the restricted
 * functionality.
 * 
 * Publishing the message once per job should be sufficient. Dispatching of more
 * than one such message may be disregarded by the user interface.
 * 
 * This class and {@link RestrictedFunctionalityException} are two ways to
 * archieve something quite similar. The main difference is that a
 * {@link RestrictedFunctionalityMessage} can be used in scenarios where a job
 * is allowed to finish with just a partially functional result (for instance a
 * transformer may change it's behaviour after the message is sent) whereas the
 * {@link RestrictedFunctionalityException} will cancel the job because the
 * functionality is simply unavailable or a partial result is not granted
 * either.
 */
public class RestrictedFunctionalityMessage implements ComponentMessage {

    private final String _message;
    private final RestrictedFunctionalityCallToAction[] _callToActions;

    /**
     * Constructs a {@link RestrictedFunctionalityMessage}
     * 
     * @param message
     *            a message to the user about what and why he is being
     *            restricted in functionality.
     * @param callToActions
     *            an array of call to actions for the user to pick from.
     */
    public RestrictedFunctionalityMessage(String message, RestrictedFunctionalityCallToAction... callToActions) {
        _message = message;
        _callToActions = callToActions;
    }

    public String getMessage() {
        return _message;
    }

    public RestrictedFunctionalityCallToAction[] getCallToActions() {
        return _callToActions;
    }
}
