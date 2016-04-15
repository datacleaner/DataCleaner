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

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.datacleaner.actions.MoveComponentTimerActionListener;
import org.datacleaner.configuration.RemoteServerState;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCHtmlBox;
import org.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Panel that shows information DataCloud status
 */
public class DataCloudInformationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int WIDTH = 360;
    private static final int POSITION_Y = 130;

    private final DCGlassPane _glassPane;
    private final Color _background = WidgetUtils.BG_COLOR_BRIGHTEST;
    private final Color _foreground = WidgetUtils.BG_COLOR_DARKEST;
    private final Color _borderColor = WidgetUtils.BG_COLOR_MEDIUM;

    private DCLabel text;
    final DCHtmlBox htmlBoxDataCloud =
            new DCHtmlBox("More information on <a href=\"http://datacleaner.org\">datacleaner.org</a>");

    public DataCloudInformationPanel(DCGlassPane glassPane) {
        super();
        _glassPane = glassPane;
        setBorder(new CompoundBorder(new LineBorder(_borderColor, 1), new EmptyBorder(20, 20, 20, 30)));
        setVisible(false);
        setSize(WIDTH, 400);
        setLocation(getXWhenOut(), POSITION_Y);

        setLayout(new VerticalLayout(10));

        DCLabel header = DCLabel.darkMultiLine("DataCloud status");
        header.setFont(WidgetUtils.FONT_HEADER1);
        header.setIcon(ImageManager.get().getImageIcon("images/menu/datacloud.png"));
        add(header);

        text = DCLabel.darkMultiLine("");
        add(text);
        add(Box.createVerticalBox());
        add(htmlBoxDataCloud);
    }

    private int getXWhenOut() {
        return _glassPane.getSize().width + WIDTH + 10;
    }

    private int getXWhenIn() {
        return _glassPane.getSize().width - WIDTH + 10;
    }

    public void setInformationStatus(RemoteServerState remoteServerState) {
        text.setText("");
        String panelContent = "";
        if (remoteServerState.getActualState() == RemoteServerState.State.OK ||
                remoteServerState.getActualState() == RemoteServerState.State.NO_CREDIT) {
            panelContent = addLine(panelContent, "Connected as " + remoteServerState.getRealName());
            panelContent = addLine(panelContent, "(email: " + remoteServerState.getEmail() + ")");
            if (remoteServerState.getCredit() > 0) {
                panelContent =
                        addLine(panelContent, "Your credit balance: " + creditFormatter(remoteServerState.getCredit()));
            } else {
                panelContent = addLine(panelContent,
                        "Your credit: <font color=\"red\">" + creditFormatter(remoteServerState.getCredit())
                                + " </font>");
            }

            if (!remoteServerState.isEmailConfirmed()) {
                panelContent = addLine(panelContent, "Email is not confirmed.");
            }
        }

        if (remoteServerState.getActualState() == RemoteServerState.State.UNKNOWN) {
            panelContent = addLine(panelContent, "Datacloud is not configured.");
            panelContent = addLine(panelContent, "You can set your credentials in");
            panelContent = addLine(panelContent, "<b>Options dialog - DataCloud</b>");
        }

        if (remoteServerState.getActualState() == RemoteServerState.State.ERROR) {
            panelContent = addLine(panelContent, "Cannot connect as " + remoteServerState.getEmail());
            panelContent =
                    addLine(panelContent, "<font color=\"red\">" + remoteServerState.getErrorMessage() + " </font>");
        }
        text.setText(panelContent);
    }

    public void moveIn(int delay) {
        setLocation(getXWhenOut(), POSITION_Y);
        setVisible(true);
        _glassPane.add(this);
        final Timer timer = new Timer(10, new MoveComponentTimerActionListener(this, getXWhenIn(), POSITION_Y, 40) {
            @Override
            protected void done() {
            }
        });
        timer.setInitialDelay(delay);
        timer.start();
    }

    public void moveOut(int delay) {
        final Timer timer = new Timer(10, new MoveComponentTimerActionListener(this, getXWhenOut(), POSITION_Y, 40) {
            @Override
            protected void done() {
                DataCloudInformationPanel me = DataCloudInformationPanel.this;
                me.setVisible(false);
                _glassPane.remove(me);
            }
        });
        timer.setInitialDelay(delay);
        timer.start();
    }

    @Override
    public Color getBackground() {
        return _background;
    }

    @Override
    public Color getForeground() {
        return _foreground;
    }


    /**
     * For spaces in number. 121 100, 1 000, -12 120
     *
     * @param credit
     * @return
     */
    private String creditFormatter(Long credit) {
        if (credit == null) {
            return "";
        }
        String creditStr = credit.toString();
        String formattedCredit = "";

        for (int i = creditStr.length() - 1; i >= 0; i--) {
            int index = creditStr.length() - 1 - i;
            if (index != 0 && index % 3 == 0 && creditStr.charAt(i) != '-') {
                formattedCredit = " " + formattedCredit;
            }
            formattedCredit = creditStr.charAt(i) + formattedCredit;
        }
        return formattedCredit;
    }

    private String addLine(String text, String newLine) {
        return text + "<p>" + newLine + "</p>";
    }
}
