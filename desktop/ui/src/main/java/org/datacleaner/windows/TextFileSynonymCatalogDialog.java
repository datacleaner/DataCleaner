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

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;

import org.apache.metamodel.util.Resource;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.guice.Nullable;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.reference.TextFileSynonymCatalog;
import org.datacleaner.user.MutableReferenceDataCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.CharSetEncodingComboBox;
import org.datacleaner.widgets.DCCheckBox;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DescriptionLabel;
import org.datacleaner.widgets.ResourceSelector;
import org.datacleaner.widgets.ResourceTypePresenter;
import org.jdesktop.swingx.JXTextField;

public final class TextFileSynonymCatalogDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final TextFileSynonymCatalog _originalsynonymCatalog;
    private final MutableReferenceDataCatalog _catalog;
    private final JXTextField _nameTextField;
    private final DCCheckBox<Boolean> _caseSensitiveCheckBox;
    private final ResourceSelector _resourceSelector;
    private final CharSetEncodingComboBox _encodingComboBox;
    private volatile boolean _nameAutomaticallySet = true;

    @Inject
    protected TextFileSynonymCatalogDialog(@Nullable TextFileSynonymCatalog synonymCatalog,
            MutableReferenceDataCatalog catalog, WindowContext windowContext, DataCleanerConfiguration configuration,
            UserPreferences userPreferences) {
        super(windowContext, ImageManager.get().getImage(IconUtils.SYNONYM_CATALOG_TEXTFILE_IMAGEPATH));
        _originalsynonymCatalog = synonymCatalog;
        _catalog = catalog;

        _nameTextField = WidgetFactory.createTextField("Synonym catalog name");
        _nameTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent e) {
                _nameAutomaticallySet = false;
            }
        });

        _resourceSelector = new ResourceSelector(configuration, userPreferences, true);
        _resourceSelector.addListener(new ResourceTypePresenter.Listener() {
            @Override
            public void onResourceSelected(ResourceTypePresenter<?> presenter, Resource resource) {
                if (_nameAutomaticallySet || StringUtils.isNullOrEmpty(_nameTextField.getText())) {
                    _nameTextField.setText(resource.getName());
                    _nameAutomaticallySet = true;
                }
            }

            @Override
            public void onPathEntered(ResourceTypePresenter<?> presenter, String path) {
                if (_nameAutomaticallySet || StringUtils.isNullOrEmpty(_nameTextField.getText())) {
                    _nameTextField.setText(path);
                    _nameAutomaticallySet = true;
                }
            }
        });

        _caseSensitiveCheckBox = new DCCheckBox<>("Case-sensitive?", false);
        _caseSensitiveCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        _caseSensitiveCheckBox.setOpaque(false);
        _caseSensitiveCheckBox.setToolTipText("Only match on dictionary terms when text-case is the same.");

        _encodingComboBox = new CharSetEncodingComboBox();

        if (synonymCatalog != null) {
            _nameTextField.setText(synonymCatalog.getName());
            _resourceSelector.setResourcePath(synonymCatalog.getFilename());
            _encodingComboBox.setSelectedItem(synonymCatalog.getEncoding());
            _caseSensitiveCheckBox.setSelected(synonymCatalog.isCaseSensitive());
        }
    }

    @Override
    protected String getBannerTitle() {
        return "Text file\nsynonym catalog";
    }

    @Override
    protected int getDialogWidth() {
        return 600;
    }

    @Override
    protected JComponent getDialogContent() {
        final DCPanel formPanel = new DCPanel();

        int row = 0;
        WidgetUtils.addToGridBag(DCLabel.bright("Synonym catalog name:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_nameTextField, formPanel, 1, row);

        row++;
        WidgetUtils.addToGridBag(DCLabel.bright("Path:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_resourceSelector, formPanel, 1, row);

        row++;
        WidgetUtils.addToGridBag(DCLabel.bright("Character encoding:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_encodingComboBox, formPanel, 1, row);

        row++;
        WidgetUtils.addToGridBag(_caseSensitiveCheckBox, formPanel, 1, row);

        row++;
        final JButton saveButton = WidgetFactory.createPrimaryButton("Save synonym catalog",
                IconUtils.ACTION_SAVE_BRIGHT);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String name = _nameTextField.getText();
                if (StringUtils.isNullOrEmpty(name)) {
                    JOptionPane.showMessageDialog(TextFileSynonymCatalogDialog.this,
                            "Please fill out the name of the synonym catalog");
                    return;
                }

                final String path = _resourceSelector.getResourcePath();
                if (StringUtils.isNullOrEmpty(path)) {
                    JOptionPane.showMessageDialog(TextFileSynonymCatalogDialog.this,
                            "Please fill out the path or select a file using the 'Browse' button");
                    return;
                }

                final String encoding = (String) _encodingComboBox.getSelectedItem();
                if (StringUtils.isNullOrEmpty(encoding)) {
                    JOptionPane.showMessageDialog(TextFileSynonymCatalogDialog.this,
                            "Please select a character encoding");
                    return;
                }

                final boolean caseSensitive = _caseSensitiveCheckBox.isSelected();

                final TextFileSynonymCatalog sc = new TextFileSynonymCatalog(name, path, caseSensitive, encoding);

                if (_originalsynonymCatalog != null) {
                    _catalog.removeSynonymCatalog(_originalsynonymCatalog);
                }
                _catalog.addSynonymCatalog(sc);
                TextFileSynonymCatalogDialog.this.dispose();
            }
        });

        final DCPanel buttonPanel = DCPanel.flow(Alignment.CENTER, saveButton);

        final DescriptionLabel descriptionLabel = new DescriptionLabel(
                "A text file synonym catalog is a synonym catalog based on a text file containing comma separated values where the first column represents the master term.");

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
        return "Text file synonym catalog";
    }

}
