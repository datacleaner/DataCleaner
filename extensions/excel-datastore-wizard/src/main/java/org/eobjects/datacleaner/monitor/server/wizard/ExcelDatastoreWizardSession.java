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

import java.io.File;

import javax.xml.parsers.DocumentBuilder;

import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardSession;
import org.eobjects.metamodel.util.FileHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ExcelDatastoreWizardSession implements DatastoreWizardSession {

    private final DatastoreWizardContext _context;

    private String _filepath;

    public ExcelDatastoreWizardSession(DatastoreWizardContext context) {
        _context = context;
    }

    @Override
    public WizardPageController firstPageController() {
        return new ExcelDatastoreUploadOrExistingFileWizardPage(_context) {
            @Override
            protected WizardPageController nextPageControllerUpload(String filename, final File tempFile) {
                return new ExcelDatastoreLocationWizardPage(_context, filename, true) {

                    @Override
                    protected WizardPageController nextPageController(String filepath, File file) {
                        final File directory = file.getParentFile();
                        if (!directory.exists() && !directory.mkdirs()) {
                            throw new DCUserInputException("Could not create directory for file:\n" + filepath);
                        }
                        if (!directory.canWrite()) {
                            throw new DCUserInputException("Cannot write data to directory of file:\n" + filepath);
                        }

                        FileHelper.copy(tempFile, file);
                        _filepath = filepath;
                        return null;
                    }
                };
            }

            @Override
            protected WizardPageController nextPageControllerExisting() {
                return new ExcelDatastoreLocationWizardPage(_context, "my_spreadsheet.xlsx", false) {

                    @Override
                    protected WizardPageController nextPageController(String filepath, File file) {
                        if (!filepath.toLowerCase().endsWith(".xls")) {
                            if (!filepath.toLowerCase().endsWith(".xlsx")) {
                                // only .csv and .tsv files are allowed to be
                                // referenced on the server, for security
                                // reasons.
                                throw new DCUserInputException(
                                        "For security reasons, only existing .xls and .xlsx files can be referenced on the server");
                            }
                        }

                        if (file.exists() && !file.canRead()) {
                            throw new DCUserInputException("Cannot read from file:\n" + filepath);
                        }

                        _filepath = filepath;
                        return null;
                    }
                };
            }
        };
    }

    @Override
    public Integer getPageCount() {
        return 2;
    }

    @Override
    public Element createDatastoreElement(DocumentBuilder documentBuilder) {
        final Document doc = documentBuilder.newDocument();
        final Element ds = doc.createElement("excel-datastore");

        final Element filename = doc.createElement("filename");
        filename.setTextContent(_filepath);
        ds.appendChild(filename);

        return ds;
    }

}
