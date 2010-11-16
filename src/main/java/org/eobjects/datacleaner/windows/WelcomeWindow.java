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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.reference.Function;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.datacleaner.actions.OpenAnalysisJobActionListener;
import org.eobjects.datacleaner.panels.DCBannerPanel;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.panels.LoginPanel;
import org.eobjects.datacleaner.panels.LoginPanel.LoginState;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.label.MultiLineLabel;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.action.OpenBrowserAction;

/**
 * The window that welcomes the user when he/she first opens DataCleaner. The
 * window contains a login form and a list of recent jobs.
 * 
 * @author Kasper Sørensen
 * 
 */
public class WelcomeWindow extends AbstractWindow {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();
	private final UserPreferences userPreferences = UserPreferences.getInstance();
	private final AnalyzerBeansConfiguration _configuration;
	private final JCheckBox _showOnStartupCheckBox;

	private final ActionListener _skipActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			_showLoginPanel = false;
			_loginPanel.moveOut(0);
			updateDialogState(false);
		}
	};
	private final ActionListener _closeActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			userPreferences.setWelcomeDialogShownOnStartup(_showOnStartupCheckBox.isSelected());
			WelcomeWindow.this.dispose();
		}
	};
	private final LoginPanel _loginPanel = new LoginPanel();
	private final DCPanel _recentJobsPanel = new DCPanel();
	private final JButton _nextStepButton;

	private volatile boolean _showLoginPanel;

	public WelcomeWindow(AnalyzerBeansConfiguration configuration) {
		super();
		_configuration = configuration;
		_showOnStartupCheckBox = new JCheckBox("Show this dialog on startup");
		_showOnStartupCheckBox.setOpaque(false);
		_showOnStartupCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		_showOnStartupCheckBox.setVisible(false);
		_showOnStartupCheckBox.setSelected(userPreferences.isWelcomeDialogShownOnStartup());
		_nextStepButton = new JButton();
		_nextStepButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		updateDialogState(true);
	}

	/**
	 * Updates the state of the welcome dialog. There are two typical states: If
	 * the login form is showing or not.
	 * 
	 * @param checkLoginStatus
	 */
	private void updateDialogState(boolean checkLoginStatus) {
		if (checkLoginStatus) {
			if (_loginPanel.getLoginState() == LoginState.LOGGED_IN) {
				_showLoginPanel = false;
			} else {
				_showLoginPanel = true;
			}
		}
		if (_showLoginPanel) {
			_nextStepButton.setText("Skip");
			_nextStepButton.setIcon(imageManager.getImageIcon("images/actions/skip.png"));
			_nextStepButton.removeActionListener(_closeActionListener);
			_nextStepButton.addActionListener(_skipActionListener);
			_loginPanel.moveIn(500);
		} else {
			_nextStepButton.setText("Close");
			_nextStepButton.setIcon(imageManager.getImageIcon("images/actions/skip.png"));
			_nextStepButton.removeActionListener(_skipActionListener);
			_nextStepButton.addActionListener(_closeActionListener);
			_showOnStartupCheckBox.setVisible(true);
		}
		_recentJobsPanel.setVisible(!_showLoginPanel);
	}

	@Override
	protected JComponent getWindowContent() {
		final DCPanel mainPanel = new DCPanel(imageManager.getImage("images/window/app-icon-hires.png"), 100, 80,
				WidgetUtils.BG_COLOR_DARK, WidgetUtils.BG_COLOR_LESS_DARK);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(new DCBannerPanel(imageManager.getImage("images/window/banner-welcome.png"), "Welcome"),
				BorderLayout.NORTH);
		mainPanel.add(getRecentJobsPanel(), BorderLayout.CENTER);

		final DCPanel toolBarPanel = new DCPanel(WidgetUtils.BG_COLOR_DARKEST, WidgetUtils.BG_COLOR_DARKEST);
		toolBarPanel.setBorder(new MatteBorder(1, 0, 0, 0, WidgetUtils.BG_COLOR_MEDIUM));
		toolBarPanel.setLayout(new BorderLayout());
		toolBarPanel.add(getBottomToolBar(), BorderLayout.CENTER);

		final DCPanel outerPanel = new DCPanel();
		outerPanel.setLayout(new BorderLayout());
		outerPanel.add(mainPanel, BorderLayout.CENTER);
		outerPanel.add(toolBarPanel, BorderLayout.SOUTH);
		outerPanel.setPreferredSize(550, 580);

		final Container glass = (Container) getGlassPane();
		glass.setLayout(null);
		glass.setVisible(true);
		glass.add(_loginPanel);
		_loginPanel.addLoginChangeListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateDialogState(true);
			}
		});

		return outerPanel;
	}

	private DCPanel getRecentJobsPanel() {
		if (_recentJobsPanel.getComponentCount() == 0) {
			_recentJobsPanel.setLayout(new VerticalLayout(4));
			_recentJobsPanel.setBorder(new EmptyBorder(0, 10, 0, 200));

			final JLabel newAnalysisLabel = new JLabel("New analysis job");
			newAnalysisLabel.setFont(WidgetUtils.FONT_HEADER);
			newAnalysisLabel.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
			_recentJobsPanel.add(newAnalysisLabel);
			MultiLineLabel newAnalysisDescriptionLabel = new MultiLineLabel(
					"<htmL>DataCleaner is all about data analysis, profiling and cleansing. "
							+ "Begin a new analysis job by clicking the '<b>New</b>' button in the left-side window.</html>");
			newAnalysisDescriptionLabel.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
			_recentJobsPanel.add(newAnalysisDescriptionLabel);

			_recentJobsPanel.add(Box.createVerticalStrut(10));

			final JLabel recentAnalysisLabel = new JLabel("Recent analysis jobs");
			recentAnalysisLabel.setFont(WidgetUtils.FONT_HEADER);
			recentAnalysisLabel.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
			_recentJobsPanel.add(recentAnalysisLabel);

			MultiLineLabel recentAnalysisDescriptionLabel = new MultiLineLabel(
					"<html>Below is a list of your recent analysis jobs for easy access. "
							+ "Select a job and click the '<b>Open recent job</b>' button to access a recent job or click the '<b>Open</b>' button in the left-side window to browse the file system for saved jobs.</html>");
			recentAnalysisDescriptionLabel.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
			_recentJobsPanel.add(recentAnalysisDescriptionLabel);

			final List<File> recentJobFiles = CollectionUtils.filter(userPreferences.getRecentJobFiles(),
					new Function<File, Boolean>() {
						private static final long serialVersionUID = 1L;

						@Override
						public Boolean run(File file) throws Exception {
							return file.exists();
						}
					});

			final DefaultListCellRenderer delegateCellRenderer = new DefaultListCellRenderer();
			final JXList list = new JXList(recentJobFiles.toArray());
			list.addHighlighter(WidgetUtils.LIBERELLO_HIGHLIGHTER);
			list.setCellRenderer(new ListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
						boolean cellHasFocus) {
					if (value instanceof File) {
						File file = (File) value;
						value = file.getName() + " - " + file.getAbsolutePath();
					}
					return delegateCellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				}
			});

			final JScrollPane listScroll = WidgetUtils.scrolleable(list);
			listScroll.setPreferredSize(new Dimension(200, 100));
			listScroll.setBorder(WidgetUtils.BORDER_THIN);
			_recentJobsPanel.add(listScroll);

			final JButton openButton = WidgetFactory.createButton("Open recent job", "images/actions/open.png");
			openButton.setBackground(WidgetUtils.BG_COLOR_DARKEST);
			openButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
			openButton.setFocusPainted(false);
			openButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int jobIndex = list.getSelectedIndex();
					if (jobIndex != -1) {
						File file = recentJobFiles.get(jobIndex);
						new OpenAnalysisJobActionListener(_configuration, file).actionPerformed(null);
					}
				}
			});

			final DCPanel buttonPanel = new DCPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			buttonPanel.add(openButton);
			_recentJobsPanel.add(buttonPanel);
		}
		return _recentJobsPanel;
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

		JToolBar toolBar = WidgetFactory.createToolBar();
		toolBar.setOpaque(false);
		toolBar.setBorder(null);
		toolBar.add(datacleanerButton);
		toolBar.add(bloggerButton);
		toolBar.add(linkedInButton);
		toolBar.add(WidgetFactory.createToolBarSeparator());
		toolBar.add(_showOnStartupCheckBox);
		toolBar.add(_nextStepButton);

		return toolBar;
	}
}
