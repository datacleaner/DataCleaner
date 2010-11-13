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

import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.util.WindowManager;

public abstract class AbstractWindow extends JFrame implements WindowListener {

	private static final long serialVersionUID = 1L;
	private volatile boolean initialized = false;

	protected void initialize() {
		updateWindowTitle();
		setIconImage(getWindowIcon());
		addWindowListener(this);
		setResizable(isWindowResizable());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		JComponent content = getWindowContent();
		getContentPane().add(content);
		setPreferredSize(content.getPreferredSize());

		pack();

		if (isCentered()) {
			centerOnScreen();
		}

		setVisible(true);
		WindowManager.getInstance().onShow(this);
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

	protected abstract String getWindowTitle();

	protected abstract Image getWindowIcon();

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
