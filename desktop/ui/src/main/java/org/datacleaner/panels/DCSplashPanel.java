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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.windows.AnalysisJobBuilderWindow;
import org.datacleaner.windows.AnalysisJobBuilderWindow.AnalysisWindowPanelType;

/**
 * Panel super class for those panels that show up as a splash in the welcoming
 * section of the application.
 */
public class DCSplashPanel extends DCPanel {

    private static final long serialVersionUID = 1L;
    private static final Image BACKGROUND_IMAGE = ImageManager.get().getImage(
            "images/window/welcome-panel-background.jpg");

    private static final int WIDTH_CONTENT = 800;
    private static final int MARGIN_LEFT = 20;

    private final AnalysisJobBuilderWindow _window;

    public DCSplashPanel(final AnalysisJobBuilderWindow window) {
        super(BACKGROUND_IMAGE, 100, 100, WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _window = window;
    }

    public AnalysisJobBuilderWindow getWindow() {
        return _window;
    }

    /**
     * Creates a label for the title of the screen
     * 
     * @param string
     * @return
     */
    protected JComponent createTitleLabel(String text, boolean includeBackButton) {
        final DCLabel titleLabel = new DCLabel(false, text, WidgetUtils.BG_COLOR_BLUE_MEDIUM, null);
        titleLabel.setFont(WidgetUtils.FONT_BANNER);

        final EmptyBorder border = new EmptyBorder(20, MARGIN_LEFT, 10, 0);

        if (includeBackButton) {
            final DCPanel panel = DCPanel.flow(Alignment.LEFT, MARGIN_LEFT, 0, createBackToWelcomeScreenButton(),
                    titleLabel);
            panel.setBorder(border);
            return panel;
        } else {
            titleLabel.setBorder(border);
            return titleLabel;
        }
    }

    private JComponent createBackToWelcomeScreenButton() {
        final ImageIcon icon = ImageManager.get().getImageIcon(IconUtils.ACTION_BACK);
        final JButton backButton = WidgetFactory.createDefaultButton(null, icon);
        backButton.setOpaque(false);
        backButton.setBorder(null);
        backButton.setMargin(new Insets(0, 0, 0, 0));
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _window.changePanel(AnalysisWindowPanelType.WELCOME);
            }
        });

        return backButton;
    }

    /**
     * Wraps a content panel in a scroll pane and applies a maximum width to the
     * content to keep it nicely in place on the screen.
     * 
     * @param panel
     * @return
     */
    protected JScrollPane wrapContent(JComponent panel) {
        panel.setMaximumSize(new Dimension(WIDTH_CONTENT, Integer.MAX_VALUE));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        final DCPanel wrappingPanel = new DCPanel();
        final BoxLayout layout = new BoxLayout(wrappingPanel, BoxLayout.PAGE_AXIS);
        wrappingPanel.setLayout(layout);
        wrappingPanel.add(panel);
        wrappingPanel.setBorder(new EmptyBorder(0, MARGIN_LEFT, 0, 0));

        final JScrollPane scroll = WidgetUtils.scrolleable(wrappingPanel);
        return scroll;
    }

    @Override
    protected void paintPanelBackgroundImage(Graphics g, Image watermark, int imageWidth, int imageHeight,
            float horizontalAlignment, float verticalAlignment) {

        final int minimumImageWidth = 1050;
        final int panelWidth = getWidth();

        if (panelWidth >= imageWidth) {
            // there's plenty of room
            super.paintPanelBackgroundImage(g, watermark, imageWidth, imageHeight, horizontalAlignment,
                    verticalAlignment);
            return;
        }

        if (panelWidth < minimumImageWidth) {
            // paint it left-aligned
            final int paintedWidth = minimumImageWidth;
            final double factor = 1.0 * paintedWidth / imageWidth;
            final int paintedHeight = (int) (factor * imageHeight);

            super.paintPanelBackgroundImage(g, watermark, paintedWidth, paintedHeight, 0, verticalAlignment);
            return;
        }

        // scale the watermark but keep right-alignment
        final int paintedWidth = panelWidth;
        final double factor = 1.0 * paintedWidth / imageWidth;
        final int paintedHeight = (int) (factor * imageHeight);
        super.paintPanelBackgroundImage(g, watermark, paintedWidth, paintedHeight, horizontalAlignment,
                verticalAlignment);
    }
}
