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

	private static final int BANNER_LOGO_WIDTH = 220;
	private static final int BANNER_BG_WIDTH = 150;
	private static final int BANNER_HEIGHT = 150;
	private static final Image BANNER_LOGO_IMAGE = ImageManager.getInstance().getImage("images/window/banner-logo.png");
	private static final Image BANNER_BG_IMAGE = ImageManager.getInstance().getImage("images/window/banner-bg.png");

	private final String _title;

	public DCBannerPanel() {
		this(null);
	}

	public DCBannerPanel(String title) {
		super();
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

		g.drawImage(BANNER_LOGO_IMAGE, x, y, this);

		int offset = BANNER_LOGO_WIDTH;
		while (offset < w) {
			g.drawImage(BANNER_BG_IMAGE, x + offset, y, this);
			offset += BANNER_BG_WIDTH;
		}

		super.paint(g);

		if (_title != null) {
			if (g instanceof Graphics2D) {
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}
			g.setFont(WidgetUtils.FONT_BANNER);
			g.setColor(WidgetUtils.BG_COLOR_LESS_BRIGHT);
			g.drawString(_title, 210, 60);
		}
	}
}
