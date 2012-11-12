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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.actions.DisplayOutputWritersAction;
import org.eobjects.datacleaner.actions.NewAnalysisJobActionListener;
import org.eobjects.datacleaner.actions.OpenAnalysisJobActionListener;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.windows.AboutDialog;
import org.eobjects.datacleaner.windows.DCWindow;
import org.eobjects.datacleaner.windows.MonitorConnectionDialog;
import org.eobjects.datacleaner.windows.OptionsDialog;
import org.eobjects.datacleaner.windows.ReferenceDataDialog;
import org.jdesktop.swingx.action.OpenBrowserAction;

/**
 * Represents the menubar of DataCleaner.
 * 
 * @author Kasper Sørensen
 */
@Singleton
public class DCWindowMenuBar extends JMenuBar {

	private static final long serialVersionUID = 1L;

	private final WindowContext _windowContext;
	private final ActionListener _windowListener;
	private final Provider<ReferenceDataDialog> _referenceDataDialogProvider;
	private final Provider<OptionsDialog> _optionsDialogProvider;
	private final JMenu _writeDataMenu;
    private final Provider<MonitorConnectionDialog> _monitorConnectionDialogProvider;

	@Inject
	protected DCWindowMenuBar(final WindowContext windowContext,
			final Provider<ReferenceDataDialog> referenceDataDialogProvider,
			NewAnalysisJobActionListener newAnalysisJobActionListener,
			OpenAnalysisJobActionListener openAnalysisJobActionListener, Provider<OptionsDialog> optionsDialogProvider, Provider<MonitorConnectionDialog> monitorConnectionDialogProvider) {
		super();
		_windowContext = windowContext;
		_referenceDataDialogProvider = referenceDataDialogProvider;
		_optionsDialogProvider = optionsDialogProvider;
		_monitorConnectionDialogProvider = monitorConnectionDialogProvider;
		
		final JMenuItem newJobMenuItem = WidgetFactory.createMenuItem("New analysis job",
				"images/actions/new_analysis_job.png");
		newJobMenuItem.addActionListener(newAnalysisJobActionListener);

		final JMenuItem openJobMenuItem = WidgetFactory.createMenuItem("Open analysis job...", "images/actions/open.png");
		openJobMenuItem.addActionListener(openAnalysisJobActionListener);

		final JMenuItem exitMenuItem = WidgetFactory.createMenuItem("Exit DataCleaner", "images/menu/exit.png");
		exitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (_windowContext.showExitDialog()) {
					_windowContext.exit();
				}
			}
		});

		final JMenuItem dictionariesMenuItem = WidgetFactory.createMenuItem("Dictionaries", "images/model/dictionary.png");
		dictionariesMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ReferenceDataDialog referenceDataDialog = _referenceDataDialogProvider.get();
				referenceDataDialog.selectDictionariesTab();
				referenceDataDialog.open();
			}
		});

		final JMenuItem synonymCatalogsMenuItem = WidgetFactory.createMenuItem("Synonyms", "images/model/synonym.png");
		synonymCatalogsMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ReferenceDataDialog referenceDataDialog = _referenceDataDialogProvider.get();
				referenceDataDialog.selectSynonymsTab();
				referenceDataDialog.open();
			}
		});

		final JMenuItem stringPatternsMenuItem = WidgetFactory.createMenuItem("String patterns",
				"images/model/stringpattern.png");
		stringPatternsMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ReferenceDataDialog referenceDataDialog = _referenceDataDialogProvider.get();
				referenceDataDialog.selectStringPatternsTab();
				referenceDataDialog.open();
			}
		});

		final JMenuItem optionsMenuItem = WidgetFactory.createMenuItem("Options", IconUtils.MENU_OPTIONS);
		optionsMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OptionsDialog optionsDialog = _optionsDialogProvider.get();
				optionsDialog.open();
			}
		});
		
		final JMenuItem dqMonitorMenuItem = WidgetFactory.createMenuItem("DataCleaner dq monitor", IconUtils.MENU_DQ_MONITOR);
		dqMonitorMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MonitorConnectionDialog dialog = _monitorConnectionDialogProvider.get();
                dialog.open();
            }
        });

		final JMenuItem helpContents = WidgetFactory.createMenuItem("Help contents", "images/widgets/help.png");
		helpContents.addActionListener(new OpenBrowserAction("http://datacleaner.org/docs"));

		final JMenuItem askAtTheForumsMenuItem = WidgetFactory.createMenuItem("Ask at the forums", "images/menu/forums.png");
		askAtTheForumsMenuItem.addActionListener(new OpenBrowserAction("http://datacleaner.org/forum/1"));

		final JMenuItem aboutMenuItem = WidgetFactory.createMenuItem("About DataCleaner", "images/menu/about.png");
		aboutMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new AboutDialog(_windowContext).setVisible(true);
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

		_writeDataMenu = WidgetFactory.createMenu("Write data", 'W');

		final JMenu windowMenu = WidgetFactory.createMenu("Window", 'W');
		windowMenu.add(optionsMenuItem);
		windowMenu.add(dqMonitorMenuItem);
		windowMenu.addSeparator();

		final int minimumSize = windowMenu.getMenuComponentCount();

		_windowListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final int currentSize = windowMenu.getMenuComponentCount();
				for (int i = currentSize; i > minimumSize; i--) {
					windowMenu.remove(i - 1);
				}
				final List<DCWindow> windows = _windowContext.getWindows();
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
		};

		final JMenu helpMenu = WidgetFactory.createMenu("Help", 'H');
		helpMenu.add(askAtTheForumsMenuItem);
		helpMenu.add(helpContents);
		helpMenu.add(aboutMenuItem);

		add(fileMenu);
		add(referenceDataMenu);
		add(_writeDataMenu);
		add(windowMenu);
		add(helpMenu);
	}

	public JMenu getWriteDataMenu() {
		return _writeDataMenu;
	}

	@Override
	public void addNotify() {
		super.addNotify();
		_windowContext.addWindowListener(_windowListener);
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		_windowContext.removeWindowListener(_windowListener);
	}

	public void setAnalysisJobBuilder(AnalysisJobBuilder analysisJobBuilder) {
		List<JMenuItem> menuItems = new DisplayOutputWritersAction(analysisJobBuilder).createMenuItems();
		_writeDataMenu.removeAll();
		for (JMenuItem menuItem : menuItems) {
			_writeDataMenu.add(menuItem);
		}
	}
}
