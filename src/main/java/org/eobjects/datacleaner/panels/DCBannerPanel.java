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

	private static final int BANNER_BG_WIDTH = 150;
	private static final int BANNER_HEIGHT = 150;
	private static final Image BANNER_BG_IMAGE = ImageManager.getInstance().getImage("images/window/banner-bg.png");
	private final int _bannerImageWidth;
	private final Image _bannerImage;
	private final Image _betaTagImage;
	private final String _title;

	public DCBannerPanel() {
		this(null, null);
	}

	public DCBannerPanel(String title) {
		this(null, title);
	}

	public DCBannerPanel(Image bannerImage) {
		this(bannerImage, null);
	}

	public DCBannerPanel(Image bannerImage, String title) {
		super();
		if (bannerImage == null) {
			_bannerImage = ImageManager.getInstance().getImage("images/window/banner-logo.png");
		} else {
			_bannerImage = bannerImage;
		}
		_bannerImageWidth = _bannerImage.getWidth(null);
		_betaTagImage = ImageManager.getInstance().getImage("images/beta-tag.png");
		_title = title;
		setOpaque(false);
	}

	@Override
	public int getHeight() {
		return BANNER_HEIGHT;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension dimension = new Dimension(400, getHeight());
		return dimension;
	}

	@Override
	public void paint(Graphics g) {
		final int x = getX();
		final int y = getY();
		final int w = getWidth();

		g.drawImage(_bannerImage, x, y, this);

		int offset = _bannerImageWidth;
		while (offset < w) {
			g.drawImage(BANNER_BG_IMAGE, x + offset, y, this);
			offset += BANNER_BG_WIDTH;
		}

		super.paint(g);

		if (_title != null) {
			int titleY = 80;
			String[] titleLines = _title.split("\n");
			if (titleLines.length > 1) {
				titleY = 60;
			}

			if (g instanceof Graphics2D) {
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}
			g.setFont(WidgetUtils.FONT_BANNER);
			g.setColor(WidgetUtils.BG_COLOR_BLUE_BRIGHT);
			for (int i = 0; i < titleLines.length; i++) {
				g.drawString(titleLines[i], _bannerImageWidth, titleY);
				titleY += 30;
			}
		}

		// temporary beta tag
		g.drawImage(_betaTagImage, x + w - 59, y, this);
	}
}
