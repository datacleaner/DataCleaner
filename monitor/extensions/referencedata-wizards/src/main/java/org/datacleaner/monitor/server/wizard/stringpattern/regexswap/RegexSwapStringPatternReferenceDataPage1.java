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
package org.datacleaner.monitor.server.wizard.stringpattern.regexswap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;
import org.datacleaner.regexswap.Category;

final class RegexSwapStringPatternReferenceDataPage1 extends AbstractFreemarkerWizardPage {
    private static final String PROPERTY_CATEGORY_OPTIONS = "categoryOptions";
    private static final String PROPERTY_CATEGORY = "category";

    private final RegexSwapStringPatternReferenceDataWizardSession _session;

    public RegexSwapStringPatternReferenceDataPage1(RegexSwapStringPatternReferenceDataWizardSession session) {
        _session = session;
    }

    @Override
    public Integer getPageIndex() {
        return 0;
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters)
            throws DCUserInputException {
        _session.setCategory(getString(formParameters, PROPERTY_CATEGORY));

        return new RegexSwapStringPatternReferenceDataPage2(_session);
    }

    @Override
    protected String getTemplateFilename() {
        return "RegexSwapStringPatternReferenceDataPage1.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> model = new HashMap<>();
        model.put(PROPERTY_CATEGORY_OPTIONS, getCategoryOptions());
        model.put(PROPERTY_CATEGORY, _session.getCategory());

        return model;
    }

    private String getCategoryOptions() {
        final StringBuilder builder = new StringBuilder();

        for (Category category : _session.getClient().getCategories()) {
            final String option =
                    String.format("<option value=\"%s\">%s</option>", category.getName(), category.getName());
            builder.append(option);
        }

        return builder.toString();
    }
}
