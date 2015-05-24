/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.NumberFormat;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.job.concurrent.MultiThreadedTaskRunner;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.panels.DCBannerPanel;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.panels.DatabaseDriversPanel;
import org.datacleaner.panels.ExtensionPackagesPanel;
import org.datacleaner.storage.StorageProvider;
import org.datacleaner.user.QuickAnalysisStrategy;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.NumberDocument;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.FileSelectionListener;
import org.datacleaner.widgets.FilenameTextField;
import org.datacleaner.widgets.HelpIcon;
import org.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

public class OptionsDialog extends AbstractWindow {

    private static final long serialVersionUID = 1L;

    private final ImageManager imageManager = ImageManager.get();
    private final UserPreferences _userPreferences;
    private final CloseableTabbedPane _tabbedPane;
    private final DataCleanerConfiguration _configuration;
    private Timer _updateMemoryTimer;

    @Inject
    protected OptionsDialog(WindowContext windowContext, DataCleanerConfiguration configuration,
            UserPreferences userPreferences, DatabaseDriversPanel databaseDriversPanel,
            ExtensionPackagesPanel extensionPackagesPanel) {
        super(windowContext);
        _userPreferences = userPreferences;
        _configuration = configuration;
        _tabbedPane = new CloseableTabbedPane(true);

        _tabbedPane.addTab("General", imageManager.getImageIcon(IconUtils.MENU_OPTIONS, IconUtils.ICON_SIZE_TAB),
                getGeneralTab());
        _tabbedPane.addTab("Database drivers",
                imageManager.getImageIcon(IconUtils.GENERIC_DATASTORE_IMAGEPATH, IconUtils.ICON_SIZE_TAB),
                databaseDriversPanel);
        _tabbedPane.addTab("Network", imageManager.getImageIcon("images/menu/network.png", IconUtils.ICON_SIZE_TAB),
                getNetworkTab());
        _tabbedPane.addTab("Performance",
                imageManager.getImageIcon("images/menu/performance.png", IconUtils.ICON_SIZE_TAB), getPerformanceTab());
        _tabbedPane.addTab("Memory", imageManager.getImageIcon("images/menu/memory.png", IconUtils.ICON_SIZE_TAB),
                getMemoryTab());
        _tabbedPane.addTab("Extensions", imageManager.getImageIcon(IconUtils.PLUGIN, IconUtils.ICON_SIZE_TAB),
                extensionPackagesPanel);

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
        final FilenameTextField saveDatastoreDirectoryField = new FilenameTextField(
                _userPreferences.getSaveDatastoreDirectory(), true);
        saveDatastoreDirectoryField.setFile(_userPreferences.getSaveDatastoreDirectory());
        saveDatastoreDirectoryField.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        saveDatastoreDirectoryField.addFileSelectionListener(new FileSelectionListener() {
            @Override
            public void onSelected(FilenameTextField filenameTextField, File file) {
                _userPreferences.setSaveDatastoreDirectory(file);
            }
        });

        final DCPanel directoriesPanel = new DCPanel().setTitledBorder("Files & directories");
        directoriesPanel.add(DCLabel.dark("Written datastores:"));
        directoriesPanel.add(saveDatastoreDirectoryField);

        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        panel.setLayout(new VerticalLayout(4));
        panel.add(getQuickAnalysisPanel());
        panel.add(directoriesPanel);

        return panel;
    }

    private DCPanel getQuickAnalysisPanel() {
        final QuickAnalysisStrategy quickAnalysisStrategy = QuickAnalysisStrategy
                .loadFromUserPreferences(_userPreferences);
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
                    QuickAnalysisStrategy.saveToUserPreferences(newStrategy, _userPreferences);
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

        final JCheckBox proxyCheckBox = new JCheckBox("Enable HTTP proxy?", _userPreferences.isProxyEnabled());
        proxyCheckBox.setOpaque(false);
        proxyCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _userPreferences.setProxyEnabled(proxyCheckBox.isSelected());
            }
        });

        final DCPanel proxyPanel = new DCPanel().setTitledBorder("Proxy settings");

        final JTextField proxyHostField = WidgetFactory.createTextField("Proxy host");
        proxyHostField.setText(_userPreferences.getProxyHostname());
        proxyHostField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent e) {
                _userPreferences.setProxyHostname(proxyHostField.getText());
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
                _userPreferences.setProxyPort(port);
            }
        });
        proxyPortField.setText("" + _userPreferences.getProxyPort());
        WidgetUtils.addToGridBag(new JLabel("Proxy port"), proxyPanel, 0, 1);
        WidgetUtils.addToGridBag(proxyPortField, proxyPanel, 1, 1);

        final JCheckBox proxyAuthCheckBox = new JCheckBox("Enable authentication?",
                _userPreferences.isProxyAuthenticationEnabled());
        proxyAuthCheckBox.setOpaque(false);
        proxyAuthCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _userPreferences.setProxyAuthenticationEnabled(proxyAuthCheckBox.isSelected());
            }
        });

        final DCPanel proxyAuthPanel = new DCPanel().setTitledBorder("Proxy authentication");
        final JTextField proxyUsernameField = WidgetFactory.createTextField("Username");
        proxyUsernameField.setText(_userPreferences.getProxyUsername());
        proxyUsernameField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                _userPreferences.setProxyUsername(proxyUsernameField.getText());
            }
        });
        WidgetUtils.addToGridBag(new JLabel("Username"), proxyAuthPanel, 0, 0);
        WidgetUtils.addToGridBag(proxyUsernameField, proxyAuthPanel, 1, 0);

        final JPasswordField proxyPasswordField = WidgetFactory.createPasswordField();
        proxyPasswordField.setText(_userPreferences.getProxyPassword());
        proxyPasswordField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                _userPreferences.setProxyPassword(String.valueOf(proxyPasswordField.getPassword()));
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

        final DCPanel networkTabPanel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        networkTabPanel.setLayout(new BorderLayout());
        networkTabPanel.add(proxyCheckBox, BorderLayout.NORTH);
        networkTabPanel.add(proxyPanel, BorderLayout.CENTER);

        return networkTabPanel;
    }

    private DCPanel getPerformanceTab() {
        DCPanel panel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);

        int row = 0;

        TaskRunner taskRunner = _configuration.getEnvironment().getTaskRunner();
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
        StorageProvider storageProvider = _configuration.getEnvironment().getStorageProvider();
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
        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);

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

        JButton button = WidgetFactory.createDefaultButton("Perform garbage collection");
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
        final JButton closeButton = WidgetFactory.createPrimaryButton("Close", IconUtils.ACTION_CLOSE_BRIGHT);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _userPreferences.save();
                OptionsDialog.this.dispose();
            }
        });

        final DCPanel buttonPanel = DCPanel.flow(Alignment.CENTER, closeButton);

        final DCBannerPanel banner = new DCBannerPanel("Options");
        _tabbedPane.bindTabTitleToBanner(banner);

        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        panel.setLayout(new BorderLayout());
        panel.add(banner, BorderLayout.NORTH);
        panel.add(_tabbedPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        panel.setPreferredSize(700, 500);
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
        return imageManager.getImage(IconUtils.MENU_OPTIONS);
    }

    public DataCleanerConfiguration getConfiguration() {
        return _configuration;
    }

    public CloseableTabbedPane getTabbedPane() {
        return _tabbedPane;
    }

    public UserPreferences getUserPreferences() {
        return _userPreferences;
    }
}
