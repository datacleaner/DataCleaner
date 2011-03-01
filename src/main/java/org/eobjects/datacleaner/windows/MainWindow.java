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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.datacleaner.Main;
import org.eobjects.datacleaner.actions.ExitActions;
import org.eobjects.datacleaner.panels.DCBannerPanel;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.panels.DatastoresListPanel;
import org.eobjects.datacleaner.panels.DictionaryListPanel;
import org.eobjects.datacleaner.panels.JobListPanel;
import org.eobjects.datacleaner.panels.StringPatternListPanel;
import org.eobjects.datacleaner.panels.SynonymCatalogListPanel;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.DCWindowMenuBar;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

public class MainWindow extends AbstractWindow {

	private static final long serialVersionUID = 1L;

	private static final int WINDOW_WIDTH = 300;
	private static final ImageManager imageManager = ImageManager.getInstance();
	private static final UserPreferences userPreferences = UserPreferences.getInstance();

	private final AnalyzerBeansConfiguration _configuration;

	public MainWindow(AnalyzerBeansConfiguration configuration) {
		super();
		_configuration = configuration;
		setJMenuBar(new DCWindowMenuBar(_configuration));
	}

	@Override
	protected void initialize() {
		super.initialize();

		centerOnScreen();
		setLocation(20, (int) getLocation().getY());
	}

	@Override
	public String getWindowTitle() {
		return "DataCleaner " + Main.VERSION;
	}

	@Override
	public Image getWindowIcon() {
		return ImageManager.getInstance().getImage("images/window/app-icon.png");
	}

	@Override
	protected DCPanel getWindowContent() {
		DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_DARK, WidgetUtils.BG_COLOR_DARK);
		Dimension dimension = new Dimension(WINDOW_WIDTH, 650);
		panel.setPreferredSize(dimension);
		panel.setSize(dimension);
		panel.setLayout(new BorderLayout());

		panel.add(getHeaderPanel(), BorderLayout.NORTH);

		final JXTaskPaneContainer taskPaneContainer = WidgetFactory.createTaskPaneContainer();

		final JXTaskPane datastoresTaskPane = WidgetFactory.createTaskPane("Datastores",
				imageManager.getImageIcon("images/model/datastore.png"));
		datastoresTaskPane.add(new DatastoresListPanel(_configuration));
		datastoresTaskPane.setCollapsed(!userPreferences.isDisplayDatastoresTaskPane());
		datastoresTaskPane.addPropertyChangeListener("collapsed", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				userPreferences.setDisplayDatastoresTaskPane(!datastoresTaskPane.isCollapsed());
			}
		});
		taskPaneContainer.add(datastoresTaskPane);

		final JXTaskPane analysisJobsTaskPane = WidgetFactory.createTaskPane("Analysis jobs",
				imageManager.getImageIcon("images/model/job.png"));
		analysisJobsTaskPane.add(new JobListPanel(_configuration));
		analysisJobsTaskPane.setCollapsed(!userPreferences.isDisplayJobsTaskPane());
		analysisJobsTaskPane.addPropertyChangeListener("collapsed", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				userPreferences.setDisplayJobsTaskPane(!analysisJobsTaskPane.isCollapsed());
			}
		});
		taskPaneContainer.add(analysisJobsTaskPane);

		final JXTaskPane dictionariesTaskPane = WidgetFactory.createTaskPane("Dictionaries",
				imageManager.getImageIcon("images/model/dictionary.png"));
		dictionariesTaskPane.setCollapsed(true);
		dictionariesTaskPane.add(new DictionaryListPanel(_configuration));
		dictionariesTaskPane.setCollapsed(!userPreferences.isDisplayDictionariesTaskPane());
		dictionariesTaskPane.addPropertyChangeListener("collapsed", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				userPreferences.setDisplayDictionariesTaskPane(!dictionariesTaskPane.isCollapsed());
			}
		});
		taskPaneContainer.add(dictionariesTaskPane);

		final JXTaskPane synonymsTaskPane = WidgetFactory.createTaskPane("Synonyms",
				imageManager.getImageIcon("images/model/synonym.png"));
		synonymsTaskPane.setCollapsed(true);
		synonymsTaskPane.add(new SynonymCatalogListPanel(_configuration));
		synonymsTaskPane.setCollapsed(!userPreferences.isDisplaySynonymCatalogsTaskPane());
		synonymsTaskPane.addPropertyChangeListener("collapsed", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				userPreferences.setDisplaySynonymCatalogsTaskPane(!synonymsTaskPane.isCollapsed());
			}
		});
		taskPaneContainer.add(synonymsTaskPane);

		final JXTaskPane patternsTaskPane = WidgetFactory.createTaskPane("String patterns",
				imageManager.getImageIcon("images/model/stringpattern.png"));
		patternsTaskPane.setCollapsed(true);
		patternsTaskPane.add(new StringPatternListPanel(_configuration));
		patternsTaskPane.setCollapsed(!userPreferences.isDisplayStringPatternsTaskPane());
		patternsTaskPane.addPropertyChangeListener("collapsed", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				userPreferences.setDisplayStringPatternsTaskPane(!patternsTaskPane.isCollapsed());
			}
		});
		taskPaneContainer.add(patternsTaskPane);

		JScrollPane scrollPane = WidgetUtils.scrolleable(taskPaneContainer);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		panel.add(scrollPane, BorderLayout.CENTER);

		JXStatusBar statusBar = WidgetFactory.createStatusBar(DCLabel.bright(getWindowTitle()));

		panel.add(statusBar, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel getHeaderPanel() {
		final DCBannerPanel headerPanel = new DCBannerPanel(imageManager.getImage("images/window/banner-main-window.png"));
		return headerPanel;
	}

	@Override
	protected boolean onWindowClosing() {
		return super.onWindowClosing();
	}

	@Override
	public void dispose() {
		ExitActions.showExitDialog();
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	protected boolean isCentered() {
		return false;
	}
}
