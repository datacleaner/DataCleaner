package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.datacleaner.panels.DCBannerPanel;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.panels.DatastoresListPanel;
import org.eobjects.datacleaner.panels.JobListPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.builder.WidgetFactory;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.action.OpenBrowserAction;

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
		return "DataCleaner 2";
	}

	@Override
	protected Image getWindowIcon() {
		return ImageManager.getInstance().getImage("images/window/app-icon.png");
	}

	@Override
	protected DCPanel getWindowContent() {
		DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHTEST, WidgetUtils.BG_COLOR_BRIGHT);
		Dimension dimension = new Dimension(WINDOW_WIDTH, 650);
		panel.setPreferredSize(dimension);
		panel.setSize(dimension);
		panel.setLayout(new BorderLayout());

		panel.add(getHeaderPanel(), BorderLayout.NORTH);

		JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();
		taskPaneContainer.setOpaque(false);

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
		taskPaneContainer.add(dictionariesTaskPane);

		JXTaskPane synonymsTaskPane = new JXTaskPane();
		synonymsTaskPane.setTitle("Synonyms");
		synonymsTaskPane.setIcon(imageManager.getImageIcon("images/model/synonym.png"));
		synonymsTaskPane.setCollapsed(true);
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
		DCBannerPanel headerPanel = new DCBannerPanel();
		headerPanel.setLayout(new HorizontalLayout());
		headerPanel.add(Box.createVerticalStrut(headerPanel.getHeight()));

		DCPanel buttonPanel = new DCPanel();
		BoxLayout layout = new BoxLayout(buttonPanel, BoxLayout.X_AXIS);
		buttonPanel.setLayout(layout);
		headerPanel.add(buttonPanel);

		buttonPanel.add(Box.createHorizontalStrut(WidgetUtils.BORDER_WIDE_WIDTH));

		JButton visitWebsiteButton = new JButton(imageManager.getImageIcon("images/actions/website.png",
				IconUtils.ICON_SIZE_SMALL));
		visitWebsiteButton.setToolTipText("Visit the DataCleaner website");
		visitWebsiteButton.addActionListener(new OpenBrowserAction("http://datacleaner.eobjects.org"));
		visitWebsiteButton.setAlignmentY(BOTTOM_ALIGNMENT);
		buttonPanel.add(visitWebsiteButton);

		buttonPanel.add(Box.createHorizontalStrut(WidgetUtils.BORDER_WIDE_WIDTH));

		JButton optionsButton = new JButton(imageManager.getImageIcon("images/menu/options.png", IconUtils.ICON_SIZE_SMALL));
		optionsButton.setToolTipText("Options");
		optionsButton.setAlignmentY(BOTTOM_ALIGNMENT);
		buttonPanel.add(optionsButton);

		return headerPanel;
	}

	@Override
	protected boolean onWindowClosing() {
		return super.onWindowClosing();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		System.exit(0);
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	private JMenuBar getWindowMenuBar() {
		JMenuBar menuBar = WidgetFactory.createMenuBar().toComponent();

		JMenu fileMenu = WidgetFactory.createMenu("File", 'F').toComponent();
		JMenuItem exitMenuItem = WidgetFactory.createMenuItem("Exit DataCleaner", "images/menu/exit.png").toComponent();
		exitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				windowClosing(null);
			}
		});
		fileMenu.add(exitMenuItem);

		JMenu editMenu = WidgetFactory.createMenu("Edit", 'E').toComponent();
		editMenu.add(WidgetFactory.createMenuItem("Options ...", "images/menu/options.png").toComponent());

		JMenu helpMenu = WidgetFactory.createMenu("Help", 'H').toComponent();
		helpMenu.add(WidgetFactory.createMenuItem("About DataCleaner", "images/menu/about.png").toComponent());

		menuBar.add(fileMenu);
		menuBar.add(editMenu);
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
