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

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
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
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCHtmlBox;
import org.jdesktop.swingx.JXTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataCloudLogInWindow extends AbstractDialog {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(DataCloudLogInWindow.class);
    private static final int PADDING = WidgetUtils.BORDER_WIDE_WIDTH;
    
    public static final String SHOW_DATACLOUD_DIALOG_USER_PREFERENCE = "show.datacloud.dialog";

    private final DataCleanerConfiguration _configuration;
    private final UserPreferences _userPreferences;
    private final JComponent _contentPanel;
    private JEditorPane invalidCredentialsLabel;
    private JXTextField usernameTextField;
    private JPasswordField passwordTextField;
    private JCheckBox dontShowAgainCheckBox;

    @Inject
    public DataCloudLogInWindow(final DataCleanerConfiguration configuration,
                                final UserPreferences userPreferences, WindowContext windowContext, AbstractWindow owner) {
        super(windowContext, null, owner);
        _configuration = configuration;
        _userPreferences = userPreferences;
        _contentPanel = createContentPanel();
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "EnterAction");
        getRootPane().getActionMap().put("EnterAction", new AbstractAction() {
            private static final long serialVersionUID = 1L;

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
        informationText.setBorder(WidgetUtils.BORDER_EMPTY);
        
        // Set initially two lines of empty text for preferred size enough for 2-lines error message.
        invalidCredentialsLabel = new DCHtmlBox("&nbsp;<br>&nbsp;");
        invalidCredentialsLabel.setBorder(WidgetUtils.BORDER_EMPTY);
        invalidCredentialsLabel.setOpaque(false);
        final JLabel usernameLabel = new JLabel();
        final JLabel passwordLabel = new JLabel();
        usernameTextField = WidgetFactory.createTextField("email address");
        usernameTextField.setName("email address");
        passwordTextField = WidgetFactory.createPasswordField();
        passwordTextField.setName("password");
        final JButton signInButton = WidgetFactory.createPrimaryButton("Sign in", IconUtils.ACTION_SAVE_BRIGHT);
        final JButton closeButton = WidgetFactory.createDefaultButton("Close", IconUtils.ACTION_CLOSE_DARK);
        dontShowAgainCheckBox = new JCheckBox("Don't show again.", false);
        dontShowAgainCheckBox.setOpaque(false);
        final DCPanel result = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        result.setOpaque(true);
        Image bannerImage = ImageManager.get().getImage("images/datacloud_banner.png")
                .getScaledInstance(530, 303, Image.SCALE_SMOOTH);
        final JLabel banner = new JLabel(new ImageIcon(bannerImage));
        final ImageIcon usernameIcon = ImageManager.get().getImageIcon(IconUtils.USERNAME_INPUT);
        final ImageIcon passwordIcon = ImageManager.get().getImageIcon(IconUtils.PASSWORD_INPUT);
        usernameLabel.setIcon(usernameIcon);
        usernameLabel.setVerticalTextPosition(JLabel.CENTER);
        usernameLabel.setHorizontalAlignment(JLabel.CENTER);
        usernameLabel.setBackground(new Color(225,225,225));
        usernameLabel.setOpaque(true);
        passwordLabel.setIcon(passwordIcon);
        passwordLabel.setVerticalTextPosition(JLabel.CENTER);
        passwordLabel.setHorizontalAlignment(JLabel.CENTER);
        passwordLabel.setBackground(new Color(225,225,225));
        passwordLabel.setOpaque(true);

        // 2. Layout
        final GroupLayout layout = new GroupLayout(result);
        result.setLayout(layout);
        result.setBorder(new EmptyBorder(15, 15, 15, 15));

        int textFieldHeight = signInButton.getPreferredSize().height;

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(banner, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(PADDING * 2)
                        .addComponent(informationText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(PADDING * 2)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(usernameTextField, GroupLayout.PREFERRED_SIZE, textFieldHeight, textFieldHeight)
                                .addComponent(usernameLabel, GroupLayout.DEFAULT_SIZE, textFieldHeight, textFieldHeight)
                                .addComponent(passwordTextField, GroupLayout.PREFERRED_SIZE, textFieldHeight, textFieldHeight)
                                .addComponent(passwordLabel, GroupLayout.DEFAULT_SIZE, textFieldHeight, textFieldHeight)
                                .addComponent(signInButton, GroupLayout.DEFAULT_SIZE, textFieldHeight, textFieldHeight)
                        )
                        .addGap(PADDING)
                        .addComponent(invalidCredentialsLabel)
                        .addGap(PADDING)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(dontShowAgainCheckBox)
                                .addComponent(closeButton)
                        )
                        .addGap(PADDING)
        );

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(banner)
                .addComponent(informationText)
                .addGroup(layout.createSequentialGroup()
                        .addGap(PADDING, PADDING, Integer.MAX_VALUE)
                        .addComponent(usernameLabel, textFieldHeight, textFieldHeight, textFieldHeight)
                        .addComponent(usernameTextField, GroupLayout.DEFAULT_SIZE, 150, 150)
                        .addGap(PADDING)
                        .addComponent(passwordLabel, textFieldHeight, textFieldHeight, textFieldHeight)
                        .addComponent(passwordTextField, GroupLayout.DEFAULT_SIZE, 150, 150)
                        .addGap(PADDING)
                        .addComponent(signInButton)
                        .addGap(PADDING, PADDING, Integer.MAX_VALUE)
                )
                .addComponent(invalidCredentialsLabel)
                .addGroup(layout.createSequentialGroup()
                        .addGap(PADDING)
                        .addComponent(dontShowAgainCheckBox)
                        .addGap(PADDING, PADDING, Integer.MAX_VALUE)
                        .addComponent(closeButton)
                        .addGap(PADDING)
                )
        );

        // 3. Add listeners
        signInButton.addActionListener(e -> signIn());
        closeButton.addActionListener(e -> {
            saveDontShowFlag();
            close();
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
        return 560;
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
        return ImageManager.get().getImage(IconUtils.MENU_DATACLOUD);
    }

    private JEditorPane createDataCloudInformationText() {
        final DCHtmlBox editorPane = new DCHtmlBox("");
        editorPane.setSize(getDialogWidth() - 30, Integer.MAX_VALUE);
        editorPane.setText(
                "<html>Thank you for using DataCleaner." +
                        " If you're a registered user on <a href=\"http://datacleaner.org\">datacleaner.org</a> then you can immediately access" +
                        " our cloud data services (there are free credits with your registration). DataCloud contains services such as:" +
                        "<ul style=\"list-style-type:none\">" +
                        "   <li>\u2022 Address correction using postal data from all over the world." +
                        "   <li>\u2022 Check the validity and standardize formatting of Email addresses and Phone numbers." +
                        "   <li>\u2022 Enrichment of contact information using mover's registries, deceased lists&nbsp;and more." +
                        "</ul>");
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

    private void saveDontShowFlag(){
        Boolean selectedNeg = !dontShowAgainCheckBox.isSelected();
        _userPreferences.getAdditionalProperties().put(SHOW_DATACLOUD_DIALOG_USER_PREFERENCE, selectedNeg.toString());
        _userPreferences.save();
    }

    public void open() {
        super.open();
        usernameTextField.requestFocusInWindow();
    }

    public static void main(String[] args) {
        DataCloudLogInWindow w = new DataCloudLogInWindow(null, null, null, null);
        w.open();
    }
}
