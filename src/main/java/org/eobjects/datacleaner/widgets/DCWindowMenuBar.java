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

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.datacleaner.actions.ExitActions;
import org.eobjects.datacleaner.actions.OpenAnalysisJobActionListener;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WindowManager;
import org.eobjects.datacleaner.windows.AboutDialog;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;
import org.eobjects.datacleaner.windows.DCWindow;
import org.eobjects.datacleaner.windows.OptionsDialog;
import org.eobjects.datacleaner.windows.ReferenceDataDialog;
import org.jdesktop.swingx.action.OpenBrowserAction;

/**
 * Represents the menubar of DataCleaner.
 * 
 * @author Kasper Sørensen
 */
public class DCWindowMenuBar extends JMenuBar {

	private static final long serialVersionUID = 1L;
	private final AnalyzerBeansConfiguration _configuration;

	public DCWindowMenuBar(AnalyzerBeansConfiguration configuration) {
		super();
		_configuration = configuration;
		final JMenuItem newJobMenuItem = WidgetFactory.createMenuItem("New analysis job",
				"images/actions/new_analysis_job.png");
		newJobMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new AnalysisJobBuilderWindow(_configuration).setVisible(true);
			}
		});

		final JMenuItem openJobMenuItem = WidgetFactory.createMenuItem("Open analysis job...", "images/actions/open.png");
		openJobMenuItem.addActionListener(new OpenAnalysisJobActionListener(_configuration));

		final JMenuItem exitMenuItem = WidgetFactory.createMenuItem("Exit DataCleaner", "images/menu/exit.png");
		exitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ExitActions.showExitDialog();
			}
		});

		final JMenuItem dictionariesMenuItem = WidgetFactory.createMenuItem("Dictionaries", "images/model/dictionary.png");
		dictionariesMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ReferenceDataDialog referenceDataDialog = new ReferenceDataDialog();
				referenceDataDialog.selectDictionariesTab();
				referenceDataDialog.setVisible(true);
			}
		});

		final JMenuItem synonymCatalogsMenuItem = WidgetFactory.createMenuItem("Synonyms", "images/model/synonym.png");
		synonymCatalogsMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ReferenceDataDialog referenceDataDialog = new ReferenceDataDialog();
				referenceDataDialog.selectSynonymsTab();
				referenceDataDialog.setVisible(true);
			}
		});

		final JMenuItem stringPatternsMenuItem = WidgetFactory.createMenuItem("String patterns",
				"images/model/stringpattern.png");
		stringPatternsMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ReferenceDataDialog referenceDataDialog = new ReferenceDataDialog();
				referenceDataDialog.selectStringPatternsTab();
				referenceDataDialog.setVisible(true);
			}
		});

		final JMenuItem optionsMenuItem = WidgetFactory.createMenuItem("Options", "images/menu/options.png");
		optionsMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new OptionsDialog().setVisible(true);
			}
		});

		final JMenuItem helpContents = WidgetFactory.createMenuItem("Help contents", "images/widgets/help.png");
		helpContents.addActionListener(new OpenBrowserAction("http://datacleaner.eobjects.org/docs"));

		final JMenuItem askAtTheForumsMenuItem = WidgetFactory.createMenuItem("Ask at the forums", "images/menu/forums.png");
		askAtTheForumsMenuItem.addActionListener(new OpenBrowserAction("http://datacleaner.eobjects.org/forum/1"));

		final JMenuItem aboutMenuItem = WidgetFactory.createMenuItem("About DataCleaner", "images/menu/about.png");
		aboutMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new AboutDialog().setVisible(true);
			}
		});

		final JMenu fileMenu = WidgetFactory.createMenu("File", 'F');
		fileMenu.add(newJobMenuItem);
		fileMenu.add(openJobMenuItem);
		fileMenu.add(exitMenuItem);

		final JMenu referenceDataMenu = WidgetFactory.createMenu("Reference data", 'R');
		referenceDataMenu.add(dictionariesMenuItem);
		referenceDataMenu.add(synonymCatalogsMenuItem);
		referenceDataMenu.add(stringPatternsMenuItem);

		final JMenu windowMenu = WidgetFactory.createMenu("Window", 'W');
		windowMenu.add(optionsMenuItem);
		windowMenu.addSeparator();

		final int minimumSize = windowMenu.getMenuComponentCount();

		WindowManager.getInstance().addListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final int currentSize = windowMenu.getMenuComponentCount();
				for (int i = currentSize; i > minimumSize; i--) {
					windowMenu.remove(i - 1);
				}
				final List<DCWindow> windows = WindowManager.getInstance().getWindows();
				for (final DCWindow window : windows) {
					final Image windowIcon = window.getWindowIcon();
					final String title = window.getWindowTitle();
					final ImageIcon icon = new ImageIcon(windowIcon.getScaledInstance(32, 32, Image.SCALE_DEFAULT));
					final JMenuItem switchToWindowItem = WidgetFactory.createMenuItem(title, icon);
					switchToWindowItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							window.toFront();
						}
					});
					windowMenu.add(switchToWindowItem);
				}
			}
		});

		final JMenu helpMenu = WidgetFactory.createMenu("Help", 'H');
		helpMenu.add(askAtTheForumsMenuItem);
		helpMenu.add(helpContents);
		helpMenu.add(aboutMenuItem);

		add(fileMenu);
		add(referenceDataMenu);
		add(windowMenu);
		add(helpMenu);
	}
}
