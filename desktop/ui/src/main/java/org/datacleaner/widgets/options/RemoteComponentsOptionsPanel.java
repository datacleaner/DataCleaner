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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import org.apache.metamodel.util.Resource;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.RemoteServerData;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.RemoteServersConfigRW;
import org.datacleaner.util.RemoteServersUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCHtmlBox;
import org.datacleaner.windows.OptionsDialog;

import com.google.common.base.Strings;

import static org.datacleaner.descriptors.RemoteDescriptorProvider.DATACLOUD_SERVER_NAME;

/**
 * The "Remote components" panel found in the {@link OptionsDialog}
 */
public class RemoteComponentsOptionsPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private final DataCleanerConfiguration _configuration;
    private final RemoteServersConfigRW _remoteServersConfigRW;
    private final RemoteServersUtils _remoteServersUtils;
    private final JTextField usernameTextField;
    private final JPasswordField passwordTextField;
    private final JButton applyButton;

    private final int wholeLineSpan = 4;
    private final int rowSpan = 1;
    private final int padding = 5;
    private final int weightx = 1;
    private final int weighty = 0;
    private int row = 0;

    public RemoteComponentsOptionsPanel(DataCleanerConfiguration configuration) {
        super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _configuration = configuration;
        _remoteServersConfigRW = new RemoteServersConfigRW(_configuration);
        _remoteServersUtils = new RemoteServersUtils(_configuration);
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
                updateDcConfiguration();
                applyButton.setEnabled(false);
            }
        });

        usernameTextField = WidgetFactory.createTextField("username");
        usernameTextField.setName("username");
        usernameTextField.getDocument().addDocumentListener(documentListener);

        passwordTextField = WidgetFactory.createPasswordField();
        passwordTextField.setName("password");
        passwordTextField.getDocument().addDocumentListener(documentListener);

        setTitledBorder("Credentials");
        setupFields();
        addAllFields();

    }

    private void setupFields() {
        final RemoteServerData serverData =
                _remoteServersUtils.getServerConfig(DATACLOUD_SERVER_NAME);
        if (serverData != null) {
            usernameTextField.setText(Strings.nullToEmpty(serverData.getUsername()));
            passwordTextField.setText(Strings.nullToEmpty(serverData.getPassword()));
        }
    }

    private void addAllFields() {
        WidgetUtils.addToGridBag(getDescriptionComponent(), this, 0, row, wholeLineSpan, rowSpan,
                GridBagConstraints.LINE_START, padding, weightx, weighty, GridBagConstraints.BOTH);

        addField("Username", usernameTextField);
        addField("Password", passwordTextField, applyButton);
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
        DCHtmlBox htmlBox = new DCHtmlBox("This dialog is for credentials setting of users registered at "
                + "<a href=\"http://datacleaner.org\">datacleaner.org</a>. <br><br>"
                + "Remote components are a cloud service providing new functions. "
                + "These remote components run at the server, consume provided input data and return the results. ");

        return htmlBox;
    }

    private Resource getDataCleanerConfigurationFileResource() {
        final RepositoryFile configurationFile = _configuration.getHomeFolder().toRepositoryFolder()
                .getFile(DataCleanerConfigurationImpl.DEFAULT_FILENAME);
        final Resource resource = configurationFile.toResource();
        return resource;
    }

    private void updateDcConfiguration() {
        final RemoteServerData existingServerData = _remoteServersUtils.getServerConfig(DATACLOUD_SERVER_NAME);
        final String username = usernameTextField.getText();
        final String password = new String(passwordTextField.getPassword());
        if(existingServerData == null){
            _remoteServersUtils.createRemoteServer(DATACLOUD_SERVER_NAME, null, username, password);
            _remoteServersConfigRW.writeCredentialsToConfig(DATACLOUD_SERVER_NAME, null, username, password);
        }else {
            _remoteServersUtils.updateRemoteServerCredentials(DATACLOUD_SERVER_NAME, username, password);
            _remoteServersConfigRW.updateCredentials(DATACLOUD_SERVER_NAME, username, password);
        }
    }
}
