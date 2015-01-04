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
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;

import org.datacleaner.connection.CompositeDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.util.StringUtils;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

public class CompositeDatastoreDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.get();

	private final MutableDatastoreCatalog _mutableDatastoreCatalog;;
	private final List<JCheckBox> _checkBoxes;
	private final JXTextField _datastoreNameField;
	private final JLabel _statusLabel;
	private final DCPanel _outerPanel = new DCPanel();
	private final JButton _addDatastoreButton;
	private final CompositeDatastore _originalDatastore;

	@Override
	protected String getBannerTitle() {
		return "Composite datastore";
	}

	public CompositeDatastoreDialog(MutableDatastoreCatalog mutableDatastoreCatalog, WindowContext windowContext) {
		this(null, mutableDatastoreCatalog, windowContext);
	}

	public CompositeDatastoreDialog(CompositeDatastore originalDatastore, MutableDatastoreCatalog mutableDatastoreCatalog,
			WindowContext windowContext) {
		super(windowContext, imageManager.getImage("images/window/banner-datastores.png"));
		_mutableDatastoreCatalog = mutableDatastoreCatalog;
		_originalDatastore = originalDatastore;
		_statusLabel = DCLabel.bright("");
		_datastoreNameField = WidgetFactory.createTextField("Datastore name");
		_datastoreNameField.getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent event) {
				updateStatusLabel();
			}
		});

		_addDatastoreButton = WidgetFactory.createButton("Save datastore", IconUtils.COMPOSITE_IMAGEPATH);
		_addDatastoreButton.setEnabled(false);

		String[] datastoreNames = _mutableDatastoreCatalog.getDatastoreNames();
		_checkBoxes = new ArrayList<JCheckBox>();
		for (int i = 0; i < datastoreNames.length; i++) {
			String datastoreName = datastoreNames[i];
			if (_originalDatastore == null || !_originalDatastore.getName().equals(datastoreName)) {
				JCheckBox checkBox = new JCheckBox(datastoreName);
				checkBox.setName(datastoreName);
				checkBox.setOpaque(false);
				checkBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
				checkBox.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						updateStatusLabel();
					}
				});
				_checkBoxes.add(checkBox);
			}
		}

		if (_originalDatastore != null) {
			_datastoreNameField.setText(_originalDatastore.getName());
			_datastoreNameField.setEnabled(false);

			List<? extends Datastore> containedDatastores = _originalDatastore.getDatastores();
			Set<String> containedDatastoreNames = new HashSet<String>();
			for (Datastore datastore : containedDatastores) {
				containedDatastoreNames.add(datastore.getName());
			}
			for (JCheckBox checkBox : _checkBoxes) {
				if (containedDatastoreNames.contains(checkBox.getText())) {
					checkBox.setSelected(true);
				}
			}
		}

		updateStatusLabel();
	}

	public void updateStatusLabel() {
		int selected = 0;
		for (JCheckBox checkBox : _checkBoxes) {
			if (checkBox.isSelected()) {
				selected++;
			}
		}

		boolean nameFilledOut = !StringUtils.isNullOrEmpty(_datastoreNameField.getText());

		if (selected < 2) {
			_statusLabel.setText("Please select at least 2 contained datastores");
			_statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_ERROR, IconUtils.ICON_SIZE_SMALL));
			_addDatastoreButton.setEnabled(false);
		} else {
			if (nameFilledOut) {
				_statusLabel.setText("Composite datastore ready");
				_statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_VALID, IconUtils.ICON_SIZE_SMALL));
				_addDatastoreButton.setEnabled(true);
			} else {
				_statusLabel.setText("Please fill out a datastore name");
				_statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_ERROR, IconUtils.ICON_SIZE_SMALL));
				_addDatastoreButton.setEnabled(false);
			}
		}
	}

	@Override
	protected int getDialogWidth() {
		return 400;
	}

	@Override
	protected JComponent getDialogContent() {
		final DCPanel formPanel = new DCPanel();

		// temporary variable to make it easier to refactor the layout
		WidgetUtils.addToGridBag(DCLabel.bright("Datastore name:"), formPanel, 0, 0);
		WidgetUtils.addToGridBag(_datastoreNameField, formPanel, 1, 0);

		final DCPanel checkBoxPanel = new DCPanel().setTitledBorder("Contained datastores");
		checkBoxPanel.setLayout(new VerticalLayout(4));

		for (JCheckBox checkBox : _checkBoxes) {
			checkBoxPanel.add(checkBox);
		}

		WidgetUtils.addToGridBag(checkBoxPanel, formPanel, 0, 1, 2, 1);

		_addDatastoreButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<Datastore> datastores = new ArrayList<Datastore>();

				for (JCheckBox checkBox : _checkBoxes) {
					if (checkBox.isSelected()) {
						String datastoreName = checkBox.getText();
						Datastore datastore = _mutableDatastoreCatalog.getDatastore(datastoreName);
						if (datastore == null) {
							throw new IllegalStateException("No such datastore: " + datastoreName);
						}
						datastores.add(datastore);
					}
				}

				final Datastore datastore = new CompositeDatastore(_datastoreNameField.getText(), datastores);

				if (_originalDatastore != null) {
					_mutableDatastoreCatalog.removeDatastore(_originalDatastore);
				}

				_mutableDatastoreCatalog.addDatastore(datastore);
				dispose();
			}
		});

		DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttonPanel.add(_addDatastoreButton);

		DCPanel centerPanel = new DCPanel();
		centerPanel.setLayout(new VerticalLayout(4));
		centerPanel.add(formPanel);
		centerPanel.add(buttonPanel);

		JXStatusBar statusBar = WidgetFactory.createStatusBar(_statusLabel);

		_outerPanel.setLayout(new BorderLayout());
		_outerPanel.add(centerPanel, BorderLayout.CENTER);
		_outerPanel.add(statusBar, BorderLayout.SOUTH);

		return _outerPanel;
	}

	@Override
	public String getWindowTitle() {
		return "Composite datastore | Datastore";
	}

	@Override
	public Image getWindowIcon() {
		return imageManager.getImage(IconUtils.COMPOSITE_IMAGEPATH);
	}
}
