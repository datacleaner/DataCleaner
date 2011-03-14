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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JComponent;
import javax.swing.JDialog;

import org.eobjects.datacleaner.panels.DCBannerPanel;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.util.WindowManager;

public abstract class AbstractDialog extends JDialog implements DCWindow, WindowListener {

	private static final long serialVersionUID = 1L;

	private volatile boolean initialized = false;
	private final Image _bannerImage;
	private volatile Color _topBackgroundColor = WidgetUtils.BG_COLOR_DARK;
	private volatile Color _bottomBackgroundColor = WidgetUtils.BG_COLOR_DARK;

	public AbstractDialog() {
		this(null);
	}

	public AbstractDialog(Image bannerImage) {
		super();

		// modal dialogs are turned off because they prevent use of default
		// uncaught exception handlers(!)
		setModal(false);
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		setResizable(isWindowResizable());
		_bannerImage = bannerImage;
	}

	protected void setTopBackgroundColor(Color topBackgroundColor) {
		_topBackgroundColor = topBackgroundColor;
	}

	protected void setBottomBackgroundColor(Color bottomBackgroundColor) {
		_bottomBackgroundColor = bottomBackgroundColor;
	}

	protected void updateWindowTitle() {
		String windowTitle = getWindowTitle();
		if (windowTitle == null) {
			windowTitle = "DataCleaner";
		} else {
			if (windowTitle.indexOf("DataCleaner") == -1) {
				windowTitle = windowTitle + " | DataCleaner";
			}
		}
		setTitle(windowTitle);
	}

	protected void initialize() {
		updateWindowTitle();
		setIconImage(getWindowIcon());
		setResizable(isWindowResizable());

		JComponent content = getWindowContent();
		getContentPane().add(content);

		getContentPane().setPreferredSize(content.getPreferredSize());

		pack();

		WidgetUtils.centerOnScreen(this);

		WindowManager.getInstance().onShow(this);
	}

	@Override
	public void setVisible(boolean b) {
		if (b == false) {
			throw new UnsupportedOperationException("Window does not support hiding, consider using dispose()");
		}
		if (!initialized) {
			initialized = true;
			initialize();
		}
		super.setVisible(true);
	}

	protected boolean isWindowResizable() {
		return false;
	}

	@Override
	public Image getWindowIcon() {
		return ImageManager.getInstance().getImage("images/window/app-icon.png");
	}

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

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public final void windowClosing(WindowEvent e) {
		boolean dispose = onWindowClosing();
		if (dispose) {
			dispose();
		}
	}

	@Override
	public void dispose() {
		WindowManager.getInstance().onDispose(this);
		super.dispose();
	}

	protected boolean onWindowClosing() {
		return true;
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}
}
