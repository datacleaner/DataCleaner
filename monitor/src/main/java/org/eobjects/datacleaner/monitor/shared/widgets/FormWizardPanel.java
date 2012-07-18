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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.datacleaner.monitor.shared.JobWizardServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardPage;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.NodeCollection;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

final class FormWizardPanel implements WizardPanel {

    private final JobWizardServiceAsync _service;
    private final TenantIdentifier _tenant;
    private final JobWizardPage _wizardPage;
    private final Element _form;

    public FormWizardPanel(JobWizardServiceAsync service, TenantIdentifier tenant, JobWizardPage wizardPage) {
        _service = service;
        _tenant = tenant;
        _wizardPage = wizardPage;
        _form = DOM.createForm();
        _form.setInnerHTML(_wizardPage.getFormInnerHtml());
    }

    @Override
    public Widget asWidget() {
        // form element needs to be appended to body before the HTMLPanel can
        // wrap it.
        final BodyElement body = Document.get().getBody();
        body.appendChild(_form);

        final HTMLPanel htmlPanel = HTMLPanel.wrap(_form);
        return htmlPanel;
    }

    @Override
    public void requestNextPage(AsyncCallback<JobWizardPage> callback) {
        Map<String, List<String>> formParameters = new HashMap<String, List<String>>();

        FormElement formElement = FormElement.as(_form);

        final NodeCollection<com.google.gwt.dom.client.Element> inputElements = formElement.getElements();
        for (int i = 0; i < inputElements.getLength(); i++) {
            final Element inputElement = inputElements.getItem(i);

            final String name = inputElement.getPropertyString("name");
            final String value = inputElement.getPropertyString("value");
            
            List<String> valueList = formParameters.get(name);
            if (valueList == null) {
                valueList = new ArrayList<String>();
                formParameters.put(name, valueList);
            }
            valueList.add(value);
        }

        _service.nextPage(_tenant, _wizardPage.getSessionIdentifier(), formParameters, callback);
    }

}
