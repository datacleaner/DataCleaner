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
package org.datacleaner.monitor.referencedata.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class UploadFormWidget extends FormPanel {
    private static final int SPACING = 10;
    private static final String FILE_UPLOAD_ACTION = "/repository/{tenant}/referencedata";
    
    public UploadFormWidget(String tenantId) {
        setAction(FILE_UPLOAD_ACTION.replace("{tenant}", tenantId));
        setEncoding(FormPanel.ENCODING_MULTIPART);
        setMethod(FormPanel.METHOD_POST);
        final HorizontalPanel formPanel = new HorizontalPanel();
        setWidget(formPanel);
        formPanel.setSpacing(SPACING);
        formPanel.add(new Label("Upload new data: "));
        final FileUpload upload = new FileUpload();
        upload.setName("file");
        formPanel.add(upload);
        formPanel.add(new Button("Upload", new ClickHandler() { // do not use lambda, java 1.7
            @Override
            public void onClick(ClickEvent event) {
                submit();
            }
        }));
        addSubmitHandler(new FormPanel.SubmitHandler() { // do not use lambda, java 1.7
            @Override
            public void onSubmit(FormPanel.SubmitEvent event) {
                if (upload.getFilename().length() == 0) {
                    Window.alert("Please select a file to upload. ");
                    event.cancel();
                }
            }
        });
        addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() { // do not use lambda, java 1.7
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                Window.Location.reload();
            }
        });
    }
}
