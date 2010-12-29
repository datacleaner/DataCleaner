/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.reference.DataStoreBasedSynonymCatalog;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.SourceColumnComboBox;
import org.eobjects.datacleaner.widgets.label.MultiLineLabel;
import org.eobjects.datacleaner.widgets.tree.SchemaTree;
import org.jdesktop.swingx.JXTextField;

import dk.eobjects.metamodel.schema.Column;

public final class DataStoreSynonymCatalogDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	private final UserPreferences _userPreferences = UserPreferences.getInstance();
	private final DataStoreBasedSynonymCatalog _originalsynonymCatalog;
	private final MutableReferenceDataCatalog _mutableReferenceCatalog;
	private final JComboBox _datastoreComboBox;
	private SourceColumnComboBox _sourceColumnComboBox;	
	private final JXTextField _nameTextField;
	private final JCheckBox _caseSensitiveCheckBox;
	private final DatastoreCatalog _dataStoreCatalog;
	private final DCPanel _treePanel;
	private volatile boolean _nameAutomaticallySet = true;
	private Datastore _datastore;

	public DataStoreSynonymCatalogDialog(MutableReferenceDataCatalog catalog, DatastoreCatalog datastoreCatalog) {
		this(null, catalog, datastoreCatalog);
	}

	public DataStoreSynonymCatalogDialog(DataStoreBasedSynonymCatalog synonymCatalog,
			MutableReferenceDataCatalog mutableReferenceCatalog, DatastoreCatalog datastoreCatalog) {
		_originalsynonymCatalog = synonymCatalog;
		_dataStoreCatalog = datastoreCatalog;
		_mutableReferenceCatalog = mutableReferenceCatalog;
		_nameTextField = WidgetFactory.createTextField("sample name harcoded");
		String[] comboBoxModel = CollectionUtils.array(new String[1], _dataStoreCatalog.getDatastoreNames());

		_datastoreComboBox = new JComboBox(comboBoxModel);
		_sourceColumnComboBox = new SourceColumnComboBox();
		_datastoreComboBox.setEditable(false);


		_treePanel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		_treePanel.setLayout(new BorderLayout());
		_datastoreComboBox.addActionListener(new ActionListener() {

            @Override
			public void actionPerformed(ActionEvent e) {
				String datastoreName = (String) _datastoreComboBox.getSelectedItem();
				if (datastoreName != null) {
					_datastore = _dataStoreCatalog.getDatastore(datastoreName);
					_sourceColumnComboBox.setModel(_datastore);
					if (_datastore != null) {
						_treePanel.removeAll();
						final SchemaTree schemaTree = new SchemaTree(_datastore);
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
		_caseSensitiveCheckBox = new JCheckBox();
		_caseSensitiveCheckBox.setSelected(false);

		if (synonymCatalog != null) {
			_caseSensitiveCheckBox.setSelected(synonymCatalog.isCaseSensitive());
		}
	}

	@Override
	protected String getBannerTitle() {
		return "data Store synonym catalog";
	}

	@Override
	protected int getDialogWidth() {
		return 465;
	}

	@Override
	protected JComponent getDialogContent() {
	    	    	    
		final DCPanel formPanel = new DCPanel();

		int row = 0;
		final MultiLineLabel descriptionLabel = new MultiLineLabel(
				"A text file synonym catalog is a synonym catalog based on a text file containing comma separated values where the first column represents the master term.");
		descriptionLabel.setBorder(new EmptyBorder(10, 10, 10, 20));
		descriptionLabel.setPreferredSize(new Dimension(300, 100));

		WidgetUtils.addToGridBag(new JLabel("Synonym catalog name:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_nameTextField, formPanel, 1, row);
		row++;
		WidgetUtils.addToGridBag(new JLabel("DataStore:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_datastoreComboBox, formPanel, 1, row);
		row++;		
		WidgetUtils.addToGridBag(new JLabel("Case sensitive matches:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_caseSensitiveCheckBox, formPanel, 1, row);
		row++;
		WidgetUtils.addToGridBag(new JLabel("Source Column:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_sourceColumnComboBox, formPanel, 1, row);
		row++;
		final JButton saveButton = WidgetFactory.createButton("Save synonym catalog", "images/model/synonym.png");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = _nameTextField.getText();
				if (StringUtils.isNullOrEmpty(name)) {
					JOptionPane.showMessageDialog(DataStoreSynonymCatalogDialog.this,
							"Please fill out the name of the synonym catalog");
					return;
				}

				String nameOfDataStore = (String) _datastoreComboBox.getSelectedItem();
				if (StringUtils.isNullOrEmpty(nameOfDataStore)) {
					JOptionPane.showMessageDialog(DataStoreSynonymCatalogDialog.this, "Please select a character encoding");
					return;
				}
				
				Datastore datastore = _dataStoreCatalog.getDatastore(nameOfDataStore);

				Column selectedItem = _sourceColumnComboBox.getSelectedItem();
                                DataStoreBasedSynonymCatalog dataStoreBasedSynonymCatalog = new DataStoreBasedSynonymCatalog(name,
				        selectedItem, _caseSensitiveCheckBox.isSelected(), datastore);

				if (_originalsynonymCatalog != null) {
					_mutableReferenceCatalog.removeSynonymCatalog(_originalsynonymCatalog);
				}

				_mutableReferenceCatalog.addSynonymCatalog(dataStoreBasedSynonymCatalog);
				DataStoreSynonymCatalogDialog.this.dispose();
			}
		});

		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttonPanel.add(saveButton);
		WidgetUtils.addToGridBag(buttonPanel, formPanel, 0, row, 2, 1);


		return formPanel;
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	protected String getWindowTitle() {
		return "Text file synonym catalog";
	}

}
