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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.border.CompoundBorder;
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
import org.datacleaner.util.RemoteServersConfigRW;
import org.datacleaner.util.RemoteServersUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXEditorPane;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by k.houzvicka on 12. 2. 2016.
 */
public class DataCloudLogInWindow extends AbstractWindow{
    private static final Logger logger = LoggerFactory.getLogger(DataCloudLogInWindow.class);
    public static final String SHOW_DATACLOUD_DIALOG_USER_PREFERENCE = "show.datacloud.dialog";


    final private DataCleanerConfiguration _configuration;
    final private UserPreferences _userPreferences;
    final private JComponent _contentPanel;

    @Inject
    public DataCloudLogInWindow(final DataCleanerConfiguration configuration,
            final UserPreferences userPreferences, WindowContext windowContext) {
        super(windowContext);
        _configuration = configuration;
        _userPreferences = userPreferences;
        _contentPanel = createContentPanel();

        setLayout(new BorderLayout());
        add(_contentPanel, BorderLayout.CENTER);
    }

    public boolean mayIShowIt(){
        final RemoteServerData datacloudConfig = new RemoteServersUtils(_configuration)
                .getServerConfig(RemoteDescriptorProvider.DATACLOUD_SERVER_NAME);
        String showDataCloudDialog = _userPreferences.getAdditionalProperties()
                .getOrDefault(SHOW_DATACLOUD_DIALOG_USER_PREFERENCE, "true");
        Boolean showDataCloudDialogBool = Boolean.parseBoolean(showDataCloudDialog);
        return datacloudConfig == null && showDataCloudDialogBool;
    }


    private JComponent createContentPanel() {
        JXEditorPane dataCloudInformationText = createDataCloudInformationText();

        final JXTextField usernameTextField = WidgetFactory.createTextField("username");
        usernameTextField.setName("username");

        final JPasswordField passwordTextField = WidgetFactory.createPasswordField();
        passwordTextField.setName("password");

        final JButton signInButton = WidgetFactory.createDefaultButton("Sign in", IconUtils.APPLICATION_ICON);
        signInButton.addActionListener(new SingInDataCloudListener(usernameTextField, passwordTextField, signInButton));

        final JCheckBox disableShowCheckBox = new JCheckBox("Don't show again.", false);
        disableShowCheckBox.addActionListener(new DisableShowDialog(disableShowCheckBox));


        final DCPanel result = new DCPanel();

        final DCPanel innerPanel = new DCPanel();
        innerPanel.setLayout(new VerticalLayout());
        innerPanel.setBorder(new CompoundBorder(WidgetUtils.BORDER_LIST_ITEM_LEFT_ONLY, new EmptyBorder(0, 20,
                0, 0)));
        innerPanel.add(dataCloudInformationText);

        result.setLayout(new VerticalLayout());
        result.add(Box.createVerticalStrut(100));
        result.add(innerPanel);
        innerPanel.add(Box.createVerticalStrut(5));
        innerPanel.add(usernameTextField);
        innerPanel.add(Box.createVerticalStrut(5));
        innerPanel.add(passwordTextField);
        innerPanel.add(Box.createVerticalStrut(5));
        innerPanel.add(signInButton);
        innerPanel.add(Box.createVerticalStrut(5));
        innerPanel.add(disableShowCheckBox);
        return result;
    }

    @Override
    protected boolean isWindowResizable() {
        return false;
    }

    @Override
    protected boolean isCentered() {
        return true;
    }

    @Override
    protected JComponent getWindowContent() {
        DCPanel outerPanel = new DCPanel();
        outerPanel.setLayout(new BorderLayout());

        outerPanel.add(_contentPanel, BorderLayout.CENTER);

        outerPanel.setPreferredSize(new Dimension(500, 300));

        return outerPanel;
    }


    @Override
    public String getWindowTitle() {
        return "DataCloud";
    }

    @Override
    public Image getWindowIcon() {
        return ImageManager.get().getImageFromCache(IconUtils.APPLICATION_ICON);
    }


    private JXEditorPane createDataCloudInformationText(){
        final JXEditorPane editorPane = new JXEditorPane("text/html",
                "Text");
        editorPane.setEditable(false);
        editorPane.setOpaque(false);
        editorPane.setFont(WidgetUtils.FONT_HEADER2);
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
                logger.error("Sign in DataCloud failed. " + ex.getMessage());
                button.setBackground(Color.RED);
                return;
            }
            logger.debug("Sign in DataCloud. User name: {}", userName);

            RemoteServersConfigRW remoteServersConfigRW = new RemoteServersConfigRW(_configuration);
            RemoteServersUtils remoteServersUtils = new RemoteServersUtils(_configuration);
            remoteServersConfigRW
                    .writeCredentialsToConfig(RemoteDescriptorProvider.DATACLOUD_SERVER_NAME, null, userName, pass);
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
