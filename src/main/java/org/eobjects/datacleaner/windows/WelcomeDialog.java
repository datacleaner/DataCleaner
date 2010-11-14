package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

import org.eobjects.datacleaner.panels.DCBannerPanel;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.panels.LoginPanel;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.action.OpenBrowserAction;

public class WelcomeDialog extends AbstractWindow {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();

	private final LoginPanel loginPanel = new LoginPanel();
	
	@Override
	protected JComponent getWindowContent() {
		final Container glass = (Container) getGlassPane();
		glass.setLayout(null);
		glass.setVisible(true);
		glass.add(loginPanel);

		final DCPanel mainPanel = new DCPanel(imageManager.getImage("images/window/app-icon-hires.png"), 100, 80,
				WidgetUtils.BG_COLOR_MEDIUM, WidgetUtils.BG_COLOR_DARK);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(new DCBannerPanel(imageManager.getImage("images/window/banner-welcome.png"), "Welcome"),
				BorderLayout.NORTH);

		final DCPanel toolBarPanel = new DCPanel(WidgetUtils.BG_COLOR_DARK, WidgetUtils.BG_COLOR_LESS_DARK);
		toolBarPanel.setLayout(new BorderLayout());
		toolBarPanel.add(getBottomToolBar(), BorderLayout.CENTER);

		final DCPanel outerPanel = new DCPanel();
		outerPanel.setLayout(new BorderLayout());
		outerPanel.add(mainPanel, BorderLayout.CENTER);
		outerPanel.add(toolBarPanel, BorderLayout.SOUTH);
		outerPanel.setPreferredSize(550, 550);

		loginPanel.moveIn();

		return outerPanel;
	}

	@Override
	protected boolean isCentered() {
		return true;
	}

	@Override
	protected boolean isWindowResizable() {
		return false;
	}

	@Override
	protected Image getWindowIcon() {
		return imageManager.getImage("images/actions/login.png");
	}

	@Override
	protected String getWindowTitle() {
		return "Welcome to DataCleaner";
	}

	private JToolBar getBottomToolBar() {
		final JButton datacleanerButton = WidgetFactory.createButton(null, "images/links/datacleaner.png");
		datacleanerButton.addActionListener(new OpenBrowserAction("http://datacleaner.eobjects.org"));
		datacleanerButton.setToolTipText("Visit the DataCleaner website");
		datacleanerButton.setMargin(new Insets(0, 0, 0, 0));

		final JButton bloggerButton = WidgetFactory.createButton(null, "images/links/blogger.png");
		bloggerButton.addActionListener(new OpenBrowserAction("http://kasper.eobjects.org"));
		bloggerButton.setToolTipText("Follow along at our blog");
		bloggerButton.setMargin(new Insets(0, 0, 0, 0));

		final JButton linkedInButton = WidgetFactory.createButton(null, "images/links/linkedin.png");
		linkedInButton.addActionListener(new OpenBrowserAction("http://www.linkedin.com/groups?gid=3352784"));
		linkedInButton.setToolTipText("Join the DataCleaner LinkedIn group");
		linkedInButton.setMargin(new Insets(0, 0, 0, 0));

		final JButton skipButton = WidgetFactory.createButton("Skip", "images/actions/skip.png");
		skipButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		skipButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WelcomeDialog.this.dispose();
			}
		});

		JToolBar toolBar = WidgetFactory.createToolBar();
		toolBar.setOpaque(false);
		toolBar.add(datacleanerButton);
		toolBar.add(bloggerButton);
		toolBar.add(linkedInButton);
		toolBar.add(new JSeparator(JSeparator.VERTICAL));
		toolBar.add(skipButton);

		return toolBar;
	}
}
