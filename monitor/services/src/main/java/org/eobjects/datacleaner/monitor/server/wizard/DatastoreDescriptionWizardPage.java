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
package org.eobjects.datacleaner.monitor.server.wizard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;

/**
 * Page for entering a description for a datastore.
 */
final class DatastoreDescriptionWizardPage extends AbstractFreemarkerWizardPage {

    private final DatastoreDescriptionCallback _callback;
    private final int _pageIndex;

    public DatastoreDescriptionWizardPage(int pageIndex, DatastoreDescriptionCallback callback) {
        _pageIndex = pageIndex;
        _callback = callback;
    }

    @Override
    public Integer getPageIndex() {
        return _pageIndex;
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters)
            throws DCUserInputException {
        String description = formParameters.get("description").get(0);

        return _callback.nextPageController(description);
    }

    @Override
    protected String getTemplateFilename() {
        return "DatastoreDescriptionWizardPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        return new HashMap<String, Object>();
    }

}
