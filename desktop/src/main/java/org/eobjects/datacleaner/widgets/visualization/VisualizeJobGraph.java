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
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.TruePredicate;
import org.apache.metamodel.schema.Table;
import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
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
import org.eobjects.analyzer.metadata.HasMetadataProperties;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.renderer.Renderable;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.util.LabelUtils;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.eobjects.datacleaner.actions.AnalyzeButtonActionListener;
import org.eobjects.datacleaner.actions.RemoveComponentMenuItem;
import org.eobjects.datacleaner.actions.RemoveSourceTableMenuItem;
import org.eobjects.datacleaner.actions.TransformButtonActionListener;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.ComponentJobBuilderPresenter;
import org.eobjects.datacleaner.panels.ComponentJobBuilderRenderingFormat;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.util.GraphUtils;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DescriptorMenuBuilder;
import org.eobjects.datacleaner.windows.ComponentConfigurationDialog;
import org.eobjects.datacleaner.windows.SourceTableConfigurationDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;

/**
 * Class capable of creating graphs that visualize {@link AnalysisJob}s or parts
 * of them.
 */
public final class VisualizeJobGraph {

    private static final String MORE_COLUMNS_VERTEX = "...";

    private static final ImageManager imageManager = ImageManager.get();
    private static final Logger logger = LoggerFactory.getLogger(VisualizeJobGraph.class);

    private final Set<Object> _highlighedVertexes;
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final RendererFactory _presenterRendererFactory;
    private final DCPanel _panel;
    private final WindowContext _windowContext;
    private final UsageLogger _usageLogger;

    public VisualizeJobGraph(WindowContext windowContext, AnalysisJobBuilder analysisJobBuilder, UsageLogger usageLogger) {
        this(windowContext, analysisJobBuilder, null, usageLogger);
    }

    public VisualizeJobGraph(WindowContext windowContext, AnalysisJobBuilder analysisJobBuilder,
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

    public VisualizeJobGraph highlightVertex(Object vertex) {
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

    public void refresh(boolean displayColumns, boolean displayOutcomes) {
        final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
        sourceColumnFinder.addSources(_analysisJobBuilder);

        final DirectedGraph<Object, VisualizeJobLink> graph = new DirectedSparseGraph<Object, VisualizeJobLink>();

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
    private JComponent createJComponent(final DirectedGraph<Object, VisualizeJobLink> graph) {
        final int vertexCount = graph.getVertexCount();
        logger.debug("Rendering graph with {} vertices", vertexCount);

        final VisualizeJobLayoutTransformer layoutTransformer = new VisualizeJobLayoutTransformer(_analysisJobBuilder,
                graph);
        final StaticLayout<Object, VisualizeJobLink> layout = new StaticLayout<Object, VisualizeJobLink>(graph,
                layoutTransformer);

        Collection<Object> vertices = graph.getVertices();
        for (Object vertex : vertices) {
            // manually initialize all vertices
            layout.transform(vertex);
        }
        if (vertexCount > 0 && !layoutTransformer.isTransformed()) {
            throw new IllegalStateException("Layout transformer was never invoked!");
        }

        final VisualizationViewer<Object, VisualizeJobLink> visualizationViewer = new VisualizationViewer<Object, VisualizeJobLink>(
                layout);
        visualizationViewer.setBackground(WidgetUtils.BG_COLOR_BRIGHTEST);
        GraphUtils.applyStyles(visualizationViewer);
        visualizationViewer.addPreRenderPaintable(new Paintable() {
            @Override
            public boolean useTransform() {
                return false;
            }

            @Override
            public void paint(Graphics g) {
                final String title;
                final String subTitle;
                final String imagePath;

                g.setColor(WidgetUtils.BG_COLOR_MEDIUM);
                if (_analysisJobBuilder.getSourceColumns().size() == 0) {
                    title = "Select source ...";
                    subTitle = "Pick table/columns in the tree to the left.";
                    imagePath = "images/window/canvas-bg-table.png";
                } else if (_analysisJobBuilder.getComponentCount() == 0) {
                    title = "Start building ...";
                    subTitle = "Use the 'Transform' and 'Analyze' buttons above.";
                    imagePath = "images/window/canvas-bg-plus.png";
                } else if (_analysisJobBuilder.getAnalyzerJobBuilders().size() == 0
                        && _analysisJobBuilder.getComponentCount() <= 3) {
                    title = "Almost ready to run ...";
                    subTitle = "Any job needs to either perform a 'Write' or 'Analyze' action.";
                    imagePath = "images/window/canvas-bg-plus.png";
                } else {
                    title = null;
                    subTitle = null;
                    imagePath = null;
                }

                final Dimension size = getPanel().getSize();
                final int yOffset = size.height - 150;
                final int xOffset = size.width / 2 - 250;

                if (title != null) {
                    g.setFont(WidgetUtils.FONT_BANNER.deriveFont(35f));
                    g.drawString(title, xOffset, yOffset);
                }

                if (subTitle != null) {
                    g.setFont(WidgetUtils.FONT_BANNER.deriveFont(26f));
                    g.drawString(subTitle, xOffset, yOffset + 60);
                }

                if (imagePath != null) {
                    g.drawImage(ImageManager.get().getImage(imagePath), xOffset - 120, yOffset - 32, null);
                }
            }
        });

        // this is ugly, but a hack to make the graph mouse listener and the
        // regular mouse listener aware of each other's actions.
        final AtomicBoolean clickCaught = new AtomicBoolean(false);

        visualizationViewer.addGraphMouseListener(new GraphMouseListener<Object>() {
            @Override
            public void graphReleased(Object v, MouseEvent me) {
                final PickedState<Object> pickedVertexState = visualizationViewer.getPickedVertexState();

                final Object[] selectedObjects = pickedVertexState.getSelectedObjects();

                for (Object vertex : selectedObjects) {
                    final Double x = layout.getX(vertex);
                    final Double y = layout.getY(vertex);
                    if (vertex instanceof HasMetadataProperties) {
                        final Map<String, String> metadataProperties = ((HasMetadataProperties) vertex)
                                .getMetadataProperties();
                        metadataProperties.put(VisualizationMetadata.METADATA_PROPERTY_COORDINATES_X, "" + x.intValue());
                        metadataProperties.put(VisualizationMetadata.METADATA_PROPERTY_COORDINATES_Y, "" + y.intValue());
                    } else if (vertex instanceof Table) {
                        VisualizationMetadata.setPointForTable(_analysisJobBuilder, (Table) vertex, x, y);
                    }
                    // TODO: Add support for Columns, FilterRequirements
                }
            }

            @Override
            public void graphPressed(Object v, MouseEvent me) {
            }

            @Override
            public void graphClicked(Object v, MouseEvent me) {
                clickCaught.set(false);
                final int button = me.getButton();
                if (v instanceof AbstractBeanJobBuilder) {
                    final AbstractBeanJobBuilder<?, ?, ?> componentBuilder = (AbstractBeanJobBuilder<?, ?, ?>) v;
                    if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
                        clickCaught.set(true);
                        final JPopupMenu popup = new JPopupMenu();
                        popup.add(new RemoveComponentMenuItem(_analysisJobBuilder, componentBuilder));
                        popup.show(visualizationViewer, me.getX(), me.getY());
                    } else if (me.getClickCount() == 2) {
                        @SuppressWarnings("unchecked")
                        final Renderer<Renderable, ? extends ComponentJobBuilderPresenter> renderer = (Renderer<Renderable, ? extends ComponentJobBuilderPresenter>) _presenterRendererFactory
                                .getRenderer(componentBuilder, ComponentJobBuilderRenderingFormat.class);
                        if (renderer != null) {
                            clickCaught.set(true);
                            final ComponentJobBuilderPresenter presenter = renderer.render(componentBuilder);

                            final ComponentConfigurationDialog dialog = new ComponentConfigurationDialog(
                                    componentBuilder, _analysisJobBuilder, presenter);
                            dialog.open();
                        }
                    }
                } else if (v instanceof Table) {
                    final Table table = (Table) v;
                    if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
                        clickCaught.set(true);
                        final JPopupMenu popup = new JPopupMenu();
                        popup.add(new RemoveSourceTableMenuItem(_analysisJobBuilder, table));
                        popup.show(visualizationViewer, me.getX(), me.getY());
                    } else if (me.getClickCount() == 2) {
                        clickCaught.set(true);
                        SourceTableConfigurationDialog dialog = new SourceTableConfigurationDialog(_windowContext,
                                _analysisJobBuilder, table);
                        dialog.open();
                    }
                }
            }
        });

        visualizationViewer.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent me) {
                int button = me.getButton();
                if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
                    if (!clickCaught.get()) {

                        final JMenu transformMenuItem = new JMenu("Transform");
                        transformMenuItem.setIcon(ImageManager.get().getImageIcon(IconUtils.TRANSFORMER_IMAGEPATH,
                                IconUtils.ICON_SIZE_SMALL));
                        {
                            final TransformButtonActionListener transformButtonHelper = new TransformButtonActionListener(
                                    _analysisJobBuilder.getConfiguration(), _analysisJobBuilder, _usageLogger);
                            final List<BeanDescriptor<?>> descriptors = transformButtonHelper.getDescriptors();
                            DescriptorMenuBuilder descriptorMenuBuilder = new DescriptorMenuBuilder(descriptors) {
                                @Override
                                protected JMenuItem createMenuItem(BeanDescriptor<?> descriptor) {
                                    final JMenuItem menuItem = transformButtonHelper.createMenuItem(descriptor);
                                    return menuItem;
                                }
                            };
                            descriptorMenuBuilder.addItemsToMenu(transformMenuItem);
                        }

                        final JMenu analyzeMenuItem = new JMenu("Analyze");
                        analyzeMenuItem.setIcon(ImageManager.get().getImageIcon(IconUtils.ANALYZER_IMAGEPATH,
                                IconUtils.ICON_SIZE_SMALL));
                        {
                            final AnalyzeButtonActionListener analyzeButtonHelper = new AnalyzeButtonActionListener(
                                    _analysisJobBuilder.getConfiguration(), _analysisJobBuilder, _usageLogger);
                            final Collection<? extends BeanDescriptor<?>> descriptors = analyzeButtonHelper
                                    .getDescriptors();
                            DescriptorMenuBuilder descriptorMenuBuilder = new DescriptorMenuBuilder(descriptors) {
                                @Override
                                protected JMenuItem createMenuItem(BeanDescriptor<?> descriptor) {
                                    final JMenuItem menuItem = analyzeButtonHelper.createMenuItem(descriptor);
                                    return menuItem;
                                }
                            };
                            descriptorMenuBuilder.addItemsToMenu(analyzeMenuItem);
                        }

                        final JMenu writeMenuItem = new JMenu("Write");
                        writeMenuItem.setIcon(ImageManager.get().getImageIcon(IconUtils.GENERIC_DATASTORE_IMAGEPATH,
                                IconUtils.ICON_SIZE_SMALL));
                        // TODO
                        writeMenuItem.add(new JMenuItem("TODO"));

                        final JPopupMenu popup = new JPopupMenu();
                        popup.add(transformMenuItem);
                        popup.add(analyzeMenuItem);
                        popup.add(writeMenuItem);
                        popup.show(visualizationViewer, me.getX(), me.getY());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        final RenderContext<Object, VisualizeJobLink> renderContext = visualizationViewer.getRenderContext();

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
        final Predicate<Context<Graph<Object, VisualizeJobLink>, VisualizeJobLink>> edgeArrowPredicate = TruePredicate
                .getInstance();
        renderContext.setEdgeArrowPredicate(edgeArrowPredicate);

        renderContext.setEdgeLabelTransformer(new Transformer<VisualizeJobLink, String>() {
            @Override
            public String transform(VisualizeJobLink link) {
                final ComponentRequirement req = link.getRequirement();
                if (req == null) {
                    return null;
                }
                return req.getSimpleName();
            }
        });

        renderContext
                .setEdgeLabelClosenessTransformer(new Transformer<Context<Graph<Object, VisualizeJobLink>, VisualizeJobLink>, Number>() {
                    @Override
                    public Number transform(Context<Graph<Object, VisualizeJobLink>, VisualizeJobLink> input) {
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
                final Icon icon = ImageManager.get()
                        .getImageIcon(IconUtils.FILTER_IMAGEPATH, IconUtils.ICON_SIZE_SMALL);
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

        return visualizationViewer;
    }

    private void addNodes(DirectedGraph<Object, VisualizeJobLink> graph, SourceColumnFinder scf, Object item,
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

    private void addEdge(DirectedGraph<Object, VisualizeJobLink> graph, Object from, Object to,
            ComponentRequirement requirement) {
        VisualizeJobLink link = new VisualizeJobLink(from, to, requirement);
        if (!graph.containsEdge(link)) {
            graph.addEdge(link, from, to, EdgeType.DIRECTED);
        }
    }
}
