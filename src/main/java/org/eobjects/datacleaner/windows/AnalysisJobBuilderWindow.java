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
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerChangeListener;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.ExploringAnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterChangeListener;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.MergedOutcomeJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.SourceColumnChangeListener;
import org.eobjects.analyzer.job.builder.TransformerChangeListener;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.job.builder.UnconfiguredConfiguredPropertyException;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.actions.AddAnalyzerActionListener;
import org.eobjects.datacleaner.actions.AddTransformerActionListener;
import org.eobjects.datacleaner.actions.HideTabTextActionListener;
import org.eobjects.datacleaner.actions.JobBuilderTabTextActionListener;
import org.eobjects.datacleaner.actions.RunAnalysisActionListener;
import org.eobjects.datacleaner.actions.SaveAnalysisJobActionListener;
import org.eobjects.datacleaner.panels.AbstractJobBuilderPanel;
import org.eobjects.datacleaner.panels.DCGlassPane;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.panels.FilterListPanel;
import org.eobjects.datacleaner.panels.MetadataPanel;
import org.eobjects.datacleaner.panels.RowProcessingAnalyzerJobBuilderPanel;
import org.eobjects.datacleaner.panels.SchemaTreePanel;
import org.eobjects.datacleaner.panels.SourceColumnsPanel;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.LabelUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.CollapsibleTreePanel;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.DCPopupBubble;
import org.eobjects.datacleaner.widgets.DCWindowMenuBar;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.eobjects.datacleaner.widgets.tabs.TabCloseEvent;
import org.eobjects.datacleaner.widgets.tabs.TabCloseListener;
import org.jdesktop.swingx.JXStatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AnalysisJobBuilderWindow extends AbstractWindow implements AnalyzerChangeListener,
		TransformerChangeListener, FilterChangeListener, SourceColumnChangeListener, TabCloseListener {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(AnalysisJobBuilderWindow.class);
	private static final ImageManager imageManager = ImageManager.getInstance();

	private static final int SOURCE_TAB = 0;
	private static final int METADATA_TAB = 1;
	private static final int FILTERS_TAB = 2;

	private final Map<RowProcessingAnalyzerJobBuilder<?>, RowProcessingAnalyzerJobBuilderPanel> _rowProcessingTabPanels = new LinkedHashMap<RowProcessingAnalyzerJobBuilder<?>, RowProcessingAnalyzerJobBuilderPanel>();
	private final Map<TransformerJobBuilder<?>, TransformerJobBuilderPanel> _transformerTabPanels = new LinkedHashMap<TransformerJobBuilder<?>, TransformerJobBuilderPanel>();
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final AnalyzerBeansConfiguration _configuration;
	private final CloseableTabbedPane _tabbedPane;
	private final FilterListPanel _filterListPanel;
	private final DCLabel _statusLabel = DCLabel.bright("");
	private final CollapsibleTreePanel _leftPanel;
	private final SourceColumnsPanel _sourceColumnsPanel;
	private volatile AbstractJobBuilderPanel _latestPanel = null;
	private final SchemaTreePanel _schemaTreePanel;
	private final JButton _saveButton;
	private final JButton _visualizeButton;
	private final JButton _addTransformerButton;
	private final JButton _addAnalyzerButton;
	private final JButton _runButton;
	private final DCGlassPane _glassPane;
	private String _jobFilename;
	private Datastore _datastore;
	private DataContextProvider _dataContextProvider;

	public AnalysisJobBuilderWindow(AnalyzerBeansConfiguration configuration) {
		this(configuration, (Datastore) null);
	}

	public AnalysisJobBuilderWindow(AnalyzerBeansConfiguration configuration, AnalysisJobBuilder analysisJobBuilder,
			String jobFilename) {
		this(configuration, analysisJobBuilder, analysisJobBuilder.getDataContextProvider().getDatastore());
		setJobFilename(jobFilename);
	}

	public AnalysisJobBuilderWindow(AnalyzerBeansConfiguration configuration, Datastore datastore) {
		this(configuration, new AnalysisJobBuilder(configuration), datastore);

	}

	public AnalysisJobBuilderWindow(AnalyzerBeansConfiguration configuration, String datastoreName) {
		this(configuration, new AnalysisJobBuilder(configuration), configuration.getDatastoreCatalog().getDatastore(
				datastoreName));
	}

	private AnalysisJobBuilderWindow(final AnalyzerBeansConfiguration configuration,
			final AnalysisJobBuilder analysisJobBuilder, final Datastore datastore) {
		super();
		_configuration = configuration;
		setJMenuBar(new DCWindowMenuBar(_configuration));
		_analysisJobBuilder = analysisJobBuilder;
		_glassPane = new DCGlassPane(this);

		_analysisJobBuilder.getAnalyzerChangeListeners().add(this);
		_analysisJobBuilder.getTransformerChangeListeners().add(this);
		_analysisJobBuilder.getFilterChangeListeners().add(this);
		_analysisJobBuilder.getSourceColumnListeners().add(this);

		_saveButton = new JButton("Save analysis job", imageManager.getImageIcon("images/actions/save.png"));
		_visualizeButton = new JButton("Visualize", imageManager.getImageIcon("images/actions/visualize.png"));
		_addTransformerButton = new JButton("Add transformer", imageManager.getImageIcon(IconUtils.TRANSFORMER_IMAGEPATH));
		_addAnalyzerButton = new JButton("Add analyzer", imageManager.getImageIcon(IconUtils.ANALYZER_IMAGEPATH));
		_runButton = new JButton("Run analysis", imageManager.getImageIcon("images/actions/execute.png"));

		_sourceColumnsPanel = new SourceColumnsPanel(_analysisJobBuilder, _configuration);
		_filterListPanel = new FilterListPanel(_configuration, _analysisJobBuilder);
		_filterListPanel.addPreconfiguredPresenter(_sourceColumnsPanel.getMaxRowsFilterShortcutPanel());

		_tabbedPane = new CloseableTabbedPane();
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
		final MetadataPanel metadataPanel = new MetadataPanel(_analysisJobBuilder);

		final DCPanel sourceTabOuterPanel = new DCPanel(imageManager.getImage("images/window/source-tab-background.png"),
				95, 95, WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		sourceTabOuterPanel.setLayout(new BorderLayout());
		_tabbedPane.addTab("Source", imageManager.getImageIcon("images/model/source.png"), sourceTabOuterPanel);
		_tabbedPane.setRightClickActionListener(0, new HideTabTextActionListener(_tabbedPane, 0));
		_tabbedPane.addTab("Metadata", imageManager.getImageIcon("images/model/metadata.png"), metadataPanel);
		_tabbedPane.setRightClickActionListener(1, new HideTabTextActionListener(_tabbedPane, 1));
		_tabbedPane.addTab("Filters", imageManager.getImageIcon(IconUtils.FILTER_IMAGEPATH),
				WidgetUtils.scrolleable(_filterListPanel));
		_tabbedPane.setRightClickActionListener(2, new HideTabTextActionListener(_tabbedPane, 2));

		_tabbedPane.setUnclosableTab(SOURCE_TAB);
		_tabbedPane.setUnclosableTab(METADATA_TAB);
		_tabbedPane.setUnclosableTab(FILTERS_TAB);

		_tabbedPane.addSeparator();

		_schemaTreePanel = new SchemaTreePanel(_analysisJobBuilder);
		_leftPanel = new CollapsibleTreePanel(_schemaTreePanel);
		_leftPanel.setCollapsed(datastore == null);
		_schemaTreePanel.setUpdatePanel(_leftPanel);

		setDatastore(datastore);
	}

	public void setDatastore(final Datastore datastore) {
		final DataContextProvider dcp;
		if (datastore == null) {
			dcp = null;
		} else {
			dcp = datastore.getDataContextProvider();
		}

		_datastore = datastore;
		if (_dataContextProvider != null) {
			_dataContextProvider.close();
		}
		_dataContextProvider = dcp;
		_analysisJobBuilder.setDatastore(datastore);
		_schemaTreePanel.setDatastore(datastore);

		if (datastore == null) {
			_analysisJobBuilder.reset();
			displayDatastoreSelection();
		} else {
			displaySourceColumnsList();
		}

		updateStatusLabel();
	}

	private void displaySourceColumnsList() {
		if (_leftPanel.isCollapsed()) {
			_leftPanel.setCollapsed(false);
		}

		DCPanel panel = (DCPanel) _tabbedPane.getComponentAt(SOURCE_TAB);
		panel.removeAll();
		panel.add(WidgetUtils.scrolleable(_sourceColumnsPanel));
	}

	private void displayDatastoreSelection() {
		if (isShowing()) {
			if (_datastore == null) {
				if (!_leftPanel.isCollapsed()) {
					_leftPanel.setCollapsed(true);
				}
				final SelectDatastorePanel selectDatastoresPanel = new SelectDatastorePanel(_configuration, this, _glassPane);
				selectDatastoresPanel.setBorder(new EmptyBorder(4, 4, 0, 150));

				final DCPanel panel = (DCPanel) _tabbedPane.getComponentAt(SOURCE_TAB);
				panel.removeAll();
				panel.add(selectDatastoresPanel);
				panel.updateUI();
			}
		}
	}

	@Override
	protected void onWindowVisible() {
		displayDatastoreSelection();
	}

	private void updateStatusLabel() {
		boolean success = false;
		try {
			if (_analysisJobBuilder.isConfigured(true)) {
				success = true;
				_statusLabel.setText("Job is correctly configured");
				_statusLabel.setIcon(imageManager.getImageIcon("images/status/valid.png", IconUtils.ICON_SIZE_SMALL));
			} else {
				_statusLabel.setText("Job is not correctly configured");
				_statusLabel.setIcon(imageManager.getImageIcon("images/status/warning.png", IconUtils.ICON_SIZE_SMALL));
			}
		} catch (Exception ex) {
			logger.debug("Job not correctly configured", ex);
			final String errorMessage;
			if (ex instanceof UnconfiguredConfiguredPropertyException) {
				ConfiguredPropertyDescriptor configuredProperty = ((UnconfiguredConfiguredPropertyException) ex)
						.getConfiguredProperty();
				AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder = ((UnconfiguredConfiguredPropertyException) ex)
						.getBeanJobBuilder();
				errorMessage = "Property '" + configuredProperty.getName() + "' in " + LabelUtils.getLabel(beanJobBuilder)
						+ " is not set!";
			} else {
				errorMessage = ex.getMessage();
			}
			_statusLabel.setText("Job error status: " + errorMessage);
			_statusLabel.setIcon(imageManager.getImageIcon("images/status/error.png", IconUtils.ICON_SIZE_SMALL));
		}
		_runButton.setEnabled(success);
	}

	public String getStatusLabelText() {
		return _statusLabel.getText();
	}

	@Override
	protected boolean onWindowClosing() {
		boolean windowClosing = super.onWindowClosing();
		if (windowClosing) {
			if (_datastore != null) {
				resetJob();
				return false;
			} else {
				_analysisJobBuilder.getAnalyzerChangeListeners().remove(this);
				_analysisJobBuilder.getTransformerChangeListeners().remove(this);
				_analysisJobBuilder.getFilterChangeListeners().remove(this);
				_analysisJobBuilder.getSourceColumnListeners().remove(this);
				_analysisJobBuilder.close();
				if (_dataContextProvider != null) {
					_dataContextProvider.close();
				}
			}
		}
		return windowClosing;
	}

	private void resetJob() {
		setDatastore(null);
	}

	public void setJobFilename(String jobFilename) {
		_jobFilename = jobFilename;
		updateWindowTitle();
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
		_saveButton.addActionListener(new SaveAnalysisJobActionListener(this, _analysisJobBuilder));

		_visualizeButton.setToolTipText("Visualize execution flow");
		_visualizeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				VisualizeJobWindow window = new VisualizeJobWindow(_analysisJobBuilder);
				window.setVisible(true);
			}
		});

		// Add transformer
		_addTransformerButton.addActionListener(new AddTransformerActionListener(_configuration, _analysisJobBuilder));

		// Add analyzer
		_addAnalyzerButton.addActionListener(new AddAnalyzerActionListener(_configuration, _analysisJobBuilder));

		// Run analysis
		final RunAnalysisActionListener runAnalysisActionListener = new RunAnalysisActionListener(_analysisJobBuilder,
				_configuration, _jobFilename);
		_runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_filterListPanel.applyPropertyValues();

				for (TransformerJobBuilderPanel panel : _transformerTabPanels.values()) {
					panel.applyPropertyValues();
				}

				for (RowProcessingAnalyzerJobBuilderPanel panel : _rowProcessingTabPanels.values()) {
					panel.applyPropertyValues();
				}

				// TODO: Also support exploring analyzers

				runAnalysisActionListener.actionPerformed(e);
			}
		});

		_saveButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		_saveButton.setFocusPainted(false);
		_visualizeButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		_visualizeButton.setFocusPainted(false);
		_addTransformerButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		_addTransformerButton.setFocusPainted(false);
		_addAnalyzerButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		_addAnalyzerButton.setFocusPainted(false);
		_runButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		_runButton.setFocusPainted(false);

		final JToolBar toolBar = WidgetFactory.createToolBar();
		toolBar.add(_saveButton);
		toolBar.add(_visualizeButton);
		toolBar.add(WidgetFactory.createToolBarSeparator());
		toolBar.add(_addTransformerButton);
		toolBar.add(_addAnalyzerButton);
		toolBar.add(WidgetFactory.createToolBarSeparator());
		toolBar.add(_runButton);

		final JXStatusBar statusBar = WidgetFactory.createStatusBar(_statusLabel);

		final Dimension windowSize = new Dimension(880, 630);

		final DCPanel toolBarPanel = new DCPanel(WidgetUtils.BG_COLOR_LESS_DARK, WidgetUtils.BG_COLOR_DARK);
		toolBarPanel.setLayout(new BorderLayout());
		toolBarPanel.add(toolBar, BorderLayout.CENTER);

		final DCPanel panel = new DCPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(windowSize);
		panel.setPreferredSize(windowSize);
		panel.add(toolBarPanel, BorderLayout.NORTH);
		panel.add(_leftPanel, BorderLayout.WEST);
		panel.add(_tabbedPane, BorderLayout.CENTER);
		panel.add(statusBar, BorderLayout.SOUTH);

		WidgetUtils.centerOnScreen(this);

		initializeExistingComponents();

		return panel;
	}

	/**
	 * Method used to initialize any components that may be in the
	 * AnalysisJobBuilder before this window has been created. Typically this
	 * will only happen when opening a saved job.
	 */
	private void initializeExistingComponents() {
		_filterListPanel.initializeExistingComponents();

		List<TransformerJobBuilder<?>> transformerJobBuilders = _analysisJobBuilder.getTransformerJobBuilders();
		for (TransformerJobBuilder<?> tjb : transformerJobBuilders) {
			onAdd(tjb);
		}

		List<MergedOutcomeJobBuilder> mergedOutcomeJobBuilders = _analysisJobBuilder.getMergedOutcomeJobBuilders();
		for (MergedOutcomeJobBuilder mojb : mergedOutcomeJobBuilders) {
			// TODO: onAdd(mojb)
			logger.warn("Job contains unsupported MergedOutcomeJobBuilders: {}", mojb);
		}

		List<AnalyzerJobBuilder<?>> analyzerJobBuilders = _analysisJobBuilder.getAnalyzerJobBuilders();
		for (AnalyzerJobBuilder<?> ajb : analyzerJobBuilders) {
			if (ajb instanceof RowProcessingAnalyzerJobBuilder<?>) {
				onAdd((RowProcessingAnalyzerJobBuilder<?>) ajb);
			} else if (ajb instanceof ExploringAnalyzerJobBuilder<?>) {
				onAdd((ExploringAnalyzerJobBuilder<?>) ajb);
			} else {
				throw new IllegalStateException("Unknown analyzer type: " + ajb);
			}
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
		_visualizeButton.setEnabled(everythingEnabled);
		_addTransformerButton.setEnabled(everythingEnabled);
		_addAnalyzerButton.setEnabled(everythingEnabled);
	}

	@Override
	protected boolean isCentered() {
		return true;
	}

	@Override
	public void tabClosed(TabCloseEvent ev) {
		Component panel = ev.getTabContents();

		if (panel != null) {
			if (panel instanceof RowProcessingAnalyzerJobBuilderPanel) {
				for (Iterator<RowProcessingAnalyzerJobBuilderPanel> it = _rowProcessingTabPanels.values().iterator(); it
						.hasNext();) {
					RowProcessingAnalyzerJobBuilderPanel analyzerPanel = it.next();
					if (analyzerPanel == panel) {
						_analysisJobBuilder.removeAnalyzer(analyzerPanel.getAnalyzerJobBuilder());
						return;
					}
				}
			} else if (panel instanceof TransformerJobBuilderPanel) {
				for (Iterator<TransformerJobBuilderPanel> it = _transformerTabPanels.values().iterator(); it.hasNext();) {
					TransformerJobBuilderPanel transformerPanel = it.next();
					if (transformerPanel == panel) {
						_analysisJobBuilder.removeTransformer(transformerPanel.getTransformerJobBuilder());
						return;
					}
				}
			}
			// TODO also handle exploring analyzers
		}
		logger.warn("Could not handle removal of tab {}, containing {}", ev.getTabIndex(), panel);
	}

	@Override
	public void onAdd(ExploringAnalyzerJobBuilder<?> analyzerJobBuilder) {
		_tabbedPane.addTab(LabelUtils.getLabel(analyzerJobBuilder), new JLabel("TODO: Exploring analyzer"));
		_tabbedPane.setSelectedIndex(_tabbedPane.getTabCount() - 1);
		updateStatusLabel();
	}

	@Override
	public void onAdd(RowProcessingAnalyzerJobBuilder<?> analyzerJobBuilder) {
		RowProcessingAnalyzerJobBuilderPanel panel = new RowProcessingAnalyzerJobBuilderPanel(_analysisJobBuilder,
				analyzerJobBuilder);
		_rowProcessingTabPanels.put(analyzerJobBuilder, panel);
		_tabbedPane.addTab(LabelUtils.getLabel(analyzerJobBuilder),
				IconUtils.getDescriptorIcon(analyzerJobBuilder.getDescriptor()), panel);
		final int tabIndex = _tabbedPane.getTabCount() - 1;
		_tabbedPane.setRightClickActionListener(tabIndex, new JobBuilderTabTextActionListener(_analysisJobBuilder,
				analyzerJobBuilder, tabIndex, _tabbedPane));
		_tabbedPane.setSelectedIndex(tabIndex);
		updateStatusLabel();
	}

	@Override
	public void onRemove(ExploringAnalyzerJobBuilder<?> analyzerJobBuilder) {
		// TODO
		updateStatusLabel();
	}

	@Override
	public void onRemove(RowProcessingAnalyzerJobBuilder<?> analyzerJobBuilder) {
		RowProcessingAnalyzerJobBuilderPanel panel = _rowProcessingTabPanels.remove(analyzerJobBuilder);
		_tabbedPane.remove(panel);
		updateStatusLabel();
	}

	@Override
	public void onAdd(TransformerJobBuilder<?> transformerJobBuilder) {
		final TransformerJobBuilderPanel panel = new TransformerJobBuilderPanel(_analysisJobBuilder, transformerJobBuilder,
				_configuration);
		_transformerTabPanels.put(transformerJobBuilder, panel);
		_tabbedPane.addTab(LabelUtils.getLabel(transformerJobBuilder),
				IconUtils.getDescriptorIcon(transformerJobBuilder.getDescriptor()), panel);
		final int tabIndex = _tabbedPane.getTabCount() - 1;
		_tabbedPane.setSelectedIndex(tabIndex);
		_tabbedPane.setRightClickActionListener(tabIndex, new JobBuilderTabTextActionListener(_analysisJobBuilder,
				transformerJobBuilder, tabIndex, _tabbedPane));
		updateStatusLabel();
	}

	@Override
	public void onRemove(TransformerJobBuilder<?> transformerJobBuilder) {
		TransformerJobBuilderPanel panel = _transformerTabPanels.remove(transformerJobBuilder);
		_tabbedPane.remove(panel);
		updateStatusLabel();
	}

	@Override
	public void onOutputChanged(TransformerJobBuilder<?> transformerJobBuilder, List<MutableInputColumn<?>> outputColumns) {
		TransformerJobBuilderPanel panel = _transformerTabPanels.get(transformerJobBuilder);
		if (panel != null) {
			panel.onOutputChanged(outputColumns);
		}
	}

	@Override
	public void onAdd(final FilterJobBuilder<?, ?> filterJobBuilder) {
		FilterJobBuilder<?, ?> maxRowsFilterJobBuilder = _sourceColumnsPanel.getMaxRowsFilterShortcutPanel()
				.getFilterJobBuilder();
		if (filterJobBuilder == maxRowsFilterJobBuilder) {
			// draw a "think bubble" near the filter tab stating that the filter
			// was added.
			final Rectangle filterTabBounds = _tabbedPane.getTabBounds(FILTERS_TAB);
			final Point tpLocation = _tabbedPane.getLocationOnScreen();

			final int x = filterTabBounds.x + tpLocation.x + 50;
			final int y = filterTabBounds.y + tpLocation.y + filterTabBounds.height;

			final DCPopupBubble popupBubble = new DCPopupBubble(_glassPane, "<html>'<b>"
					+ LabelUtils.getLabel(filterJobBuilder) + "</b>'<br/>added to <b>filters</b></html>", x, y,
					"images/menu/filter-tab.png");
			popupBubble.showTooltip(2000);
		} else {
			_tabbedPane.setSelectedIndex(FILTERS_TAB);
		}
		updateStatusLabel();
	}

	@Override
	public void onRemove(FilterJobBuilder<?, ?> filterJobBuilder) {
		updateStatusLabel();
	}

	@Override
	public void onConfigurationChanged(FilterJobBuilder<?, ?> filterJobBuilder) {
		updateStatusLabel();
	}

	@Override
	public void onRequirementChanged(FilterJobBuilder<?, ?> filterJobBuilder) {
	}

	@Override
	public void onConfigurationChanged(TransformerJobBuilder<?> transformerJobBuilder) {
		TransformerJobBuilderPanel panel = _transformerTabPanels.get(transformerJobBuilder);
		if (panel != null) {
			panel.getPropertyWidgetFactory().onConfigurationChanged();
		}
		updateStatusLabel();
	}

	@Override
	public void onRequirementChanged(TransformerJobBuilder<?> transformerJobBuilder) {
		TransformerJobBuilderPanel panel = _transformerTabPanels.get(transformerJobBuilder);
		if (panel != null) {
			panel.onRequirementChanged();
		}
	}

	@Override
	public void onConfigurationChanged(ExploringAnalyzerJobBuilder<?> analyzerJobBuilder) {
		updateStatusLabel();
	}

	@Override
	public void onConfigurationChanged(RowProcessingAnalyzerJobBuilder<?> analyzerJobBuilder) {
		RowProcessingAnalyzerJobBuilderPanel panel = _rowProcessingTabPanels.get(analyzerJobBuilder);
		if (panel != null) {
			panel.getPropertyWidgetFactory().onConfigurationChanged();
		}
		updateStatusLabel();
	}

	@Override
	public void onRequirementChanged(RowProcessingAnalyzerJobBuilder<?> analyzerJobBuilder) {
		RowProcessingAnalyzerJobBuilderPanel panel = _rowProcessingTabPanels.get(analyzerJobBuilder);
		if (panel != null) {
			panel.onRequirementChanged();
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
