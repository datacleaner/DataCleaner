/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.monitor.shared.widgets;

import org.eobjects.datacleaner.monitor.server.media.FileUploadServlet;
import org.eobjects.datacleaner.monitor.util.Urls;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Handler class that registers a method for uploading files to the
 * {@link FileUploadServlet}.
 */
public class FileUploadFunctionHandler {

    public static void uploadFile(String fileUploadElementId) {
        final Element element = Document.get().getElementById(fileUploadElementId);

        final InputElement inputElement = getFileInput(element);
        if (inputElement == null) {
            throw new IllegalArgumentException("No file input found within element id: " + fileUploadElementId);
        }

        GWT.log("Found file input element: " + inputElement);

        final String inputName = inputElement.getName();
        final Element parent = inputElement.getParentElement();

        parent.setInnerHTML("<div class='loader'></div>");

        // use "contentType" param because form submission requires everything
        // to be text/html
        final String url = Urls.createRelativeUrl("util/upload?contentType=text/html");

        final RootPanel rootPanel = RootPanel.get();

        final FormPanel form = new FormPanel();
        form.setVisible(false);
        form.setAction(url);
        form.setMethod(FormPanel.METHOD_POST);
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.getElement().appendChild(inputElement);
        form.addSubmitCompleteHandler(new SubmitCompleteHandler() {

            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                GWT.log("File upload form submit complete!");

                final String stringResponse = event.getResults();
                final JSONValue jsonResponse = JSONParser.parseLenient(stringResponse);
                final JSONArray jsonFiles = jsonResponse.isObject().get("files").isArray();
                final JSONValue jsonFile = jsonFiles.get(0);
                final String jsonFileStr = jsonFile.toString();

                parent.setInnerHTML("<p>File uploaded!</p><input type='hidden' name='" + inputName + "' value='"
                        + jsonFileStr + "' />");
                rootPanel.remove(form);
            }
        });

        rootPanel.add(form);

        GWT.log("Submitting hidden file upload form");

        form.submit();
    }

    private static InputElement getFileInput(Element element) {
        if (element == null) {
            return null;
        }

        if (InputElement.TAG.equalsIgnoreCase(element.getTagName())) {
            final InputElement input = InputElement.as(element);
            if ("file".equals(input.getType())) {
                return input;
            }
        }

        final NodeList<Node> nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            final Node node = nodes.getItem(i);
            if (Element.is(node)) {
                InputElement input = getFileInput(Element.as(node));
                if (input != null) {
                    return input;
                }
            }
        }
        return null;
    }

    /**
     * Exports the "uploadFile(elementId)" method as a function in the native
     * javascript scope.
     */
    public static native void exportFileUploadFunction() /*-{
                                            $wnd.uploadFile = $entry(@org.eobjects.datacleaner.monitor.shared.widgets.FileUploadFunctionHandler::uploadFile(Ljava/lang/String;));
                                            }-*/;
}
