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
import java.util.Arrays;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.CollectionUtils;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.SchemaNavigator;
import org.datacleaner.guice.Nullable;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.reference.DatastoreSynonymCatalog;
import org.datacleaner.user.MutableReferenceDataCatalog;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DescriptionLabel;
import org.datacleaner.widgets.SourceColumnComboBox;
import org.jdesktop.swingx.JXTextField;

public final class DatastoreSynonymCatalogDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final DatastoreSynonymCatalog _originalsynonymCatalog;
    private final MutableReferenceDataCatalog _mutableReferenceCatalog;
    private final JComboBox<String> _datastoreComboBox;
    private final SourceColumnComboBox _masterTermColumnComboBox;
    private final JXTextField _nameTextField;
    private final DatastoreCatalog _datastoreCatalog;
    private final MultiSourceColumnComboBoxPanel _synonymColumnsPanel;
    private Datastore _datastore;

    @Inject
    protected DatastoreSynonymCatalogDialog(@Nullable final DatastoreSynonymCatalog synonymCatalog,
            final MutableReferenceDataCatalog mutableReferenceCatalog, final DatastoreCatalog datastoreCatalog,
            final WindowContext windowContext) {
        super(windowContext, ImageManager.get().getImage(IconUtils.SYNONYM_CATALOG_DATASTORE_IMAGEPATH));
        _originalsynonymCatalog = synonymCatalog;
        _datastoreCatalog = datastoreCatalog;
        _mutableReferenceCatalog = mutableReferenceCatalog;
        _nameTextField = WidgetFactory.createTextField("Synonym catalog name");
        final String[] comboBoxModel = CollectionUtils.array(new String[1], _datastoreCatalog.getDatastoreNames());

        _datastoreComboBox = new JComboBox<>(comboBoxModel);
        _masterTermColumnComboBox = new SourceColumnComboBox();
        _synonymColumnsPanel = new MultiSourceColumnComboBoxPanel();
        _datastoreComboBox.setEditable(false);

        _datastoreComboBox.addActionListener(e -> {
            final String datastoreName = (String) _datastoreComboBox.getSelectedItem();
            if (datastoreName != null) {
                _datastore = _datastoreCatalog.getDatastore(datastoreName);
                _masterTermColumnComboBox.setModel(_datastore);
                _masterTermColumnComboBox.addColumnSelectedListener(column -> {
                    final Table table;
                    if (column == null) {
                        table = null;
                    } else {
                        table = column.getTable();
                    }
                    _synonymColumnsPanel.updateSourceComboBoxes(_datastore, table);
                    _synonymColumnsPanel.updateUI();
                });

                _synonymColumnsPanel.setModel(_datastore);
            }
        });

        if (synonymCatalog != null) {
            final String datastoreName = synonymCatalog.getDatastoreName();

            _nameTextField.setText(synonymCatalog.getName());
            _datastoreComboBox.setSelectedItem(datastoreName);

            final Datastore datastore = _datastoreCatalog.getDatastore(datastoreName);
            if (datastore != null) {
                try (DatastoreConnection datastoreConnection = datastore.openConnection()) {
                    final SchemaNavigator sn = datastoreConnection.getSchemaNavigator();

                    final Column masterTermColumn = sn.convertToColumn(synonymCatalog.getMasterTermColumnPath());
                    _masterTermColumnComboBox.setSelectedItem(masterTermColumn);

                    final String[] synonymColumnPaths = synonymCatalog.getSynonymColumnPaths();
                    final Column[] synonymColumns = sn.convertToColumns(synonymColumnPaths);
                    _synonymColumnsPanel.setColumns(Arrays.asList(synonymColumns));
                }
            }

        }
    }

    @Override
    protected String getBannerTitle() {
        return "Datastore\nsynonym catalog";
    }

    @Override
    protected int getDialogWidth() {
        return 565;
    }

    @Override
    protected JComponent getDialogContent() {

        final DCPanel formPanel = new DCPanel();

        int row = 0;
        WidgetUtils.addToGridBag(DCLabel.bright("Synonym catalog name:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_nameTextField, formPanel, 1, row);
        row++;
        WidgetUtils.addToGridBag(DCLabel.bright("Datastore:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_datastoreComboBox, formPanel, 1, row);
        row++;
        WidgetUtils.addToGridBag(DCLabel.bright("Master term column:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_masterTermColumnComboBox, formPanel, 1, row);
        row++;
        WidgetUtils.addToGridBag(DCLabel.bright("Synonym columns:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_synonymColumnsPanel.createPanel(), formPanel, 1, row);
        row++;
        final JButton saveButton =
                WidgetFactory.createPrimaryButton("Save Synonym Catalog", IconUtils.ACTION_SAVE_BRIGHT);
        saveButton.addActionListener(e -> {
            final String name1 = _nameTextField.getText();
            if (StringUtils.isNullOrEmpty(name1)) {
                JOptionPane.showMessageDialog(DatastoreSynonymCatalogDialog.this,
                        "Please fill out the name of the synonym catalog");
                return;
            }

            final String nameOfDatastore = (String) _datastoreComboBox.getSelectedItem();
            if (StringUtils.isNullOrEmpty(nameOfDatastore)) {
                JOptionPane.showMessageDialog(DatastoreSynonymCatalogDialog.this, "Please select a character encoding");
                return;
            }

            final Column selectedItem = _masterTermColumnComboBox.getSelectedItem();
            final String[] synonymColumnNames = _synonymColumnsPanel.getColumnNames();

            final DatastoreSynonymCatalog dataStoreBasedSynonymCatalog =
                    new DatastoreSynonymCatalog(name1, nameOfDatastore, selectedItem.getQualifiedLabel(),
                            synonymColumnNames);

            if (_originalsynonymCatalog != null) {
                _mutableReferenceCatalog.changeSynonymCatalog(_originalsynonymCatalog, dataStoreBasedSynonymCatalog);
            } else {
                _mutableReferenceCatalog.addSynonymCatalog(dataStoreBasedSynonymCatalog);
            }

            _mutableReferenceCatalog.addSynonymCatalog(dataStoreBasedSynonymCatalog);
            DatastoreSynonymCatalogDialog.this.dispose();
        });

        final DCPanel buttonPanel = DCPanel.flow(Alignment.CENTER, saveButton);

        final DescriptionLabel descriptionLabel = new DescriptionLabel("A datastore synonym catalog is based on a "
                + "datastore and columns within it. The layout of the datastore is flexible: There should be a master "
                + "term column and either a single or multiple synonym columns.");

        final DCPanel mainPanel = new DCPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(descriptionLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.setPreferredSize(getDialogWidth(), 350);

        return mainPanel;
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    public String getWindowTitle() {
        return "Datastore synonym catalog";
    }

}
