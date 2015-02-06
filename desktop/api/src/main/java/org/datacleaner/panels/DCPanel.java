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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.datacleaner.widgets.Alignment;

public class DCPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Image _watermark;
    private final int _imageHeight;
    private final int _imageWidth;
    private final float _horizontalAlignment;
    private final float _verticalAlignment;
    private final Color _bottomColor;
    private final Color _topColor;

    public static DCPanel around(Component component) {
        DCPanel panel = new DCPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(component);
        return panel;
    }

    public static DCPanel flow(Component... components) {
        return flow(Alignment.LEFT, components);
    }

    public static DCPanel flow(Alignment alignment, Component... components) {
        return flow(alignment, 10, 2, components);
    }

    public static DCPanel flow(Alignment alignment, int hgap, int vgap, Component... components) {
        DCPanel panel = new DCPanel();
        // Instead of hgap we use horizontal struts. This is to avoid initial
        // and last gaps.
        panel.setLayout(new FlowLayout(alignment.getFlowLayoutAlignment(), 0, vgap));
        for (int i = 0; i < components.length; i++) {
            if (i != 0) {
                panel.add(Box.createHorizontalStrut(hgap));
            }
            panel.add(components[i]);
        }
        return panel;
    }

    public DCPanel() {
        this(null, 0, 0);
    }

    public DCPanel(Color bgColor) {
        this(null, 0, 0, bgColor, bgColor);
    }

    /**
     * 
     * @param topColor
     * @param bottomColor
     * 
     * @deprecated since version 4 we no longer encourage gradient background.
     *             Use {@link #DCPanel(Color)} instead.
     */
    @Deprecated
    public DCPanel(Color topColor, Color bottomColor) {
        this(null, 0, 0, topColor, bottomColor);
    }

    /**
     * 
     * @param watermark
     * @param horizontalAlignmentInPercent
     *            horizontal alignment of the watermark in percent where 0 is
     *            LEFT and 100 is RIGHT
     */
    public DCPanel(Image watermark, int horizontalAlignmentInPercent, int verticalAlignmentInPercent) {
        this(watermark, horizontalAlignmentInPercent, verticalAlignmentInPercent, null, null);
    }

    public DCPanel(Image watermark, int horizontalAlignmentInPercent, int verticalAlignmentInPercent, Color bgColor) {
        this(watermark, horizontalAlignmentInPercent, verticalAlignmentInPercent, bgColor, bgColor);
    }

    /**
     * 
     * @param watermark
     * @param horizontalAlignmentInPercent
     * @param verticalAlignmentInPercent
     * @param topColor
     * @param bottomColor
     * 
     * @deprecated since version 4 we no longer encourage gradient background.
     *             Use {@link #DCPanel(Image, int, int, Color)} instead.
     */
    @Deprecated
    public DCPanel(Image watermark, int horizontalAlignmentInPercent, int verticalAlignmentInPercent, Color topColor,
            Color bottomColor) {
        super();
        if (topColor == null || bottomColor == null) {
            setOpaque(false);
        } else if (topColor.getAlpha() < 255) {
            setOpaque(false);
        } else {
            setOpaque(true);
        }
        _topColor = topColor;
        _bottomColor = bottomColor;
        _watermark = watermark;
        _horizontalAlignment = horizontalAlignmentInPercent / 100f;
        _verticalAlignment = verticalAlignmentInPercent / 100f;
        if (watermark != null) {
            ImageIcon icon = new ImageIcon(watermark);
            _imageWidth = icon.getIconWidth();
            _imageHeight = icon.getIconHeight();
        } else {
            _imageWidth = -1;
            _imageHeight = -1;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (_topColor != null) {
            if (isOpaque()) {
                paintPanelBackgroundColor(g);
            } else if (_topColor.getAlpha() < 255) {
                paintPanelBackgroundColor(g);
            }
        } else {
            super.paintComponent(g);
        }

        if (_watermark != null) {
            int x = getWidth() - _imageWidth;
            x = (int) (x * _horizontalAlignment);

            int y = getHeight() - _imageHeight;
            y = (int) (y * _verticalAlignment);

            g.drawImage(_watermark, x, y, this);
        }
    }

    protected void paintPanelBackgroundColor(Graphics g) {
        Paint paint;
        if (_topColor == _bottomColor || _bottomColor == null) {
            paint = _topColor;
        } else {
            paint = new GradientPaint(0, 0, _topColor, 0, getHeight(), _bottomColor);
        }
        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(paint);
        } else {
            g.setColor(_topColor);
        }
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    public DCPanel setPreferredSize(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        return this;
    }

    public DCPanel setTitledBorder(String title) {
        setBorder(new TitledBorder(title));
        return this;
    }
}
