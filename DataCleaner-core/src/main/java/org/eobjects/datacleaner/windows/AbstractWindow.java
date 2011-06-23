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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.util.WidgetUtils;

public abstract class AbstractWindow extends JFrame implements DCWindow, WindowListener {

	private static final long serialVersionUID = 1L;
	private volatile boolean initialized = false;
	private final WindowContext _windowContext;

	public AbstractWindow(WindowContext windowContext) {
		_windowContext = windowContext;
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		getContentPane().setBackground(WidgetUtils.BG_COLOR_BRIGHT);
	}

	protected void initialize() {
		updateWindowTitle();
		setIconImage(getWindowIcon());
		setResizable(isWindowResizable());

		JComponent content = getWindowContent();
		getContentPane().add(content);

		autoSetSize(content);

		_windowContext.onShow(this);
	}

	public Dimension autoSetSize() {
		return autoSetSize(getContentPane().getComponent(0));
	}

	public Dimension autoSetSize(Component content) {

		Dimension preferredSize = content.getPreferredSize();

		Toolkit toolkit = Toolkit.getDefaultToolkit();

		int maxWidth = toolkit.getScreenSize().width - 30;
		int maxHeight = toolkit.getScreenSize().height - 30;
		preferredSize.width = Math.min(preferredSize.width, maxWidth);
		preferredSize.height = Math.min(preferredSize.height, maxHeight);

		Dimension currentSize = getContentPane().getSize();
		preferredSize.width = Math.max(preferredSize.width, currentSize.width);
		preferredSize.width = Math.max(preferredSize.height, currentSize.height);

		getContentPane().setPreferredSize(preferredSize);
		pack();

		if (isCentered()) {
			centerOnScreen();
		}

		return preferredSize;
	}

	protected abstract boolean isWindowResizable();

	protected abstract boolean isCentered();

	@Override
	public final void setVisible(boolean b) {
		if (b == false) {
			throw new UnsupportedOperationException("Window does not support hiding, consider using dispose()");
		}
		if (!initialized) {
			initialized = true;
			initialize();
		}
		super.setVisible(true);
		onWindowVisible();
	}

	protected void onWindowVisible() {
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

	protected abstract JComponent getWindowContent();

	public void centerOnScreen() {
		WidgetUtils.centerOnScreen(this);
	}

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
		_windowContext.onDispose(this);
		super.dispose();
	}

	@Override
	public WindowContext getWindowContext() {
		return _windowContext;
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
