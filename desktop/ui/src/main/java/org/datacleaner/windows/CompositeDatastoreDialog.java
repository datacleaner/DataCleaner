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
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.CompositeDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCCheckBox;
import org.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

import com.google.inject.Inject;

public class CompositeDatastoreDialog extends AbstractDatastoreDialog<CompositeDatastore> {

    private static final long serialVersionUID = 1L;

    private static final ImageManager imageManager = ImageManager.get();

    private final List<DCCheckBox<Datastore>> _checkBoxes;
    private final JXTextField _datastoreNameField;
    private final JLabel _statusLabel;
    private final DCPanel _outerPanel = new DCPanel();

    @Override
    protected String getBannerTitle() {
        return "Composite datastore";
    }

    @Inject
    public CompositeDatastoreDialog(MutableDatastoreCatalog mutableDatastoreCatalog, WindowContext windowContext,
            UserPreferences userPreferences) {
        this(null, mutableDatastoreCatalog, windowContext, userPreferences);
    }

    public CompositeDatastoreDialog(CompositeDatastore originalDatastore,
            MutableDatastoreCatalog mutableDatastoreCatalog, WindowContext windowContext,
            UserPreferences userPreferences) {
        super(originalDatastore, mutableDatastoreCatalog, windowContext, userPreferences);
        _statusLabel = DCLabel.bright("");
        _datastoreNameField = WidgetFactory.createTextField("Datastore name");
        _datastoreNameField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                updateStatusLabel();
            }
        });

        setSaveButtonEnabled(false);

        final String[] datastoreNames = mutableDatastoreCatalog.getDatastoreNames();
        _checkBoxes = new ArrayList<>();
        for (int i = 0; i < datastoreNames.length; i++) {
            final String datastoreName = datastoreNames[i];
            if (originalDatastore == null || !originalDatastore.getName().equals(datastoreName)) {
                final DCCheckBox<Datastore> checkBox = new DCCheckBox<>(datastoreName, false);
                checkBox.setValue(mutableDatastoreCatalog.getDatastore(datastoreName));
                checkBox.setName(datastoreName);
                checkBox.setOpaque(false);
                checkBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
                checkBox.addListener(new DCCheckBox.Listener<Datastore>() {
                    @Override
                    public void onItemSelected(Datastore item, boolean selected) {
                        updateStatusLabel();
                    }
                });
                _checkBoxes.add(checkBox);
            }
        }

        if (originalDatastore != null) {
            _datastoreNameField.setText(originalDatastore.getName());
            _datastoreNameField.setEnabled(false);

            List<? extends Datastore> containedDatastores = originalDatastore.getDatastores();
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
            setSaveButtonEnabled(false);
        } else {
            if (nameFilledOut) {
                _statusLabel.setText("Composite datastore ready");
                _statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_VALID, IconUtils.ICON_SIZE_SMALL));
                setSaveButtonEnabled(true);
            } else {
                _statusLabel.setText("Please fill out a datastore name");
                _statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_ERROR, IconUtils.ICON_SIZE_SMALL));
                setSaveButtonEnabled(false);
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

        JScrollPane checkBoxScrollPane = WidgetUtils.scrolleable(checkBoxPanel);
        checkBoxScrollPane.setPreferredSize(new Dimension(checkBoxPanel.getWidth(), 300));
        checkBoxScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        checkBoxScrollPane.setBackground(_outerPanel.getBackground());
        WidgetUtils.addToGridBag(checkBoxScrollPane, formPanel, 0, 1, 2, 1);

        DCPanel buttonPanel = getButtonPanel();

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
    protected CompositeDatastore createDatastore() {
        final List<Datastore> datastores = new ArrayList<Datastore>();

        for (DCCheckBox<Datastore> checkBox : _checkBoxes) {
            if (checkBox.isSelected()) {
                String datastoreName = checkBox.getText();
                Datastore datastore = checkBox.getValue();
                if (datastore == null) {
                    throw new IllegalStateException("No such datastore: " + datastoreName);
                }
                datastores.add(datastore);
            }
        }

        final CompositeDatastore datastore = new CompositeDatastore(_datastoreNameField.getText(), datastores);
        return datastore;
    }

    @Override
    protected String getDatastoreIconPath() {
        return IconUtils.COMPOSITE_IMAGEPATH;
    }
}
