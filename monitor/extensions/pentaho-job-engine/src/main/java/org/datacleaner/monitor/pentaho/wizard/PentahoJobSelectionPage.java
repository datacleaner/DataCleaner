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
package org.datacleaner.monitor.pentaho.wizard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.monitor.pentaho.PentahoTransformation;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;

/**
 * Configuration page for Carte connection
 */
abstract class PentahoJobSelectionPage extends AbstractFreemarkerWizardPage {

    private final int _pageIndex;
    private final List<PentahoTransformation> _availableTransformations;

    public PentahoJobSelectionPage(int pageIndex, List<PentahoTransformation> availableTransformations) {
        _pageIndex = pageIndex;
        _availableTransformations = availableTransformations;
    }

    @Override
    public Integer getPageIndex() {
        return _pageIndex;
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters)
            throws DCUserInputException {
        final String groupName = formParameters.get("groupName").get(0);
        final String transformationId = formParameters.get("transformation").get(0);
        for (PentahoTransformation candidate : _availableTransformations) {
            if (candidate.matches(transformationId, null)) {
                return nextPageController(candidate, groupName);
            }
        }
        throw new DCUserInputException("Please select a transformation");
    }

    protected abstract WizardPageController nextPageController(PentahoTransformation transformation, String groupName);

    @Override
    protected String getTemplateFilename() {
        return "PentahoJobSelectionPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("transformations", _availableTransformations);
        map.put("groupName", "Pentaho jobs");
        return map;
    }

}
