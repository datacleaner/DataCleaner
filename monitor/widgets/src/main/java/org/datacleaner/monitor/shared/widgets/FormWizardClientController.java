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
package org.datacleaner.monitor.shared.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.monitor.shared.WizardNavigationServiceAsync;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.monitor.shared.model.WizardPage;
import org.datacleaner.monitor.shared.model.WizardSessionIdentifier;

import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NodeCollection;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public final class FormWizardClientController implements WizardClientController {

    private final WizardNavigationServiceAsync _service;
    private final TenantIdentifier _tenant;
    private final WizardPage _wizardPage;
    private final Element _form;

    public FormWizardClientController(final WizardNavigationServiceAsync service, final TenantIdentifier tenant,
            final WizardPage wizardPage) {
        _service = service;
        _tenant = tenant;
        _wizardPage = wizardPage;
        _form = DOM.createForm();
        _form.setInnerHTML(_wizardPage.getFormInnerHtml());
    }

    @Override
    public WizardSessionIdentifier getSessionIdentifier() {
        return _wizardPage.getSessionIdentifier();
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
    public void requestPreviousPage(final AsyncCallback<WizardPage> nextPageCallback) {
        _service.previousPage(_tenant, getSessionIdentifier(), nextPageCallback);
    }

    @Override
    public void requestNextPage(final AsyncCallback<WizardPage> callback) {
        final Map<String, List<String>> formParameters = new HashMap<>();

        final FormElement formElement = FormElement.as(_form);

        final NodeCollection<com.google.gwt.dom.client.Element> inputElements = formElement.getElements();
        for (int i = 0; i < inputElements.getLength(); i++) {
            final Element element = inputElements.getItem(i);

            final String name;
            final String value;
            final boolean included;

            final String tagName = element.getTagName();
            if (tagName.equalsIgnoreCase("input")) {
                final InputElement inputElement = InputElement.as(element);
                name = inputElement.getName();
                value = inputElement.getValue();

                final String type = inputElement.getType();
                if ("checkbox".equals(type) || "radio".equals(type)) {
                    included = inputElement.isChecked();
                } else {
                    included = true;
                }
            } else {
                // useful for eg. <textarea> and <select> element types
                name = element.getPropertyString("name");
                value = element.getPropertyString("value");
                included = true;
            }

            if (included) {
                List<String> valueList = formParameters.get(name);
                if (valueList == null) {
                    valueList = new ArrayList<>();
                    formParameters.put(name, valueList);
                }
                valueList.add(value);
            }
        }

        _service.nextPage(_tenant, _wizardPage.getSessionIdentifier(), formParameters, callback);
    }
}
