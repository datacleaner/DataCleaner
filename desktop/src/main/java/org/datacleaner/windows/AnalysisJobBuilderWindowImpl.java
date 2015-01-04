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
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.vfs2.FileObject;
import org.datacleaner.beans.api.Renderer;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.AbstractBeanJobBuilder;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerChangeListener;
import org.datacleaner.job.builder.AnalyzerJobBuilder;
import org.datacleaner.job.builder.FilterChangeListener;
import org.datacleaner.job.builder.FilterJobBuilder;
import org.datacleaner.job.builder.SourceColumnChangeListener;
import org.datacleaner.job.builder.TransformerChangeListener;
import org.datacleaner.job.builder.TransformerJobBuilder;
import org.datacleaner.job.builder.UnconfiguredConfiguredPropertyException;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.StringUtils;
import org.datacleaner.Version;
import org.datacleaner.actions.AnalyzeButtonActionListener;
import org.datacleaner.actions.HideTabTextActionListener;
import org.datacleaner.actions.ComponentBuilderTabTextActionListener;
import org.datacleaner.actions.OpenAnalysisJobActionListener;
import org.datacleaner.actions.RenameComponentActionListener;
import org.datacleaner.actions.RunAnalysisActionListener;
import org.datacleaner.actions.SaveAnalysisJobActionListener;
import org.datacleaner.actions.TransformButtonActionListener;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.guice.JobFile;
import org.datacleaner.guice.Nullable;
import org.datacleaner.panels.AbstractJobBuilderPanel;
import org.datacleaner.panels.AnalyzerJobBuilderPresenter;
import org.datacleaner.panels.ComponentJobBuilderPresenter;
import org.datacleaner.panels.ComponentJobBuilderRenderingFormat;
import org.datacleaner.panels.DCGlassPane;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.panels.ExecuteJobWithoutAnalyzersDialog;
import org.datacleaner.panels.FilterJobBuilderPresenter;
import org.datacleaner.panels.MetadataPanel;
import org.datacleaner.panels.SchemaTreePanel;
import org.datacleaner.panels.SourceColumnsPanel;
import org.datacleaner.panels.TransformerJobBuilderPresenter;
import org.datacleaner.panels.WelcomePanel;
import org.datacleaner.panels.maxrows.MaxRowsFilterShortcutPanel;
import org.datacleaner.user.UsageLogger;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.CollapsibleTreePanel;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DCPersistentSizedPanel;
import org.datacleaner.widgets.DCPopupBubble;
import org.datacleaner.widgets.DCWindowMenuBar;
import org.datacleaner.widgets.LicenceAndEditionStatusLabel;
import org.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.datacleaner.widgets.tabs.TabCloseEvent;
import org.datacleaner.widgets.tabs.TabCloseListener;
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
public final class AnalysisJobBuilderWindowImpl extends AbstractWindow implements AnalysisJobBuilderWindow,
        AnalyzerChangeListener, TransformerChangeListener, FilterChangeListener, SourceColumnChangeListener,
        TabCloseListener {

    private static final String USER_PREFERENCES_PROPERTY_EDITING_MODE_PREFERENCE = "editing_mode_preference";

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(AnalysisJobBuilderWindow.class);
    private static final ImageManager imageManager = ImageManager.get();

    private static final int TAB_ICON_SIZE = IconUtils.ICON_SIZE_LARGE;

    private static final int DEFAULT_WINDOW_WIDTH = 1000;
    private static final int DEFAULT_WINDOW_HEIGHT = 710;

    private static final int SOURCE_TAB = 0;
    private static final int METADATA_TAB = 1;

    private final Map<AnalyzerJobBuilder<?>, AnalyzerJobBuilderPresenter> _analyzerPresenters = new LinkedHashMap<AnalyzerJobBuilder<?>, AnalyzerJobBuilderPresenter>();
    private final Map<TransformerJobBuilder<?>, TransformerJobBuilderPresenter> _transformerPresenters = new LinkedHashMap<TransformerJobBuilder<?>, TransformerJobBuilderPresenter>();
    private final Map<FilterJobBuilder<?, ?>, FilterJobBuilderPresenter> _filterPresenters = new LinkedHashMap<FilterJobBuilder<?, ?>, FilterJobBuilderPresenter>();
    private final Map<ComponentJobBuilderPresenter, JComponent> _jobBuilderTabs = new HashMap<ComponentJobBuilderPresenter, JComponent>();
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final AnalyzerBeansConfiguration _configuration;
    private final RendererFactory _presenterRendererFactory;
    private final CloseableTabbedPane _tabbedPane;
    private final DCLabel _statusLabel = DCLabel.bright("");
    private final CollapsibleTreePanel _leftPanel;
    private final SourceColumnsPanel _sourceColumnsPanel;
    private final SchemaTreePanel _schemaTreePanel;
    private final JButton _saveButton;
    private final JButton _saveAsButton;
    private final JButton _transformButton;
    private final JButton _analyzeButton;
    private final JButton _executeButton;
    private final Provider<RunAnalysisActionListener> _runAnalysisActionProvider;
    private final Provider<SaveAnalysisJobActionListener> _saveAnalysisJobActionListenerProvider;
    private final Provider<AnalyzeButtonActionListener> _addAnalyzerActionListenerProvider;
    private final Provider<TransformButtonActionListener> _addTransformerActionListenerProvider;
    private final DCGlassPane _glassPane;
    private final WelcomePanel _welcomePanel;
    private final UserPreferences _userPreferences;
    private final InjectorBuilder _injectorBuilder;
    private final DCWindowMenuBar _windowMenuBar;
    private final JToggleButton _classicViewButton;
    private final JToggleButton _graphViewButton;
    private final MetadataPanel _metadataPanel;
    private final JobGraph _graph;
    private final DCPanel _contentContainerPanel;
    private final JComponent _editingContentView;
    private volatile AbstractJobBuilderPanel _latestPanel = null;
    private FileObject _jobFilename;
    private Datastore _datastore;
    private DatastoreConnection _datastoreConnection;
    private boolean _datastoreSelectionEnabled;

    @Inject
    protected AnalysisJobBuilderWindowImpl(AnalyzerBeansConfiguration configuration, WindowContext windowContext,
            SchemaTreePanel schemaTreePanel, SourceColumnsPanel sourceColumnsPanel,
            Provider<RunAnalysisActionListener> runAnalysisActionProvider, MetadataPanel metadataPanel,
            AnalysisJobBuilder analysisJobBuilder, InjectorBuilder injectorBuilder, UserPreferences userPreferences,
            @Nullable @JobFile FileObject jobFilename, DCWindowMenuBar windowMenuBar,
            Provider<SaveAnalysisJobActionListener> saveAnalysisJobActionListenerProvider,
            Provider<AnalyzeButtonActionListener> addAnalyzerActionListenerProvider,
            Provider<TransformButtonActionListener> addTransformerActionListenerProvider, UsageLogger usageLogger,
            Provider<OptionsDialog> optionsDialogProvider, OpenAnalysisJobActionListener openAnalysisJobActionListener,
            DatabaseDriverCatalog databaseDriverCatalog) {
        super(windowContext);
        _jobFilename = jobFilename;
        _configuration = configuration;
        _windowMenuBar = windowMenuBar;
        _runAnalysisActionProvider = runAnalysisActionProvider;
        _saveAnalysisJobActionListenerProvider = saveAnalysisJobActionListenerProvider;
        _addAnalyzerActionListenerProvider = addAnalyzerActionListenerProvider;
        _addTransformerActionListenerProvider = addTransformerActionListenerProvider;
        _userPreferences = userPreferences;

        if (analysisJobBuilder == null) {
            _analysisJobBuilder = new AnalysisJobBuilder(_configuration);
        } else {
            _analysisJobBuilder = analysisJobBuilder;
            DatastoreConnection con = _analysisJobBuilder.getDatastoreConnection();
            if (con != null) {
                _datastore = con.getDatastore();
            }
        }
        _windowMenuBar.setAnalysisJobBuilder(_analysisJobBuilder);

        _datastoreSelectionEnabled = true;
        _presenterRendererFactory = new RendererFactory(configuration);
        _glassPane = new DCGlassPane(this);
        _injectorBuilder = injectorBuilder;

        _graph = new JobGraph(windowContext, _analysisJobBuilder, _presenterRendererFactory, usageLogger);
        final DCPanel graphPanel = _graph.getPanel();

        _analysisJobBuilder.getAnalyzerChangeListeners().add(this);
        _analysisJobBuilder.getTransformerChangeListeners().add(this);
        _analysisJobBuilder.getFilterChangeListeners().add(this);
        _analysisJobBuilder.getSourceColumnListeners().add(this);

        _saveButton = createToolBarButton("Save", imageManager.getImageIcon("images/actions/save.png"));
        _saveAsButton = createToolBarButton("Save As...", imageManager.getImageIcon("images/actions/save.png"));

        _transformButton = createToolbarButton(
                "Transform",
                IconUtils.TRANSFORMER_IMAGEPATH,
                "<html><b>Transformers and filters</b><br/>Preprocess or filter your data in order to extract, limit, combine or generate separate values.</html>");
        _analyzeButton = createToolbarButton("Analyze", IconUtils.ANALYZER_IMAGEPATH,
                "<html><b>Analyzers</b><br/>Analyzers provide Data Quality analysis and profiling operations.</html>");
        _executeButton = createToolBarButton("Execute", imageManager.getImageIcon(IconUtils.ACTION_EXECUTE));

        _welcomePanel = new WelcomePanel(configuration, this, _glassPane, optionsDialogProvider, injectorBuilder,
                openAnalysisJobActionListener, databaseDriverCatalog, userPreferences);
        _welcomePanel.setBorder(new EmptyBorder(4, 4, 0, 20));

        _sourceColumnsPanel = sourceColumnsPanel;

        _tabbedPane = new CloseableTabbedPane(false);
        _tabbedPane.addTabCloseListener(this);
        _tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public synchronized void stateChanged(ChangeEvent e) {
                if (_latestPanel != null) {
                    _latestPanel.applyPropertyValues(false);
                }
                Component selectedComponent = _tabbedPane.getSelectedComponent();
                if (selectedComponent instanceof AbstractJobBuilderPanel) {
                    _latestPanel = (AbstractJobBuilderPanel) selectedComponent;
                } else {
                    _latestPanel = null;
                }
                updateStatusLabel();
            }
        });

        _editingContentView = new DCPanel();
        _editingContentView.setLayout(new BorderLayout());
        _editingContentView.add(_tabbedPane, BorderLayout.CENTER);
        _editingContentView.add(graphPanel, BorderLayout.CENTER);

        _contentContainerPanel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
        _contentContainerPanel.setLayout(new BorderLayout());
        _contentContainerPanel.add(_welcomePanel, BorderLayout.NORTH);
        _contentContainerPanel.add(_editingContentView, BorderLayout.CENTER);

        final boolean graphPreferred = isGraphPreferred();

        if (graphPreferred) {
            setEditingView(graphPanel);
        } else {
            setEditingView(_tabbedPane);
        }

        _classicViewButton = createViewToggleButton("Classic view", _tabbedPane,
                "images/actions/editing-view-classic.png");
        _classicViewButton.setSelected(!graphPreferred);
        _graphViewButton = createViewToggleButton("Graph view", graphPanel, "images/actions/editing-view-graph.png");
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
        _metadataPanel = metadataPanel;

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

    private JButton createToolBarButton(String text, ImageIcon imageIcon) {
        final JButton button = new JButton(text, imageIcon);
        button.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        button.setFocusPainted(false);
        return button;
    }

    private JButton createToolbarButton(String text, String iconPath, String popupDescription) {
        JButton button = createToolBarButton(text, imageManager.getImageIcon(iconPath));
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

                _welcomePanel.requestSearchFieldFocus();

                _editingContentView.setVisible(false);
                _welcomePanel.setVisible(true);
            }
        }
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
                    AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder = unconfiguredConfiguredPropertyException
                            .getBeanJobBuilder();
                    errorMessage = "Property '" + configuredProperty.getName() + "' in "
                            + LabelUtils.getLabel(beanJobBuilder) + " is not set!";
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
        return imageManager.getImage("images/filetypes/analysis_job.png");
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

        setJMenuBar(_windowMenuBar);

        // add source tab
        _tabbedPane.addTab("Source", imageManager.getImageIcon("images/model/source.png", TAB_ICON_SIZE),
                WidgetUtils.scrolleable(_sourceColumnsPanel));
        _tabbedPane.setRightClickActionListener(SOURCE_TAB, new HideTabTextActionListener(_tabbedPane, SOURCE_TAB));
        _tabbedPane.setUnclosableTab(SOURCE_TAB);

        // add metadata tab
        _tabbedPane.addTab("Metadata", imageManager.getImageIcon("images/model/metadata.png", TAB_ICON_SIZE),
                _metadataPanel);
        _tabbedPane.setRightClickActionListener(METADATA_TAB, new HideTabTextActionListener(_tabbedPane, METADATA_TAB));
        _tabbedPane.setUnclosableTab(METADATA_TAB);

        // add separator for fixed vs dynamic tabs
        _tabbedPane.addSeparator();

        final SaveAnalysisJobActionListener saveAnalysisJobActionListener = _saveAnalysisJobActionListenerProvider
                .get();
        _saveButton.addActionListener(saveAnalysisJobActionListener);
        _saveAsButton.addActionListener(saveAnalysisJobActionListener);
        _saveAsButton.setActionCommand(SaveAnalysisJobActionListener.ACTION_COMMAND_SAVE_AS);

        // Transform button
        _transformButton.addActionListener(_addTransformerActionListenerProvider.get());

        // Analyze button
        _analyzeButton.addActionListener(_addAnalyzerActionListenerProvider.get());

        // Run analysis
        final RunAnalysisActionListener runAnalysisActionListener = _runAnalysisActionProvider.get();
        _executeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyPropertyValues();

                if (_analysisJobBuilder.getAnalyzerJobBuilders().isEmpty()) {
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

        final JToolBar toolBar = WidgetFactory.createToolBar();
        toolBar.add(_saveButton);
        toolBar.add(_saveAsButton);
        toolBar.add(WidgetFactory.createToolBarSeparator());
        toolBar.add(_transformButton);
        toolBar.add(_analyzeButton);
        toolBar.add(WidgetFactory.createToolBarSeparator());
        toolBar.add(_executeButton);

        final JXStatusBar statusBar = WidgetFactory.createStatusBar(_statusLabel);
        statusBar.add(_classicViewButton);
        statusBar.add(_graphViewButton);
        statusBar.add(Box.createHorizontalStrut(10));

        final LicenceAndEditionStatusLabel statusLabel = new LicenceAndEditionStatusLabel(_glassPane);
        statusBar.add(statusLabel);

        final DCPanel toolBarPanel = new DCPanel(WidgetUtils.BG_COLOR_LESS_DARK, WidgetUtils.BG_COLOR_DARK);
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

        initializeExistingComponents();

        return panel;
    }

    private JToggleButton createViewToggleButton(final String text, final JComponent editingContentView,
            final String iconPath) {
        final ImageIcon icon = imageManager.getImageIcon(iconPath);
        final JToggleButton button = new JToggleButton(text, icon);
        button.setFont(WidgetUtils.FONT_SMALL);
        button.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        button.setBackground(WidgetUtils.BG_COLOR_DARK);
        button.setBorderPainted(false);
        button.setBorder(new CompoundBorder(WidgetUtils.BORDER_THIN, new EmptyBorder(0, 4, 0, 4)));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setEditingView(editingContentView);
            }
        });
        return button;
    }

    /**
     * Applies property values for all components visible in the window.
     */
    @Override
    public void applyPropertyValues() {
        for (FilterJobBuilderPresenter presenter : _filterPresenters.values()) {
            presenter.applyPropertyValues();
        }

        for (TransformerJobBuilderPresenter presenter : _transformerPresenters.values()) {
            presenter.applyPropertyValues();
        }

        for (AnalyzerJobBuilderPresenter presenter : _analyzerPresenters.values()) {
            presenter.applyPropertyValues();
        }
    }

    /**
     * Method used to initialize any components that may be in the
     * AnalysisJobBuilder before this window has been created. Typically this
     * will only happen when opening a saved job.
     */
    private void initializeExistingComponents() {
        List<FilterJobBuilder<?, ?>> filterJobBuilders = _analysisJobBuilder.getFilterJobBuilders();
        for (FilterJobBuilder<?, ?> fjb : filterJobBuilders) {
            onAdd(fjb);
        }

        List<TransformerJobBuilder<?>> transformerJobBuilders = _analysisJobBuilder.getTransformerJobBuilders();
        for (TransformerJobBuilder<?> tjb : transformerJobBuilders) {
            onAdd(tjb);
        }

        List<AnalyzerJobBuilder<?>> analyzerJobBuilders = _analysisJobBuilder.getAnalyzerJobBuilders();
        for (AnalyzerJobBuilder<?> ajb : analyzerJobBuilders) {
            onAdd((AnalyzerJobBuilder<?>) ajb);
        }

        onSourceColumnsChanged();
    }

    private void onSourceColumnsChanged() {
        boolean everythingEnabled = true;

        if (_analysisJobBuilder.getSourceColumns().isEmpty()) {
            _tabbedPane.setSelectedIndex(SOURCE_TAB);
            everythingEnabled = false;
        }

        int tabCount = _tabbedPane.getTabCount();
        for (int i = 1; i < tabCount; i++) {
            _tabbedPane.setEnabledAt(i, everythingEnabled);
        }
        _saveButton.setEnabled(everythingEnabled);
        _saveAsButton.setEnabled(everythingEnabled);
        _transformButton.setEnabled(everythingEnabled);
        _analyzeButton.setEnabled(everythingEnabled);
        _windowMenuBar.getWriteDataMenu().setEnabled(everythingEnabled);
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

    @Override
    public void tabClosed(TabCloseEvent ev) {
        Component panel = ev.getTabContents();

        if (panel != null) {
            // if panel was a row processing analyzer panel
            for (Iterator<AnalyzerJobBuilderPresenter> it = _analyzerPresenters.values().iterator(); it.hasNext();) {
                AnalyzerJobBuilderPresenter analyzerPresenter = it.next();
                if (_jobBuilderTabs.get(analyzerPresenter) == panel) {
                    _analysisJobBuilder.removeAnalyzer(analyzerPresenter.getJobBuilder());
                    return;
                }
            }

            // if panel was a transformer panel
            for (Iterator<TransformerJobBuilderPresenter> it = _transformerPresenters.values().iterator(); it.hasNext();) {
                TransformerJobBuilderPresenter transformerPresenter = it.next();
                if (_jobBuilderTabs.get(transformerPresenter) == panel) {
                    _analysisJobBuilder.removeTransformer(transformerPresenter.getJobBuilder());
                    return;
                }
            }

            // if panel was a filter panel
            for (Iterator<FilterJobBuilderPresenter> it = _filterPresenters.values().iterator(); it.hasNext();) {
                FilterJobBuilderPresenter filterPresenter = it.next();
                if (_jobBuilderTabs.get(filterPresenter) == panel) {
                    _analysisJobBuilder.removeFilter(filterPresenter.getJobBuilder());
                    return;
                }
            }
        }
        logger.info("Could not handle removal of tab {}, containing {}", ev.getTabIndex(), panel);
    }

    @Override
    public void onAdd(final AnalyzerJobBuilder<?> analyzerJobBuilder) {
        @SuppressWarnings("unchecked")
        final Renderer<AnalyzerJobBuilder<?>, ? extends ComponentJobBuilderPresenter> renderer = (Renderer<AnalyzerJobBuilder<?>, ? extends ComponentJobBuilderPresenter>) _presenterRendererFactory
                .getRenderer(analyzerJobBuilder, ComponentJobBuilderRenderingFormat.class);
        AnalyzerJobBuilderPresenter presenter = (AnalyzerJobBuilderPresenter) renderer.render(analyzerJobBuilder);

        _analyzerPresenters.put(analyzerJobBuilder, presenter);
        JComponent comp = presenter.createJComponent();
        _tabbedPane.addTab(LabelUtils.getLabel(analyzerJobBuilder),
                IconUtils.getDescriptorIcon(analyzerJobBuilder.getDescriptor(), TAB_ICON_SIZE), comp);
        _jobBuilderTabs.put(presenter, comp);
        final int tabIndex = _tabbedPane.getTabCount() - 1;
        _tabbedPane.setRightClickActionListener(tabIndex, new ComponentBuilderTabTextActionListener(_analysisJobBuilder,
                analyzerJobBuilder, tabIndex, _tabbedPane));
        _tabbedPane.setDoubleClickActionListener(tabIndex, new RenameComponentActionListener(analyzerJobBuilder) {
            @Override
            protected void onNameChanged() {
                _tabbedPane.setTitleAt(tabIndex, LabelUtils.getLabel(analyzerJobBuilder));
            }
        });

        _tabbedPane.setSelectedIndex(tabIndex);
        updateStatusLabel();
        _graph.refresh();
    }

    @Override
    public void onRemove(AnalyzerJobBuilder<?> analyzerJobBuilder) {
        AnalyzerJobBuilderPresenter presenter = _analyzerPresenters.remove(analyzerJobBuilder);
        JComponent comp = _jobBuilderTabs.remove(presenter);
        _tabbedPane.remove(comp);
        updateStatusLabel();
        _graph.refresh();
    }

    @Override
    public void onAdd(final TransformerJobBuilder<?> transformerJobBuilder) {
        @SuppressWarnings("unchecked")
        final Renderer<TransformerJobBuilder<?>, ? extends ComponentJobBuilderPresenter> renderer = (Renderer<TransformerJobBuilder<?>, ? extends ComponentJobBuilderPresenter>) _presenterRendererFactory
                .getRenderer(transformerJobBuilder, ComponentJobBuilderRenderingFormat.class);
        final TransformerJobBuilderPresenter presenter = (TransformerJobBuilderPresenter) renderer
                .render(transformerJobBuilder);

        _transformerPresenters.put(transformerJobBuilder, presenter);
        final JComponent comp = presenter.createJComponent();
        _tabbedPane.addTab(LabelUtils.getLabel(transformerJobBuilder),
                IconUtils.getDescriptorIcon(transformerJobBuilder.getDescriptor(), TAB_ICON_SIZE), comp);
        _jobBuilderTabs.put(presenter, comp);
        final int tabIndex = _tabbedPane.getTabCount() - 1;
        _tabbedPane.setSelectedIndex(tabIndex);
        _tabbedPane.setRightClickActionListener(tabIndex, new ComponentBuilderTabTextActionListener(_analysisJobBuilder,
                transformerJobBuilder, tabIndex, _tabbedPane));
        _tabbedPane.setDoubleClickActionListener(tabIndex, new RenameComponentActionListener(transformerJobBuilder) {
            @Override
            protected void onNameChanged() {
                _tabbedPane.setTitleAt(tabIndex, LabelUtils.getLabel(transformerJobBuilder));
            }
        });
        updateStatusLabel();
        _graph.refresh();
    }

    @Override
    public void onRemove(TransformerJobBuilder<?> transformerJobBuilder) {
        TransformerJobBuilderPresenter presenter = _transformerPresenters.remove(transformerJobBuilder);
        JComponent comp = _jobBuilderTabs.remove(presenter);
        _tabbedPane.remove(comp);
        updateStatusLabel();
        _graph.refresh();
    }

    @Override
    public void onOutputChanged(TransformerJobBuilder<?> transformerJobBuilder,
            List<MutableInputColumn<?>> outputColumns) {
        _graph.refresh();
    }

    @Override
    public void onAdd(final FilterJobBuilder<?, ?> filterJobBuilder) {
        @SuppressWarnings("unchecked")
        final Renderer<FilterJobBuilder<?, ?>, ? extends ComponentJobBuilderPresenter> renderer = (Renderer<FilterJobBuilder<?, ?>, ? extends ComponentJobBuilderPresenter>) _presenterRendererFactory
                .getRenderer(filterJobBuilder, ComponentJobBuilderRenderingFormat.class);
        final FilterJobBuilderPresenter presenter = (FilterJobBuilderPresenter) renderer.render(filterJobBuilder);

        _filterPresenters.put(filterJobBuilder, presenter);
        JComponent comp = presenter.createJComponent();
        _tabbedPane.addTab(LabelUtils.getLabel(filterJobBuilder),
                IconUtils.getDescriptorIcon(filterJobBuilder.getDescriptor(), TAB_ICON_SIZE), comp);
        _jobBuilderTabs.put(presenter, comp);
        final int tabIndex = _tabbedPane.getTabCount() - 1;
        if (MaxRowsFilterShortcutPanel.isFilter(filterJobBuilder)) {
            // the max rows shortcut must be disabled using checkbox on source
            // tab
            _tabbedPane.setUnclosableTab(tabIndex);
        } else {
            _tabbedPane.setSelectedIndex(tabIndex);
            _tabbedPane.setRightClickActionListener(tabIndex, new ComponentBuilderTabTextActionListener(_analysisJobBuilder,
                    filterJobBuilder, tabIndex, _tabbedPane));
            _tabbedPane.setDoubleClickActionListener(tabIndex, new RenameComponentActionListener(filterJobBuilder) {
                @Override
                protected void onNameChanged() {
                    _tabbedPane.setTitleAt(tabIndex, LabelUtils.getLabel(filterJobBuilder));
                }
            });
        }
        updateStatusLabel();
        _graph.refresh();
    }

    @Override
    public void onRemove(FilterJobBuilder<?, ?> filterJobBuilder) {
        FilterJobBuilderPresenter presenter = _filterPresenters.remove(filterJobBuilder);
        JComponent comp = _jobBuilderTabs.remove(presenter);
        _tabbedPane.remove(comp);

        if (MaxRowsFilterShortcutPanel.isFilter(filterJobBuilder)) {
            _sourceColumnsPanel.getMaxRowsFilterShortcutPanel().resetToDefault();
        }
        updateStatusLabel();
        _graph.refresh();
    }

    @Override
    public void onConfigurationChanged(FilterJobBuilder<?, ?> filterJobBuilder) {
        updateStatusLabel();
        _graph.refresh();
    }

    @Override
    public void onRequirementChanged(FilterJobBuilder<?, ?> filterJobBuilder) {
        _graph.refresh();
    }

    @Override
    public void onConfigurationChanged(TransformerJobBuilder<?> transformerJobBuilder) {
        updateStatusLabel();
        _graph.refresh();
    }

    @Override
    public void onRequirementChanged(TransformerJobBuilder<?> transformerJobBuilder) {
        _graph.refresh();
    }

    @Override
    public void onConfigurationChanged(AnalyzerJobBuilder<?> analyzerJobBuilder) {
        updateStatusLabel();
        _graph.refresh();
    }

    @Override
    public void onRequirementChanged(AnalyzerJobBuilder<?> analyzerJobBuilder) {
        _graph.refresh();
    }

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

    @Override
    public AnalysisJobBuilder getAnalysisJobBuilder() {
        return _analysisJobBuilder;
    }
}
