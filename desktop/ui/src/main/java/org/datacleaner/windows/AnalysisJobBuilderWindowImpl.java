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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.apache.commons.vfs2.FileObject;
import org.datacleaner.Version;
import org.datacleaner.actions.NewAnalysisJobActionListener;
import org.datacleaner.actions.OpenAnalysisJobActionListener;
import org.datacleaner.actions.RunAnalysisActionListener;
import org.datacleaner.actions.SaveAnalysisJobActionListener;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.api.InputColumn;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.guice.JobFile;
import org.datacleaner.guice.Nullable;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerChangeListener;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.FilterChangeListener;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.SourceColumnChangeListener;
import org.datacleaner.job.builder.TransformerChangeListener;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.builder.UnconfiguredConfiguredPropertyException;
import org.datacleaner.panels.DCGlassPane;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.panels.ExecuteJobWithoutAnalyzersDialog;
import org.datacleaner.panels.SchemaTreePanel;
import org.datacleaner.panels.WelcomePanel;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.user.UsageLogger;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.CollapsibleTreePanel;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DCPersistentSizedPanel;
import org.datacleaner.widgets.DCPopupBubble;
import org.datacleaner.widgets.DescriptorMenuBuilder;
import org.datacleaner.widgets.LicenceAndEditionStatusLabel;
import org.datacleaner.widgets.PopupButton;
import org.datacleaner.widgets.tabs.JobClassicView;
import org.datacleaner.widgets.visualization.JobGraph;
import org.jdesktop.swingx.JXStatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main window in the DataCleaner GUI. This window is called the
 * AnalysisJobBuilderWindow because it's main purpose is to present a job that
 * is being built. Behind the covers this job state is respresented in the
 * {@link AnalysisJobBuilder} class.
 */
@Singleton
public final class AnalysisJobBuilderWindowImpl extends AbstractWindow implements AnalysisJobBuilderWindow {

    private static final String USER_PREFERENCES_PROPERTY_EDITING_MODE_PREFERENCE = "editing_mode_preference";

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(AnalysisJobBuilderWindow.class);
    private static final ImageManager imageManager = ImageManager.get();

    private static final int DEFAULT_WINDOW_WIDTH = 1000;
    private static final int DEFAULT_WINDOW_HEIGHT = 710;

    private final List<PopupButton> _superCategoryButtons = new ArrayList<>();
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final AnalyzerBeansConfiguration _configuration;
    private final RendererFactory _presenterRendererFactory;
    private final DCLabel _statusLabel = DCLabel.bright("");
    private final CollapsibleTreePanel _leftPanel;
    private final SchemaTreePanel _schemaTreePanel;
    private final JButton _saveButton;
    private final JButton _saveAsButton;
    private final JButton _executeButton;
    private final Provider<RunAnalysisActionListener> _runAnalysisActionProvider;
    private final Provider<SaveAnalysisJobActionListener> _saveAnalysisJobActionListenerProvider;
    private final Provider<NewAnalysisJobActionListener> _newAnalysisJobActionListenerProvider;
    private final Provider<OpenAnalysisJobActionListener> _openAnalysisJobActionListenerProvider;
    private final Provider<ReferenceDataDialog> _referenceDataDialogProvider;
    private final Provider<MonitorConnectionDialog> _monitorConnectionDialogProvider;
    private final Provider<OptionsDialog> _optionsDialogProvider;
    private final DCGlassPane _glassPane;
    private final WelcomePanel _welcomePanel;
    private final UserPreferences _userPreferences;
    private final InjectorBuilder _injectorBuilder;
    private final JToggleButton _classicViewButton;
    private final JToggleButton _graphViewButton;
    private final JobGraph _graph;
    private final DCPanel _contentContainerPanel;
    private final JComponent _editingContentView;
    private final UsageLogger _usageLogger;
    private JobClassicView _classicView;
    private FileObject _jobFilename;
    private Datastore _datastore;
    private DatastoreConnection _datastoreConnection;
    private boolean _datastoreSelectionEnabled;

    @Inject
    protected AnalysisJobBuilderWindowImpl(AnalyzerBeansConfiguration configuration, WindowContext windowContext,
            SchemaTreePanel schemaTreePanel, Provider<RunAnalysisActionListener> runAnalysisActionProvider,
            AnalysisJobBuilder analysisJobBuilder, InjectorBuilder injectorBuilder, UserPreferences userPreferences,
            @Nullable @JobFile FileObject jobFilename,
            Provider<NewAnalysisJobActionListener> newAnalysisJobActionListenerProvider,
            Provider<OpenAnalysisJobActionListener> openAnalysisJobActionListenerProvider,
            Provider<SaveAnalysisJobActionListener> saveAnalysisJobActionListenerProvider,
            Provider<ReferenceDataDialog> referenceDataDialogProvider, UsageLogger usageLogger,
            Provider<OptionsDialog> optionsDialogProvider,
            Provider<MonitorConnectionDialog> monitorConnectionDialogProvider,
            OpenAnalysisJobActionListener openAnalysisJobActionListener, DatabaseDriverCatalog databaseDriverCatalog) {
        super(windowContext);
        _jobFilename = jobFilename;
        _configuration = configuration;
        _runAnalysisActionProvider = runAnalysisActionProvider;
        _newAnalysisJobActionListenerProvider = newAnalysisJobActionListenerProvider;
        _openAnalysisJobActionListenerProvider = openAnalysisJobActionListenerProvider;
        _saveAnalysisJobActionListenerProvider = saveAnalysisJobActionListenerProvider;
        _referenceDataDialogProvider = referenceDataDialogProvider;
        _monitorConnectionDialogProvider = monitorConnectionDialogProvider;
        _optionsDialogProvider = optionsDialogProvider;
        _userPreferences = userPreferences;
        _usageLogger = usageLogger;

        if (analysisJobBuilder == null) {
            _analysisJobBuilder = new AnalysisJobBuilder(_configuration);
        } else {
            _analysisJobBuilder = analysisJobBuilder;
            DatastoreConnection con = _analysisJobBuilder.getDatastoreConnection();
            if (con != null) {
                _datastore = con.getDatastore();
            }
        }

        _datastoreSelectionEnabled = true;
        _presenterRendererFactory = new RendererFactory(configuration);
        _glassPane = new DCGlassPane(this);
        _injectorBuilder = injectorBuilder;

        _graph = new JobGraph(windowContext, _analysisJobBuilder, _presenterRendererFactory, usageLogger);

        _analysisJobBuilder.getAnalyzerChangeListeners().add(createAnalyzerChangeListener());
        _analysisJobBuilder.getTransformerChangeListeners().add(createTransformerChangeListener());
        _analysisJobBuilder.getFilterChangeListeners().add(createFilterChangeListener());
        _analysisJobBuilder.getSourceColumnListeners().add(createSourceColumnChangeListener());

        _saveButton = createToolbarButton("Save", IconUtils.MENU_SAVE, null);
        _saveAsButton = createToolbarButton("Save As...", IconUtils.MENU_SAVE, null);

        _executeButton = createToolbarButton("Execute", IconUtils.MENU_EXECUTE, null);

        _welcomePanel = new WelcomePanel();
        _welcomePanel.setBorder(new EmptyBorder(4, 4, 0, 20));

        _editingContentView = new DCPanel();
        _editingContentView.setLayout(new BorderLayout());

        _contentContainerPanel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _contentContainerPanel.setLayout(new BorderLayout());
        _contentContainerPanel.add(_welcomePanel, BorderLayout.NORTH);
        _contentContainerPanel.add(_editingContentView, BorderLayout.CENTER);

        final boolean graphPreferred = isGraphPreferred();

        if (graphPreferred) {
            setEditingViewGraph();
        } else {
            setEditingViewClassic();
        }

        _classicViewButton = createViewToggleButton("Classic view", "images/actions/editing-view-classic.png");
        _classicViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setEditingViewClassic();
            }
        });

        _classicViewButton.setSelected(!graphPreferred);
        _graphViewButton = createViewToggleButton("Graph view", "images/actions/editing-view-graph.png");
        _graphViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setEditingViewGraph();
            }
        });
        _graphViewButton.setSelected(graphPreferred);

        final ActionListener viewToggleButtonActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == _classicViewButton) {
                    _classicViewButton.setSelected(true);
                    _graphViewButton.setSelected(false);
                    _userPreferences.getAdditionalProperties().put(USER_PREFERENCES_PROPERTY_EDITING_MODE_PREFERENCE,
                            "Classic");
                } else {
                    _classicViewButton.setSelected(false);
                    _graphViewButton.setSelected(true);
                    _userPreferences.getAdditionalProperties().put(USER_PREFERENCES_PROPERTY_EDITING_MODE_PREFERENCE,
                            "Graph");
                }
            }
        };
        _classicViewButton.addActionListener(viewToggleButtonActionListener);
        _graphViewButton.addActionListener(viewToggleButtonActionListener);

        _schemaTreePanel = schemaTreePanel;

        _leftPanel = new CollapsibleTreePanel(_schemaTreePanel);
        _leftPanel.setVisible(false);
        _leftPanel.setCollapsed(true);
        _schemaTreePanel.setUpdatePanel(_leftPanel);
    }

    private boolean isGraphPreferred() {
        final Map<String, String> additionalProperties = _userPreferences.getAdditionalProperties();
        final String property = additionalProperties.get(USER_PREFERENCES_PROPERTY_EDITING_MODE_PREFERENCE);
        if ("Classic".equals(property)) {
            return false;
        }
        return true;
    }

    private JButton createToolbarButton(String text, String iconPath, String popupDescription) {
        final ImageIcon icon;
        if (iconPath == null) {
            icon = null;
        } else {
            icon = imageManager.getImageIcon(iconPath, IconUtils.ICON_SIZE_SMALL);
        }
        final JButton button = new JButton(text, icon);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(WidgetUtils.BG_COLOR_BLUE_BRIGHT);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
            }
        });

        if (icon == null) {
            button.setBorder(new EmptyBorder(10, 8, 10, 8));
        } else {
            button.setBorder(new EmptyBorder(10, 4, 10, 4));
        }
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        WidgetUtils.setDarkButtonStyle(button);
        if (popupDescription != null) {
            DCPopupBubble popupBubble = new DCPopupBubble(_glassPane, popupDescription, 0, 0, iconPath);
            popupBubble.attachTo(button);
        }
        return button;
    }

    /**
     * Gets whether or not the datastore has been set in this window (ie. if the
     * tree is showing a datastore).
     * 
     * @return true if a datastore is set.
     */
    @Override
    public boolean isDatastoreSet() {
        return _datastore != null;
    }

    /**
     * Initializes the window to use a particular datastore in the schema tree.
     * 
     * @param datastore
     */
    @Override
    public void setDatastore(final Datastore datastore) {
        setDatastore(datastore, false);
    }

    /**
     * Initializes the window to use a particular datastore in the schema tree.
     * 
     * @param datastore
     * @param expandTree
     *            true if the datastore tree should be initially expanded.
     */
    @Override
    public void setDatastore(final Datastore datastore, boolean expandTree) {
        final DatastoreConnection con;
        if (datastore == null) {
            con = null;
        } else {
            con = datastore.openConnection();
        }

        _datastore = datastore;
        if (_datastoreConnection != null) {
            _datastoreConnection.close();
        }
        _analysisJobBuilder.setDatastore(datastore);
        _schemaTreePanel.setDatastore(datastore, expandTree);
        _datastoreConnection = con;

        if (datastore == null) {
            _analysisJobBuilder.reset();
            displayWelcomeView();
        } else {
            displayEditingView();
        }

        updateStatusLabel();
    }

    private void displayEditingView() {
        _leftPanel.setVisible(true);
        if (_leftPanel.isCollapsed()) {
            _leftPanel.setCollapsed(false);
        }

        _welcomePanel.setVisible(false);
        _editingContentView.setVisible(true);
    }

    private void displayWelcomeView() {
        if (isShowing()) {
            if (_datastore == null) {
                if (!_leftPanel.isCollapsed()) {
                    _leftPanel.setCollapsed(true);
                }
                final Timer timer = new Timer(500, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        _leftPanel.setVisible(false);
                    }
                });
                timer.setRepeats(false);
                timer.start();

                _editingContentView.setVisible(false);
                _welcomePanel.setVisible(true);
            }
        }
    }

    private void setEditingViewGraph() {
        setEditingView(_graph.getPanel());
        _classicView = null;
    }

    private void setEditingViewClassic() {
        if (_classicView == null) {
            _classicView = new JobClassicView(getWindowContext(), _analysisJobBuilder, _presenterRendererFactory,
                    _usageLogger);
        }
        setEditingView(_classicView);
    }

    private void setEditingView(JComponent component) {
        final Component[] components = _editingContentView.getComponents();
        for (Component existing : components) {
            if (component == existing) {
                existing.setVisible(true);
            } else {
                existing.setVisible(false);
            }
        }

        _editingContentView.add(component, BorderLayout.CENTER);
        _editingContentView.updateUI();
    }

    @Override
    protected void onWindowVisible() {
        displayWelcomeView();
    }

    public void updateStatusLabel() {
        boolean executeable = false;

        if (_datastore == null) {
            setStatusLabelText("Welcome to DataCleaner " + Version.getVersion());
            _statusLabel.setIcon(imageManager.getImageIcon("images/window/app-icon.png", IconUtils.ICON_SIZE_SMALL));
        } else {
            if (!_analysisJobBuilder.getSourceColumns().isEmpty()) {
                executeable = true;
            }
            try {
                if (_analysisJobBuilder.isConfigured(true)) {
                    setStatusLabelText("Job is correctly configured");
                    setStatusLabelValid();
                } else {
                    setStatusLabelText("Job is not correctly configured");
                    setStatusLabelWarning();
                }
            } catch (Exception ex) {
                logger.debug("Job not correctly configured", ex);
                final String errorMessage;
                if (ex instanceof UnconfiguredConfiguredPropertyException) {
                    executeable = false;
                    UnconfiguredConfiguredPropertyException unconfiguredConfiguredPropertyException = (UnconfiguredConfiguredPropertyException) ex;
                    ConfiguredPropertyDescriptor configuredProperty = unconfiguredConfiguredPropertyException
                            .getConfiguredProperty();
                    ComponentBuilder componentBuilder = unconfiguredConfiguredPropertyException.getComponentBuilder();
                    errorMessage = "Property '" + configuredProperty.getName() + "' in "
                            + LabelUtils.getLabel(componentBuilder) + " is not set!";
                } else {
                    errorMessage = ex.getMessage();
                }
                setStatusLabelText("Job error status: " + errorMessage);
                setStatusLabelError();
            }
        }

        _executeButton.setEnabled(executeable);
    }

    @Override
    public void setStatusLabelError() {
        _statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_ERROR, IconUtils.ICON_SIZE_SMALL));
    }

    @Override
    public void setStatusLabelNotice() {
        _statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_INFO, IconUtils.ICON_SIZE_SMALL));
    }

    @Override
    public void setStatusLabelValid() {
        _statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_VALID, IconUtils.ICON_SIZE_SMALL));
    }

    @Override
    public void setStatusLabelWarning() {
        _statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_WARNING, IconUtils.ICON_SIZE_SMALL));
    }

    @Override
    public String getStatusLabelText() {
        return _statusLabel.getText();
    }

    @Override
    public void setStatusLabelText(String statusLabelText) {
        _statusLabel.setText(statusLabelText);
    }

    @Override
    protected boolean onWindowClosing() {
        if (!super.onWindowClosing()) {
            return false;
        }

        final int count = getWindowContext().getWindowCount(AnalysisJobBuilderWindow.class);

        final boolean windowClosing;
        final boolean exit;

        if (count == 1) {
            // if this is the last workspace window
            if (isDatastoreSet() && isDatastoreSelectionEnabled()) {
                // if datastore is set and datastore selection is enabled,
                // return to datastore selection.
                resetJob();
                exit = false;
                windowClosing = false;
            } else {
                // if datastore is not set, show exit dialog
                exit = getWindowContext().showExitDialog();
                windowClosing = exit;
            }
        } else {
            // if there are more workspace windows, simply close the window
            exit = false;
            windowClosing = true;
        }

        if (windowClosing) {
            _analysisJobBuilder.getAnalyzerChangeListeners().remove(this);
            _analysisJobBuilder.getTransformerChangeListeners().remove(this);
            _analysisJobBuilder.getFilterChangeListeners().remove(this);
            _analysisJobBuilder.getSourceColumnListeners().remove(this);
            _analysisJobBuilder.close();
            if (_datastoreConnection != null) {
                _datastoreConnection.close();
            }
            getContentPane().removeAll();
        }

        if (exit) {
            // trigger removeAll() to make sure removeNotify() methods are
            // invoked.
            getWindowContext().exit();
        }
        return windowClosing;
    }

    private void resetJob() {
        setDatastore(null);
        setJobFile(null);
    }

    @Override
    public void setJobFile(FileObject jobFile) {
        _jobFilename = jobFile;
        updateWindowTitle();
    }

    @Override
    public FileObject getJobFile() {
        return _jobFilename;
    }

    @Override
    public String getWindowTitle() {
        String title = "Analysis job";
        if (_datastore != null) {
            String datastoreName = _datastore.getName();
            if (!StringUtils.isNullOrEmpty(datastoreName)) {
                title = datastoreName + " | " + title;
            }
        }
        if (_jobFilename != null) {
            title = _jobFilename.getName().getBaseName() + " | " + title;
        }
        return title;
    }

    @Override
    public Image getWindowIcon() {
        return imageManager.getImage(IconUtils.MODEL_JOB);
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    protected JComponent getWindowContent() {
        if (_datastore != null) {
            setDatastore(_datastore);
        }

        final SaveAnalysisJobActionListener saveAnalysisJobActionListener = _saveAnalysisJobActionListenerProvider
                .get();
        _saveButton.addActionListener(saveAnalysisJobActionListener);
        _saveAsButton.addActionListener(saveAnalysisJobActionListener);
        _saveAsButton.setActionCommand(SaveAnalysisJobActionListener.ACTION_COMMAND_SAVE_AS);

        // Run analysis
        final RunAnalysisActionListener runAnalysisActionListener = _runAnalysisActionProvider.get();
        _executeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyPropertyValues();

                if (_analysisJobBuilder.getAnalyzerComponentBuilders().isEmpty()) {
                    // Present choices to user to write file somewhere,
                    // and then run a copy of the job based on that.
                    ExecuteJobWithoutAnalyzersDialog executeJobWithoutAnalyzersPanel = new ExecuteJobWithoutAnalyzersDialog(
                            _injectorBuilder, getWindowContext(), _analysisJobBuilder, _userPreferences);
                    executeJobWithoutAnalyzersPanel.open();
                    return;
                }

                runAnalysisActionListener.actionPerformed(e);
            }
        });

        final JButton newJobButton = createToolbarButton("New", IconUtils.MENU_NEW, null);
        newJobButton.addActionListener(_newAnalysisJobActionListenerProvider.get());

        final JButton openJobButton = createToolbarButton("Open", IconUtils.MENU_OPEN, null);
        openJobButton.addActionListener(_openAnalysisJobActionListenerProvider.get());

        final JToggleButton moreButton = createMoreMenuButton();

        final JButton logoButton = new JButton(imageManager.getImageIcon("images/menu/dc-logo-30.png"));
        logoButton.setToolTipText("About DataCleaner");
        logoButton.setBorder(new EmptyBorder(0, 4, 0, 10));
        logoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AboutDialog(getWindowContext()).open();
            }
        });

        final JToolBar toolBar = WidgetFactory.createToolBar();
        toolBar.add(logoButton);
        toolBar.add(newJobButton);
        toolBar.add(openJobButton);
        toolBar.add(_saveButton);
        toolBar.add(_saveAsButton);
        toolBar.add(DCLabel.bright(" | "));
        toolBar.add(moreButton);

        toolBar.add(WidgetFactory.createToolBarSeparator());
        addComponentDescriptorButtons(toolBar);
        toolBar.add(WidgetFactory.createToolBarSeparator());
        toolBar.add(_executeButton);

        final JXStatusBar statusBar = WidgetFactory.createStatusBar(_statusLabel);
        statusBar.add(_classicViewButton);
        statusBar.add(_graphViewButton);
        statusBar.add(Box.createHorizontalStrut(10));

        final LicenceAndEditionStatusLabel statusLabel = new LicenceAndEditionStatusLabel(_glassPane);
        statusBar.add(statusLabel);

        final DCPanel toolBarPanel = new DCPanel(WidgetUtils.BG_COLOR_DARK);
        toolBarPanel.setLayout(new BorderLayout());
        toolBarPanel.add(toolBar, BorderLayout.CENTER);

        final DCPanel panel = new DCPersistentSizedPanel(_userPreferences, getClass().getName(), DEFAULT_WINDOW_WIDTH,
                DEFAULT_WINDOW_HEIGHT);
        panel.setLayout(new BorderLayout());
        panel.add(toolBarPanel, BorderLayout.NORTH);
        panel.add(_leftPanel, BorderLayout.WEST);

        // newPanel.add(_tabbedPane, BorderLayout.NORTH);
        panel.add(_contentContainerPanel, BorderLayout.CENTER);

        panel.add(statusBar, BorderLayout.SOUTH);

        WidgetUtils.centerOnScreen(this);

        return panel;
    }

    private void addComponentDescriptorButtons(JToolBar toolBar) {
        final DescriptorProvider descriptorProvider = _analysisJobBuilder.getConfiguration().getDescriptorProvider();
        final Set<ComponentSuperCategory> superCategories = descriptorProvider.getComponentSuperCategories();
        for (ComponentSuperCategory superCategory : superCategories) {
            final String name = superCategory.getName();
            final String description = "<html><b>" + name + "</b><br/>" + superCategory.getDescription() + "</html>";

            final PopupButton popupButton = new PopupButton(name);
            applyMenuPopupButttonStyling(popupButton);

            DCPopupBubble popupBubble = new DCPopupBubble(_glassPane, description, 0, 0,
                    IconUtils.getComponentSuperCategoryIcon(superCategory));
            popupBubble.attachTo(popupButton);

            final JPopupMenu menu = popupButton.getMenu();

            final DescriptorMenuBuilder menuBuilder = new DescriptorMenuBuilder(_analysisJobBuilder, _usageLogger,
                    superCategory, null);
            menuBuilder.addItemsToPopupMenu(menu);

            toolBar.add(popupButton);
            _superCategoryButtons.add(popupButton);
        }
    }

    private JToggleButton createMoreMenuButton() {
        final JMenuItem optionsMenuItem = WidgetFactory.createMenuItem("Options", IconUtils.MENU_OPTIONS);
        optionsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsDialog optionsDialog = _optionsDialogProvider.get();
                optionsDialog.open();
            }
        });

        final JMenuItem monitorMenuItem = WidgetFactory
                .createMenuItem("DataCleaner monitor", IconUtils.MENU_DQ_MONITOR);
        monitorMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MonitorConnectionDialog dialog = _monitorConnectionDialogProvider.get();
                dialog.open();
            }
        });

        final JMenuItem dictionariesMenuItem = WidgetFactory.createMenuItem("Dictionaries",
                IconUtils.DICTIONARY_IMAGEPATH);
        dictionariesMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReferenceDataDialog referenceDataDialog = _referenceDataDialogProvider.get();
                referenceDataDialog.selectDictionariesTab();
                referenceDataDialog.open();
            }
        });

        final JMenuItem synonymCatalogsMenuItem = WidgetFactory.createMenuItem("Synonyms",
                IconUtils.SYNONYM_CATALOG_IMAGEPATH);
        synonymCatalogsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReferenceDataDialog referenceDataDialog = _referenceDataDialogProvider.get();
                referenceDataDialog.selectSynonymsTab();
                referenceDataDialog.open();
            }
        });

        final JMenuItem stringPatternsMenuItem = WidgetFactory.createMenuItem("String patterns",
                IconUtils.STRING_PATTERN_IMAGEPATH);
        stringPatternsMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ReferenceDataDialog referenceDataDialog = _referenceDataDialogProvider.get();
                referenceDataDialog.selectStringPatternsTab();
                referenceDataDialog.open();
            }
        });

        final PopupButton popupButton = new PopupButton("More",
                imageManager.getImageIcon(IconUtils.ACTION_SCROLLDOWN_BRIGHT));
        applyMenuPopupButttonStyling(popupButton);

        final JMenu windowsMenuItem = WidgetFactory.createMenu("Windows", 'w');
        windowsMenuItem.setIcon(imageManager.getImageIcon("images/menu/windows.png", IconUtils.ICON_SIZE_SMALL));
        final List<DCWindow> windows = getWindowContext().getWindows();

        getWindowContext().addWindowListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                windowsMenuItem.removeAll();
                for (final DCWindow window : windows) {
                    final Image windowIcon = window.getWindowIcon();
                    final String title = window.getWindowTitle();
                    final ImageIcon icon = new ImageIcon(windowIcon.getScaledInstance(IconUtils.ICON_SIZE_SMALL,
                            IconUtils.ICON_SIZE_SMALL, Image.SCALE_DEFAULT));
                    final JMenuItem switchToWindowItem = WidgetFactory.createMenuItem(title, icon);
                    switchToWindowItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            window.toFront();
                        }
                    });
                    windowsMenuItem.add(switchToWindowItem);
                }

                windowsMenuItem.add(new JSeparator());

                JMenuItem closeAllWindowsItem = WidgetFactory.createMenuItem("Close all dialogs", (ImageIcon) null);
                closeAllWindowsItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        List<DCWindow> windows = new ArrayList<>(getWindowContext().getWindows());
                        for (DCWindow window : windows) {
                            if (window instanceof AbstractDialog) {
                                window.close();
                            }
                        }
                    }
                });
                windowsMenuItem.add(closeAllWindowsItem);
            }
        });

        popupButton.getMenu().removeAll();
        popupButton.getMenu().add(dictionariesMenuItem);
        popupButton.getMenu().add(synonymCatalogsMenuItem);
        popupButton.getMenu().add(stringPatternsMenuItem);
        popupButton.getMenu().add(new JSeparator());
        popupButton.getMenu().add(windowsMenuItem);
        popupButton.getMenu().add(new JSeparator());
        popupButton.getMenu().add(monitorMenuItem);
        popupButton.getMenu().add(optionsMenuItem);

        return popupButton;
    }

    private void applyMenuPopupButttonStyling(PopupButton popupButton) {
        popupButton.setBorder(new EmptyBorder(10, 4, 10, 4));
        popupButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        popupButton.setFocusPainted(false);
        WidgetUtils.setDarkButtonStyle(popupButton);
        popupButton.setHorizontalTextPosition(SwingConstants.LEFT);
    }

    private JToggleButton createViewToggleButton(final String text, final String iconPath) {
        final ImageIcon icon = imageManager.getImageIcon(iconPath);
        final JToggleButton button = new JToggleButton(text, icon);
        button.setFont(WidgetUtils.FONT_SMALL);
        button.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        button.setBackground(WidgetUtils.BG_COLOR_DARK);
        button.setBorderPainted(false);
        button.setBorder(new CompoundBorder(WidgetUtils.BORDER_THIN, new EmptyBorder(0, 4, 0, 4)));
        return button;
    }

    /**
     * Applies property values for all components visible in the window.
     */
    @Override
    public void applyPropertyValues() {
        if (_classicView != null) {
            _classicView.applyPropertyValues();
        }
    }

    private void onSourceColumnsChanged() {
        boolean everythingEnabled = !_analysisJobBuilder.getSourceColumns().isEmpty();

        if (_classicView != null) {
            _classicView.onSourceColumnsChanged(everythingEnabled);
        }

        _saveButton.setEnabled(everythingEnabled);
        _saveAsButton.setEnabled(everythingEnabled);

        for (PopupButton superCategoryButton : _superCategoryButtons) {
            superCategoryButton.setEnabled(everythingEnabled);
        }
    }

    @Override
    public void setDatastoreSelectionEnabled(boolean datastoreSelectionEnabled) {
        _datastoreSelectionEnabled = datastoreSelectionEnabled;
    }

    @Override
    public boolean isDatastoreSelectionEnabled() {
        return _datastoreSelectionEnabled;
    }

    @Override
    protected boolean isCentered() {
        return true;
    }

    private AnalyzerChangeListener createAnalyzerChangeListener() {
        return new AnalyzerChangeListener() {

            @Override
            public void onAdd(final AnalyzerComponentBuilder<?> analyzerJobBuilder) {
                if (_classicView != null) {
                    _classicView.addAnalyzer(analyzerJobBuilder);
                }
                updateStatusLabel();
                _graph.refresh();
            }

            @Override
            public void onRemove(AnalyzerComponentBuilder<?> analyzerJobBuilder) {
                if (_classicView != null) {
                    _classicView.removeAnalyzer(analyzerJobBuilder);
                }
                updateStatusLabel();
                _graph.refresh();
            }

            @Override
            public void onConfigurationChanged(AnalyzerComponentBuilder<?> analyzerJobBuilder) {
                updateStatusLabel();
                _graph.refresh();
            }

            @Override
            public void onRequirementChanged(AnalyzerComponentBuilder<?> analyzerJobBuilder) {
                _graph.refresh();
            }
        };
    }

    private TransformerChangeListener createTransformerChangeListener() {
        return new TransformerChangeListener() {

            @Override
            public void onAdd(final TransformerComponentBuilder<?> transformerJobBuilder) {
                if (_classicView != null) {
                    _classicView.addTransformer(transformerJobBuilder);
                }
                updateStatusLabel();
                _graph.refresh();
            }

            @Override
            public void onRemove(TransformerComponentBuilder<?> transformerJobBuilder) {
                if (_classicView != null) {
                    _classicView.removeTransformer(transformerJobBuilder);
                }
                updateStatusLabel();
                _graph.refresh();
            }

            @Override
            public void onOutputChanged(TransformerComponentBuilder<?> transformerJobBuilder,
                    List<MutableInputColumn<?>> outputColumns) {
                _graph.refresh();
            }

            @Override
            public void onRequirementChanged(TransformerComponentBuilder<?> transformerJobBuilder) {
                _graph.refresh();
            }

            @Override
            public void onConfigurationChanged(TransformerComponentBuilder<?> transformerJobBuilder) {
                updateStatusLabel();
                _graph.refresh();
            }
        };
    }

    private FilterChangeListener createFilterChangeListener() {
        return new FilterChangeListener() {

            @Override
            public void onAdd(final FilterComponentBuilder<?, ?> filterJobBuilder) {
                if (_classicView != null) {
                    _classicView.addFilter(filterJobBuilder);
                }
                updateStatusLabel();
                _graph.refresh();
            }

            @Override
            public void onRemove(FilterComponentBuilder<?, ?> filterJobBuilder) {
                if (_classicView != null) {
                    _classicView.removeFilter(filterJobBuilder);
                }
                updateStatusLabel();
                _graph.refresh();
            }

            @Override
            public void onConfigurationChanged(FilterComponentBuilder<?, ?> filterJobBuilder) {
                updateStatusLabel();
                _graph.refresh();
            }

            @Override
            public void onRequirementChanged(FilterComponentBuilder<?, ?> filterJobBuilder) {
                _graph.refresh();
            }
        };
    }

    private SourceColumnChangeListener createSourceColumnChangeListener() {
        return new SourceColumnChangeListener() {

            @Override
            public void onAdd(InputColumn<?> sourceColumn) {
                onSourceColumnsChanged();
                updateStatusLabel();
                _graph.refresh();
            }

            @Override
            public void onRemove(InputColumn<?> sourceColumn) {
                onSourceColumnsChanged();
                updateStatusLabel();
                _graph.refresh();
            }
        };
    }

    @Override
    public AnalysisJobBuilder getAnalysisJobBuilder() {
        return _analysisJobBuilder;
    }
}
