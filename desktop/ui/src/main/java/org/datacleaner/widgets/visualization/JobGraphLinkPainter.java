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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.apache.metamodel.schema.Table;
import org.datacleaner.api.ColumnProperty;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.CompoundComponentRequirement;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.HasFilterOutcomes;
import org.datacleaner.job.InputColumnSourceJob;
import org.datacleaner.job.SimpleComponentRequirement;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.util.GraphUtils;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.WidgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.visualization.VisualizationServer;

/**
 * Supporting class containing the state surrounding the drawing of new
 * {@link JobGraphLink}s.
 */
public class JobGraphLinkPainter {
    public static class VertexContext {
        private final Object _vertex;
        private final OutputDataStream _outputDataStream;
        private final AnalysisJobBuilder _analysisJobBuilder;

        public VertexContext(final Object vertex, final AnalysisJobBuilder analysisJobBuilder,
                final OutputDataStream outputDataStream) {
            _vertex = vertex;
            _outputDataStream = outputDataStream;
            _analysisJobBuilder = analysisJobBuilder;
        }

        public Object getVertex() {
            return _vertex;
        }

        public OutputDataStream getOutputDataStream() {
            return _outputDataStream;
        }

        public AnalysisJobBuilder getAnalysisJobBuilder() {
            return _analysisJobBuilder;
        }
    }

    /**
     * Used for the edge creation visual effect during mouse drag
     */
    class EdgePaintable implements VisualizationServer.Paintable {

        public void paint(final Graphics graphics) {
            if (_edgeShape != null) {
                final Color oldColor = graphics.getColor();
                graphics.setColor(Color.black);
                ((Graphics2D) graphics).draw(_edgeShape);
                graphics.setColor(oldColor);
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

        public void paint(final Graphics graphics) {
            if (_arrowShape != null) {
                final Color oldColor = graphics.getColor();
                graphics.setColor(Color.black);
                ((Graphics2D) graphics).fill(_arrowShape);
                graphics.setColor(oldColor);
            }
        }

        public boolean useTransform() {
            return false;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(JobGraphLinkPainter.class);
    private final JobGraphContext _graphContext;
    private final JobGraphActions _actions;
    private final VisualizationServer.Paintable _edgePaintable;
    private final VisualizationServer.Paintable _arrowPaintable;
    private Shape _edgeShape;
    private Shape _arrowShape;
    private VertexContext _startVertex;
    private Point2D _startPoint;

    public JobGraphLinkPainter(final JobGraphContext graphContext, final JobGraphActions actions) {
        _graphContext = graphContext;
        _actions = actions;
        _edgePaintable = new EdgePaintable();
        _arrowPaintable = new ArrowPaintable();
    }

    /**
     * Called when the drawing of a new link/edge is started
     *
     * @param startVertex
     */
    public void startLink(final VertexContext startVertex) {
        if (startVertex == null) {
            return;
        }

        final AbstractLayout<Object, JobGraphLink> graphLayout = _graphContext.getGraphLayout();
        final int x = (int) graphLayout.getX(startVertex.getVertex());
        final int y = (int) graphLayout.getY(startVertex.getVertex());

        logger.debug("startLink({})", startVertex);

        _startVertex = startVertex;
        _startPoint = new Point(x, y);

        transformEdgeShape(_startPoint, _startPoint);
        _graphContext.getVisualizationViewer().addPostRenderPaintable(_edgePaintable);
        transformArrowShape(_startPoint, _startPoint);
        _graphContext.getVisualizationViewer().addPostRenderPaintable(_arrowPaintable);
    }

    public boolean endLink(final MouseEvent me) {
        if (_startVertex != null) {
            final Object vertex = _graphContext.getVertex(me);
            return endLink(vertex, me);
        }
        return false;
    }

    /**
     * If startVertex is non-null this method will attempt to end the
     * link-painting at the given endVertex
     *
     * @return true if a link drawing was ended or false if it wasn't started
     */
    public boolean endLink(final Object endVertex, final MouseEvent mouseEvent) {
        logger.debug("endLink({})", endVertex);
        boolean result = false;
        if (_startVertex != null && endVertex != null) {
            if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
                final boolean created = createLink(_startVertex, endVertex, mouseEvent);
                if (created && _graphContext.getVisualizationViewer().isVisible()) {
                    _graphContext.getJobGraph().refresh();
                }
                result = true;
            }
        }
        stopDrawing();
        return result;
    }

    private void stopDrawing() {
        _startVertex = null;
        _startPoint = null;
        _graphContext.getVisualizationViewer().removePostRenderPaintable(_edgePaintable);
        _graphContext.getVisualizationViewer().removePostRenderPaintable(_arrowPaintable);
    }

    /**
     * Cancels the drawing of the link
     */
    public void cancelLink() {
        logger.debug("cancelLink()");
        stopDrawing();
    }

    public void moveCursor(final MouseEvent me) {
        if (_startVertex != null) {
            moveCursor(me.getPoint());
        }
    }

    public void moveCursor(final Point2D currentPoint) {
        if (_startVertex != null) {
            logger.debug("moveCursor({})", currentPoint);
            transformEdgeShape(_startPoint, currentPoint);
            transformArrowShape(_startPoint, currentPoint);
            _graphContext.getVisualizationViewer().repaint();
        }
    }

    private boolean createLink(final VertexContext fromVertex, final Object toVertex, final MouseEvent mouseEvent) {
        logger.debug("createLink({}, {}, {})", fromVertex, toVertex, mouseEvent);

        final List<? extends InputColumn<?>> sourceColumns;
        final Collection<FilterOutcome> filterOutcomes;

        final AnalysisJobBuilder sourceAnalysisJobBuilder = fromVertex.getAnalysisJobBuilder();

        if (fromVertex.getOutputDataStream() != null) {
            sourceColumns = sourceAnalysisJobBuilder.getSourceColumns();
            filterOutcomes = null;
        } else if (fromVertex.getVertex() instanceof Table) {
            final Table table = (Table) fromVertex.getVertex();
            sourceColumns = sourceAnalysisJobBuilder.getSourceColumnsOfTable(table);
            filterOutcomes = null;
        } else if (fromVertex.getVertex() instanceof InputColumnSourceJob) {
            InputColumn<?>[] outputColumns;
            try {
                outputColumns = ((InputColumnSourceJob) fromVertex.getVertex()).getOutput();
            } catch (final Exception e) {
                outputColumns = new InputColumn[0];
            }
            sourceColumns = getVisibleOutputColumns(outputColumns);
            filterOutcomes = null;
        } else if (fromVertex.getVertex() instanceof HasFilterOutcomes) {
            final HasFilterOutcomes hasFilterOutcomes = (HasFilterOutcomes) fromVertex.getVertex();
            filterOutcomes = hasFilterOutcomes.getFilterOutcomes();
            sourceColumns = null;
        } else {
            sourceColumns = null;
            filterOutcomes = null;
        }
        if (toVertex instanceof ComponentBuilder) {
            final ComponentBuilder componentBuilder = (ComponentBuilder) toVertex;

            if (sourceColumns != null && !sourceColumns.isEmpty()) {
                if (componentBuilder.getDescriptor().isMultiStreamComponent()) {
                    if (!fromVertex.getAnalysisJobBuilder().isRootJobBuilder()) {
                        // we don't yet support MultiStreamComponents on output
                        // data streams. See issue #620
                        return false;
                    }
                }

                if (!scopeUpdatePermitted(sourceAnalysisJobBuilder, componentBuilder)) {
                    return false;
                }

                sourceAnalysisJobBuilder.moveComponent(componentBuilder);

                try {

                    final ConfiguredPropertyDescriptor inputProperty =
                            componentBuilder.getDefaultConfiguredPropertyForInput();

                    final ColumnProperty columnProperty = inputProperty.getAnnotation(ColumnProperty.class);
                    if (inputProperty.isArray() || (columnProperty != null && columnProperty
                            .escalateToMultipleJobs())) {
                        componentBuilder
                                .addInputColumns(getRelevantSourceColumns(sourceColumns, inputProperty), inputProperty);
                    } else {
                        final InputColumn<?> firstRelevantSourceColumn =
                                getFirstRelevantSourceColumn(sourceColumns, inputProperty);
                        if (firstRelevantSourceColumn != null) {
                            componentBuilder.setConfiguredProperty(inputProperty, firstRelevantSourceColumn);
                        }
                    }
                    _actions.showConfigurationDialog(componentBuilder);

                    // returning true to indicate a change
                    logger.debug("createLink(...) returning true - input column(s) added");
                    return true;
                } catch (final Exception e) {
                    // nothing to do
                    logger.info("Failed to add input columns ({}) to {}", sourceColumns.size(), componentBuilder, e);
                }
            } else if (filterOutcomes != null && !filterOutcomes.isEmpty()) {
                final JPopupMenu popup = new JPopupMenu();
                for (final FilterOutcome filterOutcome : filterOutcomes) {
                    final JMenuItem menuItem =
                            WidgetFactory.createMenuItem(filterOutcome.getSimpleName(), IconUtils.FILTER_OUTCOME_PATH);
                    menuItem.addActionListener(e -> {
                        if (scopeUpdatePermitted(sourceAnalysisJobBuilder, componentBuilder)) {
                            sourceAnalysisJobBuilder.moveComponent(componentBuilder);
                            addOrSetFilterOutcomeAsRequirement(componentBuilder, filterOutcome);
                        }
                    });
                    popup.add(menuItem);
                }
                popup.show(_graphContext.getVisualizationViewer(), mouseEvent.getX(), mouseEvent.getY());

                // we return false because no change was applied (yet)
                logger.debug("createLink(...) returning false - popup with choices presented to user");
                return false;
            }

            // When we can't do anything, at least show the dialog.
            _actions.showConfigurationDialog(componentBuilder);
        }
        logger.debug("createLink(...) returning false - no applicable action");
        return false;
    }

    /**
     * This will check if components are in a different scope, and ask the user
     * for permission to change the scope of the target component
     *
     * @return true if permitted or irrelevant, false if user refused a
     *         necessary scope change.
     */
    private boolean scopeUpdatePermitted(final AnalysisJobBuilder sourceAnalysisJobBuilder,
            final ComponentBuilder componentBuilder) {
        if (sourceAnalysisJobBuilder != componentBuilder.getAnalysisJobBuilder()) {
            if (componentBuilder.getInput().length > 0 || componentBuilder.getComponentRequirement() != null) {
                final String scopeText;
                scopeText = LabelUtils.getScopeLabel(sourceAnalysisJobBuilder);
                final int response = JOptionPane.showConfirmDialog(_graphContext.getVisualizationViewer(),
                        "This will move " + LabelUtils.getLabel(componentBuilder) + " into the " + scopeText
                                + ", thereby losing its configured columns and/or requirements", "Change scope?",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

                if (response == JOptionPane.CANCEL_OPTION) {
                    _graphContext.getJobGraph().refresh();
                    return false;
                }
            }
        }
        return true;
    }

    protected void addOrSetFilterOutcomeAsRequirement(final ComponentBuilder componentBuilder,
            final FilterOutcome filterOutcome) {
        final ComponentRequirement existingRequirement = componentBuilder.getComponentRequirement();
        if (existingRequirement == null) {
            // set a new requirement
            final ComponentRequirement requirement = new SimpleComponentRequirement(filterOutcome);
            componentBuilder.setComponentRequirement(requirement);
            return;
        }

        final ComponentRequirement defaultRequirement =
                componentBuilder.getAnalysisJobBuilder().getDefaultRequirement();
        if (existingRequirement.equals(defaultRequirement)) {
            // override the default requirement
            final ComponentRequirement requirement = new SimpleComponentRequirement(filterOutcome);
            componentBuilder.setComponentRequirement(requirement);
            return;
        }

        // add outcome to a compound requirement
        final CompoundComponentRequirement requirement =
                new CompoundComponentRequirement(existingRequirement, filterOutcome);
        componentBuilder.setComponentRequirement(requirement);
    }

    private InputColumn<?> getFirstRelevantSourceColumn(final List<? extends InputColumn<?>> sourceColumns,
            final ConfiguredPropertyDescriptor inputProperty) {
        assert inputProperty.isInputColumn();

        final Class<?> expectedDataType = inputProperty.getTypeArgument(0);
        for (final InputColumn<?> inputColumn : sourceColumns) {
            final Class<?> actualDataType = inputColumn.getDataType();
            if (ReflectionUtils.is(actualDataType, expectedDataType, false)) {
                return inputColumn;
            }
        }

        return null;
    }

    private Collection<? extends InputColumn<?>> getRelevantSourceColumns(
            final List<? extends InputColumn<?>> sourceColumns, final ConfiguredPropertyDescriptor inputProperty) {
        assert inputProperty.isInputColumn();

        final List<InputColumn<?>> result = new ArrayList<>();
        final Class<?> expectedDataType = inputProperty.getTypeArgument(0);
        for (final InputColumn<?> inputColumn : sourceColumns) {
            final Class<?> actualDataType = inputColumn.getDataType();
            if (ReflectionUtils.is(actualDataType, expectedDataType, false)) {
                result.add(inputColumn);
            }
        }

        return result;
    }

    private List<InputColumn<?>> getVisibleOutputColumns(final InputColumn<?>[] outputColumns) {
        final List<InputColumn<?>> visibleColumns = new ArrayList<>();
        for (int i = 0; i < outputColumns.length; i++) {
            if (outputColumns[i] instanceof MutableInputColumn) {
                final MutableInputColumn<?> mutableOutputColum = (MutableInputColumn<?>) outputColumns[i];
                if (!mutableOutputColum.isHidden()) {
                    visibleColumns.add(mutableOutputColum);
                }
            } else {
                visibleColumns.add(outputColumns[i]);
            }
        }
        return visibleColumns;
    }

    private void transformEdgeShape(final Point2D down, final Point2D out) {
        _edgeShape = new Line2D.Float(down, out);
    }

    private void transformArrowShape(final Point2D down, final Point2D out) {
        final float x1 = (float) down.getX();
        final float y1 = (float) down.getY();
        final float x2 = (float) out.getX();
        final float y2 = (float) out.getY();

        final AffineTransform xform = AffineTransform.getTranslateInstance(x2, y2);

        final float dx = x2 - x1;
        final float dy = y2 - y1;
        final float thetaRadians = (float) Math.atan2(dy, dx);
        xform.rotate(thetaRadians);
        _arrowShape = xform.createTransformedShape(GraphUtils.ARROW_SHAPE);
    }
}
