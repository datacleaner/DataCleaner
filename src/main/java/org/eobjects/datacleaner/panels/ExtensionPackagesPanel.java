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
package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.eobjects.datacleaner.user.DCConfiguration;
import org.eobjects.datacleaner.user.ExtensionPackage;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.ExtensionFilter;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCFileChooser;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Panel for configuring extension packages.
 * 
 * @author Kasper Sørensen
 */
public class ExtensionPackagesPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private final UserPreferences userPreferences = UserPreferences.getInstance();
	private final ImageManager imageManager = ImageManager.getInstance();

	public ExtensionPackagesPanel() {
		super(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		setLayout(new BorderLayout());

		updateComponents();
	}

	private void updateComponents() {
		removeAll();

		final List<ExtensionPackage> extensionPackages = UserPreferences.getInstance().getExtensionPackages();

		final JButton addExtensionButton = new JButton("Add extension package",
				imageManager.getImageIcon("images/actions/add.png"));
		addExtensionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DCFileChooser fileChooser = new DCFileChooser(userPreferences.getConfiguredFileDirectory());
				fileChooser.setFileFilter(new ExtensionFilter("DataCleaner extension JAR file (.jar)", ".jar"));
				int result = fileChooser.showOpenDialog(ExtensionPackagesPanel.this);
				if (result == DCFileChooser.APPROVE_OPTION) {
					final File file = fileChooser.getSelectedFile();
					final String packageName = ExtensionPackage.autoDetectPackageName(file);
					if (packageName == null) {
						throw new IllegalStateException(
								"Extension JAR file does not have a single rooted package hierarchy.");
					}
					final ExtensionPackage userExtensionPackage = new ExtensionPackage(file.getName(), packageName, true,
							new File[] { file });
					userExtensionPackage.loadExtension(DCConfiguration.get().getDescriptorProvider());
					extensionPackages.add(userExtensionPackage);

					updateComponents();
				}

			}
		});

		final JToolBar toolBar = WidgetFactory.createToolBar();
		toolBar.add(WidgetFactory.createToolBarSeparator());
		toolBar.add(addExtensionButton);

		final DCPanel listPanel = new DCPanel();
		listPanel.setLayout(new VerticalLayout(4));
		listPanel.setBorder(new EmptyBorder(0, 10, 10, 0));

		for (final ExtensionPackage extensionPackage : extensionPackages) {
			final DCLabel extensionLabel = DCLabel.dark("<html><b>" + extensionPackage.getName() + "</b><br/>Loaded: "
					+ extensionPackage.getLoadedAnalyzers() + " Analyzers, " + extensionPackage.getLoadedTransformers()
					+ " Transformers, " + extensionPackage.getLoadedFilters() + " Filters.<br/>Root package: '"
					+ extensionPackage.getScanPackage() + "'.</html>");
			extensionLabel.setIcon(imageManager.getImageIcon("images/component-types/plugin.png"));

			final JButton removeButton = WidgetFactory.createSmallButton("images/actions/remove.png");
			removeButton.setToolTipText("Remove extension");
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					extensionPackages.remove(extensionPackage);
					removeButton.setEnabled(false);
					extensionLabel.setText("*** Removal requires application restart ***");
				}
			});

			final DCPanel extensionPanel = new DCPanel();
			extensionPanel.setBorder(WidgetUtils.BORDER_LIST_ITEM);

			WidgetUtils.addToGridBag(extensionLabel, extensionPanel, 0, 0, 1.0, 0.0);
			WidgetUtils.addToGridBag(removeButton, extensionPanel, 1, 0, GridBagConstraints.EAST);

			listPanel.add(extensionPanel);
		}

		if (extensionPackages.isEmpty()) {
			listPanel.add(DCLabel.dark("(none)"));
		}

		add(toolBar, BorderLayout.NORTH);
		add(listPanel, BorderLayout.CENTER);

		updateUI();
	}
}
