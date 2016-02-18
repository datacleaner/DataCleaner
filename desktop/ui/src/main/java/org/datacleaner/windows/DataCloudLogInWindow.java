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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.RemoteServerData;
import org.datacleaner.descriptors.RemoteDescriptorProvider;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.restclient.ComponentRESTClient;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.RemoteServersConfigUtils;
import org.datacleaner.util.RemoteServersUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.DCHtmlBox;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataCloudLogInWindow extends AbstractDialog {
    private static final Logger logger = LoggerFactory.getLogger(DataCloudLogInWindow.class);
    public static final String SHOW_DATACLOUD_DIALOG_USER_PREFERENCE = "show.datacloud.dialog";

    final private DataCleanerConfiguration _configuration;
    final private UserPreferences _userPreferences;
    final private JComponent _contentPanel;
    private JEditorPane invalidCredentialsLabel;

    @Inject
    public DataCloudLogInWindow(final DataCleanerConfiguration configuration,
                                final UserPreferences userPreferences, WindowContext windowContext, AbstractWindow owner) {
        super(windowContext, ImageManager.get().getImage("images/window/banner-logo.png"), owner);
        _configuration = configuration;
        _userPreferences = userPreferences;
        _contentPanel = createContentPanel();
    }

    public static boolean mayIShowIt(UserPreferences userPreferences, DataCleanerConfiguration configuration){
        final RemoteServerData datacloudConfig = new RemoteServersUtils(configuration)
                .getServerConfig(RemoteDescriptorProvider.DATACLOUD_SERVER_NAME);
        String showDataCloudDialog = userPreferences.getAdditionalProperties()
                .getOrDefault(SHOW_DATACLOUD_DIALOG_USER_PREFERENCE, "true");
        Boolean showDataCloudDialogBool = Boolean.parseBoolean(showDataCloudDialog);
        return datacloudConfig == null && showDataCloudDialogBool;
    }

    private JComponent createContentPanel() {

        // 1. Create components
        final JEditorPane informationText = createDataCloudInformationText();
        invalidCredentialsLabel = new DCHtmlBox("&nbsp;<br>&nbsp;");
        invalidCredentialsLabel.setSize(500, Integer.MAX_VALUE);
        invalidCredentialsLabel.setForeground(new Color(170,10,10));
        invalidCredentialsLabel.setOpaque(false);
        final JXLabel usernameLabel = new JXLabel("Name:");
        final JXLabel passwordLabel = new JXLabel("Password:");
        final JXTextField usernameTextField = WidgetFactory.createTextField("username");
        usernameTextField.setName("username");
        final JPasswordField passwordTextField = WidgetFactory.createPasswordField();
        passwordTextField.setName("password");
        final JButton signInButton = WidgetFactory.createDefaultButton("Sign in", IconUtils.APPLICATION_ICON);
        final JCheckBox dontShowAgainCheckBox = new JCheckBox("Don't show again.", false);
        dontShowAgainCheckBox.setOpaque(false);
        final DCPanel result = new DCPanel();
        result.setOpaque(true);

        // 2. Layout
        GroupLayout layout = new GroupLayout(result);
        result.setLayout(layout);
        result.setBorder(new EmptyBorder(15,15,15,15));

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addComponent(informationText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameTextField, GroupLayout.PREFERRED_SIZE, usernameTextField.getPreferredSize().height+5, usernameTextField.getPreferredSize().height+5)
                    .addComponent(usernameLabel)
                )
                .addGap(3)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordTextField, GroupLayout.PREFERRED_SIZE, passwordTextField.getPreferredSize().height+5, passwordTextField.getPreferredSize().height+5)
                    .addComponent(passwordLabel)
                )
                .addGap(5)
                .addComponent(invalidCredentialsLabel)
                .addGap(20, 20, Integer.MAX_VALUE)
                .addComponent(dontShowAgainCheckBox)
                .addGroup(layout.createParallelGroup()
                    .addComponent(signInButton)
                )
        );

        layout.setHorizontalGroup(layout.createParallelGroup()
            .addComponent(informationText)
            .addGroup(layout.createSequentialGroup()
                .addGap(20)
                .addGroup(layout.createParallelGroup()
                        .addComponent(usernameLabel)
                        .addComponent(passwordLabel)
                )
                .addGap(5)
                .addGroup(layout.createParallelGroup()
                    .addComponent(usernameTextField)
                    .addComponent(passwordTextField)
                )
                .addGap(20)
            )
            .addComponent(invalidCredentialsLabel)
            .addComponent(dontShowAgainCheckBox)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Integer.MAX_VALUE)
                .addComponent(signInButton)
                .addGap(0, 0, Integer.MAX_VALUE)
            )
        );

        // 3. Add listeners
        // TODO: don't remember on click the checkbox, but on dialog close.
        dontShowAgainCheckBox.addActionListener(new DisableShowDialog(dontShowAgainCheckBox));
        signInButton.addActionListener(new SingInDataCloudListener(usernameTextField, passwordTextField, signInButton));

        return result;
    }

    @Override
    protected String getBannerTitle() {
        return "Sign In / Register";
    }

    @Override
    protected int getDialogWidth() {
        return 500;
    }

    @Override
    protected JComponent getDialogContent() {
        return _contentPanel;
    }


    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    public String getWindowTitle() {
        return "DataCloud Sign In";
    }

    @Override
    public Image getWindowIcon() {
        return ImageManager.get().getImageFromCache(IconUtils.APPLICATION_ICON);
    }

    private JEditorPane createDataCloudInformationText(){
        final DCHtmlBox editorPane = new DCHtmlBox("");
        editorPane.setSize(500, Integer.MAX_VALUE);
        editorPane.setText(
                "<html>HI! Thank you for using DataCleaner." +
                " Are you aware that there are many cool features available online?" +
                " Just register on our <a href=\"http://datacleaner.org\">website</a> and you can immediately use" +
                " them to improve your data quality." +
                " (You will get free credits to spend.) Enter your credentials to:" +
                "<ul>" +
                "   <li>Validate your contacts addresses/emails." +
                "   <li>Parse names, phones, emails." +
                "   <li>Ensure you have recent addresses of your contacts." +
                "</ul>" +
                "<b>Your <a href=\"http://datacleaner.org\">datacleaner.org</a> credentials:</b>");
        editorPane.setEditable(false);
        editorPane.setOpaque(false);
        //editorPane.setFont(WidgetUtils.FONT_HEADER2);
        return editorPane;
    }

    private class SingInDataCloudListener implements ActionListener {
        final private JXTextField userNameField;
        final private JPasswordField passwordField;
        final private JButton button;

        private SingInDataCloudListener(final JXTextField userNameField, final JPasswordField passwordField,
                final JButton button) {
            this.userNameField = userNameField;
            this.passwordField = passwordField;
            this.button = button;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            String userName = userNameField.getText();
            String pass = new String(passwordField.getPassword());
            try {
                new ComponentRESTClient(RemoteDescriptorProvider.DATACLOUD_URL, userName, pass);
            } catch (Exception ex) {
                invalidCredentialsLabel.setText("Sign in to DataCloud failed: " + ex.getMessage());
                logger.warn("Sign in to DataCloud failed for user '{}'", userName, ex);
                return;
            }
            logger.debug("Sign in to DataCloud succeeded. User name: {}", userName);

            RemoteServersConfigUtils remoteServersConfigUtils = new RemoteServersConfigUtils(_configuration);
            RemoteServersUtils remoteServersUtils = new RemoteServersUtils(_configuration);
            remoteServersConfigUtils
                    .createCredentials(RemoteDescriptorProvider.DATACLOUD_SERVER_NAME, null, userName, pass);
            remoteServersUtils.createRemoteServer(RemoteDescriptorProvider.DATACLOUD_SERVER_NAME, RemoteDescriptorProvider.DATACLOUD_URL, userName, pass);
            button.setBackground(Color.GREEN);
        }
    }

    private class DisableShowDialog implements ActionListener {
        private final JCheckBox _jCheckBox;

        private DisableShowDialog(final JCheckBox jCheckBox) {
            _jCheckBox = jCheckBox;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            Boolean selectedNeg = !_jCheckBox.isSelected();
            _userPreferences.getAdditionalProperties().put(SHOW_DATACLOUD_DIALOG_USER_PREFERENCE, selectedNeg.toString());
            _userPreferences.save();
        }
    }
}
