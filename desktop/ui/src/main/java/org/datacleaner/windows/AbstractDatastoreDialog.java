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

import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.Datastore;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXTextField;

public abstract class AbstractDatastoreDialog<D extends Datastore> extends AbstractDialog {

    private static final String DEFAULT_BANNER_IMAGE = "images/window/banner-datastores.png";

    private static final long serialVersionUID = 1L;

    protected static final ImageManager imageManager = ImageManager.get();

    private final MutableDatastoreCatalog _mutableDatastoreCatalog;
    private final D _originalDatastore;
    private final JButton _saveButton;
    private final JButton _cancelButton;
    private final UserPreferences _userPreferences;
    
    protected final JXTextField _datastoreNameTextField;
    
    protected final DatastoreDialogKeyListener _keyListener = new DatastoreDialogKeyListener();

    protected class DatastoreDialogKeyListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
            onSettingsUpdate();
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

    }

    public AbstractDatastoreDialog(D originalDatastore, MutableDatastoreCatalog mutableDatastoreCatalog,
            WindowContext windowContext, UserPreferences userPreferences) {
        super(windowContext, imageManager.getImage(DEFAULT_BANNER_IMAGE));
        _originalDatastore = originalDatastore;
        _mutableDatastoreCatalog = mutableDatastoreCatalog;
        _userPreferences = userPreferences;
        
        _datastoreNameTextField = WidgetFactory.createTextField();

        _saveButton = WidgetFactory.createPrimaryButton("Save datastore", IconUtils.ACTION_SAVE);
        _saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Datastore datastore = createDatastore();

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
    }

    /**
     * Gets the existing/original datastore definition. If this dialog is in
     * "edit existing datastore" mode then the existing datastore will be
     * returned, otherwise this method will return null.
     * 
     * @return
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
    
    protected void onSettingsUpdate() {
        boolean valid = _datastoreNameTextField.getText().length() > 0;
        setSaveButtonEnabled(valid);
    }

    /**
     * Method for subclasses to invoke for setting the enabled state of the save
     * button
     * 
     * @param enabled
     */
    protected void setSaveButtonEnabled(boolean enabled) {
        _saveButton.setEnabled(enabled);
    }

    @Override
    public Image getWindowIcon() {
        return imageManager.getImage(getDatastoreIconPath());
    }

    @Override
    protected int getDialogWidth() {
        return 500;
    }

    public UserPreferences getUserPreferences() {
        return _userPreferences;
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }
}
