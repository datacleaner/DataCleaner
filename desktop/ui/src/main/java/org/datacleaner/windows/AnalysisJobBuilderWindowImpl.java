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
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.Version;
import org.datacleaner.actions.NewAnalysisJobActionListener;
import org.datacleaner.actions.OpenAnalysisJobActionListener;
import org.datacleaner.actions.RunAnalysisActionListener;
import org.datacleaner.actions.SaveAnalysisJobActionListener;
import org.datacleaner.api.InputColumn;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.components.maxrows.MaxRowsFilter;
import org.datacleaner.components.maxrows.MaxRowsFilter.Category;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.guice.DCModule;
import org.datacleaner.guice.JobFile;
import org.datacleaner.guice.Nullable;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentValidationException;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.JaxbJobWriter;
import org.datacleaner.job.SimpleComponentRequirement;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalysisJobChangeListener;
import org.datacleaner.job.builder.AnalyzerChangeListener;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.FilterChangeListener;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.SourceColumnChangeListener;
import org.datacleaner.job.builder.TransformerChangeListener;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.builder.UnconfiguredConfiguredPropertyException;
import org.datacleaner.job.concurrent.SingleThreadedTaskRunner;
import org.datacleaner.panels.DCGlassPane;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.panels.DatastoreManagementPanel;
import org.datacleaner.panels.ExecuteJobWithoutAnalyzersDialog;
import org.datacleaner.panels.SchemaTreePanel;
import org.datacleaner.panels.SelectDatastoreContainerPanel;
import org.datacleaner.panels.WelcomePanel;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UsageLogger;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.util.WindowSizePreferences;
import org.datacleaner.widgets.CollapsibleTreePanel;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DCPersistentSizedPanel;
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
public final class AnalysisJobBuilderWindowImpl extends AbstractWindow implements AnalysisJobBuilderWindow,
        WindowListener {

    private class WindowAnalysisJobChangeListener implements AnalysisJobChangeListener {
        @Override
        public void onActivation(final AnalysisJobBuilder builder) {
            builder.addAnalyzerChangeListener(_analyzerChangeListener);
            builder.addTransformerChangeListener(_transformerChangeListener);
            builder.addFilterChangeListener(_filterChangeListener);
            builder.addSourceColumnChangeListener(_sourceColumnChangeListener);
            builder.addAnalysisJobChangeListener(this);

            // We'll need to listen to already added output data stream job
            // builders
            for (AnalysisJobBuilder analysisJobBuilder : builder.getConsumedOutputDataStreamsJobBuilders()) {
                onActivation(analysisJobBuilder);
            }
        }

        @Override
        public void onDeactivation(final AnalysisJobBuilder builder) {
            builder.removeAnalyzerChangeListener(_analyzerChangeListener);
            builder.removeTransformerChangeListener(_transformerChangeListener);
            builder.removeFilterChangeListener(_filterChangeListener);
            builder.removeSourceColumnChangeListener(_sourceColumnChangeListener);
            builder.removeAnalysisJobChangeListener(this);
        }
    }

    private class WindowAnalyzerChangeListener implements AnalyzerChangeListener {
        @Override
        public void onAdd(final AnalyzerComponentBuilder<?> analyzerJobBuilder) {
            if (_classicView != null) {
                _classicView.addAnalyzer(analyzerJobBuilder);
            }
            updateStatusLabel();
            _graph.refresh();
        }

        @Override
        public void onRemove(final AnalyzerComponentBuilder<?> analyzerJobBuilder) {
            if (_classicView != null) {
                _classicView.removeAnalyzer(analyzerJobBuilder);
            }
            updateStatusLabel();
            _graph.refresh();
        }

        @Override
        public void onConfigurationChanged(final AnalyzerComponentBuilder<?> analyzerJobBuilder) {
            updateStatusLabel();
            _graph.refresh();
        }

        @Override
        public void onRequirementChanged(final AnalyzerComponentBuilder<?> analyzerJobBuilder) {
            _graph.refresh();
        }
    }

    private class WindowTransformerChangeListener implements TransformerChangeListener {

        @Override
        public void onAdd(final TransformerComponentBuilder<?> transformerJobBuilder) {
            if (_classicView != null) {
                _classicView.addTransformer(transformerJobBuilder);
            }
            updateStatusLabel();
            _graph.refresh();
        }

        @Override
        public void onRemove(final TransformerComponentBuilder<?> transformerJobBuilder) {
            if (_classicView != null) {
                _classicView.removeTransformer(transformerJobBuilder);
            }
            updateStatusLabel();
            _graph.refresh();
        }

        @Override
        public void onOutputChanged(final TransformerComponentBuilder<?> transformerJobBuilder,
                List<MutableInputColumn<?>> outputColumns) {
            _graph.refresh();
        }

        @Override
        public void onRequirementChanged(final TransformerComponentBuilder<?> transformerJobBuilder) {
            _graph.refresh();
        }

        @Override
        public void onConfigurationChanged(final TransformerComponentBuilder<?> transformerJobBuilder) {
            updateStatusLabel();
            _graph.refresh();
        }
    }

    private class WindowFilterChangeListener implements FilterChangeListener {

        @Override
        public void onAdd(final FilterComponentBuilder<?, ?> filterJobBuilder) {
            if (_classicView != null) {
                _classicView.addFilter(filterJobBuilder);
            }
            updateStatusLabel();
            _graph.refresh();
        }

        @Override
        public void onRemove(final FilterComponentBuilder<?, ?> filterJobBuilder) {
            if (_classicView != null) {
                _classicView.removeFilter(filterJobBuilder);
            }
            updateStatusLabel();
            _graph.refresh();
        }

        @Override
        public void onConfigurationChanged(final FilterComponentBuilder<?, ?> filterJobBuilder) {
            updateStatusLabel();
            _graph.refresh();
        }

        @Override
        public void onRequirementChanged(final FilterComponentBuilder<?, ?> filterJobBuilder) {
            _graph.refresh();
        }
    }

    private class WindowSourceColumnChangeListener implements SourceColumnChangeListener {

        @Override
        public void onAdd(final InputColumn<?> sourceColumn) {
            onSourceColumnsChanged();
            updateStatusLabel();
            _graph.refresh();
        }

        @Override
        public void onRemove(final InputColumn<?> sourceColumn) {
            onSourceColumnsChanged();
            updateStatusLabel();
            _graph.refresh();
        }
    }

    private static final String USER_PREFERENCES_PROPERTY_EDITING_MODE_PREFERENCE = "editing_mode_preference";

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(AnalysisJobBuilderWindow.class);
    private static final ImageManager imageManager = ImageManager.get();

    private static final int DEFAULT_WINDOW_WIDTH = 1000;
    private static final int DEFAULT_WINDOW_HEIGHT = 710;

    private final List<PopupButton> _superCategoryButtons = new ArrayList<>();
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final DataCleanerConfiguration _configuration;
    private final RendererFactory _presenterRendererFactory;
    private final DCLabel _statusLabel = DCLabel.bright("");
    private final CollapsibleTreePanel _leftPanel;
    private final SchemaTreePanel _schemaTreePanel;
    private final JButton _saveButton;
    private final JButton _saveAsButton;
    private final JButton _executeButton;
    private final JButton _executionAlternativesButton;
    private final Provider<SaveAnalysisJobActionListener> _saveAnalysisJobActionListenerProvider;
    private final Provider<NewAnalysisJobActionListener> _newAnalysisJobActionListenerProvider;
    private final Provider<OpenAnalysisJobActionListener> _openAnalysisJobActionListenerProvider;
    private final Provider<ReferenceDataDialog> _referenceDataDialogProvider;
    private final Provider<MonitorConnectionDialog> _monitorConnectionDialogProvider;
    private final Provider<OptionsDialog> _optionsDialogProvider;
    private final DCGlassPane _glassPane;
    private final WelcomePanel _welcomePanel;
    private final DatastoreManagementPanel _datastoreManagementPanel;
    private final SelectDatastoreContainerPanel _selectDatastorePanel;
    private final UserPreferences _userPreferences;
    private final DCModule _dcModule;
    private final JToggleButton _classicViewButton;
    private final JToggleButton _graphViewButton;
    private final JobGraph _graph;
    private final DCPanel _contentContainerPanel;
    private final JComponent _editingContentView;
    private final UsageLogger _usageLogger;
    private final AnalyzerChangeListener _analyzerChangeListener = new WindowAnalyzerChangeListener();
    private final TransformerChangeListener _transformerChangeListener = new WindowTransformerChangeListener();
    private final FilterChangeListener _filterChangeListener = new WindowFilterChangeListener();
    private final SourceColumnChangeListener _sourceColumnChangeListener = new WindowSourceColumnChangeListener();
    private final AnalysisJobChangeListener _analysisJobChangeListener = new WindowAnalysisJobChangeListener();
    private JobClassicView _classicView;
    private FileObject _jobFilename;
    private Datastore _datastore;
    private DatastoreConnection _datastoreConnection;
    private boolean _datastoreSelectionEnabled;
    private JComponent _windowContent;
    private WindowSizePreferences _windowSizePreference;
    private AnalysisWindowPanelType _currentPanelType;

    @Inject
    protected AnalysisJobBuilderWindowImpl(DataCleanerConfiguration configuration, WindowContext windowContext,
            SchemaTreePanel schemaTreePanel, AnalysisJobBuilder analysisJobBuilder, DCModule dcModule,
            UserPreferences userPreferences, @Nullable @JobFile FileObject jobFilename,
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
        _dcModule = dcModule;
        _newAnalysisJobActionListenerProvider = newAnalysisJobActionListenerProvider;
        _openAnalysisJobActionListenerProvider = openAnalysisJobActionListenerProvider;
        _saveAnalysisJobActionListenerProvider = saveAnalysisJobActionListenerProvider;
        _referenceDataDialogProvider = referenceDataDialogProvider;
        _monitorConnectionDialogProvider = monitorConnectionDialogProvider;
        _optionsDialogProvider = optionsDialogProvider;
        _userPreferences = userPreferences;
        _usageLogger = usageLogger;
        _windowSizePreference = new WindowSizePreferences(_userPreferences, getClass(), DEFAULT_WINDOW_WIDTH,
                DEFAULT_WINDOW_HEIGHT);

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

        _graph = new JobGraph(windowContext, userPreferences, analysisJobBuilder, _presenterRendererFactory,
                usageLogger);

        _analysisJobChangeListener.onActivation(_analysisJobBuilder);

        _saveButton = createToolbarButton("Save", IconUtils.ACTION_SAVE_BRIGHT);
        _saveAsButton = createToolbarButton("Save As...", IconUtils.ACTION_SAVE_BRIGHT);

        _executeButton = createToolbarButton("Execute", IconUtils.MENU_EXECUTE);
        _executionAlternativesButton = createToolbarButton("\uf0d7", null);
        _executionAlternativesButton.setFont(WidgetUtils.FONT_FONTAWESOME);

        _welcomePanel = new WelcomePanel(this, _userPreferences, _openAnalysisJobActionListenerProvider.get(),
                _dcModule);

        _datastoreManagementPanel = new DatastoreManagementPanel(_configuration, this, _glassPane,
                _optionsDialogProvider, _dcModule, databaseDriverCatalog, _userPreferences);
        _selectDatastorePanel = new SelectDatastoreContainerPanel(this, _dcModule, databaseDriverCatalog,
                (MutableDatastoreCatalog) configuration.getDatastoreCatalog(), _userPreferences);

        _editingContentView = new DCPanel();
        _editingContentView.setLayout(new BorderLayout());

        _contentContainerPanel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _contentContainerPanel.setLayout(new CardLayout());
        _contentContainerPanel.add(_welcomePanel, AnalysisWindowPanelType.WELCOME.getName());
        _contentContainerPanel.add(_editingContentView, AnalysisWindowPanelType.EDITING_CONTEXT.getName());
        _contentContainerPanel.add(_datastoreManagementPanel, AnalysisWindowPanelType.MANAGE_DS.getName());
        _contentContainerPanel.add(_selectDatastorePanel, AnalysisWindowPanelType.SELECT_DS.getName());

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

    @Override
    public void changePanel(AnalysisWindowPanelType panel) {
        if (_datastore == null) {
            _currentPanelType = panel;
        } else {
            _currentPanelType = AnalysisWindowPanelType.EDITING_CONTEXT;
        }
        updateCurrentPanel();
    }

    private void updateCurrentPanel() {
        ((CardLayout) _contentContainerPanel.getLayout()).show(_contentContainerPanel, _currentPanelType.getName());
        updateLeftPanelVisibility(_currentPanelType == AnalysisWindowPanelType.EDITING_CONTEXT);
        updateWindowTitle();
    }

    private boolean isGraphPreferred() {
        final Map<String, String> additionalProperties = _userPreferences.getAdditionalProperties();
        final String property = additionalProperties.get(USER_PREFERENCES_PROPERTY_EDITING_MODE_PREFERENCE);
        if ("Classic".equals(property)) {
            return false;
        }
        return true;
    }

    private JButton createToolbarButton(String text, String iconPath) {
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

        WidgetUtils.setDarkButtonStyle(button);
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
        _datastoreConnection = con;

        if (datastore == null) {
            _analysisJobBuilder.reset();
            changePanel(AnalysisWindowPanelType.WELCOME);
        } else {
            changePanel(AnalysisWindowPanelType.EDITING_CONTEXT);
            addTableToSource(con);
        }

        setSchemaTree(datastore, expandTree, con);
        updateStatusLabel();
    }

    private void setSchemaTree(final Datastore datastore, boolean expandTree, final DatastoreConnection con) {
        if (con != null) {
            final Schema defaultSchema = con.getSchemaNavigator().getDefaultSchema();
            final int datastoreSize = defaultSchema.getTables().length;
            if (datastoreSize == 1) {
                _schemaTreePanel.setDatastore(datastore, true);
            } else {
                _schemaTreePanel.setDatastore(datastore, expandTree);
            }
        }
    }

    private void addTableToSource(final DatastoreConnection con) {
        if (con != null) {
            final Schema defaultSchema = con.getSchemaNavigator().getDefaultSchema();
            final int datastoreSize = defaultSchema.getTables().length;
            if (datastoreSize == 1) {
                final Column[] columns = defaultSchema.getTable(0).getColumns();
                _analysisJobBuilder.addSourceColumns(columns);
            }
        }
    }

    private void updateLeftPanelVisibility(boolean show) {
        if (show) {
            _leftPanel.setVisible(true);
            if (_leftPanel.isCollapsed()) {
                _leftPanel.setCollapsed(false);
            }
        } else {
            if (!_leftPanel.isCollapsed()) {
                _leftPanel.setCollapsed(true);
            }
            final Timer timer = new Timer(500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (_leftPanel.isCollapsed()) {
                        _leftPanel.setVisible(false);
                    }
                }
            });
            timer.setRepeats(false);
            timer.start();
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
        changePanel(AnalysisWindowPanelType.WELCOME);
    }

    public void updateStatusLabel() {
        boolean executeable = false;

        if (_datastore == null) {
            setStatusLabelText("Welcome to DataCleaner " + Version.getDistributionVersion());
            _statusLabel.setIcon(imageManager.getImageIcon(IconUtils.APPLICATION_ICON, IconUtils.ICON_SIZE_SMALL));
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
                    final UnconfiguredConfiguredPropertyException unconfiguredConfiguredPropertyException = (UnconfiguredConfiguredPropertyException) ex;
                    final ConfiguredPropertyDescriptor configuredProperty = unconfiguredConfiguredPropertyException
                            .getConfiguredProperty();
                    final ComponentBuilder componentBuilder = unconfiguredConfiguredPropertyException
                            .getComponentBuilder();
                    errorMessage = "Property '" + configuredProperty.getName() + "' in "
                            + LabelUtils.getLabel(componentBuilder) + " is not set!";
                } else if (ex instanceof ComponentValidationException) {
                    executeable = false;
                    final ComponentValidationException componentValidationException = (ComponentValidationException) ex;
                    errorMessage = componentValidationException.getComponentDescriptor().getDisplayName()
                            + " validation failed: " + ex.getMessage();
                } else {
                    errorMessage = ex.getMessage();
                }
                setStatusLabelText("Job error status: " + errorMessage);
                setStatusLabelError();
            }
        }

        _executeButton.setEnabled(executeable);
        _executionAlternativesButton.setEnabled(executeable);
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

        switch (_currentPanelType) {
        case WELCOME:
            final int count = getWindowContext().getWindowCount(AnalysisJobBuilderWindow.class);
            if (count == 1) {
                if (getWindowContext().showExitDialog()) {
                    cleanupForWindowClose();
                    getWindowContext().exit();
                }
            } else {
                cleanupForWindowClose();
                return true;
            }
            break;
        case EDITING_CONTEXT:
            // if datastore is set and datastore selection is enabled,
            // return to datastore selection.

            if (isJobUnsaved(getJobFile(), _analysisJobBuilder) && (_saveButton.isEnabled())) {

                final Object[] buttons = { "Save changes", "Discard changes", "Cancel" };
                final int unsavedChangesChoice = JOptionPane.showOptionDialog(this,
                        "The job has unsaved changes. What would you like to do?", "Unsaved changes detected",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, buttons, buttons[1]);

                if (unsavedChangesChoice == 0) { // save changes
                    _saveButton.doClick();
                } else if (unsavedChangesChoice != 1) { // cancel closing
                    return false;
                }
            }

            resetJob();
            break;
        default:
            changePanel(AnalysisWindowPanelType.WELCOME);
        }

        return false;
    }

    private void cleanupForWindowClose() {
        _analysisJobChangeListener.onDeactivation(_analysisJobBuilder);
        _analysisJobBuilder.close();
        if (_datastoreConnection != null) {
            _datastoreConnection.close();
        }
        getContentPane().removeAll();
    }

    private boolean isJobUnsaved(FileObject lastSavedJobFile, AnalysisJobBuilder analysisJobBuilder) {
        if (lastSavedJobFile == null) {
            if (analysisJobBuilder.getComponentCount() == 0) {
                // user didn't actually do anything yet
                return false;
            }
            return true;
        }
        try {
            if (!lastSavedJobFile.exists()) {
                return true;
            }
        } catch (FileSystemException e) {
            logger.warn("Error while determining if the job file already exists", e);
        }

        InputStream lastSavedOutputStream = null;
        ByteArrayOutputStream currentOutputStream = null;
        try {
            File jobFile = new File(getJobFile().getURL().getFile());
            if (jobFile.length() == 0) {
                return true;
            }

            String lastSavedJob = FileHelper.readFileAsString(jobFile);
            String lastSavedJobNoMetadata = lastSavedJob.replaceAll("\n", "").replaceAll(
                    "<job-metadata>.*</job-metadata>", "");

            JaxbJobWriter writer = new JaxbJobWriter(_configuration);
            currentOutputStream = new ByteArrayOutputStream();
            writer.write(_analysisJobBuilder.toAnalysisJob(false), currentOutputStream);
            String currentJob = new String(currentOutputStream.toByteArray());
            String currentJobNoMetadata = currentJob.replaceAll("\n", "").replaceAll("<job-metadata>.*</job-metadata>",
                    "");

            return !currentJobNoMetadata.equals(lastSavedJobNoMetadata);
        } catch (FileSystemException e) {
            throw new IllegalStateException(e);
        } finally {
            FileHelper.safeClose(currentOutputStream);
            FileHelper.safeClose(lastSavedOutputStream);
        }
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
        String title;
        if (_currentPanelType != null) {
            title = _currentPanelType.getName();
        } else {
            title = AnalysisWindowPanelType.WELCOME.getName();
        }

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
        return imageManager.getImage(IconUtils.APPLICATION_ICON);
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    protected JComponent getWindowContent() {
        if (_windowContent == null) {
            _windowContent = getWindowPanelContent();
        }
        return _windowContent;
    }

    private JComponent getWindowPanelContent() {
        if (_datastore != null) {
            setDatastore(_datastore);
        }

        final SaveAnalysisJobActionListener saveAnalysisJobActionListener = _saveAnalysisJobActionListenerProvider
                .get();
        _saveButton.addActionListener(saveAnalysisJobActionListener);
        _saveAsButton.addActionListener(saveAnalysisJobActionListener);
        _saveAsButton.setActionCommand(SaveAnalysisJobActionListener.ACTION_COMMAND_SAVE_AS);

        // Run analysis
        _executeButton.addActionListener(execute(_analysisJobBuilder));

        _executionAlternativesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JMenuItem executeNormallyMenutItem = WidgetFactory.createMenuItem("Run normally",
                        IconUtils.ACTION_EXECUTE);
                executeNormallyMenutItem.addActionListener(execute(_analysisJobBuilder));

                final JMenuItem executePreviewMenuItem = WidgetFactory.createMenuItem("Run first N records",
                        IconUtils.ACTION_PREVIEW);
                executePreviewMenuItem.addActionListener(executePreview());

                final JMenuItem executeSingleThreadedMenuItem = WidgetFactory.createMenuItem("Run single-threaded",
                        IconUtils.MODEL_ROW);
                executeSingleThreadedMenuItem.addActionListener(executeSingleThreaded());

                final JPopupMenu menu = new JPopupMenu();
                menu.add(executeNormallyMenutItem);
                menu.addSeparator();
                menu.add(executePreviewMenuItem);
                menu.add(executeSingleThreadedMenuItem);

                final int horizontalPosition = -1 * menu.getPreferredSize().width
                        + _executionAlternativesButton.getWidth();
                menu.show(_executionAlternativesButton, horizontalPosition, _executionAlternativesButton.getHeight());
            }
        });

        final JButton newJobButton = createToolbarButton("New", IconUtils.MENU_NEW);
        newJobButton.addActionListener(_newAnalysisJobActionListenerProvider.get());

        final JButton openJobButton = createToolbarButton("Open", IconUtils.MENU_OPEN);
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
        toolBar.add(_executeButton);
        toolBar.add(DCLabel.bright("|"));
        toolBar.add(_executionAlternativesButton);

        final JXStatusBar statusBar = WidgetFactory.createStatusBar(_statusLabel);
        statusBar.add(_classicViewButton);
        statusBar.add(_graphViewButton);
        statusBar.add(Box.createHorizontalStrut(10));

        final LicenceAndEditionStatusLabel statusLabel = new LicenceAndEditionStatusLabel(_glassPane);
        statusBar.add(statusLabel);

        final DCPanel toolBarPanel = new DCPanel(WidgetUtils.BG_COLOR_DARK);
        toolBarPanel.setLayout(new BorderLayout());
        toolBarPanel.add(toolBar, BorderLayout.CENTER);

        final DCPanel panel = new DCPersistentSizedPanel(_windowSizePreference);
        panel.setLayout(new BorderLayout());
        panel.add(toolBarPanel, BorderLayout.NORTH);
        panel.add(_leftPanel, BorderLayout.WEST);

        // newPanel.add(_tabbedPane, BorderLayout.NORTH);
        panel.add(_contentContainerPanel, BorderLayout.CENTER);

        panel.add(statusBar, BorderLayout.SOUTH);

        // invoke to trigger enablement/disablement of buttons.
        onSourceColumnsChanged();
        updateStatusLabel();

        WidgetUtils.centerOnScreen(this);
        return panel;
    }

    private ActionListener execute(final AnalysisJobBuilder analysisJobBuilder) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyPropertyValues();

                if (analysisJobBuilder.getResultProducingComponentBuilders().isEmpty()) {
                    if (analysisJobBuilder.getConsumedOutputDataStreamsJobBuilders().isEmpty()) {
                        // Present choices to user to write file somewhere,
                        // and then run a copy of the job based on that.
                        ExecuteJobWithoutAnalyzersDialog executeJobWithoutAnalyzersPanel = new ExecuteJobWithoutAnalyzersDialog(
                                _dcModule, getWindowContext(), analysisJobBuilder, _userPreferences);
                        executeJobWithoutAnalyzersPanel.open();
                        return;
                    }
                }

                final RunAnalysisActionListener runAnalysis = new RunAnalysisActionListener(_dcModule,
                        analysisJobBuilder);
                runAnalysis.run();
            }
        };
    }

    private ActionListener executeSingleThreaded() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final DataCleanerConfigurationImpl configuration = new DataCleanerConfigurationImpl(_configuration)
                        .withEnvironment(new DataCleanerEnvironmentImpl(_configuration.getEnvironment())
                                .withTaskRunner(new SingleThreadedTaskRunner()));

                final AnalysisJob jobCopy = _analysisJobBuilder.toAnalysisJob(false);
                final AnalysisJobBuilder jobBuilderCopy = new AnalysisJobBuilder(configuration, jobCopy);

                execute(jobBuilderCopy).actionPerformed(e);
            }
        };
    }

    private ActionListener executePreview() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String maxRowsString = JOptionPane.showInputDialog("How many records do you want to process?",
                        "100");
                final Number maxRows = ConvertToNumberTransformer.transformValue(maxRowsString);
                if (maxRows == null || maxRows.intValue() < 1) {
                    WidgetUtils.showErrorMessage("Not a valid number", "Please enter a valid number of records.");
                    return;
                }

                final AnalysisJob jobCopy = _analysisJobBuilder.toAnalysisJob(false);
                final AnalysisJobBuilder jobBuilderCopy = new AnalysisJobBuilder(_configuration, jobCopy);
                final FilterComponentBuilder<MaxRowsFilter, Category> maxRowsFilter = jobBuilderCopy
                        .addFilter(MaxRowsFilter.class);
                maxRowsFilter.getComponentInstance().setMaxRows(maxRows.intValue());
                maxRowsFilter.addInputColumn(jobBuilderCopy.getSourceColumns().get(0));
                final FilterOutcome filterOutcome = maxRowsFilter.getFilterOutcome(MaxRowsFilter.Category.VALID);
                final Collection<ComponentBuilder> componentBuilders = jobBuilderCopy.getComponentBuilders();
                for (ComponentBuilder componentBuilder : componentBuilders) {
                    if (componentBuilder != maxRowsFilter && componentBuilder.getComponentRequirement() == null) {
                        componentBuilder.setComponentRequirement(new SimpleComponentRequirement(filterOutcome));
                    }
                }

                execute(jobBuilderCopy).actionPerformed(e);
            }
        };
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
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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

    @Override
    public AnalysisJobBuilder getAnalysisJobBuilder() {
        return _analysisJobBuilder;
    }

    public void windowClosed(WindowEvent e) {
        if (this.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
            _windowSizePreference.setUserPreferredSize(null, true);
        } else {
            _windowSizePreference.setUserPreferredSize(getSize(), false);
        }
    }

    @Override
    protected boolean maximizeWindow() {
        return _windowSizePreference.isWindowMaximized();
    }
}
