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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;

import org.eobjects.datacleaner.bootstrap.DCWindowContext;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.MonitorConnection;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.user.UserPreferencesImpl;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.LookAndFeelManager;
import org.eobjects.datacleaner.util.NumberDocument;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCCheckBox;
import org.eobjects.datacleaner.widgets.DCCheckBox.Listener;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.DescriptionLabel;
import org.jdesktop.swingx.JXTextField;

import com.google.inject.Inject;

/**
 * Dialog for setting up the users connection to the DataCleaner DQ Monitor
 * webapp.
 */
public class MonitorConnectionDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private static final ImageManager imageManager = ImageManager.getInstance();

    private final UserPreferences _userPreferences;
    private final DCCheckBox<Void> _httpsCheckBox;
    private final JXTextField _hostnameTextField;
    private final JXTextField _portTextField;
    private final JXTextField _contextPathTextField;
    private final DCLabel _baseUrlLabel;

    @Inject
    public MonitorConnectionDialog(WindowContext windowContext, UserPreferences userPreferences) {
        super(windowContext, imageManager.getImage("images/window/banner-dq-monitor.png"));
        _userPreferences = userPreferences;

        final MonitorConnection monitorConnection = _userPreferences.getMonitorConnection();

        _baseUrlLabel = DCLabel.bright("");
        _baseUrlLabel.setForeground(WidgetUtils.BG_COLOR_LESS_BRIGHT);

        _httpsCheckBox = new DCCheckBox<Void>("Use HTTPS", false);
        if (monitorConnection != null && monitorConnection.isHttps()) {
            _httpsCheckBox.setSelected(true);
        }
        _httpsCheckBox.setBorderPainted(false);
        _httpsCheckBox.setOpaque(false);
        _httpsCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        _httpsCheckBox.addListener(new Listener<Void>() {
            @Override
            public void onItemSelected(Void item, boolean selected) {
                updateBaseUrlLabel();
            }
        });

        _hostnameTextField = WidgetFactory.createTextField("Hostname");
        if (monitorConnection != null && monitorConnection.getHostname() != null) {
            _hostnameTextField.setText(monitorConnection.getHostname());
        } else {
            _hostnameTextField.setText("localhost");
        }
        _hostnameTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                updateBaseUrlLabel();
            }
        });

        _portTextField = WidgetFactory.createTextField("Port");
        _portTextField.setDocument(new NumberDocument(false));
        if (monitorConnection != null) {
            _portTextField.setText(monitorConnection.getPort() + "");
        } else {
            _portTextField.setText("8080");
        }
        _portTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                updateBaseUrlLabel();
            }
        });

        _contextPathTextField = WidgetFactory.createTextField("Context path");
        if (monitorConnection != null) {
            _contextPathTextField.setText(monitorConnection.getContextPath());
        } else {
            _contextPathTextField.setText("DataCleaner-monitor");
        }
        _contextPathTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                updateBaseUrlLabel();
            }
        });

        updateBaseUrlLabel();
    }

    public MonitorConnection createMonitorConnection() {
        int port = 8080;
        try {
            port = Integer.parseInt(_portTextField.getText());
        } catch (NumberFormatException e) {
            // do nothing, fall back to 8080.
        }

        return new MonitorConnection(_hostnameTextField.getText(), port, _contextPathTextField.getText(),
                _httpsCheckBox.isSelected());
    }

    private void updateBaseUrlLabel() {
        final MonitorConnection monitorConnection = createMonitorConnection();
        _baseUrlLabel.setText("Base url: " + monitorConnection.getBaseUrl());
    }

    @Override
    public String getWindowTitle() {
        return "DataCleaner dq monitor connection";
    }

    @Override
    protected String getBannerTitle() {
        return "DataCleaner dq monitor\nSet up connection";
    }

    @Override
    protected int getDialogWidth() {
        return 400;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    protected JComponent getDialogContent() {
        final DCPanel formPanel = new DCPanel();

        WidgetUtils.addToGridBag(DCLabel.bright("Hostname:"), formPanel, 0, 0);
        WidgetUtils.addToGridBag(_hostnameTextField, formPanel, 1, 0);

        WidgetUtils.addToGridBag(DCLabel.bright("Port:"), formPanel, 0, 1);
        WidgetUtils.addToGridBag(_portTextField, formPanel, 1, 1);

        WidgetUtils.addToGridBag(DCLabel.bright("Context path:"), formPanel, 0, 2);
        WidgetUtils.addToGridBag(_contextPathTextField, formPanel, 1, 2);

        WidgetUtils.addToGridBag(_httpsCheckBox, formPanel, 1, 3);

        WidgetUtils.addToGridBag(_baseUrlLabel, formPanel, 0, 4, 2, 1);

        formPanel.setBorder(WidgetUtils.BORDER_EMPTY);

        final JButton testButton = WidgetFactory.createButton("Test connection", "images/actions/refresh.png");
        // TODO: Make ping available in monitor and invoke it from here.

        final JButton saveButton = WidgetFactory.createButton("Save connection", "images/actions/save.png");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final MonitorConnection monitorConnection = createMonitorConnection();
                _userPreferences.setMonitorConnection(monitorConnection);

                MonitorConnectionDialog.this.close();
            }
        });

        final DCPanel buttonPanel = new DCPanel();
        buttonPanel.setBorder(WidgetUtils.BORDER_EMPTY);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        buttonPanel.add(testButton);
        buttonPanel.add(saveButton);

        final DescriptionLabel descriptionLabel = new DescriptionLabel();
        descriptionLabel
                .setText("The DataCleaner dq monitor is a separate web application that is part of the DataCleaner eco-system. "
                        + "In this dialog you can configure your connection to it. "
                        + "With it you can create, share, monitor and govern current and historic data quality metrics. "
                        + "You can also set up alerts to react when certain metrics are out of their expected ranges.");

        final DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(descriptionLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        panel.setPreferredSize(400, 250);

        return panel;
    }

    public static void main(String[] args) {
        LookAndFeelManager.getInstance().init();
        UserPreferences userPreferences = new UserPreferencesImpl(null);
        WindowContext windowContext = new DCWindowContext(null, userPreferences, null);
        MonitorConnectionDialog dialog = new MonitorConnectionDialog(windowContext, userPreferences);

        dialog.open();
    }
}
