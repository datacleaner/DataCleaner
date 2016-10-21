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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

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
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.Version;
import org.datacleaner.actions.NewAnalysisJobActionListener;
import org.datacleaner.actions.OpenAnalysisJobActionListener;
import org.datacleaner.actions.SaveAnalysisJobActionListener;
import org.datacleaner.api.InputColumn;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.guice.DCModule;
import org.datacleaner.guice.JobFile;
import org.datacleaner.guice.Nullable;
import org.datacleaner.job.ComponentValidationException;
import org.datacleaner.job.JaxbJobWriter;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalysisJobChangeListener;
import org.datacleaner.job.builder.AnalyzerChangeListener;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.FilterChangeListener;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.user.ReferenceDataChangeListener;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.job.builder.SourceColumnChangeListener;
import org.datacleaner.job.builder.TransformerChangeListener;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.builder.UnconfiguredConfiguredPropertyException;
import org.datacleaner.panels.DCGlassPane;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.panels.DatastoreManagementPanel;
import org.datacleaner.panels.RightInformationPanel;
import org.datacleaner.panels.SchemaTreePanel;
import org.datacleaner.panels.SelectDatastoreContainerPanel;
import org.datacleaner.panels.WelcomePanel;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.MutableReferenceDataCatalog;
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
import org.datacleaner.widgets.CommunityEditionStatusLabel;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DCPersistentSizedPanel;
import org.datacleaner.widgets.DataCloudStatusLabel;
import org.datacleaner.widgets.ExecuteButtonBuilder;
import org.datacleaner.widgets.InformationPanelDescriptor;
import org.datacleaner.widgets.NewsChannelStatusLabel;
import org.datacleaner.widgets.InformationPanelLabel;
import org.datacleaner.widgets.PopupButton;
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
public final class AnalysisJobBuilderWindowImpl extends AbstractWindow implements
        AnalysisJobBuilderWindow,
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
            updateStatusLabel();
            _graph.refresh();
        }

        @Override
        public void onRemove(final AnalyzerComponentBuilder<?> analyzerJobBuilder) {
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
            updateStatusLabel();
            _graph.refresh();
        }

        @Override
        public void onRemove(final TransformerComponentBuilder<?> transformerJobBuilder) {
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
            updateStatusLabel();
            _graph.refresh();
        }

        @Override
        public void onRemove(final FilterComponentBuilder<?, ?> filterJobBuilder) {
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
    private final ExecuteButtonBuilder _executeButton;
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
    private final JobGraph _graph;
    private final DCPanel _contentContainerPanel;
    private final AnalyzerChangeListener _analyzerChangeListener = new WindowAnalyzerChangeListener();
    private final TransformerChangeListener _transformerChangeListener = new WindowTransformerChangeListener();
    private final FilterChangeListener _filterChangeListener = new WindowFilterChangeListener();
    private final SourceColumnChangeListener _sourceColumnChangeListener = new WindowSourceColumnChangeListener();
    private final AnalysisJobChangeListener _analysisJobChangeListener = new WindowAnalysisJobChangeListener();
    private final ReferenceDataAnalysisJobWindowImplListeners _referenceDataAnalysisJobWindowListeners;
    private ReferenceDataChangeListener<StringPattern> _stringPatternChangeListener;
    private ReferenceDataChangeListener<Dictionary> _dictionaryChangeListener;
    private ReferenceDataChangeListener<SynonymCatalog> _synonymCatalogListener;
    private FileObject _jobFilename;
    private Datastore _datastore;
    private DatastoreConnection _datastoreConnection;
    private boolean _datastoreSelectionEnabled;
    private JComponent _windowContent;
    private WindowSizePreferences _windowSizePreference;
    private AnalysisWindowPanelType _currentPanelType;
    private MutableReferenceDataCatalog _mutableReferenceCatalog;

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
            OpenAnalysisJobActionListener openAnalysisJobActionListener, DatabaseDriverCatalog databaseDriverCatalog,
            MutableReferenceDataCatalog mutableReferenceCatalog) {
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
        _mutableReferenceCatalog = mutableReferenceCatalog;
        _windowSizePreference = new WindowSizePreferences(_userPreferences, getClass(), DEFAULT_WINDOW_WIDTH,
                DEFAULT_WINDOW_HEIGHT);

        setMinimumSize(new Dimension(900, 550));

        if (analysisJobBuilder == null) {
            _analysisJobBuilder = new AnalysisJobBuilder(_configuration);
        } else {
            _analysisJobBuilder = analysisJobBuilder;
            final DatastoreConnection con = _analysisJobBuilder.getDatastoreConnection();
            if (con != null) {
                _datastore = con.getDatastore();
            }
        }

        _executeButton = new ExecuteButtonBuilder(this);

        _datastoreSelectionEnabled = true;
        _presenterRendererFactory = new RendererFactory(configuration);
        _glassPane = new DCGlassPane(this);

        _graph = new JobGraph(windowContext, userPreferences, analysisJobBuilder, _presenterRendererFactory,
                usageLogger);

        _analysisJobChangeListener.onActivation(_analysisJobBuilder);
        //Add listeners for ReferenceData classes 
        _referenceDataAnalysisJobWindowListeners = new ReferenceDataAnalysisJobWindowImplListeners(_analysisJobBuilder);
        _stringPatternChangeListener = _referenceDataAnalysisJobWindowListeners.new WindowChangeStringPatternListener();
        _dictionaryChangeListener = _referenceDataAnalysisJobWindowListeners.new WindowChangeDictionaryListener();
        _synonymCatalogListener = _referenceDataAnalysisJobWindowListeners.new WindowChangeSynonymCatalogListener();
        _mutableReferenceCatalog.addStringPatternListener(_stringPatternChangeListener);
        _mutableReferenceCatalog.addDictionaryListener(_dictionaryChangeListener);
        _mutableReferenceCatalog.addSynonymCatalogListener(_synonymCatalogListener);

        _saveButton = WidgetFactory.createToolbarButton("Save", IconUtils.ACTION_SAVE_BRIGHT);
        _saveAsButton = WidgetFactory.createToolbarButton("Save As...", IconUtils.ACTION_SAVE_BRIGHT);

        _welcomePanel = new WelcomePanel(this, _userPreferences, _openAnalysisJobActionListenerProvider.get(),
                _dcModule);

        _datastoreManagementPanel = new DatastoreManagementPanel(_configuration, this, _glassPane,
                _optionsDialogProvider, _dcModule, databaseDriverCatalog, _userPreferences);
        _selectDatastorePanel = new SelectDatastoreContainerPanel(this, _dcModule, databaseDriverCatalog,
                (MutableDatastoreCatalog) configuration.getDatastoreCatalog(), configuration.getServerInformationCatalog(), _userPreferences, windowContext);
        _contentContainerPanel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _contentContainerPanel.setLayout(new CardLayout());
        _contentContainerPanel.add(_welcomePanel, AnalysisWindowPanelType.WELCOME.getName());
        _contentContainerPanel.add(_graph.getPanel(), AnalysisWindowPanelType.EDITING_CONTEXT.getName());
        _contentContainerPanel.add(_datastoreManagementPanel, AnalysisWindowPanelType.MANAGE_DS.getName());
        _contentContainerPanel.add(_selectDatastorePanel, AnalysisWindowPanelType.SELECT_DS.getName());

        _schemaTreePanel = schemaTreePanel;

        _leftPanel = new CollapsibleTreePanel(_schemaTreePanel);
        _leftPanel.setVisible(false);
        _leftPanel.setCollapsed(true);
        _schemaTreePanel.setUpdatePanel(_leftPanel);
        
    }

    @Override
    public void open() {
        super.open();
    }
    
    @Override
    public void changePanel(AnalysisWindowPanelType panel) {
        if (_datastore == null) {
            _currentPanelType = panel;
            _schemaTreePanel.onPanelHiding();
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
        _graph.refresh();
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
            final Timer timer = new Timer(500, e -> {
                if (_leftPanel.isCollapsed()) {
                    _leftPanel.setVisible(false);
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
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
                executeable = false;
                logger.debug("Job not correctly configured", ex);
                final String errorMessage;
                if (ex instanceof UnconfiguredConfiguredPropertyException) {
                    final UnconfiguredConfiguredPropertyException unconfiguredConfiguredPropertyException = (UnconfiguredConfiguredPropertyException) ex;
                    final ConfiguredPropertyDescriptor configuredProperty = unconfiguredConfiguredPropertyException
                            .getConfiguredProperty();
                    final ComponentBuilder componentBuilder = unconfiguredConfiguredPropertyException
                            .getComponentBuilder();
                    errorMessage = "Property '" + configuredProperty.getName() + "' in " + LabelUtils.getLabel(
                            componentBuilder) + " is not set!";
                } else if (ex instanceof ComponentValidationException) {
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
                    final ActionListener[] actionListeners = _saveButton.getActionListeners();
                    if (actionListeners[0] instanceof SaveAnalysisJobActionListener){
                        final SaveAnalysisJobActionListener saveAnalysisJobActionListener = (SaveAnalysisJobActionListener) actionListeners[0];
                        if (!saveAnalysisJobActionListener.isSaved()) {
                            return false;
                        }
                    }
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
        
        //Remove the reference data listener
        _mutableReferenceCatalog.removeStringPatternListener(_stringPatternChangeListener);
        _mutableReferenceCatalog.removeDictionaryListener(_dictionaryChangeListener);
        _mutableReferenceCatalog.removeSynonymCatalogListener(_synonymCatalogListener);
        
        getContentPane().removeAll();
    }

    private boolean isJobUnsaved(FileObject lastSavedJobFile, AnalysisJobBuilder analysisJobBuilder) {
        if (lastSavedJobFile == null) {
            return analysisJobBuilder.getComponentCount() != 0;
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

        final JButton newJobButton = WidgetFactory.createToolbarButton("New", IconUtils.MENU_NEW);
        newJobButton.addActionListener(_newAnalysisJobActionListenerProvider.get());

        final JButton openJobButton = WidgetFactory.createToolbarButton("Open", IconUtils.MENU_OPEN);
        openJobButton.addActionListener(_openAnalysisJobActionListenerProvider.get());

        final JToggleButton moreButton = createMoreMenuButton();

        final JButton logoButton = new JButton(imageManager.getImageIcon("images/menu/dc-logo-30.png"));
        logoButton.setToolTipText("About DataCleaner");
        logoButton.setBorder(new EmptyBorder(0, 4, 0, 10));
        logoButton.addActionListener(e -> new AboutDialog(getWindowContext()).open());

        final JToolBar toolBar = WidgetFactory.createToolBar();
        toolBar.add(logoButton);
        toolBar.add(newJobButton);
        toolBar.add(openJobButton);
        toolBar.add(_saveButton);
        toolBar.add(_saveAsButton);
        toolBar.add(DCLabel.bright(" | "));
        toolBar.add(moreButton);

        toolBar.add(WidgetFactory.createToolBarSeparator());
        _executeButton.addComponentsToToolbar(toolBar);

        final JXStatusBar statusBar = WidgetFactory.createStatusBar(_statusLabel);
        RightInformationPanel rightInformationPanel = new RightInformationPanel(_glassPane);
        
        final DataCloudStatusLabel dataCloudStatusLabel =
                new DataCloudStatusLabel(rightInformationPanel, _configuration, _userPreferences, getWindowContext(), this);
        statusBar.add(dataCloudStatusLabel);
        statusBar.add(Box.createHorizontalStrut(20));

        final NewsChannelStatusLabel newChannelStatusLabel = new NewsChannelStatusLabel(rightInformationPanel, _userPreferences);
        statusBar.add(newChannelStatusLabel);
        statusBar.add(Box.createHorizontalStrut(20));
        
        if (Version.isCommunityEdition()) {
            final CommunityEditionStatusLabel statusLabel = new CommunityEditionStatusLabel(rightInformationPanel);
            statusBar.add(statusLabel);
            statusBar.add(Box.createHorizontalStrut(20));
        } else {
            final ServiceLoader<InformationPanelDescriptor> panelsLoaders = ServiceLoader.load(InformationPanelDescriptor.class);
            for (InformationPanelDescriptor panel : panelsLoaders) {
                final InformationPanelLabel plugableRightPanelLabel = new InformationPanelLabel(rightInformationPanel, panel);
                statusBar.add(plugableRightPanelLabel);
                statusBar.add(Box.createHorizontalStrut(20));
            }
        }

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

    private JToggleButton createMoreMenuButton() {
        final JMenuItem optionsMenuItem = WidgetFactory.createMenuItem("Options", IconUtils.MENU_OPTIONS);
        optionsMenuItem.addActionListener(e -> {
            final OptionsDialog optionsDialog = _optionsDialogProvider.get();
            optionsDialog.getTabbedPane().setSelectedIndex(0);
            optionsDialog.open();
        });
        
        final JMenuItem monitorMenuItem = WidgetFactory.createMenuItem("DataCleaner monitor",
                IconUtils.MENU_DQ_MONITOR);
        monitorMenuItem.addActionListener(e -> {
            MonitorConnectionDialog dialog = _monitorConnectionDialogProvider.get();
            dialog.open();
        });

        final JMenuItem dictionariesMenuItem = WidgetFactory.createMenuItem("Dictionaries",
                IconUtils.DICTIONARY_IMAGEPATH);
        dictionariesMenuItem.addActionListener(e -> {
            ReferenceDataDialog referenceDataDialog = _referenceDataDialogProvider.get();
            referenceDataDialog.selectDictionariesTab();
            referenceDataDialog.open();
        });

        final JMenuItem synonymCatalogsMenuItem = WidgetFactory.createMenuItem("Synonyms",
                IconUtils.SYNONYM_CATALOG_IMAGEPATH);
        synonymCatalogsMenuItem.addActionListener(e -> {
            ReferenceDataDialog referenceDataDialog = _referenceDataDialogProvider.get();
            referenceDataDialog.selectSynonymsTab();
            referenceDataDialog.open();
        });

        final JMenuItem stringPatternsMenuItem = WidgetFactory.createMenuItem("String patterns",
                IconUtils.STRING_PATTERN_IMAGEPATH);
        stringPatternsMenuItem.addActionListener(e -> {
            ReferenceDataDialog referenceDataDialog = _referenceDataDialogProvider.get();
            referenceDataDialog.selectStringPatternsTab();
            referenceDataDialog.open();
        });

        final PopupButton popupButton = new PopupButton("More", imageManager.getImageIcon(
                IconUtils.ACTION_SCROLLDOWN_BRIGHT));
        applyMenuPopupButttonStyling(popupButton);

        final JMenu windowsMenuItem = WidgetFactory.createMenu("Windows", 'w');
        windowsMenuItem.setIcon(imageManager.getImageIcon("images/menu/windows.png", IconUtils.ICON_SIZE_SMALL));
        final List<DCWindow> windows = getWindowContext().getWindows();

        getWindowContext().addWindowListener(e -> {
            windowsMenuItem.removeAll();
            for (final DCWindow window : windows) {
                final Image windowIcon = window.getWindowIcon();
                final String title = window.getWindowTitle();
                final String titleText;
                // see issue #1454
                if (title.length() > 100) {
                    titleText = title.substring(0, 100).concat(" ...");
                } else {
                    titleText = title;
                }
                final ImageIcon icon = new ImageIcon(windowIcon.getScaledInstance(IconUtils.ICON_SIZE_SMALL,
                        IconUtils.ICON_SIZE_SMALL, Image.SCALE_DEFAULT));
                final JMenuItem switchToWindowItem = WidgetFactory.createMenuItem(titleText, icon);
                switchToWindowItem.addActionListener(e1 -> window.toFront());
                windowsMenuItem.add(switchToWindowItem);
            }

            windowsMenuItem.add(new JSeparator());

            JMenuItem closeAllWindowsItem = WidgetFactory.createMenuItem("Close all dialogs", (ImageIcon) null);
            closeAllWindowsItem.addActionListener(e1 -> {
                List<DCWindow> windows1 = new ArrayList<>(getWindowContext().getWindows());
                for (DCWindow window : windows1) {
                    if (window instanceof AbstractDialog) {
                        window.close();
                    }
                }
            });
            windowsMenuItem.add(closeAllWindowsItem);
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

    /**
     * Applies property values for all components visible in the window.
     */
    @Override
    public void applyPropertyValues() {
    }

    private void onSourceColumnsChanged() {
        boolean everythingEnabled = !_analysisJobBuilder.getSourceColumns().isEmpty() && _datastore != null;

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

    @Override
    public DataCleanerConfiguration getConfiguration() {
        return _configuration;
    }

    @Override
    public UserPreferences getUserPreferences() {
        return _userPreferences;
    }

    @Override
    public DCModule getDCModule() {
        return _dcModule;
    }
}
