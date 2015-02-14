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
package org.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.extensions.ExtensionPackage;
import org.datacleaner.extensions.ExtensionReader;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.ExtensionFilter;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DCFileChooser;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.PopupButton;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.action.OpenBrowserAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.colt.Arrays;

/**
 * Panel for configuring extension packages.
 */
public class ExtensionPackagesPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(ExtensionPackagesPanel.class);
    private static final ImageManager imageManager = ImageManager.get();

    private static final ImageIcon ICON_PLUGIN = imageManager.getImageIcon(IconUtils.PLUGIN);
    private static final ImageIcon ICON_ERROR = imageManager.getImageIcon(IconUtils.STATUS_ERROR);

    private final UserPreferences _userPreferences;
    private final AnalyzerBeansConfiguration _configuration;

    @Inject
    protected ExtensionPackagesPanel(AnalyzerBeansConfiguration configuration, UserPreferences userPreferences) {
        super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _configuration = configuration;
        _userPreferences = userPreferences;

        setLayout(new BorderLayout());

        updateComponents();
    }

    private void updateComponents() {
        removeAll();

        final PopupButton addExtensionButton = WidgetFactory.createDefaultPopupButton("Add extension package",
                IconUtils.ACTION_ADD);
        final JPopupMenu addExtensionMenu = addExtensionButton.getMenu();

        final JMenuItem extensionSwapMenuItem = new JMenuItem("Browse the ExtensionSwap",
                imageManager.getImageIcon("images/actions/website.png"));
        extensionSwapMenuItem.addActionListener(new OpenBrowserAction("http://datacleaner.org/extensions"));

        final JMenuItem manualInstallMenuItem = new JMenuItem("Manually install JAR file",
                imageManager.getImageIcon("images/filetypes/archive.png"));
        manualInstallMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final DCFileChooser fileChooser = new DCFileChooser(_userPreferences.getConfiguredFileDirectory());
                fileChooser.setMultiSelectionEnabled(true);
                fileChooser.setFileFilter(new ExtensionFilter("DataCleaner extension JAR file (.jar)", ".jar"));
                int result = fileChooser.showOpenDialog(ExtensionPackagesPanel.this);
                if (result == DCFileChooser.APPROVE_OPTION) {

                    final File[] files = fileChooser.getSelectedFiles();

                    final ExtensionReader extensionReader = new ExtensionReader();
                    final ExtensionPackage extensionPackage = extensionReader.readExternalExtension(files);

                    extensionPackage.loadDescriptors(_configuration.getDescriptorProvider());
                    _userPreferences.addExtensionPackage(extensionPackage);

                    updateComponents();
                }
            }
        });

        addExtensionMenu.add(extensionSwapMenuItem);
        addExtensionMenu.add(manualInstallMenuItem);

        final DCPanel listPanel = new DCPanel();
        listPanel.setLayout(new VerticalLayout(4));
        listPanel.setBorder(new EmptyBorder(0, 10, 10, 0));

        for (final ExtensionPackage extensionPackage : _userPreferences.getExtensionPackages()) {
            final DCPanel extensionPanel = createExtensionPanel(extensionPackage);
            listPanel.add(extensionPanel);
        }

        listPanel.add(Box.createVerticalStrut(20));

        for (final ExtensionPackage extensionPackage : new ExtensionReader().getInternalExtensions()) {
            final DCPanel extensionPanel = createExtensionPanel(extensionPackage);
            listPanel.add(extensionPanel);
        }

        final JScrollPane scrollArea = WidgetUtils.scrolleable(listPanel);
        scrollArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(DCPanel.flow(Alignment.RIGHT, addExtensionButton), BorderLayout.NORTH);
        add(scrollArea, BorderLayout.CENTER);

        updateUI();
    }

    private DCPanel createExtensionPanel(final ExtensionPackage extensionPackage) {
        boolean valid = true;
        final File[] files = extensionPackage.getFiles();
        if (extensionPackage.isExternal()) {
            for (File file : files) {
                if (!file.exists()) {
                    valid = false;
                }
            }
        }

        final DCLabel extensionLabel;
        if (valid) {
            final ImageIcon extensionIcon = getExtensionIcon(extensionPackage);

            final StringBuilder labelBuilder = new StringBuilder();
            labelBuilder.append("<html><div style='width:280px'><b>");
            labelBuilder.append(extensionPackage.getName());
            labelBuilder.append("</b>");

            final String author = extensionPackage.getAuthor();
            if (author != null) {
                labelBuilder.append("<br/>By ");
                labelBuilder.append(author);
            }

            final String description = extensionPackage.getDescription();
            if (description != null) {
                labelBuilder.append("<br/>");
                labelBuilder.append(description);
            }

            final String version = extensionPackage.getVersion();
            if (version != null) {
                labelBuilder.append("<br/>Version ");
                labelBuilder.append(version);
            }

            labelBuilder.append("</div></html>");

            extensionLabel = DCLabel.dark(labelBuilder.toString());
            extensionLabel.setIcon(extensionIcon);
        } else {
            extensionLabel = DCLabel.dark("<html><div style='width:300px'><b>" + extensionPackage.getName()
                    + "</b><br/>Error loading extension files:<br/>" + Arrays.toString(files) + "</div></html>");
            extensionLabel.setIcon(ICON_ERROR);
        }

        extensionLabel.setFont(WidgetUtils.FONT_SMALL);

        final DCPanel extensionPanel = new DCPanel();
        extensionPanel.setBorder(WidgetUtils.BORDER_LIST_ITEM);

        int col = 0;

        WidgetUtils.addToGridBag(extensionLabel, extensionPanel, col, 0, 1.0, 0.0);
        col++;

        final String url = extensionPackage.getUrl();
        if (url != null) {
            final JButton urlButton = WidgetFactory.createSmallButton(IconUtils.WEBSITE);
            urlButton.addActionListener(new OpenBrowserAction(url));
            WidgetUtils.addToGridBag(urlButton, extensionPanel, col, 0, GridBagConstraints.EAST);
            col++;
        }

        if (extensionPackage.isExternal()) {
            final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE);
            removeButton.setToolTipText("Remove extension");
            removeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    _userPreferences.removeExtensionPackage(extensionPackage);
                    removeButton.setEnabled(false);
                    extensionLabel.setText("*** Removal requires application restart ***");
                }
            });
            WidgetUtils.addToGridBag(removeButton, extensionPanel, col, 0, GridBagConstraints.EAST);
        } else {
            // make extensions that cannot be removed less emphasized
            extensionLabel.setForeground(WidgetUtils.BG_COLOR_LESS_DARK);
        }

        return extensionPanel;
    }

    private ImageIcon getExtensionIcon(ExtensionPackage extensionPackage) {
        final String iconPath = extensionPackage.getAdditionalProperties().get("icon");
        if (iconPath != null) {
            try {
                final ImageIcon imageIcon = imageManager.getImageIcon(iconPath, IconUtils.ICON_SIZE_LARGE,
                        ExtensionPackage.getExtensionClassLoader());
                if (imageIcon != null) {
                    return imageIcon;
                }
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to get extension icon of path: " + iconPath, e);
                }
            }
        }
        return ICON_PLUGIN;
    }
}
