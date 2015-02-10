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
package org.datacleaner.widgets.visualization;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.TransferHandler;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.UnconfiguredConfiguredPropertyException;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.user.UsageLogger;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.DragDropUtils;
import org.datacleaner.util.GraphUtils;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.windows.ComponentConfigurationDialog;
import org.datacleaner.windows.SourceTableConfigurationDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.VisualizationViewer.GraphMouse;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;

/**
 * Class capable of creating graphs that visualize {@link AnalysisJob}s or parts
 * of them as a graph.
 */
public final class JobGraph {

    public static final String MORE_COLUMNS_VERTEX = "...";

    private static final Logger logger = LoggerFactory.getLogger(JobGraph.class);
    private final Map<ComponentBuilder, ComponentConfigurationDialog> _componentConfigurationDialogs;
    private final Map<Table, SourceTableConfigurationDialog> _tableConfigurationDialogs;
    private final Set<Object> _highlighedVertexes;
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final RendererFactory _presenterRendererFactory;
    private final DCPanel _panel;
    private final WindowContext _windowContext;
    private final UsageLogger _usageLogger;
    private final UserPreferences _userPreferences;

    private int _scrollHorizontal;
    private int _scrollVertical;

    public JobGraph(WindowContext windowContext, UserPreferences userPreferences,
            AnalysisJobBuilder analysisJobBuilder, UsageLogger usageLogger) {
        this(windowContext, userPreferences, analysisJobBuilder, null, usageLogger);
    }

    public JobGraph(WindowContext windowContext, UserPreferences userPreferences,
            AnalysisJobBuilder analysisJobBuilder, RendererFactory presenterRendererFactory, UsageLogger usageLogger) {
        _highlighedVertexes = new HashSet<Object>();
        _analysisJobBuilder = analysisJobBuilder;
        _userPreferences = userPreferences;
        _windowContext = windowContext;
        _usageLogger = usageLogger;
        _componentConfigurationDialogs = new IdentityHashMap<>();
        _tableConfigurationDialogs = new IdentityHashMap<>();

        if (presenterRendererFactory == null) {
            _presenterRendererFactory = new RendererFactory(analysisJobBuilder.getConfiguration());
        } else {
            _presenterRendererFactory = presenterRendererFactory;
        }

        _panel = new DCPanel();
        _panel.setLayout(new BorderLayout());
    }

    public JobGraph highlightVertex(Object vertex) {
        _highlighedVertexes.add(vertex);
        return this;
    }

    public DCPanel getPanel() {
        if (_panel.getComponentCount() == 0) {
            refresh();
        }
        _panel.updateUI();
        return _panel;
    }

    public AnalysisJobBuilder getAnalysisJobBuilder() {
        return _analysisJobBuilder;
    }

    public void refresh() {
        final JobGraphNodeBuilder nodeBuilder = new JobGraphNodeBuilder(_analysisJobBuilder);
        final DirectedGraph<Object, JobGraphLink> graph = nodeBuilder.buildGraph();

        final JComponent newComponent = createJComponent(graph);
        _panel.removeAll();
        _panel.add(newComponent, BorderLayout.CENTER);
        _panel.updateUI();
    }

    /**
     * Creates the {@link JComponent} that shows the graph
     * 
     * @return
     */
    private JComponent createJComponent(final DirectedGraph<Object, JobGraphLink> graph) {
        final int vertexCount = graph.getVertexCount();
        logger.debug("Rendering graph with {} vertices", vertexCount);

        final JobGraphLayoutTransformer layoutTransformer = new JobGraphLayoutTransformer(_analysisJobBuilder, graph);
        final Dimension preferredSize = layoutTransformer.getPreferredSize();

        final StaticLayout<Object, JobGraphLink> layout = new StaticLayout<Object, JobGraphLink>(graph,
                layoutTransformer, preferredSize);

        final Collection<Object> vertices = graph.getVertices();
        for (Object vertex : vertices) {
            // manually initialize all vertices
            layout.transform(vertex);
        }

        if (!vertices.isEmpty() && !layoutTransformer.isTransformed()) {
            throw new IllegalStateException("Layout transformer was never invoked!");
        }

        final VisualizationViewer<Object, JobGraphLink> visualizationViewer = new VisualizationViewer<Object, JobGraphLink>(
                layout, preferredSize);
        visualizationViewer.setTransferHandler(new TransferHandler() {

            private static final long serialVersionUID = 1L;

            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DragDropUtils.MODEL_DATA_FLAVOR);
            };

            public boolean importData(TransferSupport support) {
                Transferable transferable = support.getTransferable();

                final Object data;
                try {
                    data = transferable.getTransferData(DragDropUtils.MODEL_DATA_FLAVOR);
                } catch (Exception ex) {
                    logger.warn("Unexpected error while dropping data", ex);
                    return false;
                }

                if (data == null) {
                    return false;
                }

                final Point dropPoint = support.getDropLocation().getDropPoint();

                if (data instanceof Table) {
                    final Table table = (Table) data;
                    // position the table
                    JobGraphMetadata.setPointForTable(_analysisJobBuilder, table, dropPoint.x, dropPoint.y);
                    _analysisJobBuilder.addSourceColumns(table.getColumns());
                }

                if (data instanceof Column) {
                    final Column column = (Column) data;
                    final Table table = column.getTable();
                    final List<MetaModelInputColumn> columnsOfSameTable = _analysisJobBuilder
                            .getSourceColumnsOfTable(table);
                    if (columnsOfSameTable.isEmpty()) {
                        // the table is new - position it
                        JobGraphMetadata.setPointForTable(_analysisJobBuilder, table, dropPoint.x, dropPoint.y);
                    }
                    _analysisJobBuilder.addSourceColumn(column);
                }

                return true;
            };
        });

        GraphUtils.applyStyles(visualizationViewer);
        visualizationViewer.addPreRenderPaintable(new Paintable() {
            @Override
            public boolean useTransform() {
                return false;
            }

            @Override
            public void paint(Graphics g) {
                final GradientPaint paint = new GradientPaint(0, 0, WidgetUtils.BG_COLOR_BRIGHTEST, 0,
                        visualizationViewer.getHeight(), WidgetUtils.BG_COLOR_BRIGHTEST);
                if (g instanceof Graphics2D) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setPaint(paint);
                } else {
                    g.setColor(WidgetUtils.BG_COLOR_BRIGHT);
                }
                g.fillRect(0, 0, visualizationViewer.getWidth(), visualizationViewer.getHeight());

                final Dimension size = _panel.getSize();
                if (size.height < 300) {
                    // don't show the background hints - it will be too
                    // disturbing
                    return;
                }

                final String title;
                String subTitle;
                final String imagePath;

                g.setColor(WidgetUtils.BG_COLOR_MEDIUM);
                if (_analysisJobBuilder.getSourceColumns().size() == 0) {
                    title = "Select source ...";
                    subTitle = "Pick table/columns in the tree to the left.\n"
                            + "You can drag it onto this canvas with your mouse.";
                    imagePath = "images/window/canvas-bg-table.png";
                } else if (_analysisJobBuilder.getComponentCount() == 0) {
                    title = "Start building ...";
                    subTitle = "Add components to your job. Right-click the canvas\n"
                            + "to explore the library of available components.";
                    imagePath = "images/window/canvas-bg-plus.png";
                } else if (graph.getEdgeCount() == 0) {
                    title = "Connect the pieces ...";
                    subTitle = "Right-click the source table and select 'Link to ...'.\n"
                            + "This directs the flow of data to the component.";
                    imagePath = "images/window/canvas-bg-connect.png";
                } else if (_analysisJobBuilder.getAnalyzerComponentBuilders().size() == 0
                        && _analysisJobBuilder.getComponentCount() <= 3) {
                    title = "Your job is almost ready.";
                    subTitle = "Jobs need to either 'Analyze' or 'Write' something.\n"
                            + "So add one or more such components.";
                    imagePath = "images/window/canvas-bg-plus.png";
                } else {
                    title = null;
                    subTitle = null;
                    imagePath = null;

                    try {
                        if (!_analysisJobBuilder.isConfigured(true)) {
                            subTitle = "Job is not correctly configured";
                        }
                    } catch (Exception ex) {
                        logger.debug("Job not correctly configured", ex);
                        final String errorMessage;
                        if (ex instanceof UnconfiguredConfiguredPropertyException) {
                            UnconfiguredConfiguredPropertyException unconfiguredConfiguredPropertyException = (UnconfiguredConfiguredPropertyException) ex;
                            ConfiguredPropertyDescriptor configuredProperty = unconfiguredConfiguredPropertyException
                                    .getConfiguredProperty();
                            ComponentBuilder componentBuilder = unconfiguredConfiguredPropertyException
                                    .getComponentBuilder();
                            errorMessage = "Please set '" + configuredProperty.getName() + "' in "
                                    + LabelUtils.getLabel(componentBuilder) + " to continue";
                        } else {
                            errorMessage = ex.getMessage();
                        }
                        subTitle = errorMessage;
                    }
                }

                final int yOffset = size.height - 150;
                final int xOffset = 150;

                float titleFontSize;
                float subTitleFontSize;
                if (size.width < 650) {
                    titleFontSize = 30f;
                    subTitleFontSize = 17f;
                } else {
                    titleFontSize = 35f;
                    subTitleFontSize = 20f;
                }

                if (title != null) {
                    g.setFont(WidgetUtils.FONT_BANNER.deriveFont(titleFontSize));
                    g.drawString(title, xOffset, yOffset);
                }

                if (subTitle != null) {
                    final String[] lines = subTitle.split("\n");
                    g.setFont(WidgetUtils.FONT_BANNER.deriveFont(subTitleFontSize));
                    int y = yOffset + 10;
                    for (String line : lines) {
                        y = y + 30;
                        g.drawString(line, xOffset, y);
                    }
                }

                if (imagePath != null) {
                    g.drawImage(ImageManager.get().getImage(imagePath), xOffset - 120, yOffset - 30, null);
                }
            }
        });

        final JobGraphContext graphContext = new JobGraphContext(this, visualizationViewer, _analysisJobBuilder);

        final JobGraphLinkPainter linkPainter = new JobGraphLinkPainter(graphContext);

        final JobGraphLinkPainterMousePlugin linkPainterMousePlugin = new JobGraphLinkPainterMousePlugin(linkPainter,
                graphContext);
        final GraphMouse graphMouse = visualizationViewer.getGraphMouse();
        if (graphMouse instanceof PluggableGraphMouse) {
            PluggableGraphMouse pluggableGraphMouse = (PluggableGraphMouse) graphMouse;
            pluggableGraphMouse.add(linkPainterMousePlugin);
        }

        final JobGraphMouseListener graphMouseListener = new JobGraphMouseListener(graphContext, linkPainter,
                _presenterRendererFactory, _windowContext, _usageLogger, _componentConfigurationDialogs,
                _tableConfigurationDialogs);

        visualizationViewer.addGraphMouseListener(graphMouseListener);
        visualizationViewer.addMouseListener(graphMouseListener);
        visualizationViewer.addKeyListener(new JobGraphKeyListener(graphContext));

        final RenderContext<Object, JobGraphLink> renderContext = visualizationViewer.getRenderContext();

        final JobGraphTransformers transformers = new JobGraphTransformers(_userPreferences, _highlighedVertexes);

        // instrument the render context with all our transformers and stuff
        renderContext.setVertexFontTransformer(transformers.getVertexFontTransformer());
        renderContext.setVertexLabelTransformer(JobGraphTransformers.VERTEX_LABEL_TRANSFORMER);
        renderContext.setEdgeArrowPredicate(JobGraphTransformers.EDGE_ARROW_PREDICATE);
        renderContext.setEdgeArrowTransformer(JobGraphTransformers.EDGE_ARROW_TRANSFORMER);
        renderContext.setEdgeLabelTransformer(JobGraphTransformers.EDGE_LABEL_TRANSFORMER);
        renderContext.setEdgeShapeTransformer(transformers.getEdgeShapeTransformer());
        renderContext.setEdgeLabelClosenessTransformer(JobGraphTransformers.EDGE_LABEL_CLOSENESS_TRANSFORMER);
        renderContext.setEdgeLabelRenderer(transformers.getEdgeLabelRenderer());
        renderContext.setVertexIconTransformer(JobGraphTransformers.VERTEX_ICON_TRANSFORMER);
        renderContext.setVertexShapeTransformer(JobGraphTransformers.VERTEX_SHAPE_TRANSFORMER);

        final JButton graphPreferencesButton = createGraphPreferencesButton();
        visualizationViewer.setLayout(new BorderLayout());
        visualizationViewer.add(DCPanel.flow(Alignment.RIGHT, 0, 0, graphPreferencesButton), BorderLayout.SOUTH);

        // we save the values of the scrollbars in order to allow refreshes to
        // retain scroll position.
        final GraphZoomScrollPane scrollPane = new GraphZoomScrollPane(visualizationViewer);
        scrollPane.setCorner(new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND));
        if (_scrollHorizontal > 0) {
            setScrollbarValue(scrollPane.getHorizontalScrollBar(), _scrollHorizontal);
        }
        if (_scrollVertical > 0) {
            setScrollbarValue(scrollPane.getVerticalScrollBar(), _scrollVertical);
        }
        final AdjustmentListener adjustmentListener = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                _scrollHorizontal = scrollPane.getHorizontalScrollBar().getValue();
                _scrollVertical = scrollPane.getVerticalScrollBar().getValue();
            }
        };
        scrollPane.getHorizontalScrollBar().addAdjustmentListener(adjustmentListener);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(adjustmentListener);

        return scrollPane;
    }

    private JButton createGraphPreferencesButton() {
        final JButton uiPreferencesButton = WidgetFactory.createSmallButton(ImageManager.get().getImageIcon(
                IconUtils.MENU_OPTIONS, IconUtils.ICON_SIZE_MEDIUM));
        uiPreferencesButton.setOpaque(false);
        uiPreferencesButton.setBorder(null);
        uiPreferencesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JobGraphPreferencesPanel panel = new JobGraphPreferencesPanel(_userPreferences, JobGraph.this);

                final JPopupMenu popup = new JPopupMenu("Graph UI Preferences");
                popup.add(panel);
                final Dimension panelSize = panel.getPreferredSize();
                popup.show(uiPreferencesButton, -1 * panelSize.width - 4, 0);
            }
        });
        return uiPreferencesButton;
    }

    private void setScrollbarValue(JScrollBar scrollBar, int value) {
        final BoundedRangeModel scrollModel = scrollBar.getModel();
        scrollBar.setValues(value, scrollModel.getExtent(), scrollModel.getMinimum(), scrollModel.getMaximum());
    }

}
