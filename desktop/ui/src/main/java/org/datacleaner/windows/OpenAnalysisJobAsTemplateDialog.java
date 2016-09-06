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

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Provider;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.apache.commons.vfs2.FileObject;
import org.apache.metamodel.schema.Column;
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
import org.datacleaner.widgets.DCComboBox.Listener;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.SourceColumnComboBox;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXTextField;

import com.google.inject.Injector;

/**
 * Dialog for opening a job as a template. This feature allows the user to reuse
 * an existing job but on a new set of columns, typically from a different
 * datastore.
 */
public class OpenAnalysisJobAsTemplateDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

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
    private final JButton _autoMapButton;
    private final Provider<OpenAnalysisJobActionListener> _openAnalysisJobActionListenerProvider;

    private volatile Datastore _datastore;

    public OpenAnalysisJobAsTemplateDialog(WindowContext windowContext, DataCleanerConfiguration configuration,
            FileObject file, AnalysisJobMetadata metadata,
            Provider<OpenAnalysisJobActionListener> openAnalysisJobActionListenerProvider) {
        super(windowContext, imageManager.getImage("images/window/banner-logo.png"));
        _configuration = configuration;
        _file = file;
        _metadata = metadata;
        _openAnalysisJobActionListenerProvider = openAnalysisJobActionListenerProvider;
        _sourceColumnMapping = new SourceColumnMapping(metadata);
        _variableTextFields = new HashMap<String, JXTextField>();

        _openButton = WidgetFactory.createPrimaryButton("Open job", IconUtils.MODEL_JOB);
        _openButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JaxbJobReader reader = new JaxbJobReader(_configuration);
                try {
                    SourceColumnMapping sourceColumnMapping = getSourceColumnMapping();

                    Map<String, String> variableOverrides = new HashMap<String, String>();
                    for (Entry<String, JXTextField> entry : _variableTextFields.entrySet()) {
                        variableOverrides.put(entry.getKey(), entry.getValue().getText());
                    }

                    final InputStream inputStream = _file.getContent().getInputStream();
                    final AnalysisJobBuilder ajb;
                    try {
                        ajb = reader.create(inputStream, sourceColumnMapping, variableOverrides);
                    } finally {
                        FileHelper.safeClose(inputStream);
                    }

                    final OpenAnalysisJobActionListener openAnalysisJobActionListener = _openAnalysisJobActionListenerProvider
                            .get();
                    final Injector injector = openAnalysisJobActionListener.openAnalysisJob(_file, ajb);

                    OpenAnalysisJobAsTemplateDialog.this.dispose();

                    final AnalysisJobBuilderWindow window = injector.getInstance(AnalysisJobBuilderWindow.class);
                    window.open();
                } catch (Exception e1) {
                    throw new IllegalStateException(e1);
                }
            }
        });

        final List<String> columnPaths = _metadata.getSourceColumnPaths();
        _sourceColumnComboBoxes = new HashMap<String, List<SourceColumnComboBox>>();
        for (String columnPath : columnPaths) {
            int columnDelim = columnPath.lastIndexOf('.');
            final String tablePath;
            if (columnDelim == -1) {
                // some column path contain only the column name
                tablePath = _metadata.getDatastoreName();
            } else {
                // this tablePath will be used to group together columns from
                // the same original table
                // The column's path contains also the table name in the path
                tablePath = columnPath.substring(0, columnDelim);
            }

            final SourceColumnComboBox comboBox = new SourceColumnComboBox();
            comboBox.setEnabled(false);
            comboBox.setName(columnPath);
            comboBox.addColumnSelectedListener(new Listener<Column>() {

                @Override
                public void onItemSelected(Column col) {
                    if (col != null) {
                        // make sure all comboboxes in a group use the same
                        // table
                        List<SourceColumnComboBox> comboBoxes = _sourceColumnComboBoxes.get(tablePath);
                        for (SourceColumnComboBox sameTableComboBox : comboBoxes) {
                            sameTableComboBox.setModel(_datastore, col.getTable());
                        }
                    }
                    refreshOpenButtonVisibility();
                }
            });

            if (!_sourceColumnComboBoxes.containsKey(tablePath)) {
                _sourceColumnComboBoxes.put(tablePath, new ArrayList<SourceColumnComboBox>());
            }

            _sourceColumnComboBoxes.get(tablePath).add(comboBox);
        }

        for (Entry<String, String> variableEntry : metadata.getVariables().entrySet()) {
            String id = variableEntry.getKey();
            String value = variableEntry.getValue();
            JXTextField textField = WidgetFactory.createTextField("Original: " + value);
            textField.setText(value);
            _variableTextFields.put(id, textField);
        }

        _openButton.setEnabled(false);
        _datastoreCatalog = configuration.getDatastoreCatalog();

        final String[] datastoreNames = _datastoreCatalog.getDatastoreNames();
        // the combobox will contain all datastore names and a null for
        // "not selected"
        final String[] comboBoxModel = CollectionUtils.array(new String[1], datastoreNames);
        _datastoreCombobox = new JComboBox<String>(comboBoxModel);
        _datastoreCombobox.setEditable(false);
        _datastoreCombobox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String datastoreName = (String) _datastoreCombobox.getSelectedItem();
                _datastore = _datastoreCatalog.getDatastore(datastoreName);

                _sourceColumnMapping.setDatastore(_datastore);

                refreshOpenButtonVisibility();

                for (List<SourceColumnComboBox> comboBoxes : _sourceColumnComboBoxes.values()) {
                    for (SourceColumnComboBox comboBox : comboBoxes) {
                        comboBox.setModel(_datastore);
                        if (_datastore == null) {
                            // no datastore selected
                            comboBox.setEnabled(false);
                        } else {
                            comboBox.setEnabled(true);
                        }
                    }
                }

                if (_datastore == null) {
                    _autoMapButton.setVisible(false);
                } else {
                    _autoMapButton.setVisible(true);
                }
            }
        });

        _autoMapButton = WidgetFactory.createDefaultButton("Map automatically");
        _autoMapButton.setVisible(false);
        _autoMapButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _sourceColumnMapping.autoMap(_datastore);
                Set<String> paths = _sourceColumnMapping.getPaths();
                for (String path : paths) {
                    for (List<SourceColumnComboBox> comboBoxes : _sourceColumnComboBoxes.values()) {
                        for (SourceColumnComboBox comboBox : comboBoxes) {
                            if (path.equals(comboBox.getName())) {
                                comboBox.setSelectedItem(_sourceColumnMapping.getColumn(path));
                            }
                        }
                    }
                }
            }
        });
    }

    public void refreshOpenButtonVisibility() {
        if (_datastore == null) {
            // no datastore selected
            _openButton.setEnabled(false);
            return;
        }

        for (List<SourceColumnComboBox> comboBoxes : _sourceColumnComboBoxes.values()) {
            for (SourceColumnComboBox comboBox : comboBoxes) {
                if (comboBox.getSelectedItem() == null) {
                    // not all columns selected
                    _openButton.setEnabled(false);
                    return;
                }
            }
        }

        _openButton.setEnabled(true);
    }

    public SourceColumnMapping getSourceColumnMapping() {
        for (List<SourceColumnComboBox> comboBoxes : _sourceColumnComboBoxes.values()) {
            for (SourceColumnComboBox comboBox : comboBoxes) {
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
    protected JComponent getDialogContent() {
        final DCPanel panel = new DCPanel();

        int row = 0;
        WidgetUtils.addToGridBag(DCLabel.bright("<html><b>Original value:</b></html>"), panel, 1, row);
        WidgetUtils.addToGridBag(DCLabel.bright("<html><b>New/mapped value:</b></html>"), panel, 2, row);

        row++;
        WidgetUtils.addToGridBag(new JLabel(imageManager.getImageIcon(IconUtils.GENERIC_DATASTORE_IMAGEPATH)), panel,
                0, row);
        WidgetUtils.addToGridBag(DCLabel.bright(_metadata.getDatastoreName()), panel, 1, row, GridBagConstraints.WEST);

        DCPanel datastoreButtonPanel = new DCPanel();
        datastoreButtonPanel.setLayout(new HorizontalLayout(0));
        datastoreButtonPanel.add(_datastoreCombobox);
        datastoreButtonPanel.add(Box.createHorizontalStrut(4));
        datastoreButtonPanel.add(_autoMapButton);

        WidgetUtils.addToGridBag(datastoreButtonPanel, panel, 2, row, GridBagConstraints.WEST);

        Set<String> tableNames = _sourceColumnComboBoxes.keySet();
        for (final String tableName : tableNames) {
            row++;
            final JLabel tableLabel = DCLabel.bright("<html><b>" + tableName + "</b></html>");
            tableLabel.setIcon(imageManager.getImageIcon(IconUtils.MODEL_TABLE, IconUtils.ICON_SIZE_SMALL));
            WidgetUtils.addToGridBag(tableLabel, panel, 0, row, 2, 1, GridBagConstraints.WEST);

            final JButton clearButton = WidgetFactory.createDefaultButton("Clear");
            clearButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    List<SourceColumnComboBox> comboBoxes = _sourceColumnComboBoxes.get(tableName);
                    for (SourceColumnComboBox comboBox : comboBoxes) {
                        comboBox.setModel(_datastore, false);
                    }
                }
            });
            final DCPanel clearButtonPanel = new DCPanel();
            clearButtonPanel.add(clearButton);
            WidgetUtils.addToGridBag(clearButtonPanel, panel, 2, row, GridBagConstraints.CENTER);

            final List<SourceColumnComboBox> comboBoxes = _sourceColumnComboBoxes.get(tableName);
            for (SourceColumnComboBox comboBox : comboBoxes) {
                row++;
                WidgetUtils.addToGridBag(
                        new JLabel(imageManager.getImageIcon(IconUtils.MODEL_COLUMN, IconUtils.ICON_SIZE_SMALL)),
                        panel, 0, row);
                WidgetUtils.addToGridBag(DCLabel.bright(comboBox.getName()), panel, 1, row, GridBagConstraints.WEST);
                WidgetUtils.addToGridBag(comboBox, panel, 2, row, GridBagConstraints.WEST);
            }
        }
        row++;

        if (!_variableTextFields.isEmpty()) {
            final JLabel tableLabel = DCLabel.bright("<html><b>Job-level variables</b></html>");
            tableLabel.setIcon(imageManager.getImageIcon(IconUtils.MODEL_JOB, IconUtils.ICON_SIZE_SMALL));
            WidgetUtils.addToGridBag(tableLabel, panel, 0, row, 2, 1, GridBagConstraints.WEST);

            for (Entry<String, JXTextField> entry : _variableTextFields.entrySet()) {
                row++;
                String variableId = entry.getKey();
                JXTextField textField = entry.getValue();

                WidgetUtils.addToGridBag(
                        new JLabel(imageManager.getImageIcon("images/model/variable.png", IconUtils.ICON_SIZE_SMALL)),
                        panel, 0, row);
                WidgetUtils.addToGridBag(DCLabel.bright(variableId), panel, 1, row, GridBagConstraints.WEST);
                WidgetUtils.addToGridBag(textField, panel, 2, row, GridBagConstraints.WEST);
            }
            row++;
        }

        final DCPanel openButtonPanel = new DCPanel();
        openButtonPanel.add(_openButton);
        WidgetUtils.addToGridBag(openButtonPanel, panel, 2, row, GridBagConstraints.EAST);

        return WidgetUtils.scrolleable(panel);
    }

    @Override
    public String getWindowTitle() {
        return "Open analysis job as template";
    }

}
