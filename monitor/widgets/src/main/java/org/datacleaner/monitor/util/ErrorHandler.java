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
package org.datacleaner.monitor.util;

import java.util.Set;

import org.datacleaner.monitor.shared.JavaScriptCallbacks;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.shared.widgets.ButtonPanel;
import org.datacleaner.monitor.shared.widgets.DCButtons;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DialogBox.Caption;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.web.bindery.event.shared.UmbrellaException;

/**
 * Utility class for handling errors in GWT code
 */
public class ErrorHandler {

    /**
     * This dialog box instance will be shared for all messages
     */
    private static DialogBox sharedDialogBox;

    private static DialogBox getDialogBox() {
        if (sharedDialogBox == null) {
            final Caption caption = new DialogBox.CaptionImpl();
            caption.setText("Error");
            sharedDialogBox = new DialogBox(false, true, caption);
            sharedDialogBox.addStyleName("ErrorDialog");
        }
        return sharedDialogBox;
    }

    /**
     * This method first call handleError method which is a native method to
     * call onError JS function, in case onError JS method is not present on
     * page it simply shows an error dialog as an alert.
     *
     * @param headerMessage
     * @param additionalDetails
     * @param t
     */
    public static void showErrorDialog(final String headerMessage, final String additionalDetails, final Throwable t) {
        final String oneLineMessage = buildOneLineMessage(headerMessage, additionalDetails, t);

        if (t instanceof DCUserInputException) {
            GWT.log("User input exception", t);
            if (JavaScriptCallbacks.onError(oneLineMessage, true)) {
                return;
            }
            Window.alert(oneLineMessage);
            return;
        }

        GWT.log("Uncaught exception", t);
        GWT.log("Additional details: " + additionalDetails);

        final String detailsMessage;
        if (t instanceof UmbrellaException) {
            // sometimes several exceptions are bundled in an UmbrellaException
            final Set<Throwable> causes = ((UmbrellaException) t).getCauses();
            if (causes.size() == 1) {
                final Throwable cause = causes.iterator().next();
                showErrorDialog(cause.getMessage(), additionalDetails, cause);
                return;
            }
            final StringBuilder messageBuilder = new StringBuilder();
            for (final Throwable cause : causes) {
                if (messageBuilder.length() != 0) {
                    messageBuilder.append("\n");
                }
                messageBuilder.append(cause);
                messageBuilder.append(": ");
                messageBuilder.append(cause.getMessage());
            }
            detailsMessage = messageBuilder.toString();
        } else {
            detailsMessage = t.getMessage();
        }

        if (JavaScriptCallbacks.onError(oneLineMessage, false)) {
            return;
        }

        final String details = detailsMessage + "\n\n" + t;
        showErrorDialog(headerMessage, details, additionalDetails);
    }

    private static String buildOneLineMessage(final String headerMessage, final String additionalDetails,
            final Throwable t) {
        final StringBuilder oneLineMessage = new StringBuilder();
        if (headerMessage != null && !"".equals(headerMessage)) {
            oneLineMessage.append(headerMessage);
        }

        if (additionalDetails != null && !"".equals(additionalDetails)) {
            if (oneLineMessage.length() != 0) {
                oneLineMessage.append(": ");
            }
            oneLineMessage.append(additionalDetails);
        }

        final String exceptionMessage = t.getMessage();
        if (exceptionMessage != null && !"".equals(exceptionMessage)
                && oneLineMessage.indexOf(exceptionMessage) == -1) {
            if (oneLineMessage.length() != 0) {
                oneLineMessage.append(": ");
            }
            oneLineMessage.append(exceptionMessage);
        }
        return oneLineMessage.toString();
    }

    /**
     * Shows an error dialog
     *
     * @param message
     * @param res
     */
    public static void showErrorDialog(final String message, final Response res) {
        final String mainMessage = message + ":\n" + res.getStatusCode() + ":" + res.getStatusText();
        final String details = res.getText();
        showErrorDialog(mainMessage, details, (String) null);
    }

    /**
     * Shows an error dialog
     *
     * @param message
     */
    public static void showErrorDialog(final String message) {
        showErrorDialog(message, (String) null, (String) null);
    }

    /**
     * Shows an error dialog
     *
     * @param messageHeader
     * @param details
     * @param additionalDetails
     */
    public static void showErrorDialog(final String messageHeader, final String details,
            final String additionalDetails) {
        final DialogBox dialogBox = getDialogBox();

        final FlowPanel panel = new FlowPanel();
        final Label messageLabel = new Label(messageHeader);
        messageLabel.addStyleName("Message");
        panel.add(messageLabel);

        if (details != null && details.trim().length() > 0) {
            final TextArea textArea = new TextArea();
            textArea.setText(details);
            textArea.setReadOnly(true);
            textArea.addStyleName("Details");

            panel.add(textArea);
        }

        if (additionalDetails != null && additionalDetails.trim().length() > 0) {
            final TextArea textArea = new TextArea();
            textArea.setText(additionalDetails);
            textArea.setReadOnly(true);
            textArea.addStyleName("AdditionalMessageDetails");

            panel.add(textArea);
        }

        final Button closeButton = DCButtons.primaryButton(null, "Close");
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                dialogBox.hide();
            }
        });

        final ButtonPanel buttonPanel = new ButtonPanel();
        buttonPanel.addButton(closeButton);
        panel.add(buttonPanel);

        dialogBox.setWidget(panel);
        dialogBox.center();
        dialogBox.show();
    }

    /**
     * Creates an {@link UncaughtExceptionHandler} for use by
     * {@link GWT#setUncaughtExceptionHandler(UncaughtExceptionHandler)}
     *
     * @return
     */
    public static UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return new UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(final Throwable e) {
                showErrorDialog("Unexpected error occurred", (String) null, e);
            }
        };
    }

}
