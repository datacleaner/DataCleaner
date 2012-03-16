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
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerChangeListener;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.ExplorerChangeListener;
import org.eobjects.analyzer.job.builder.ExplorerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterChangeListener;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.MergedOutcomeJobBuilder;
import org.eobjects.analyzer.job.builder.SourceColumnChangeListener;
import org.eobjects.analyzer.job.builder.TransformerChangeListener;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.job.builder.UnconfiguredConfiguredPropertyException;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.Main;
import org.eobjects.datacleaner.actions.AnalyzeButtonActionListener;
import org.eobjects.datacleaner.actions.TransformButtonActionListener;
import org.eobjects.datacleaner.actions.HideTabTextActionListener;
import org.eobjects.datacleaner.actions.JobBuilderTabTextActionListener;
import org.eobjects.datacleaner.actions.RenameComponentActionListener;
import org.eobjects.datacleaner.actions.RunAnalysisActionListener;
import org.eobjects.datacleaner.actions.SaveAnalysisJobActionListener;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.InjectorBuilder;
import org.eobjects.datacleaner.guice.JobFilename;
import org.eobjects.datacleaner.guice.Nullable;
import org.eobjects.datacleaner.panels.AbstractJobBuilderPanel;
import org.eobjects.datacleaner.panels.AnalyzerJobBuilderPresenter;
import org.eobjects.datacleaner.panels.ComponentJobBuilderPresenter;
import org.eobjects.datacleaner.panels.ComponentJobBuilderRenderingFormat;
import org.eobjects.datacleaner.panels.DCGlassPane;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.panels.DatastoreListPanel;
import org.eobjects.datacleaner.panels.FilterJobBuilderPresenter;
import org.eobjects.datacleaner.panels.MetadataPanel;
import org.eobjects.datacleaner.panels.SchemaTreePanel;
import org.eobjects.datacleaner.panels.SourceColumnsPanel;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPresenter;
import org.eobjects.datacleaner.panels.maxrows.MaxRowsFilterShortcutPanel;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.LabelUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.CollapsibleTreePanel;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.DCPersistentSizedPanel;
import org.eobjects.datacleaner.widgets.DCPopupBubble;
import org.eobjects.datacleaner.widgets.DCWindowMenuBar;
import org.eobjects.datacleaner.widgets.LoginStatusLabel;
import org.eobjects.datacleaner.widgets.result.DCRendererInitializer;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.eobjects.datacleaner.widgets.tabs.TabCloseEvent;
import org.eobjects.datacleaner.widgets.tabs.TabCloseListener;
import org.eobjects.metamodel.util.LazyRef;
import org.eobjects.metamodel.util.Ref;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * The main window in the DataCleaner GUI. This window is called the
 * AnalysisJobBuilderWindow because it's main purpose is to present a job that
 * is being built. Behind the covers this job state is respresented in the
 * {@link AnalysisJobBuilder} class.
 * 
 * @author Kasper Sørensen
 */
@Singleton
public final class AnalysisJobBuilderWindowImpl extends AbstractWindow
		implements AnalysisJobBuilderWindow, AnalyzerChangeListener,
		ExplorerChangeListener, TransformerChangeListener,
		FilterChangeListener, SourceColumnChangeListener, TabCloseListener {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory
			.getLogger(AnalysisJobBuilderWindow.class);
	private static final ImageManager imageManager = ImageManager.getInstance();

	private static final int DEFAULT_WINDOW_WIDTH = 900;
	private static final int DEFAULT_WINDOW_HEIGHT = 630;

	private static final int SOURCE_TAB = 0;
	private static final int METADATA_TAB = 1;

	private final Map<AnalyzerJobBuilder<?>, AnalyzerJobBuilderPresenter> _analyzerPresenters = new LinkedHashMap<AnalyzerJobBuilder<?>, AnalyzerJobBuilderPresenter>();
	private final Map<TransformerJobBuilder<?>, TransformerJobBuilderPresenter> _transformerPresenters = new LinkedHashMap<TransformerJobBuilder<?>, TransformerJobBuilderPresenter>();
	private final Map<FilterJobBuilder<?, ?>, FilterJobBuilderPresenter> _filterPresenters = new LinkedHashMap<FilterJobBuilder<?, ?>, FilterJobBuilderPresenter>();
	private final Map<ComponentJobBuilderPresenter, JComponent> _jobBuilderTabs = new HashMap<ComponentJobBuilderPresenter, JComponent>();
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final AnalyzerBeansConfiguration _configuration;
	private final RendererFactory _componentJobBuilderPresenterRendererFactory;
	private final CloseableTabbedPane _tabbedPane;
	private final DCLabel _statusLabel = DCLabel.bright("");
	private final CollapsibleTreePanel _leftPanel;
	private final SourceColumnsPanel _sourceColumnsPanel;
	private final SchemaTreePanel _schemaTreePanel;
	private final JButton _saveButton;
	private final JButton _visualizeButton;
	private final JButton _transformButton;
	private final JButton _analyzeButton;
	private final JButton _runButton;
	private final Provider<RunAnalysisActionListener> _runAnalysisActionProvider;
	private final Provider<SaveAnalysisJobActionListener> _saveAnalysisJobActionListenerProvider;
	private final Provider<AnalyzeButtonActionListener> _addAnalyzerActionListenerProvider;
	private final Provider<TransformButtonActionListener> _addTransformerActionListenerProvider;
	private final DCGlassPane _glassPane;
	private final Ref<DatastoreListPanel> _datastoreListPanelRef;
	private final UserPreferences _userPreferences;
	private final Injector _injectorWithGlassPane;
	private final DCWindowMenuBar _windowMenuBar;
	private volatile AbstractJobBuilderPanel _latestPanel = null;
	private final DCPanel _sourceTabOuterPanel;
	private String _jobFilename;
	private Datastore _datastore;
	private DatastoreConnection _datastoreConnection;
	private boolean _datastoreSelectionEnabled;
	private final MetadataPanel _metadataPanel;

	@Inject
	protected AnalysisJobBuilderWindowImpl(
			AnalyzerBeansConfiguration configuration,
			WindowContext windowContext,
			Provider<DCRendererInitializer> rendererInitializerProvider,
			SchemaTreePanel schemaTreePanel,
			SourceColumnsPanel sourceColumnsPanel,
			Provider<RunAnalysisActionListener> runAnalysisActionProvider,
			MetadataPanel metadataPanel,
			AnalysisJobBuilder analysisJobBuilder,
			InjectorBuilder injectorBuilder,
			UserPreferences userPreferences,
			@Nullable @JobFilename String jobFilename,
			DCWindowMenuBar windowMenuBar,
			Provider<SaveAnalysisJobActionListener> saveAnalysisJobActionListenerProvider,
			Provider<AnalyzeButtonActionListener> addAnalyzerActionListenerProvider,
			Provider<TransformButtonActionListener> addTransformerActionListenerProvider,
			UsageLogger usageLogger) {
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
			DatastoreConnection con = _analysisJobBuilder
					.getDatastoreConnection();
			if (con != null) {
				_datastore = con.getDatastore();
			}
		}
		_windowMenuBar.setAnalysisJobBuilder(_analysisJobBuilder);

		_datastoreSelectionEnabled = true;
		_componentJobBuilderPresenterRendererFactory = new RendererFactory(
				_configuration.getDescriptorProvider(),
				rendererInitializerProvider.get());
		_glassPane = new DCGlassPane(this);
		_injectorWithGlassPane = injectorBuilder.with(DCGlassPane.class,
				_glassPane).createInjector();

		_analysisJobBuilder.getAnalyzerChangeListeners().add(this);
		_analysisJobBuilder.getTransformerChangeListeners().add(this);
		_analysisJobBuilder.getFilterChangeListeners().add(this);
		_analysisJobBuilder.getSourceColumnListeners().add(this);

		_saveButton = new JButton("Save analysis job",
				imageManager.getImageIcon("images/actions/save.png"));
		_visualizeButton = createToolbarButton(
				"Visualize",
				"images/actions/visualize.png",
				"<html><b>Visualize job</b><br/>Visualize the components of this job in a flow-chart.</html>");
		_transformButton = createToolbarButton(
				"Transform",
				IconUtils.TRANSFORMER_IMAGEPATH,
				"<html><b>Transformers and filters</b><br/>Preprocess or filter your data in order to extract, limit, combine or generate separate values.</html>");
		_analyzeButton = createToolbarButton(
				"Analyze",
				IconUtils.ANALYZER_IMAGEPATH,
				"<html><b>Analyzers</b><br/>Analyzers provide Data Quality analysis and profiling operations.</html>");
		_runButton = new JButton("Execute",
				imageManager.getImageIcon("images/actions/execute.png"));

		_datastoreListPanelRef = new LazyRef<DatastoreListPanel>() {
			@Override
			protected DatastoreListPanel fetch() {
				DatastoreListPanel datastoreListPanel = _injectorWithGlassPane
						.getInstance(DatastoreListPanel.class);
				datastoreListPanel.setBorder(new EmptyBorder(4, 4, 0, 150));
				return datastoreListPanel;
			}

		};

		_sourceColumnsPanel = sourceColumnsPanel;

		_tabbedPane = new CloseableTabbedPane(false);
		_tabbedPane.addTabCloseListener(this);
		_tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public synchronized void stateChanged(ChangeEvent e) {
				if (_latestPanel != null) {
					_latestPanel.applyPropertyValues(false);
				}
				Component selectedComponent = _tabbedPane
						.getSelectedComponent();
				if (selectedComponent instanceof AbstractJobBuilderPanel) {
					_latestPanel = (AbstractJobBuilderPanel) selectedComponent;
				} else {
					_latestPanel = null;
				}
				updateStatusLabel();
			}
		});

		_sourceTabOuterPanel = new DCPanel(
				imageManager
						.getImage("images/window/source-tab-background.png"),
				95, 95, WidgetUtils.BG_COLOR_BRIGHT,
				WidgetUtils.BG_COLOR_BRIGHTEST);
		_sourceTabOuterPanel.setLayout(new VerticalLayout(0));

		_schemaTreePanel = schemaTreePanel;
		_metadataPanel = metadataPanel;

		_leftPanel = new CollapsibleTreePanel(_schemaTreePanel);
		_leftPanel.setVisible(false);
		_leftPanel.setCollapsed(true);
		_schemaTreePanel.setUpdatePanel(_leftPanel);
	}

	private JButton createToolbarButton(String text, String iconPath,
			String popupDescription) {
		JButton button = new JButton(text, imageManager.getImageIcon(iconPath));
		button.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		button.setFocusPainted(false);
		if (popupDescription != null) {
			DCPopupBubble popupBubble = new DCPopupBubble(_glassPane,
					popupDescription, 0, 0, iconPath);
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
			displayDatastoreSelection();
		} else {
			displaySourceColumnsList();
		}

		updateStatusLabel();
	}

	private void displaySourceColumnsList() {
		_leftPanel.setVisible(true);
		if (_leftPanel.isCollapsed()) {
			_leftPanel.setCollapsed(false);
		}

		_sourceColumnsPanel.setVisible(true);
		_datastoreListPanelRef.get().setVisible(false);
	}

	private void displayDatastoreSelection() {
		if (isShowing()) {
			if (_datastore == null) {
				if (!_leftPanel.isCollapsed()) {
					_leftPanel.setCollapsed(true);
				}
				Timer timer = new Timer(500, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						_leftPanel.setVisible(false);
					}
				});
				timer.setRepeats(false);
				timer.start();

				_sourceColumnsPanel.setVisible(false);
				_datastoreListPanelRef.get().setVisible(true);
				_datastoreListPanelRef.get().requestSearchFieldFocus();
			}
		}
	}

	@Override
	protected void onWindowVisible() {
		displayDatastoreSelection();
	}

	private void updateStatusLabel() {
		boolean success = false;

		if (_datastore == null) {
			_statusLabel.setText("Welcome to DataCleaner " + Main.VERSION);
			_statusLabel.setIcon(imageManager.getImageIcon(
					"images/window/app-icon.png", IconUtils.ICON_SIZE_SMALL));
		} else {
			try {
				if (_analysisJobBuilder.isConfigured(true)) {
					success = true;
					_statusLabel.setText("Job is correctly configured");
					_statusLabel.setIcon(imageManager.getImageIcon(
							IconUtils.STATUS_VALID, IconUtils.ICON_SIZE_SMALL));
				} else {
					_statusLabel.setText("Job is not correctly configured");
					_statusLabel.setIcon(imageManager
							.getImageIcon(IconUtils.STATUS_WARNING,
									IconUtils.ICON_SIZE_SMALL));
				}
			} catch (Exception ex) {
				logger.debug("Job not correctly configured", ex);
				final String errorMessage;
				if (ex instanceof UnconfiguredConfiguredPropertyException) {
					UnconfiguredConfiguredPropertyException unconfiguredConfiguredPropertyException = (UnconfiguredConfiguredPropertyException) ex;
					ConfiguredPropertyDescriptor configuredProperty = unconfiguredConfiguredPropertyException
							.getConfiguredProperty();
					AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder = unconfiguredConfiguredPropertyException
							.getBeanJobBuilder();
					errorMessage = "Property '" + configuredProperty.getName()
							+ "' in " + LabelUtils.getLabel(beanJobBuilder)
							+ " is not set!";
				} else {
					errorMessage = ex.getMessage();
				}
				_statusLabel.setText("Job error status: " + errorMessage);
				_statusLabel.setIcon(imageManager.getImageIcon(
						IconUtils.STATUS_ERROR, IconUtils.ICON_SIZE_SMALL));
			}
		}

		_runButton.setEnabled(success);
	}

	@Override
	public String getStatusLabelText() {
		return _statusLabel.getText();
	}

	@Override
	protected boolean onWindowClosing() {
		if (!super.onWindowClosing()) {
			return false;
		}

		final int count = getWindowContext().getWindowCount(
				AnalysisJobBuilderWindow.class);

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
		setJobFilename(null);
	}

	@Override
	public void setJobFilename(String jobFilename) {
		_jobFilename = jobFilename;
		updateWindowTitle();
	}

	@Override
	public String getJobFilename() {
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
		if (!StringUtils.isNullOrEmpty(_jobFilename)) {
			title = _jobFilename + " | " + title;
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

		_sourceTabOuterPanel.add(_datastoreListPanelRef.get());
		_sourceTabOuterPanel.add(_sourceColumnsPanel);

		// add source tab
		_tabbedPane.addTab("Source",
				imageManager.getImageIcon("images/model/source.png"),
				WidgetUtils.scrolleable(_sourceTabOuterPanel));
		_tabbedPane.setRightClickActionListener(SOURCE_TAB,
				new HideTabTextActionListener(_tabbedPane, SOURCE_TAB));
		_tabbedPane.setUnclosableTab(SOURCE_TAB);

		// add metadata tab
		_tabbedPane.addTab("Metadata",
				imageManager.getImageIcon("images/model/metadata.png"),
				_metadataPanel);
		_tabbedPane.setRightClickActionListener(METADATA_TAB,
				new HideTabTextActionListener(_tabbedPane, METADATA_TAB));
		_tabbedPane.setUnclosableTab(METADATA_TAB);

		// add separator for fixed vs dynamic tabs
		_tabbedPane.addSeparator();

		_saveButton.addActionListener(_saveAnalysisJobActionListenerProvider
				.get());

		_visualizeButton.setToolTipText("Visualize execution flow");
		_visualizeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				VisualizeJobWindow window = new VisualizeJobWindow(
						_analysisJobBuilder, getWindowContext());
				window.setVisible(true);
			}
		});

		// Transform button
		_transformButton
				.addActionListener(_addTransformerActionListenerProvider.get());

		// Analyze button
		_analyzeButton.addActionListener(_addAnalyzerActionListenerProvider
				.get());

		// Run analysis
		final RunAnalysisActionListener runAnalysisActionListener = _runAnalysisActionProvider
				.get();
		_runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applyPropertyValues();

				// TODO: Also support exploring analyzers

				runAnalysisActionListener.actionPerformed(e);
			}
		});

		_saveButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		_saveButton.setFocusPainted(false);
		_visualizeButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		_visualizeButton.setFocusPainted(false);
		_analyzeButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		_analyzeButton.setFocusPainted(false);
		_runButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		_runButton.setFocusPainted(false);

		final JToolBar toolBar = WidgetFactory.createToolBar();
		toolBar.add(_saveButton);
		toolBar.add(_visualizeButton);
		toolBar.add(WidgetFactory.createToolBarSeparator());
		toolBar.add(_transformButton);
		toolBar.add(_analyzeButton);
		toolBar.add(WidgetFactory.createToolBarSeparator());
		toolBar.add(_runButton);

		final JXStatusBar statusBar = WidgetFactory
				.createStatusBar(_statusLabel);

		final LoginStatusLabel loggedInStatusLabel = _injectorWithGlassPane
				.getInstance(LoginStatusLabel.class);
		statusBar.add(loggedInStatusLabel);

		final DCPanel toolBarPanel = new DCPanel(
				WidgetUtils.BG_COLOR_LESS_DARK, WidgetUtils.BG_COLOR_DARK);
		toolBarPanel.setLayout(new BorderLayout());
		toolBarPanel.add(toolBar, BorderLayout.CENTER);

		final DCPanel panel = new DCPersistentSizedPanel(_userPreferences,
				getClass().getName(), DEFAULT_WINDOW_WIDTH,
				DEFAULT_WINDOW_HEIGHT);
		panel.setLayout(new BorderLayout());
		panel.add(toolBarPanel, BorderLayout.NORTH);
		panel.add(_leftPanel, BorderLayout.WEST);
		panel.add(_tabbedPane, BorderLayout.CENTER);
		panel.add(statusBar, BorderLayout.SOUTH);

		WidgetUtils.centerOnScreen(this);

		initializeExistingComponents();

		return panel;
	}

	/**
	 * Applies property values for all components visible in the window.
	 */
	@Override
	public void applyPropertyValues() {
		for (FilterJobBuilderPresenter presenter : _filterPresenters.values()) {
			presenter.applyPropertyValues();
		}

		for (TransformerJobBuilderPresenter presenter : _transformerPresenters
				.values()) {
			presenter.applyPropertyValues();
		}

		for (AnalyzerJobBuilderPresenter presenter : _analyzerPresenters
				.values()) {
			presenter.applyPropertyValues();
		}
	}

	/**
	 * Method used to initialize any components that may be in the
	 * AnalysisJobBuilder before this window has been created. Typically this
	 * will only happen when opening a saved job.
	 */
	private void initializeExistingComponents() {
		List<FilterJobBuilder<?, ?>> filterJobBuilders = _analysisJobBuilder
				.getFilterJobBuilders();
		for (FilterJobBuilder<?, ?> fjb : filterJobBuilders) {
			onAdd(fjb);
		}

		List<TransformerJobBuilder<?>> transformerJobBuilders = _analysisJobBuilder
				.getTransformerJobBuilders();
		for (TransformerJobBuilder<?> tjb : transformerJobBuilders) {
			onAdd(tjb);
		}

		List<MergedOutcomeJobBuilder> mergedOutcomeJobBuilders = _analysisJobBuilder
				.getMergedOutcomeJobBuilders();
		for (MergedOutcomeJobBuilder mojb : mergedOutcomeJobBuilders) {
			// TODO: onAdd(mojb)
			logger.warn(
					"Job contains unsupported MergedOutcomeJobBuilders: {}",
					mojb);
		}

		List<AnalyzerJobBuilder<?>> analyzerJobBuilders = _analysisJobBuilder
				.getAnalyzerJobBuilders();
		for (AnalyzerJobBuilder<?> ajb : analyzerJobBuilders) {
			onAdd((AnalyzerJobBuilder<?>) ajb);
		}

		// TODO: Support for explorers

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
		_visualizeButton.setEnabled(everythingEnabled);
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
			for (Iterator<AnalyzerJobBuilderPresenter> it = _analyzerPresenters
					.values().iterator(); it.hasNext();) {
				AnalyzerJobBuilderPresenter analyzerPresenter = it.next();
				if (_jobBuilderTabs.get(analyzerPresenter) == panel) {
					_analysisJobBuilder.removeAnalyzer(analyzerPresenter
							.getJobBuilder());
					return;
				}
			}

			// if panel was a transformer panel
			for (Iterator<TransformerJobBuilderPresenter> it = _transformerPresenters
					.values().iterator(); it.hasNext();) {
				TransformerJobBuilderPresenter transformerPresenter = it.next();
				if (_jobBuilderTabs.get(transformerPresenter) == panel) {
					_analysisJobBuilder.removeTransformer(transformerPresenter
							.getJobBuilder());
					return;
				}
			}

			// if panel was a filter panel
			for (Iterator<FilterJobBuilderPresenter> it = _filterPresenters
					.values().iterator(); it.hasNext();) {
				FilterJobBuilderPresenter filterPresenter = it.next();
				if (_jobBuilderTabs.get(filterPresenter) == panel) {
					_analysisJobBuilder.removeFilter(filterPresenter
							.getJobBuilder());
					return;
				}
			}

			// TODO also handle exploring analyzers
		}
		logger.info("Could not handle removal of tab {}, containing {}",
				ev.getTabIndex(), panel);
	}

	@Override
	public void onAdd(ExplorerJobBuilder<?> explorerJobBuilder) {
		_tabbedPane.addTab(LabelUtils.getLabel(explorerJobBuilder), new JLabel(
				"TODO: Exploring analyzer"));
		_tabbedPane.setSelectedIndex(_tabbedPane.getTabCount() - 1);
		updateStatusLabel();
	}

	@Override
	public void onAdd(final AnalyzerJobBuilder<?> analyzerJobBuilder) {
		@SuppressWarnings("unchecked")
		final Renderer<AnalyzerJobBuilder<?>, ? extends ComponentJobBuilderPresenter> renderer = (Renderer<AnalyzerJobBuilder<?>, ? extends ComponentJobBuilderPresenter>) _componentJobBuilderPresenterRendererFactory
				.getRenderer(analyzerJobBuilder,
						ComponentJobBuilderRenderingFormat.class);
		AnalyzerJobBuilderPresenter presenter = (AnalyzerJobBuilderPresenter) renderer
				.render(analyzerJobBuilder);

		_analyzerPresenters.put(analyzerJobBuilder, presenter);
		JComponent comp = presenter.createJComponent();
		_tabbedPane.addTab(LabelUtils.getLabel(analyzerJobBuilder), IconUtils
				.getDescriptorIcon(analyzerJobBuilder.getDescriptor(),
						IconUtils.ICON_SIZE_LARGE), comp);
		_jobBuilderTabs.put(presenter, comp);
		final int tabIndex = _tabbedPane.getTabCount() - 1;
		_tabbedPane.setRightClickActionListener(tabIndex,
				new JobBuilderTabTextActionListener(_analysisJobBuilder,
						analyzerJobBuilder, tabIndex, _tabbedPane));
		_tabbedPane.setDoubleClickActionListener(tabIndex,
				new RenameComponentActionListener(analyzerJobBuilder) {
					@Override
					protected void onNameChanged() {
						_tabbedPane.setTitleAt(tabIndex,
								LabelUtils.getLabel(analyzerJobBuilder));
					}
				});

		_tabbedPane.setSelectedIndex(tabIndex);
		updateStatusLabel();
	}

	@Override
	public void onRemove(ExplorerJobBuilder<?> explorerJobBuilder) {
		// TODO
		updateStatusLabel();
	}

	@Override
	public void onRemove(AnalyzerJobBuilder<?> analyzerJobBuilder) {
		AnalyzerJobBuilderPresenter presenter = _analyzerPresenters
				.remove(analyzerJobBuilder);
		JComponent comp = _jobBuilderTabs.remove(presenter);
		_tabbedPane.remove(comp);
		updateStatusLabel();
	}

	@Override
	public void onAdd(final TransformerJobBuilder<?> transformerJobBuilder) {
		@SuppressWarnings("unchecked")
		final Renderer<TransformerJobBuilder<?>, ? extends ComponentJobBuilderPresenter> renderer = (Renderer<TransformerJobBuilder<?>, ? extends ComponentJobBuilderPresenter>) _componentJobBuilderPresenterRendererFactory
				.getRenderer(transformerJobBuilder,
						ComponentJobBuilderRenderingFormat.class);
		final TransformerJobBuilderPresenter presenter = (TransformerJobBuilderPresenter) renderer
				.render(transformerJobBuilder);

		_transformerPresenters.put(transformerJobBuilder, presenter);
		JComponent comp = presenter.createJComponent();
		_tabbedPane.addTab(LabelUtils.getLabel(transformerJobBuilder),
				IconUtils.getDescriptorIcon(
						transformerJobBuilder.getDescriptor(),
						IconUtils.ICON_SIZE_LARGE), comp);
		_jobBuilderTabs.put(presenter, comp);
		final int tabIndex = _tabbedPane.getTabCount() - 1;
		_tabbedPane.setSelectedIndex(tabIndex);
		_tabbedPane.setRightClickActionListener(tabIndex,
				new JobBuilderTabTextActionListener(_analysisJobBuilder,
						transformerJobBuilder, tabIndex, _tabbedPane));
		_tabbedPane.setDoubleClickActionListener(tabIndex,
				new RenameComponentActionListener(transformerJobBuilder) {
					@Override
					protected void onNameChanged() {
						_tabbedPane.setTitleAt(tabIndex,
								LabelUtils.getLabel(transformerJobBuilder));
					}
				});
		updateStatusLabel();
	}

	@Override
	public void onRemove(TransformerJobBuilder<?> transformerJobBuilder) {
		TransformerJobBuilderPresenter presenter = _transformerPresenters
				.remove(transformerJobBuilder);
		JComponent comp = _jobBuilderTabs.remove(presenter);
		_tabbedPane.remove(comp);
		updateStatusLabel();
	}

	@Override
	public void onOutputChanged(TransformerJobBuilder<?> transformerJobBuilder,
			List<MutableInputColumn<?>> outputColumns) {
		TransformerJobBuilderPresenter presenter = _transformerPresenters
				.get(transformerJobBuilder);
		if (presenter != null) {
			presenter.onOutputChanged(outputColumns);
		}
	}

	@Override
	public void onAdd(final FilterJobBuilder<?, ?> filterJobBuilder) {
		@SuppressWarnings("unchecked")
		final Renderer<FilterJobBuilder<?, ?>, ? extends ComponentJobBuilderPresenter> renderer = (Renderer<FilterJobBuilder<?, ?>, ? extends ComponentJobBuilderPresenter>) _componentJobBuilderPresenterRendererFactory
				.getRenderer(filterJobBuilder,
						ComponentJobBuilderRenderingFormat.class);
		final FilterJobBuilderPresenter presenter = (FilterJobBuilderPresenter) renderer
				.render(filterJobBuilder);

		_filterPresenters.put(filterJobBuilder, presenter);
		JComponent comp = presenter.createJComponent();
		_tabbedPane.addTab(LabelUtils.getLabel(filterJobBuilder), IconUtils
				.getDescriptorIcon(filterJobBuilder.getDescriptor(),
						IconUtils.ICON_SIZE_LARGE), comp);
		_jobBuilderTabs.put(presenter, comp);
		final int tabIndex = _tabbedPane.getTabCount() - 1;
		if (MaxRowsFilterShortcutPanel.isFilter(filterJobBuilder)) {
			// the max rows shortcut must be disabled using checkbox on source
			// tab
			_tabbedPane.setUnclosableTab(tabIndex);
		} else {
			_tabbedPane.setSelectedIndex(tabIndex);
			_tabbedPane.setRightClickActionListener(tabIndex,
					new JobBuilderTabTextActionListener(_analysisJobBuilder,
							filterJobBuilder, tabIndex, _tabbedPane));
			_tabbedPane.setDoubleClickActionListener(tabIndex,
					new RenameComponentActionListener(filterJobBuilder) {
						@Override
						protected void onNameChanged() {
							_tabbedPane.setTitleAt(tabIndex,
									LabelUtils.getLabel(filterJobBuilder));
						}
					});
		}
		updateStatusLabel();
	}

	@Override
	public void onRemove(FilterJobBuilder<?, ?> filterJobBuilder) {
		FilterJobBuilderPresenter presenter = _filterPresenters
				.remove(filterJobBuilder);
		JComponent comp = _jobBuilderTabs.remove(presenter);
		_tabbedPane.remove(comp);

		if (MaxRowsFilterShortcutPanel.isFilter(filterJobBuilder)) {
			_sourceColumnsPanel.getMaxRowsFilterShortcutPanel()
					.resetToDefault();
		}
		updateStatusLabel();
	}

	@Override
	public void onConfigurationChanged(FilterJobBuilder<?, ?> filterJobBuilder) {
		FilterJobBuilderPresenter presenter = _filterPresenters
				.get(filterJobBuilder);
		if (presenter != null) {
			presenter.onConfigurationChanged();
		}
		updateStatusLabel();
	}

	@Override
	public void onRequirementChanged(FilterJobBuilder<?, ?> filterJobBuilder) {
	}

	@Override
	public void onConfigurationChanged(
			TransformerJobBuilder<?> transformerJobBuilder) {
		TransformerJobBuilderPresenter presenter = _transformerPresenters
				.get(transformerJobBuilder);
		if (presenter != null) {
			presenter.onConfigurationChanged();
		}
		updateStatusLabel();
	}

	@Override
	public void onRequirementChanged(
			TransformerJobBuilder<?> transformerJobBuilder) {
		TransformerJobBuilderPresenter presenter = _transformerPresenters
				.get(transformerJobBuilder);
		if (presenter != null) {
			presenter.onRequirementChanged();
		}
	}

	@Override
	public void onConfigurationChanged(ExplorerJobBuilder<?> explorerJobBuilder) {
		updateStatusLabel();
	}

	@Override
	public void onConfigurationChanged(AnalyzerJobBuilder<?> analyzerJobBuilder) {
		AnalyzerJobBuilderPresenter presenter = _analyzerPresenters
				.get(analyzerJobBuilder);
		if (presenter != null) {
			presenter.onConfigurationChanged();
		}
		updateStatusLabel();
	}

	@Override
	public void onRequirementChanged(AnalyzerJobBuilder<?> analyzerJobBuilder) {
		AnalyzerJobBuilderPresenter presenter = _analyzerPresenters
				.get(analyzerJobBuilder);
		if (presenter != null) {
			presenter.onRequirementChanged();
		}
	}

	@Override
	public void onAdd(InputColumn<?> sourceColumn) {
		onSourceColumnsChanged();
		updateStatusLabel();
	}

	@Override
	public void onRemove(InputColumn<?> sourceColumn) {
		onSourceColumnsChanged();
		updateStatusLabel();
	}
}
