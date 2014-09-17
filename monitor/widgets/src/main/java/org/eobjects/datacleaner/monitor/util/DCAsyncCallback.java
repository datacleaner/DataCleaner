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
package org.eobjects.datacleaner.monitor.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.gwt.user.client.rpc.StatusCodeException;

/**
 * Convenient abstract implementation of the {@link AsyncCallback} which handles
 * error situations.
 * 
 * @see AsyncCallback
 * 
 * @param <T>
 *            the payload of the response.
 */
public abstract class DCAsyncCallback<T> implements AsyncCallback<T> {

    @Override
    public void onFailure(Throwable e) {
        GWT.log("Error occurred", e);

        if (e instanceof InvocationException) {
            String message = e.getMessage();
            if (message != null && message.indexOf("j_spring_security_check") != -1) {
                // user has been logged out, reload the page!
                Window.Location.reload();
                return;
            }
        }

        if (e instanceof StatusCodeException) {
            final String response = ((StatusCodeException) e).getEncodedResponse();

            GWT.log("Encoded error response: " + response);

            final int statusCode = ((StatusCodeException) e).getStatusCode();

            ErrorHandler.showErrorDialog("Server reported HTTP error: " + statusCode, response, e);
        } else {
            ErrorHandler.showErrorDialog("", null, e);
        }
    }
}
