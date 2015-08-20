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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.Datastore;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.ErrorUtils;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.ImmutableEntry;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DescriptionLabel;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTextField;

public abstract class AbstractDatastoreDialog<D extends Datastore> extends AbstractDialog {

    private static final String DEFAULT_BANNER_IMAGE = "images/window/banner-datastores.png";

    private static final long serialVersionUID = 1L;

    protected static final ImageManager imageManager = ImageManager.get();

    protected final JLabel _statusLabel;
    private final MutableDatastoreCatalog _mutableDatastoreCatalog;
    private final D _originalDatastore;
    private D _savedDatastore = null;
    private final JButton _saveButton;
    private final JButton _cancelButton;
    private final UserPreferences _userPreferences;
    
    protected final DCPanel _outerPanel = new DCPanel();
    
    protected final JXTextField _datastoreNameTextField;
    
    public AbstractDatastoreDialog(D originalDatastore, MutableDatastoreCatalog mutableDatastoreCatalog,
            WindowContext windowContext, UserPreferences userPreferences) {
        super(windowContext, imageManager.getImage(DEFAULT_BANNER_IMAGE));
        _statusLabel = DCLabel.bright("Please specify datastore name");
        _originalDatastore = originalDatastore;
        _mutableDatastoreCatalog = mutableDatastoreCatalog;
        _userPreferences = userPreferences;
        
        _datastoreNameTextField = WidgetFactory.createTextField("Datastore name");

        _saveButton = WidgetFactory.createPrimaryButton("Save datastore", IconUtils.ACTION_SAVE_BRIGHT);
        _saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Datastore datastore = _savedDatastore = createDatastore();

                if (_originalDatastore != null) {
                    _mutableDatastoreCatalog.removeDatastore(_originalDatastore);
                }

                _mutableDatastoreCatalog.addDatastore(datastore);
                dispose();
            }
        });

        _cancelButton = WidgetFactory.createDefaultButton("Cancel", IconUtils.ACTION_CANCEL);
        _cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractDatastoreDialog.this.close();
            }
        });
        
        if (!DEFAULT_BANNER_IMAGE.equals(getDatastoreIconPath())) {
            final Image image = imageManager.getImage(getDatastoreIconPath());
            setBannerImage(image);
        }
        
        if (originalDatastore != null) {
            _datastoreNameTextField.setText(originalDatastore.getName());
            _datastoreNameTextField.setEnabled(false);
        }
        
        // add listeners after setting initial values.
        _datastoreNameTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                validateAndUpdateInternal();
            }
        });
    }

    protected void validateAndUpdate() {
        validateAndUpdateInternal();
    }

    private void validateAndUpdateInternal() {
        boolean valid = validateForm();
        setSaveButtonEnabled(valid);
    }

    protected boolean validateForm() {
        final String datastoreName = _datastoreNameTextField.getText();
        if (StringUtils.isNullOrEmpty(datastoreName)) {
            setStatusError("Please enter a datastore name");
            return false;
        }

        setStatusValid();
        return true;
    }
    
    protected void setStatusWarning(String text) {
        _statusLabel.setText(text);
        _statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_WARNING, IconUtils.ICON_SIZE_SMALL));
    }
    
    protected void setStatusError(Throwable error) {
        error = ErrorUtils.unwrapForPresentation(error);
        setStatusError(error.getMessage());
    }
    
    protected void setStatusError(String text) {
        _statusLabel.setText(text);
        _statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_ERROR, IconUtils.ICON_SIZE_SMALL));
    }
    
    protected void setStatusValid() {
        _statusLabel.setText("Datastore ready");
        _statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_VALID, IconUtils.ICON_SIZE_SMALL));
    }

    /**
     * Gets the existing/original datastore definition. If this dialog is in
     * "edit existing datastore" mode then the existing datastore will be
     * returned, otherwise this method will return null.
     * 
     */
    public D getOriginalDatastore() {
        return _originalDatastore;
    }

    protected DCPanel getButtonPanel() {
        final DCPanel buttonPanel = new DCPanel();
        buttonPanel.setBorder(WidgetUtils.BORDER_EMPTY);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));
        buttonPanel.add(_saveButton);
        buttonPanel.add(_cancelButton);
        return buttonPanel;
    }

    protected abstract D createDatastore();

    protected abstract String getDatastoreIconPath();
    
    /**
     * Method for subclasses to invoke for setting the enabled state of the save
     * button
     * 
     */
    protected void setSaveButtonEnabled(boolean enabled) {
        _saveButton.setEnabled(enabled);
    }

    public D getSavedDatastore(){
        return _savedDatastore;
    }
    
    @Override
    public Image getWindowIcon() {
        return imageManager.getImage(getDatastoreIconPath());
    }

    @Override
    protected int getDialogWidth() {
        return 600;
    }

    public UserPreferences getUserPreferences() {
        return _userPreferences;
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }
    
    @Override
    protected JComponent getDialogContent() {
        DCPanel formPanel = new DCPanel();

        List<Entry<String, JComponent>> formElements = getFormElements();
        // temporary variable to make it easier to refactor the layout
        int row = 0;
        for (Entry<String, JComponent> entry : formElements) {
            String key = entry.getKey();
            if (StringUtils.isNullOrEmpty(key)) {
                WidgetUtils.addToGridBag(entry.getValue(), formPanel, 0, row, 2, 1);
            } else {
                WidgetUtils.addToGridBag(DCLabel.bright(key + ":"), formPanel, 0, row);
                WidgetUtils.addToGridBag(entry.getValue(), formPanel, 1, row);
            }
            row++;
        }

        final DCPanel centerPanel = new DCPanel();
        centerPanel.setLayout(new GridBagLayout());
        WidgetUtils.addToGridBag(formPanel, centerPanel, 0, 0, 1, 1, GridBagConstraints.NORTH, 4, 0, 0);

        WidgetUtils.addToGridBag(getButtonPanel(), centerPanel, 0, 2, 1, 1, GridBagConstraints.SOUTH, 4, 0, 0.1);

        centerPanel.setBorder(WidgetUtils.BORDER_TOP_PADDING);

        JXStatusBar statusBar = WidgetFactory.createStatusBar(_statusLabel);

        _outerPanel.setLayout(new BorderLayout());
        _outerPanel.add(centerPanel, BorderLayout.CENTER);
        _outerPanel.add(statusBar, BorderLayout.SOUTH);

        final String descriptionText = getDescriptionText();
        if (descriptionText != null) {
            DescriptionLabel descriptionLabel = new DescriptionLabel();
            descriptionLabel.setText(descriptionText);
            _outerPanel.add(descriptionLabel, BorderLayout.NORTH);
        }

        validateAndUpdate();

        return _outerPanel;
    }
    
    protected List<Entry<String, JComponent>> getFormElements() {
        ArrayList<Entry<String, JComponent>> res = new ArrayList<>();
        res.add(new ImmutableEntry<String, JComponent>("Datastore name", _datastoreNameTextField));
        return res;
    }
    
    protected String getDescriptionText() {
        return "Configure your datastore in this dialog.";
    }
}
