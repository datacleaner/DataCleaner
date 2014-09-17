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
package org.eobjects.datacleaner.monitor.server.wizard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;

/**
 * Wizard page for entering the base url of a SugarCRM system
 */
public abstract class SugarCrmBaseUrlWizardPage extends AbstractFreemarkerWizardPage {

    private String _baseUrl = "http://localhost/sugarcrm";

    @Override
    public Integer getPageIndex() {
        return 0;
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters) throws DCUserInputException {
        _baseUrl = getString(formParameters, "baseUrl");
        if (StringUtils.isNullOrEmpty(_baseUrl)) {
            throw new DCUserInputException("Please provide a valid base URL.");
        }
        return nextPageController(_baseUrl);
    }

    /**
     * Invoked when a base URL has been entered and 'next' is clicked.
     * 
     * @param baseUrl
     * @return
     */
    protected abstract WizardPageController nextPageController(String baseUrl);

    @Override
    protected String getTemplateFilename() {
        return "SugarCrmBaseUrlWizardPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("baseUrl", _baseUrl);
        return map;
    }

}
