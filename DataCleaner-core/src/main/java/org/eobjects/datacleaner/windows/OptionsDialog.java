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
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.concurrent.MultiThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.storage.StorageProvider;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.DCBannerPanel;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.panels.DatabaseDriversPanel;
import org.eobjects.datacleaner.panels.ExtensionPackagesPanel;
import org.eobjects.datacleaner.user.DCConfiguration;
import org.eobjects.datacleaner.user.QuickAnalysisStrategy;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.NumberDocument;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.FileSelectionListener;
import org.eobjects.datacleaner.widgets.FilenameTextField;
import org.eobjects.datacleaner.widgets.HelpIcon;
import org.eobjects.datacleaner.widgets.HumanInferenceToolbarButton;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

public class OptionsDialog extends AbstractWindow {

	private static final long serialVersionUID = 1L;

	private final ImageManager imageManager = ImageManager.getInstance();
	private final UserPreferences userPreferences = UserPreferences.getInstance();
	private final CloseableTabbedPane _tabbedPane;
	private final AnalyzerBeansConfiguration _configuration;
	private Timer _updateMemoryTimer;

	public OptionsDialog(WindowContext windowContext) {
		super(windowContext);
		_configuration = DCConfiguration.get();
		_tabbedPane = new CloseableTabbedPane();

		_tabbedPane.addTab("General", imageManager.getImageIcon("images/menu/options.png"), getGeneralTab());
		_tabbedPane.addTab("Database drivers", imageManager.getImageIcon("images/model/datastore.png"),
				new DatabaseDriversPanel(_configuration, windowContext));
		_tabbedPane.addTab("Network", imageManager.getImageIcon("images/menu/network.png"), getNetworkTab());
		_tabbedPane.addTab("Performance", imageManager.getImageIcon("images/menu/performance.png"), getPerformanceTab());
		_tabbedPane.addTab("Memory", imageManager.getImageIcon("images/menu/memory.png"), getMemoryTab());
		_tabbedPane.addTab("Extensions", imageManager.getImageIcon("images/component-types/plugin.png"),
				new ExtensionPackagesPanel());

		_tabbedPane.setUnclosableTab(0);
		_tabbedPane.setUnclosableTab(1);
		_tabbedPane.setUnclosableTab(2);
		_tabbedPane.setUnclosableTab(3);
		_tabbedPane.setUnclosableTab(4);
		_tabbedPane.setUnclosableTab(5);
	}

	public void selectDatabaseDriversTab() {
		_tabbedPane.setSelectedIndex(1);
	}

	private DCPanel getGeneralTab() {
		final String username = userPreferences.getUsername();
		final JXTextField usernameTextField = WidgetFactory.createTextField();
		usernameTextField.setText(username);
		usernameTextField.setEnabled(false);

		final JButton logoutButton = WidgetFactory.createSmallButton("images/actions/remove.png");
		logoutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				userPreferences.setUsername(null);
				usernameTextField.setText("");
				logoutButton.setEnabled(false);
			}
		});
		logoutButton.setEnabled(userPreferences.isLoggedIn());

		final DCPanel userRegistrationPanel = new DCPanel().setTitledBorder("User registration");
		userRegistrationPanel.add(DCLabel.dark("Logged in as:"));
		userRegistrationPanel.add(usernameTextField);
		userRegistrationPanel.add(logoutButton);

		final FilenameTextField saveDatastoreDirectoryField = new FilenameTextField(
				userPreferences.getSaveDatastoreDirectory(), true);
		saveDatastoreDirectoryField.setFile(userPreferences.getSaveDatastoreDirectory());
		saveDatastoreDirectoryField.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		saveDatastoreDirectoryField.addFileSelectionListener(new FileSelectionListener() {
			@Override
			public void onSelected(FilenameTextField filenameTextField, File file) {
				userPreferences.setSaveDatastoreDirectory(file);
			}
		});

		final DCPanel directoriesPanel = new DCPanel().setTitledBorder("Files & directories");
		directoriesPanel.add(DCLabel.dark("Written datastores:"));
		directoriesPanel.add(saveDatastoreDirectoryField);

		final DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		panel.setLayout(new VerticalLayout(4));
		panel.add(userRegistrationPanel);
		panel.add(getQuickAnalysisPanel());
		panel.add(directoriesPanel);

		return panel;
	}

	private DCPanel getQuickAnalysisPanel() {
		final QuickAnalysisStrategy quickAnalysisStrategy = userPreferences.getQuickAnalysisStrategy();
		final JXTextField columnsTextField = WidgetFactory.createTextField("Columns");
		columnsTextField.setColumns(2);
		columnsTextField.setDocument(new NumberDocument());
		columnsTextField.setText("" + quickAnalysisStrategy.getColumnsPerAnalyzer());

		final JCheckBox valueDistributionCheckBox = new JCheckBox("Include Value distribution in Quick analysis?");
		valueDistributionCheckBox.setOpaque(false);
		valueDistributionCheckBox.setSelected(quickAnalysisStrategy.isIncludeValueDistribution());

		final JCheckBox patternFinderCheckBox = new JCheckBox("Include Pattern finder in Quick analysis?");
		patternFinderCheckBox.setOpaque(false);
		patternFinderCheckBox.setSelected(quickAnalysisStrategy.isIncludePatternFinder());

		final ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					int columns = Integer.parseInt(columnsTextField.getText());
					QuickAnalysisStrategy newStrategy = new QuickAnalysisStrategy(columns,
							valueDistributionCheckBox.isSelected(), patternFinderCheckBox.isSelected());
					userPreferences.setQuickAnalysisStrategy(newStrategy);
				} catch (NumberFormatException e) {
					// skip this action, could not parse columns
				}
			}
		};
		valueDistributionCheckBox.addActionListener(actionListener);
		patternFinderCheckBox.addActionListener(actionListener);
		columnsTextField.getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent event) {
				actionListener.actionPerformed(null);
			}
		});

		final DCPanel quickAnalysisPanel = new DCPanel().setTitledBorder("Quick analysis");
		WidgetUtils.addToGridBag(DCLabel.dark("Max columns per analyzer:"), quickAnalysisPanel, 0, 0);
		WidgetUtils.addToGridBag(columnsTextField, quickAnalysisPanel, 1, 0);
		WidgetUtils.addToGridBag(valueDistributionCheckBox, quickAnalysisPanel, 0, 1, 2, 1);
		WidgetUtils.addToGridBag(patternFinderCheckBox, quickAnalysisPanel, 0, 2, 2, 1);
		return quickAnalysisPanel;
	}

	private DCPanel getNetworkTab() {

		final JCheckBox proxyCheckBox = new JCheckBox("Enable HTTP proxy?", userPreferences.isProxyEnabled());
		proxyCheckBox.setOpaque(false);
		proxyCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				userPreferences.setProxyEnabled(proxyCheckBox.isSelected());
			}
		});

		final DCPanel proxyPanel = new DCPanel().setTitledBorder("Proxy settings");

		final JTextField proxyHostField = WidgetFactory.createTextField("Proxy host");
		proxyHostField.setText(userPreferences.getProxyHostname());
		proxyHostField.getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent e) {
				userPreferences.setProxyHostname(proxyHostField.getText());
			}
		});
		WidgetUtils.addToGridBag(new JLabel("Proxy host"), proxyPanel, 0, 0);
		WidgetUtils.addToGridBag(proxyHostField, proxyPanel, 1, 0);

		final JTextField proxyPortField = WidgetFactory.createTextField("Proxy port");
		proxyPortField.setDocument(new NumberDocument());
		proxyPortField.getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent event) {
				int port;
				try {
					port = Integer.parseInt(proxyPortField.getText());
				} catch (Exception e) {
					port = 8080;
				}
				userPreferences.setProxyPort(port);
			}
		});
		proxyPortField.setText("" + userPreferences.getProxyPort());
		WidgetUtils.addToGridBag(new JLabel("Proxy port"), proxyPanel, 0, 1);
		WidgetUtils.addToGridBag(proxyPortField, proxyPanel, 1, 1);

		final JCheckBox proxyAuthCheckBox = new JCheckBox("Enable authentication?",
				userPreferences.isProxyAuthenticationEnabled());
		proxyAuthCheckBox.setOpaque(false);
		proxyAuthCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				userPreferences.setProxyAuthenticationEnabled(proxyAuthCheckBox.isSelected());
			}
		});

		final DCPanel proxyAuthPanel = new DCPanel().setTitledBorder("Proxy authentication");
		final JTextField proxyUsernameField = WidgetFactory.createTextField("Username");
		proxyUsernameField.setText(userPreferences.getProxyUsername());
		proxyUsernameField.getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent event) {
				userPreferences.setProxyUsername(proxyUsernameField.getText());
			}
		});
		WidgetUtils.addToGridBag(new JLabel("Username"), proxyAuthPanel, 0, 0);
		WidgetUtils.addToGridBag(proxyUsernameField, proxyAuthPanel, 1, 0);

		final JPasswordField proxyPasswordField = new JPasswordField(userPreferences.getProxyPassword());
		proxyPasswordField.getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent event) {
				userPreferences.setProxyPassword(String.valueOf(proxyPasswordField.getPassword()));
			}
		});
		WidgetUtils.addToGridBag(new JLabel("Password"), proxyAuthPanel, 0, 1);
		WidgetUtils.addToGridBag(proxyPasswordField, proxyAuthPanel, 1, 1);

		WidgetUtils.addToGridBag(proxyAuthCheckBox, proxyPanel, 0, 2, 2, 1);
		WidgetUtils.addToGridBag(proxyAuthPanel, proxyPanel, 0, 3, 2, 1);

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				proxyHostField.setEnabled(proxyCheckBox.isSelected());
				proxyPortField.setEnabled(proxyCheckBox.isSelected());
				proxyAuthCheckBox.setEnabled(proxyCheckBox.isSelected());
				proxyUsernameField.setEnabled(proxyAuthCheckBox.isSelected() && proxyCheckBox.isSelected());
				proxyPasswordField.setEnabled(proxyAuthCheckBox.isSelected() && proxyCheckBox.isSelected());
			}
		};
		proxyCheckBox.addActionListener(actionListener);
		proxyAuthCheckBox.addActionListener(actionListener);

		// use ActionListener to initialize components
		actionListener.actionPerformed(null);

		final DCPanel networkTabPanel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		networkTabPanel.setLayout(new BorderLayout());
		networkTabPanel.add(proxyCheckBox, BorderLayout.NORTH);
		networkTabPanel.add(proxyPanel, BorderLayout.CENTER);

		return networkTabPanel;
	}

	private DCPanel getPerformanceTab() {
		DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);

		int row = 0;

		TaskRunner taskRunner = _configuration.getTaskRunner();
		WidgetUtils.addToGridBag(new JLabel("Task runner type:"), panel, 0, row);
		WidgetUtils.addToGridBag(new JLabel(taskRunner.getClass().getSimpleName()), panel, 1, row);
		WidgetUtils
				.addToGridBag(
						new HelpIcon(
								"The task runner is used to determine the execution strategy of Analysis jobs. The most common strategy for this is to use a multithreaded task runner which will spawn several threads to enable concurrent execution of jobs."),
						panel, 2, row);

		if (taskRunner instanceof MultiThreadedTaskRunner) {
			int numThreads = ((MultiThreadedTaskRunner) taskRunner).getNumThreads();

			if (numThreads > 0) {
				row++;
				WidgetUtils.addToGridBag(new JLabel("Thread pool size:"), panel, 0, row);
				WidgetUtils.addToGridBag(new JLabel("" + numThreads), panel, 1, row);
			}
		}

		row++;
		StorageProvider storageProvider = _configuration.getStorageProvider();
		WidgetUtils.addToGridBag(new JLabel("Storage provider type:"), panel, 0, row);
		WidgetUtils.addToGridBag(new JLabel(storageProvider.getClass().getSimpleName()), panel, 1, row);
		WidgetUtils
				.addToGridBag(
						new HelpIcon(
								"The storage provider is used for staging data during and after analysis, typically to store the results on disk in stead of holding everything in memory."),
						panel, 2, row);

		row++;
		DCLabel descriptionLabel = DCLabel
				.darkMultiLine("Performance options are currently not configurable while you're running the application. "
						+ "You need to edit the applications configuration file for this. The configuration file is named "
						+ "<b>conf.xml</b> and is located in the root of the folder where you've installed DataCleaner.");
		descriptionLabel.setBorder(new EmptyBorder(10, 10, 0, 10));
		WidgetUtils.addToGridBag(descriptionLabel, panel, 0, row, 2, 1);
		return panel;
	}

	private DCPanel getMemoryTab() {
		final DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);

		final JLabel maxMemoryLabel = new JLabel("? kb", JLabel.RIGHT);
		final JLabel totalMemoryLabel = new JLabel("? kb", JLabel.RIGHT);
		final JLabel usedMemoryLabel = new JLabel("? kb", JLabel.RIGHT);
		final JLabel freeMemoryLabel = new JLabel("? kb", JLabel.RIGHT);

		WidgetUtils.addToGridBag(new JLabel("Max available memory:"), panel, 0, 0);
		WidgetUtils.addToGridBag(maxMemoryLabel, panel, 1, 0);
		WidgetUtils.addToGridBag(new JLabel("Allocated memory:"), panel, 0, 1);
		WidgetUtils.addToGridBag(totalMemoryLabel, panel, 1, 1);
		WidgetUtils.addToGridBag(new JLabel("Used memory:"), panel, 0, 2);
		WidgetUtils.addToGridBag(usedMemoryLabel, panel, 1, 2);
		WidgetUtils.addToGridBag(new JLabel("Free memory:"), panel, 0, 3);
		WidgetUtils.addToGridBag(freeMemoryLabel, panel, 1, 3);

		_updateMemoryTimer = new Timer(1000, new ActionListener() {
			private final Runtime runtime = Runtime.getRuntime();
			private final NumberFormat nf = NumberFormat.getIntegerInstance();

			@Override
			public void actionPerformed(ActionEvent e) {

				long totalMemory = runtime.totalMemory();
				long freeMemory = runtime.freeMemory();
				long maxMemory = runtime.maxMemory();
				long usedMemory = totalMemory - freeMemory;

				if (maxMemory == Long.MAX_VALUE) {
					maxMemoryLabel.setText("(no limit)");
				} else {
					maxMemoryLabel.setText(nf.format(maxMemory / 1024) + " kb");
				}
				totalMemoryLabel.setText(nf.format(totalMemory / 1024) + " kb");
				usedMemoryLabel.setText(nf.format(usedMemory / 1024) + " kb");
				freeMemoryLabel.setText(nf.format(freeMemory / 1024) + " kb");
			}
		});
		_updateMemoryTimer.setInitialDelay(0);
		_updateMemoryTimer.start();

		JButton button = new JButton("Perform garbage collection");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.gc();
				System.runFinalization();
			}
		});
		WidgetUtils.addToGridBag(button, panel, 1, 4);

		return panel;
	}

	@Override
	protected boolean onWindowClosing() {
		boolean closing = super.onWindowClosing();
		if (closing) {
			if (_updateMemoryTimer != null) {
				_updateMemoryTimer.stop();
			}
		}
		return closing;
	}

	@Override
	protected JComponent getWindowContent() {
		final JButton closeButton = WidgetFactory.createButton("Close", "images/actions/save.png");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OptionsDialog.this.dispose();
			}
		});

		final JToolBar toolBar = WidgetFactory.createToolBar();
		toolBar.add(new HumanInferenceToolbarButton());
		toolBar.add(WidgetFactory.createToolBarSeparator());
		toolBar.add(closeButton);

		final DCPanel toolBarPanel = new DCPanel(WidgetUtils.BG_COLOR_DARKEST, WidgetUtils.BG_COLOR_DARKEST);
		toolBarPanel.setLayout(new BorderLayout());
		toolBarPanel.add(toolBar, BorderLayout.CENTER);

		final DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_DARK, WidgetUtils.BG_COLOR_DARK);
		panel.setLayout(new BorderLayout());
		panel.add(new DCBannerPanel("Options"), BorderLayout.NORTH);
		panel.add(_tabbedPane, BorderLayout.CENTER);
		panel.add(toolBarPanel, BorderLayout.SOUTH);
		panel.setPreferredSize(500, 500);
		return panel;
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	protected boolean isCentered() {
		return true;
	}

	@Override
	public String getWindowTitle() {
		return "Options";
	}

	@Override
	public Image getWindowIcon() {
		return imageManager.getImage("images/menu/options.png");
	}
}
