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
package org.eobjects.datacleaner.monitor.pentaho.wizard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;

/**
 * Configuration page for Carte connection
 */
abstract class PentahoCarteConfigurationPage extends AbstractFreemarkerWizardPage {
    
    private final int _pageIndex;

    public PentahoCarteConfigurationPage(int pageIndex) {
        _pageIndex = pageIndex;
    }

    @Override
    public Integer getPageIndex() {
        return _pageIndex;
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters)
            throws DCUserInputException {
        final String hostname = formParameters.get("hostname").get(0);
        final String portStr = formParameters.get("port").get(0);
        final int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            throw new DCUserInputException("Not a valid port number: " + portStr);
        }

        final String username = formParameters.get("username").get(0);
        final String password = formParameters.get("password").get(0);
        return nextPageController(hostname, port, username, password);
    }

    protected abstract WizardPageController nextPageController(String hostname, int port, String username,
            String password);

    @Override
    protected String getTemplateFilename() {
        return "PentahoCarteConfigurationPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("hostname", "localhost");
        map.put("port", "8081");
        map.put("username", "cluster");
        map.put("password", "cluster");
        return map;
    }

}
