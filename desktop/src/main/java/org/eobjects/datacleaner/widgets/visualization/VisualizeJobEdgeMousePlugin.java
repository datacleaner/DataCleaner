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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.metamodel.schema.Table;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.ComponentRequirement;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.job.HasFilterOutcomes;
import org.eobjects.analyzer.job.InputColumnSourceJob;
import org.eobjects.analyzer.job.SimpleComponentRequirement;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.ComponentBuilder;
import org.eobjects.datacleaner.util.GraphUtils;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.EditingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.GraphMousePlugin;

/**
 * {@link GraphMousePlugin} inpsired by {@link EditingGraphMousePlugin} to
 * support the creation of new links between parts of a visualized DataCleaner
 * job.
 */
public class VisualizeJobEdgeMousePlugin extends AbstractGraphMousePlugin implements MouseListener, MouseMotionListener {

    private static final Logger logger = LoggerFactory.getLogger(VisualizeJobEdgeMousePlugin.class);

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final VisualizeJobGraph _graph;
    private final CubicCurve2D _rawEdge;
    private final VisualizationServer.Paintable _edgePaintable;
    private final VisualizationServer.Paintable _arrowPaintable;
    private Point2D _mouseDownPoint;
    private EdgeType _edgeIsDirected;
    private Shape _edgeShape;
    private Shape _arrowShape;
    private Object _startVertex;

    public VisualizeJobEdgeMousePlugin(AnalysisJobBuilder analysisJobBuilder, VisualizeJobGraph graph) {
        super(MouseEvent.BUTTON1_MASK + MouseEvent.SHIFT_MASK);
        _analysisJobBuilder = analysisJobBuilder;
        _graph = graph;
        _rawEdge = new CubicCurve2D.Float();
        _rawEdge.setCurve(0.0f, 0.0f, 0.20f, 20, .33f, -15, 1.0f, 0.0f);
        _edgePaintable = new EdgePaintable();
        _arrowPaintable = new ArrowPaintable();
        cursor = Cursor.getDefaultCursor();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (checkModifiers(e)) {
            if (_startVertex != null) {
                transformEdgeShape(_mouseDownPoint, e.getPoint());
                if (_edgeIsDirected == EdgeType.DIRECTED) {
                    transformArrowShape(_mouseDownPoint, e.getPoint());
                }
            }
            VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();
            vv.repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (checkModifiers(e)) {
            final VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();
            final Point2D p = e.getPoint();
            GraphElementAccessor<?, ?> pickSupport = vv.getPickSupport();
            if (pickSupport != null) {
                Graph<?, ?> graph = vv.getModel().getGraphLayout().getGraph();
                // set default edge type
                if (graph instanceof DirectedGraph) {
                    _edgeIsDirected = EdgeType.DIRECTED;
                } else {
                    _edgeIsDirected = EdgeType.UNDIRECTED;
                }

                @SuppressWarnings("rawtypes")
                final Layout graphLayout = vv.getModel().getGraphLayout();

                @SuppressWarnings("unchecked")
                final Object vertex = pickSupport.getVertex(graphLayout, p.getX(), p.getY());

                if (vertex != null) { // get ready to make an edge
                    _startVertex = vertex;
                    _mouseDownPoint = e.getPoint();
                    transformEdgeShape(_mouseDownPoint, _mouseDownPoint);
                    vv.addPostRenderPaintable(_edgePaintable);
                    if ((e.getModifiers() & MouseEvent.SHIFT_MASK) != 0
                            && vv.getModel().getGraphLayout().getGraph() instanceof UndirectedGraph == false) {
                        _edgeIsDirected = EdgeType.DIRECTED;
                    }
                    if (_edgeIsDirected == EdgeType.DIRECTED) {
                        transformArrowShape(_mouseDownPoint, e.getPoint());
                        vv.addPostRenderPaintable(_arrowPaintable);
                    }
                }
            }
        }
    }

    /**
     * If startVertex is non-null, and the mouse is released over an existing
     * vertex, create an undirected edge from startVertex to the vertex under
     * the mouse pointer. If shift was also pressed, create a directed edge
     * instead.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (checkModifiers(e)) {
            final VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();
            final Point2D p = e.getPoint();
            @SuppressWarnings("rawtypes")
            final Layout layout = vv.getModel().getGraphLayout();
            final GraphElementAccessor<?, ?> pickSupport = vv.getPickSupport();
            if (pickSupport != null) {
                @SuppressWarnings("unchecked")
                final Object vertex = pickSupport.getVertex(layout, p.getX(), p.getY());
                if (vertex != null && _startVertex != null) {
                    final boolean created = createLink(e, _startVertex, vertex);
                    if (created && vv.isVisible()) {
                        _graph.refresh();
                    }
                }
            }
            _startVertex = null;
            _mouseDownPoint = null;
            _edgeIsDirected = EdgeType.UNDIRECTED;
            vv.removePostRenderPaintable(_edgePaintable);
            vv.removePostRenderPaintable(_arrowPaintable);
        }
    }

    private boolean createLink(MouseEvent me, Object fromVertex, Object toVertex) {
        final List<? extends InputColumn<?>> sourceColumns;
        final Collection<FilterOutcome> filterOutcomes;

        if (fromVertex instanceof Table) {
            final Table table = (Table) fromVertex;
            sourceColumns = _analysisJobBuilder.getSourceColumnsOfTable(table);
            filterOutcomes = null;
        } else if (fromVertex instanceof InputColumnSourceJob) {
            InputColumn<?>[] outputColumns = null;
            try {
                outputColumns = ((InputColumnSourceJob) fromVertex).getOutput();
            } catch (Exception e) {
                outputColumns = new InputColumn[0];
            }
            sourceColumns = Arrays.<InputColumn<?>> asList(outputColumns);
            filterOutcomes = null;
        } else if (fromVertex instanceof HasFilterOutcomes) {
            final HasFilterOutcomes hasFilterOutcomes = (HasFilterOutcomes) fromVertex;
            filterOutcomes = hasFilterOutcomes.getFilterOutcomes();
            sourceColumns = null;
        } else {
            sourceColumns = null;
            filterOutcomes = null;
        }

        if (toVertex instanceof ComponentBuilder) {
            final ComponentBuilder componentBuilder = (ComponentBuilder) toVertex;
            if (sourceColumns != null && !sourceColumns.isEmpty()) {
                try {
                    final ConfiguredPropertyDescriptor inputProperty = componentBuilder
                            .getDefaultConfiguredPropertyForInput();
                    if (inputProperty.isArray()) {
                        componentBuilder.addInputColumns(sourceColumns, inputProperty);
                    } else {
                        componentBuilder.addInputColumn(sourceColumns.get(0), inputProperty);
                    }
                    // returning true to indicate a change
                    return true;
                } catch (Exception e) {
                    // nothing to do
                    logger.info("Failed to add input columns ({}) to {}", sourceColumns.size(), componentBuilder, e);
                }
            } else if (filterOutcomes != null && !filterOutcomes.isEmpty()) {
                final JPopupMenu popup = new JPopupMenu();
                final int iconSize = IconUtils.ICON_SIZE_MEDIUM;
                final Icon icon = ImageManager.get().getImageIcon(IconUtils.FILTER_IMAGEPATH, iconSize);
                for (final FilterOutcome filterOutcome : filterOutcomes) {
                    final JMenuItem menuItem = new JMenuItem(filterOutcome.getSimpleName(), icon);
                    menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            final ComponentRequirement requirement = new SimpleComponentRequirement(filterOutcome);
                            componentBuilder.setComponentRequirement(requirement);
                        }
                    });
                    final int preferredWidth = menuItem.getPreferredSize().width;
                    menuItem.setPreferredSize(new Dimension(Math.max(preferredWidth, 200), iconSize + 20));
                    menuItem.setBorder(null);
                    popup.add(menuItem);
                    popup.show(me.getComponent(), me.getX(), me.getY());
                }
                // we return false because no change was applied (yet)
                return false;
            }
        }
        return false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     * code lifted from PluggableRenderer to move an edge shape into an
     * arbitrary position
     */
    private void transformEdgeShape(Point2D down, Point2D out) {
        float x1 = (float) down.getX();
        float y1 = (float) down.getY();
        float x2 = (float) out.getX();
        float y2 = (float) out.getY();

        AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

        float dx = x2 - x1;
        float dy = y2 - y1;
        float thetaRadians = (float) Math.atan2(dy, dx);
        xform.rotate(thetaRadians);
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        xform.scale(dist / _rawEdge.getBounds().getWidth(), 1.0);
        _edgeShape = xform.createTransformedShape(_rawEdge);
    }

    private void transformArrowShape(Point2D down, Point2D out) {
        float x1 = (float) down.getX();
        float y1 = (float) down.getY();
        float x2 = (float) out.getX();
        float y2 = (float) out.getY();

        AffineTransform xform = AffineTransform.getTranslateInstance(x2, y2);

        float dx = x2 - x1;
        float dy = y2 - y1;
        float thetaRadians = (float) Math.atan2(dy, dx);
        xform.rotate(thetaRadians);
        _arrowShape = xform.createTransformedShape(GraphUtils.ARROW_SHAPE);
    }

    /**
     * Used for the edge creation visual effect during mouse drag
     */
    class EdgePaintable implements VisualizationServer.Paintable {

        public void paint(Graphics g) {
            if (_edgeShape != null) {
                Color oldColor = g.getColor();
                g.setColor(Color.black);
                ((Graphics2D) g).draw(_edgeShape);
                g.setColor(oldColor);
            }
        }

        public boolean useTransform() {
            return false;
        }
    }

    /**
     * Used for the directed edge creation visual effect during mouse drag
     */
    class ArrowPaintable implements VisualizationServer.Paintable {

        public void paint(Graphics g) {
            if (_arrowShape != null) {
                Color oldColor = g.getColor();
                g.setColor(Color.black);
                ((Graphics2D) g).fill(_arrowShape);
                g.setColor(oldColor);
            }
        }

        public boolean useTransform() {
            return false;
        }
    }
}
