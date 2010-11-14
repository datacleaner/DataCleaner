package org.eobjects.datacleaner.panels;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.actions.MoveComponentTimerActionListener;
import org.eobjects.datacleaner.user.AuthenticationService;
import org.eobjects.datacleaner.user.DCAuthenticationService;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.label.MultiLineLabel;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.action.OpenBrowserAction;

/**
 * Panel for logging in to datacleaners website
 * 
 * @author Kasper Sørensen
 */
public class LoginPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final UserPreferences userPreferences = UserPreferences.getInstance();
	private AuthenticationService _authenticationService;
	private final int _alpha = 220;
	private final Color _background = WidgetUtils.BG_COLOR_DARKEST;
	private final Color _foreground = WidgetUtils.BG_COLOR_BRIGHTEST;

	public LoginPanel() {
		this(new DCAuthenticationService());
	}

	public LoginPanel(AuthenticationService authenticationService) {
		super();
		_authenticationService = authenticationService;
		setOpaque(false);
		setBorder(new EmptyBorder(30, 30, 30, 30));

		updateContents();

		setSize(340, 330);
		setLocation(-340, 150);
	}

	public void moveIn() {
		final Timer timer = new Timer(10, new MoveComponentTimerActionListener(this, 10, 150, 40));
		timer.setInitialDelay(500);
		timer.start();
	}

	public void moveOut() {
		final Timer timer = new Timer(10, new MoveComponentTimerActionListener(this, -340, 150, 40));
		timer.setInitialDelay(2500);
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
		int margin = 0;
		int x = margin;
		int y = margin;
		int w = getWidth() - (margin * 2);
		int h = getHeight() - (margin * 2);
		int arc = 30;

		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Color bg = getBackground();
		Color bgWithAlpha = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), getAlpha());

		g2.setColor(bgWithAlpha);
		g2.fillRoundRect(x, y, w, h, arc, arc);

		g2.setStroke(new BasicStroke(3f));
		g2.setColor(getForeground());
		g2.drawRoundRect(x, y, w, h, arc, arc);

		g2.dispose();
	}

	private int getAlpha() {
		return _alpha;
	}

	private void updateContents() {
		removeAll();
		if (userPreferences.getUsername() != null) {
			JLabel loggedInLabel = new JLabel("Logged in as: " + userPreferences.getUsername());
			loggedInLabel.setForeground(getForeground());
			
			WidgetUtils.addToGridBag(loggedInLabel, this, 0, 0);

			JButton logoutButton = WidgetFactory.createButton("Log out", "images/actions/logout.png");
			logoutButton.setForeground(getForeground());
			logoutButton.setBackground(getBackground());
			logoutButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					userPreferences.setUsername(null);
					updateContents();
				}
			});
			WidgetUtils.addToGridBag(logoutButton, this, 0, 1);
		} else {
			final JXTextField usernameTextField = new JXTextField();
			usernameTextField.setColumns(15);
			final JPasswordField passwordTextField = new JPasswordField(15);

			final JButton loginButton = WidgetFactory.createButton("Login", "images/actions/login.png");
			loginButton.setBackground(getBackground());
			loginButton.setForeground(getForeground());
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
							userPreferences.setUsername(username);
							updateContents();
							moveOut();
						} else {
							JOptionPane.showMessageDialog(LoginPanel.this, "The entered username and password was incorrect.",
									"Invalid credentials", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});

			final JButton registerButton = WidgetFactory.createButton("Register", "images/actions/register.png");
			registerButton.setBackground(getBackground());
			registerButton.setForeground(getForeground());
			registerButton.addActionListener(new OpenBrowserAction("http://datacleaner.eobjects.org/?register"));

			int y = 0;
			final String loginInfo = "Thank you for using DataCleaner. We kindly ask you to identify yourself by "
					+ "providing us with your eobjects.org user credentials.\n\n"
					+ "If you are not registered yet, we hope that you will do so now, giving "
					+ "the DataCleaner development community a better sense of it's users and audience.\n\n"
					+ "By logging in, you also accept transmitting very simple usage statistics to the DataCleaner "
					+ "community, signaling which features you are using.\n";
			MultiLineLabel loginInfoLabel = new MultiLineLabel(loginInfo);
			loginInfoLabel.setForeground(getForeground());
			WidgetUtils.addToGridBag(loginInfoLabel, this, 0, y, 2, 1);

			y++;
			JLabel usernameLabel = new JLabel("Username:");
			usernameLabel.setForeground(getForeground());
			WidgetUtils.addToGridBag(usernameLabel, this, 0, y);
			WidgetUtils.addToGridBag(usernameTextField, this, 1, y);

			y++;
			JLabel passwordLabel = new JLabel("Password:");
			passwordLabel.setForeground(getForeground());
			WidgetUtils.addToGridBag(passwordLabel, this, 0, y);
			WidgetUtils.addToGridBag(passwordTextField, this, 1, y);

			y++;
			final DCPanel buttonPanel = new DCPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			buttonPanel.add(registerButton);
			buttonPanel.add(loginButton);
			WidgetUtils.addToGridBag(buttonPanel, this, 0, y, 2, 1);
		}
		updateUI();
	}
}
