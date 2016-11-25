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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.WizardContext;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;
import org.datacleaner.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Page where user has to select if Excel spreadsheet is already existing on server, or
 * if he wants to upload it.
 */
public abstract class ExcelDatastoreUploadOrExistingFileWizardPage extends AbstractFreemarkerWizardPage {

    private static final Logger logger = LoggerFactory.getLogger(ExcelDatastoreUploadOrExistingFileWizardPage.class);

    private final WizardContext _wizardContext;

    public ExcelDatastoreUploadOrExistingFileWizardPage(final WizardContext wizardContext) {
        _wizardContext = wizardContext;
    }

    @Override
    public Integer getPageIndex() {
        return 0;
    }

    @Override
    protected String getTemplateFilename() {
        return "ExcelDatastoreUploadOrExistingFileWizardPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        return new HashMap<>();
    }

    @Override
    public WizardPageController nextPageController(final Map<String, List<String>> formParameters)
            throws DCUserInputException {
        final List<String> sources = formParameters.get("source");
        if (sources == null || sources.isEmpty()) {
            throw new DCUserInputException("Please select a source for the Excel spreadsheet");
        }

        final String source = sources.get(0);
        if ("existing".equals(source)) {
            return nextPageControllerExisting();
        } else if ("upload".equals(source)) {
            return nextPageControllerUpload(formParameters);
        } else {
            throw new IllegalStateException("Unexpected source type: " + source);
        }
    }

    /**
     * Invoked when the user continues to the next page, after selecting to use
     * a file that already exist on the server.
     *
     * @return
     */
    protected abstract WizardPageController nextPageControllerExisting();

    /**
     * Invoked when the user continues to the next page, after selecting to
     * upload a file to the server.
     *
     * @param filename
     *            the name of the file
     * @param tempFile
     *            a temporary file containing the uploaded data
     * @return
     */
    protected abstract WizardPageController nextPageControllerUpload(String filename, File tempFile);

    private WizardPageController nextPageControllerUpload(final Map<String, List<String>> formParameters) {
        final List<String> excelSpreadsheetUploads = formParameters.get("excel_spreadsheet_upload");
        if (excelSpreadsheetUploads == null || excelSpreadsheetUploads.isEmpty()) {
            throw new DCUserInputException("Please upload a file before clicking 'Next'!");
        }
        final String fileJsonString = excelSpreadsheetUploads.get(0);
        if (StringUtils.isNullOrEmpty(fileJsonString)) {
            throw new DCUserInputException("Please upload a file before clicking 'Next'!");
        }

        final Map<String, String> fileJson = parseJson(fileJsonString);
        final String sessionKey = fileJson.get("session_key");
        final String filename = fileJson.get("file_name");

        final File tempFile = (File) _wizardContext.getHttpSession().eval(sessionKey);

        return nextPageControllerUpload(filename, tempFile);
    }

    private Map<String, String> parseJson(final String fileJsonString) {
        try {
            @SuppressWarnings("unchecked") final Map<String, String> map =
                    new ObjectMapper().readValue(fileJsonString, Map.class);
            return map;
        } catch (final Exception e) {
            logger.warn("Could not parse form result as JSON: {}", fileJsonString);
            if (fileJsonString.indexOf('\n') != -1) {
                // it's a multi-line response, probably showing an exception or
                // http error message
                throw new IllegalStateException(e);
            }
            // typically this occurs because the user has not yet uploaded the
            // file.
            throw new DCUserInputException("Please upload the file before proceeding");
        }
    }

}
