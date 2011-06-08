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
package org.eobjects.datacleaner.widgets;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.eobjects.datacleaner.actions.LoginChangeListener;
import org.eobjects.datacleaner.panels.DCGlassPane;
import org.eobjects.datacleaner.panels.LoginPanel;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.windows.OptionsDialog;

/**
 * A widget that displays the login status (online or offline) and provides a
 * link to change it
 * 
 * @author Kasper Sørensen
 */
public class LoginStatusLabel extends JLabel implements LoginChangeListener {

	private static final long serialVersionUID = 1L;

	private static final ImageIcon ONLINE_ICON = ImageManager.getInstance().getImageIcon(
			"images/status/trafficlight-green.png");
	private static final ImageIcon OFFLINE_ICON = ImageManager.getInstance().getImageIcon(
			"images/status/trafficlight-red.png");

	private final UserPreferences _userPreferences;
	private final DCGlassPane _glassPane;
	private final LoginPanel _loginPanel;

	public LoginStatusLabel(DCGlassPane glassPane) {
		super();
		_glassPane = glassPane;
		_loginPanel = new LoginPanel(_glassPane);
		setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		_userPreferences = UserPreferences.getInstance();
		_userPreferences.addLoginChangeListener(this);
		final boolean loggedIn = _userPreferences.isLoggedIn();
		final String username = _userPreferences.getUsername();
		onLoginStateChanged(loggedIn, username);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onMouseClick();
			}
		});
	}

	private void onMouseClick() {
		if (_userPreferences.isLoggedIn()) {
			new OptionsDialog().setVisible(true);
		} else {
			if (_loginPanel.isVisible()) {
				_loginPanel.moveOut(0);
			} else {
				_loginPanel.moveIn(0);
			}
		}
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		_userPreferences.removeLoginChangeListener(this);
	}

	@Override
	public void onLoginStateChanged(boolean loggedIn, String username) {
		if (loggedIn) {
			setIcon(ONLINE_ICON);
			setText("Online: " + username);
			setToolTipText(null);
		} else {
			setIcon(OFFLINE_ICON);
			setText("Offline");
			setToolTipText("<html>Log in to gain access<br/>to online content through<br/>the RegexSwap and more.</html>");
		}
	}
}
