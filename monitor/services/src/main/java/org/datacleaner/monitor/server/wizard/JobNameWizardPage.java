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
package org.datacleaner.monitor.server.wizard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;
import org.datacleaner.monitor.wizard.job.JobWizardContext;
import org.datacleaner.util.StringUtils;

/**
 * Page for entering a name for a job.
 */
public abstract class JobNameWizardPage extends AbstractFreemarkerWizardPage {

    private final JobWizardContext _context;
    private final int _pageIndex;
    private final String _suggestedName;

    public JobNameWizardPage(final JobWizardContext context, final int pageIndex, final String suggestedName) {
        _context = context;
        _pageIndex = pageIndex;
        _suggestedName = (suggestedName == null ? "" : suggestedName);
    }

    public JobNameWizardPage(final JobWizardContext context, final int pageIndex) {
        this(context, pageIndex, null);
    }

    @Override
    public Integer getPageIndex() {
        return _pageIndex;
    }

    @Override
    protected Class<?> getTemplateFriendlyClass() {
        return JobNameWizardPage.class;
    }

    @Override
    public WizardPageController nextPageController(final Map<String, List<String>> formParameters)
            throws DCUserInputException {
        final String name = formParameters.get("name").get(0);

        if (StringUtils.isNullOrEmpty(name)) {
            throw new DCUserInputException("Please provide a job name.");
        }

        final TenantContext tenantContext = _context.getTenantContext();

        final boolean exists = tenantContext.containsJob(name);
        if (exists) {
            throw new DCUserInputException("A job with the name '" + name + "' already exist.");
        }

        return nextPageController(name);
    }

    protected abstract WizardPageController nextPageController(String name);

    @Override
    protected String getTemplateFilename() {
        return "JobNameWizardPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> map = new HashMap<>();
        map.put("name", _suggestedName);
        return map;
    }

}
