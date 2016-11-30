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
import org.datacleaner.reference.regexswap.Category;
import org.datacleaner.reference.regexswap.Regex;
import org.datacleaner.reference.regexswap.RegexSwapClient;

final class RegexSwapStringPatternReferenceDataPageName extends AbstractFreemarkerWizardPage {
    private static final String PROPERTY_NAME_OPTIONS = "nameOptions";
    private static final String PROPERTY_NAME = "name";

    private final RegexSwapStringPatternReferenceDataWizardSession _session;

    public RegexSwapStringPatternReferenceDataPageName(final RegexSwapStringPatternReferenceDataWizardSession session) {
        _session = session;
    }

    @Override
    public Integer getPageIndex() {
        return 1;
    }

    @Override
    public WizardPageController nextPageController(final Map<String, List<String>> formParameters)
            throws DCUserInputException {
        _session.setName(getString(formParameters, PROPERTY_NAME));

        return null;
    }

    @Override
    protected String getTemplateFilename() {
        return "RegexSwapStringPatternReferenceDataPageName.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> model = new HashMap<>();
        model.put(PROPERTY_NAME_OPTIONS, getNameOptions());
        model.put(PROPERTY_NAME, _session.getName());

        return model;
    }

    private String getNameOptions() {
        final StringBuilder builder = new StringBuilder();

        for (final Regex regex : _session.getClient().getRegexes(createCategory())) {
            final String option = String.format("<option value=\"%s\">%s</option>", regex.getName(), regex.getName());
            builder.append(option);
        }

        return builder.toString();
    }

    private Category createCategory() {
        final String categoryName = _session.getCategory();
        final String detailsUrl =
                String.format("%s/%s", RegexSwapClient.REGEXES_URL, categoryName.replaceAll(" ", "%20"));
        return new Category(_session.getCategory(), "", detailsUrl);
    }
}
