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
package org.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.DataSetTableModel;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.FileDatastore;
import org.datacleaner.connection.ResourceDatastore;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.ImmutableEntry;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.AbstractResourceTextField;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DescriptionLabel;
import org.datacleaner.widgets.FileSelectionListener;
import org.datacleaner.widgets.FilenameTextField;
import org.datacleaner.widgets.LoadingIcon;
import org.datacleaner.widgets.table.DCTable;
import org.jdesktop.swingx.JXStatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass for rather simple file-based datastores such as Excel-datastores,
 * Access-datastores, dBase-datastores etc.
 * 
 * @param <D>
 *            the type of datastore
 */
public abstract class AbstractFileBasedDatastoreDialog<D extends Datastore> extends AbstractDatastoreDialog<D> {

    private static final long serialVersionUID = 1L;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Amount of bytes to read for autodetection of encoding, separator and
     * quotes
     */
    private static final int SAMPLE_BUFFER_SIZE = 128 * 1024;

    /**
     * Max amount of columns to display in the preview table
     */
    private static final int PREVIEW_COLUMNS = 10;

    private final FilenameTextField _filenameField;
    private final DCPanel _previewTablePanel;
    private final DCTable _previewTable;
    private final LoadingIcon _loadingIcon;

    protected AbstractFileBasedDatastoreDialog(D originalDatastore, MutableDatastoreCatalog mutableDatastoreCatalog,
            WindowContext windowContext, UserPreferences userPreferences) {
        super(originalDatastore, mutableDatastoreCatalog, windowContext, userPreferences);
        _statusLabel.setText("Please select file");
        _filenameField = new FilenameTextField(getUserPreferences().getOpenDatastoreDirectory(), true);

        if (originalDatastore != null) {
            if (originalDatastore instanceof ResourceDatastore) {
                final ResourceDatastore resourceDatastore = (ResourceDatastore) originalDatastore;
                final Resource resource = resourceDatastore.getResource();
                if (resource instanceof FileResource) {
                    final File file = ((FileResource) resource).getFile();
                    _filenameField.setFile(file);
                }
            } else if (originalDatastore instanceof FileDatastore) {
                final FileDatastore fileDatastore = (FileDatastore) originalDatastore;
                final String filename = fileDatastore.getFilename();
                _filenameField.setFilename(filename);
            }
        }

        // add listeners after setting initial values.
        setFileFilters(_filenameField);
        _filenameField.addSelectionListener(new FileSelectionListener() {
            @Override
            public void onSelected(FilenameTextField filenameTextField, File file) {
                final File dir;
                if (file.isDirectory()) {
                    dir = file;
                } else {
                    dir = file.getParentFile();
                }
                getUserPreferences().setOpenDatastoreDirectory(dir);

                if (StringUtils.isNullOrEmpty(_datastoreNameTextField.getText())) {
                    _datastoreNameTextField.setText(file.getName());
                }

                validateAndUpdate();

                onFileSelected(file);
            }
        });

        if (isDirectoryBased()) {
            _filenameField.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }

        if (isPreviewTableEnabled()) {
            _previewTable = new DCTable(new DefaultTableModel(7, 10));
            _previewTablePanel = _previewTable.toPanel(false);
            _loadingIcon = new LoadingIcon();
            _loadingIcon.setVisible(false);
            _loadingIcon.setPreferredSize(_previewTablePanel.getPreferredSize());
        } else {
            _previewTable = null;
            _previewTablePanel = null;
            _loadingIcon = null;
        }
    }

    /**
     * Can be overridden by subclasses in order to react to file selection
     * events.
     * 
     * @param file
     */
    protected void onFileSelected(File file) {
    }

    protected abstract D createDatastore(String name, String filename);

    protected abstract void setFileFilters(AbstractResourceTextField filenameField);

    @Override
    protected final void validateAndUpdate() {
        boolean valid = validateForm();
        setSaveButtonEnabled(valid);
        if (valid) {
            updatePreviewTable();
        }
    }

    protected boolean validateForm() {
        final String filename = _filenameField.getFilename();
        if (StringUtils.isNullOrEmpty(filename)) {
            setStatusError("Please enter or select a filename");
            return false;
        }

        final File file = new File(filename);
        if (!file.exists()) {
            setStatusError("The file does not exist!");
            return false;
        }

        if (isDirectoryBased()) {
            if (!file.isDirectory()) {
                setStatusError("Not a valid directory!");
                return false;
            }
        } else {
            if (!file.isFile()) {
                setStatusError("Not a valid file!");
                return false;
            }
        }

        final String datastoreName = _datastoreNameTextField.getText();
        if (StringUtils.isNullOrEmpty(datastoreName)) {
            setStatusError("Please enter a datastore name");
            return false;
        }

        setStatusValid();
        return true;
    }

    protected boolean isPreviewTableEnabled() {
        return false;
    }

    @Override
    protected int getDialogWidth() {
        if (isPreviewTableEnabled()) {
            return 650;
        }
        return super.getDialogWidth();
    }

    @Override
    protected final D createDatastore() {
        return createDatastore(getDatastoreName(), getFilename());
    }

    protected List<Entry<String, JComponent>> getFormElements() {
        List<Entry<String, JComponent>> res = super.getFormElements();
        if (isDirectoryBased()) {
            res.add(new ImmutableEntry<String, JComponent>("Directory", _filenameField));
        } else {
            res.add(new ImmutableEntry<String, JComponent>("Filename", _filenameField));
        }
        return res;
    }

    public String getDatastoreName() {
        return _datastoreNameTextField.getText();
    }

    public String getFilename() {
        return _filenameField.getFilename();
    }

    private void updatePreviewTable() {
        if (!isPreviewTableEnabled()) {
            return;
        }

        // show loading indicator
        setSaveButtonEnabled(false);
        _previewTable.setVisible(false);
        _loadingIcon.setVisible(true);

        // read file in background, it may take time if eg. it's located on a
        // network drive
        new SwingWorker<DataSet, Void>() {

            @Override
            protected DataSet doInBackground() throws Exception {
                return getPreviewData(getFilename());
            }

            @Override
            protected void done() {
                try {
                    DataSet dataSet = get();
                    if (dataSet != null) {
                        TableModel tableModel = new DataSetTableModel(dataSet);
                        _previewTable.setModel(tableModel);
                    }
                } catch (Throwable e) {
                    if (e instanceof ExecutionException) {
                        // get the cause of the execution exception (it's a
                        // wrapper around the throwable)
                        e = e.getCause();
                    }
                    if (logger.isWarnEnabled()) {
                        logger.warn("Error creating preview data: " + e.getMessage(), e);
                    }

                    setStatusError("Error create preview data: " + e.getMessage());
                }

                // show table
                _previewTable.setVisible(true);
                _loadingIcon.setVisible(false);
                setSaveButtonEnabled(true);
            }
        }.execute();
    }

    private final DataSet getPreviewData(String filename) {
        if (!isPreviewDataAvailable()) {
            logger.info("Not displaying preview table because isPreviewDataAvailable() returned false");
            return null;
        }

        final D datastore = getPreviewDatastore(filename);
        try (DatastoreConnection con = datastore.openConnection()) {
            final DataContext dc = con.getDataContext();
            final Table table = getPreviewTable(dc);

            Column[] columns = table.getColumns();
            if (columns.length > getPreviewColumns()) {
                // include max 10 columns
                columns = Arrays.copyOf(columns, getPreviewColumns());
            }
            final Query q = dc.query().from(table).select(columns).toQuery();
            q.setMaxRows(7);

            final DataSet dataSet = dc.executeQuery(q);

            return dataSet;
        }
    }

    @Override
    protected final JComponent getDialogContent() {
        DCPanel formPanel = new DCPanel();

        List<Entry<String, JComponent>> formElements = getFormElements();
        // temporary variable to make it easier to refactor the layout
        int row = 0;
        for (Entry<String, JComponent> entry : formElements) {
            String key = entry.getKey();
            if (StringUtils.isNullOrEmpty(key)) {
                WidgetUtils.addToGridBag(entry.getValue(), formPanel, 0, row, 2, 1);
            } else {
                WidgetUtils.addToGridBag(DCLabel.bright(key + ":"), formPanel, 0, row);
                WidgetUtils.addToGridBag(entry.getValue(), formPanel, 1, row);
            }
            row++;
        }

        if (isPreviewTableEnabled()) {
            WidgetUtils.addToGridBag(_loadingIcon, formPanel, 0, row, 2, 1);
            row++;
        }

        final DCPanel centerPanel = new DCPanel();
        centerPanel.setLayout(new GridBagLayout());
        WidgetUtils.addToGridBag(formPanel, centerPanel, 0, 0, 1, 1, GridBagConstraints.NORTH, 4, 0, 0);

        if (isPreviewTableEnabled()) {
            WidgetUtils.addToGridBag(_previewTablePanel, centerPanel, 0, 1, 1, 1, GridBagConstraints.NORTH, 4, 0.1,
                    1.0, GridBagConstraints.BOTH);
        }
        WidgetUtils.addToGridBag(getButtonPanel(), centerPanel, 0, 2, 1, 1, GridBagConstraints.SOUTH, 4, 0, 0.1);

        centerPanel.setBorder(WidgetUtils.BORDER_TOP_PADDING);

        JXStatusBar statusBar = WidgetFactory.createStatusBar(_statusLabel);

        _outerPanel.setLayout(new BorderLayout());
        _outerPanel.add(centerPanel, BorderLayout.CENTER);
        _outerPanel.add(statusBar, BorderLayout.SOUTH);

        final String descriptionText = getDescriptionText();
        if (descriptionText != null) {
            DescriptionLabel descriptionLabel = new DescriptionLabel();
            descriptionLabel.setText(descriptionText);
            _outerPanel.add(descriptionLabel, BorderLayout.NORTH);
        }

        validateAndUpdate();

        return _outerPanel;
    }

    protected boolean isPreviewDataAvailable() {
        return true;
    }

    protected Table getPreviewTable(DataContext dc) {
        return dc.getDefaultSchema().getTables()[0];
    }

    protected int getPreviewColumns() {
        return PREVIEW_COLUMNS;
    }

    protected D getPreviewDatastore(String filename) {
        D datastore = createDatastore("Preview", filename);
        return datastore;
    }

    protected byte[] getSampleBuffer() {
        final File file = new File(getFilename());
        byte[] bytes = new byte[SAMPLE_BUFFER_SIZE];
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            int bufferSize = fileInputStream.read(bytes, 0, SAMPLE_BUFFER_SIZE);
            if (bufferSize != -1 && bufferSize != SAMPLE_BUFFER_SIZE) {
                bytes = Arrays.copyOf(bytes, bufferSize);
            }
            return bytes;
        } catch (IOException e) {
            logger.error("IOException occurred while reading sample buffer", e);
            return new byte[0];
        } finally {
            FileHelper.safeClose(fileInputStream);
        }
    }

    protected char[] readSampleBuffer(byte[] bytes, final String charSet) {
        char[] buffer = new char[bytes.length];
        Reader reader = null;
        try {
            reader = new InputStreamReader(new ByteArrayInputStream(bytes), charSet);

            // read a sample of the file to auto-detect quotes and separators
            int bufferSize = reader.read(buffer);
            if (bufferSize != -1) {
                buffer = Arrays.copyOf(buffer, bufferSize);
            }
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Error reading from file: " + e.getMessage(), e);
            }
            setStatusError("Error reading from file: " + e.getMessage());
            return new char[0];
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    logger.debug("Could not close reader", ioe);
                }
            }
        }
        return buffer;
    }

    protected DCTable getPreviewTable() {
        return _previewTable;
    }

    protected boolean isDirectoryBased() {
        return false;
    }
}
