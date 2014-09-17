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
package org.eobjects.datacleaner.monitor.server.wizard;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.wizard.WizardContext;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.repository.file.FileRepositoryFolder;

/**
 * Page where user selects which folder the file should be located in
 */
public abstract class ExcelDatastoreLocationWizardPage extends AbstractFreemarkerWizardPage {

    private final WizardContext _wizardContext;
    private final String _filename;
    private final boolean _newFile;

    public ExcelDatastoreLocationWizardPage(WizardContext wizardContext, String filename, boolean newFile) {
        _wizardContext = wizardContext;
        _filename = filename;
        _newFile = newFile;
    }

    @Override
    public Integer getPageIndex() {
        return 1;
    }

    @Override
    protected String getTemplateFilename() {
        return "ExcelDatastoreLocationWizardPage.html";
    }

    @Override
    protected Map<String, Object> getFormModel() {
        final String absolutePrefix = File.listRoots()[0].getAbsolutePath() + "data" + File.separatorChar;

        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("filename", _filename);
        map.put("absolutePrefix", absolutePrefix);

        if (_newFile) {
            map.put("introductionText", "What should be the server location of the Excel spreadsheet:");
            map.put("repositoryText", "Copy it to a location in the repository:");
            map.put("absoluteText", "Copy it to an absolute location on the server:");
        } else {
            map.put("introductionText", "What is the server location of the Excel spreadsheet:");
            map.put("repositoryText", "It's located in the repository:");
            map.put("absoluteText", "It's at an absolute location on the server:");
        }

        return map;
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters)
            throws DCUserInputException {
        final List<String> locations = formParameters.get("location");
        if (locations == null || locations.isEmpty()) {
            throw new DCUserInputException("Please select a location for the Excel spreadsheet");
        }

        final String location = locations.get(0);
        final String filepath;
        final File file;
        if ("repository".equals(location)) {
            filepath = formParameters.get("filepath_repository").get(0);
            final RepositoryFolder tenantFolder = _wizardContext.getTenantContext().getTenantRootFolder();
            if (!(tenantFolder instanceof FileRepositoryFolder)) {
                throw new DCUserInputException("Your repository type is not support for hosting raw data files");
            }

            final FileRepositoryFolder fileRepositoryFolder = (FileRepositoryFolder) tenantFolder;

            file = new File(fileRepositoryFolder.getFile(), filepath);
        } else if ("absolute".equals(location)) {
            filepath = formParameters.get("filepath_absolute").get(0);
            file = new File(filepath);
        } else {
            throw new IllegalArgumentException("Invalid location value: " + location);
        }

        return nextPageController(filepath, file);
    }

    /**
     * Invoked when the user has selected a file location on the server of the
     * Excel spreadsheet.
     * 
     * @param filepath
     * @param file
     * @return
     */
    protected abstract WizardPageController nextPageController(String filepath, File file);

}
