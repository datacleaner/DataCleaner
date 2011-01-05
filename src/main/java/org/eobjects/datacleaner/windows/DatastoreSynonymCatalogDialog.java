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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.reference.DatastoreSynonymCatalog;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.SourceColumnComboBox;
import org.eobjects.datacleaner.widgets.label.MultiLineLabel;
import org.eobjects.datacleaner.widgets.tree.SchemaTree;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

import dk.eobjects.metamodel.schema.Column;

public final class DatastoreSynonymCatalogDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	private final DatastoreSynonymCatalog _originalsynonymCatalog;
	private final MutableReferenceDataCatalog _mutableReferenceCatalog;
	private final JComboBox _datastoreComboBox;
	private SourceColumnComboBox _sourceColumnComboBox;
	private final JXTextField _nameTextField;
	private final DatastoreCatalog _datastoreCatalog;
	private Datastore _datastore;
	private final DCPanel _treePanel;
	private volatile boolean _nameAutomaticallySet = true;
	private MultiSourceColumnComboBoxPanel _multiSourceColumnComboBoxPanel;

	public DatastoreSynonymCatalogDialog(MutableReferenceDataCatalog catalog, DatastoreCatalog datastoreCatalog) {
		this(null, catalog, datastoreCatalog);
	}

	public DatastoreSynonymCatalogDialog(DatastoreSynonymCatalog synonymCatalog,
			MutableReferenceDataCatalog mutableReferenceCatalog, DatastoreCatalog datastoreCatalog) {
		_originalsynonymCatalog = synonymCatalog;
		_datastoreCatalog = datastoreCatalog;
		_mutableReferenceCatalog = mutableReferenceCatalog;
		_nameTextField = WidgetFactory.createTextField("sample name harcoded");
		String[] comboBoxModel = CollectionUtils.array(new String[1], _datastoreCatalog.getDatastoreNames());

		_datastoreComboBox = new JComboBox(comboBoxModel);
		_sourceColumnComboBox = new SourceColumnComboBox();
		_multiSourceColumnComboBoxPanel = new MultiSourceColumnComboBoxPanel();
		_datastoreComboBox.setEditable(false);
		_treePanel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		_treePanel.setLayout(new BorderLayout());

		_datastoreComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String datastoreName = (String) _datastoreComboBox.getSelectedItem();
				if (datastoreName != null) {
					_datastore = _datastoreCatalog.getDatastore(datastoreName);
					_sourceColumnComboBox.setModel(_datastore);
					_multiSourceColumnComboBoxPanel.setModel(_datastore);
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
	}

	@Override
	protected String getBannerTitle() {
		return "Data Store Synonym Catalog";
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
				"A datastore synonym catalog is based on a datastore containing columns. The Source Column represents the user selected master term. Synonyms Column are user selected synonymical representation.");
		descriptionLabel.setBorder(new EmptyBorder(10, 10, 10, 20));
		descriptionLabel.setPreferredSize(new Dimension(300, 100));
		WidgetUtils.addToGridBag(new JLabel("Synonym Catalog Name:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_nameTextField, formPanel, 1, row);
		row++;
		WidgetUtils.addToGridBag(new JLabel("Datastore:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_datastoreComboBox, formPanel, 1, row);
		row++;
		WidgetUtils.addToGridBag(new JLabel("Source Column:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_sourceColumnComboBox, formPanel, 1, row);
		row++;
		WidgetUtils.addToGridBag(new JLabel("Synonym Columns:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_multiSourceColumnComboBoxPanel.createPanel(), formPanel, 1, row);
		row++;
		final JButton saveButton = WidgetFactory.createButton("Save Synonym Catalog", "images/model/synonym.png");
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
					JOptionPane.showMessageDialog(DatastoreSynonymCatalogDialog.this, "Please select a character encoding");
					return;
				}

				Column selectedItem = _sourceColumnComboBox.getSelectedItem();
				String[] synonymColumnNames = _multiSourceColumnComboBoxPanel.getColumnNames();

				DatastoreSynonymCatalog dataStoreBasedSynonymCatalog = new DatastoreSynonymCatalog(name, _datastoreCatalog,
						nameOfDatastore, selectedItem.getQualifiedLabel(), synonymColumnNames);

				if (_originalsynonymCatalog != null) {
					_mutableReferenceCatalog.removeSynonymCatalog(_originalsynonymCatalog);
				}

				_mutableReferenceCatalog.addSynonymCatalog(dataStoreBasedSynonymCatalog);
				DatastoreSynonymCatalogDialog.this.dispose();
			}
		});

		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttonPanel.add(saveButton);
		WidgetUtils.addToGridBag(buttonPanel, formPanel, 0, row, 2, 1);

		final DCPanel mainPanel = new DCPanel();
		mainPanel.setLayout(new VerticalLayout(4));
		mainPanel.add(descriptionLabel);
		mainPanel.add(formPanel);

		return mainPanel;
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	protected String getWindowTitle() {
		return "Data Store Synonym Catalog";
	}

}
