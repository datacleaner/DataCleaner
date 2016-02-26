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
import java.awt.event.KeyEvent;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.RemoteServerData;
import org.datacleaner.descriptors.RemoteDescriptorProvider;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
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

    private final DataCleanerConfiguration _configuration;
    private final UserPreferences _userPreferences;
    private final JComponent _contentPanel;
    private JEditorPane invalidCredentialsLabel;
    private JXTextField usernameTextField;
    private JPasswordField passwordTextField;

    @Inject
    public DataCloudLogInWindow(final DataCleanerConfiguration configuration,
                                final UserPreferences userPreferences, WindowContext windowContext, AbstractWindow owner) {
        super(windowContext, ImageManager.get().getImage("images/window/banner-logo.png"), owner);
        _configuration = configuration;
        _userPreferences = userPreferences;
        _contentPanel = createContentPanel();
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "EnterAction");
        getRootPane().getActionMap().put("EnterAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                signIn();
            }
        });

    }

    public static boolean isRelevantToShow(UserPreferences userPreferences, DataCleanerConfiguration configuration) {
        final RemoteServerData datacloudConfig = configuration.getEnvironment().getRemoteServerConfiguration()
                .getServerConfig(RemoteDescriptorProvider.DATACLOUD_SERVER_NAME);
        String showDataCloudDialog = userPreferences.getAdditionalProperties()
                .getOrDefault(SHOW_DATACLOUD_DIALOG_USER_PREFERENCE, "true");
        Boolean showDataCloudDialogBool = Boolean.parseBoolean(showDataCloudDialog);
        return datacloudConfig == null && showDataCloudDialogBool;
    }

    private JComponent createContentPanel() {

        // 1. Create components
        final JEditorPane informationText = createDataCloudInformationText();
        // Set initially two lines of empty text for preferred size enough for 2-lines error message.
        invalidCredentialsLabel = new DCHtmlBox("&nbsp;<br>&nbsp;");
        invalidCredentialsLabel.setSize(500 - 30, Integer.MAX_VALUE);
        invalidCredentialsLabel.setOpaque(false);
        final JXLabel usernameLabel = new JXLabel("Name:");
        final JXLabel passwordLabel = new JXLabel("Password:");
        usernameTextField = WidgetFactory.createTextField("username");
        usernameTextField.setName("username");
        passwordTextField = WidgetFactory.createPasswordField();
        passwordTextField.setName("password");
        final JButton signInButton = WidgetFactory.createDefaultButton("Sign in", IconUtils.APPLICATION_ICON);
        final JCheckBox dontShowAgainCheckBox = new JCheckBox("Don't show again.", false);
        dontShowAgainCheckBox.setOpaque(false);
        final DCPanel result = new DCPanel();
        result.setOpaque(true);

        // 2. Layout
        GroupLayout layout = new GroupLayout(result);
        result.setLayout(layout);
        result.setBorder(new EmptyBorder(15, 15, 15, 15));

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(informationText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(usernameTextField, GroupLayout.PREFERRED_SIZE, usernameTextField.getPreferredSize().height + 5, usernameTextField.getPreferredSize().height + 5)
                                .addComponent(usernameLabel)
                        )
                        .addGap(3)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(passwordTextField, GroupLayout.PREFERRED_SIZE, passwordTextField.getPreferredSize().height + 5, passwordTextField.getPreferredSize().height + 5)
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
                        .addGap(0, 0, Integer.MAX_VALUE)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(usernameLabel)
                                .addComponent(passwordLabel)
                        )
                        .addGap(5)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(usernameTextField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, 250)
                                .addComponent(passwordTextField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, 250)
                        )
                        .addGap(0, 0, Integer.MAX_VALUE)
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
        signInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                signIn();
            }
        });
        ClearErrorLabelDocumentListener clearErrorListener = new ClearErrorLabelDocumentListener();
        usernameTextField.getDocument().addDocumentListener(clearErrorListener);
        passwordTextField.getDocument().addDocumentListener(clearErrorListener);

        return result;
    }

    class ClearErrorLabelDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            invalidCredentialsLabel.setText("");
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            invalidCredentialsLabel.setText("");
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            invalidCredentialsLabel.setText("");
        }
    }

    @Override
    protected String getBannerTitle() {
        return getWindowTitle();
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
        return false;
    }

    @Override
    public String getWindowTitle() {
        return "Sign in to DataCloud";
    }

    @Override
    public Image getWindowIcon() {
        return ImageManager.get().getImageFromCache(IconUtils.APPLICATION_ICON);
    }

    private JEditorPane createDataCloudInformationText() {
        final DCHtmlBox editorPane = new DCHtmlBox("");
        editorPane.setSize(500 - 30, Integer.MAX_VALUE);
        editorPane.setText(
                "<html>HI! Thank you for using DataCleaner." +
                        " Are you aware that there are many cool features available online?" +
                        " Just register on our <a href=\"http://datacleaner.org\">website</a> and you can immediately use" +
                        " them to improve your data quality." +
                        " (You will get free credits to spend.)" +
                        "<p>Enter your credentials to:" +
                        "<ul style=\"list-style-type:none\">" +
                        "   <li>\u2022 Validate your contacts addresses/emails." +
                        "   <li>\u2022 Parse names, phones, emails." +
                        "   <li>\u2022 Ensure you have recent addresses of your contacts." +
                        "</ul>" +
                        "<b>Your <a href=\"http://datacleaner.org\">datacleaner.org</a> credentials:</b>");
        editorPane.setOpaque(false);
        //editorPane.setFont(WidgetUtils.FONT_HEADER2);
        return editorPane;
    }

    private void signIn() {

        invalidCredentialsLabel.setText("Verifying credentials...");
        invalidCredentialsLabel.setForeground(null);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String userName = usernameTextField.getText();
                String pass = new String(passwordTextField.getPassword());
                try {
                    RemoteServersUtils.checkServerWithCredentials(RemoteDescriptorProvider.DATACLOUD_URL, userName, pass);
                } catch (Exception ex) {
                    invalidCredentialsLabel.setForeground(new Color(170, 10, 10));
                    invalidCredentialsLabel.setText("Sign in to DataCloud failed: " + ex.getMessage());
                    logger.warn("Sign in to DataCloud failed for user '{}'", userName, ex);
                    return;
                }

                invalidCredentialsLabel.setText("");
                logger.debug("Sign in to DataCloud succeeded. User name: {}", userName);

                RemoteServersUtils.addRemoteServer(_configuration.getEnvironment(), RemoteDescriptorProvider.DATACLOUD_SERVER_NAME, RemoteDescriptorProvider.DATACLOUD_URL, userName, pass);

                // close dialog
                close();
            }
        });
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

    public void open() {
        super.open();
        usernameTextField.requestFocusInWindow();
    }
}
