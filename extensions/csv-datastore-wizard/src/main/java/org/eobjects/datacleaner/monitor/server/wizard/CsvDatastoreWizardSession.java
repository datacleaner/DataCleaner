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

import org.eobjects.analyzer.configuration.DatastoreXmlExternalizer;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.datastore.AbstractDatastoreWizardSession;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;
import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
import org.w3c.dom.Element;

/**
 * Wizard session for creating a CSV file datastore.
 */
public class CsvDatastoreWizardSession extends AbstractDatastoreWizardSession {

    private File _file;
    private String _filepath;
    private CsvConfiguration _configuration;
    private String _name;
    private String _description;

    public CsvDatastoreWizardSession(DatastoreWizardContext context) {
        super(context);
    }

    @Override
    public Integer getPageCount() {
        return 4;
    }

    @Override
    public WizardPageController firstPageController() {
        return new CsvDatastoreUploadOrExistingFileWizardPage(getWizardContext()) {

            @Override
            protected WizardPageController nextPageControllerUpload(final String filename, final File tempFile) {
                return new CsvDatastoreLocationWizardPage(getWizardContext(), filename, true) {

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
                        _file = file;
                        _filepath = filepath;
                        return showCsvConfigurationPage(filename);
                    }
                };
            }

            @Override
            protected WizardPageController nextPageControllerExisting() {
                return new CsvDatastoreLocationWizardPage(getWizardContext(), "my_file.csv", false) {

                    @Override
                    protected WizardPageController nextPageController(String filepath, File file) {
                        if (!filepath.toLowerCase().endsWith(".csv")) {
                            if (!filepath.toLowerCase().endsWith(".tsv")) {
                                if (!filepath.toLowerCase().endsWith(".txt")) {
                                    // only .csv and .tsv files are allowed to
                                    // be referenced on the server, for security
                                    // reasons.
                                    throw new DCUserInputException(
                                            "For security reasons, only existing .csv, .tsv or .txt files can be referenced on the server");
                                }
                            }
                        }

                        if (file.exists() && !file.canRead()) {
                            throw new DCUserInputException("Cannot read from file:\n" + filepath);
                        }

                        _file = file;
                        _filepath = filepath;
                        return showCsvConfigurationPage(file.getName());
                    }
                };
            }
        };
    }

    /**
     * Invoked when a file has been selected as a source for the datastore. At
     * this point we will prompt the user to fill in {@link CsvConfiguration}
     * items such as separator char, quote char, escape char, header line
     * number, encoding etc.
     * 
     * @return
     */
    protected WizardPageController showCsvConfigurationPage(final String filename) {
        return new CsvConfigurationWizardPage(_file) {
            @Override
            protected WizardPageController nextPageController(CsvConfiguration configuration) {
                _configuration = configuration;
                return new DatastoreNameAndDescriptionWizardPage(getWizardContext(), 3, filename) {

                    @Override
                    protected WizardPageController nextPageController(String name, String description) {
                        _name = name;
                        _description = description;
                        return null;
                    }
                };
            }
        };
    }

    @Override
    public Element createDatastoreElement(DocumentBuilder documentBuilder) {
        final Resource resource = new FileResource(_file);
        final CsvDatastore datastore = new CsvDatastore(_name, resource, _configuration);
        datastore.setDescription(_description);

        final DatastoreXmlExternalizer externalizer = new DatastoreXmlExternalizer();
        final Element element = externalizer.toElement(datastore, _filepath);
        return element;
    }
}
