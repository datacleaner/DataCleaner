/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;

/**
 * Page for entering a name and a description for a datastore.
 */
public abstract class DatastoreNameAndDescriptionWizardPage extends AbstractFreemarkerWizardPage {

    private final DatastoreWizardContext _context;
    private final int _pageIndex;
    private final String _suggestedName;
    private final String _suggestedDescription;

    public DatastoreNameAndDescriptionWizardPage(DatastoreWizardContext context, int pageIndex, String suggestedName,
            String suggestedDescription) {
        _context = context;
        _pageIndex = pageIndex;
        _suggestedName = (suggestedName == null ? "" : suggestedName);
        _suggestedDescription = (suggestedDescription == null ? "" : suggestedDescription);
    }

    public DatastoreNameAndDescriptionWizardPage(DatastoreWizardContext context, int pageIndex, String suggestedName) {
        this(context, pageIndex, suggestedName, null);
    }

    public DatastoreNameAndDescriptionWizardPage(DatastoreWizardContext context, int pageIndex) {
        this(context, pageIndex, null);
    }

    @Override
    public Integer getPageIndex() {
        return _pageIndex;
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters)
            throws DCUserInputException {
        final String name = formParameters.get("name").get(0);

        if (StringUtils.isNullOrEmpty(name)) {
            throw new DCUserInputException("Please provide a datastore name.");
        }

        final TenantContext tenantContext = _context.getTenantContext();

        final Datastore datastore = tenantContext.getConfiguration().getDatastoreCatalog().getDatastore(name);
        if (datastore != null) {
            throw new DCUserInputException("A datastore with the name '" + name + "' already exist.");
        }

        final String description = formParameters.get("description").get(0);

        return nextPageController(name, description);
    }

    protected abstract WizardPageController nextPageController(String name, String description);

    @Override
    protected String getTemplateFilename() {
        return "DatastoreNameAndDescriptionWizardPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", _suggestedName);
        map.put("description", _suggestedDescription);
        return map;
    }

}
