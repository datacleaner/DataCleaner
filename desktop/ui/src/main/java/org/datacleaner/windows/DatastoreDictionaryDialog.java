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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.event.DocumentEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.util.CollectionUtils;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.guice.Nullable;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.reference.DatastoreDictionary;
import org.datacleaner.user.MutableReferenceDataCatalog;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DescriptionLabel;
import org.datacleaner.widgets.tree.SchemaTree;
import org.jdesktop.swingx.JXTextField;

import com.google.inject.Injector;

public final class DatastoreDictionaryDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;
    private final DatastoreDictionary _originalDictionary;
    private final MutableReferenceDataCatalog _referenceDataCatalog;
    private final DatastoreCatalog _datastoreCatalog;
    private final JXTextField _nameTextField;
    private final JXTextField _columnTextField;
    private final JComboBox<String> _datastoreComboBox;
    private final DCPanel _treePanel;
    private final JSplitPane _splitPane;
    private final InjectorBuilder _injectorBuilder;
    private volatile boolean _nameAutomaticallySet = true;

    @Inject
    protected DatastoreDictionaryDialog(@Nullable final DatastoreDictionary dictionary,
            final MutableReferenceDataCatalog referenceDataCatalog, final DatastoreCatalog datastoreCatalog,
            final WindowContext windowContext, final InjectorBuilder injectorBuilder) {
        super(windowContext, ImageManager.get().getImage(IconUtils.DICTIONARY_DATASTORE_IMAGEPATH));
        _originalDictionary = dictionary;
        _referenceDataCatalog = referenceDataCatalog;
        _datastoreCatalog = datastoreCatalog;
        _injectorBuilder = injectorBuilder;

        _nameTextField = WidgetFactory.createTextField("Dictionary name");
        _nameTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(final DocumentEvent e) {
                _nameAutomaticallySet = false;
            }
        });
        _columnTextField = WidgetFactory.createTextField("Column name");

        final String[] comboBoxModel = CollectionUtils.array(new String[1], _datastoreCatalog.getDatastoreNames());

        _datastoreComboBox = new JComboBox<>(comboBoxModel);
        _datastoreComboBox.setEditable(false);

        _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        _splitPane.setBackground(WidgetUtils.BG_COLOR_DARK);
        _splitPane.setBorder(WidgetUtils.BORDER_EMPTY);
        _splitPane.setDividerLocation(320);
        _splitPane.setLayout(new BorderLayout());

        _treePanel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _treePanel.setLayout(new BorderLayout());
        _datastoreComboBox.addActionListener(e -> {
            final String datastoreName = (String) _datastoreComboBox.getSelectedItem();
            if (datastoreName != null) {
                final Datastore datastore = _datastoreCatalog.getDatastore(datastoreName);
                if (datastore != null) {
                    _treePanel.removeAll();

                    final Injector injectorWithDatastore =
                            _injectorBuilder.with(Datastore.class, datastore).with(AnalyzerComponentBuilder.class, null)
                                    .createInjector();

                    final SchemaTree schemaTree = injectorWithDatastore.getInstance(SchemaTree.class);
                    schemaTree.setIncludeLibraryNode(false);
                    schemaTree.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(final MouseEvent e) {
                            final TreePath path = schemaTree.getSelectionPath();
                            if (path == null) {
                                return;
                            }

                            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                            if (node.getUserObject() instanceof Column) {
                                final Column column = (Column) node.getUserObject();

                                if (_nameAutomaticallySet || StringUtils.isNullOrEmpty(_nameTextField.getText())) {
                                    _nameTextField.setText(column.getName());
                                    _nameAutomaticallySet = true;
                                }

                                _columnTextField.setText(column.getQualifiedLabel());
                            }
                        }

                    });
                    _treePanel.add(WidgetUtils.scrolleable(schemaTree), BorderLayout.CENTER);
                    _treePanel.updateUI();
                }
            }
        });

        if (dictionary != null) {
            _nameTextField.setText(dictionary.getName());
            _columnTextField.setText(dictionary.getQualifiedColumnName());
            _datastoreComboBox.setSelectedItem(dictionary.getDatastoreName());
        }
    }

    @Override
    protected String getBannerTitle() {
        return "Datastore dictionary";
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
        final DCPanel formPanel = new DCPanel();

        int row = 0;
        WidgetUtils.addToGridBag(DCLabel.bright("Dictionary name:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_nameTextField, formPanel, 1, row);

        row++;
        WidgetUtils.addToGridBag(DCLabel.bright("Datastore:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_datastoreComboBox, formPanel, 1, row);

        row++;
        WidgetUtils.addToGridBag(DCLabel.bright("Lookup column:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_columnTextField, formPanel, 1, row);

        final JButton createDictionaryButton =
                WidgetFactory.createPrimaryButton("Save dictionary", IconUtils.ACTION_SAVE_BRIGHT);
        createDictionaryButton.addActionListener(e -> {
            final String name1 = _nameTextField.getText();
            if (StringUtils.isNullOrEmpty(name1)) {
                JOptionPane.showMessageDialog(DatastoreDictionaryDialog.this,
                        "Please fill out the name of the dictionary");
                return;
            }

            final String datastoreName = (String) _datastoreComboBox.getSelectedItem();
            if (StringUtils.isNullOrEmpty(datastoreName)) {
                JOptionPane.showMessageDialog(DatastoreDictionaryDialog.this, "Please select a datastore");
                return;
            }

            final String columnPath = _columnTextField.getText();
            if (StringUtils.isNullOrEmpty(columnPath)) {
                JOptionPane.showMessageDialog(DatastoreDictionaryDialog.this, "Please select a lookup column");
                return;
            }

            final DatastoreDictionary dictionary = new DatastoreDictionary(name1, datastoreName, columnPath);
            if (_originalDictionary != null) {
                _referenceDataCatalog.changeDictionary(_originalDictionary, dictionary);
            } else {
                _referenceDataCatalog.addDictionary(dictionary);
            }
            DatastoreDictionaryDialog.this.dispose();
        });

        final DCPanel buttonPanel = DCPanel.flow(Alignment.CENTER, createDictionaryButton);

        final DescriptionLabel descriptionLabel = new DescriptionLabel("A datastore dictionary is a dictionary based "
                + "on a column in one of your datastores. Please select a datastore in the form below and a tree of "
                + "that datastore will appear. From here on you can select which column in the datastore to "
                + "use for dictionary lookups.");

        _splitPane.add(formPanel, BorderLayout.WEST);
        _splitPane.add(_treePanel, BorderLayout.CENTER);

        final DCPanel outerPanel = new DCPanel();
        outerPanel.setLayout(new BorderLayout());
        outerPanel.add(descriptionLabel, BorderLayout.NORTH);
        outerPanel.add(_splitPane, BorderLayout.CENTER);
        outerPanel.add(buttonPanel, BorderLayout.SOUTH);

        outerPanel.setPreferredSize(getDialogWidth(), 400);

        return outerPanel;
    }

    @Override
    public String getWindowTitle() {
        return "Datastore dictionary";
    }

}
