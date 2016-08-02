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
package org.datacleaner.panels;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.RemoteServerState;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCHtmlBox;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DataCloudStatusLabel;
import org.datacleaner.windows.AbstractWindow;
import org.datacleaner.windows.DataCloudLogInWindow;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Panel that shows information DataCloud status
 */
public class DataCloudInformationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Color _background = WidgetUtils.BG_COLOR_BRIGHTEST;
    private final Color _foreground = WidgetUtils.BG_COLOR_DARKEST;

    private final DCLabel text;
    private final JButton optionButton;
    final private DCHtmlBox htmlBoxDataCloud =
            new DCHtmlBox("More information on <a href=\"http://datacleaner.org\">datacleaner.org</a>");

    public DataCloudInformationPanel(RightInformationPanel rightPanel, final DataCleanerConfiguration configuration,
            final UserPreferences userPreferences, WindowContext windowContext, AbstractWindow owner) {
        super();
        optionButton = WidgetFactory.createDefaultButton("Sign in to DataCloud", IconUtils.MENU_OPTIONS);
        optionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                rightPanel.toggleWindow(DataCloudStatusLabel.PANEL_NAME);
                WidgetUtils.invokeSwingAction(new Runnable() {
                    @Override
                    public void run() {
                        if (DataCloudLogInWindow.isRelevantToShow(userPreferences, configuration)) {
                            final DataCloudLogInWindow dataCloudLogInWindow = new DataCloudLogInWindow(configuration,
                                    userPreferences, windowContext, owner);
                            dataCloudLogInWindow.open();
                        }
                    }
                });
            }
        });
        setLayout(new VerticalLayout(10));

        DCLabel header = DCLabel.darkMultiLine("DataCloud status");
        header.setFont(WidgetUtils.FONT_HEADER1);
        header.setIcon(ImageManager.get().getImageIcon("images/menu/datacloud.png"));
        add(header);

        text = DCLabel.darkMultiLine("");
        add(text);
        add(Box.createVerticalBox());
        add(optionButton);
        add(Box.createVerticalBox());
        add(htmlBoxDataCloud);
    }

    public void setInformationStatus(RemoteServerState remoteServerState) {
        optionButton.setVisible(false);
        text.setText("");
        String panelContent = "";
        if (remoteServerState.getActualState() == RemoteServerState.State.OK ||
                remoteServerState.getActualState() == RemoteServerState.State.NO_CREDIT) {
            panelContent = addLine(panelContent, "Connected as " + remoteServerState.getRealName());
            panelContent = addLine(panelContent, "(email: " + remoteServerState.getEmail() + ")");
            if (remoteServerState.getCredit() > 0) {
                panelContent =
                        addLine(panelContent,
                                "Your credit balance: " + String.format("%,d", remoteServerState.getCredit()));
            } else {
                panelContent = addLine(panelContent,
                        "Your credit: <font color=\"red\">" + String.format("%,d", remoteServerState.getCredit())
                                + " </font>");
            }

            if (!remoteServerState.isEmailConfirmed()) {
                panelContent = addLine(panelContent, "Email is not confirmed.");
            }
        }

        if (remoteServerState.getActualState() == RemoteServerState.State.NOT_CONNECTED) {
            panelContent = addLine(panelContent, "Datacloud is not configured.");
            panelContent = addLine(panelContent, "You can set your credentials here.");
            optionButton.setVisible(true);
        }

        if (remoteServerState.getActualState() == RemoteServerState.State.ERROR) {
            panelContent = addLine(panelContent, "Cannot connect as " + remoteServerState.getEmail());
            panelContent =
                    addLine(panelContent, "<font color=\"red\">" + remoteServerState.getErrorMessage() + " </font>");
        }
        text.setText(panelContent);
    }

    @Override
    public Color getBackground() {
        return _background;
    }

    @Override
    public Color getForeground() {
        return _foreground;
    }

    private String addLine(String text, String newLine) {
        return text + "<p>" + newLine + "</p>";
    }
}
