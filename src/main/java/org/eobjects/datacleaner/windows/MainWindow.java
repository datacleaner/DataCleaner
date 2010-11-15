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
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.datacleaner.Main;
import org.eobjects.datacleaner.actions.OpenAnalysisJobActionListener;
import org.eobjects.datacleaner.panels.DCBannerPanel;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.panels.DatastoresListPanel;
import org.eobjects.datacleaner.panels.DictionaryListPanel;
import org.eobjects.datacleaner.panels.JobListPanel;
import org.eobjects.datacleaner.panels.SynonymCatalogListPanel;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.util.WindowManager;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

public class MainWindow extends AbstractWindow {

	private static final long serialVersionUID = 1L;

	private static final int WINDOW_WIDTH = 220;
	private static final ImageManager imageManager = ImageManager.getInstance();

	private final AnalyzerBeansConfiguration _configuration;

	public MainWindow(AnalyzerBeansConfiguration configuration) {
		super();
		_configuration = configuration;
		setJMenuBar(getWindowMenuBar());
	}

	@Override
	protected void initialize() {
		super.initialize();

		centerOnScreen();
		setLocation(20, (int) getLocation().getY());
	}

	@Override
	protected String getWindowTitle() {
		return "DataCleaner " + Main.VERSION;
	}

	@Override
	protected Image getWindowIcon() {
		return ImageManager.getInstance().getImage("images/window/app-icon.png");
	}

	@Override
	protected DCPanel getWindowContent() {
		DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_MEDIUM, WidgetUtils.BG_COLOR_LESS_DARK);
		Dimension dimension = new Dimension(WINDOW_WIDTH, 650);
		panel.setPreferredSize(dimension);
		panel.setSize(dimension);
		panel.setLayout(new BorderLayout());

		panel.add(getHeaderPanel(), BorderLayout.NORTH);

		JXTaskPaneContainer taskPaneContainer = WidgetFactory.createTaskPaneContainer();

		JXTaskPane datastoresTaskPane = new JXTaskPane();
		datastoresTaskPane.setTitle("Datastores");
		datastoresTaskPane.setIcon(imageManager.getImageIcon("images/model/datastore.png"));
		datastoresTaskPane.add(new DatastoresListPanel(_configuration));
		taskPaneContainer.add(datastoresTaskPane);

		JXTaskPane analysisJobsTaskPane = new JXTaskPane();
		analysisJobsTaskPane.setTitle("Analysis jobs");
		analysisJobsTaskPane.setIcon(imageManager.getImageIcon("images/model/job.png"));
		analysisJobsTaskPane.add(new JobListPanel(_configuration));
		taskPaneContainer.add(analysisJobsTaskPane);

		JXTaskPane dictionariesTaskPane = new JXTaskPane();
		dictionariesTaskPane.setTitle("Dictionaries");
		dictionariesTaskPane.setIcon(imageManager.getImageIcon("images/model/dictionary.png"));
		dictionariesTaskPane.setCollapsed(true);
		dictionariesTaskPane.add(new DictionaryListPanel(_configuration));
		taskPaneContainer.add(dictionariesTaskPane);

		JXTaskPane synonymsTaskPane = new JXTaskPane();
		synonymsTaskPane.setTitle("Synonyms");
		synonymsTaskPane.setIcon(imageManager.getImageIcon("images/model/synonym.png"));
		synonymsTaskPane.setCollapsed(true);
		synonymsTaskPane.add(new SynonymCatalogListPanel(_configuration));
		taskPaneContainer.add(synonymsTaskPane);

		JXTaskPane expressionsTaskPane = new JXTaskPane();
		expressionsTaskPane.setTitle("Expressions");
		expressionsTaskPane.setIcon(imageManager.getImageIcon("images/model/expression.png"));
		expressionsTaskPane.setCollapsed(true);
		taskPaneContainer.add(expressionsTaskPane);

		JScrollPane scrollPane = WidgetUtils.scrolleable(taskPaneContainer);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		panel.add(scrollPane, BorderLayout.CENTER);

		JXStatusBar statusBar = new JXStatusBar();
		JXStatusBar.Constraint c1 = new JXStatusBar.Constraint(JXStatusBar.Constraint.ResizeBehavior.FILL);
		statusBar.add(new JLabel(getWindowTitle()), c1);

		panel.add(statusBar, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel getHeaderPanel() {
		final DCBannerPanel headerPanel = new DCBannerPanel();
		return headerPanel;
	}

	@Override
	protected boolean onWindowClosing() {
		return super.onWindowClosing();
	}

	@Override
	public void dispose() {
		UserPreferences.getInstance().save();
		System.exit(0);
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	private JMenuBar getWindowMenuBar() {
		final JMenuBar menuBar = new JMenuBar();

		final JMenuItem openJobMenuItem = WidgetFactory.createMenuItem("Open analysis job", "images/actions/open.png");
		openJobMenuItem.addActionListener(new OpenAnalysisJobActionListener(_configuration));

		final JMenuItem exitMenuItem = WidgetFactory.createMenuItem("Exit DataCleaner", "images/menu/exit.png");
		exitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				windowClosing(null);
			}
		});

		final JMenuItem optionsMenuItem = WidgetFactory.createMenuItem("Options ...", "images/menu/options.png");
		optionsMenuItem.setEnabled(false);

		final JMenuItem aboutMenuItem = WidgetFactory.createMenuItem("About DataCleaner", "images/menu/about.png");
		aboutMenuItem.setEnabled(false);

		final JMenu fileMenu = WidgetFactory.createMenu("File", 'F');
		fileMenu.add(openJobMenuItem);
		fileMenu.add(exitMenuItem);

		final JMenu editMenu = WidgetFactory.createMenu("Edit", 'E');
		editMenu.add(optionsMenuItem);

		final JMenu windowsMenu = WidgetFactory.createMenu("Windows", 'W');
		WindowManager.getInstance().addListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				windowsMenu.removeAll();
				List<AbstractWindow> windows = WindowManager.getInstance().getWindows();
				for (final AbstractWindow window : windows) {
					final Image windowIcon = window.getWindowIcon();
					final ImageIcon icon = new ImageIcon(windowIcon.getScaledInstance(32, 32, Image.SCALE_DEFAULT));
					final JMenuItem switchToWindowItem = WidgetFactory.createMenuItem(window.getWindowTitle(), icon);
					switchToWindowItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							window.toFront();
						}
					});
					windowsMenu.add(switchToWindowItem);
				}
			}
		});

		final JMenu helpMenu = WidgetFactory.createMenu("Help", 'H');
		helpMenu.add(aboutMenuItem);

		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(windowsMenu);
		menuBar.add(helpMenu);
		return menuBar;
	}

	@Override
	protected boolean isCentered() {
		return false;
	}

	public AnalyzerBeansConfiguration getConfiguration() {
		return _configuration;
	}
}
