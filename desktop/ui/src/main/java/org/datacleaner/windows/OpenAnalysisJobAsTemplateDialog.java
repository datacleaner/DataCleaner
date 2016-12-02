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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Provider;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import org.apache.commons.vfs2.FileObject;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.actions.OpenAnalysisJobActionListener;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.SourceColumnMapping;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.job.AnalysisJobMetadata;
import org.datacleaner.job.JaxbJobReader;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.LoadingIcon;
import org.datacleaner.widgets.SourceColumnComboBox;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * Dialog for opening a job as a template. This feature allows the user to reuse an existing job but on a new set 
 * of columns, typically from a different datastore.
 */
public class OpenAnalysisJobAsTemplateDialog extends AbstractDialog {
    private class DialogContentMaker {
        private static final int MAX_HEIGHT = 800;
        private final DCPanel _panel;
        private int _row;

        public DialogContentMaker() {
            _panel = new DCPanel();
            _row = 0;
        }

        public JScrollPane make() {
            addTopLabels();
            addDatastoreButtonPanel();

            for (final String tableName : _sourceColumnComboBoxes.keySet()) {
                addTable(tableName);
            }

            _row++;

            if (!_variableTextFields.isEmpty()) {
                addJobLevelVariables();
            }

            addOpenButtonPanel();

            return WidgetUtils.scrollable(_panel, MAX_HEIGHT);
        }

        private void addTopLabels() {
            WidgetUtils.addToGridBag(DCLabel.bright("<html><b>Original value:</b></html>"), _panel, 1, _row);
            WidgetUtils.addToGridBag(DCLabel.bright("<html><b>New/mapped value:</b></html>"), _panel, 2, _row);

            _row++;
            WidgetUtils
                    .addToGridBag(new JLabel(imageManager.getImageIcon(IconUtils.GENERIC_DATASTORE_IMAGEPATH)), _panel,
                            0, _row);
            WidgetUtils.addToGridBag(DCLabel.bright(_metadata.getDatastoreName()), _panel, 1, _row,
                    GridBagConstraints.WEST);
        }

        private void addDatastoreButtonPanel() {
            final DCPanel datastoreButtonPanel = new DCPanel();
            datastoreButtonPanel.setLayout(new HorizontalLayout(0));
            datastoreButtonPanel.add(_datastoreCombobox);
            datastoreButtonPanel.add(_loadingIcon);
            datastoreButtonPanel.add(Box.createHorizontalStrut(4));
            datastoreButtonPanel.add(_autoMapButton);

            WidgetUtils.addToGridBag(datastoreButtonPanel, _panel, 2, _row, GridBagConstraints.WEST);
        }

        private void addTable(final String tableName) {
            addTableLabel(tableName);
            addTableClearButton(tableName);

            for (final SourceColumnComboBox comboBox : _sourceColumnComboBoxes.get(tableName)) {
                addTableSourceColumnComboBox(comboBox);
            }
        }

        private void addTableSourceColumnComboBox(final SourceColumnComboBox comboBox) {
            _row++;
            WidgetUtils.addToGridBag(
                    new JLabel(imageManager.getImageIcon(IconUtils.MODEL_COLUMN, IconUtils.ICON_SIZE_SMALL)), _panel, 0,
                    _row);
            WidgetUtils.addToGridBag(DCLabel.bright(comboBox.getName()), _panel, 1, _row, GridBagConstraints.WEST);
            WidgetUtils.addToGridBag(comboBox, _panel, 2, _row, GridBagConstraints.WEST);
        }

        private void addTableClearButton(final String tableName) {
            _clearButton.addActionListener(e -> {
                for (final SourceColumnComboBox comboBox : _sourceColumnComboBoxes.get(tableName)) {
                    comboBox.setModel(_datastore, false);
                }
            });

            final DCPanel clearButtonPanel = new DCPanel();
            clearButtonPanel.add(_clearButton);
            WidgetUtils.addToGridBag(clearButtonPanel, _panel, 2, _row, GridBagConstraints.CENTER);
        }

        private void addTableLabel(final String tableName) {
            _row++;
            final JLabel tableLabel = DCLabel.bright("<html><b>" + tableName + "</b></html>");
            tableLabel.setIcon(imageManager.getImageIcon(IconUtils.MODEL_TABLE, IconUtils.ICON_SIZE_SMALL));
            WidgetUtils.addToGridBag(tableLabel, _panel, 0, _row, 2, 1, GridBagConstraints.WEST);
        }

        private void addJobLevelVariables() {
            final JLabel tableLabel = DCLabel.bright("<html><b>Job-level variables</b></html>");
            tableLabel.setIcon(imageManager.getImageIcon(IconUtils.MODEL_JOB, IconUtils.ICON_SIZE_SMALL));
            WidgetUtils.addToGridBag(tableLabel, _panel, 0, _row, 2, 1, GridBagConstraints.WEST);

            for (final Entry<String, JXTextField> entry : _variableTextFields.entrySet()) {
                _row++;
                final String variableId = entry.getKey();
                final JXTextField textField = entry.getValue();

                WidgetUtils.addToGridBag(
                        new JLabel(imageManager.getImageIcon("images/model/variable.png", IconUtils.ICON_SIZE_SMALL)),
                        _panel, 0, _row);
                WidgetUtils.addToGridBag(DCLabel.bright(variableId), _panel, 1, _row, GridBagConstraints.WEST);
                WidgetUtils.addToGridBag(textField, _panel, 2, _row, GridBagConstraints.WEST);
            }

            _row++;
        }

        private void addOpenButtonPanel() {
            final DCPanel openButtonPanel = new DCPanel();
            openButtonPanel.add(_openButton);
            WidgetUtils.addToGridBag(openButtonPanel, _panel, 2, _row, GridBagConstraints.EAST);
        }
    }

    private class ComboBoxUpdater extends SwingWorker<Void, Void> {
        public ComboBoxUpdater(final JDialog parent) {
            final String datastoreName = (String) _datastoreCombobox.getSelectedItem();
            _datastore = _datastoreCatalog.getDatastore(datastoreName);
        }

        private void update() {
            _sourceColumnMapping.setDatastore(_datastore);

            for (final List<SourceColumnComboBox> comboBoxes : _sourceColumnComboBoxes.values()) {
                for (final SourceColumnComboBox comboBox : comboBoxes) {
                    comboBox.setModel(_datastore);
                    final boolean datastoreSelected = (_datastore != null);
                    comboBox.setEnabled(datastoreSelected);
                }
            }
        }

        private void disableGUI() {
            refreshOpenButtonVisibility();
            _clearButton.setEnabled(false);
            _datastoreCombobox.setEnabled(false);
        }

        private void enableGUI() {
            final boolean datastoreSelected = (_datastore != null);
            _autoMapButton.setVisible(datastoreSelected);
            _clearButton.setEnabled(true);
            _datastoreCombobox.setEnabled(true);
        }

        protected Void doInBackground() throws Exception {
            disableGUI();
            _loadingIcon.setVisible(true);
            update();

            return null;
        }

        protected void done() {
            enableGUI();
            _loadingIcon.setVisible(false);
        }
    }

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(OpenAnalysisJobAsTemplateDialog.class);
    private static final ImageManager imageManager = ImageManager.get();
    private final DataCleanerConfiguration _configuration;
    private final FileObject _file;
    private final AnalysisJobMetadata _metadata;
    private final SourceColumnMapping _sourceColumnMapping;
    private final DatastoreCatalog _datastoreCatalog;
    private final JComboBox<String> _datastoreCombobox;
    private final Map<String, List<SourceColumnComboBox>> _sourceColumnComboBoxes;
    private final Map<String, JXTextField> _variableTextFields;
    private final JButton _openButton;
    private final JButton _clearButton;
    private final JButton _autoMapButton;
    private final Provider<OpenAnalysisJobActionListener> _openAnalysisJobActionListenerProvider;
    private final LoadingIcon _loadingIcon = createLoadingIcon();
    private volatile Datastore _datastore;

    public OpenAnalysisJobAsTemplateDialog(final WindowContext windowContext,
            final DataCleanerConfiguration configuration, final FileObject file, final AnalysisJobMetadata metadata,
            final Provider<OpenAnalysisJobActionListener> openAnalysisJobActionListenerProvider) {
        super(windowContext, imageManager.getImage("images/window/banner-logo.png"));
        _configuration = configuration;
        _file = file;
        _metadata = metadata;
        _openAnalysisJobActionListenerProvider = openAnalysisJobActionListenerProvider;
        _sourceColumnMapping = new SourceColumnMapping(metadata);
        _clearButton = WidgetFactory.createDefaultButton("Clear");
        _openButton = createOpenButton();
        _sourceColumnComboBoxes = createSourceColumnComboBoxes();
        _variableTextFields = createVariableTextFields();
        _openButton.setEnabled(false);
        _datastoreCatalog = configuration.getDatastoreCatalog();
        _datastoreCombobox = createDatastoreCombobox();
        _autoMapButton = createAutoMapButton();
    }

    public static LoadingIcon createLoadingIcon() {
        final LoadingIcon loadingIcon = new LoadingIcon();
        final int formElementHeight = 32;
        final Dimension size = new Dimension(formElementHeight, formElementHeight);
        loadingIcon.setPreferredSize(size);
        loadingIcon.setBackground(Color.WHITE);
        loadingIcon.setOpaque(true);
        loadingIcon.setVisible(false);

        return loadingIcon;
    }

    private Map<String, JXTextField> createVariableTextFields() {
        final Map<String, JXTextField> variableTextFields = new HashMap<>();

        for (final Entry<String, String> variableEntry : _metadata.getVariables().entrySet()) {
            final String id = variableEntry.getKey();
            final String value = variableEntry.getValue();
            final JXTextField textField = WidgetFactory.createTextField("Original: " + value);
            textField.setText(value);
            variableTextFields.put(id, textField);
        }

        return variableTextFields;
    }

    private JButton createOpenButton() {
        final JButton openButton = WidgetFactory.createPrimaryButton("Open job", IconUtils.MODEL_JOB);
        openButton.addActionListener(event -> {
            final JaxbJobReader reader = new JaxbJobReader(_configuration);

            try {
                final SourceColumnMapping sourceColumnMapping = getSourceColumnMapping();
                final Map<String, String> variableOverrides = new HashMap<>();

                for (final Entry<String, JXTextField> entry : _variableTextFields.entrySet()) {
                    variableOverrides.put(entry.getKey(), entry.getValue().getText());
                }

                final InputStream inputStream = _file.getContent().getInputStream();
                final AnalysisJobBuilder analysisJobBuilder;

                try {
                    analysisJobBuilder = reader.create(inputStream, sourceColumnMapping, variableOverrides);
                } finally {
                    FileHelper.safeClose(inputStream);
                }

                final OpenAnalysisJobActionListener openAnalysisJobActionListener =
                        _openAnalysisJobActionListenerProvider.get();
                final Injector injector = openAnalysisJobActionListener.openAnalysisJob(_file, analysisJobBuilder);
                OpenAnalysisJobAsTemplateDialog.this.dispose();
                final AnalysisJobBuilderWindow window = injector.getInstance(AnalysisJobBuilderWindow.class);
                window.open();
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        });

        return openButton;
    }

    private Map<String, List<SourceColumnComboBox>> createSourceColumnComboBoxes() {
        final Map<String, List<SourceColumnComboBox>> sourceColumnComboBoxes = new HashMap<>();
        final List<String> columnPaths = _metadata.getSourceColumnPaths();

        for (final String columnPath : columnPaths) {
            final String tablePath = getTablePath(columnPath);
            final SourceColumnComboBox comboBox =
                    createSourceColumnComboBoxForColumn(sourceColumnComboBoxes, tablePath, columnPath);
            sourceColumnComboBoxes.get(tablePath).add(comboBox);
        }

        return sourceColumnComboBoxes;
    }

    private String getTablePath(final String columnPath) {
        final int columnDelim = columnPath.lastIndexOf('.');
        final String tablePath;

        if (columnDelim == -1) { // some column paths contain only the column name
            tablePath = _metadata.getDatastoreName();
        } else {
            // this tablePath will be used to group together columns from the same original table
            // The column's path contains also the table name in the path
            tablePath = columnPath.substring(0, columnDelim);
        }

        return tablePath;
    }

    private SourceColumnComboBox createSourceColumnComboBoxForColumn(
            final Map<String, List<SourceColumnComboBox>> sourceColumnComboBoxes, final String tablePath,
            final String columnPath) {
        final SourceColumnComboBox comboBox = new SourceColumnComboBox();
        comboBox.setEnabled(false);
        comboBox.setName(columnPath);
        comboBox.addColumnSelectedListener(col -> {
            if (col != null) { // make sure all comboboxes in a group use the same table
                final List<SourceColumnComboBox> comboBoxes = sourceColumnComboBoxes.get(tablePath);

                for (final SourceColumnComboBox sameTableComboBox : comboBoxes) {
                    sameTableComboBox.setModel(_datastore, col.getTable());
                }
            }

            refreshOpenButtonVisibility();
        });

        if (!sourceColumnComboBoxes.containsKey(tablePath)) {
            sourceColumnComboBoxes.put(tablePath, new ArrayList<>());
        }

        return comboBox;
    }

    private JComboBox<String> createDatastoreCombobox() {
        final String[] comboBoxModel = CollectionUtils.array(new String[1], _datastoreCatalog.getDatastoreNames());
        final JComboBox<String> comboBox = new JComboBox<>(comboBoxModel);
        comboBox.setEditable(false);
        final JDialog parent = this;
        comboBox.addActionListener(e -> {
            try {
                final ComboBoxUpdater comboBoxUpdater = new ComboBoxUpdater(parent);
                comboBoxUpdater.execute();
            } catch (final Exception exception) {
                final String exceptionMessage =
                        "An unexpected error occurred while updating combo boxes:\n" + exception.getMessage();
                logger.error(exceptionMessage);
                WidgetUtils.showErrorMessage("Unexpected error", exceptionMessage);
            }
        });

        return comboBox;
    }

    private JButton createAutoMapButton() {
        final JButton button = WidgetFactory.createDefaultButton("Map automatically");
        button.setVisible(false);
        button.addActionListener(e -> {
            _sourceColumnMapping.autoMap(_datastore);

            for (final String path : _sourceColumnMapping.getPaths()) {
                for (final List<SourceColumnComboBox> comboBoxes : _sourceColumnComboBoxes.values()) {
                    for (final SourceColumnComboBox comboBox : comboBoxes) {
                        if (path.equals(comboBox.getName())) {
                            comboBox.setSelectedItem(_sourceColumnMapping.getColumn(path));
                        }
                    }
                }
            }
        });

        return button;
    }

    public void refreshOpenButtonVisibility() {
        if (_datastore == null) { // no datastore selected
            _openButton.setEnabled(false);
            return;
        }

        for (final List<SourceColumnComboBox> comboBoxes : _sourceColumnComboBoxes.values()) {
            for (final SourceColumnComboBox comboBox : comboBoxes) {
                if (comboBox.getSelectedItem() == null) { // not all columns selected
                    _openButton.setEnabled(false);
                    return;
                }
            }
        }

        _openButton.setEnabled(true);
    }

    public SourceColumnMapping getSourceColumnMapping() {
        for (final List<SourceColumnComboBox> comboBoxes : _sourceColumnComboBoxes.values()) {
            for (final SourceColumnComboBox comboBox : comboBoxes) {
                _sourceColumnMapping.setColumn(comboBox.getName(), comboBox.getSelectedItem());
            }
        }

        return _sourceColumnMapping;
    }

    @Override
    protected String getBannerTitle() {
        return "Open as template";
    }

    @Override
    protected int getDialogWidth() {
        return 600;
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    public String getWindowTitle() {
        return "Open analysis job as template";
    }

    @Override
    protected JComponent getDialogContent() {
        return new DialogContentMaker().make();
    }
}
