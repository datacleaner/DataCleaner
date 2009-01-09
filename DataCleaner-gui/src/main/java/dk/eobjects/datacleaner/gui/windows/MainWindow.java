/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.gui.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.action.OpenBrowserAction;

import dk.eobjects.datacleaner.catalog.IDictionary;
import dk.eobjects.datacleaner.catalog.NamedRegex;
import dk.eobjects.datacleaner.catalog.TextFileDictionary;
import dk.eobjects.datacleaner.gui.DataCleanerGui;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.dialogs.AboutDialog;
import dk.eobjects.datacleaner.gui.dialogs.DatabaseDictionaryDialog;
import dk.eobjects.datacleaner.gui.dialogs.DatabaseDriverDialog;
import dk.eobjects.datacleaner.gui.dialogs.NamedRegexDialog;
import dk.eobjects.datacleaner.gui.dialogs.NewTaskDialog;
import dk.eobjects.datacleaner.gui.dialogs.RegexSwapDialog;
import dk.eobjects.datacleaner.gui.dialogs.SettingsDialog;
import dk.eobjects.datacleaner.gui.dialogs.TextFileDictionaryDialog;
import dk.eobjects.datacleaner.gui.model.DatabaseDictionary;
import dk.eobjects.datacleaner.gui.model.ExtensionFilter;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;
import dk.eobjects.datacleaner.gui.widgets.OpenFileActionListener;
import dk.eobjects.datacleaner.util.WeakObservable;
import dk.eobjects.datacleaner.util.WeakObserver;

public class MainWindow implements WeakObserver, WindowListener {

	private static final long serialVersionUID = -2859158909004213811L;
	private static final Log _log = LogFactory.getLog(MainWindow.class);
	private JFrame _frame;
	private List<JFrame> _windows = new ArrayList<JFrame>();
	private JXStatusBar _statusBar;
	private JMenuBar _menubar;
	private JMenu _windowMenu;
	private JToolBar _topToolbar;
	private JPanel _contentPanel;
	private JToolBar _bottomToolbar;
	private JComboBox _dictionaryList;
	private JComboBox _regexList;
	private JButton _editRegexButton;
	private JButton _removeRegexButton;
	private JButton _removeDictionaryButton;
	private JButton _editDictionaryButton;

	public JFrame getFrame() {
		return _frame;
	}

	public MainWindow removeAllWindows() {
		JFrame[] windows = _windows.toArray(new JFrame[_windows.size()]);
		for (JFrame window : windows) {
			window.dispose();
		}
		return this;
	}

	public MainWindow() {
		_frame = new JFrame("DataCleaner");
		_frame.setIconImage(GuiHelper.getImage("images/datacleaner_icon.png"));
		Dimension d = new Dimension(200, 430);
		_frame.setPreferredSize(d);
		_frame.setSize(d);
		_frame.setResizable(false);
		_frame.setLocation(100, 100);
		_frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		_frame.addWindowListener(this);

		_frame.setLayout(new BorderLayout());
		_frame.add(getBanner(), BorderLayout.NORTH);

		ActionListener newTaskListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NewTaskDialog dialog = new NewTaskDialog();
				dialog.setVisible(true);
			}
		};

		_menubar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		JMenuItem newTaskItem = new JMenuItem("New task", GuiHelper
				.getImageIcon("images/toolbar_new_window.png"));
		newTaskItem.addActionListener(newTaskListener);
		newTaskItem.setMnemonic('N');
		fileMenu.add(newTaskItem);

		JMenuItem openFileItem = new JMenuItem("Open file", GuiHelper
				.getImageIcon("images/toolbar_open.png"));
		openFileItem.setMnemonic('O');
		openFileItem.addActionListener(new OpenFileActionListener());
		fileMenu.add(openFileItem);

		fileMenu.add(new JSeparator(JSeparator.HORIZONTAL));

		fileMenu.add(DatabaseDriverDialog.getMenu());

		fileMenu.add(new JSeparator(JSeparator.HORIZONTAL));

		JMenuItem exitItem = new JMenuItem("Exit DataCleaner", GuiHelper
				.getImageIcon("images/menu_exit.png"));
		exitItem.setMnemonic('x');
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GuiHelper.confirmExit();
			}
		});
		fileMenu.add(exitItem);

		_menubar.add(fileMenu);

		ActionListener settingsActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SettingsDialog dialog = new SettingsDialog();
				dialog.setVisible(true);
			}
		};

		_menubar.add(createWindowMenu(settingsActionListener));

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');

		JMenuItem helpContentItem = new JMenuItem("Help contents", GuiHelper
				.getImageIcon("images/menu_help.png"));
		helpContentItem.setMnemonic('H');
		try {
			helpContentItem.addActionListener(new OpenBrowserAction(
					"http://datacleaner.eobjects.org/docs"));
		} catch (MalformedURLException e) {
			_log.error(e);
		}
		helpMenu.add(helpContentItem);

		JMenuItem forumItem = new JMenuItem("Ask at the forums", GuiHelper
				.getImageIcon("images/toolbar_forum.png"));
		forumItem.setMnemonic('F');
		try {
			forumItem.addActionListener(new OpenBrowserAction(
					"http://datacleaner.eobjects.org/forum/1"));
		} catch (MalformedURLException e) {
			_log.error(e);
		}
		helpMenu.add(forumItem);

		JMenuItem aboutItem = new JMenuItem("About DataCleaner", GuiHelper
				.getImageIcon("images/menu_about.png"));
		aboutItem.setMnemonic('A');
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AboutDialog dialog = new AboutDialog();
				dialog.setVisible(true);
			}
		});

		helpMenu.add(aboutItem);
		_menubar.add(helpMenu);
		_frame.setJMenuBar(_menubar);

		_topToolbar = GuiHelper.createToolBar();
		JButton newTaskButton = new JButton("New task", GuiHelper
				.getImageIcon("images/toolbar_new_window.png"));
		newTaskButton.addActionListener(newTaskListener);
		_topToolbar.add(newTaskButton);

		JButton settingsButton = new JButton("Settings", GuiHelper
				.getImageIcon("images/toolbar_settings.png"));
		settingsButton.addActionListener(settingsActionListener);
		_topToolbar.add(settingsButton);

		_statusBar = new JXStatusBar();
		JXStatusBar.Constraint c1 = new JXStatusBar.Constraint(
				JXStatusBar.Constraint.ResizeBehavior.FILL);
		_statusBar.add(new JLabel("DataCleaner " + DataCleanerGui.VERSION), c1);

		_contentPanel = GuiHelper.createPanel().applyBorderLayout()
				.applyDarkBlueBackground().toComponent();
		_contentPanel.add(_topToolbar, BorderLayout.NORTH);

		JPanel panel = GuiHelper.createPanel().applyDarkBlueBackground()
				.toComponent();
		GuiSettings settings = GuiSettings.getSettings();
		GuiHelper.addToGridBag(getDictionaryPanel(settings), panel, 0, 0);
		GuiHelper.addToGridBag(GuiHelper.createPanel()
				.applyDarkBlueBackground().applySize(null, 2).toComponent(),
				panel, 0, 1);
		GuiHelper.addToGridBag(getRegexPanel(settings), panel, 0, 2);
		_contentPanel.add(panel, BorderLayout.CENTER);

		_bottomToolbar = GuiHelper.createToolBar();

		JButton websiteButton = GuiHelper
				.createButton("Visit DataCleaner website",
						"images/toolbar_visit_website.png").toComponent();
		try {
			websiteButton.addActionListener(new OpenBrowserAction(
					"http://datacleaner.eobjects.org"));
		} catch (MalformedURLException e) {
			_log.error(e);
		}
		_bottomToolbar.add(websiteButton);

		_contentPanel.add(_bottomToolbar, BorderLayout.SOUTH);

		_frame.add(_contentPanel, BorderLayout.CENTER);
		_frame.add(_statusBar, BorderLayout.SOUTH);

		settings.addObserver(this);
		_frame.pack();
		_frame.setVisible(true);
	}

	private JPanel getDictionaryPanel(GuiSettings settings) {
		JPanel panel = GuiHelper.createPanel().applyBorder().toComponent();
		GuiHelper.addToGridBag(new JLabel("Dictionary catalog", GuiHelper
				.getImageIcon("images/dictionaries.png"), JLabel.LEFT), panel,
				0, 0, 2, 1);

		_dictionaryList = new JComboBox(new DefaultComboBoxModel());
		_dictionaryList.setEditable(false);
		Dimension d = new Dimension(105, 16);
		_dictionaryList.setPreferredSize(d);
		_dictionaryList.setSize(d);

		GuiHelper.addToGridBag(_dictionaryList, panel, 0, 1);

		JToolBar buttonBar = GuiHelper.createToolBar();
		_editDictionaryButton = new JButton(GuiHelper
				.getImageIcon("images/toolbar_open.png"));
		_editDictionaryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IDictionary dictionary = GuiSettings.getSettings()
						.getDictionaries().get(
								_dictionaryList.getSelectedIndex());
				if (dictionary instanceof TextFileDictionary) {
					TextFileDictionaryDialog dialog = new TextFileDictionaryDialog(
							(TextFileDictionary) dictionary);
					dialog.setVisible(true);
				} else if (dictionary instanceof DatabaseDictionary) {
					DatabaseDictionaryDialog dialog = new DatabaseDictionaryDialog(
							(DatabaseDictionary) dictionary);
					dialog.setVisible(true);
				}
			}
		});
		buttonBar.add(_editDictionaryButton);
		_removeDictionaryButton = new JButton(GuiHelper
				.getImageIcon("images/toolbar_remove.png"));
		_removeDictionaryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int choise = JOptionPane.showConfirmDialog(_frame,
						"Are you sure you want to remove this dictionary?",
						"Remove dictionary", JOptionPane.YES_NO_OPTION);
				if (choise == JOptionPane.YES_OPTION) {
					GuiSettings settings = GuiSettings.getSettings();
					settings.getDictionaries().remove(
							_dictionaryList.getSelectedIndex());
					GuiSettings.saveSettings(settings);
				}
			}
		});
		buttonBar.add(_removeDictionaryButton);
		GuiHelper.addToGridBag(buttonBar, panel, 1, 1);

		buttonBar = GuiHelper.createToolBar();
		final JButton addButton = new JButton("New dictionary", GuiHelper
				.getImageIcon("images/toolbar_add.png"));
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPopupMenu popupMenu = new JPopupMenu("Add dictionary");
				JMenuItem textfileItem = new JMenuItem("Text-file dictionary",
						GuiHelper.getImageIcon("images/toolbar_file.png"));
				textfileItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						TextFileDictionaryDialog dialog = new TextFileDictionaryDialog(
								null);
						dialog.setVisible(true);
					}
				});
				popupMenu.add(textfileItem);
				JMenuItem databaseItem = new JMenuItem("Database dictionary",
						GuiHelper.getImageIcon("images/toolbar_database.png"));
				databaseItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						DatabaseDictionaryDialog dialog = new DatabaseDictionaryDialog(
								null);
						dialog.setVisible(true);
					}
				});
				popupMenu.add(databaseItem);
				popupMenu.show(addButton, addButton.getWidth(), 0);
			}
		});
		buttonBar.add(addButton);
		GuiHelper.addToGridBag(buttonBar, panel, 0, 2, 2, 1);

		updateDictionaryList(settings);

		return panel;
	}

	private void updateDictionaryList(GuiSettings settings) {
		DefaultComboBoxModel model = (DefaultComboBoxModel) _dictionaryList
				.getModel();
		model.removeAllElements();
		List<IDictionary> dictionaries = settings.getDictionaries();
		for (IDictionary dictionary : dictionaries) {
			model.addElement(dictionary.getName());
		}
		if (dictionaries.size() == 0) {
			_editDictionaryButton.setEnabled(false);
			_removeDictionaryButton.setEnabled(false);
		} else {
			_editDictionaryButton.setEnabled(true);
			_removeDictionaryButton.setEnabled(true);
		}
	}

	private JPanel getRegexPanel(GuiSettings settings) {
		JPanel panel = GuiHelper.createPanel().applyBorder()
				.applyBorderLayout().toComponent();
		GuiHelper.addToGridBag(new JLabel("Regex catalog", GuiHelper
				.getImageIcon("images/regexes.png"), JLabel.LEFT), panel, 0, 0,
				2, 1);
		_regexList = new JComboBox(new DefaultComboBoxModel());
		_regexList.setEditable(false);
		Dimension d = new Dimension(105, 16);
		_regexList.setPreferredSize(d);
		_regexList.setSize(d);

		GuiHelper.addToGridBag(_regexList, panel, 0, 1);

		JToolBar buttonBar = GuiHelper.createToolBar();
		_editRegexButton = new JButton(GuiHelper
				.getImageIcon("images/toolbar_open.png"));
		_editRegexButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NamedRegex regex = GuiSettings.getSettings().getRegexes().get(
						_regexList.getSelectedIndex());
				NamedRegexDialog dialog = new NamedRegexDialog(regex);
				dialog.setVisible(true);
			}
		});
		buttonBar.add(_editRegexButton);
		_removeRegexButton = new JButton(GuiHelper
				.getImageIcon("images/toolbar_remove.png"));
		_removeRegexButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int choise = JOptionPane.showConfirmDialog(_frame,
						"Are you sure you want to remove this regex?",
						"Remove regex", JOptionPane.YES_NO_OPTION);
				if (choise == JOptionPane.YES_OPTION) {
					GuiSettings settings = GuiSettings.getSettings();
					settings.getRegexes().remove(_regexList.getSelectedIndex());
					GuiSettings.saveSettings(settings);
				}
			}
		});
		buttonBar.add(_removeRegexButton);
		GuiHelper.addToGridBag(buttonBar, panel, 1, 1);

		buttonBar = GuiHelper.createToolBar();
		final JButton addButton = new JButton("New regex", GuiHelper
				.getImageIcon("images/toolbar_add.png"));
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPopupMenu popup = new JPopupMenu();

				JMenuItem createRegexItem = new JMenuItem(
						"Create new expression", GuiHelper
								.getImageIcon("images/regexes.png"));
				createRegexItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						NamedRegexDialog dialog = new NamedRegexDialog(null);
						dialog.setVisible(true);
					}
				});
				popup.add(createRegexItem);

				JMenuItem regexSwapItem = new JMenuItem(
						"Import from the RegexSwap",
						GuiHelper
								.getImageIcon("images/toolbar_visit_website.png"));
				regexSwapItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						RegexSwapDialog dialog = new RegexSwapDialog();
						dialog.setVisible(true);
					}
				});
				popup.add(regexSwapItem);

				JMenuItem loadRegexesItem = new JMenuItem(
						"Load from .properties file", GuiHelper
								.getImageIcon("images/toolbar_file.png"));
				loadRegexesItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						JFileChooser fileChooser = new JFileChooser(new File(
								"samples/regexes"));
						ExtensionFilter filter = new ExtensionFilter(
								"Property file (.properties)", "properties");
						fileChooser.setFileFilter(filter);
						GuiHelper.centerOnScreen(fileChooser);
						if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
							File selectedFile = fileChooser.getSelectedFile();
							try {
								List<NamedRegex> regexes = NamedRegex
										.loadFromFile(selectedFile);
								GuiSettings settings = GuiSettings
										.getSettings();
								settings.getRegexes().addAll(regexes);
								GuiSettings.saveSettings(settings);
							} catch (IllegalArgumentException e) {
								GuiHelper.showErrorMessage(
										"Could not load regexes",
										"Error occurred during load of regexes from file: "
												+ selectedFile
														.getAbsolutePath(), e);
							}
						}
					}
				});
				popup.add(loadRegexesItem);

				popup.show(addButton, addButton.getWidth(), 0);

			}
		});
		buttonBar.add(addButton);

		GuiHelper.addToGridBag(buttonBar, panel, 0, 2, 2, 1);

		updateRegexList(settings);

		return panel;
	}

	private void updateRegexList(GuiSettings settings) {
		DefaultComboBoxModel model = (DefaultComboBoxModel) _regexList
				.getModel();
		model.removeAllElements();
		List<NamedRegex> regexes = settings.getRegexes();
		for (NamedRegex regex : regexes) {
			model.addElement(regex.getName());
		}
		if (regexes.size() == 0) {
			_editRegexButton.setEnabled(false);
			_removeRegexButton.setEnabled(false);
		} else {
			_editRegexButton.setEnabled(true);
			_removeRegexButton.setEnabled(true);
		}
	}

	private JMenu createWindowMenu(ActionListener settingsActionListener) {
		_windowMenu = new JMenu("Window");
		_windowMenu.setMnemonic('W');

		JMenuItem settingsItem = new JMenuItem("Settings", GuiHelper
				.getImageIcon("images/toolbar_settings.png"));
		settingsItem.setMnemonic('S');
		settingsItem.addActionListener(settingsActionListener);
		_windowMenu.add(settingsItem);

		JMenuItem cascadeItem = new JMenuItem("Cascade", GuiHelper
				.getImageIcon("images/menu_cascade.png"));
		cascadeItem.setMnemonic('C');
		cascadeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cascade();
			}
		});
		_windowMenu.add(cascadeItem);

		_windowMenu.add(new JSeparator());
		return _windowMenu;
	}

	public MainWindow addWindow(final AbstractWindow window) {
		_log.debug("addWindow()");
		JFrame frame = window.toFrame(_windows, _windowMenu);
		frame.setVisible(true);
		return this;
	}

	public void dispose() {
		removeAllWindows();
		_frame.setVisible(false);
		_frame.dispose();
	}

	private void cascade() {
		int x = _frame.getLocation().x + _frame.getWidth();
		int y = _frame.getLocation().y;

		for (int i = 0; i < _windows.size(); i++) {
			JFrame window = _windows.get(i);
			x += 10;
			window.setLocation(x, y);
			y += 30;
			if (i == _windows.size() - 1) {
				window.toFront();
			}
		}
	}

	private Component getBanner() {
		JPanel panel = GuiHelper.createPanel().applyLayout(
				new FlowLayout(FlowLayout.LEFT, 0, 0)).toComponent();
		panel.setBorder(new MatteBorder(0, 0, 1, 0, Color.BLACK));
		Icon logo = GuiHelper.getImageIcon("images/main_banner.png");
		JLabel label = new JLabel(logo);
		panel.add(label, 0, 0);
		return panel;
	}

	public void update(WeakObservable o) {
		if (o instanceof GuiSettings) {
			GuiSettings settings = (GuiSettings) o;
			updateDictionaryList(settings);
			updateRegexList(settings);
		}
	}

	public void repaintAll() {
		SwingUtilities.updateComponentTreeUI(_frame);
		List<JFrame> windows = _windows;
		for (JFrame frame : windows) {
			SwingUtilities.updateComponentTreeUI(frame);
		}
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		GuiHelper.confirmExit();
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}
}