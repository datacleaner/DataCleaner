/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.panels;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;

/**
 * Renders a DataCleaner-banner as a panel
 * 
 * @author Kasper Sørensen
 */
public class DCBannerPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final Image DEFAULT_LEFT_IMAGE = ImageManager.getInstance().getImage("images/window/banner-logo.png");
	private static final Image DEFAULT_BG_IMAGE = ImageManager.getInstance().getImage("images/window/banner-bg.png");

	private final int _titleIndent;
	private final Image _leftImage;
	private final Image _rightImage;
	private final Image _bgImage;
	private final String _title;

	public DCBannerPanel() {
		this((String) null);
	}

	public DCBannerPanel(String title) {
		this(DEFAULT_LEFT_IMAGE, title);
	}

	public DCBannerPanel(Image bannerImage) {
		this(bannerImage, null);
	}

	public DCBannerPanel(Image bannerImage, String title) {
		this(bannerImage, DEFAULT_BG_IMAGE, null, title);
	}

	public DCBannerPanel(Image leftImage, Image bgImage, Image rightImage, String title) {
		super();
		_leftImage = leftImage;
		_bgImage = bgImage;
		_rightImage = rightImage;
		_title = title;
		if (leftImage == null) {
			_titleIndent = 0;
		} else {
			_titleIndent = leftImage.getWidth(this);
		}
		setOpaque(false);
	}

	@Override
	public int getHeight() {
		return _bgImage.getHeight(this);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension dimension = new Dimension(400, getHeight());
		return dimension;
	}

	@Override
	public void paint(Graphics g) {
		final int x = getX();
		final int y = 0;
		final int w = getWidth();

		if (_leftImage != null) {
			g.drawImage(_leftImage, x, y, this);
		}

		int offset = _titleIndent;
		while (offset < w) {
			g.drawImage(_bgImage, x + offset, y, this);
			offset += _bgImage.getWidth(this);
		}

		if (_rightImage != null) {
			int rightImageWidth = _rightImage.getWidth(this);
			g.drawImage(_rightImage, x + w - rightImageWidth, y, this);
		}

		super.paint(g);

		if (_title != null) {
			int titleY = 45;
			final String[] titleLines = _title.split("\n");
			if (titleLines.length > 1) {
				titleY = 30;
			}

			if (g instanceof Graphics2D) {
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}

			final int titleX = _titleIndent + 10;

			for (int i = 0; i < titleLines.length; i++) {
				g.setFont(WidgetUtils.FONT_BANNER);
				g.setColor(WidgetUtils.BG_COLOR_BLUE_DARK);
				g.drawString(titleLines[i], titleX + 2, titleY + 2);
				g.setFont(WidgetUtils.FONT_BANNER);
				g.setColor(WidgetUtils.BG_COLOR_BLUE_BRIGHT);
				g.drawString(titleLines[i], titleX, titleY);
				titleY += 30;
			}
		}
	}
}
