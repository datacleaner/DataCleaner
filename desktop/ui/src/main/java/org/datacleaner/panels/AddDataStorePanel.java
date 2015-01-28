package org.datacleaner.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Dropzone;

public class AddDataStorePanel extends DCPanel {
    private static final long serialVersionUID = 1L;

    public AddDataStorePanel() {
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        Dropzone dropzone = new Dropzone();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.insets = new Insets(0, 0, 10, 0);
        c.fill = GridBagConstraints.BOTH;
        add(dropzone, c);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1.0;
        c.insets = new Insets(0, 0, 0, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        JButton databaseButton = new JButton("Add database", ImageManager.get().getImageIcon(IconUtils.GENERIC_DATASTORE_IMAGEPATH, IconUtils.ICON_SIZE_LARGE));
        WidgetUtils.setWhiteButtonStyle(databaseButton);
        
        add(databaseButton, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1.0;
        c.insets = new Insets(0, 5, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        JButton cloudButton = new JButton("Add cloud service", ImageManager.get().getImageIcon(IconUtils.DATASTORE_TYPE_CLOUD_DARK, IconUtils.ICON_SIZE_LARGE));
        WidgetUtils.setWhiteButtonStyle(cloudButton);
        add(cloudButton, c);
    }
}
