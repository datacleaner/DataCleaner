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
package org.eobjects.datacleaner.monitor.util;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

/**
 * Convenient abstract implementation of the {@link RequestCallback} which
 * handles error situations.
 * 
 * @see RequestCallback
 */
public abstract class DCRequestCallback implements RequestCallback {

    @Override
    public void onResponseReceived(Request request, Response response) {
        final int statusCode = response.getStatusCode();
        if (statusCode == 200) {
            onSuccess(request, response);
            return;
        }

        onNonSuccesfullStatusCode(request, response, statusCode, response.getStatusText());
    }

    protected abstract void onSuccess(Request request, Response response);

    @Override
    public void onError(Request request, Throwable exception) {
        ErrorHandler.showErrorDialog("", null, exception);
    }

    public void onNonSuccesfullStatusCode(Request request, Response response, int statusCode, String statusText) {
        ErrorHandler.showErrorDialog("Server reported error (HTTP " + statusCode + ")", statusText, response.getText());
    }

}
