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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerChangeListener;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.ExploringAnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterChangeListener;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.MergedOutcomeJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerChangeListener;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.actions.AddAnalyzerActionListener;
import org.eobjects.datacleaner.actions.AddTransformerActionListener;
import org.eobjects.datacleaner.actions.RunAnalysisActionListener;
import org.eobjects.datacleaner.actions.SaveAnalysisJobActionListener;
import org.eobjects.datacleaner.panels.AbstractJobBuilderPanel;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.panels.FilterListPanel;
import org.eobjects.datacleaner.panels.RowProcessingAnalyzerJobBuilderPanel;
import org.eobjects.datacleaner.panels.SourceColumnsPanel;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.eobjects.datacleaner.widgets.tabs.TabCloseEvent;
import org.eobjects.datacleaner.widgets.tabs.TabCloseListener;
import org.eobjects.datacleaner.widgets.tree.SchemaTree;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AnalysisJobBuilderWindow extends AbstractWindow implements AnalyzerChangeListener,
		TransformerChangeListener, FilterChangeListener, TabCloseListener {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(AnalysisJobBuilderWindow.class);

	private static final int SOURCE_TAB = 0;
	private static final int METADATA_TAB = 1;
	private static final int FILTERS_TAB = 2;

	private final Map<RowProcessingAnalyzerJobBuilder<?>, RowProcessingAnalyzerJobBuilderPanel> _rowProcessingTabPanels = new LinkedHashMap<RowProcessingAnalyzerJobBuilder<?>, RowProcessingAnalyzerJobBuilderPanel>();
	private final Map<TransformerJobBuilder<?>, TransformerJobBuilderPanel> _transformerTabPanels = new LinkedHashMap<TransformerJobBuilder<?>, TransformerJobBuilderPanel>();
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final AnalyzerBeansConfiguration _configuration;
	private final Datastore _datastore;
	private final DataContextProvider _dataContextProvider;
	private final CloseableTabbedPane _tabbedPane;
	private final FilterListPanel _filterListPanel;
	private final JLabel _statusLabel = new JLabel();

	private volatile AbstractJobBuilderPanel _latestPanel = null;
	private String _jobFilename;

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

	private AnalysisJobBuilderWindow(AnalyzerBeansConfiguration configuration, AnalysisJobBuilder ajb, Datastore datastore) {
		_configuration = configuration;
		_datastore = datastore;
		_dataContextProvider = _datastore.getDataContextProvider();
		_analysisJobBuilder = ajb;
		_analysisJobBuilder.setDatastore(datastore);
		_analysisJobBuilder.getAnalyzerChangeListeners().add(this);
		_analysisJobBuilder.getTransformerChangeListeners().add(this);
		_analysisJobBuilder.getFilterChangeListeners().add(this);
		_filterListPanel = new FilterListPanel(_configuration, _analysisJobBuilder);
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
		updateStatusLabel();
	}

	private void updateStatusLabel() {
		ImageManager imageManager = ImageManager.getInstance();
		try {
			if (_analysisJobBuilder.isConfigured(true)) {
				_statusLabel.setText("Job is correctly configured");
				_statusLabel.setIcon(imageManager.getImageIcon("images/status/valid.png", IconUtils.ICON_SIZE_SMALL));
			} else {
				_statusLabel.setText("Job is not correctly configured");
				_statusLabel.setIcon(imageManager.getImageIcon("images/status/warning.png", IconUtils.ICON_SIZE_SMALL));
			}
		} catch (Exception ex) {
			_statusLabel.setText("Job error status: " + ex.getMessage());
			_statusLabel.setIcon(imageManager.getImageIcon("images/status/error.png", IconUtils.ICON_SIZE_SMALL));
		}
	}

	public String getStatusLabelText() {
		return _statusLabel.getText();
	}

	@Override
	protected boolean onWindowClosing() {
		boolean windowClosing = super.onWindowClosing();
		if (windowClosing) {
			_analysisJobBuilder.getAnalyzerChangeListeners().remove(this);
			_analysisJobBuilder.getTransformerChangeListeners().remove(this);
			_analysisJobBuilder.close();
			_dataContextProvider.close();
		}
		return windowClosing;
	}

	public void setJobFilename(String jobFilename) {
		_jobFilename = jobFilename;
		updateWindowTitle();
	}

	@Override
	protected String getWindowTitle() {
		if (_jobFilename == null) {
			return "Analysis job";
		}
		return _jobFilename + " | Analysis job";
	}

	@Override
	protected Image getWindowIcon() {
		return ImageManager.getInstance().getImage("images/model/job.png");
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	protected JComponent getWindowContent() {
		final ImageManager imageManager = ImageManager.getInstance();

		final JButton saveButton = new JButton("Save analysis job", imageManager.getImageIcon("images/actions/save.png"));
		saveButton.addActionListener(new SaveAnalysisJobActionListener(this, _analysisJobBuilder));

		// Add transformer
		final JButton addTransformerButton = new JButton("Add transformer",
				imageManager.getImageIcon(IconUtils.TRANSFORMER_IMAGEPATH));
		addTransformerButton.addActionListener(new AddTransformerActionListener(_configuration, _analysisJobBuilder));

		// Add analyzer
		final JButton addAnalyzerButton = new JButton("Add analyzer",
				imageManager.getImageIcon(IconUtils.ANALYZER_IMAGEPATH));
		addAnalyzerButton.addActionListener(new AddAnalyzerActionListener(_configuration, _analysisJobBuilder));

		// Run analysis
		final JButton runButton = new JButton("Run analysis", imageManager.getImageIcon("images/actions/execute.png"));
		final RunAnalysisActionListener runAnalysisActionListener = new RunAnalysisActionListener(_analysisJobBuilder,
				_configuration);
		runButton.addActionListener(new ActionListener() {
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

		saveButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		saveButton.setFocusPainted(false);
		addTransformerButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		addTransformerButton.setFocusPainted(false);
		addAnalyzerButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		addAnalyzerButton.setFocusPainted(false);
		runButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		runButton.setFocusPainted(false);
		final JToolBar toolBar = WidgetFactory.createToolBar();
		toolBar.setOpaque(false);
		toolBar.setBorder(null);
		toolBar.add(saveButton);
		toolBar.add(WidgetFactory.createToolBarSeparator());
		toolBar.add(addTransformerButton);
		toolBar.add(addAnalyzerButton);
		toolBar.add(WidgetFactory.createToolBarSeparator());
		toolBar.add(runButton);

		final SchemaTree schemaTree = new SchemaTree(_datastore, _analysisJobBuilder);

		final Image treeBackgroundImage = imageManager.getImage("images/window/schema-tree-background.png");
		final JScrollPane schemaTreeScroll = WidgetUtils.scrolleable(schemaTree);
		schemaTreeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		final DCPanel schemaTreePanel = new DCPanel(treeBackgroundImage, 100, 100, WidgetUtils.BG_COLOR_BRIGHTEST,
				WidgetUtils.BG_COLOR_BRIGHT);
		schemaTreePanel.setLayout(new BorderLayout());
		schemaTreePanel.setBorder(WidgetUtils.BORDER_WIDE);
		schemaTreePanel.add(schemaTreeScroll, BorderLayout.CENTER);

		final SourceColumnsPanel sourceColumnsPanel = new SourceColumnsPanel(_analysisJobBuilder, _configuration);
		final DCPanel metadataPanel = new DCPanel(imageManager.getImage("images/window/metadata-tab-background.png"), 95, 95);

		_tabbedPane.addTab("Source", imageManager.getImageIcon("images/model/source.png"),
				WidgetUtils.scrolleable(sourceColumnsPanel));
		_tabbedPane.addTab("Metadata", imageManager.getImageIcon("images/model/metadata.png"),
				WidgetUtils.scrolleable(metadataPanel));
		_tabbedPane.addTab("Filters", imageManager.getImageIcon(IconUtils.FILTER_IMAGEPATH),
				WidgetUtils.scrolleable(_filterListPanel));

		_tabbedPane.setUnclosableTab(SOURCE_TAB);
		_tabbedPane.setUnclosableTab(METADATA_TAB);
		_tabbedPane.setUnclosableTab(FILTERS_TAB);

		_tabbedPane.addSeparator();

		final JXCollapsiblePane collapsibleTreePane = new JXCollapsiblePane(JXCollapsiblePane.Direction.LEFT);
		collapsibleTreePane.getContentPane().setBackground(WidgetUtils.BG_COLOR_DARK);
		collapsibleTreePane.add(schemaTreePanel);

		final JButton toggleTreeViewButton = new JButton(imageManager.getImageIcon("images/widgets/tree-panel-collapse.png"));
		toggleTreeViewButton.setBorder(null);
		toggleTreeViewButton.setOpaque(false);
		toggleTreeViewButton.setContentAreaFilled(false);
		toggleTreeViewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean collapsed = collapsibleTreePane.isCollapsed();
				if (collapsed) {
					toggleTreeViewButton.setIcon(imageManager.getImageIcon("images/widgets/tree-panel-collapse.png"));
					toggleTreeViewButton.setBorder(null);
				} else {
					toggleTreeViewButton.setIcon(imageManager.getImageIcon("images/widgets/tree-panel-expand.png"));
					toggleTreeViewButton.setBorder(new EmptyBorder(0, 2, 0, 0));
				}
				collapsibleTreePane.setCollapsed(!collapsed);
			}
		});

		final DCPanel collapseButtonPanel = new DCPanel();
		collapseButtonPanel.setOpaque(true);
		collapseButtonPanel.setBackground(WidgetUtils.BG_COLOR_DARK);
		collapseButtonPanel.setLayout(new VerticalLayout(4));
		collapseButtonPanel.setBorder(null);
		collapseButtonPanel.add(toggleTreeViewButton);

		final DCPanel leftPanel = new DCPanel();
		leftPanel.setLayout(new BorderLayout());
		leftPanel.add(collapsibleTreePane, BorderLayout.CENTER);
		leftPanel.add(collapseButtonPanel, BorderLayout.EAST);

		schemaTree.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				leftPanel.updateUI();
			}
		});

		final JXStatusBar statusBar = new JXStatusBar();
		final JXStatusBar.Constraint c1 = new JXStatusBar.Constraint(JXStatusBar.Constraint.ResizeBehavior.FILL);
		statusBar.add(_statusLabel, c1);

		final Dimension windowSize = new Dimension(780, 630);

		final DCPanel toolBarPanel = new DCPanel(WidgetUtils.BG_COLOR_LESS_DARK, WidgetUtils.BG_COLOR_DARK);
		toolBarPanel.setLayout(new BorderLayout());
		toolBarPanel.add(toolBar, BorderLayout.CENTER);

		final DCPanel panel = new DCPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(windowSize);
		panel.setPreferredSize(windowSize);
		panel.add(toolBarPanel, BorderLayout.NORTH);
		panel.add(leftPanel, BorderLayout.WEST);
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
	}

	@Override
	protected boolean isCentered() {
		return true;
	}

	@Override
	public void tabClosing(TabCloseEvent ev) {
		int tabIndex = ev.getClosedTab();
		if (tabIndex > 3) {
			Component panel = _tabbedPane.getComponent(tabIndex);

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
			logger.warn("Could not handle removal of tab {}, containing {}", tabIndex, panel);
		}
	}

	@Override
	public void onAdd(ExploringAnalyzerJobBuilder<?> analyzerJobBuilder) {
		_tabbedPane.addTab(analyzerJobBuilder.getDescriptor().getDisplayName(), new JLabel("TODO: Exploring analyzer"));
		_tabbedPane.setSelectedIndex(_tabbedPane.getTabCount() - 1);
		updateStatusLabel();
	}

	@Override
	public void onAdd(RowProcessingAnalyzerJobBuilder<?> analyzerJobBuilder) {
		RowProcessingAnalyzerJobBuilderPanel panel = new RowProcessingAnalyzerJobBuilderPanel(_analysisJobBuilder,
				analyzerJobBuilder);
		_rowProcessingTabPanels.put(analyzerJobBuilder, panel);
		_tabbedPane.addTab(analyzerJobBuilder.getDescriptor().getDisplayName(),
				IconUtils.getDescriptorIcon(analyzerJobBuilder.getDescriptor()), panel);
		_tabbedPane.setSelectedIndex(_tabbedPane.getTabCount() - 1);
		updateStatusLabel();
	}

	@Override
	public void onRemove(ExploringAnalyzerJobBuilder<?> analyzerJobBuilder) {
		// TODO Auto-generated method stub
		updateStatusLabel();
	}

	@Override
	public void onRemove(RowProcessingAnalyzerJobBuilder<?> analyzerJobBuilder) {
		_rowProcessingTabPanels.remove(analyzerJobBuilder);
		updateStatusLabel();
	}

	@Override
	public void onAdd(TransformerJobBuilder<?> transformerJobBuilder) {
		TransformerJobBuilderPanel panel = new TransformerJobBuilderPanel(_analysisJobBuilder, transformerJobBuilder,
				_configuration);
		_transformerTabPanels.put(transformerJobBuilder, panel);
		_tabbedPane.addTab(transformerJobBuilder.getDescriptor().getDisplayName(),
				IconUtils.getDescriptorIcon(transformerJobBuilder.getDescriptor()), panel);
		_tabbedPane.setSelectedIndex(_tabbedPane.getTabCount() - 1);
		updateStatusLabel();
	}

	@Override
	public void onRemove(TransformerJobBuilder<?> transformerJobBuilder) {
		_transformerTabPanels.remove(transformerJobBuilder);
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
	public void onAdd(FilterJobBuilder<?, ?> filterJobBuilder) {
		_tabbedPane.setSelectedIndex(FILTERS_TAB);
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
}
