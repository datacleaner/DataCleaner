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
package org.datacleaner.monitor.server.wizard.stringpattern.regex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.monitor.server.wizard.shared.ReferenceDataHelper;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;

final class RegexStringPatternReferenceDataPage extends AbstractFreemarkerWizardPage {
    
    private static final String PROPERTY_MATCH_ENTIRE_STRING = "matchEntireString";
    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_EXPRESSION = "expression";

    private final RegexStringPatternReferenceDataWizardSession _session;

    public RegexStringPatternReferenceDataPage(RegexStringPatternReferenceDataWizardSession session) {
        _session = session;
    }

    @Override
    public Integer getPageIndex() {
        return 0;
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters)
            throws DCUserInputException {
        final String name = getString(formParameters, PROPERTY_NAME);
        final String expression = getString(formParameters, PROPERTY_EXPRESSION);
        final String matchEntireString = getString(formParameters, PROPERTY_MATCH_ENTIRE_STRING);
        ReferenceDataHelper.checkUniqueStringPattern(name, _session.getWizardContext().getTenantContext()
                .getConfiguration().getReferenceDataCatalog()); 
        
        _session.setName(name);
        _session.setExpression(expression);
        _session.setMatchEntireString(matchEntireString);
        
        return null;
    }

    @Override
    protected String getTemplateFilename() {
        return "RegexStringPatternReferenceDataPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> model = new HashMap<>();
        model.put("name", _session.getName());
        model.put("expression", _session.getExpression());
        model.put("matchEntireString", _session.getMatchEntireString());
        
        return model;
    }
}
