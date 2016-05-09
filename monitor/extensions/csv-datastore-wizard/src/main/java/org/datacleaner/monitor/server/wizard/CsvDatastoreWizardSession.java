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

import org.apache.commons.io.FilenameUtils;
import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
import org.datacleaner.configuration.DomConfigurationWriter;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.datastore.AbstractDatastoreWizardSession;
import org.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;
import org.w3c.dom.Element;


/**
 * Wizard session for creating a CSV file datastore.
 */
public class CsvDatastoreWizardSession extends AbstractDatastoreWizardSession {

    private Resource _resource;
    private CsvConfiguration _configuration;
    private String _name;
    private String _description;
    final static String[] extensions = new String[]{"csv", "tsv","txt"}; 

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
                    protected WizardPageController nextPageController(Resource resource) {
                        if (resource instanceof FileResource){
                        final File file = ((FileResource) resource).getFile();
                        final File directory = file.getParentFile();
                        if (!directory.exists() && !directory.mkdirs()) {
                            throw new DCUserInputException("Could not create directory for file:\n" + file.getAbsolutePath());
                        }
                        if (!directory.canWrite()) {
                            throw new DCUserInputException("Cannot write data to directory of file:\n" + file.getAbsolutePath());
                        }

                        FileHelper.copy(tempFile, file);
                        }
                        _resource = resource;
                        return showCsvConfigurationPage(filename);
                    }
                };
            }

            @Override
            protected WizardPageController nextPageControllerExisting() {
                return new CsvDatastoreLocationWizardPage(getWizardContext(), "my_file.csv", false) {

                    @Override
                    protected WizardPageController nextPageController(Resource resource) {
                        if (resource instanceof FileResource) {
                            final File file = ((FileResource) resource).getFile();
                            final String filepath = file.getAbsolutePath();
                            if (!FilenameUtils.isExtension(file.getName() , extensions)) {
                                        // only .csv and .tsv files are allowed
                                        // to
                                        // be referenced on the server, for
                                        // security
                                        // reasons.
                                        throw new DCUserInputException(
                                                "For security reasons, only existing .csv, .tsv or .txt files can be referenced on the server");
                                    }
                                if (file.exists() && !file.canRead()) {
                                    throw new DCUserInputException("Cannot read from file:\n" + filepath);
                                }
                            }
                        _resource = resource;
                        return showCsvConfigurationPage(resource.getName());
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
        return new CsvConfigurationWizardPage(_resource) {
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
        final CsvDatastore datastore = new CsvDatastore(_name, _resource, _configuration);
        datastore.setDescription(_description);
        final DomConfigurationWriter writer = new DomConfigurationWriter(); 
        final Element element = writer.externalize(datastore); 
        return element;
    }
}
