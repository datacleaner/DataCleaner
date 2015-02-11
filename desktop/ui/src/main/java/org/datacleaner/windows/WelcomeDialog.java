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
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.jdesktop.swingx.action.OpenBrowserAction;

/**
 * A welcome dialog for DataCleaner. Specifically used in embedded situations as
 * a "landing page" for new users to the application.
 */
public class WelcomeDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    public WelcomeDialog(AnalysisJobBuilderWindow window, Image welcomeImage) {
        super((Window) window, "Welcome to DataCleaner");

        final JLabel banner = new JLabel(new ImageIcon(welcomeImage));
        banner.setPreferredSize(new Dimension(welcomeImage.getWidth(this), welcomeImage.getHeight(this)));

        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        panel.setLayout(new BorderLayout());
        panel.add(banner, BorderLayout.CENTER);

        final JButton continueButton = WidgetFactory.createDefaultButton("Continue", IconUtils.ACTION_FORWARD);
        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WelcomeDialog.this.setVisible(false);
            }
        });

        final JButton websiteButton = WidgetFactory.createDefaultButton("Visit the DataCleaner website",
                IconUtils.WEBSITE);
        websiteButton.addActionListener(new OpenBrowserAction("http://datacleaner.org"));

        panel.add(DCPanel.flow(Alignment.CENTER, continueButton, websiteButton), BorderLayout.SOUTH);

        getContentPane().add(panel);
        pack();
        setResizable(false);
        WidgetUtils.centerOnScreen(this);
        setModal(true);
    }
}
