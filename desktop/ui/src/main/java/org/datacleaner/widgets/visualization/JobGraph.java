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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.datatransfer.Transferable;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.TransferHandler;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.TruePredicate;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.user.UsageLogger;
import org.datacleaner.util.DragDropUtils;
import org.datacleaner.util.GraphUtils;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.windows.ComponentConfigurationDialog;
import org.datacleaner.windows.SourceTableConfigurationDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.VisualizationViewer.GraphMouse;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;

/**
 * Class capable of creating graphs that visualize {@link AnalysisJob}s or parts
 * of them as a graph.
 */
public final class JobGraph {

    private static final String MORE_COLUMNS_VERTEX = "...";

    private static final ImageManager imageManager = ImageManager.get();
    private static final Logger logger = LoggerFactory.getLogger(JobGraph.class);

    private final Map<ComponentBuilder, ComponentConfigurationDialog> _componentConfigurationDialogs;
    private final Map<Table, SourceTableConfigurationDialog> _tableConfigurationDialogs;    
    private final Set<Object> _highlighedVertexes;
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final RendererFactory _presenterRendererFactory;
    private final DCPanel _panel;
    private final WindowContext _windowContext;
    private final UsageLogger _usageLogger;

    public JobGraph(WindowContext windowContext, AnalysisJobBuilder analysisJobBuilder, UsageLogger usageLogger) {
        this(windowContext, analysisJobBuilder, null, usageLogger);
    }

    public JobGraph(WindowContext windowContext, AnalysisJobBuilder analysisJobBuilder,
            RendererFactory presenterRendererFactory, UsageLogger usageLogger) {
        _highlighedVertexes = new HashSet<Object>();
        _analysisJobBuilder = analysisJobBuilder;
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

        // TODO: Make the size dynamic as per the graphs size
        final Dimension preferredSize = new Dimension(2500, 2000);

        final JobGraphLayoutTransformer layoutTransformer = new JobGraphLayoutTransformer(_analysisJobBuilder, graph);
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

                final Dimension size = getPanel().getSize();
                if (size.height < 300) {
                    // don't show the background hints - it will be too
                    // disturbing
                    return;
                }

                final String title;
                final String subTitle;
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
                    g.drawImage(imageManager.getImage(imagePath), xOffset - 120, yOffset - 30, null);
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
                _presenterRendererFactory, _windowContext, _usageLogger, _componentConfigurationDialogs, _tableConfigurationDialogs);

        visualizationViewer.addGraphMouseListener(graphMouseListener);
        visualizationViewer.addMouseListener(graphMouseListener);
        visualizationViewer.addKeyListener(new JobGraphKeyListener(graphContext));

        final RenderContext<Object, JobGraphLink> renderContext = visualizationViewer.getRenderContext();

        // render fonts (some may be highlighted)
        renderContext.setVertexFontTransformer(new Transformer<Object, Font>() {

            private final Font normalFont = WidgetUtils.FONT_SMALL;
            private final Font highlighedFont = normalFont.deriveFont(Font.BOLD);

            @Override
            public Font transform(Object vertex) {
                if (_highlighedVertexes.contains(vertex)) {
                    return highlighedFont;
                }
                return normalFont;
            }
        });

        // render labels
        renderContext.setVertexLabelTransformer(new Transformer<Object, String>() {
            @Override
            public String transform(Object obj) {
                if (obj instanceof InputColumn) {
                    return ((InputColumn<?>) obj).getName();
                }
                if (obj instanceof ComponentBuilder) {
                    return LabelUtils.getLabel((ComponentBuilder) obj);
                }
                if (obj instanceof FilterOutcome) {
                    return ((FilterOutcome) obj).getCategory().name();
                }
                if (obj instanceof Table) {
                    return ((Table) obj).getName();
                }
                if (obj instanceof Class) {
                    Class<?> cls = (Class<?>) obj;
                    if (ReflectionUtils.is(cls, AnalyzerResult.class)) {
                        return "Analyzer result";
                    }
                    return cls.getSimpleName();
                }
                return obj.toString();
            }
        });

        // render arrows
        final Predicate<Context<Graph<Object, JobGraphLink>, JobGraphLink>> edgeArrowPredicate = TruePredicate
                .getInstance();
        renderContext.setEdgeArrowPredicate(edgeArrowPredicate);
        renderContext
                .setEdgeArrowTransformer(new Transformer<Context<Graph<Object, JobGraphLink>, JobGraphLink>, Shape>() {
                    @Override
                    public Shape transform(Context<Graph<Object, JobGraphLink>, JobGraphLink> input) {
                        return GraphUtils.ARROW_SHAPE;
                    }
                });

        renderContext.setEdgeLabelTransformer(new Transformer<JobGraphLink, String>() {
            @Override
            public String transform(JobGraphLink link) {
                final ComponentRequirement req = link.getRequirement();
                if (req == null) {
                    return null;
                }
                return req.getSimpleName();
            }
        });

        renderContext
                .setEdgeLabelClosenessTransformer(new Transformer<Context<Graph<Object, JobGraphLink>, JobGraphLink>, Number>() {
                    @Override
                    public Number transform(Context<Graph<Object, JobGraphLink>, JobGraphLink> input) {
                        return 0.4d;
                    }
                });

        renderContext.setEdgeLabelRenderer(new EdgeLabelRenderer() {
            @Override
            public void setRotateEdgeLabels(boolean state) {
            }

            @Override
            public boolean isRotateEdgeLabels() {
                return true;
            }

            @Override
            public <T> Component getEdgeLabelRendererComponent(JComponent vv, Object value, Font font,
                    boolean isSelected, T edge) {
                final Icon icon = imageManager.getImageIcon(IconUtils.FILTER_IMAGEPATH, IconUtils.ICON_SIZE_SMALL);
                final JLabel label = new JLabel(value + "", icon, JLabel.LEFT);
                label.setFont(WidgetUtils.FONT_SMALL);
                return label;
            }
        });

        // render icons
        renderContext.setVertexIconTransformer(new Transformer<Object, Icon>() {

            @Override
            public Icon transform(Object obj) {
                if (obj == MORE_COLUMNS_VERTEX || obj instanceof InputColumn) {
                    return imageManager.getImageIcon(IconUtils.MODEL_COLUMN, IconUtils.ICON_SIZE_MEDIUM);
                }
                if (obj instanceof ComponentBuilder) {
                    final ComponentBuilder componentBuilder = (ComponentBuilder) obj;
                    final ComponentDescriptor<?> descriptor = componentBuilder.getDescriptor();
                    final boolean configured;
                    if (componentBuilder.getInput().length == 0) {
                        configured = true;
                    } else {
                        configured = componentBuilder.isConfigured(false);
                    }
                    return IconUtils.getDescriptorIcon(descriptor, configured, IconUtils.ICON_SIZE_LARGE);
                }
                if (obj instanceof FilterOutcome) {
                    return imageManager.getImageIcon(IconUtils.FILTER_OUTCOME_PATH, IconUtils.ICON_SIZE_MEDIUM);
                }
                if (obj instanceof Table) {
                    return imageManager.getImageIcon(IconUtils.MODEL_TABLE, IconUtils.ICON_SIZE_LARGE);
                }
                if (obj instanceof Class) {
                    Class<?> cls = (Class<?>) obj;
                    if (ReflectionUtils.is(cls, AnalyzerResult.class)) {
                        return imageManager.getImageIcon(IconUtils.MODEL_RESULT, IconUtils.ICON_SIZE_LARGE);
                    }
                }
                return imageManager.getImageIcon(IconUtils.STATUS_ERROR);
            }
        });

        GraphZoomScrollPane scrollPane = new GraphZoomScrollPane(visualizationViewer);
        return scrollPane;
    }

}
