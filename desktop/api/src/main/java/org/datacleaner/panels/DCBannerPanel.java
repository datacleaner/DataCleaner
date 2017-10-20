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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetScreenResolutionAdjuster;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;

/**
 * Renders a DataCleaner-banner as a panel
 */
public class DCBannerPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Image DEFAULT_LEFT_IMAGE = ImageManager.get().getImage("images/window/banner-logo.png");
    private static final Image DEFAULT_BG_IMAGE = ImageManager.get().getImage("images/window/banner-bg.png");
    private static final Image DEFAULT_RIGHT_IMAGE = ImageManager.get().getImage("images/window/banner-right.png");

    private static final WidgetScreenResolutionAdjuster adjuster = WidgetScreenResolutionAdjuster.get();
    private static final int DEFAULT_HEIGHT = adjuster.adjust(80);

    private final int _titleIndent;
    private final Image _leftImage;
    private final Image _rightImage;
    private final Image _bgImage;
    private final String _title1;
    private String _title2;

    public DCBannerPanel() {
        this((String) null);
    }

    public DCBannerPanel(final String title) {
        this(DEFAULT_LEFT_IMAGE, title);
    }

    public DCBannerPanel(final Image bannerImage) {
        this(bannerImage, null);
    }

    public DCBannerPanel(final Image bannerImage, final String title) {
        this(bannerImage, DEFAULT_BG_IMAGE, DEFAULT_RIGHT_IMAGE, title);
    }

    public DCBannerPanel(final Image leftImage, final Image bgImage, final Image rightImage, final String title) {
        _leftImage = adjuster.scale(leftImage);
        _bgImage = adjuster.scale(bgImage);
        _rightImage = adjuster.scale(rightImage);
        if (title == null) {
            _title1 = null;
            _title2 = null;
        } else {
            final int linebreak = title.indexOf('\n');
            if (linebreak == -1) {
                _title1 = title;
                _title2 = null;
            } else {
                _title1 = title.substring(0, linebreak);
                _title2 = title.substring(linebreak + 1);
            }
        }
        if (leftImage == null) {
            _titleIndent = 0;
        } else {
            _titleIndent = _leftImage.getWidth(this);
        }
        setOpaque(false);

        final FlowLayout layout = new FlowLayout(Alignment.RIGHT.getFlowLayoutAlignment(), 4, 38);
        layout.setAlignOnBaseline(true);
        setLayout(layout);
    }

    public String getTitle1() {
        return _title1;
    }

    public String getTitle2() {
        return _title2;
    }

    public void setTitle2(final String title2) {
        _title2 = title2;
    }

    @Override
    public int getHeight() {
        if (_bgImage != null) {
            return _bgImage.getHeight(this);
        }
        if (_leftImage != null) {
            return _leftImage.getHeight(this);
        }
        if (_rightImage != null) {
            return _rightImage.getHeight(this);
        }
        return DEFAULT_HEIGHT;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(adjuster.adjust(400), getHeight());
    }

    @Override
    public void paintComponent(final Graphics g) {
        final int x = getX();
        final int y = 0;
        final int w = getWidth();

        int offset = 0;
        while (offset < w) {
            g.drawImage(_bgImage, x + offset, y, this);
            offset += _bgImage.getWidth(this);
        }

        if (_leftImage != null) {
            final int imageHeight = _leftImage.getHeight(this);
            final int imageY = (getHeight() - imageHeight) / 2;
            g.drawImage(_leftImage, x + adjuster.adjust(5), imageY, this);
        }

        if (_rightImage != null) {
            final int rightImageWidth = _rightImage.getWidth(this);
            g.drawImage(_rightImage, x + w - rightImageWidth, y, this);
        }

        super.paintComponent(g);

        if (_title1 != null) {
            int titleY = adjuster.adjust(45);
            if (_title2 != null) {
                titleY = adjuster.adjust(33);
            }

            if (g instanceof Graphics2D) {
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }

            final int titleX = _titleIndent + adjuster.adjust(15);

            // draw title 1
            g.setFont(WidgetUtils.FONT_BANNER);
            g.setColor(WidgetUtils.BG_COLOR_DARK);
            g.drawString(_title1, titleX, titleY);

            if (_title2 != null) {
                titleY += adjuster.adjust(26);
                g.setFont(WidgetUtils.FONT_HEADER1);
                g.setColor(WidgetUtils.BG_COLOR_GREEN_MEDIUM);
                g.drawString(_title2, titleX, titleY);
            }
        }
    }
}
