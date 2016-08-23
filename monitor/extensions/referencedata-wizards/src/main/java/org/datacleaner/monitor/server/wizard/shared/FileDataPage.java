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
package org.datacleaner.monitor.server.wizard.shared;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.htrace.fasterxml.jackson.databind.ObjectMapper;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;
import org.datacleaner.util.StringUtils;

public abstract class FileDataPage extends AbstractFreemarkerWizardPage {
    private static final String PROPERTY_CASE_SENSITIVE = "caseSensitive";
    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_ENCODING = "encoding";
    private static final String PROPERTY_FILE = "reference_data_file";
    private static final String PROPERTY_FILE_NAME = "file_name";
    private static final String PROPERTY_SESSION_KEY = "session_key";

    protected final FileWizardSession _session; 
    
    public FileDataPage(FileWizardSession session) {
        _session = session;
    }
    
    @Override
    public Integer getPageIndex() {
        return 0;
    }
    
    @Override
    protected Map<String, Object> getFormModel() {
        final Map<String, Object> model = new HashMap<>();
        model.put("name", _session.getName());
        model.put("filePath", _session.getFilePath());
        model.put("encoding", _session.getEncoding());
        model.put("caseSensitive", _session.getCaseSensitive());

        return model;
    }
    
    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters)
            throws DCUserInputException {
        storeFileProperties(formParameters, _session);

        return null;
    } 
    
    protected void storeFileProperties(Map<String, List<String>> formParameters, FileWizardSession session) {
        final String caseSensitive = getBoolean(formParameters, PROPERTY_CASE_SENSITIVE) ? "on" : "";
        final String name = getString(formParameters, PROPERTY_NAME);
        final String encoding = getString(formParameters, PROPERTY_ENCODING);

        session.setName(name);
        session.setEncoding(encoding);
        session.setCaseSensitive(caseSensitive);

        final List<String> fileUploads = formParameters.get(PROPERTY_FILE);

        if (fileUploads == null || fileUploads.isEmpty() || StringUtils.isNullOrEmpty(fileUploads.get(0))) {
            throw new DCUserInputException("Please upload a file before clicking 'Next'!");
        }

        final Map<String, String> fileJson = parseJson(fileUploads.get(0));
        final String sessionKey = fileJson.get(PROPERTY_SESSION_KEY);
        final String fileName = fileJson.get(PROPERTY_FILE_NAME);
        session.setSessionKey(sessionKey);
        session.setFilePath(fileName);
    }

    private Map<String, String> parseJson(String fileJsonString) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> map = new ObjectMapper().readValue(fileJsonString, Map.class);
            return map;
        } catch (Exception e) {
            if (fileJsonString.indexOf('\n') != -1) {
                throw new IllegalStateException(e);
            }

            throw new DCUserInputException("Please upload the file before proceeding. ");
        }
    }
}
