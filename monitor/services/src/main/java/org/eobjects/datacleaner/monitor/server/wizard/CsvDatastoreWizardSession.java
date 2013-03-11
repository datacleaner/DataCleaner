package org.eobjects.datacleaner.monitor.server.wizard;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;

import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardContext;
import org.eobjects.datacleaner.monitor.wizard.datastore.DatastoreWizardSession;
import org.eobjects.metamodel.util.FileHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CsvDatastoreWizardSession implements DatastoreWizardSession {

    private final DatastoreWizardContext _context;
    private int _pageCount;
    private File _file;
    private String _filepath;

    public CsvDatastoreWizardSession(DatastoreWizardContext context) {
        _context = context;
        _pageCount = 3;
    }

    @Override
    public Integer getPageCount() {
        return _pageCount;
    }

    @Override
    public WizardPageController firstPageController() {
        return new CsvDatastoreUploadOrExistingFileWizardPage(_context) {

            @Override
            protected WizardPageController nextPageControllerUpload(final String filename, final File tempFile) {
                return new CsvDatastoreLocationWizardPage(_context, filename) {

                    @Override
                    protected WizardPageController nextPageController(String filepath, File file) {
                        _file = file;
                        _file.getParentFile().mkdirs();
                        FileHelper.copy(tempFile, _file);
                        _filepath = filepath;
                        return null;
                    }
                };
            }
        };
    }

    @Override
    public Element createDatastoreElement(DocumentBuilder documentBuilder) {
        final Document document = documentBuilder.newDocument();

        final Element filename = document.createElement("filename");
        filename.setTextContent(_filepath);

        final Element datastore = document.createElement("csv-datastore");
        datastore.setAttribute("name", _context.getDatastoreName());
        datastore.appendChild(filename);

        return datastore;
    }
}
