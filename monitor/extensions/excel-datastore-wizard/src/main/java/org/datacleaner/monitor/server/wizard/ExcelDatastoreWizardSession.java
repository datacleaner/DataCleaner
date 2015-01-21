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

import javax.xml.parsers.DocumentBuilder;

import org.datacleaner.configuration.DatastoreXmlExternalizer;
import org.datacleaner.connection.ExcelDatastore;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.datastore.AbstractDatastoreWizardSession;
import org.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.w3c.dom.Element;

public class ExcelDatastoreWizardSession extends AbstractDatastoreWizardSession {

    private String _filepath;
    private String _name;
    private String _description;

    public ExcelDatastoreWizardSession(DatastoreWizardContext context) {
        super(context);
    }

    @Override
    public WizardPageController firstPageController() {
        return new ExcelDatastoreUploadOrExistingFileWizardPage(getWizardContext()) {
            @Override
            protected WizardPageController nextPageControllerUpload(final String filename, final File tempFile) {
                return new ExcelDatastoreLocationWizardPage(getWizardContext(), filename, true) {

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

                        return createNameAndDescriptionWizardPage(filename);
                    }
                };
            }

            @Override
            protected WizardPageController nextPageControllerExisting() {
                return new ExcelDatastoreLocationWizardPage(getWizardContext(), "my_spreadsheet.xlsx", false) {

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
                        return createNameAndDescriptionWizardPage(file.getName());
                    }
                };
            }
        };
    }

    private WizardPageController createNameAndDescriptionWizardPage(String name) {
        return new DatastoreNameAndDescriptionWizardPage(getWizardContext(), 2, name) {
            @Override
            protected WizardPageController nextPageController(String name, String description) {
                _name = name;
                _description = description;
                return null;
            }
        };
    }

    @Override
    public Integer getPageCount() {
        return 3;
    }

    @Override
    public Element createDatastoreElement(DocumentBuilder documentBuilder) {
        final DatastoreXmlExternalizer externalizer = new DatastoreXmlExternalizer();

        final File file = new File(_filepath);
        final ExcelDatastore datastore = new ExcelDatastore(_name, new FileResource(file), _filepath);
        datastore.setDescription(_description);

        final Element ds = externalizer.toElement(datastore, _filepath);
        return ds;
    }

}
