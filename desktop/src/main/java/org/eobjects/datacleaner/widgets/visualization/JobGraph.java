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
package org.eobjects.datacleaner.widgets.visualization;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.datatransfer.Transferable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.ComponentRequirement;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.job.HasComponentRequirement;
import org.eobjects.analyzer.job.HasFilterOutcomes;
import org.eobjects.analyzer.job.InputColumnSinkJob;
import org.eobjects.analyzer.job.InputColumnSourceJob;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.util.LabelUtils;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.util.DragDropUtils;
import org.eobjects.datacleaner.util.GraphUtils;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeType;
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

    public void refresh() {
        refresh(false, false);
    }

    public AnalysisJobBuilder getAnalysisJobBuilder() {
        return _analysisJobBuilder;
    }

    public void refresh(boolean displayColumns, boolean displayOutcomes) {
        final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
        sourceColumnFinder.addSources(_analysisJobBuilder);

        final DirectedGraph<Object, JobGraphLink> graph = new DirectedSparseGraph<Object, JobGraphLink>();

        final List<Table> sourceTables = _analysisJobBuilder.getSourceTables();
        for (Table table : sourceTables) {
            addNodes(graph, sourceColumnFinder, table, displayColumns, displayOutcomes, -1);
        }

        final List<TransformerJobBuilder<?>> tjbs = _analysisJobBuilder.getTransformerJobBuilders();
        for (TransformerJobBuilder<?> tjb : tjbs) {
            addNodes(graph, sourceColumnFinder, tjb, displayColumns, displayOutcomes, -1);
        }

        final List<AnalyzerJobBuilder<?>> ajbs = _analysisJobBuilder.getAnalyzerJobBuilders();
        for (AnalyzerJobBuilder<?> ajb : ajbs) {
            addNodes(graph, sourceColumnFinder, ajb, displayColumns, displayOutcomes, -1);
        }

        final List<FilterJobBuilder<?, ?>> fjbs = _analysisJobBuilder.getFilterJobBuilders();
        for (FilterJobBuilder<?, ?> fjb : fjbs) {
            addNodes(graph, sourceColumnFinder, fjb, displayColumns, displayOutcomes, -1);
        }

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

                if (data instanceof Table) {
                    _analysisJobBuilder.addSourceColumns(((Table) data).getColumns());
                }

                if (data instanceof Column) {
                    _analysisJobBuilder.addSourceColumn((Column) data);
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
                GradientPaint paint = new GradientPaint(0, 0, WidgetUtils.BG_COLOR_BRIGHTEST, 0, visualizationViewer
                        .getHeight(), WidgetUtils.BG_COLOR_BRIGHT);
                if (g instanceof Graphics2D) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setPaint(paint);
                } else {
                    g.setColor(WidgetUtils.BG_COLOR_BRIGHT);
                }
                g.fillRect(0, 0, visualizationViewer.getWidth(), visualizationViewer.getHeight());

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
                    subTitle = "Add components to your job. Components define 'what to do'.\n"
                            + "Right-click the canvas or use the 'Transform' and 'Analyze' buttons above.";
                    imagePath = "images/window/canvas-bg-plus.png";
                } else if (graph.getEdgeCount() == 0) {
                    title = "Connect the pieces";
                    subTitle = "Click the source table and drag a connection\n"
                            + "with your mouse while holding down the Shift button.";
                    imagePath = "images/window/canvas-bg-connect.png";
                } else if (_analysisJobBuilder.getAnalyzerJobBuilders().size() == 0
                        && _analysisJobBuilder.getComponentCount() <= 3) {
                    title = "Almost ready to run ...";
                    subTitle = "Your job is almost ready. But any job needs to\n"
                            + "either perform a 'Write' or 'Analyze' action.";
                    imagePath = "images/window/canvas-bg-plus.png";
                } else {
                    title = null;
                    subTitle = null;
                    imagePath = null;
                }

                final Dimension size = getPanel().getSize();
                final int yOffset = size.height - 150;
                final int xOffset = 150;

                if (title != null) {
                    g.setFont(WidgetUtils.FONT_BANNER.deriveFont(35f));
                    g.drawString(title, xOffset, yOffset);
                }

                if (subTitle != null) {
                    final String[] lines = subTitle.split("\n");
                    g.setFont(WidgetUtils.FONT_BANNER.deriveFont(20f));
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
                _presenterRendererFactory, _windowContext, _usageLogger);

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
                if (obj instanceof AbstractBeanJobBuilder) {
                    return LabelUtils.getLabel((AbstractBeanJobBuilder<?, ?, ?>) obj);
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
                if (obj instanceof AbstractBeanJobBuilder) {
                    return IconUtils.getDescriptorIcon(((AbstractBeanJobBuilder<?, ?, ?>) obj).getDescriptor(),
                            IconUtils.ICON_SIZE_LARGE);
                }
                if (obj instanceof FilterOutcome) {
                    return imageManager.getImageIcon("images/component-types/filter-outcome.png",
                            IconUtils.ICON_SIZE_MEDIUM);
                }
                if (obj instanceof Table) {
                    return imageManager.getImageIcon(IconUtils.MODEL_TABLE, IconUtils.ICON_SIZE_LARGE);
                }
                if (obj instanceof Class) {
                    Class<?> cls = (Class<?>) obj;
                    if (ReflectionUtils.is(cls, AnalyzerResult.class)) {
                        return imageManager.getImageIcon("images/model/result.png", IconUtils.ICON_SIZE_LARGE);
                    }
                }
                return imageManager.getImageIcon(IconUtils.STATUS_ERROR);
            }
        });

        GraphZoomScrollPane scrollPane = new GraphZoomScrollPane(visualizationViewer);
        return scrollPane;
    }

    private void addNodes(DirectedGraph<Object, JobGraphLink> graph, SourceColumnFinder scf, Object item,
            boolean displayColumns, boolean displayFilterOutcomes, int recurseCount) {
        if (item == null) {
            throw new IllegalArgumentException("Node item cannot be null");
        }

        if (!displayColumns && item instanceof InputColumn) {
            return;
        } else if (!displayFilterOutcomes && item instanceof FilterOutcome) {
            return;
        }
        if (!graph.containsVertex(item)) {
            graph.addVertex(item);

            if (recurseCount == 0) {
                return;
            }

            // decrement recurseCount
            recurseCount--;

            if (item instanceof InputColumnSinkJob) {
                InputColumn<?>[] inputColumns = ((InputColumnSinkJob) item).getInput();
                for (InputColumn<?> inputColumn : inputColumns) {
                    if (displayColumns) {
                        // add the column itself
                        addNodes(graph, scf, inputColumn, displayColumns, displayFilterOutcomes, recurseCount);
                        addEdge(graph, inputColumn, item, null);
                    } else {
                        // add the origin of the column
                        if (inputColumn.isVirtualColumn()) {
                            InputColumnSourceJob source = scf.findInputColumnSource(inputColumn);
                            if (source != null) {
                                addNodes(graph, scf, source, displayColumns, displayFilterOutcomes, recurseCount);
                                addEdge(graph, source, item, null);
                            }
                        }

                        if (inputColumn.isPhysicalColumn()) {
                            Table table = inputColumn.getPhysicalColumn().getTable();
                            if (table != null) {
                                addNodes(graph, scf, table, displayColumns, displayFilterOutcomes, recurseCount);
                                addEdge(graph, table, item, null);
                            }
                        }
                    }
                }
            }

            if (item instanceof FilterOutcome) {
                final HasFilterOutcomes source = scf.findOutcomeSource((FilterOutcome) item);
                if (source != null) {
                    addNodes(graph, scf, source, displayColumns, displayFilterOutcomes, recurseCount);
                    addEdge(graph, source, item, null);
                }
            }

            if (item instanceof HasComponentRequirement) {
                final HasComponentRequirement hasComponentRequirement = (HasComponentRequirement) item;
                final Collection<FilterOutcome> filterOutcomes = getProcessingDependencyFilterOutcomes(hasComponentRequirement);
                for (final FilterOutcome filterOutcome : filterOutcomes) {
                    if (displayFilterOutcomes) {
                        // add the filter outcome itself
                        addNodes(graph, scf, filterOutcome, displayColumns, displayFilterOutcomes, recurseCount);
                        addEdge(graph, filterOutcome, item, null);
                    } else {
                        // add the origin of the filter outcome
                        final HasFilterOutcomes source = scf.findOutcomeSource(filterOutcome);
                        if (source != null) {
                            addNodes(graph, scf, source, displayColumns, displayFilterOutcomes, recurseCount);
                            addEdge(graph, source, item, hasComponentRequirement.getComponentRequirement());
                        }
                    }
                }
            }

            if (item instanceof InputColumn) {
                InputColumn<?> inputColumn = (InputColumn<?>) item;
                if (inputColumn.isVirtualColumn()) {
                    InputColumnSourceJob source = scf.findInputColumnSource(inputColumn);
                    if (source != null) {
                        addNodes(graph, scf, source, displayColumns, displayFilterOutcomes, recurseCount);
                        addEdge(graph, source, item, null);
                    }
                }

                if (inputColumn.isPhysicalColumn()) {
                    final Table table = inputColumn.getPhysicalColumn().getTable();
                    if (table != null) {
                        addNodes(graph, scf, table, displayColumns, displayFilterOutcomes, recurseCount);
                        addEdge(graph, table, item, null);
                    }
                }
            }
        }
    }

    private Collection<FilterOutcome> getProcessingDependencyFilterOutcomes(HasComponentRequirement item) {
        final ComponentRequirement componentRequirement = item.getComponentRequirement();
        if (componentRequirement == null) {
            return Collections.emptyList();
        }
        return componentRequirement.getProcessingDependencies();
    }

    private void addEdge(DirectedGraph<Object, JobGraphLink> graph, Object from, Object to,
            ComponentRequirement requirement) {
        JobGraphLink link = new JobGraphLink(from, to, requirement);
        if (!graph.containsEdge(link)) {
            graph.addEdge(link, from, to, EdgeType.DIRECTED);
        }
    }
}
