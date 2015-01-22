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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.CollectionUtils;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.SchemaNavigator;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.guice.Nullable;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.reference.DatastoreSynonymCatalog;
import org.datacleaner.user.MutableReferenceDataCatalog;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DCComboBox.Listener;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DescriptionLabel;
import org.datacleaner.widgets.SourceColumnComboBox;
import org.datacleaner.widgets.tree.SchemaTree;
import org.jdesktop.swingx.JXTextField;

import com.google.inject.Injector;

public final class DatastoreSynonymCatalogDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final DatastoreSynonymCatalog _originalsynonymCatalog;
    private final MutableReferenceDataCatalog _mutableReferenceCatalog;
    private final JComboBox<String> _datastoreComboBox;
    private final SourceColumnComboBox _masterTermColumnComboBox;
    private final JXTextField _nameTextField;
    private final DatastoreCatalog _datastoreCatalog;
    private final MultiSourceColumnComboBoxPanel _synonymColumnsPanel;
    private final InjectorBuilder _injectorBuilder;
    private Datastore _datastore;
    private final DCPanel _treePanel;
    private volatile boolean _nameAutomaticallySet = true;

    @Inject
    protected DatastoreSynonymCatalogDialog(@Nullable DatastoreSynonymCatalog synonymCatalog,
            MutableReferenceDataCatalog mutableReferenceCatalog, DatastoreCatalog datastoreCatalog,
            WindowContext windowContext, InjectorBuilder injectorBuilder) {
        super(windowContext, ImageManager.get().getImage(IconUtils.SYNONYM_CATALOG_DATASTORE_IMAGEPATH));
        _originalsynonymCatalog = synonymCatalog;
        _datastoreCatalog = datastoreCatalog;
        _mutableReferenceCatalog = mutableReferenceCatalog;
        _injectorBuilder = injectorBuilder;
        _nameTextField = WidgetFactory.createTextField("Synonym catalog name");
        String[] comboBoxModel = CollectionUtils.array(new String[1], _datastoreCatalog.getDatastoreNames());

        _datastoreComboBox = new JComboBox<String>(comboBoxModel);
        _masterTermColumnComboBox = new SourceColumnComboBox();
        _synonymColumnsPanel = new MultiSourceColumnComboBoxPanel();
        _datastoreComboBox.setEditable(false);
        _treePanel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _treePanel.setLayout(new BorderLayout());

        _datastoreComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String datastoreName = (String) _datastoreComboBox.getSelectedItem();
                if (datastoreName != null) {
                    _datastore = _datastoreCatalog.getDatastore(datastoreName);
                    _masterTermColumnComboBox.setModel(_datastore);
                    _masterTermColumnComboBox.addColumnSelectedListener(new Listener<Column>() {

                        @Override
                        public void onItemSelected(Column column) {
                            final Table table;
                            if (column == null) {
                                table = null;
                            } else {
                                table = column.getTable();
                            }
                            _synonymColumnsPanel.updateSourceComboBoxes(_datastore, table);
                            _synonymColumnsPanel.updateUI();
                        }
                    });

                    _synonymColumnsPanel.setModel(_datastore);
                    if (_datastore != null) {
                        _treePanel.removeAll();
                        Injector injectorWithDatastore = _injectorBuilder.with(Datastore.class, _datastore)
                                .with(AnalysisJobBuilder.class, null).createInjector();

                        final SchemaTree schemaTree = injectorWithDatastore.getInstance(SchemaTree.class);
                        schemaTree.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                TreePath path = schemaTree.getSelectionPath();
                                if (path == null) {
                                    return;
                                }
                                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                                if (node.getUserObject() instanceof Column) {
                                    Column column = (Column) node.getUserObject();

                                    if (_nameAutomaticallySet || StringUtils.isNullOrEmpty(_nameTextField.getText())) {
                                        _nameTextField.setText(column.getName());
                                        _nameAutomaticallySet = true;
                                    }

                                }
                            };
                        });
                    }
                }
            }
        });

        if (synonymCatalog != null) {
            String datastoreName = synonymCatalog.getDatastoreName();

            _nameTextField.setText(synonymCatalog.getName());
            _datastoreComboBox.setSelectedItem(datastoreName);

            Datastore datastore = _datastoreCatalog.getDatastore(datastoreName);
            if (datastore != null) {
                try (final DatastoreConnection datastoreConnection = datastore.openConnection()) {
                    SchemaNavigator sn = datastoreConnection.getSchemaNavigator();

                    Column masterTermColumn = sn.convertToColumn(synonymCatalog.getMasterTermColumnPath());
                    _masterTermColumnComboBox.setSelectedItem(masterTermColumn);

                    String[] synonymColumnPaths = synonymCatalog.getSynonymColumnPaths();
                    Column[] synonymColumns = sn.convertToColumns(synonymColumnPaths);
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
        return 465;
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
        final JButton saveButton = WidgetFactory.createPrimaryButton("Save Synonym Catalog", IconUtils.ACTION_SAVE);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = _nameTextField.getText();
                if (StringUtils.isNullOrEmpty(name)) {
                    JOptionPane.showMessageDialog(DatastoreSynonymCatalogDialog.this,
                            "Please fill out the name of the synonym catalog");
                    return;
                }

                String nameOfDatastore = (String) _datastoreComboBox.getSelectedItem();
                if (StringUtils.isNullOrEmpty(nameOfDatastore)) {
                    JOptionPane.showMessageDialog(DatastoreSynonymCatalogDialog.this,
                            "Please select a character encoding");
                    return;
                }

                Column selectedItem = _masterTermColumnComboBox.getSelectedItem();
                String[] synonymColumnNames = _synonymColumnsPanel.getColumnNames();

                DatastoreSynonymCatalog dataStoreBasedSynonymCatalog = new DatastoreSynonymCatalog(name,
                        nameOfDatastore, selectedItem.getQualifiedLabel(), synonymColumnNames);

                if (_originalsynonymCatalog != null) {
                    _mutableReferenceCatalog.removeSynonymCatalog(_originalsynonymCatalog);
                }

                _mutableReferenceCatalog.addSynonymCatalog(dataStoreBasedSynonymCatalog);
                DatastoreSynonymCatalogDialog.this.dispose();
            }
        });

        final DCPanel buttonPanel = DCPanel.flow(Alignment.CENTER, saveButton);

        final DescriptionLabel descriptionLabel = new DescriptionLabel(
                "A datastore synonym catalog is based on a datastore and columns within it. The layout of the datastore is flexible: There should be a master term column and either a single or multiple synonym columns.");

        final DCPanel mainPanel = new DCPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(descriptionLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.setPreferredSize(getDialogWidth(), 230);

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
