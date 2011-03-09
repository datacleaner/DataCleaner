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
package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;

import javax.swing.JComponent;

import org.eobjects.datacleaner.panels.DCBannerPanel;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;

public abstract class AbstractDialog extends AbstractWindow {

	private static final long serialVersionUID = 1L;

	private final Image _bannerImage;
	private volatile Color _topBackgroundColor = WidgetUtils.BG_COLOR_DARK;
	private volatile Color _bottomBackgroundColor = WidgetUtils.BG_COLOR_DARK;

	public AbstractDialog() {
		this(null);
	}

	public AbstractDialog(Image bannerImage) {
		_bannerImage = bannerImage;
	}
	
	protected void setTopBackgroundColor(Color topBackgroundColor) {
		_topBackgroundColor = topBackgroundColor;
	}
	
	protected void setBottomBackgroundColor(Color bottomBackgroundColor) {
		_bottomBackgroundColor = bottomBackgroundColor;
	}

	@Override
	protected boolean isWindowResizable() {
		return false;
	}

	@Override
	protected final boolean isCentered() {
		return true;
	}

	@Override
	public Image getWindowIcon() {
		return ImageManager.getInstance().getImage("images/window/app-icon.png");
	}

	@Override
	protected final JComponent getWindowContent() {
		DCPanel panel = new DCPanel(_topBackgroundColor, _bottomBackgroundColor);
		panel.setLayout(new BorderLayout());
		DCBannerPanel bannerPanel = new DCBannerPanel(_bannerImage, getBannerTitle());
		panel.add(bannerPanel, BorderLayout.NORTH);
		JComponent dialogContent = getDialogContent();
		panel.add(dialogContent, BorderLayout.CENTER);

		panel.setPreferredSize(getDialogWidth(), bannerPanel.getPreferredSize().height
				+ dialogContent.getPreferredSize().height);

		return panel;
	}

	protected abstract String getBannerTitle();

	protected abstract int getDialogWidth();

	protected abstract JComponent getDialogContent();
}
