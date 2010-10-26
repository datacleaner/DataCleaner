package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerChangeListener;
import org.eobjects.analyzer.job.builder.ExploringAnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterChangeListener;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerChangeListener;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.actions.AddAnalyzerActionListener;
import org.eobjects.datacleaner.actions.AddTransformerActionListener;
import org.eobjects.datacleaner.actions.RunAnalysisActionListener;
import org.eobjects.datacleaner.panels.AbstractJobBuilderPanel;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.panels.FilterListPanel;
import org.eobjects.datacleaner.panels.RowProcessingAnalyzerJobBuilderPanel;
import org.eobjects.datacleaner.panels.SourceColumnsPanel;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.builder.WidgetFactory;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.eobjects.datacleaner.widgets.tabs.TabCloseEvent;
import org.eobjects.datacleaner.widgets.tabs.TabCloseListener;
import org.eobjects.datacleaner.widgets.tree.SchemaTree;
import org.jdesktop.swingx.JXStatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AnalysisJobBuilderWindow extends AbstractWindow implements AnalyzerChangeListener,
		TransformerChangeListener, FilterChangeListener, TabCloseListener {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(AnalysisJobBuilderWindow.class);

	private final AnalysisJobBuilder _analysisJobBuilder;
	private final AnalyzerBeansConfiguration _configuration;
	private final Datastore _datastore;
	private final CloseableTabbedPane _tabbedPane;
	private final List<RowProcessingAnalyzerJobBuilderPanel> _rowProcessingTabPanels = new LinkedList<RowProcessingAnalyzerJobBuilderPanel>();
	private final List<TransformerJobBuilderPanel> _transformerTabPanels = new LinkedList<TransformerJobBuilderPanel>();
	private final FilterListPanel _filterListPanel;
	private final JLabel _statusLabel;

	private volatile AbstractJobBuilderPanel _latestPanel = null;
	
	public AnalysisJobBuilderWindow(AnalyzerBeansConfiguration configuration, String datastoreName) {
		this(configuration, configuration.getDatastoreCatalog().getDatastore(datastoreName));
	}

	public AnalysisJobBuilderWindow(AnalyzerBeansConfiguration configuration, Datastore datastore) {
		_configuration = configuration;
		_datastore = datastore;
		_analysisJobBuilder = new AnalysisJobBuilder(configuration);
		_analysisJobBuilder.setDatastore(datastore);
		_analysisJobBuilder.getAnalyzerChangeListeners().add(this);
		_analysisJobBuilder.getTransformerChangeListeners().add(this);
		_analysisJobBuilder.getFilterChangeListeners().add(this);
		_filterListPanel = new FilterListPanel(this, _configuration, _analysisJobBuilder);
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
		_statusLabel = new JLabel();
		updateStatusLabel();
	}

	public void updateStatusLabel() {
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

	@Override
	protected boolean onWindowClosing() {
		boolean windowClosing = super.onWindowClosing();
		if (windowClosing) {
			_analysisJobBuilder.getAnalyzerChangeListeners().remove(this);
			_analysisJobBuilder.getTransformerChangeListeners().remove(this);
		}
		return windowClosing;
	}

	@Override
	protected String getWindowTitle() {
		return "Analysis job";
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
		DCPanel panel = new DCPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(750, 630);
		panel.setPreferredSize(new Dimension(750, 630));

		JToolBar toolBar = WidgetFactory.createToolBar();

		ImageManager imageManager = ImageManager.getInstance();
		JButton saveButton = new JButton("Save analysis job", imageManager.getImageIcon("images/actions/save.png"));
		toolBar.add(saveButton);

		toolBar.add(new JSeparator(JSeparator.VERTICAL));

		// Add transformer
		JButton addTransformerButton = new JButton("Add transformer",
				imageManager.getImageIcon(IconUtils.TRANSFORMER_IMAGEPATH));
		addTransformerButton.addActionListener(new AddTransformerActionListener(_configuration, _analysisJobBuilder));
		toolBar.add(addTransformerButton);

		// Add analyzer
		JButton addAnalyzerButton = new JButton("Add analyzer", imageManager.getImageIcon(IconUtils.ANALYZER_IMAGEPATH));
		addAnalyzerButton.addActionListener(new AddAnalyzerActionListener(_configuration, _analysisJobBuilder));
		toolBar.add(addAnalyzerButton);

		toolBar.add(new JSeparator(JSeparator.VERTICAL));

		// Run analysis
		JButton runButton = new JButton("Run analysis", imageManager.getImageIcon("images/actions/execute.png"));

		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_filterListPanel.applyPropertyValues();

				for (TransformerJobBuilderPanel panel : _transformerTabPanels) {
					panel.applyPropertyValues();
				}

				for (RowProcessingAnalyzerJobBuilderPanel panel : _rowProcessingTabPanels) {
					panel.applyPropertyValues();
				}

				// TODO: Also support exploring analyzers

				new RunAnalysisActionListener(_analysisJobBuilder, _configuration).actionPerformed(e);
			}
		});
		toolBar.add(runButton);

		panel.add(toolBar, BorderLayout.NORTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setBackground(WidgetUtils.BG_COLOR_DARK);

		SchemaTree schemaTree = new SchemaTree(_datastore, _analysisJobBuilder);
		splitPane.add(schemaTree);

		SourceColumnsPanel sourceColumnsPanel = new SourceColumnsPanel(_analysisJobBuilder, _configuration);
		_tabbedPane.addTab("Source", imageManager.getImageIcon("images/model/source.png"),
				WidgetUtils.scrolleable(sourceColumnsPanel));
		DCPanel metadataPanel = new DCPanel(imageManager.getImage("images/window/metadata-tab-background.png"), 95, 95);
		_tabbedPane.addTab("Metadata", imageManager.getImageIcon("images/model/metadata.png"),
				WidgetUtils.scrolleable(metadataPanel));

		_tabbedPane.addTab("Filters", imageManager.getImageIcon(IconUtils.FILTER_IMAGEPATH),
				WidgetUtils.scrolleable(_filterListPanel));

		_tabbedPane.setUnclosableTab(0);
		_tabbedPane.setUnclosableTab(1);
		_tabbedPane.setUnclosableTab(2);

		_tabbedPane.addSeparator();

		splitPane.add(_tabbedPane);

		panel.add(splitPane, BorderLayout.CENTER);

		JXStatusBar statusBar = new JXStatusBar();
		JXStatusBar.Constraint c1 = new JXStatusBar.Constraint(JXStatusBar.Constraint.ResizeBehavior.FILL);
		statusBar.add(_statusLabel, c1);

		panel.add(statusBar, BorderLayout.SOUTH);

		WidgetUtils.centerOnScreen(this);

		return panel;
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
					for (Iterator<RowProcessingAnalyzerJobBuilderPanel> it = _rowProcessingTabPanels.iterator(); it
							.hasNext();) {
						RowProcessingAnalyzerJobBuilderPanel analyzerPanel = it.next();
						if (analyzerPanel == panel) {
							it.remove();
							_analysisJobBuilder.removeAnalyzer(analyzerPanel.getAnalyzerJobBuilder());
							return;
						}
					}
				} else if (panel instanceof TransformerJobBuilderPanel) {
					for (Iterator<TransformerJobBuilderPanel> it = _transformerTabPanels.iterator(); it.hasNext();) {
						TransformerJobBuilderPanel transformerPanel = it.next();
						if (transformerPanel == panel) {
							it.remove();
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
		updateStatusLabel();
	}

	@Override
	public void onAdd(RowProcessingAnalyzerJobBuilder<?> analyzerJobBuilder) {
		RowProcessingAnalyzerJobBuilderPanel panel = new RowProcessingAnalyzerJobBuilderPanel(this, _analysisJobBuilder,
				analyzerJobBuilder);
		_rowProcessingTabPanels.add(panel);
		_tabbedPane.addTab(analyzerJobBuilder.getDescriptor().getDisplayName(),
				IconUtils.getDescriptorIcon(analyzerJobBuilder.getDescriptor()), panel);
		updateStatusLabel();
	}

	@Override
	public void onRemove(ExploringAnalyzerJobBuilder<?> analyzerJobBuilder) {
		// TODO Auto-generated method stub
		updateStatusLabel();
	}

	@Override
	public void onRemove(RowProcessingAnalyzerJobBuilder<?> analyzerJobBuilder) {
		for (Iterator<RowProcessingAnalyzerJobBuilderPanel> it = _rowProcessingTabPanels.iterator(); it.hasNext();) {
			RowProcessingAnalyzerJobBuilderPanel analyzerPanel = it.next();
			if (analyzerJobBuilder == analyzerPanel.getAnalyzerJobBuilder()) {
				it.remove();
				updateStatusLabel();
				return;
			}
		}
	}

	@Override
	public void onAdd(TransformerJobBuilder<?> transformerJobBuilder) {
		TransformerJobBuilderPanel panel = new TransformerJobBuilderPanel(this, _analysisJobBuilder, transformerJobBuilder,
				_configuration);
		_transformerTabPanels.add(panel);
		_tabbedPane.addTab(transformerJobBuilder.getDescriptor().getDisplayName(),
				IconUtils.getDescriptorIcon(transformerJobBuilder.getDescriptor()), panel);
		updateStatusLabel();
	}

	@Override
	public void onRemove(TransformerJobBuilder<?> transformerJobBuilder) {
		for (Iterator<TransformerJobBuilderPanel> it = _transformerTabPanels.iterator(); it.hasNext();) {
			TransformerJobBuilderPanel panel = it.next();
			if (transformerJobBuilder == panel.getTransformerJobBuilder()) {
				it.remove();
				updateStatusLabel();
				return;
			}
		}
	}

	@Override
	public void onOutputChanged(TransformerJobBuilder<?> transformerJobBuilder, List<MutableInputColumn<?>> outputColumns) {
		for (Iterator<TransformerJobBuilderPanel> it = _transformerTabPanels.iterator(); it.hasNext();) {
			TransformerJobBuilderPanel panel = it.next();
			if (transformerJobBuilder == panel.getTransformerJobBuilder()) {
				panel.setOutputColumns(outputColumns);
				updateStatusLabel();
				return;
			}
		}
	}

	@Override
	public void onAdd(FilterJobBuilder<?, ?> filterJobBuilder) {
		updateStatusLabel();
	}

	@Override
	public void onRemove(FilterJobBuilder<?, ?> filterJobBuilder) {
		updateStatusLabel();
	}
}
