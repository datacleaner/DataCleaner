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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.actions.LoginChangeListener;
import org.eobjects.datacleaner.actions.MoveComponentTimerActionListener;
import org.eobjects.datacleaner.user.AuthenticationService;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.HumanInferenceToolbarButton;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.action.OpenBrowserAction;

/**
 * Panel for logging in to datacleaners website
 * 
 * @author Kasper Sørensen
 */
public class LoginPanel extends JPanel implements LoginChangeListener {

	private static final long serialVersionUID = 1L;

	private static final int WIDTH = 360;
	private static final int POSITION_Y = 130;

	private final AuthenticationService _authenticationService;
	private final DCGlassPane _glassPane;
	private final int _alpha = 220;
	private final int _margin = 0;
	private final Color _background = WidgetUtils.BG_COLOR_DARKEST;
	private final Color _foreground = WidgetUtils.BG_COLOR_BRIGHTEST;
	private final Color _borderColor = WidgetUtils.BG_COLOR_MEDIUM;
	private final UserPreferences _userPreferences;

	public LoginPanel(AuthenticationService authenticationService, DCGlassPane glassPane, UserPreferences userPreferences) {
		super();
		_authenticationService = authenticationService;
		_glassPane = glassPane;
		_userPreferences = userPreferences;

		setOpaque(false);
		setBorder(new CompoundBorder(new LineBorder(_borderColor, 1), new EmptyBorder(20, 20, 20, 30)));
		setVisible(false);
		setSize(WIDTH, 400);
		setLocation(getXWhenOut(), POSITION_Y);
	}

	@Override
	public void addNotify() {
		super.addNotify();
		updateContents();
		_userPreferences.addLoginChangeListener(this);
	}

	public void removeNotify() {
		super.removeNotify();
		_userPreferences.removeLoginChangeListener(this);
	};

	private int getXWhenOut() {
		return _glassPane.getSize().width + WIDTH + 10;
	}

	private int getXWhenIn() {
		return _glassPane.getSize().width - WIDTH + 10;
	}

	public void moveIn(int delay) {
		setLocation(getXWhenOut(), POSITION_Y);
		setVisible(true);
		_glassPane.add(this);
		final Timer timer = new Timer(10, new MoveComponentTimerActionListener(this, getXWhenIn(), POSITION_Y, 40) {
			@Override
			protected void done() {
			}
		});
		timer.setInitialDelay(delay);
		timer.start();
	}

	public void moveOut(int delay) {
		final Timer timer = new Timer(10, new MoveComponentTimerActionListener(this, getXWhenOut(), POSITION_Y, 40) {
			@Override
			protected void done() {
				LoginPanel loginPanel = LoginPanel.this;
				loginPanel.setVisible(false);
				_glassPane.remove(loginPanel);
			}
		});
		timer.setInitialDelay(delay);
		timer.start();
	}

	@Override
	public Color getBackground() {
		return _background;
	}

	@Override
	public Color getForeground() {
		return _foreground;
	}

	// renders this panel as a translucent black panel with rounded border.
	@Override
	protected void paintComponent(Graphics g) {
		int x = _margin;
		int y = _margin;
		int w = getWidth() - (_margin * 2);
		int h = getHeight() - (_margin * 2);
		// int arc = 30;

		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Color bg = getBackground();
		Color bgWithAlpha = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), getAlpha());

		g2.setColor(bgWithAlpha);
		g2.fillRect(x, y, w, h);

		g2.dispose();
	}

	private int getAlpha() {
		return _alpha;
	}

	private void updateContents() {
		removeAll();
		if (_userPreferences.isLoggedIn()) {
			final JLabel loggedInLabel = new JLabel("Logged in as: " + _userPreferences.getUsername());
			loggedInLabel.setForeground(getForeground());

			WidgetUtils.addToGridBag(new JLabel(ImageManager.getInstance().getImageIcon("images/status/valid.png")), this,
					0, 0);
			WidgetUtils.addToGridBag(loggedInLabel, this, 0, 1);
		} else {
			final JXTextField usernameTextField = new JXTextField();
			usernameTextField.setColumns(15);
			final JPasswordField passwordTextField = new JPasswordField(15);

			final JButton registerButton = WidgetFactory.createButton("Register", "images/actions/website.png");
			registerButton.addActionListener(new OpenBrowserAction("http://datacleaner.eobjects.org/?register"));

			final JButton loginButton = WidgetFactory.createButton("Login", "images/actions/login.png");
			loginButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String username = usernameTextField.getText();
					char[] password = passwordTextField.getPassword();
					if (StringUtils.isNullOrEmpty(username) || password == null || password.length == 0) {
						JOptionPane.showMessageDialog(LoginPanel.this, "Please enter a username and a password.",
								"Invalid credentials", JOptionPane.ERROR_MESSAGE);
					} else {
						boolean authenticated = _authenticationService.auth(username, password);
						if (authenticated) {
							_userPreferences.setUsername(username);
							updateContents();
							moveOut(1000);
						} else {
							JOptionPane.showMessageDialog(LoginPanel.this,
									"The entered username and password was incorrect.", "Invalid credentials",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});

			final JButton cancelButton = WidgetFactory.createButton("Cancel", "images/actions/back.png");
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					moveOut(500);
				}
			});

			int y = 0;
			final String loginInfo = "Thank you for using DataCleaner. We kindly ask you to identify yourself by "
					+ "providing us with your eobjects.org user credentials.<br><br>"
					+ "If you are not registered yet, we hope that you will do so now, giving "
					+ "the DataCleaner development community a better sense of it's users and audience.<br><br>"
					+ "By logging in, you also accept transmitting very simple usage statistics to the DataCleaner "
					+ "community, signaling which features you are using.";
			final DCLabel loginInfoLabel = DCLabel.brightMultiLine(loginInfo);
			loginInfoLabel.setSize(300, 250);
			loginInfoLabel.setPreferredSize(new Dimension(300, 250));
			WidgetUtils.addToGridBag(loginInfoLabel, this, 0, y, 2, 1, GridBagConstraints.CENTER, 0, 1.0, 1.0);

			y++;
			WidgetUtils.addToGridBag(Box.createVerticalStrut(4), this, 0, y, 2, 1);

			y++;
			final JLabel usernameLabel = new JLabel("Username:");
			usernameLabel.setForeground(getForeground());
			WidgetUtils.addToGridBag(usernameLabel, this, 0, y);
			WidgetUtils.addToGridBag(usernameTextField, this, 1, y);

			y++;
			final JLabel passwordLabel = new JLabel("Password:");
			passwordLabel.setForeground(getForeground());
			WidgetUtils.addToGridBag(passwordLabel, this, 0, y);
			WidgetUtils.addToGridBag(passwordTextField, this, 1, y);

			y++;
			WidgetUtils.addToGridBag(Box.createVerticalStrut(10), this, 0, y, 2, 1);

			y++;
			final JToolBar buttonPanel = WidgetFactory.createToolBar();
			buttonPanel.add(registerButton);
			buttonPanel.add(Box.createHorizontalGlue());
			buttonPanel.add(loginButton);
			buttonPanel.add(Box.createHorizontalStrut(4));
			buttonPanel.add(cancelButton);
			WidgetUtils.addToGridBag(buttonPanel, this, 0, y, 2, 1);

			y++;
			WidgetUtils.addToGridBag(Box.createVerticalStrut(10), this, 0, y, 2, 1);

			y++;
			WidgetUtils.addToGridBag(new HumanInferenceToolbarButton(), this, 0, y, 2, 1);
		}
		updateUI();
	}

	@Override
	public void onLoginStateChanged(boolean loggedIn, String username) {
		updateContents();
	}
}
