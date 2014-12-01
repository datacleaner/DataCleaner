package org.eobjects.datacleaner.widgets.visualization;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Set;

import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.LayoutDecorator;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.picking.PickedState;

public class JobGraphContext {

    private final VisualizationViewer<Object, JobGraphLink> _visualizationViewer;
    private final JobGraph _jobGraph;
    private final AnalysisJobBuilder _analysisJobBuilder;

    public JobGraphContext(JobGraph jobGraph, VisualizationViewer<Object, JobGraphLink> visualizationViewer,
            AnalysisJobBuilder analysisJobBuilder) {
        _jobGraph = jobGraph;
        _visualizationViewer = visualizationViewer;
        _analysisJobBuilder = analysisJobBuilder;
    }

    public VisualizationViewer<Object, JobGraphLink> getVisualizationViewer() {
        return _visualizationViewer;
    }

    public JobGraph getJobGraph() {
        return _jobGraph;
    }

    public AnalysisJobBuilder getAnalysisJobBuilder() {
        return _analysisJobBuilder;
    }

    public Object getVertex(MouseEvent me) {
        return getVertex(me.getPoint());
    }

    /**
     * Gets the vertex at a particular point, or null if it does not exist.
     * 
     * @param p
     * @return
     */
    public Object getVertex(Point2D p) {
        final GraphElementAccessor<?, ?> pickSupport = _visualizationViewer.getPickSupport();

        @SuppressWarnings("rawtypes")
        final Layout graphLayout = _visualizationViewer.getModel().getGraphLayout();

        @SuppressWarnings("unchecked")
        final Object vertex = pickSupport.getVertex(graphLayout, p.getX(), p.getY());

        return vertex;
    }

    public AbstractLayout<Object, JobGraphLink> getGraphLayout() {
        Layout<Object, JobGraphLink> layout = getVisualizationViewer().getGraphLayout();
        while (layout instanceof LayoutDecorator) {
            layout = ((LayoutDecorator<Object, JobGraphLink>) layout).getDelegate();
        }
        return (AbstractLayout<Object, JobGraphLink>) layout;
    }

    public Object getSelectedVertex() {
        final PickedState<Object> pickedVertexState = _visualizationViewer.getPickedVertexState();
        if (pickedVertexState == null) {
            return null;
        }
        final Set<Object> pickedVertices = pickedVertexState.getPicked();
        // ensure that there is 1 and only 1 selected vertex
        if (pickedVertices == null || pickedVertices.size() != 1) {
            return null;
        }
        return pickedVertices.iterator().next();
    }

}
