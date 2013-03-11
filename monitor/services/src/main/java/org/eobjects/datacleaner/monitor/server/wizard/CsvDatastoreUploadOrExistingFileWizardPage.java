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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.wizard.WizardContext;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;

/**
 * Page where user has to select if CSV file is already existing on server, or
 * if he wants to upload it.
 */
public abstract class CsvDatastoreUploadOrExistingFileWizardPage extends AbstractFreemarkerWizardPage {

    private final WizardContext _wizardContext;

    public CsvDatastoreUploadOrExistingFileWizardPage(WizardContext wizardContext) {
        _wizardContext = wizardContext;
    }

    @Override
    public Integer getPageIndex() {
        return 0;
    }

    @Override
    protected String getTemplateFilename() {
        return "CsvDatastoreUploadOrExistingFileWizardPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        return new HashMap<String, Object>();
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters)
            throws DCUserInputException {
        final List<String> sources = formParameters.get("source");
        if (sources == null || sources.isEmpty()) {
            throw new DCUserInputException("Please select a source for the CSV file");
        }

        final String source = sources.get(0);
        if ("existing".equals(source)) {
            // TODO
            return null;
        } else if ("upload".equals(source)) {
            return nextPageControllerUpload(formParameters);
        } else {
            throw new IllegalStateException("Unexpected source type: " + source);
        }
    }

    private WizardPageController nextPageControllerUpload(Map<String, List<String>> formParameters) {
        final List<String> csvFileUploads = formParameters.get("csv_file_upload");
        if (csvFileUploads == null || csvFileUploads.isEmpty()) {
            throw new DCUserInputException("Please upload a file before clicking 'Next'!");
        }
        final String fileJsonString = csvFileUploads.get(0);
        if (fileJsonString == null) {
            throw new DCUserInputException("Please upload a file before clicking 'Next'!");
        }

        final Map<String, String> fileJson = parseJson(fileJsonString);
        final String sessionKey = fileJson.get("session_key");
        final String filename = fileJson.get("file_name");

        final File tempFile = (File) _wizardContext.getHttpSession().eval(sessionKey);

        return nextPageControllerUpload(filename, tempFile);
    }

    protected abstract WizardPageController nextPageControllerUpload(String filename, File tempFile);

    private Map<String, String> parseJson(String fileJsonString) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> map = new ObjectMapper().readValue(fileJsonString, Map.class);
            return map;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
