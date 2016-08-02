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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.datacleaner.Version;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.RemoteServerData;
import org.datacleaner.descriptors.RemoteDescriptorProvider;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.restclient.RESTClient;
import org.datacleaner.restclient.RESTClientImpl;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.RemoteServersUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCHtmlBox;
import org.jdesktop.swingx.JXEditorPane;
import org.jdesktop.swingx.JXTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataCloudLogInWindow extends AbstractDialog {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(DataCloudLogInWindow.class);
    private static final int PADDING = WidgetUtils.BORDER_WIDE_WIDTH;
    
    public static final String SHOW_DATACLOUD_DIALOG_USER_PREFERENCE = "show.datacloud.dialog";
    private final static String LOGIN_CARD = "Card with login form";
    private final static String SUCCESS_CARD = "Card with information about successful login";
    private final static String TERMS_CARD = "Card with terms and conditions";

    private final DataCleanerConfiguration _configuration;
    private final UserPreferences _userPreferences;
    private final JComponent _contentPanel;
    private JPanel cards;
    private JEditorPane invalidCredentialsLabel;
    private JXTextField usernameTextField;
    private JPasswordField passwordTextField;
    private JCheckBox dontShowAgainCheckBox;
    private DCPanel loginCard;
    private DCPanel successCard;
    private DCPanel termsCard;
    private JXEditorPane tacArea = new JXEditorPane();
    private JButton acceptButton;

    Image bannerImage = ImageManager.get().getImage("images/datacloud_banner.png")
            .getScaledInstance(530, 303, Image.SCALE_SMOOTH);

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
                if(loginCard.isVisible()) {
                    signIn();
                } else if(termsCard.isVisible()) {
                    acceptTerms();
                } else if(successCard.isVisible()) {
                    close();
                }
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

        final DCPanel result = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        final GroupLayout layout = new GroupLayout(result);
        result.setLayout(layout);
        result.setBorder(new EmptyBorder(15, 15, 15, 15));

        invalidCredentialsLabel = new DCHtmlBox("");
        invalidCredentialsLabel.setBorder(WidgetUtils.BORDER_EMPTY);
        invalidCredentialsLabel.setOpaque(false);
        final JEditorPane resetPasswordText = new DCHtmlBox("Forgot your password? " +
                "<a href='https://datacleaner.org/reset_password'>Reset it here</a>.");

        CardLayout cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.setOpaque(true);
        final DCPanel bottomPanel = contentBottom();
        loginCard = contentLoginCard();
        successCard = contentSuccessCard();
        termsCard = contentTermsAndConditions();
        cards.add(loginCard, LOGIN_CARD);
        cards.add(successCard, SUCCESS_CARD);
        cards.add(termsCard, TERMS_CARD);
        cardLayout.show(cards, LOGIN_CARD);

        layout.setHorizontalGroup(
            layout.createParallelGroup()
                .addComponent(cards)
                .addComponent(bottomPanel)
                .addComponent(invalidCredentialsLabel)
                .addComponent(resetPasswordText)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addComponent(cards, 300, cards.getPreferredSize().height + 36, cards.getPreferredSize().height + 36)
                .addGap(PADDING)
                .addComponent(invalidCredentialsLabel)
//                .addGap(PADDING)
                .addComponent(resetPasswordText)
                .addGap(PADDING)
                .addComponent(bottomPanel, bottomPanel.getPreferredSize().height, bottomPanel.getPreferredSize().height, bottomPanel.getPreferredSize().height)
        );
        return result;
    }

    private DCPanel contentLoginCard() {

        // 1. Create components
        final JEditorPane informationText = createDataCloudInformationText();
        informationText.setBorder(WidgetUtils.BORDER_EMPTY);

        // Set initially two lines of empty text for preferred size enough for 2-lines error message.
        final JLabel usernameLabel = new JLabel();
        final JLabel passwordLabel = new JLabel();
        usernameTextField = WidgetFactory.createTextField("email address");
        usernameTextField.setName("email address");
        passwordTextField = WidgetFactory.createPasswordField();
        passwordTextField.setName("password");
        final JButton signInButton = WidgetFactory.createPrimaryButton("Sign in", IconUtils.ACTION_SAVE_BRIGHT);
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

        // 2. Layouts
        final DCPanel loginCard = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        loginCard.setOpaque(true);
        final GroupLayout loginLayout = new GroupLayout(loginCard);
        loginCard.setLayout(loginLayout);

        int textFieldHeight = signInButton.getPreferredSize().height;

        loginLayout.setVerticalGroup(
                loginLayout.createSequentialGroup()
                        .addComponent(banner, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(PADDING * 2)
                        .addComponent(informationText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(PADDING * 2)
                        .addGroup(loginLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(usernameTextField, GroupLayout.PREFERRED_SIZE, textFieldHeight, textFieldHeight)
                                .addComponent(usernameLabel, GroupLayout.DEFAULT_SIZE, textFieldHeight, textFieldHeight)
                                .addComponent(passwordTextField, GroupLayout.PREFERRED_SIZE, textFieldHeight, textFieldHeight)
                                .addComponent(passwordLabel, GroupLayout.DEFAULT_SIZE, textFieldHeight, textFieldHeight)
                                .addComponent(signInButton, GroupLayout.DEFAULT_SIZE, textFieldHeight, textFieldHeight)
                        )
                        .addGap(PADDING)
        );

        loginLayout.setHorizontalGroup(loginLayout.createParallelGroup()
                .addComponent(banner)
                .addComponent(informationText)
                .addGroup(loginLayout.createSequentialGroup()
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
        );

        // 3. Add listeners
        signInButton.addActionListener(e -> signIn());
        ClearErrorLabelDocumentListener clearErrorListener = new ClearErrorLabelDocumentListener();
        usernameTextField.getDocument().addDocumentListener(clearErrorListener);
        passwordTextField.getDocument().addDocumentListener(clearErrorListener);

        return loginCard;
    }

    private DCPanel contentTermsAndConditions() {

        final DCPanel p = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        final GroupLayout l = new GroupLayout(p);
        final JLabel description = new JLabel("Please review terms and conditions before using DataCloud services");
        acceptButton = WidgetFactory.createPrimaryButton("Accept", IconUtils.ACTION_SAVE_BRIGHT);
        tacArea = new JXEditorPane();
        final JScrollPane tacScroll = new JScrollPane(tacArea);
        tacScroll.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        tacScroll.setPreferredSize(new Dimension(100,200));
        tacScroll.getHorizontalScrollBar().setEnabled(false);
        tacScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        p.setLayout(l);
        p.setOpaque(true);

        l.setVerticalGroup(l.createSequentialGroup()
                .addGap(PADDING)
                .addComponent(description)
                .addGap(PADDING)
                .addComponent(tacScroll)
                .addGap(PADDING*2)
                .addComponent(acceptButton)
                .addGap(PADDING)
        );
        l.setHorizontalGroup(l.createParallelGroup()
                .addComponent(description, GroupLayout.Alignment.LEADING)
                .addComponent(tacScroll, GroupLayout.Alignment.CENTER)
                .addComponent(acceptButton, GroupLayout.Alignment.CENTER)
        );

        acceptButton.addActionListener(e -> {
            acceptTerms();
        });

        return p;
    }

    private DCPanel contentSuccessCard() {

        final DCPanel result = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        final BorderLayout borderLayout = new BorderLayout();
        result.setLayout(borderLayout);

        final JLabel banner = new JLabel(new ImageIcon(bannerImage));
        // Green color BG_COLOR_GREEN_MEDIUM = #7ABE44
        final JLabel successLoginText = new JLabel("<html><center>You have been <font color='#7ABE44'>successfully</font> logged on." +
                "<br><br>Thank you!</center></html>");
        successLoginText.setHorizontalAlignment(JLabel.CENTER);
        successLoginText.setFont(WidgetUtils.FONT_BANNER);

        result.add(banner, BorderLayout.NORTH);
        result.add(successLoginText, BorderLayout.CENTER);
        return result;
    }

    private DCPanel contentBottom() {

        final JButton closeButton = WidgetFactory.createDefaultButton("Close", IconUtils.ACTION_CLOSE_DARK);
        dontShowAgainCheckBox = new JCheckBox("Don't show again.", false);
        dontShowAgainCheckBox.setOpaque(false);
        final DCPanel result = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        final BorderLayout borderLayout = new BorderLayout();
        result.setLayout(borderLayout);

        result.add(dontShowAgainCheckBox, BorderLayout.WEST);
        result.add(closeButton, BorderLayout.EAST);

        closeButton.addActionListener(e -> {
            saveDontShowFlag();
            close();
        });

        return result;
    }

    class ClearErrorLabelDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            showError("");
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            showError("");
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            showError("");
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
        return true;
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
                        " If you're a registered user on <a href=\"https://datacleaner.org\">datacleaner.org</a> then you can immediately access" +
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
                try {
                    checkServer();
                } catch (Exception ex) {

                    String msg = ex.getMessage();
                    if(msg != null && msg.toLowerCase().contains("terms and conditions")) {
                        showError("");
                        try {
                            String tac = RemoteServersUtils.getDataCloudTermsAndConditions();
                            tacArea.setContentType("text/html");
                            tacArea.setText(tac);
                            tacArea.setCaretPosition(0);
                        } catch (IOException e) {
                            showError("Cannot download DataCloud Terms and Conditions from the website: " + e.toString() + ".<br/>Please, visit <a href=\"" + RemoteDescriptorProvider.DATACLOUD_TERMS_URL + "\">" + RemoteDescriptorProvider.DATACLOUD_TERMS_URL + "</a>");
                            tacArea.setText("Cannot download DataCloud Terms and Conditions from the website: " + e.toString() + "\n\nPlease, visit " + RemoteDescriptorProvider.DATACLOUD_TERMS_URL);
                            acceptButton.setEnabled(false);
                        }

                        CardLayout cardLayout = (CardLayout) cards.getLayout();
                        cardLayout.show(cards, TERMS_CARD);
                        return;
                    } else {
                        showError("Sign in to DataCloud failed: " + ex.getMessage());
                        logger.warn("Sign in to DataCloud failed for user '{}'", userName, ex);
                        return;
                    }
                }
                showError("");

                logger.debug("Sign in to DataCloud succeeded. User name: {}", userName);

                addRemoteServer();
            }
        });
    }

    private void showError(String s) {
        if(s == null || s.isEmpty()) {
            s = "";
        }
        invalidCredentialsLabel.setForeground(WidgetUtils.ADDITIONAL_COLOR_RED_BRIGHT);
        invalidCredentialsLabel.setText(s);
        invalidCredentialsLabel.invalidate();
        _contentPanel.invalidate();
        _contentPanel.validate();
        _contentPanel.revalidate();
        _contentPanel.repaint();
    }

    private void acceptTerms() {
        try {
            // call web service to accept T&C
            final RESTClientImpl client = new RESTClientImpl(usernameTextField.getText(), new String(passwordTextField.getPassword()),
                    Version.getVersion());
            client.getResponse(RESTClient.HttpMethod.POST, RemoteDescriptorProvider.DATACLOUD_TERMS_ACCEPT_URL, "");

            addRemoteServer();
            dontShowAgainCheckBox.setVisible(false);
        } catch(Exception e) {
            showError("Cannot send acceptance message to server: " + e.getMessage());
        }
    }

    protected void addRemoteServer() {
        RemoteServersUtils.addRemoteServer(
                _configuration.getEnvironment(),
                RemoteDescriptorProvider.DATACLOUD_SERVER_NAME,
                RemoteDescriptorProvider.DATACLOUD_URL,
                usernameTextField.getText(),
                new String(passwordTextField.getPassword()));
        CardLayout cardLayout = (CardLayout) cards.getLayout();
        cardLayout.show(cards, SUCCESS_CARD);
    }

    protected void checkServer() throws Exception {
        String userName = usernameTextField.getText();
        String pass = new String(passwordTextField.getPassword());
        RemoteServersUtils.checkServerWithCredentials(RemoteDescriptorProvider.DATACLOUD_URL, userName, pass);
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
        AtomicInteger i = new AtomicInteger();
        DataCloudLogInWindow w = new DataCloudLogInWindow(null, null, null, null) {
            protected void checkServer() {
                System.out.println("Check server");
                if(i.getAndIncrement() < 1) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    throw new RuntimeException("TEST ERROR WITH SOME LONGER (ALTHOUGH NOT SO LONG) TEXT. TRY AGAIN.");
                }
                throw new RuntimeException("DataCloud terms and conditions not accepted");
            }
            protected void addRemoteServer() {
                System.out.println("Add server");
            }
        };
        w.open();
    }
}
