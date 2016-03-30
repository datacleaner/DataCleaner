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
package org.datacleaner.widgets.options;

import static org.datacleaner.descriptors.RemoteDescriptorProvider.DATACLOUD_SERVER_NAME;
import static org.datacleaner.descriptors.RemoteDescriptorProvider.DATACLOUD_URL;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.RemoteServerConfiguration;
import org.datacleaner.configuration.RemoteServerData;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.RemoteServersUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCHtmlBox;
import org.datacleaner.windows.OptionsDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * The "Remote components" panel found in the {@link OptionsDialog}
 */
public class DataCloudOptionsPanel extends DCPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(DataCloudOptionsPanel.class);

    private final DataCleanerConfiguration _configuration;
    private final JTextField emailAddressTextField;
    private final JPasswordField passwordTextField;
    private final JButton applyButton;
    private final JEditorPane invalidCredentialsLabel;

    private final int wholeLineSpan = 4;
    private final int rowSpan = 1;
    private final int padding = 5;
    private final int weightx = 1;
    private final int weighty = 0;
    private int row = 0;

    public DataCloudOptionsPanel(DataCleanerConfiguration configuration) {
        super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _configuration = configuration;
        final DCDocumentListener documentListener = new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                applyButton.setEnabled(true);
            }
        };

        applyButton = WidgetFactory.createDefaultButton("Apply");
        applyButton.setEnabled(false);
        applyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final boolean isOk = updateDcConfiguration();
                applyButton.setEnabled(!isOk);
            }
        });

        emailAddressTextField = WidgetFactory.createTextField("Email address");
        emailAddressTextField.setName("email address");
        emailAddressTextField.getDocument().addDocumentListener(documentListener);

        passwordTextField = WidgetFactory.createPasswordField();
        passwordTextField.setName("password");
        passwordTextField.getDocument().addDocumentListener(documentListener);

        invalidCredentialsLabel = new DCHtmlBox("&nbsp;");
        invalidCredentialsLabel.setSize(500-30, Integer.MAX_VALUE);
        invalidCredentialsLabel.setOpaque(false);

        setTitledBorder("Credentials");
        setupFields();
        addAllFields();

    }

    private void setupFields() {
        final RemoteServerData serverData =
                _configuration.getEnvironment().getRemoteServerConfiguration().getServerConfig(DATACLOUD_SERVER_NAME);
        if (serverData != null) {
            emailAddressTextField.setText(Strings.nullToEmpty(serverData.getUsername()));
            passwordTextField.setText(Strings.nullToEmpty(serverData.getPassword()));
        }
    }

    private void addAllFields() {
        WidgetUtils.addToGridBag(getDescriptionComponent(), this, 0, row, wholeLineSpan, rowSpan,
                GridBagConstraints.LINE_START, padding, weightx, weighty, GridBagConstraints.BOTH);

        addField("Email address", emailAddressTextField);
        addField("Password", passwordTextField, applyButton);

        row++;
        WidgetUtils.addToGridBag(invalidCredentialsLabel, this, 0, row, 3, 1, GridBagConstraints.LINE_START, padding,
                0, weighty, GridBagConstraints.HORIZONTAL);
    }

    private void addField(String labelText, JComponent... fields) {
        row++;

        WidgetUtils.addToGridBag(new JLabel(labelText), this, 0, row, 1, 1, GridBagConstraints.LINE_START, padding,
                0, weighty, GridBagConstraints.HORIZONTAL);
        for (int i = 0; i < fields.length; i++) {
            WidgetUtils.addToGridBag(fields[i], this, 1 + i, row, 1, 1, GridBagConstraints.LINE_START, padding, weightx,
                    weighty, GridBagConstraints.HORIZONTAL);
        }
    }

    private Component getDescriptionComponent() {
        final DCHtmlBox htmlBox = new DCHtmlBox("When registered at "
                + "<a href=\"https://datacleaner.org\">datacleaner.org</a> you can get access to DataCloud. <br><br>"
                + "DataCloud is an online service platform providing new functions to DataCleaner users and more. Sign in to the service using the form below.");

        return htmlBox;
    }

    /**
     * Update configuration
     *
     * @return True - everything is ok. False - problem, do not nothing.
     */
    private boolean updateDcConfiguration() {
        final String username = emailAddressTextField.getText();
        final String password = new String(passwordTextField.getPassword());
        try {
            RemoteServersUtils.checkServerWithCredentials(DATACLOUD_URL, username, password);
        } catch (Exception ex) {
            invalidCredentialsLabel.setForeground(WidgetUtils.ADDITIONAL_COLOR_RED_BRIGHT);
            invalidCredentialsLabel.setText("Sign in to DataCloud failed: " + ex.getMessage());
            logger.warn("Sign in to DataCloud failed for user '{}'", username, ex);
            return false;
        }

        invalidCredentialsLabel.setForeground(WidgetUtils.BG_COLOR_GREEN_MEDIUM);
        invalidCredentialsLabel.setText("Sign in to DataCloud succeeded.");
        logger.debug("Sign in to DataCloud succeeded. User name: {}", username);
        final RemoteServerConfiguration remoteServerConfig = _configuration.getEnvironment().getRemoteServerConfiguration();
        final RemoteServerData existingServerData = remoteServerConfig.getServerConfig(DATACLOUD_SERVER_NAME);
        if (existingServerData == null) {
            RemoteServersUtils.addRemoteServer(_configuration.getEnvironment(), DATACLOUD_SERVER_NAME, null, username, password);
        } else {
            RemoteServersUtils.updateRemoteServerCredentials(_configuration.getEnvironment(), DATACLOUD_SERVER_NAME, username, password);
        }
        return true;
    }
}
